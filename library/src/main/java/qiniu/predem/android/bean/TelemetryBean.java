package qiniu.predem.android.bean;

import java.util.List;
import java.util.Map;

/**
 * Created by Misty on 17/6/15.
 */

public class TelemetryBean {
    private static final String TAG = "TelemetryBean";

    public String resultId;
    public PingResult pingResult;
    public TCPResult tcpResult;
    public TraceRouteResult trResult;
    public HttpResult httpResult;
    public String dnsRecords;

    public String toJsonString() {
        StringBuilder jsonStr = new StringBuilder();
        jsonStr.append("{ ");
        jsonStr.append("\"result_id\": \"").append(resultId).append("\", ");
        jsonStr.append(pingResult.toJsonString());
        jsonStr.append(tcpResult.toJsonString());
        jsonStr.append(trResult.toJsonString());
        jsonStr.append("\"dns_records\": \"").append(dnsRecords).append("\", ");
        jsonStr.append(httpResult.toJsonString());
        jsonStr.append(" }");
        return jsonStr.toString();
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("result_id").append(resultId).append(" ");
        str.append(pingResult.toString());
        str.append(tcpResult.toString());
        str.append(trResult.toString());
        str.append("dnsRecords:").append(dnsRecords).append(" ");
        str.append(httpResult.toString());
        return str.toString();
    }

    public static class PingResult {
        public final String result;
        public final String ip;
        public final int size;
        //        public final int interval;
        private final String lastLinePrefix = "rtt min/avg/max/mdev = ";
        private final String packetWords = " packets transmitted";
        private final String receivedWords = " received";
        private final String totalTime = " time ";
        public int sent;
        public int dropped;
        public float max;
        public float min;
        public float avg;
        public float stddev;
        public int count;
        public int time;

        public PingResult(String result, String ip, int size) {
            this.result = result;
            this.ip = ip;
            this.size = size;
//            this.interval = interval;
            parseResult();
        }

        String trimNoneDigital(String s) {
            if (s == null || s.length() == 0) {
                return "";
            }
            char[] v = s.toCharArray();
            char[] v2 = new char[v.length];
            int j = 0;
            for (char aV : v) {
                if ((aV >= '0' && aV <= '9') || aV == '.') {
                    v2[j++] = aV;
                }
            }
            return new String(v2, 0, j);
        }

        private void parseRttLine(String s) {
            String s2 = s.substring(lastLinePrefix.length(), s.length() - 3);
            String[] l = s2.split("/");
            if (l.length != 4) {
                return;
            }
            min = Float.parseFloat(trimNoneDigital(l[0]));
            avg = Float.parseFloat(trimNoneDigital(l[1]));
            max = Float.parseFloat(trimNoneDigital(l[2]));
            stddev = Float.parseFloat(trimNoneDigital(l[3]));
        }

        private void parsePacketLine(String s) {
            String[] l = s.split(",");
            if (l.length != 4) {
                return;
            }
            if (l[0].length() > packetWords.length()) {
                String s2 = l[0].substring(0, l[0].length() - packetWords.length());
                count = Integer.parseInt(s2);
            }
            if (l[1].length() > receivedWords.length()) {
                String s3 = l[1].substring(0, l[1].length() - receivedWords.length());
                sent = Integer.parseInt(s3.trim());
            }
            if (l[3].length() > totalTime.length()) {
                String s3 = l[3].substring(totalTime.length(), l[3].length() - 2);
                time = Integer.parseInt(s3);
            }

            dropped = count - sent;
        }

        private void parseResult() {
            String[] rs = result.split("\n");
            try {
                for (String s : rs) {
                    if (s.contains(packetWords)) {
                        parsePacketLine(s);
                    } else if (s.contains(lastLinePrefix)) {
                        parseRttLine(s);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String toJsonString() {
            StringBuilder jsonStr = new StringBuilder();
            jsonStr.append("\"ping_code\": ").append(200).append(", ");
            jsonStr.append("\"ping_ip\": \"").append(ip).append("\", ");
            jsonStr.append("\"ping_size\": ").append(size).append(", ");
            jsonStr.append("\"ping_max_rtt\": ").append(max).append(", ");
            jsonStr.append("\"ping_min_rtt\": ").append(min).append(", ");
            jsonStr.append("\"ping_avg_rtt\": ").append(avg).append(", ");
            jsonStr.append("\"ping_loss\": ").append(dropped).append(", ");
            jsonStr.append("\"ping_count\": ").append(count).append(", ");
            jsonStr.append("\"ping_total_time\": ").append(totalTime).append(", ");
            jsonStr.append("\"ping_stddev\": ").append(stddev).append(", ");
            return jsonStr.toString();
        }

        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append("pingCode:").append(200).append(" ");
            str.append("pingIP:").append(ip).append(" ");
            str.append("pingSize:").append(size).append(" ");
            str.append("pingMaxRtt:").append(max).append(" ");
            str.append("pingMinRtt:").append(min).append(" ");
            str.append("pingAvgRtt:").append(avg).append(" ");
            str.append("pingLoss:").append(dropped).append(" ");
            str.append("pingCount:").append(count).append(" ");
            str.append("pingTotalTime:").append(totalTime).append(" ");
            str.append("pingStddev:").append(stddev).append(" ");
            return str.toString();
        }
    }

    public static class TCPResult {
        public final int code;
        public final String ip;
        public final int maxTime;
        public final int minTime;
        public final int avgTime;
        public final int stddevTime;
        public final int count;
        public final int dropped;
        public final long totalTime;

        public TCPResult(int code, String ip, int maxTime, int minTime, int avgTime, long totalTime,
                         int stddevTime, int count, int dropped) {
            this.code = code;
            this.ip = ip;
            this.maxTime = maxTime;
            this.minTime = minTime;
            this.avgTime = avgTime;
            this.totalTime = totalTime;
            this.stddevTime = stddevTime;
            this.count = count;
            this.dropped = dropped;
        }

        public String toJsonString() {
            StringBuilder jsonStr = new StringBuilder();
            jsonStr.append("\"tcp_code\": ").append(code).append(", ");
            jsonStr.append("\"tcp_ip\": \"").append(ip).append("\", ");
            jsonStr.append("\"tcp_max_time\": ").append(maxTime).append(", ");
            jsonStr.append("\"tcp_min_time\": ").append(minTime).append(", ");
            jsonStr.append("\"tcp_avg_time\": ").append(avgTime).append(", ");
            jsonStr.append("\"tcp_loss\": ").append(dropped).append(", ");
            jsonStr.append("\"tcp_count\": ").append(count).append(", ");
            jsonStr.append("\"tcp_total_time\": ").append(totalTime).append(", ");
            jsonStr.append("\"tcp_stddev\": ").append(stddevTime).append(", ");
            return jsonStr.toString();
        }

        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append("tcpCode:").append(code).append(" ");
            str.append("tcpIp:").append(ip).append(" ");
            str.append("tcpMaxTime:").append(maxTime).append(" ");
            str.append("tcpMinTime:").append(minTime).append(" ");
            str.append("tcpAvgTime:").append(avgTime).append(" ");
            str.append("tcpLoss:").append(dropped).append(" ");
            str.append("tcpCount:").append(count).append(" ");
            str.append("tcpTotalTime:").append(totalTime).append(" ");
            str.append("tcpStddev:").append(stddevTime).append(" ");
            return str.toString();
        }
    }

    public static class TraceRouteResult {
        public final String ip;
        private final StringBuilder builder = new StringBuilder();
        private String allData;

        public TraceRouteResult(String ip) {
            this.ip = ip;
        }

        public String content() {
            if (allData != null) {
                return allData;
            }
            allData = builder.toString();
            return allData;
        }

        public void append(String str) {
            builder.append(str);
        }

        public String toJsonString() {
            StringBuilder jsonStr = new StringBuilder();
            jsonStr.append("\"tr_code\": ").append(200).append(", ");
            jsonStr.append("\"tr_ip\": \"").append(ip).append("\", ");
            jsonStr.append("\"tr_content\": \"").append(content()).append("\", ");
            return jsonStr.toString();
        }

        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append("trCode:").append(200).append(" ");
            str.append("trIp:").append(ip).append(" ");
            str.append("trContent:").append(content()).append(" ");
            return str.toString();
        }
    }

    public static class HttpResult {
        public final int code;
        public final Map<String, List<String>> headers;
        public final byte[] body;
        public final int duration;
        public final String errorMessage;

        public HttpResult(int code,
                          Map<String, List<String>> headers, byte[] body, int duration, String errorMessage) {
            this.code = code;
            this.headers = headers;
            this.body = body;
            this.duration = duration;
            this.errorMessage = errorMessage;
        }

        public String toJsonString() {
            StringBuilder jsonStr = new StringBuilder();
            jsonStr.append("\"http_code\": ").append(code).append(", ");
//            jsonStr.append("\"http_ip\": \"").append(httpIp).append("\", ");
            jsonStr.append("\"http_duration\": ").append(duration).append(", ");
            jsonStr.append("\"http_body_size\": ").append(body == null ? 0 : body.length);
            return jsonStr.toString();
        }

        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append("httpCode:").append(code).append(" ");
//            str.append("httpIp:").append(httpIp).append(" ");
            str.append("httpDuration:").append(duration).append(" ");
            str.append("httpBodySize:").append(body.length).append(" ");
            return str.toString();
        }
    }
}
