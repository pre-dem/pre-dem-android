package qiniu.predem.android.bean;

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
    protected String osVersion;
    protected String deviceModel;
    protected String deviceUUID;
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

    //TODO 用list保存数据，我们直接保存到文件中，这里应该是写文件操作
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
        platform = 1;
        appName = AppBean.APP_NAME;
        appBundleId = AppBean.APP_PACKAGE;
        osVersion = AppBean.ANDROID_VERSION;
        deviceModel = AppBean.PHONE_MODEL;
        deviceUUID = AppBean.DEVICE_IDENTIFIER;
        domain = "-";
        path = "-";
        method = "GET";
        hostIP = "-";
        statusCode = -1;
        startTimestamp = -1;
        responseTimestamp = -1;
        endTimestamp = -1;
        dnsTime = -1;
        dataLength = 0;
        networkErrorCode = -1;
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
        StringBuilder jsonStr = new StringBuilder();
        jsonStr.append("{ ");
        jsonStr.append("\"platform\": \"").append(platform).append("\", ");
        jsonStr.append("\"appName\": \"").append(appName).append("\", ");
        jsonStr.append("\"appBundleId\": \"").append(appBundleId).append("\", ");
        jsonStr.append("\"osVersion\": \"").append(osVersion).append("\", ");
        jsonStr.append("\"deviceModel\": \"").append(deviceModel).append("\", ");
        jsonStr.append("\"deviceUUID\": \"").append(deviceUUID).append("\", ");
        if (domain != null) {
            jsonStr.append("\"domain\": \"").append(domain).append("\", ");
        }
        if (path != null) {
            jsonStr.append("\"path\": \"").append(path).append("\", ");
        }
        if (method != null) {
            jsonStr.append("\"method\": \"").append(method).append("\", ");
        }
        if (hostIP != null) {
            jsonStr.append("\"hostIpSet\": \"").append(hostIP).append("\", ");
        }
        jsonStr.append("\"startTimestamp\": ").append(startTimestamp).append(", ");
        jsonStr.append("\"endTimestamp\": ").append(endTimestamp).append(", ");
        jsonStr.append("\"responseTimestamp\": ").append(responseTimestamp).append(", ");
        jsonStr.append("\"dnsTime\": ").append(dnsTime).append(", ");
        jsonStr.append("\"dataLength\": ").append(dataLength).append(", ");
        jsonStr.append("\"statusCode\": ").append(statusCode).append(", ");
        jsonStr.append("\"networkErrorCode\": ").append(networkErrorCode).append(", ");
        jsonStr.append("\"networkErrorMsg\": ").append(networkErrorMsg);
        jsonStr.append(" }");
        return jsonStr.toString();
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append(platform).append("\\t");
        str.append(appName).append("\\t");
        str.append(appBundleId).append("\\t");
        str.append(osVersion).append("\\n");
        str.append(deviceModel).append("\\t");
        str.append(deviceUUID).append("\\t");
        str.append(domain).append("\\t");
        str.append(path).append("\\n");
        str.append(method).append("\\t");
        str.append(hostIP).append("\\t");
        str.append(statusCode).append("\\t");
        str.append(startTimestamp).append("\\n");
        str.append(responseTimestamp).append("\\t");
        str.append(endTimestamp).append("\\t");
        str.append(dnsTime).append("\\t");
        str.append(dataLength).append("\\n");
        str.append(networkErrorCode).append("\\t");
        str.append(networkErrorMsg).append("\\t");
        return str.toString();
    }
}
