package qiniu.predem.android.core;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import qiniu.predem.android.DEMManager;
import qiniu.predem.android.bean.AppBean;
import qiniu.predem.android.config.Configuration;
import qiniu.predem.android.config.HttpConfig;
import qiniu.predem.android.crash.CrashManager;
import qiniu.predem.android.diagnosis.NetDiagnosis;
import qiniu.predem.android.http.HttpMonitorManager;
import qiniu.predem.android.util.SharedPreUtil;

/**
 * Created by long on 2017/7/4.
 */

public final class DEMImpl {
    private static final String TAG = "DEMManager";

    private static final DEMImpl _instance = new DEMImpl();

    public static DEMImpl instance(){
        return _instance;
    }

    private boolean enable = true;

    private WeakReference<Context> context;

    public void start(String domain, String appKey, Context context){
        HttpConfig.appKey = appKey;
        HttpConfig.domain = domain;
        this.context = new WeakReference<>(context);

        //获取AppBean信息
        Configuration.init(context);
        if (askForConfiguration(context)) {
            updateAppConfig(context);
        }

        if (Configuration.httpMonitorEnable) {
            HttpMonitorManager.getInstance().register(context);
        }
        if (Configuration.crashReportEnable) {
            CrashManager.register(context);
        }
    }


    public void unInit() {
        if (Configuration.httpMonitorEnable) {
            HttpMonitorManager.getInstance().unregister();
        }
    }

    private static boolean askForConfiguration(Context context) {
        long lastTime = SharedPreUtil.getConfigurationLastTime(context);
        if (lastTime == -1) {
            return true;
        }
        long now = System.currentTimeMillis();
        if ((now - lastTime) > 86400000) {
            return true;
        }
        return false;
    }

    public  void updateAppConfig(final Context context) {
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
                        if (conn.getResponseCode() == 200) {
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

    public void netDiag(String domain, String address, DEMManager.NetDiagCallback netDiagCallback) {
        NetDiagnosis.start(this.context.get(), domain, address, netDiagCallback);
    }

    public static String getApp() {
        return "app_key:" + Configuration.appKey + ",user_id:" + Configuration.userId + ",platform:" + Configuration.platform + ",http_monitor_enabled:" + Configuration.httpMonitorEnable + ",crash_report_enable:" + Configuration.crashReportEnable + ",telemetry_enable:" + Configuration.telemetryEnable;
    }

    private void signOut(Context context) {
        if (isApplicationBroughtToBackground(context)) {
            unInit();
        }
    }

    //需要申请GETTask权限
    private boolean isApplicationBroughtToBackground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(AppBean.APP_PACKAGE)) {
                return true;
            }
        }
        return false;
    }

    public void trackEvent(String eventName, Map<String, Object> event){

    }

    public void trackEvent(String eventName, JSONObject event){

    }
}
