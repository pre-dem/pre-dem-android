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

    public interface NetDiagCallback {
        void complete(NetDiagBean bean);
    }
}
