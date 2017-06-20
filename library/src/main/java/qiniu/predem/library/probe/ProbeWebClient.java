package qiniu.predem.library.probe;

import android.webkit.WebViewClient;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Misty on 17/6/15.
 */

public class ProbeWebClient extends WebViewClient {
    private static final String TAG = "ProbeWebClient";

    protected static final Set<String> excludeIPs = new HashSet<>();

    public static boolean isExcludeIPs(String ip) {
        return excludeIPs.contains(ip);
    }
}
