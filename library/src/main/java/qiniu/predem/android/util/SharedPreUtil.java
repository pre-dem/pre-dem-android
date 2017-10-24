package qiniu.predem.android.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Misty on 17/6/16.
 */

public final class SharedPreUtil {
    private static final String TAG = "SharedPreUtil";

    private static final String FILE_NAME = "dem_sdk";

    private static final String LAST_CONFIGURATION_TIME = "last_time";

    private static final String HTTP_MONITOR_ENABLE = "http_monitor_enable";
    private static final String CRASH_REPORT_ENABLE = "crash_report_enable";
    private static final String WEBVIEW_ENABLE = "webview_enable";
    private static final String LAG_MONITOR_ENABLE = "lag_monitor_enable";

    private static final String CRASH_CONFIRMED_FILENAMES = "crash_confirmed_filenames";

    protected static final String PREFS_DEVICE_ID="device_id";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(FILE_NAME, context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        SharedPreferences sh = getSharedPreferences(context);
        return sh.edit();
    }

    /**
     * http 上报开关
     *
     * @param context
     * @param enable
     */
    public static void setHttpMonitorEnable(Context context, boolean enable) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putBoolean(HTTP_MONITOR_ENABLE, enable);
        editor.apply();
    }

    /**
     * crash 上报开关
     *
     * @param context
     * @param enable
     */
    public static void setCrashReportEable(Context context, boolean enable) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putBoolean(CRASH_REPORT_ENABLE, enable);
        editor.apply();
    }

    /**
     * webview 上报开关
     *
     * @param context
     * @param enable
     */
    public static void setWebviewEnable(Context context, boolean enable) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putBoolean(WEBVIEW_ENABLE, enable);
        editor.apply();
    }

    /**
     * view卡顿上报开关
     *
     * @param context
     * @param enable
     */
    public static void setLagMonitorEnable(Context context, boolean enable) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putBoolean(LAG_MONITOR_ENABLE, enable);
        editor.apply();
    }

    /**
     * crash 上报重试次数
     *
     * @param context
     * @param count
     */
    public static void setCrashRetryCount(Context context, String name, int count) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putInt(name, count);
        editor.apply();
    }

    /**
     * crash 文件列表
     *
     * @param context
     * @param confirmFiles
     */
    public static void setCrashConfirmedFilenames(Context context, String confirmFiles) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(CRASH_CONFIRMED_FILENAMES, confirmFiles);
        editor.apply();
    }

    /**
     * 保存deviceid
     * @param context
     */
    public static void setDeviceId(Context context, String deviceid){
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(PREFS_DEVICE_ID,deviceid);
        editor.apply();
    }

    public static void setLastTime(Context context) {
        SharedPreferences.Editor editor = getEditor(context);
        long time = System.currentTimeMillis();
        editor.putLong(LAST_CONFIGURATION_TIME, time);
        editor.apply();
    }

    public static boolean getHttpMonitorEnable(Context context) {
        SharedPreferences sh = getSharedPreferences(context);
        return sh.getBoolean(HTTP_MONITOR_ENABLE, true);
    }

    public static boolean getCrashReportEnable(Context context) {
        SharedPreferences sh = getSharedPreferences(context);
        return sh.getBoolean(CRASH_REPORT_ENABLE, true);
    }

    public static boolean getWebviewEnable(Context context) {
        SharedPreferences sh = getSharedPreferences(context);
        return sh.getBoolean(WEBVIEW_ENABLE, true);
    }

    public static boolean getLagMonitorEnable(Context context) {
        SharedPreferences sh = getSharedPreferences(context);
        return sh.getBoolean(LAG_MONITOR_ENABLE, true);
    }

    public static int getCrashRetryCount(Context context, String name) {
        SharedPreferences sh = getSharedPreferences(context);
        return sh.getInt(name, 0);
    }

    public static String getCrashConfirmedFilenames(Context context) {
        SharedPreferences sh = getSharedPreferences(context);
        return sh.getString(CRASH_CONFIRMED_FILENAMES, "");
    }

    public static long getConfigurationLastTime(Context context) {
        SharedPreferences sh = getSharedPreferences(context);
        return sh.getLong(LAST_CONFIGURATION_TIME, -1);
    }

    public static String getDeviceId(Context context){
        SharedPreferences sh = getSharedPreferences(context);
        return sh.getString(PREFS_DEVICE_ID,null);
    }

    public static void removeCrashConfirmedFilenames(Context context) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.remove(CRASH_CONFIRMED_FILENAMES);
        editor.apply();
    }

    public static void removeCrashRetryCount(Context context, String name) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.remove(name);
        editor.apply();
    }
}
