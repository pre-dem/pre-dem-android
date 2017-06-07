package qiniu.presniff.library.bean;

/**
 * Created by Misty on 17/6/7.
 */

public class TelemetryBean {
    private static final String TAG = "TelemetryBean";

    protected String resultId;
    protected int pingCode;
    protected String pingIP;
    protected int pingSize;
    protected float pingMaxRtt;
    protected float pingMinRtt;
    protected float pingAvgRtt;
    protected int pingLoss;
    protected int pingCount;
    protected long pingTotalTime;
    protected long pingStddev;
    protected int tcpCode;
    protected String tcpIp;
    protected long tcpMaxTime;
    protected long tcpMinTime;
    protected long tcpAvgTime;
    protected int tcpLoss;
    protected int tcpCount;
    protected long tcpTotalTime;
    protected long tcpStddev;
    protected int trCode;
    protected String trIp;
    protected String trContent;
    protected String dnsRecords;
    protected int httpCode;
    protected String httpIp;
    protected long httpDuration;
    protected int httpBodySize;

    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
    }

    public int getPingCode() {
        return pingCode;
    }

    public void setPingCode(int pingCode) {
        this.pingCode = pingCode;
    }

    public String getPingIP() {
        return pingIP;
    }

    public void setPingIP(String pingIP) {
        this.pingIP = pingIP;
    }

    public int getPingSize() {
        return pingSize;
    }

    public void setPingSize(int pingSize) {
        this.pingSize = pingSize;
    }

    public float getPingMaxRtt() {
        return pingMaxRtt;
    }

    public void setPingMaxRtt(float pingMaxRtt) {
        this.pingMaxRtt = pingMaxRtt;
    }

    public float getPingMinRtt() {
        return pingMinRtt;
    }

    public void setPingMinRtt(float pingMinRtt) {
        this.pingMinRtt = pingMinRtt;
    }

    public float getPingAvgRtt() {
        return pingAvgRtt;
    }

    public void setPingAvgRtt(float pingAvgRtt) {
        this.pingAvgRtt = pingAvgRtt;
    }

    public int getPingLoss() {
        return pingLoss;
    }

    public void setPingLoss(int pingLoss) {
        this.pingLoss = pingLoss;
    }

    public int getPingCount() {
        return pingCount;
    }

    public void setPingCount(int pingCount) {
        this.pingCount = pingCount;
    }

    public long getPingTotalTime() {
        return pingTotalTime;
    }

    public void setPingTotalTime(long pingTotalTime) {
        this.pingTotalTime = pingTotalTime;
    }

    public long getPingStddev() {
        return pingStddev;
    }

    public void setPingStddev(long pingStddev) {
        this.pingStddev = pingStddev;
    }

    public int getTcpCode() {
        return tcpCode;
    }

    public void setTcpCode(int tcpCode) {
        this.tcpCode = tcpCode;
    }

    public String getTcpIp() {
        return tcpIp;
    }

    public void setTcpIp(String tcpIp) {
        this.tcpIp = tcpIp;
    }

    public long getTcpMaxTime() {
        return tcpMaxTime;
    }

    public void setTcpMaxTime(long tcpMaxTime) {
        this.tcpMaxTime = tcpMaxTime;
    }

    public long getTcpMinTime() {
        return tcpMinTime;
    }

    public void setTcpMinTime(long tcpMinTime) {
        this.tcpMinTime = tcpMinTime;
    }

    public long getTcpAvgTime() {
        return tcpAvgTime;
    }

    public void setTcpAvgTime(long tcpAvgTime) {
        this.tcpAvgTime = tcpAvgTime;
    }

    public int getTcpLoss() {
        return tcpLoss;
    }

    public void setTcpLoss(int tcpLoss) {
        this.tcpLoss = tcpLoss;
    }

    public int getTcpCount() {
        return tcpCount;
    }

    public void setTcpCount(int tcpCount) {
        this.tcpCount = tcpCount;
    }

    public long getTcpTotalTime() {
        return tcpTotalTime;
    }

    public void setTcpTotalTime(long tcpTotalTime) {
        this.tcpTotalTime = tcpTotalTime;
    }

    public long getTcpStddev() {
        return tcpStddev;
    }

    public void setTcpStddev(long tcpStddev) {
        this.tcpStddev = tcpStddev;
    }

    public int getTrCode() {
        return trCode;
    }

    public void setTrCode(int trCode) {
        this.trCode = trCode;
    }

    public String getTrIp() {
        return trIp;
    }

    public void setTrIp(String trIp) {
        this.trIp = trIp;
    }

    public String getTrContent() {
        return trContent;
    }

    public void setTrContent(String trContent) {
        this.trContent = trContent;
    }

    public String getDnsRecords() {
        return dnsRecords;
    }

    public void setDnsRecords(String dnsRecords) {
        this.dnsRecords = dnsRecords;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public String getHttpIp() {
        return httpIp;
    }

    public void setHttpIp(String httpIp) {
        this.httpIp = httpIp;
    }

    public long getHttpDuration() {
        return httpDuration;
    }

    public void setHttpDuration(long httpDuration) {
        this.httpDuration = httpDuration;
    }

    public int getHttpBodySize() {
        return httpBodySize;
    }

    public void setHttpBodySize(int httpBodySize) {
        this.httpBodySize = httpBodySize;
    }
}
