package qiniu.predem.android.util;

import android.app.ActivityManager;
import android.content.Context;

import com.qiniu.android.common.FixedZone;
import com.qiniu.android.common.Zone;
import com.qiniu.android.storage.UploadManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import qiniu.predem.android.config.FileConfig;


/**
 * Created by Misty on 17/7/14.
 */

public final class Functions {
    private static final String TAG = "ToolUtil";

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

    public static String getSdkId() {
        try {
            String uuid;
            String path = FileConfig.FILES_PATH + File.separator + "dem_sdk_id.log";
            File f = new File(path);
            if (f.exists()) {
                FileInputStream inputStream = new FileInputStream(f);
                byte[] b = new byte[inputStream.available()];
                inputStream.read(b);
                uuid = new String(b);
            } else {
                FileOutputStream fos = new FileOutputStream(f);
                uuid = UUID.randomUUID().toString();
                fos.write(uuid.getBytes());
                fos.close();
            }
            return uuid;
        } catch (Exception e) {
//            e.printStackTrace();
            return UUID.randomUUID().toString();
        }
    }

    public static boolean isBackground(Context context) {
        if (context == null) {
            return false;
        }

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return true;
        }

        List<ActivityManager.RunningAppProcessInfo> appProcesses = null;
        appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null || appProcesses.size() == 0) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                return appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
            }
        }
        return false;
    }

    public static UploadManager getUploadManager() {
        return uploadInstance;
    }

    private static Zone getZone() {
//        return new FixedZone(new String[]{"10.200.20.23:5010"});
        return FixedZone.zone0;
    }

    private static UploadManager getUpManagerByZone() {
        com.qiniu.android.storage.Configuration config = new com.qiniu.android.storage.Configuration.Builder().zone(getZone()).build();
        return new UploadManager(config);
    }
}
