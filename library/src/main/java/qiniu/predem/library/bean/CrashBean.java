package qiniu.predem.library.bean;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;

/**
 * Created by Misty on 17/6/15.
 */

public class CrashBean {
    private static final String TAG = "CrashBean";

    private final String crashIdentifier;

    private String reporterKey;

    private Date appStartDate;
    private Date appCrashDate;

    private String osVersion;
    private String osBuild;
    private String deviceManufacturer;
    private String deviceModel;

    private String appPackage;
    private String appVersionName;
    private String appVersionCode;

    private String threadName;

    private String throwableStackTrace;

    private Boolean isXamarinException;

    private String format;

    public CrashBean(String crashIdentifier) {
        this.crashIdentifier = crashIdentifier;
        isXamarinException = false;
        throwableStackTrace = "";
    }

    public CrashBean(String crashIdentifier, Throwable throwable) {
        this(crashIdentifier);

        isXamarinException = false;

        final Writer stackTraceResult = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stackTraceResult);
        throwable.printStackTrace(printWriter);
        throwableStackTrace = stackTraceResult.toString();
    }

    public String getCrashIdentifier() {
        return crashIdentifier;
    }

    public String getReporterKey() {
        return reporterKey;
    }

    public void setReporterKey(String reporterKey) {
        this.reporterKey = reporterKey;
    }

    public Date getAppStartDate() {
        return appStartDate;
    }

    public void setAppStartDate(Date appStartDate) {
        this.appStartDate = appStartDate;
    }

    public Date getAppCrashDate() {
        return appCrashDate;
    }

    public void setAppCrashDate(Date appCrashDate) {
        this.appCrashDate = appCrashDate;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getOsBuild() {
        return osBuild;
    }

    public void setOsBuild(String osBuild) {
        this.osBuild = osBuild;
    }

    public String getDeviceManufacturer() {
        return deviceManufacturer;
    }

    public void setDeviceManufacturer(String deviceManufacturer) {
        this.deviceManufacturer = deviceManufacturer;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }

    public String getAppVersionName() {
        return appVersionName;
    }

    public void setAppVersionName(String appVersionName) {
        this.appVersionName = appVersionName;
    }

    public String getAppVersionCode() {
        return appVersionCode;
    }

    public void setAppVersionCode(String appVersionCode) {
        this.appVersionCode = appVersionCode;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getThrowableStackTrace() {
        return throwableStackTrace;
    }

    public void setThrowableStackTrace(String throwableStackTrace) {
        this.throwableStackTrace = throwableStackTrace;
    }

    public Boolean getXamarinException() {
        return isXamarinException;
    }

    public void setXamarinException(Boolean xamarinException) {
        isXamarinException = xamarinException;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
