package qiniu.predem.android.config;

import android.content.Context;

import qiniu.predem.android.bean.AppBean;

/**
 * Created by Misty on 17/6/15.
 */

public final class Configuration {
    private static final String TAG = "Configuration";

    public static String appKey = null;

    public static String userId = null;

    public static int platform = 2;// 1.ios 2.android

    public static boolean httpMonitorEnable = true;

    public static boolean crashReportEnable = true;

    public static boolean telemetryEnable = true;

    public static boolean networkDiagnosis = true; //网络诊断上报

    public static boolean dnsEnable = true; //默认是否使用dns

    public static void init(Context context) {
        if (context == null) {
            return;
        }

        //初始化 app 信息
        AppBean.loadFromContext(context);
        //初始化文件路径
        FileConfig.loadFilesPath(context);
    }
}
