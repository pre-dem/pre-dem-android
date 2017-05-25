package qiniu.presniff.library.bean;

/**
 * Created by Misty on 5/18/17.
 */

import java.util.LinkedList;
import java.util.List;

import qiniu.presniff.library.config.ConstantConfig;


/**
 * {
 platform          int    // 上报的客户端类型，1: iOS, 2: Android
 appName           string // 宿主 App 的名字。
 appBundleId       string // 宿主 App 的唯一标识号(包名)
 osVersion         string // 系统版本号
 deviceModel       string // 设备型号
 deviceUUID        string // 设备唯一识别号
 domain            string // 请求的 Domain Name
 path              string // 请求的 Path
 method            string // 请求使用的 HTTP 方法，如 POST 等
 hostIP            string // 实际发生请求的主机 IP 地址
 statusCode        int    // 服务器返回的 HTTP 状态码
 startTimestamp    uint64 // 请求开始时间戳，单位是 Unix ms
 responseTimeStamp uint64 // 服务器返回 Response 的时间戳，单位是 Unix ms
 endTimestamp      uint64 // 请求结束时间戳，单位是 Unix ms
 dnsTime           uint   // 请求的 DNS 解析时间, 单位是 ms
 dataLength        uint   // 请求返回的 data 的总长度，单位是 byte
 networkErrorCode  int    // 请求发生网络错误时的错误码
 networkErrorMsg   string // 请求发生网络错误时的错误信息
 }
 *
 * */

public class LogBean {
    private static final String TAG = "LogBean";

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

    private static final List<LogBean> mPool = new LinkedList<>();

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

    protected LogBean(){
        super();
        init();
    }

    public void init(){
        platform = 1;
        appName = ConstantConfig.APP_NAME;
        appBundleId = ConstantConfig.APP_PACKAGE;
        osVersion = ConstantConfig.ANDROID_VERSION;
        deviceModel = ConstantConfig.PHONE_MODEL;
        deviceUUID = ConstantConfig.DEVICE_IDENTIFIER;
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
        if (path != null){
            jsonStr.append("\"path\": \"").append(path).append("\", ");
        }
        if (method != null){
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

    public String toString(){
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
