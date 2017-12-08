package qiniu.predem.android.core;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import qiniu.predem.android.DEMManager;
import qiniu.predem.android.bean.AppBean;
import qiniu.predem.android.bean.CustomBean;
import qiniu.predem.android.block.TraceInfoCatcher;
import qiniu.predem.android.config.Configuration;
import qiniu.predem.android.crash.CrashManager;
import qiniu.predem.android.diagnosis.NetDiagnosis;
import qiniu.predem.android.http.HttpMonitorManager;
import qiniu.predem.android.logcat.PrintLogger;
import qiniu.predem.android.util.HttpURLConnectionBuilder;
import qiniu.predem.android.util.LogUtils;
import qiniu.predem.android.util.SharedPreUtil;

import static qiniu.predem.android.config.Configuration.CRASH_REPORT_ENABLE;
import static qiniu.predem.android.config.Configuration.HTTP_MONITOR_ENABLE;
import static qiniu.predem.android.config.Configuration.LAG_MONITOR_ENABLE;
import static qiniu.predem.android.config.Configuration.WEBVIEW_ENABLE;

/**
 * Created by long on 2017/7/4.
 */

public final class DEMImpl {
    private static final String TAG = "DEMManager";

    private static final DEMImpl _instance = new DEMImpl();
    /**
     * log's level
     */
    private final static int V = Log.VERBOSE;
    private final static int W = Log.WARN;
    private final static int I = Log.INFO;
    private final static int D = Log.DEBUG;
    private final static int E = Log.ERROR;
    private final static int P = Integer.MAX_VALUE;
    private WeakReference<Context> context;
    /**
     * print level
     */
    private int level = 0;

    public static DEMImpl instance() {
        return _instance;
    }

    //获取Exception堆栈
    private static String getStackTraceString(String str, Throwable e) {
        //将Exception的错误信息转换成String
        String log = "";
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            log = str + "\r\n" + sw.toString() + "\r\n";
        } catch (Exception e2) {
            log = str + " fail to print Exception";
        }
        return log;
    }

    public void start(String domain, String appKey, Context context) {
        this.context = new WeakReference<>(context);

        LogUtils.d(TAG, "DemManager start");

        //获取 App 信息
        Configuration.init(context, appKey, domain);
        updateAppConfig(context);
    }

    public void setUserTag(String userid) {
        AppBean.setAppTag(userid);
    }

    public void netDiag(String domain, String address, DEMManager.NetDiagCallback netDiagCallback) {
        NetDiagnosis.getInstance().start(this.context.get(), domain, address, netDiagCallback);
    }

    public void trackEvent(String eventName, JSONObject event) {
        CustomBean paramter = new CustomBean(eventName, event.toString());
        sendRequest(Configuration.getEventUrl(), paramter.toJsonString());
    }

    /**
     * 更新 配置文件
     *
     * @param cxt
     */
    public void updateAppConfig(final Context cxt) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream in = null;
                try {
                    JSONObject parameters = new JSONObject();
                    parameters.put("app_bundle_id", AppBean.APP_PACKAGE);
                    parameters.put("app_name", AppBean.APP_NAME);
                    parameters.put("app_version", AppBean.APP_VERSION);
                    parameters.put("device_model", AppBean.PHONE_MODEL);
                    parameters.put("os_platform", AppBean.ANDROID_PLATFORM);
                    parameters.put("os_version", AppBean.ANDROID_VERSION);
                    parameters.put("sdk_version", AppBean.SDK_VERSION);
                    parameters.put("sdk_id", AppBean.SDK_ID);
                    parameters.put("device_id", AppBean.PHONE_MANUFACTURER);

                    HttpURLConnection httpConn = new HttpURLConnectionBuilder(Configuration.getConfigUrl())
                            .setRequestMethod("POST")
                            .setHeader("Content-Type", "application/json")
                            .setRequestBody(parameters.toString())
                            .build();

                    int responseCode = httpConn.getResponseCode();
                    boolean successful = (responseCode == HttpURLConnection.HTTP_ACCEPTED || responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK);
                    if (successful) {
                        byte[] data = new byte[4 * 1024];
                        in = httpConn.getInputStream();
                        in.read(data);
                        JSONObject jsonObject = new JSONObject(new String(data));
                        SharedPreUtil.setHttpMonitorEnable(cxt, jsonObject.optBoolean(HTTP_MONITOR_ENABLE));
                        SharedPreUtil.setCrashReportEable(cxt, jsonObject.optBoolean(CRASH_REPORT_ENABLE));
                        SharedPreUtil.setWebviewEnable(cxt, jsonObject.optBoolean(WEBVIEW_ENABLE));
                        SharedPreUtil.setLagMonitorEnable(cxt, jsonObject.optBoolean(LAG_MONITOR_ENABLE));
                    }
                    //获取各项上报开关
                    Configuration.httpMonitorEnable = SharedPreUtil.getHttpMonitorEnable(cxt);
                    Configuration.crashReportEnable = SharedPreUtil.getCrashReportEnable(cxt);
                    Configuration.webviewEnable = SharedPreUtil.getWebviewEnable(cxt);
                    Configuration.lagMonitorEnable = SharedPreUtil.getLagMonitorEnable(cxt);

                    if (Configuration.httpMonitorEnable) {
                        LogUtils.d(TAG, "---Http monitor " + Configuration.httpMonitorEnable);
                        HttpMonitorManager.getInstance().register(cxt);
                    }
                    if (Configuration.crashReportEnable) {
                        LogUtils.d(TAG, "---Crash report " + Configuration.crashReportEnable);
                        CrashManager.register(cxt);
                    }
                    if (Configuration.lagMonitorEnable) {
                        LogUtils.d(TAG, "----Lag monitor " + Configuration.lagMonitorEnable);
                        TraceInfoCatcher traceInfoCatcher = new TraceInfoCatcher(cxt);
                        traceInfoCatcher.start();
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private boolean sendRequest(String url, String content) {
        LogUtils.d(TAG, "----url:" + url + "\tcontent:" + content);
        try {
            HttpURLConnection httpConn = new HttpURLConnectionBuilder(url)
                    .setRequestMethod("POST")
                    .setHeader("Content-Type", "application/json")
                    .setRequestBody(content)
                    .build();

            int responseCode = httpConn.getResponseCode();
            boolean successful = (responseCode == HttpURLConnection.HTTP_ACCEPTED || responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK);
            return successful;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void openLogs(int level) {
        this.level = level;
        PrintLogger.getInstance(context.get()).openLogs();
    }

    public void closeLogs() {
        level = P;
        PrintLogger.getInstance(context.get()).closeLogs();
    }

    public void i(String tag, String msg) {
        Log.i(tag, msg);
        if (level < I) {
            PrintLogger.getInstance(context.get()).Log(tag, msg);
        }
    }

    public void i(String tag, String msg, Throwable tr) {
        Log.i(tag, msg, tr);
        if (level < I) {
            PrintLogger.getInstance(context.get()).Log(tag, getStackTraceString(msg, tr));
        }
    }

    public void v(String tag, String msg) {
        Log.v(tag, msg);
        if (level < V) {
            PrintLogger.getInstance(context.get()).Log(tag, msg);
        }
    }

    public void v(String tag, String msg, Throwable tr) {
        Log.v(tag, msg, tr);
        if (level < V) {
            PrintLogger.getInstance(context.get()).Log(tag, getStackTraceString(msg, tr));
        }
    }

    public void d(String tag, String msg) {
        Log.d(tag, msg);
        if (level < D) {
            PrintLogger.getInstance(context.get()).Log(tag, msg);
        }
    }

    public void d(String tag, String msg, Throwable tr) {
        Log.d(tag, msg, tr);
        if (level < D) {
            PrintLogger.getInstance(context.get()).Log(tag, getStackTraceString(msg, tr));
        }
    }

    public void w(String tag, String msg) {
        Log.w(tag, msg);
        if (level < W) {
            PrintLogger.getInstance(context.get()).Log(tag, msg);
        }
    }

    public void w(String tag, Throwable tr) {
        Log.w(tag, tr);
        if (level < W) {
            PrintLogger.getInstance(context.get()).Log(tag, getStackTraceString("", tr));
        }
    }

    public void w(String tag, String msg, Throwable tr) {
        Log.w(tag, msg, tr);
        if (level < W) {
            PrintLogger.getInstance(context.get()).Log(tag, getStackTraceString(msg, tr));
        }
    }

    public void e(String tag, String msg) {
        Log.e(tag, msg);
        if (level < E) {
            PrintLogger.getInstance(context.get()).Log(tag, msg);
        }
    }

    public void e(String tag, String msg, Throwable tr) {
        Log.e(tag, msg, tr);
        if (level < E) {
            PrintLogger.getInstance(context.get()).Log(tag, getStackTraceString(msg, tr));
        }
    }

    public void wtf(String tag, String msg) {
        Log.wtf(tag, msg);
        if (level < W) {
            PrintLogger.getInstance(context.get()).Log(tag, msg);
        }
    }

    public void wtf(String tag, Throwable tr) {
        Log.wtf(tag, tr);
        if (level < W) {
            PrintLogger.getInstance(context.get()).Log(tag, getStackTraceString("", tr));
        }
    }

    public void wtf(String tag, String msg, Throwable tr) {
        Log.wtf(tag, msg, tr);
        if (level < W) {
            PrintLogger.getInstance(context.get()).Log(tag, getStackTraceString(msg, tr));
        }
    }
}
