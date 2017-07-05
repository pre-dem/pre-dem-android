package qiniu.predem.android.config;

import android.content.Context;

import qiniu.predem.android.bean.AppBean;

/**
 * Created by Misty on 17/6/15.
 */

public final class Configuration {
    public static final long DEFAULT_TIMEOUT = 5000;
    private static final String TAG = "Configuration";
    public static String appKey = null;
    public static String domain = null;
    public static boolean httpMonitorEnable = true;
    public static boolean webviewEnable = true;
    public static boolean crashReportEnable = true;
    public static boolean networkDiagnosis = true; //网络诊断上报
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
}
