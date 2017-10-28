package qiniu.predem.android.util;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;

import com.qiniu.android.common.FixedZone;
import com.qiniu.android.common.Zone;
import com.qiniu.android.storage.UploadManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.List;
import java.util.UUID;

import static android.R.attr.path;
import static android.os.Environment.getExternalStorageState;

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

    public static String generateDeviceId(Context context){
        UUID uuid = null;
        if (uuid == null){
            synchronized(Functions.class) {
                if(uuid==null) {
                    String id = SharedPreUtil.getDeviceId(context);
                    if(id !=null) {
                        uuid= UUID.fromString(id);
                    }else{
                        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                        try{
                            if(!"9774d56d682e549c".equals(androidId)) {
                                uuid= UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
                            }else{
                                String deviceId = ((TelephonyManager) context.getSystemService( Context.TELEPHONY_SERVICE)).getDeviceId();
                                uuid= deviceId!=null? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
                            }
                        }catch(UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                        SharedPreUtil.setDeviceId(context,uuid.toString());
                    }
                }
                return uuid.toString();
            }
        }
        return "";
    }

    public static String getUUID(){
        try {
            String uuid = "";

            File file = Environment.getExternalStorageDirectory();
            String path = file.getAbsolutePath() + File.separator +"uuid.log";
            File f = new File(path);
            if (f.exists()){
                FileInputStream inputStream = new FileInputStream(f);
                byte[] b = new byte[inputStream.available()];
                inputStream.read(b);
                uuid = new String(b);
            }else{
                FileOutputStream fos = new FileOutputStream(f);
                uuid = UUID.randomUUID().toString();
                fos.write(uuid.getBytes());
                fos.close();
            }
            return uuid;
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

//    public static String generateUUID(Context context) {
//        int permissionCheck = ContextCompat.checkSelfPermission(context,
//                Manifest.permission.READ_PHONE_STATE);
//        if (permissionCheck > 0) {
//            String s = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
//            if (s == null) {
//                s = "";
//            }
//            String s1 = android.provider.Settings.Secure.getString(context.getContentResolver(), "android_id");
//            if (s1 == null) {
//                s1 = "";
//            }
//            String s2;
//            String s3;
//            WifiInfo wifiinfo;
//            String s4;
//            if (android.os.Build.VERSION.SDK_INT >= 9) {
//                s2 = Build.SERIAL;
//                if (s2 == null)
//                    s2 = "";
//            } else {
//                s2 = getDeviceSerial();
//            }
//            s3 = "";
//            wifiinfo = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
//            if (wifiinfo != null) {
//                s3 = wifiinfo.getMacAddress();
//                if (s3 == null) {
//                    s3 = "";
//                }
//            }
//            try {
//                s4 = getMD5String((new StringBuilder()).append(s).append(s1).append(s2).append(s3).toString());
//            } catch (NoSuchAlgorithmException nosuchalgorithmexception) {
//                nosuchalgorithmexception.printStackTrace();
//                return null;
//            }
//            return s4;
//        } else {
//            return "";
//        }
//    }

    private static String getMD5String(String s) throws NoSuchAlgorithmException {
        byte abyte0[] = MessageDigest.getInstance("SHA-1").digest(s.getBytes());
        Formatter formatter = new Formatter();
        int i = abyte0.length;
        for (int j = 0; j < i; j++) {
            byte byte0 = abyte0[j];
            Object aobj[] = new Object[1];
            aobj[0] = Byte.valueOf(byte0);
            formatter.format("%02x", aobj);
        }
        return formatter.toString();
    }

    private static String getDeviceSerial() {
        String s;
        try {
            Method method = Class.forName("android.os.Build").getDeclaredMethod("getString", new Class[]{
                    Class.forName("java.lang.String")
            });
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            s = (String) method.invoke(new Build(), new Object[]{
                    "ro.serialno"
            });
        } catch (ClassNotFoundException classnotfoundexception) {
            classnotfoundexception.printStackTrace();
            return "";
        } catch (NoSuchMethodException nosuchmethodexception) {
            nosuchmethodexception.printStackTrace();
            return "";
        } catch (InvocationTargetException invocationtargetexception) {
            invocationtargetexception.printStackTrace();
            return "";
        } catch (IllegalAccessException illegalaccessexception) {
            illegalaccessexception.printStackTrace();
            return "";
        }
        return s;
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
//        return new FixedZone(new String[]{"10.200.20.23:5010"});
        return FixedZone.zone0;
    }

    private static UploadManager getUpManagerByZone() {
        com.qiniu.android.storage.Configuration config = new com.qiniu.android.storage.Configuration.Builder().zone(getZone()).build();
        return new UploadManager(config);
    }
}
