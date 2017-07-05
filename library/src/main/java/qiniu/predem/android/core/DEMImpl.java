package qiniu.predem.android.core;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import qiniu.predem.android.DEMManager;
import qiniu.predem.android.bean.AppBean;
import qiniu.predem.android.config.Configuration;
import qiniu.predem.android.config.HttpConfig;
import qiniu.predem.android.crash.CrashManager;
import qiniu.predem.android.diagnosis.NetDiagnosis;
import qiniu.predem.android.http.HttpMonitorManager;
import qiniu.predem.android.util.LogUtils;
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

    private WeakReference<Context> context;

    public void start(String domain, String appKey, Context context){
        HttpConfig.appKey = appKey;
        HttpConfig.domain = domain;
        this.context = new WeakReference<>(context);

        //获取AppBean信息
        Configuration.init(context);
        if (askForConfiguration(context)) {
            updateAppConfig();
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

    public  void updateAppConfig() {
        new Thread(new Runnable() {
            @Override
            public void run() {
//                URL url = null;
                try {
                    JSONObject parameters = new JSONObject();
                    parameters.put("app_bundle_id", AppBean.APP_PACKAGE);
                    parameters.put("app_name", AppBean.APP_NAME);
                    parameters.put("app_version", AppBean.APP_VERSION);
                    parameters.put("device_model", AppBean.PHONE_MODEL);
                    parameters.put("os_platform","a");
                    parameters.put("os_version",AppBean.ANDROID_VERSION);
                    parameters.put("sdk_version", AppBean.SDK_VERSION);
                    parameters.put("sdk_id",AppBean.ANDROID_BUILD);
                    parameters.put("device_id",AppBean.DEVICE_IDENTIFIER);

                    HttpURLConnection httpConn = (HttpURLConnection) new URL(HttpConfig.getConfigUrl()).openConnection();

                    httpConn.setConnectTimeout(3000);
                    httpConn.setReadTimeout(10000);
                    httpConn.setRequestMethod("POST");

                    httpConn.setRequestProperty("Content-Type", "application/json");
                    httpConn.setRequestProperty("Accept-Encoding", "identity");

                    byte[] bytes = parameters.toString().getBytes();

                    ByteArrayOutputStream compressed = new ByteArrayOutputStream();
                    GZIPOutputStream gzip = new GZIPOutputStream(compressed);
                    gzip.write(bytes);
                    gzip.close();
                    httpConn.getOutputStream().write(compressed.toByteArray());
                    httpConn.getOutputStream().flush();

                    int responseCode = 0;
                    try {
                        responseCode = httpConn.getResponseCode();
                    } catch (IOException e) {
                        LogUtils.e(TAG, e.toString());
                    }

                    LogUtils.e("-----configuration code : " + responseCode);
                    LogUtils.e("-----configuration body : " + httpConn.getResponseMessage());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
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
