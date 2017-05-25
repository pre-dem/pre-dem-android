package qiniu.presniff.library.io;

import android.webkit.WebViewClient;

import static qiniu.presniff.library.io.ProbeTXWebClient.excludeIPs;

/**
 * Created by Misty on 5/18/17.
 */

public class ProbeWebClient extends WebViewClient{
    private static final String TAG = "ProbeWebClient";

    public static boolean isExcludeIPs(String ip) {
        return excludeIPs.contains(ip);
    }
}
