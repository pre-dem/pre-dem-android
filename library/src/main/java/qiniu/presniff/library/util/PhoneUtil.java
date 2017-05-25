package qiniu.presniff.library.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Created by Misty on 5/18/17.
 */

public class PhoneUtil {
    private static final String TAG = "PhoneUtil";

    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getAppVersion(Context context) {
        try {
            PackageInfo pi=context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "Unknown";
        }
    }

    public static String getPackageName(Context context){
        try {
            PackageInfo pi=context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "Unknown";
        }
    }

    public static String getAppName(Context context){
        int lableInfo = context.getApplicationInfo().labelRes;
        String textTitle = context.getString(lableInfo);
        LogUtils.i(TAG,"-----AppName:"+textTitle);
        return textTitle;
    }
}
