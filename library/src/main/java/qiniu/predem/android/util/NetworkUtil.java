package qiniu.predem.android.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Misty on 17/6/15.
 */

public class NetworkUtil {
    private static final String TAG = "NetworkUtil";

    public static boolean isConnectedToNetwork(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnected();
            }
        } catch (Exception e) {
            LogUtils.e(TAG,"Exception thrown when check network is connected:"+e.toString());
            e.printStackTrace();
        }
        return false;
    }
}
