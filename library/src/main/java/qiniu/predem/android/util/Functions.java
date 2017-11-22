package qiniu.predem.android.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

import com.qiniu.android.common.FixedZone;
import com.qiniu.android.common.Zone;
import com.qiniu.android.storage.UploadManager;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.List;
import java.util.UUID;

/**
 * Created by Misty on 17/7/14.
 */

public final class Functions {
    private static final String TAG = "ToolUtil";
//     staging

    private static UploadManager uploadInstance = getUpManagerByZone();

    public static String getStringMd5(String content) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(content.getBytes());
        byte[] m = md5.digest();//加密
        return getString(m);
    }

    private static String getString(byte[] bytes) {
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

    public static String generateSdkId(){
        return UUID.randomUUID().toString();
    }


    public static boolean isBackground(Context context) {
        if (context == null) {
            return false;
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null || appProcesses.size() == 0) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    public static UploadManager getUploadManager() {
        return uploadInstance;
    }

    private static Zone getZone() {
        return new FixedZone(new String[]{"10.200.20.23:5010"});
//        return FixedZone.zone0;
    }

    private static UploadManager getUpManagerByZone() {
        com.qiniu.android.storage.Configuration config = new com.qiniu.android.storage.Configuration.Builder().zone(getZone()).build();
        return new UploadManager(config);
    }
}
