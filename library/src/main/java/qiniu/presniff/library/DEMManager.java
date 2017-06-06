package qiniu.presniff.library;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import qiniu.presniff.library.bean.AppBean;
import qiniu.presniff.library.config.NetConfig;
import qiniu.presniff.library.util.LogUtils;
import qiniu.presniff.library.util.ManifestUtil;

/**
 * Created by Misty on 5/18/17.
 */

public class DEMManager {
    private static final String TAG = "DEMManager";

    protected static boolean enable = true;
    protected static boolean dns = true;

    private static String appKey ;
    private static String appId ;

    private static AppBean app;

    public static void init(Context context) {
        app = new AppBean();
        appKey = ManifestUtil.getAppKey(context);
        appId = ManifestUtil.getAppId(context);
        updateAppConfig(context);
    }

    public static void unInit(){
        if (app.isHttpMonitorEnable()){
            HttpReportManager.getInstance().unregister();
        }
    }

    public static void updateAppConfig(final Context context) {
        enable = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                try {
                    url = new URL(NetConfig.configPath.concat(appKey));
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    StringBuffer jsonStr = new StringBuffer();
                    byte[] buf = new byte[1024];
                    int len;
                    InputStream in = conn.getInputStream();
                    while ((len = in.read(buf)) != -1) {
                        jsonStr.append(new String(Arrays.copyOfRange(buf, 0, len)));
                    }

                    try {
                        if (conn.getResponseCode() == 200){
                            JSONObject jo = new JSONObject(jsonStr.toString());
                            app.setAppKey(jo.optString("app_key"));
                            app.setUserId(jo.optString("user_id"));
                            app.setPlatform(jo.optInt("platform"));
                            app.setHttpMonitorEnable(jo.optBoolean("http_monitor_enabled"));
                            app.setCrashReportEnable(jo.optBoolean("crash_report_enabled"));
                            app.setTelemetryEnable(jo.optBoolean("telemetry_enabled"));

                            if (app.isHttpMonitorEnable()){
                                LogUtils.d(TAG,"------initReporter");
                                HttpReportManager.getInstance().register(context);
                            }
                            if (app.isCrashReportEnable()){
                                LogUtils.d(TAG,"------registerCrash");

                            }
                            if (app.isTelemetryEnable()){
                                LogUtils.d(TAG,"------initTelemetry");
                            }
                        }else{
                            //联网失败，使用默认配置
                            app.setDefault();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public static String getApp(){
        return "app_key:"+app.getAppKey()+",user_id:"+app.getUserId()+",platform:"+app.getPlatform()+",http_monitor_enabled:"+app.isHttpMonitorEnable()+",crash_report_enable:"+app.isCrashReportEnable()+",telemetry_enable:"+app.isTelemetryEnable();
    }

    public static boolean isDns() {
        return dns;
    }

    public static boolean isHttpMonitorEnable() {
        return app.isHttpMonitorEnable();
    }
    public static boolean isEnable() {
        return enable;
    }
}
