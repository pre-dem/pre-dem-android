package qiniu.predem.android.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Misty on 17/6/16.
 */

public class SharedPreUtil {
    private static final String TAG = "SharedPreUtil";

    private static final String FILE_NAME = "dem_sdk";

    private static final String LAST_CONFIGURATION_TIME = "last_time";

    private static final String HTTP_MONITOR_ENABLE = "http_monitor_enable";
    private static final String CRASH_REPORT_ENABLE = "crash_report_enable";
    private static final String TELEMETRY_ENABLE = "telemetry_enable";
    private static final String NETWORK_DIAGNOSIS_ENABLE = "network_diagnosis_enable";

    private static SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences(FILE_NAME, context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor getEditor(Context context){
        SharedPreferences sh = getSharedPreferences(context);
        return sh.edit();
    }

    public static void setHttpMonitorEnable(Context context, boolean enable){
        SharedPreferences.Editor editor = getEditor(context);
        editor.putBoolean(HTTP_MONITOR_ENABLE, enable);
        editor.apply();
    }

    public static  void setCrashReportEable(Context context, boolean enable){
        SharedPreferences.Editor editor = getEditor(context);
        editor.putBoolean(CRASH_REPORT_ENABLE, enable);
        editor.apply();
    }

    public static void setTelemetryEable(Context context, boolean enable){
        SharedPreferences.Editor editor = getEditor(context);
        editor.putBoolean(TELEMETRY_ENABLE, enable);
        editor.apply();
    }

    public static void setNetWorkDiagnosisEable(Context context, boolean enable){
        SharedPreferences.Editor editor = getEditor(context);
        editor.putBoolean(NETWORK_DIAGNOSIS_ENABLE, enable);
        editor.apply();
    }

    public static void setLastTime(Context context){
        SharedPreferences.Editor editor = getEditor(context);
        long time = System.currentTimeMillis();
        editor.putLong(LAST_CONFIGURATION_TIME, time);
        editor.apply();
    }

    public static boolean getHttpMonitorEnable(Context context){
        SharedPreferences sh = getSharedPreferences(context);
        return sh.getBoolean(HTTP_MONITOR_ENABLE, false);
    }
    public static boolean getCrashReportEnable(Context context){
        SharedPreferences sh = getSharedPreferences(context);
        return sh.getBoolean(CRASH_REPORT_ENABLE, false);
    }
    public static boolean getNetWorkDiagnosisEnable(Context context){
        SharedPreferences sh = getSharedPreferences(context);
        return sh.getBoolean(NETWORK_DIAGNOSIS_ENABLE, false);
    }
    public static boolean getTelemetryEnable(Context context){
        SharedPreferences sh = getSharedPreferences(context);
        return sh.getBoolean(TELEMETRY_ENABLE, false);
    }
    public static long getConfigurationLastTime(Context context){
        SharedPreferences sh = getSharedPreferences(context);
        return sh.getLong(LAST_CONFIGURATION_TIME, -1);
    }
}
