package qiniu.presniff.library.bean;

/**
 * Created by Misty on 17/6/5.
 */

public class AppBean {
    /**
     * {"app_key":"test","user_id":"testuser","platform":1,"http_monitor_enabled":true,"crash_report_enabled":false,"telemetry_enabled":true}
     */
    private String appKey;
    private String userId;
    private int platform ;
    private boolean httpMonitorEnable;
    private boolean crashReportEnable;
    private boolean telemetryEnable;

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getPlatform() {
        return platform;
    }

    public void setPlatform(int platform) {
        this.platform = platform;
    }

    public boolean isHttpMonitorEnable() {
        return httpMonitorEnable;
    }

    public void setHttpMonitorEnable(boolean httpMonitorEnable) {
        this.httpMonitorEnable = httpMonitorEnable;
    }

    public boolean isCrashReportEnable() {
        return crashReportEnable;
    }

    public void setCrashReportEnable(boolean crashReportEnable) {
        this.crashReportEnable = crashReportEnable;
    }

    public boolean isTelemetryEnable() {
        return telemetryEnable;
    }

    public void setTelemetryEnable(boolean telemetryEnable) {
        this.telemetryEnable = telemetryEnable;
    }

    public void setDefault(){
        setAppKey("");
        setUserId("");
        setPlatform(1);
        setHttpMonitorEnable(false);
        setCrashReportEnable(false);
        setTelemetryEnable(false);
    }
}
