package qiniu.predem.library;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import qiniu.predem.library.bean.TelemetryBean;
import qiniu.predem.library.config.Configuration;
import qiniu.predem.library.config.HttpConfig;
import qiniu.predem.library.diagnosis.NetDiagnosis;
import qiniu.predem.library.util.LogUtils;
import qiniu.predem.library.util.SharedPreUtil;

/**
 * Created by Misty on 17/6/15.
 */

public class DEMManager {
    private static final String TAG = "DEMManager";

    protected static boolean enable = true;

    public static void init(Context context) {

        //获取AppBean信息
        Configuration.init(context);
        if (askForConfiguration(context)){
            updateAppConfig(context);
        }

        LogUtils.d(TAG,"------isHttpMonitorReportEnable : " + Configuration.httpMonitorEnable);
        if (Configuration.httpMonitorEnable){
            HttpMonitorManager.getInstance().register(context);
        }
        if (Configuration.crashReportEnable){
            CrashManager.register(context);
        }
    }

    public static void unInit(){
        if (Configuration.httpMonitorEnable){
            HttpMonitorManager.getInstance().unregister();
        }
    }

    private static boolean askForConfiguration(Context context){
        long lastTime = SharedPreUtil.getConfigurationLastTime(context);
        if (lastTime == -1){
            return true;
        }
        long now = System.currentTimeMillis();
        if ((now - lastTime) > 86400000){
            return true;
        }
        return false;
    }

    public static void updateAppConfig(final Context context) {
        enable = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                try {
                    url = new URL(HttpConfig.getConfigUrl());
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
                            Configuration.appKey = jo.optString("app_key");
                            Configuration.userId = jo.optString("user_id");
                            Configuration.platform = jo.optInt("platform");
                            Configuration.httpMonitorEnable = jo.optBoolean("http_monitor_enabled");
                            Configuration.crashReportEnable = jo.optBoolean("crash_report_enabled");
                            Configuration.telemetryEnable = jo.optBoolean("telemetry_enabled");
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

    public static void startDiagnosisNetWork(Context context, String domain , String address, NetDiagnosis.Callback callback){
        NetDiagnosis.start(context, domain, address, callback);
    }

    public static String getApp(){
        return "app_key:"+Configuration.appKey+",user_id:"+Configuration.userId+",platform:"+Configuration.platform+",http_monitor_enabled:"+Configuration.httpMonitorEnable+",crash_report_enable:"+Configuration.crashReportEnable+",telemetry_enable:"+Configuration.telemetryEnable;
    }
}
