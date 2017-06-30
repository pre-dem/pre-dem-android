package qiniu.predem.android.probe;

import android.webkit.WebViewClient;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Misty on 17/6/15.
 */

public class ProbeWebClient extends WebViewClient {
    protected static final Set<String> excludeIPs = new HashSet<>();
    private static final String TAG = "ProbeWebClient";

    public static boolean isExcludeIPs(String ip) {
        return excludeIPs.contains(ip);
    }
}
