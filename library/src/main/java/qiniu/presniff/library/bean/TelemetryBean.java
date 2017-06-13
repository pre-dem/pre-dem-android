package qiniu.presniff.library.bean;

import static android.R.attr.path;

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
    protected float pingStddev;
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

    public float getPingStddev() {
        return pingStddev;
    }

    public void setPingStddev(float pingStddev) {
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

    public void setDefaultPingResult(){
        this.pingCode = -1;
        this.pingIP = "";
        this.pingSize = -1;
        this.pingMaxRtt = -1;
        this.pingMinRtt = -1;
        this.pingAvgRtt = -1;
        this.pingLoss = -1;
        this.pingCount = -1;
        this.pingTotalTime = -1;
        this.pingStddev = -1;
    }

    public String toJsonString() {
        StringBuilder jsonStr = new StringBuilder();
        jsonStr.append("{ ");
        jsonStr.append("\"result_id\": \"").append("").append("\", ");
        jsonStr.append("\"ping_code\": ").append(pingCode).append(", ");
        jsonStr.append("\"ping_ip\": \"").append(pingIP).append("\", ");
        jsonStr.append("\"ping_size\": ").append(pingSize).append(", ");
        jsonStr.append("\"ping_max_rtt\": ").append(pingMaxRtt).append(", ");
        jsonStr.append("\"ping_min_rtt\": ").append(pingMinRtt).append(", ");
        jsonStr.append("\"ping_avg_rtt\": ").append(pingAvgRtt).append(", ");
        jsonStr.append("\"ping_loss\": ").append(pingLoss).append(", ");
        jsonStr.append("\"ping_count\": ").append(pingCount).append(", ");
        jsonStr.append("\"ping_total_time\": ").append(pingTotalTime).append(", ");
        jsonStr.append("\"ping_stddev\": ").append(pingStddev).append(", ");
        jsonStr.append("\"tcp_code\": ").append(tcpCode).append(", ");
        jsonStr.append("\"tcp_ip\": \"").append(tcpIp).append("\", ");
        jsonStr.append("\"tcp_max_time\": ").append(tcpMaxTime).append(", ");
        jsonStr.append("\"tcp_min_time\": ").append(tcpMinTime).append(", ");
        jsonStr.append("\"tcp_avg_time\": ").append(tcpAvgTime).append(", ");
        jsonStr.append("\"tcp_loss\": ").append(tcpLoss).append(", ");
        jsonStr.append("\"tcp_count\": ").append(tcpCount).append(", ");
        jsonStr.append("\"tcp_total_time\": ").append(tcpTotalTime).append(", ");
        jsonStr.append("\"tcp_stddev\": ").append(tcpStddev).append(", ");
        jsonStr.append("\"tr_code\": ").append(trCode).append(", ");
        jsonStr.append("\"tr_ip\": \"").append(trIp).append("\", ");
        jsonStr.append("\"tr_content\": \"").append(trContent).append("\", ");
        jsonStr.append("\"dns_records\": \"").append(dnsRecords).append("\", ");
        jsonStr.append("\"http_code\": ").append(httpCode).append(", ");
        jsonStr.append("\"http_ip\": \"").append(httpIp).append("\", ");
        jsonStr.append("\"http_duration\": ").append(httpDuration).append(", ");
        jsonStr.append("\"http_body_size\": ").append(httpBodySize);
        jsonStr.append(" }");
        return jsonStr.toString();
    }

    public String toString(){
        StringBuffer str = new StringBuffer();
        str.append("pingCode:").append(pingCode).append(" ");
        str.append("pingIP:").append(pingIP).append(" ");
        str.append("pingSize:").append(pingSize).append(" ");
        str.append("pingMaxRtt:").append(pingMaxRtt).append(" ");
        str.append("pingMinRtt:").append(pingMinRtt).append(" ");
        str.append("pingAvgRtt:").append(pingAvgRtt).append(" ");
        str.append("pingLoss:").append(pingLoss).append(" ");
        str.append("pingCount:").append(pingCount).append(" ");
        str.append("pingTotalTime:").append(pingTotalTime).append(" ");
        str.append("pingStddev:").append(pingStddev).append(" ");
        str.append("tcpCode:").append(tcpCode).append(" ");
        str.append("tcpIp:").append(tcpIp).append(" ");
        str.append("tcpMaxTime:").append(tcpMaxTime).append(" ");
        str.append("tcpMinTime:").append(tcpMinTime).append(" ");
        str.append("tcpAvgTime:").append(tcpAvgTime).append(" ");
        str.append("tcpLoss:").append(tcpLoss).append(" ");
        str.append("tcpCount:").append(tcpCount).append(" ");
        str.append("tcpTotalTime:").append(tcpTotalTime).append(" ");
        str.append("tcpStddev:").append(tcpStddev).append(" ");
        str.append("trCode:").append(trCode).append(" ");
        str.append("trIp:").append(trIp).append(" ");
        str.append("trContent:").append(trContent).append(" ");
        str.append("dnsRecords:").append(dnsRecords).append(" ");
        str.append("httpCode:").append(httpCode).append(" ");
        str.append("httpIp:").append(httpIp).append(" ");
        str.append("httpDuration:").append(httpDuration).append(" ");
        str.append("httpBodySize:").append(httpBodySize).append(" ");
        return str.toString();
    }
}
