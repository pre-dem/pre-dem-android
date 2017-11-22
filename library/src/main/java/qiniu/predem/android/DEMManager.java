package qiniu.predem.android;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

import qiniu.predem.android.bean.NetDiagBean;
import qiniu.predem.android.core.DEMImpl;

/**
 * Created by Misty on 17/6/15.
 */
public final class DEMManager {
    public static void start(String domain, String appKey, Context context) {
        DEMImpl.instance().start(domain, appKey, context.getApplicationContext());
    }

    public static void setUserTag(String userid) {
        DEMImpl.instance().setUserTag(userid);
    }

    public static void trackEvent(String eventName, Map<String, Object> event) {
        DEMImpl.instance().trackEvent(eventName, event);
    }

    public static void trackEvent(String eventName, JSONArray event) {
        DEMImpl.instance().trackEvent(eventName, event);
    }

    public static void trackEvent(String eventName, JSONObject event) {
        DEMImpl.instance().trackEvent(eventName, event);
    }

    public static void netDiag(String domain, String address, NetDiagCallback netDiagCallback) {
        DEMImpl.instance().netDiag(domain, address, netDiagCallback);
    }

    public static void startLogging(int level) {
        DEMImpl.instance().openLogs(level);
    }

    public static void stopLogging() {
        DEMImpl.instance().closeLogs();
    }

    public static void i(String tag, String msg) {
        DEMImpl.instance().i(tag, msg);
    }

    public static void i(String tag, String msg, Throwable tr) {
        DEMImpl.instance().i(tag, msg, tr);
    }

    public static void v(String tag, String msg) {
        DEMImpl.instance().v(tag, msg);
    }

    public static void v(String tag, String msg, Throwable tr) {
        DEMImpl.instance().v(tag, msg, tr);
    }

    public static void d(String tag, String msg) {
        DEMImpl.instance().d(tag, msg);
    }

    public static void d(String tag, String msg, Throwable tr) {
        DEMImpl.instance().d(tag, msg, tr);
    }

    public static void w(String tag, String msg) {
        DEMImpl.instance().w(tag, msg);
    }

    public static void w(String tag, String msg, Throwable tr) {
        DEMImpl.instance().w(tag, msg, tr);
    }

    public static void e(String tag, String msg) {
        DEMImpl.instance().e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        DEMImpl.instance().e(tag, msg, tr);
    }

    public interface NetDiagCallback {
        void complete(NetDiagBean bean);
    }
}
