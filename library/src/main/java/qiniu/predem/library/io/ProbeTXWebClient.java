package qiniu.predem.library.io;

import android.webkit.WebViewClient;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Misty on 5/18/17.
 */

public class ProbeTXWebClient extends WebViewClient {
    private static final String TAG = "ProbeTXWebClient";

    protected static final Set<String> excludeIPs = new HashSet<>();

    public static boolean isExcludeIPs(String ip) {
        return excludeIPs.contains(ip);
    }
}
