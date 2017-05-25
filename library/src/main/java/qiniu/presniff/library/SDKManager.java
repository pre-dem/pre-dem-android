package qiniu.presniff.library;

import android.content.Context;

import qiniu.presniff.library.http.LogReporter;

/**
 * Created by Misty on 5/18/17.
 */

public class SDKManager {
    private static final String TAG = "Probe";

    protected static boolean enable = true;
    protected static boolean dns = true;

    private static boolean enableHttpReport = true;
    private static boolean enableCrashReport = true;

    public static void init(Context context, String appId, String appKey) {
        if (enableHttpReport){
            //注册reporter
            initReporter(context);
        }
        if (enableCrashReport){
            //TODO 注册崩溃上报
            CrashManager.register(context,"9a9c127726b746e5b5fa7fc816a17407");
        }
    }

    private static boolean initialized = false;

    private static void initReporter(Context context){
        if (initialized || context == null) {
            return;
        }
        initialized = true;

        //注册定时器
        LogReporter.getInstance().initialize(context.getApplicationContext());
    }

    public static void unInit() {
        //销毁定时器
        LogReporter.getInstance().destroy();
        initialized = false;
    }

    public static boolean isDns() {
        return dns;
    }

    public static boolean isEnable() {
        return enable;
    }
}
