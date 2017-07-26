package qiniu.predem.android.probe;

import android.annotation.SuppressLint;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

import javax.net.ssl.HttpsURLConnection;

import qiniu.predem.android.bean.LogBean;
import qiniu.predem.android.config.Configuration;
import qiniu.predem.android.http.MySSLSocketFactory;
import qiniu.predem.android.http.ProbeInputStream;
import qiniu.predem.android.util.LogUtils;
import qiniu.predem.android.util.MatcherUtil;

/**
 * Created by Misty on 17/6/15.
 */

public class ProbeWebClient extends WebViewClient {
    protected static final Set<String> excludeIPs = new HashSet<>();
    private static final String TAG = "ProbeWebClient";

    public static boolean isExcludeIPs(String ip) {
        return excludeIPs.contains(ip);
    }

    public static final ProbeWebClient instance = new ProbeWebClient();

    protected ProbeWebClient() {}

    @SuppressLint("NewApi")
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        if (request != null && request.getUrl() != null) {
            try {
                if (!GlobalConfig.isExcludeHost(request.getUrl().getHost())) {
                    return getResponseFromUrl(new URL(request.getUrl().toString()));
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (url != null) {
            String domain = null;
            int sindex = url.indexOf("://");
            if (sindex > 0) {
                int eindex = url.indexOf(":", sindex);
                if (eindex < 0) eindex = url.indexOf("/");
                else {
                    if (url.indexOf("/", sindex) > 0)
                        eindex = eindex > url.indexOf("/", sindex) ? url.indexOf("/", sindex) : eindex;
                }
                if (eindex < 0) eindex = url.length();
                domain = url.substring(sindex, eindex);
            }
            if (domain != null && !GlobalConfig.isExcludeHost(domain)) {
                try {
                    if (!GlobalConfig.isExcludeHost(domain)) {
                        return getResponseFromUrl(new URL(url));
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    protected WebResourceResponse getResponseFromUrl(URL url) throws Throwable {
        String proto = url.getProtocol();
        if (proto.equalsIgnoreCase("http") || proto.equalsIgnoreCase("https")) {
            LogBean record = LogBean.obtain();
            record.setDomain(url.getHost());
            record.setPath(url.getPath());

            if (Configuration.dnsEnable) {
                Matcher matcher = MatcherUtil.IP_Pattern.matcher(url.getHost());
                if (matcher.find()) {
                    String hostIp = matcher.group();
                    if (excludeIPs.contains(hostIp)) return null;
                    record.setHostIP(hostIp);
                    record.setDnsTime(-1);
                } else {
                    long now = System.currentTimeMillis();
                    String hostIp = InetAddress.getByName(url.getHost()).getHostAddress();
                    excludeIPs.add(hostIp);
                    record.setDnsTime(System.currentTimeMillis() - now);
                    record.setHostIP(hostIp);
                }
                record.setStartTimestamp(System.currentTimeMillis());
            }

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if (url.toString().startsWith("https://") && record.getHostIP() != null && record.getHostIP().length() > 0) {
                ((HttpsURLConnection) conn).setSSLSocketFactory(new MySSLSocketFactory(record.getHostIP()));
            }

            if (Configuration.dnsEnable) {
                conn.setRequestProperty("Host", url.getHost());
            }

            String contentType = conn.getContentType();
            if (contentType != null) {
                Matcher matcher = MatcherUtil.MIMETYPE_Pattern.matcher(contentType);
                if (matcher.find()) {
                    contentType = matcher.group();
                }
            }

            InputStream ins = conn.getInputStream();
            if (ins instanceof ProbeInputStream){
                ins = ((ProbeInputStream) ins).getSource();
            }

            if (conn.getResponseCode() / 100 == 3) {
                return null;
            }

            record.setStatusCode(conn.getResponseCode());
            record.setResponseTimestamp(System.currentTimeMillis());
            record.setDataLength(conn.getContentLength());
            record.setMethod(conn.getRequestMethod());

            WebResourceResponse wrr = new WebResourceResponse(contentType, conn.getContentEncoding(),
                    ProbeInputStream.obtain(ins, record));
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
                wrr.setStatusCodeAndReasonPhrase(conn.getResponseCode(), conn.getResponseMessage());
            }
            record.setEndTimestamp(System.currentTimeMillis());
            return wrr;
        }
        return null;
    }
}
