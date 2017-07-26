package qiniu.predem.android.core;

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
import java.util.Map;

import qiniu.predem.android.DEMManager;
import qiniu.predem.android.bean.AppBean;
import qiniu.predem.android.block.TraceInfoCatcher;
import qiniu.predem.android.config.Configuration;
import qiniu.predem.android.crash.CrashManager;
import qiniu.predem.android.diagnosis.NetDiagnosis;
import qiniu.predem.android.http.HttpMonitorManager;
import qiniu.predem.android.http.HttpURLConnectionBuilder;
import qiniu.predem.android.util.LogUtils;
import qiniu.predem.android.util.SharedPreUtil;

/**
 * Created by long on 2017/7/4.
 */

public final class DEMImpl {
    private static final String TAG = "DEMManager";

    private static final DEMImpl _instance = new DEMImpl();
    private WeakReference<Context> context;

    public static DEMImpl instance() {
        return _instance;
    }

    public static String getApp() {
        return "app_key:" + Configuration.appKey
                + ",http_monitor_enabled:" + Configuration.httpMonitorEnable
                + ",crash_report_enable:" + Configuration.crashReportEnable;
    }

    public void start(String domain, String appKey, Context context) {
        this.context = new WeakReference<>(context);

        LogUtils.d(TAG,"DemManager start");

        //获取AppBean信息
        Configuration.init(context, appKey, domain);
        if (askForConfiguration(context)) {
            updateAppConfig(context);
        }else{
            //获取各项上报开关
            Configuration.httpMonitorEnable = SharedPreUtil.getHttpMonitorEnable(context);
            Configuration.crashReportEnable = SharedPreUtil.getCrashReportEnable(context);
            Configuration.webviewEnable = SharedPreUtil.getWebviewEnable(context);
            Configuration.lagMonitorEnable = SharedPreUtil.getLagMonitorEnable(context);

            if (Configuration.httpMonitorEnable) {
                HttpMonitorManager.getInstance().register(context);
            }
            if (Configuration.crashReportEnable) {
                CrashManager.register(context);
            }
            if (Configuration.lagMonitorEnable){
                TraceInfoCatcher traceInfoCatcher = new TraceInfoCatcher(context);
                traceInfoCatcher.start();
            }
        }
        LogUtils.d(TAG,"Http monitor " + Configuration.httpMonitorEnable);
        LogUtils.d(TAG,"Crash report " + Configuration.crashReportEnable);
        LogUtils.d(TAG,"WebView monitor " + Configuration.webviewEnable);
        LogUtils.d(TAG,"Lag monitor " + Configuration.lagMonitorEnable);
    }

    private static boolean askForConfiguration(Context context) {
        long lastTime = SharedPreUtil.getConfigurationLastTime(context);
        long now = System.currentTimeMillis();
        //超过一天更新config
        if (lastTime == -1 || (now - lastTime) > 86400000) {
            SharedPreUtil.setLastTime(context);
            return true;
        }
        return false;
    }

    public  void updateAppConfig(final Context cxt) {
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
                    parameters.put("os_platform",AppBean.ANDROID_PLATFORM);
                    parameters.put("os_version",AppBean.ANDROID_VERSION);
                    parameters.put("sdk_version", AppBean.SDK_VERSION);
                    parameters.put("sdk_id",AppBean.DEVICE_IDENTIFIER);
                    parameters.put("device_id",AppBean.PHONE_MANUFACTURER);

                    HttpURLConnection httpConn = new HttpURLConnectionBuilder(Configuration.getConfigUrl())
                            .setRequestMethod("POST")
                            .setHeader("Content-Type", "application/json")
                            .setRequestBody(parameters.toString())
                            .build();

                    int responseCode = httpConn.getResponseCode();
                    boolean successful = (responseCode == HttpURLConnection.HTTP_ACCEPTED || responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK);
                    if (successful){
                        byte[] data = new byte[4*1024];
                        in = httpConn.getInputStream();
                        in.read(data);
                        JSONObject jsonObject = new JSONObject(new String(data));
                        SharedPreUtil.setHttpMonitorEnable(cxt, true);//jsonObject.optBoolean(HTTP_MONITOR_ENABLE));
                        SharedPreUtil.setCrashReportEable(cxt, true);//jsonObject.optBoolean(CRASH_REPORT_ENABLE));
                        SharedPreUtil.setWebviewEnable(cxt,true);//jsonObject.optBoolean(WEBVIEW_ENABLE));
                        SharedPreUtil.setLagMonitorEnable(cxt,true);//jsonObject.optBoolean(LAG_MONITOR_ENABLE));
                    }
                    //获取各项上报开关
                    Configuration.httpMonitorEnable = SharedPreUtil.getHttpMonitorEnable(cxt);
                    Configuration.crashReportEnable = SharedPreUtil.getCrashReportEnable(cxt);
                    Configuration.webviewEnable = SharedPreUtil.getWebviewEnable(cxt);
                    Configuration.lagMonitorEnable = SharedPreUtil.getLagMonitorEnable(cxt);

                    if (Configuration.httpMonitorEnable) {
                        HttpMonitorManager.getInstance().register(cxt);
                    }
                    if (Configuration.crashReportEnable) {
                        CrashManager.register(cxt);
                    }
                    if (Configuration.lagMonitorEnable){
                        TraceInfoCatcher traceInfoCatcher = new TraceInfoCatcher(cxt);
                        traceInfoCatcher.start();
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }finally {
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

    public void netDiag(String domain, String address, DEMManager.NetDiagCallback netDiagCallback) {
        NetDiagnosis.start(this.context.get(), domain, address, netDiagCallback);
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
