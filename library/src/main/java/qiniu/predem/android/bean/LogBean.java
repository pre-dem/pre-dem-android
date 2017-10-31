package qiniu.predem.android.bean;

import android.graphics.Path;
import android.nfc.Tag;

import com.qiniu.android.dns.Domain;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Misty on 17/6/15.
 */

public class LogBean {
    private static final String TAG = "LogBean";
    private static final List<LogBean> mPool = new LinkedList<>();
    protected int platform;
    protected String appName;
    protected String appBundleId;
    protected String appVersion;
    protected String osVersion;
    protected String osBuild;
    protected String sdkVersion;
    protected String sdkID;
    protected String deviceModel;
    protected String deviceUUID;
    protected String userTag;
    protected String manufacturer;
    protected String domain;
    protected String path;
    protected String method;
    protected String hostIP;
    protected int statusCode;
    protected long startTimestamp;
    protected long responseTimestamp;
    protected long endTimestamp;
    protected long dnsTime;
    protected long dataLength;
    protected int networkErrorCode;
    protected String networkErrorMsg;

    protected LogBean() {
        super();
        init();
    }

    //TODO 用list保存数据，这里应该是写文件操作
    public static LogBean obtain() {
        if (mPool.size() > 0) {
            synchronized (mPool) {
                if (mPool.size() > 0) {
                    LogBean obj = mPool.get(0);
                    mPool.remove(0);
                    obj.init();
                    return obj;
                }
            }
        }
        return new LogBean();
    }

    public void release() {
        synchronized (mPool) {
            if (mPool.size() < 256) mPool.add(this);
        }
    }

    public void init() {
        platform = 2;
        appName = AppBean.APP_NAME;
        appBundleId = AppBean.APP_PACKAGE;
        appVersion = AppBean.APP_VERSION;
        osVersion = AppBean.ANDROID_VERSION;
        osBuild = AppBean.ANDROID_BUILD;
        sdkVersion = AppBean.SDK_VERSION;
        sdkID = AppBean.SDK_ID;
        deviceModel = AppBean.PHONE_MODEL;
        deviceUUID = AppBean.DEVICE_IDENTIFIER;
        manufacturer = AppBean.PHONE_MANUFACTURER;
        userTag = AppBean.APP_TAG;
        domain = "-";
        path = "-";
        method = "GET";
        hostIP = "-";
        statusCode = 0;
        startTimestamp = 0;
        responseTimestamp = 0;
        endTimestamp = 0;
        dnsTime = 0;
        dataLength = 0;
        networkErrorCode = 0;
        networkErrorMsg = "-";
    }

    public int getPlatform() {
        return platform;
    }

    public void setPlatform(int platform) {
        this.platform = platform;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppBundleId() {
        return appBundleId;
    }

    public void setAppBundleId(String appBundleId) {
        this.appBundleId = appBundleId;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDeviceUUID() {
        return deviceUUID;
    }

    public void setDeviceUUID(String deviceUUID) {
        this.deviceUUID = deviceUUID;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getHostIP() {
        return hostIP;
    }

    public void setHostIP(String hostIP) {
        this.hostIP = hostIP;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getResponseTimestamp() {
        return responseTimestamp;
    }

    public void setResponseTimestamp(long responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public long getDnsTime() {
        return dnsTime;
    }

    public void setDnsTime(long dnsTime) {
        this.dnsTime = dnsTime;
    }

    public long getDataLength() {
        return dataLength;
    }

    public void setDataLength(long dataLength) {
        this.dataLength = dataLength;
    }

    public int getNetworkErrorCode() {
        return networkErrorCode;
    }

    public void setNetworkErrorCode(int networkErrorCode) {
        this.networkErrorCode = networkErrorCode;
    }

    public String getNetworkErrorMsg() {
        return networkErrorMsg;
    }

    public void setNetworkErrorMsg(String networkErrorMsg) {
        this.networkErrorMsg = networkErrorMsg;
    }

    public String toJsonString() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("app_bundle_id",appBundleId);
            jsonObject.put("app_name",appName);
            jsonObject.put("app_version",appVersion);
            jsonObject.put("device_model",deviceModel);
            jsonObject.put("os_platform",platform);
            jsonObject.put("os_version",osVersion);
            jsonObject.put("os_build",osBuild);
            jsonObject.put("sdk_version",sdkVersion);
            jsonObject.put("sdk_id",sdkID);
            jsonObject.put("device_id",deviceUUID);
            jsonObject.put("tag",userTag);
            jsonObject.put("manufacturer",manufacturer);
            if (domain == null || domain.equals("")) {
                jsonObject.put("domain", "-");
            } else {
                jsonObject.put("domain", domain);
            }
            if (path == null || path.equals("")) {
                jsonObject.put("path", "-");
            } else {
                jsonObject.put("path", path);
            }
            if (method == null || method.equals("")) {
                jsonObject.put("method", "-");
            } else {
                jsonObject.put("method", method);
            }
            if (hostIP == null || hostIP.equals("")) {
                jsonObject.put("hostIpSet", "-");
            } else {
                jsonObject.put("hostIpSet", hostIP);
            }
            jsonObject.put("status_code",statusCode);
            jsonObject.put("startTimestamp", startTimestamp);
            jsonObject.put("responseTimestamp", responseTimestamp);
            jsonObject.put("endTimestamp", endTimestamp);
            jsonObject.put("dnsTime", dnsTime);
            jsonObject.put("dataLength", dataLength);
            jsonObject.put("networkErrorCode", networkErrorCode);
            jsonObject.put("networkErrorMsg", networkErrorMsg);
            return jsonObject.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append(appBundleId).append("\t");
        str.append(appName).append("\t");
        str.append(appVersion).append("\t");
        str.append(deviceModel).append("\t");
        str.append(platform).append("\t");
        str.append(osVersion).append("\t");
        str.append(osBuild).append("\t");
        str.append(sdkVersion).append("\t");
        if (sdkID == null || sdkID.equals("")) {
            str.append("-").append("\t");
        }else{
            str.append(sdkID).append("\t");
        }
        str.append(deviceUUID).append("\t");
        str.append(userTag).append("\t");
        str.append(manufacturer).append("\t");
        if (domain == null || domain.equals("")) {
            str.append("-").append("\t");
        } else {
            str.append(domain).append("\t");
        }
        if (path == null || path.equals("")) {
            str.append("-").append("\t");
        } else {
            str.append(path).append("\t");
        }
        str.append(method).append("\t");
        if (hostIP == null || hostIP.equals("")) {
            str.append("-").append("\t");
        } else {
            str.append(hostIP).append("\t");
        }
        str.append(statusCode).append("\t");
        str.append(startTimestamp).append("\t");
        str.append(responseTimestamp).append("\t");
        str.append(endTimestamp).append("\t");
        str.append(dnsTime).append("\t");
        str.append(dataLength).append("\t");
        str.append(networkErrorCode).append("\t");
        str.append(networkErrorMsg).append("\n");
        return str.toString();
    }
}
