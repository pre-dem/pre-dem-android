package qiniu.predem.android.probe;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import okio.BufferedSource;
import qiniu.predem.android.bean.LogBean;
import qiniu.predem.android.bean.RespBean;
import qiniu.predem.android.config.Configuration;
import qiniu.predem.android.http.ProbeBufferedSource;
import qiniu.predem.android.util.LogUtils;
import qiniu.predem.android.util.MatcherUtil;

/**
 * Created by Misty on 17/6/19.
 */
@Aspect
public class OkHttp2Probe {
    private static final String TAG = "OkHttp2Probe";

    protected static final HashMap<ResponseBody, Response> RespBodyToRespMap = new HashMap<>();
    protected static final HashMap<String, Long> dnsTimeMap = new HashMap<>();
    protected static final HashMap<String, String> DomainToIpMap = new HashMap<>();

    protected static final List<RespBean> startTimeStamp = new ArrayList<>();

    @Around("call(* com.squareup.okhttp.Dns+.lookup(..))")
    public Object onOkHttpDnsLookup(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!Configuration.httpMonitorEnable || !Configuration.dnsEnable){
            return joinPoint.proceed();
        }
        try {
            if (joinPoint.getArgs().length != 1) {
                return joinPoint.proceed();
            }
            String hostName = (String) joinPoint.getArgs()[0];

            // exclude url
            if (GlobalConfig.isExcludeHost(hostName)) {
                return joinPoint.proceed();
            }

            long stime = System.currentTimeMillis();
            Object result;
            try {
                result = joinPoint.proceed();
            } catch (UnknownHostException e) {
                LogUtils.e(TAG,"-----networkError " + e.toString());
                return joinPoint.proceed();
            }
            long dnsTime = System.currentTimeMillis() - stime;
            synchronized (dnsTimeMap) {
                if (!dnsTimeMap.containsKey(hostName)){
                    dnsTimeMap.put(hostName, dnsTime);
                }
            }
            if (result instanceof List && ((List)result).get(0) instanceof InetAddress) {
                DomainToIpMap.put(((List<InetAddress>) result).get(0).getHostName(),
                        ((List<InetAddress>) result).get(0).getHostAddress());
            }
            return result;
        } catch (Throwable e) {
            LogUtils.e(TAG,"-----networkError " + e.toString());
            return joinPoint.proceed();
        }
    }

    @Around("call(* java.net.InetSocketAddress+.createUnresolved(..))")
    public Object onSocketAddressResolve(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!Configuration.httpMonitorEnable || !Configuration.dnsEnable) {
            return joinPoint.proceed();
        }
        try {
            if (joinPoint.getArgs().length != 2) {
                return joinPoint.proceed();
            }
            String domain = (String) joinPoint.getArgs()[0];

            // exclude url
            if (GlobalConfig.isExcludeHost(domain)){
                return joinPoint.proceed();
            }

            // match ip
            Matcher matcher = MatcherUtil.IP_Pattern.matcher(domain);
            if (matcher.find()) {
                return joinPoint.proceed();
            } else {
                long stime = System.currentTimeMillis();
                Object result = joinPoint.proceed();
                synchronized (dnsTimeMap) {
                    dnsTimeMap.put(domain, System.currentTimeMillis() - stime);
                }
                return result;
            }
        } catch (Throwable e) {
            LogUtils.e(TAG,"-----networkError " + e.toString());
            return joinPoint.proceed();
        }
    }

    @Around("call(* com.squareup.okhttp.Response.Builder+.build(..))")
    public Object onOkHttpRespBuild(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!Configuration.httpMonitorEnable) {
            return joinPoint.proceed();
        }
        Object result = joinPoint.proceed();
        if (result instanceof Response) {
            Response resp = (Response) result;
            RespBodyToRespMap.put(resp.body(), resp);
        }
        return result;
    }

    @Around("call(* com.squareup.okhttp.OkHttpClient+.newCall(..))")
    public Object onOkHttpNew(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!Configuration.httpMonitorEnable || joinPoint.getArgs().length != 1) {
            return joinPoint.proceed();
        }
        Object[] args = joinPoint.getArgs();
        Request request = (Request) args[0];

        //url
        URL url = request.url();
        if (GlobalConfig.isExcludeHost(url.getHost())){
            return joinPoint.proceed();
        }
        RespBean bean = new RespBean();
        bean.setUrl(url.toString());
        bean.setStartTimestamp(System.currentTimeMillis());
        startTimeStamp.add(bean);
        return joinPoint.proceed();
    }

    @Around("call(* com.squareup.okhttp.ResponseBody+.source(..))")
    public Object onOkHttpRespBodySource(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!Configuration.httpMonitorEnable) {
            return joinPoint.proceed();
        }

        Object result = joinPoint.proceed();
        if (result instanceof BufferedSource && joinPoint.getTarget() instanceof ResponseBody) {
            ResponseBody respBody = (ResponseBody) joinPoint.getTarget();

            if (RespBodyToRespMap.containsKey(respBody)) {

                URL url = RespBodyToRespMap.get(respBody).request().url();
                String hostName = url.getHost();

                LogUtils.d(TAG,"-----okhttp url " + url.toString());

                // exclude host
                if (GlobalConfig.isExcludeHost(hostName)){
                    return result;
                }

                Response resp = RespBodyToRespMap.get(respBody);
                RespBodyToRespMap.remove(respBody);

                String location =  resp.header("Location");
                LogUtils.d(TAG,"-----location " + location);
                if (location != null){
                    for (int i = 0 ;i < startTimeStamp.size(); i++){
                        RespBean bean = startTimeStamp.get(i);
                        if (bean.getUrl().equals(url.toString())){
                            bean.setUrl(location);
                            startTimeStamp.set(i,bean);
                            break;
                        }
                    }
                }

                if (resp != null) {
                    synchronized (dnsTimeMap) {
                        LogBean urlTraceRecord = LogBean.obtain();
                        urlTraceRecord.setDomain(hostName);
                        urlTraceRecord.setPath(url.getPath());
                        urlTraceRecord.setMethod(resp.request().method());
                        urlTraceRecord.setStatusCode(resp.code());
                        urlTraceRecord.setResponseTimestamp(System.currentTimeMillis());
                        for (int i = 0 ;i < startTimeStamp.size(); i++){
                            RespBean bean = startTimeStamp.get(i);
                            if (bean.getUrl().equals(url.toString())){
                                urlTraceRecord.setStartTimestamp(bean.getStartTimestamp());
//                                        startTimeStamp.remove(i);
                                break;
                            }
                        }
                        if (dnsTimeMap.containsKey(hostName)) {
                            if (DomainToIpMap.containsKey(hostName)) {
                                urlTraceRecord.setHostIP(DomainToIpMap.get(hostName));
                            }
                            urlTraceRecord.setDnsTime(dnsTimeMap.get(hostName));
                        } else {
                            urlTraceRecord.setHostIP("-");
                            urlTraceRecord.setDnsTime(0);
                        }

                        if (result instanceof ProbeBufferedSource) {
                            return result;
                        } else {
                            return ProbeBufferedSource.obtain((BufferedSource) result, urlTraceRecord);
                        }
                    }
                }
            }
        }
        return result;
    }
}
