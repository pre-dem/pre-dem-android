package qiniu.predem.library.config;

import android.content.Context;

import qiniu.predem.library.util.ManifestUtil;

/**
 * Created by Misty on 17/6/15.
 */

public class HttpConfig {
    private static final String TAG = "HttpConfig";

    public static String domain = null;

    public static String appKey = null;

    private static String scheme = "http://";

    public static final long DEFAULT_TIMEOUT = 5000;

    public static void loadFromManifest(Context context){
        domain = ManifestUtil.getDomain(context);
        appKey = ManifestUtil.getAppKey(context);
    }

    public static void setScheme(String s){
        scheme = s;
    }

    public static String getConfigUrl(){
        return  scheme + domain + "/v1/app_config/" + appKey;
    }

    public static String getHttpUrl(){
        return scheme + domain + "/http_monitor";
    }

    public static String getCrashUrl(){
        return scheme + domain +"/api/1/a/apps/" + appKey + "/crashes";
    }

    public static String getDiagnosisUrl(){
        return scheme + domain + "/v1/net_diag/test";
    }

    public static String getTelemetryUrl(){
        return "";
    }
}
