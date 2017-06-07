package qiniu.presniff.library.probe;

import android.util.Log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.regex.Matcher;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Request;
import qiniu.presniff.library.DEMManager;
import qiniu.presniff.library.bean.LogBean;
import qiniu.presniff.library.config.GlobalConfig;
import qiniu.presniff.library.http.MySSLSocketFactory;
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

    @Around("call(* okhttp3.OkHttpClient.*(..))")
//    @Around("call(* okhttp3.OkHttpClient.new(..))")
    public Object onOkHttpNew(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!DEMManager.isHttpMonitorEnable() || joinPoint.getArgs().length != 1) {
            return joinPoint.proceed();
        }
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
        LogUtils.d(TAG,"------>host : " + url.getHost());
        Matcher matcher = PatternUtil.IP_Pattern.matcher(url.getHost());
        LogUtils.d(TAG,"------>matcher : " + matcher.find());
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
    }

    @Around("call(* okhttp3.Response+.*(..))")
    public Object onOkHttpResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!DEMManager.isHttpMonitorEnable()) {
            return joinPoint.proceed();
        }

        LogUtils.d(TAG,"------" + joinPoint.getTarget().toString());

        Object[] args = joinPoint.getArgs();
        for (int i = 0 ;i < args.length; i++){
            Log.d(TAG,"-------目标方法参数:"+args[i]);
        }
        return joinPoint.proceed();
    }
}
