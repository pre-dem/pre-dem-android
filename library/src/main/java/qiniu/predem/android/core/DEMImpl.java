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
import java.net.ProtocolException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import qiniu.predem.android.DEMManager;
import qiniu.predem.android.bean.AppBean;
import qiniu.predem.android.config.Configuration;
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
    private boolean enable = true;
    private WeakReference<Context> context;

    public static DEMImpl instance() {
        return _instance;
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

    public static String getApp() {
        return "app_key:" + Configuration.appKey
                + ",http_monitor_enabled:" + Configuration.httpMonitorEnable
                + ",crash_report_enable:" + Configuration.crashReportEnable;
    }

    public void start(String domain, String appKey, Context context) {
        this.context = new WeakReference<>(context);

        //获取AppBean信息
        Configuration.init(context, appKey, domain);
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

    public void updateAppConfig(final Context context) {
        enable = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                try {
                    url = new URL(Configuration.getConfigUrl());
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
                            Configuration.httpMonitorEnable = jo.optBoolean("http_monitor_enabled");
                            Configuration.crashReportEnable = jo.optBoolean("crash_report_enabled");
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

    public void trackEvent(String eventName, Map<String, Object> event) {
        JSONObject obj = new JSONObject(event);
        trackEvent(eventName, event);
    }

    public void trackEvent(String eventName, JSONObject event) {
        sendRequest(Configuration.getEventUrl(eventName), event.toString());
    }

    private boolean sendRequest(String url, String content) {
        LogUtils.d(TAG, "------url = " + url + "\ncontent = " + content);

        HttpURLConnection httpConn;
        try {
            httpConn = (HttpURLConnection) new URL(url).openConnection();
        } catch (IOException e) {
            LogUtils.e(TAG, e.toString());
            return false;
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
            return false;
        }
        httpConn.setConnectTimeout(3000);
        httpConn.setReadTimeout(10000);
        try {
            httpConn.setRequestMethod("POST");
        } catch (ProtocolException e) {
            LogUtils.e(TAG, e.toString());
            return false;
        }
        httpConn.setRequestProperty("Content-Type", "application/json");
        httpConn.setRequestProperty("Accept-Encoding", "identity");

        try {
            httpConn.getOutputStream().write(content.getBytes());
            httpConn.getOutputStream().flush();
        } catch (IOException e) {
            LogUtils.e(TAG, e.toString());
            return false;
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
            return false;
        }
        int responseCode = 0;
        try {
            responseCode = httpConn.getResponseCode();
        } catch (IOException e) {
            LogUtils.e(TAG, e.toString());
            return false;
        }
        if (responseCode != 201 && responseCode != 200) {
            return false;
        }
        int length = httpConn.getContentLength();
        if (length == 0) {
            return false;
        } else if (length < 0) {
            length = 16 * 1024;
        }
        InputStream is;
        try {
            is = httpConn.getInputStream();
        } catch (IOException e) {
            LogUtils.e(TAG, e.toString());
            return false;
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
            return false;
        }
        byte[] data = new byte[length];
        int read = 0;
        try {
            read = is.read(data);
        } catch (IOException e) {
            LogUtils.e(TAG, e.toString());
            return false;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                LogUtils.e(TAG, e.toString());
            }
        }
        return read > 0;
    }
}
