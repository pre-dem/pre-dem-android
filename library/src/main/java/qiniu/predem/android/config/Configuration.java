package qiniu.predem.android.config;

import android.content.Context;

import qiniu.predem.android.bean.AppBean;

/**
 * Created by Misty on 17/6/15.
 */

public final class Configuration {
    public static final String HTTP_MONITOR_ENABLE = "http_monitor_enabled";
    public static final String CRASH_REPORT_ENABLE = "crash_report_enabled";
    public static final String LAG_MONITOR_ENABLE = "lag_monitor_enabled";
    public static final String WEBVIEW_ENABLE = "webview_enable";
    public static final long DEFAULT_TIMEOUT = 5000;
    private static final String TAG = "Configuration";
    public static String appKey = null;
    public static String domain = null;
    public static boolean httpMonitorEnable = true;//http上报
    public static boolean webviewEnable = true;//webview上报
    public static boolean crashReportEnable = true;//crash上报
    public static boolean lagMonitorEnable = true;//view卡顿上报
    public static boolean dnsEnable = true; //默认是否使用dns
    private static String scheme = "http://";

    public static void init(Context context, String appKey, String domain) {
        if (context == null) {
            return;
        }

        Configuration.appKey = appKey;
        Configuration.domain = domain;
        //初始化 app 信息
        AppBean.loadFromContext(context);
        //初始化文件路径
        FileConfig.loadFilesPath(context);
    }

    private static String getAppId(String appKey) {
        if (appKey == null || appKey.length() < 8){
            return "";
        }
        return appKey.substring(0, 8);
    }

    public static void setScheme(String s) {
        scheme = s;
    }

    private static String baseUrl() {
        if (domain.startsWith("http") || domain.startsWith("https")){
            return domain + "/v1/" + getAppId(appKey);
        }else{
            return scheme + domain + "/v1/" + getAppId(appKey);
        }
    }

    public static String getConfigUrl() {
        return baseUrl() + "/app-config/a";
    }

    public static String getHttpUrl() {
        return baseUrl() + "/http-stats/a";
    }

    public static String getCrashUrl() {
        return baseUrl() + "/crashes/a";
    }

    public static String getLagMonitorUrl() {
        return baseUrl() + "/lag-monitor/a";
    }

    public static String getDiagnosisUrl() {
        return baseUrl() + "/net-diags/a";
    }

    public static String getEventUrl() {
        return baseUrl() + "/events";
    }

    public static String getLagMonitorUpToken() {
        return baseUrl() + "/lag-report-token/a";
    }

    public static String getCrashUpToken() {
        return baseUrl() + "/crash-report-token/a";
    }

    public static String getLogcatUpToken() {
        return baseUrl() + "/log-capture-token/a";
    }

    public static String getLogcatUrl() {
        return baseUrl() + "/log-capture/a";
    }
}
