package qiniu.predem.android.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Misty on 2017/10/23.
 */

public class CustomBean {
    private String appBundleId;
    private String appName;
    private String appVersion;

    private String deviceModel;
    private String manufacturer;
    private String deviceId;

    private String osPlatform;
    private String osVersion;
    private String osBuild;

    private String sdkVersion;
    private String sdkId;

    private String tag;

    private String clientIp;

    private String type;
    private String name;
    private String content;

    public CustomBean(String name, String content){
        this.appBundleId = AppBean.APP_PACKAGE;
        this.appName = AppBean.APP_NAME;
        this.appVersion = AppBean.APP_VERSION;

        this.deviceModel = AppBean.PHONE_MODEL;
        this.manufacturer = AppBean.PHONE_MANUFACTURER;
        this.deviceId = AppBean.DEVICE_IDENTIFIER;

        this.osPlatform = AppBean.ANDROID_PLATFORM;
        this.osVersion = AppBean.ANDROID_VERSION;
        this.osBuild = AppBean.ANDROID_BUILD;

        this.sdkVersion = AppBean.SDK_VERSION;
        this.sdkId = AppBean.SDK_ID;

        this.tag = AppBean.APP_TAG;

        this.clientIp = "";

        this.type = "custom";
        this.name = name;
        this.content = content;
    }

    public String toJsonString(){
        JSONObject object = new JSONObject();
        try {
            object.put("app_bundle_id",this.appBundleId);
            object.put("app_name",this.appName);
            object.put("app_version",this.appVersion);

            object.put("device_model",this.deviceModel);
            object.put("manufacturer",this.manufacturer);
            object.put("device_id",this.deviceId);

            object.put("os_platform",this.osPlatform);
            object.put("os_version",this.osVersion);
            object.put("os_build",this.osBuild);

            object.put("sdk_version",this.sdkVersion);
            object.put("sdk_id",this.sdkId);

            object.put("tag",tag);

            object.put("type",this.type);
            object.put("name",this.name);
            object.put("content",this.content);
            return object.toString();
        }catch (JSONException e){
            e.printStackTrace();
            return "";
        }
    }
}
