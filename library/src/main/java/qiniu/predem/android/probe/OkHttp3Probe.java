package qiniu.predem.android.probe;

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
import qiniu.predem.android.bean.LogBean;
import qiniu.predem.android.config.Configuration;
import qiniu.predem.android.http.ProbeResponse3;
import qiniu.predem.android.util.LogUtils;
import qiniu.predem.android.util.MatcherUtil;

import static qiniu.predem.android.probe.HttpURLConnProbe.reportMap;

/**
 * Created by Misty on 17/6/5.
 */
@Aspect
public class OkHttp3Probe {
    private static final String TAG = "OkHttp3Probe";

    @Around("call(* okhttp3.OkHttpClient+.newCall(..))")
    public Object onOkHttpNew(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!Configuration.httpMonitorEnable || joinPoint.getArgs().length != 1) {
            return joinPoint.proceed();
        }

        LogBean urlTraceRecord = LogBean.obtain();

        Object[] args = joinPoint.getArgs();
        Request request = (Request) args[0];

        //url
        URL url = request.url().url();
        //判断是否需要收集url信息
        if (GlobalConfig.isExcludeHost(url.getHost()) || ProbeWebClient.isExcludeIPs(url.getHost())) {//!(GlobalConfig.isIncludeHost(url.getHost()))
            return joinPoint.proceed();
        }
        urlTraceRecord.setMethod(request.method());
        urlTraceRecord.setStartTimestamp(System.currentTimeMillis());

        urlTraceRecord.setDomain(url.getHost());
        urlTraceRecord.setPath(url.getPath());

        // match ip
        Matcher matcher = MatcherUtil.IP_Pattern.matcher(url.getHost());
        if (matcher.find()) {
            if (!ProbeWebClient.isExcludeIPs(url.getHost())) {
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
            return joinPoint.proceed();
        }
    }

    @Around("call(* okhttp3.Response+.body(..))")
    public Object onOkHttpResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!Configuration.httpMonitorEnable) {
            return joinPoint.proceed();
        }

        Response response = (Response) joinPoint.getTarget();
        HttpUrl url = response.request().url();
        try {
            synchronized (reportMap) {
                if (reportMap.containsKey(url.url().toString())) {
                    LogBean urlTraceRecord = reportMap.get(url.toString());
                    reportMap.remove(url.toString());
                    if (GlobalConfig.isExcludeHost(urlTraceRecord.getDomain()) || ProbeWebClient.isExcludeIPs(urlTraceRecord.getDomain())) {//!GlobalConfig.isIncludeHost(urlTraceRecord.getDomain())
                        return joinPoint.proceed();
                    }
                    if (Configuration.dnsEnable) {
                        long s = System.currentTimeMillis();
                        String ip = InetAddress.getByName(response.request().url().host()).getHostAddress();
                        urlTraceRecord.setHostIP(ip);
                        urlTraceRecord.setDnsTime(System.currentTimeMillis() - s);
                    } else {
                        urlTraceRecord.setHostIP("-");
                        urlTraceRecord.setDnsTime(0);
                    }

                    urlTraceRecord.setResponseTimestamp(System.currentTimeMillis());
                    urlTraceRecord.setStatusCode(response.code());
                    urlTraceRecord.setNetworkErrorCode(response.networkResponse().code());
                    urlTraceRecord.setNetworkErrorMsg(response.networkResponse().message());
                    return ProbeResponse3.obtain((ResponseBody) joinPoint.proceed(), urlTraceRecord);//ProbeInputStream.obtain((InputStream) joinPoint.proceed(), urlTraceRecord);
                } else {
                    LogBean urlTraceRecord = LogBean.obtain();
                    if (Configuration.dnsEnable) {
                        long s = System.currentTimeMillis();
                        String ip = InetAddress.getByName(response.request().url().host()).getHostAddress();
                        urlTraceRecord.setHostIP(ip);
                        urlTraceRecord.setDnsTime(System.currentTimeMillis() - s);
                    } else {
                        urlTraceRecord.setHostIP("-");
                        urlTraceRecord.setDnsTime(0);
                    }

                    urlTraceRecord.setDomain(url.host());
                    urlTraceRecord.setPath(url.url().getPath());
                    urlTraceRecord.setResponseTimestamp(System.currentTimeMillis());
                    urlTraceRecord.setStatusCode(response.code());
                    urlTraceRecord.setNetworkErrorCode(response.networkResponse().code());
                    urlTraceRecord.setNetworkErrorMsg(response.networkResponse().message());
                    return ProbeResponse3.obtain((ResponseBody) joinPoint.proceed(), urlTraceRecord);//ProbeInputStream.obtain((InputStream) joinPoint.proceed(), urlTraceRecord);
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
            return joinPoint.proceed();
        }
    }
}
