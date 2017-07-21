package qiniu.predem.android.bean;

import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import qiniu.predem.android.util.LogUtils;

import static qiniu.predem.android.config.FileConfig.FIELD_APP_BUNDLE_ID;
import static qiniu.predem.android.config.FileConfig.FIELD_APP_NAME;
import static qiniu.predem.android.config.FileConfig.FIELD_APP_VERSION;
import static qiniu.predem.android.config.FileConfig.FIELD_DEVICE_ID;
import static qiniu.predem.android.config.FileConfig.FIELD_DEVICE_MODEL;
import static qiniu.predem.android.config.FileConfig.FIELD_OS_BUILD;
import static qiniu.predem.android.config.FileConfig.FIELD_OS_PLATFORM;
import static qiniu.predem.android.config.FileConfig.FIELD_REPORT_UUID;
import static qiniu.predem.android.config.FileConfig.FIELD_SDK_ID;
import static qiniu.predem.android.config.FileConfig.FIELD_SDK_VERSION;
import static qiniu.predem.android.config.FileConfig.FILELD_CRASH_CONTENT;
import static qiniu.predem.android.config.FileConfig.FILELD_CRASH_TIME;
import static qiniu.predem.android.config.FileConfig.FILELD_MANUFACTURER;
import static qiniu.predem.android.config.FileConfig.FILELD_START_TIME;

/**
 * Created by Misty on 17/6/15.
 */

public class CrashBean {
    private static final String TAG = "CrashBean";

    private String AppBundleId;
    private String AppName;
    private String AppVersion;
    private String DeviceModel;
    private String OsPlatform;
    private String OsBuild;
    private String SDKVersion;
    private String SDKId;
    private String DeviceId;
    private String ReportUUID;
    private String Manufacturer;
    private String StartTime;
    private String CrashTime;
    private String throwableStackTrace;

    public CrashBean(String crashIdentifier, Throwable throwable) {

        final Writer stackTraceResult = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stackTraceResult);
        throwable.printStackTrace(printWriter);
        throwableStackTrace = stackTraceResult.toString();

        //init info
        this.AppBundleId = AppBean.APP_PACKAGE;
        this.AppName = AppBean.APP_NAME;
        this.AppVersion = AppBean.APP_VERSION;
        this.DeviceModel = AppBean.PHONE_MODEL;
        this.DeviceId = AppBean.DEVICE_IDENTIFIER;
        this.OsPlatform = AppBean.ANDROID_PLATFORM;
        this.OsBuild = AppBean.ANDROID_BUILD;
        this.SDKVersion = AppBean.SDK_VERSION;
        this.SDKId = AppBean.SDK_NAME;
        this.ReportUUID = crashIdentifier;
        this.Manufacturer = AppBean.PHONE_MANUFACTURER;
    }

    public String getThrowableStackTrace(){
        return throwableStackTrace;
    }

    public String getCrashIdentifier(){
        return this.ReportUUID;
    }

    public String getAppBundleId() {
        return AppBundleId;
    }

    public String getAppName() {
        return AppName;
    }

    public String getAppVersion() {
        return AppVersion;
    }

    public String getDeviceModel() {
        return DeviceModel;
    }

    public String getOsPlatform() {
        return OsPlatform;
    }

    public String getOsBuild() {
        return OsBuild;
    }

    public String getSDKVersion() {
        return SDKVersion;
    }

    public String getSDKId() {
        return SDKId;
    }

    public String getDeviceId() {
        return DeviceId;
    }

    public String getReportUUID() {
        return ReportUUID;
    }

    public String getManufacturer() {
        return Manufacturer;
    }

    public String getStartTime() {
        return StartTime;
    }

    public void setStartTime(String startTime) {
        StartTime = startTime;
    }

    public String getCrashTime() {
        return CrashTime;
    }

    public void setCrashTime(String crashTime) {
        CrashTime = crashTime;
    }

    public String ToJsonString(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(FIELD_APP_BUNDLE_ID, getAppBundleId());
            jsonObject.put(FIELD_APP_VERSION,getAppVersion());
            jsonObject.put(FIELD_APP_NAME, getAppName());
            jsonObject.put(FIELD_DEVICE_MODEL,getDeviceModel());
            jsonObject.put(FIELD_OS_PLATFORM,getOsPlatform());
            jsonObject.put(FIELD_OS_BUILD,getOsBuild());
            jsonObject.put(FIELD_SDK_VERSION,getSDKVersion());
            jsonObject.put(FIELD_SDK_ID,getSDKId());
            jsonObject.put(FIELD_DEVICE_ID,getDeviceId());
            jsonObject.put(FIELD_REPORT_UUID,getReportUUID());
            jsonObject.put(FILELD_MANUFACTURER,getManufacturer());
            jsonObject.put(FILELD_START_TIME,getStartTime());
            jsonObject.put(FILELD_CRASH_TIME,getCrashTime());
            jsonObject.put(FILELD_CRASH_CONTENT,getThrowableStackTrace());
            return jsonObject.toString();
        }catch (Exception e){
            LogUtils.e(TAG,e.toString());
            e.printStackTrace();
        }
        return null;
    }
}
