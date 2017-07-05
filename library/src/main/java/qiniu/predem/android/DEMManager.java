package qiniu.predem.android;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import qiniu.predem.android.bean.AppBean;
import qiniu.predem.android.config.Configuration;
import qiniu.predem.android.config.HttpConfig;
import qiniu.predem.android.diagnosis.NetDiagnosis;
import qiniu.predem.android.util.LogUtils;
import qiniu.predem.android.util.SharedPreUtil;

/**
 * Created by Misty on 17/6/15.
 */

public class DEMManager {
    private static final String TAG = "DEMManager";

    protected static boolean enable = true;

    public static void init(Context context) {
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

    public static void unInit() {
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

    public static void updateAppConfig() {
        enable = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                URL url = null;
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

    public static void startDiagnosisNetWork(Context context, String domain, String address, NetDiagnosis.Callback callback) {
        NetDiagnosis.start(context, domain, address, callback);
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
}
