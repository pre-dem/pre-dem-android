package qiniu.predem.android.util;

import android.app.ActivityManager;
import android.content.Context;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by Misty on 17/7/14.
 */

public class ToolUtil {
    private static final String TAG = "ToolUtil";

    public static String getStringMd5(String content) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(content.getBytes());
        byte[] m = md5.digest();//加密
        return getString(m);
    }

    private static String getString(byte[] bytes){
        String result = "";
        for (byte b : bytes) {
            String temp = Integer.toHexString(b & 0xff);
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            result += temp;
        }
        return result;
    }

    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    return true;
                }else{
                    return false;
                }
            }
        }
        return false;
    }
}
