package qiniu.predem.android.config;

import android.content.Context;

import qiniu.predem.android.bean.AppBean;

/**
 * Created by Misty on 17/6/15.
 */

public final class Configuration {
    private static final String TAG = "Configuration";

    public static final String HTTP_MONITOR_ENABLE = "http_monitor_enabled";
    public static final String CRASH_REPORT_ENABLE = "crash_report_enabled";
    public static final String DEVICE_SYMBOLICATION_ENABLE = "on_device_symbolication_enabled";
    public static final String LAG_MONITOR_ENABLE = "lag_monitor_enable";
    public static final String WEBVIEW_ENABLE = "webview_enable";

    public static final long DEFAULT_TIMEOUT = 5000;
    public static String appKey = null;
    public static String domain = null;
    private static String scheme = "http://";

    public static boolean httpMonitorEnable = true;//http上报
    public static boolean webviewEnable = true;//webview上报
    public static boolean crashReportEnable = true;//crash上报
    public static boolean symbilicationEnable = true;//自定义数据上报
    public static boolean networkDiagnosis = true; //网络诊断上报
    public static boolean dnsEnable = true; //默认是否使用dns

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
        return appKey.substring(0, 8);
    }

    public static void setScheme(String s) {
        scheme = s;
    }

    private static String baseUrl() {
        return scheme + domain + "/v1/" + getAppId(appKey);
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

    public static String getDiagnosisUrl() {
        return baseUrl() + "/net-diags/a";
    }

    public static String getEventUrl(String name) {
        return baseUrl() + "/events/" + name;
    }

    public static String getUpToken(){
        return baseUrl() +"/lag-report-token/a";
    }
}
