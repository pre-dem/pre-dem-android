package qiniu.presniff.library.probe;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.net.InetAddress;
import java.net.URL;
import java.util.regex.Matcher;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import qiniu.presniff.library.DEMManager;
import qiniu.presniff.library.bean.LogBean;
import qiniu.presniff.library.config.GlobalConfig;
import qiniu.presniff.library.http.ProbeResponse;
import qiniu.presniff.library.http.ProbeWebClient;
import qiniu.presniff.library.util.LogUtils;
import qiniu.presniff.library.util.PatternUtil;

import static qiniu.presniff.library.probe.HttpURLConnProbe.reportMap;

/**
 * Created by Misty on 17/6/5.
 */
@Aspect
public class OkHttpProbe {
    private static final String TAG = "OkHttpProbe";

    @Around("call(* okhttp3.OkHttpClient+.newCall(..))")
//    @Around("call(* okhttp3.OkHttpClient.new(..))")
    public Object onOkHttpNew(ProceedingJoinPoint joinPoint) throws Throwable {
//        if (!DEMManager.isHttpMonitorEnable() || joinPoint.getArgs().length != 1) {
//            return joinPoint.proceed();
//        }
        LogBean urlTraceRecord = LogBean.obtain();

        Object[] args = joinPoint.getArgs();
        Request request = (Request) args[0];

        //url
        URL url = request.url().url();
        //判断是否需要收集url信息
        if (GlobalConfig.isExcludeHost(url.getHost()) || !(GlobalConfig.isIncludeHost(url.getHost())) || ProbeWebClient.isExcludeIPs(url.getHost())) {
            return joinPoint.proceed();
        }
        urlTraceRecord.setMethod(request.method());
        urlTraceRecord.setStartTimestamp(System.currentTimeMillis());

        urlTraceRecord.setDomain(url.getHost());
        urlTraceRecord.setPath(url.getPath());

        // match ip
        Matcher matcher = PatternUtil.IP_Pattern.matcher(url.getHost());
        if (matcher.find()) {
            if (!ProbeWebClient.isExcludeIPs(url.getHost()) && GlobalConfig.isIncludeHost(url.getHost())) {
                urlTraceRecord.setHostIP(url.getHost());
                urlTraceRecord.setDnsTime(0);
                synchronized (reportMap) {
                    reportMap.put(url.toString(), urlTraceRecord);
                }
            }
            return joinPoint.proceed();
        } else {
            String ipUrl = url.toString();
            synchronized (reportMap) {
                reportMap.put(ipUrl, urlTraceRecord);
            }
            LogUtils.d(TAG,"------joinPoint.proceed()" + urlTraceRecord.toJsonString());
            return joinPoint.proceed();
        }
    }

    @Around("call(* okhttp3.Response+.body(..))")
    public Object onOkHttpResponse(ProceedingJoinPoint joinPoint) throws Throwable {
//        if (!DEMManager.isHttpMonitorEnable()) {
//            return joinPoint.proceed();
//        }

        Response response = (Response) joinPoint.getTarget();
        HttpUrl url = response.request().url();
        try {
            synchronized (reportMap) {
                if (reportMap.containsKey(url.url().toString())){
                    LogBean urlTraceRecord = reportMap.get(url.toString());
                    reportMap.remove(url.toString());
                    if (GlobalConfig.isExcludeHost(urlTraceRecord.getDomain()) || !GlobalConfig.isIncludeHost(urlTraceRecord.getDomain()) || ProbeWebClient.isExcludeIPs(urlTraceRecord.getDomain())) {
                        return joinPoint.proceed();
                    }
                    if (DEMManager.isDns()){
                        long s = System.currentTimeMillis();
                        String ip = InetAddress.getByName(response.request().url().host()).getHostAddress();
                        urlTraceRecord.setHostIP(ip);
                        urlTraceRecord.setDnsTime(System.currentTimeMillis() - s);
                    }else{
                        urlTraceRecord.setHostIP("-");
                        urlTraceRecord.setDnsTime(-1);
                    }

                    urlTraceRecord.setResponseTimestamp(System.currentTimeMillis());
                    urlTraceRecord.setStatusCode(response.code());
                    urlTraceRecord.setNetworkErrorCode(response.networkResponse().code());
                    urlTraceRecord.setNetworkErrorMsg(response.networkResponse().message());
                    return ProbeResponse.obtain((ResponseBody)joinPoint.proceed(), urlTraceRecord);//ProbeInputStream.obtain((InputStream) joinPoint.proceed(), urlTraceRecord);
                }else {
                    // exclude url
                    if (!GlobalConfig.isIncludeHost(url.host())){
                        return joinPoint.proceed();
                    }
//
                    LogBean urlTraceRecord = LogBean.obtain();
                    if (DEMManager.isDns()){
                        long s = System.currentTimeMillis();
                        String ip = InetAddress.getByName(response.request().url().host()).getHostAddress();
                        urlTraceRecord.setHostIP(ip);
                        urlTraceRecord.setDnsTime(System.currentTimeMillis() - s);
                    }else{
                        urlTraceRecord.setHostIP("-");
                        urlTraceRecord.setDnsTime(-1);
                    }

                    urlTraceRecord.setDomain(url.host());
                    urlTraceRecord.setPath(url.url().getPath());
                    urlTraceRecord.setResponseTimestamp(System.currentTimeMillis());
                    urlTraceRecord.setStatusCode(response.code());
                    urlTraceRecord.setNetworkErrorCode(response.networkResponse().code());
                    urlTraceRecord.setNetworkErrorMsg(response.networkResponse().message());
                    return ProbeResponse.obtain((ResponseBody)joinPoint.proceed(), urlTraceRecord);//ProbeInputStream.obtain((InputStream) joinPoint.proceed(), urlTraceRecord);
                }
            }
        }catch (Exception e){
            LogUtils.e(TAG, e.toString());
            return joinPoint.proceed();
        }
    }
}
