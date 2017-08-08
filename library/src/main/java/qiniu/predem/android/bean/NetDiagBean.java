package qiniu.predem.android.bean;

import com.qiniu.android.netdiag.DNS;
import com.qiniu.android.netdiag.HttpPing;
import com.qiniu.android.netdiag.Ping;
import com.qiniu.android.netdiag.TcpPing;
import com.qiniu.android.netdiag.TraceRoute;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import qiniu.predem.android.util.ToolUtil;

/**
 * Created by Misty on 2017/8/7.
 */

public class NetDiagBean {
//    private String appBundleId;
//    private String appName;
//    private String appVersion;
//    private String deviceModel;
//    private String osPlatform;
//    private String osVersion;
//    private String sdkVersion;
//    private String sdkId;
//    private String deviceId;
//    private String resultId;
    private int pingCode;
    private String pingIp;
    private int pingSize;
    private float pingMaxRtt;
    private float pingMinRtt;
    private float pingAvgRtt;
    private int pingLoss;
    private int pingCount;
    private float pingTotalTime;
    private float pingStddev;
    private int tcpCode;
    private String tcpIp;
    private float tcpMaxTime;
    private float tcpMinTime;
    private float tcpAvgTime;
    private int tcpLoss;
    private int tcpCount;
    private float tcpTotalTime;
    private int tcpStddev;
    private int trCode;
    private String trIp;
    private String trContent;
    private String dnsRecords;
    private int httpCode;
    private String httpIp;
    private float httpDuration;
    private int httpBodySize;

    public void setPingResult(Ping.Result result){
        this.pingCode = 0;
        this.pingIp = result.ip;
        this.pingSize = result.size;
        this.pingMaxRtt = result.max;
        this.pingMinRtt = result.min;
        this.pingAvgRtt = result.avg;
        this.pingLoss = result.dropped;
        this.pingCount = result.count;
        this.pingTotalTime = result.interval;
        this.pingStddev = result.stddev;
    }

    public void setTcpResult(TcpPing.Result result){
        this.tcpCode = result.code;
        this.tcpIp = result.ip;
        this.tcpMaxTime = result.maxTime;
        this.tcpMinTime = result.minTime;
        this.tcpAvgTime = result.avgTime;
        this.tcpLoss = result.dropped;
        this.tcpCount = result.count;
        this.tcpTotalTime = 0;
        this.tcpStddev = result.stddevTime;
    }

    public void setTrResult(TraceRoute.Result result){
        this.trCode = 0;
        this.trIp = result.ip;
        this.trContent = result.content();
    }

    public void setDnsResult(String result){
        this.dnsRecords = result;
    }

    public void setHttpResult(HttpPing.Result result){
        this.httpCode = result.code;
        this.httpBodySize = result.body == null ? 0 : result.body.length;
        this.httpDuration = result.duration;
        this.httpIp = "";
    }

    private String getResultID(){
        //[PREDHelper MD5:[NSString stringWithFormat:@"%f%@%@%@", [[NSDate date] timeIntervalSince1970], self.ping_ip, self.tr_content, self.dns_records]]
        String content = System.currentTimeMillis()+this.pingIp +this.trContent+this.dnsRecords;
        String md5 = null;
        try {
            md5 = ToolUtil.getStringMd5(content);
            return md5;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String toJsonString(){
        StringBuilder jsonStr = new StringBuilder();
        jsonStr.append("{ ");
        jsonStr.append("\"app_bundle_id\":\"").append(AppBean.APP_PACKAGE).append("\",");
        jsonStr.append("\"app_name\":\"").append(AppBean.APP_NAME).append("\",");
        jsonStr.append("\"app_version\":\"").append(AppBean.APP_VERSION).append("\",");
        jsonStr.append("\"device_model\":\"").append(AppBean.PHONE_MODEL).append("\",");
        jsonStr.append("\"os_platform\":\"").append(AppBean.ANDROID_PLATFORM).append("\",");
        jsonStr.append("\"os_version\":\"").append(AppBean.ANDROID_VERSION).append("\",");
        jsonStr.append("\"sdk_version\":\"").append(AppBean.SDK_VERSION).append("\",");
        jsonStr.append("\"sdk_id\":\"").append("").append("\",");
        jsonStr.append("\"device_id\":\"").append(AppBean.DEVICE_IDENTIFIER).append("\",");
        jsonStr.append("\"result_id\": \"").append(getResultID()).append("\", ");
        jsonStr.append("\"ping_code\": ").append(pingCode).append(", ");
        jsonStr.append("\"ping_ip\": \"").append(pingIp).append("\", ");
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
}
