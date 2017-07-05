package qiniu.predem.android.config;

import android.content.Context;

import qiniu.predem.android.util.ManifestUtil;

/**
 * Created by Misty on 17/6/15.
 */

public class HttpConfig {
    public static final long DEFAULT_TIMEOUT = 5000;
    private static final String TAG = "HttpConfig";
    public static String domain = null;
    public static String appKey = null;
    private static String ak = null;
    private static String scheme = "http://";

    private static String getAk(String appKey) {
        return appKey.substring(0, 8);
    }

    public static void setScheme(String s) {
        scheme = s;
    }

    public static String getConfigUrl() {
        return scheme + domain + "/v1/" + ak + "/app-config/a";
    }

    public static String getHttpUrl() {
        return scheme + domain + "/v1/" + ak + "/http-stats/a";
    }

    public static String getCrashUrl() {
        return scheme + domain + "/v1/" + ak + "/crashes/a";
    }

    public static String getDiagnosisUrl() {
        return scheme + domain + "/v1/" + ak + "/net-diags/a";
    }

    public static String getCustomUrl() {
        return scheme + domain + "/v1/" + ak + "/custom/";
    }
}
