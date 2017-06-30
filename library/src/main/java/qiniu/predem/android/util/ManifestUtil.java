package qiniu.predem.android.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

/**
 * Created by Misty on 17/6/15.
 */

public class ManifestUtil {
    private static final String APP_KEY = "qiniu.predem.android.appKey";
    private static final String APP_DOMAIN= "qiniu.predem.android.domain";

    public static String getDomain(Context context){
        return getManifestString(context, APP_DOMAIN);
    }

    public static String getAppKey(Context context){
        return getManifestString(context, APP_KEY);
    }

    public static String getManifestString(Context context, String key){
        return getBundle(context).getString(key);
    }

    public static Bundle getBundle(Context context){
        Bundle bundle;
        try {
            bundle = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        return bundle;
    }
}
