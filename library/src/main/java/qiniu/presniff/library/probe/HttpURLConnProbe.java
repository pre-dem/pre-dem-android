package qiniu.presniff.library.probe;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.regex.Matcher;

import javax.net.ssl.HttpsURLConnection;

import qiniu.presniff.library.DEMManager;
import qiniu.presniff.library.bean.LogBean;
import qiniu.presniff.library.config.GlobalConfig;
import qiniu.presniff.library.http.MySSLSocketFactory;
import qiniu.presniff.library.http.ProbeInputStream;
import qiniu.presniff.library.http.ProbeWebClient;
import qiniu.presniff.library.util.LogUtils;
import qiniu.presniff.library.util.PatternUtil;

/**
 * Created by Misty on 5/18/17.
 */
@Aspect
public class HttpURLConnProbe {
    private static final String TAG = "HttpURLConnProbe";

    protected static final HashMap<Object, LogBean> reportMap = new HashMap<>();
//    protected static final Set<String> ExcludeIPs = new HashSet<>();

    @Pointcut("call(* java.net.URL+.openConnection(..))")
    public void callOpenConnection(){}

    @Pointcut("call(* java.net.HttpURLConnection+.setRequestMethod(..))")
    public void callRequestMethod(){}

    @Around("callOpenConnection() || callRequestMethod()")
    public Object onHttpURLOpenConnect(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!DEMManager.isHttpMonitorEnable() || joinPoint.getArgs().length > 1){
            return joinPoint.proceed();
        }
        LogBean urlTraceRecord = LogBean.obtain();

        if (joinPoint.getArgs().length == 1){
            //获取method
            String method = joinPoint.getArgs()[0].toString();
            urlTraceRecord.setMethod(method);
            return joinPoint.proceed();
        }else if (joinPoint.getArgs().length == 0){
            try {
                if (!joinPoint.getTarget().toString().startsWith("http://") && !joinPoint.getTarget().toString().startsWith("https://")){
                    return joinPoint.proceed();
                }
                URL url = (URL) joinPoint.getTarget();
                urlTraceRecord.setStartTimestamp(System.currentTimeMillis());

                //判断是否需要收集url信息
                if (GlobalConfig.isExcludeHost(url.getHost()) || !(GlobalConfig.isIncludeHost(url.getHost())) || ProbeWebClient.isExcludeIPs(url.getHost())) {
                    return joinPoint.proceed();
                }

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
                }else {
                    String ipUrl;
                    URLConnection conn;
                    if (DEMManager.isDns()) {
                        long stime = System.currentTimeMillis();
                        try {
                            urlTraceRecord.setHostIP(InetAddress.getByName(url.getHost()).getHostAddress());
                        } catch (UnknownHostException e) {
                            throw e;
                        }
                        urlTraceRecord.setDnsTime(System.currentTimeMillis() - stime);
//                        ExcludeIPs.add(urlTraceRecord.getHostIP());
                        //https请求
                        if (url.toString().startsWith("https://")) {
                            ipUrl = url.toString();
                            conn = (URLConnection) joinPoint.proceed();
                            conn.setRequestProperty("Host", url.getHost());
                            ((HttpsURLConnection) conn).setSSLSocketFactory(new MySSLSocketFactory(urlTraceRecord.getHostIP()));
                        } else {
                            //http请求
                            ipUrl = url.toString().replaceFirst(url.getHost(), urlTraceRecord.getHostIP());
                            conn = new URL(ipUrl).openConnection();
                            conn.setRequestProperty("Host", url.getHost());
                        }
                    } else {
                        urlTraceRecord.setDnsTime(-1);
                        ipUrl = url.toString();
                        conn = (URLConnection) joinPoint.proceed();
                    }
                    synchronized (reportMap) {
                        reportMap.put(ipUrl, urlTraceRecord);
                    }
                    return conn;
                }
            }catch (Exception e){
                LogUtils.e(TAG,e.toString());
                return joinPoint.proceed();
            }
        }else{
            return joinPoint.proceed();
        }
    }

    @Around("call(* java.net.URLConnection+.getInputStream(..))")
    public Object onHttpURLConnectInput(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!DEMManager.isHttpMonitorEnable()) {
            return joinPoint.proceed();
        }
        try {
            URLConnection conn = (URLConnection) joinPoint.getTarget();

            URL url;
            if (conn instanceof HttpURLConnection) {
                url = ((HttpURLConnection) joinPoint.getTarget()).getURL();
            } else {
                return joinPoint.proceed();
            }

            if (GlobalConfig.isExcludeHost(url.getHost()) || ProbeWebClient.isExcludeIPs(url.getHost())){
                return joinPoint.proceed();
            }

            try {
                synchronized (reportMap) {
                    if (reportMap.containsKey(url.toString())){
                        LogBean urlTraceRecord = reportMap.get(url.toString());
                        reportMap.remove(url.toString());
                        if (GlobalConfig.isExcludeHost(urlTraceRecord.getDomain()) || !GlobalConfig.isIncludeHost(urlTraceRecord.getDomain()) || ProbeWebClient.isExcludeIPs(urlTraceRecord.getDomain())) {
                            return joinPoint.proceed();
                        }

                        urlTraceRecord.setResponseTimestamp(System.currentTimeMillis());
                        urlTraceRecord.setStatusCode(((HttpURLConnection) conn).getResponseCode());
                        return ProbeInputStream.obtain((InputStream) joinPoint.proceed(), urlTraceRecord);
                    }else {
                        // exclude url
                        if (!GlobalConfig.isIncludeHost(url.getHost())) return joinPoint.proceed();

                        LogBean urlTraceRecord = LogBean.obtain();
                        urlTraceRecord.setDomain(url.getHost());
                        urlTraceRecord.setPath(url.getPath());
                        urlTraceRecord.setDnsTime(-1);
                        urlTraceRecord.setResponseTimestamp(System.currentTimeMillis());
                        urlTraceRecord.setHostIP("");
                        urlTraceRecord.setStatusCode(((HttpURLConnection) conn).getResponseCode());
                        return ProbeInputStream.obtain((InputStream) joinPoint.proceed(), urlTraceRecord);
                    }
                }
            }catch (Exception e){
                LogUtils.e(TAG, e.toString());
                return joinPoint.proceed();
            }
        }catch (Exception e){
            LogUtils.e(TAG,e.toString());
            return joinPoint.proceed();
        }
    }
}
