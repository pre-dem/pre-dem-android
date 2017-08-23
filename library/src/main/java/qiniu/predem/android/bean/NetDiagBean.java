package qiniu.predem.android.bean;

import com.qiniu.android.netdiag.HttpPing;
import com.qiniu.android.netdiag.Ping;
import com.qiniu.android.netdiag.TcpPing;
import com.qiniu.android.netdiag.TraceRoute;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import qiniu.predem.android.util.Functions;

/**
 * Created by Misty on 2017/8/7.
 */

public class NetDiagBean {
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
            md5 = Functions.getStringMd5(content);
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
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("app_bundle_id",AppBean.APP_PACKAGE);
            jsonObject.put("app_name",AppBean.APP_NAME);
            jsonObject.put("app_version",AppBean.APP_VERSION);
            jsonObject.put("device_model",AppBean.PHONE_MODEL);
            jsonObject.put("os_platform",AppBean.ANDROID_VERSION);
            jsonObject.put("sdk_version",AppBean.SDK_VERSION);
            jsonObject.put("sdk_id","");
            jsonObject.put("device_id",AppBean.DEVICE_IDENTIFIER);
            jsonObject.put("tag",AppBean.APP_TAG);
            jsonObject.put("result_id",getResultID());
            jsonObject.put("ping_code",pingCode);
            jsonObject.put("ping_ip",pingIp);
            jsonObject.put("ping_size",pingSize);
            jsonObject.put("ping_max_rtt",pingMaxRtt);
            jsonObject.put("ping_min_rtt",pingMinRtt);
            jsonObject.put("ping_avg_rtt",pingAvgRtt);
            jsonObject.put("ping_loss",pingLoss);
            jsonObject.put("ping_count",pingCount);
            jsonObject.put("ping_total_time",pingTotalTime);
            jsonObject.put("ping_stddev",pingStddev);
            jsonObject.put("tcp_code",tcpCode);
            jsonObject.put("tcp_ip",tcpIp);
            jsonObject.put("tcp_max_time",tcpMaxTime);
            jsonObject.put("tcp_min_time",tcpMinTime);
            jsonObject.put("tcp_avg_time",tcpAvgTime);
            jsonObject.put("tcp_loss",tcpLoss);
            jsonObject.put("tcp_count",tcpCount);
            jsonObject.put("tcp_total_time",tcpTotalTime);
            jsonObject.put("tcp_stddev",tcpStddev);
            jsonObject.put("tr_code",trCode);
            jsonObject.put("tr_ip",trIp);
            jsonObject.put("tr_content",trContent);
            jsonObject.put("dns_records",dnsRecords);
            jsonObject.put("http_code",httpCode);
            jsonObject.put("http_ip",httpIp);
            jsonObject.put("http_duration",httpDuration);
            jsonObject.put("http_body_size",httpBodySize);
            return jsonObject.toString();
        }catch (Exception e){
            return null;
        }
    }
}
