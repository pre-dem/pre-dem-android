package qiniu.predem.android;

import android.content.Context;

import org.json.JSONObject;

import java.util.Map;

import qiniu.predem.android.core.DEMImpl;

/**
 * Created by Misty on 17/6/15.
 */

//public class DEMManager {
//    private static final String TAG = "DEMManager";
//
//    protected static boolean enable = true;
//
//    public static void init(Context context) {
//        //获取AppBean信息
//        Configuration.init(context);
//        if (askForConfiguration(context)) {
//            updateAppConfig();
//        }
//
//        if (Configuration.httpMonitorEnable) {
//            HttpMonitorManager.getInstance().register(context);
//        }
//        if (Configuration.crashReportEnable) {
//            CrashManager.register(context);
//        }
//    }
//
//    public static void unInit() {
//        if (Configuration.httpMonitorEnable) {
//            HttpMonitorManager.getInstance().unregister();
//        }
//    }
//
//    private static boolean askForConfiguration(Context context) {
//        long lastTime = SharedPreUtil.getConfigurationLastTime(context);
//        if (lastTime == -1) {
//            return true;
//        }
//        long now = System.currentTimeMillis();
//        if ((now - lastTime) > 86400000) {
//            return true;
//        }
//        return false;
//    }
//
//    public static void updateAppConfig() {
//        enable = false;
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                URL url = null;
//                try {
//                    JSONObject parameters = new JSONObject();
//                    parameters.put("app_bundle_id", AppBean.APP_PACKAGE);
//                    parameters.put("app_name", AppBean.APP_NAME);
//                    parameters.put("app_version", AppBean.APP_VERSION);
//                    parameters.put("device_model", AppBean.PHONE_MODEL);
//                    parameters.put("os_platform","a");
//                    parameters.put("os_version",AppBean.ANDROID_VERSION);
//                    parameters.put("sdk_version", AppBean.SDK_VERSION);
//                    parameters.put("sdk_id",AppBean.ANDROID_BUILD);
//                    parameters.put("device_id",AppBean.DEVICE_IDENTIFIER);
//
//                    HttpURLConnection httpConn = (HttpURLConnection) new URL(HttpConfig.getConfigUrl()).openConnection();
//
//                    httpConn.setConnectTimeout(3000);
//                    httpConn.setReadTimeout(10000);
//                    httpConn.setRequestMethod("POST");
//
//                    httpConn.setRequestProperty("Content-Type", "application/json");
//                    httpConn.setRequestProperty("Accept-Encoding", "identity");
//
//                    byte[] bytes = parameters.toString().getBytes();
//
//                    ByteArrayOutputStream compressed = new ByteArrayOutputStream();
//                    GZIPOutputStream gzip = new GZIPOutputStream(compressed);
//                    gzip.write(bytes);
//                    gzip.close();
//                    httpConn.getOutputStream().write(compressed.toByteArray());
//                    httpConn.getOutputStream().flush();
//
//                    int responseCode = 0;
//                    try {
//                        responseCode = httpConn.getResponseCode();
//                    } catch (IOException e) {
//                        LogUtils.e(TAG, e.toString());
//                    }
//
//                    LogUtils.e("-----configuration code : " + responseCode);
//                    LogUtils.e("-----configuration body : " + httpConn.getResponseMessage());
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

public final class DEMManager {
    public static void start(String domain, String appKey, Context context){
        DEMImpl.instance().start(domain, appKey, context.getApplicationContext());
    }

    public static void trackEvent(String eventName, Map<String, Object> event){
        DEMImpl.instance().trackEvent(eventName, event);
    }

    public static void trackEvent(String eventName, JSONObject event){
        DEMImpl.instance().trackEvent(eventName, event);
    }

    public static void netDiag(String domain, String address, NetDiagCallback netDiagCallback){
        DEMImpl.instance().netDiag(domain, address, netDiagCallback);
    }

    public interface NetDiagCallback {
        void complete(boolean isSuccessful, Exception e);
    }
}
