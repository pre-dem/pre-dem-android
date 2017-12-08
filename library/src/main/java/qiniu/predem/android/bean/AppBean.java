package qiniu.predem.android.bean;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;

import java.security.MessageDigest;

import qiniu.predem.android.BuildConfig;
import qiniu.predem.android.util.Functions;
import qiniu.predem.android.util.LogUtils;

/**
 * Created by Misty on 17/6/15.
 */

public class AppBean {
    /**
     * Name of this SDK.
     */
    public static final String SDK_NAME = "PreDemSDK";
    /**
     * Version of the SDK - retrieved from the build configuration.
     */
    public static final String SDK_VERSION = BuildConfig.VERSION_NAME;
    /**
     * The user agent string the SDK will send with every HockeyApp API request.
     */
    public static final String SDK_USER_AGENT = "PreDemSDK/Android " + BuildConfig.VERSION_NAME;
    private static final String TAG = "AppBean";
    private static final String BUNDLE_BUILD_NUMBER = "buildNumber";
    private static final String DefaultTag = "unknown_user";
    /**
     * The app's name.
     */
    public static String APP_NAME = "-";
    /**
     * The app's version code.
     */
    public static String APP_VERSION = "-";
    /**
     * The app's package name.
     */
    public static String APP_PACKAGE = "-";
    /**
     * The user's id
     */
    public static String APP_TAG = "";
    /**
     * UUID
     */
    public static String SDK_ID = "-";
    /**
     * The device's OS version.
     */
    public static String ANDROID_VERSION = "-";
    /**
     * The device's OS build.
     */
    public static String ANDROID_BUILD = "-";
    /**
     * The device's platform
     */
    public static String ANDROID_PLATFORM = "Android";

    /**
     * The device's model name.
     */
    public static String PHONE_MODEL = "-";
    /**
     * The device's model manufacturer name.
     */
    public static String PHONE_MANUFACTURER = "-";
    /**
     * Unique identifier for device, not dependent on package or device.
     */
    public static String DEVICE_IDENTIFIER = "-";
    /**
     * Unique identifier for crash, not dependent on package or device.
     */
    public static String CRASH_IDENTIFIER = "-";


    public static void loadFromContext(Context context) {
        APP_NAME = getAppName(context);
        ANDROID_VERSION = Build.VERSION.RELEASE;
        ANDROID_BUILD = Build.DISPLAY;
        PHONE_MODEL = Build.MODEL;
        PHONE_MANUFACTURER = Build.MANUFACTURER;

        SDK_ID = Functions.getSdkId();

        loadPackageData(context);
        loadCrashIdentifier(context);
    }

    public static void setAppTag(String tag) {
        APP_TAG = tag == null ? DefaultTag : tag;
    }

    @SuppressLint("HardwareIds")
    private static String getId(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return SDK_ID;
    }

    private static void loadCrashIdentifier(Context context) {
        String deviceIdentifier = getId(context);
        if (!TextUtils.isEmpty(APP_PACKAGE) && !TextUtils.isEmpty(deviceIdentifier)) {
            String combined = APP_PACKAGE + ":" + deviceIdentifier + ":" + createSalt(context);
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                byte[] bytes = combined.getBytes("UTF-8");
                digest.update(bytes, 0, bytes.length);
                bytes = digest.digest();

                CRASH_IDENTIFIER = bytesToHex(bytes);
            } catch (Throwable e) {
                LogUtils.e(TAG, "Couldn't create CrashIdentifier with Exception:" + e.toString());
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("InlinedApi")
    @SuppressWarnings("deprecation")
    private static String createSalt(Context context) {
        String abiString;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            abiString = Build.SUPPORTED_ABIS[0];
        } else {
            abiString = Build.CPU_ABI;
        }

        String fingerprint = "HA" + (Build.BOARD.length() % 10) + (Build.BRAND.length() % 10) +
                (abiString.length() % 10) + (Build.PRODUCT.length() % 10);
        String serial = "";
        try {
            serial = Build.class.getField("SERIAL").get(null).toString();
        } catch (Throwable t) {
        }
        return fingerprint + ":" + serial;
    }

    private static String bytesToHex(byte[] bytes) {
        final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char[] hex = new char[bytes.length * 2];
        for (int index = 0; index < bytes.length; index++) {
            int value = bytes[index] & 0xFF;
            hex[index * 2] = HEX_ARRAY[value >>> 4];
            hex[index * 2 + 1] = HEX_ARRAY[value & 0x0F];
        }
        String result = new String(hex);
        return result.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
    }

    private static void loadPackageData(Context context) {
        if (context != null) {
            try {
                PackageManager packageManager = context.getPackageManager();
                PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
                APP_PACKAGE = packageInfo.packageName;
                APP_VERSION = packageInfo.versionName;

                int buildNumber = loadBuildNumber(context, packageManager);
                if ((buildNumber != 0) && (buildNumber > packageInfo.versionCode)) {
                    APP_VERSION = "" + buildNumber;
                }
            } catch (Exception e) {
                LogUtils.e(TAG, "Exception thrown when accessing the package info:" + e.toString());
                e.printStackTrace();
            }
        }
    }

    private static int loadBuildNumber(Context context, PackageManager packageManager) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = appInfo.metaData;
            if (metaData != null) {
                return metaData.getInt(BUNDLE_BUILD_NUMBER, 0);
            }
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(TAG, "Exception thrown when accessing the application info:" + e.toString());
            e.printStackTrace();
        }
        return 0;
    }

    public static String getAppName(Context context) {
        int lableInfo = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.DONUT) {
            lableInfo = context.getApplicationInfo().labelRes;
        }
        return context.getString(lableInfo);
    }
}
