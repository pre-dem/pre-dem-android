package qiniu.predem.android.probe;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.regex.Matcher;

import javax.net.ssl.HttpsURLConnection;

import qiniu.predem.android.bean.LogBean;
import qiniu.predem.android.config.Configuration;
import qiniu.predem.android.http.MySSLSocketFactory;
import qiniu.predem.android.http.ProbeInputStream;
import qiniu.predem.android.util.LogUtils;
import qiniu.predem.android.util.MatcherUtil;

/**
 * Created by Misty on 5/18/17.
 */
@Aspect
public final class HttpURLConnProbe {
    protected static final HashMap<Object, LogBean> reportMap = new HashMap<>();
    private static final String TAG = "HttpURLConnProbe";

    @Around("call(* java.net.URL+.openConnection(..))")
    public Object onHttpURLOpenConnect(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!Configuration.httpMonitorEnable || joinPoint.getArgs().length != 0) {
            return joinPoint.proceed();
        }

        try {
            if (!joinPoint.getTarget().toString().startsWith("http://") && !joinPoint.getTarget().toString().startsWith("https://")) {
                return joinPoint.proceed();
            }

            URL url = (URL) joinPoint.getTarget();

            //判断是否需要收集url信息
            if (GlobalConfig.isExcludeHost(url.getHost()) || ProbeWebClient.isExcludeIPs(url.getHost())) {
                return joinPoint.proceed();
            }

            HttpURLConnection conn;

            LogBean bean = LogBean.obtain();
            bean.setStartTimestamp(System.currentTimeMillis());
            conn = (HttpURLConnection) joinPoint.proceed();
            // 判断host是否是IP
            Matcher matcher = MatcherUtil.IP_Pattern.matcher(url.getHost());
            if (matcher.find()) {
                bean.setHostIP(url.getHost());
                bean.setDnsTime(0);
                bean.setDomain(url.getHost());
                bean.setPath(url.getPath());
            } else {
                String hostIp = null;
                if (Configuration.dnsEnable) {
                    long stime = System.currentTimeMillis();
                    try {
                        bean.setHostIP(InetAddress.getByName(url.getHost()).getHostAddress());
                    } catch (UnknownHostException e) {
                        throw e;
                    }
                    bean.setDnsTime(System.currentTimeMillis() - stime);
                    bean.setDomain(url.getHost());
                    bean.setPath(url.getPath());

                    //302跳转
                    conn.setInstanceFollowRedirects(false);
                    //https请求
                    if (url.toString().startsWith("https://")) {
                        ((HttpsURLConnection) conn).setSSLSocketFactory(new MySSLSocketFactory(hostIp));
                    }
                } else {
                    return joinPoint.proceed();
                }
                synchronized (reportMap) {
                    reportMap.put(conn.hashCode(), bean);
                }
            }
            return conn;
        } catch (Exception e) {
            LogUtils.e(TAG, "-----" + e.toString());
            return joinPoint.proceed();
        }
    }

    @Around("call(* java.net.URLConnection+.getInputStream(..))")
    public Object onHttpURLConnectInput(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!Configuration.httpMonitorEnable) {
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

            if (GlobalConfig.isExcludeHost(url.getHost()) || ProbeWebClient.isExcludeIPs(url.getHost())) {
                return joinPoint.proceed();
            }

            try {
                synchronized (reportMap) {
                    int key = conn.hashCode();
                    if (reportMap.containsKey(key)) {
                        LogBean urlTraceRecord = reportMap.get(key);
                        reportMap.remove(key);
                        if (GlobalConfig.isExcludeHost(urlTraceRecord.getDomain()) || ProbeWebClient.isExcludeIPs(urlTraceRecord.getDomain())) {
                            return joinPoint.proceed();
                        }

                        urlTraceRecord.setResponseTimestamp(System.currentTimeMillis());
                        urlTraceRecord.setStatusCode(((HttpURLConnection) conn).getResponseCode());
                        return ProbeInputStream.obtain((InputStream) joinPoint.proceed(), urlTraceRecord);
                    } else {
                        LogBean urlTraceRecord = LogBean.obtain();
                        urlTraceRecord.setDomain(url.getHost());
                        urlTraceRecord.setPath(url.getPath());
                        urlTraceRecord.setMethod(((HttpURLConnection) conn).getRequestMethod());
                        urlTraceRecord.setDnsTime(0);
                        urlTraceRecord.setResponseTimestamp(System.currentTimeMillis());
                        urlTraceRecord.setHostIP("-");
                        urlTraceRecord.setStatusCode(((HttpURLConnection) conn).getResponseCode());
                        return ProbeInputStream.obtain((InputStream) joinPoint.proceed(), urlTraceRecord);
                    }
                }
            } catch (Exception e) {
                LogUtils.e(TAG, e.toString());
                return joinPoint.proceed();
            }
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
            return joinPoint.proceed();
        }
    }
}
