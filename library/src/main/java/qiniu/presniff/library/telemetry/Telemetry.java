package qiniu.presniff.library.telemetry;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

import qiniu.presniff.library.bean.TelemetryBean;
import qiniu.presniff.library.telemetry.output.Task;
import qiniu.presniff.library.util.AsyncRun;
import qiniu.presniff.library.util.LogUtils;

import static java.lang.Runtime.getRuntime;
import static qiniu.presniff.library.telemetry.Ping.Result.trimNoneDigital;
import static qiniu.presniff.library.telemetry.TcpPing.NotReach;
import static qiniu.presniff.library.telemetry.TcpPing.TimeOut;
import static qiniu.presniff.library.telemetry.TraceRoute.getIpFromTraceMatcher;
import static qiniu.presniff.library.telemetry.TraceRoute.ipMatcher;
import static qiniu.presniff.library.telemetry.TraceRoute.timeMatcher;
import static qiniu.presniff.library.telemetry.TraceRoute.traceMatcher;

/**
 * Created by Misty on 17/6/8.
 */

public class Telemetry implements Task {
    private static final String TAG = "Telemetry";

    private static final int MaxHop = 31;
    private static final int MAX = 64 * 1024;

    private final String address;
    private volatile boolean stopped;
    private final Callback complete;
    private Context mContext;

    private Telemetry(Context context, String address, Callback complete) {
        this.mContext = context;
        this.address = address;
        this.complete = complete;
        this.stopped = false;
    }

    public static Task start(Context context, String address, Callback complete) {
        final Telemetry p = new Telemetry(context, address, complete);
        AsyncRun.runInBack(new Runnable() {
            @Override
            public void run() {
                p.run();
            }
        });
        return p;
    }

    private void run() {
        TelemetryBean r = execCmd();
        complete.complete(r);
    }

    private static String getIp(String host) throws UnknownHostException {
        InetAddress i = InetAddress.getByName(host);
        return i.getHostAddress();
    }

    private TelemetryBean execCmd() {
        TelemetryBean result = new TelemetryBean();
        String ip;
        try {
            ip = getIp(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            //TODO ping 失败
            return null;
        }
        String cmd = String.format(Locale.getDefault(), "ping -n -i %f -s %d -c %d %s", ((double) 200 / 1000), 56, 10, ip);
        Process process = null;
        StringBuilder str = new StringBuilder();
        BufferedReader reader = null;
        BufferedReader errorReader = null;
        try {
            process = getRuntime().exec(cmd);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = reader.readLine()) != null) {
                str.append(line).append("\n");
            }
            while ((line = errorReader.readLine()) != null) {
                str.append(line);
            }
            reader.close();
            errorReader.close();
            process.waitFor();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //TODO 解析ping结果
        result.setPingCode(2);
        result.setPingIP(ip);
        result.setPingSize(56);
        parsePingResult(str.toString(),result);

//        LogUtils.d(TAG,"-----ping result : " + result.toString());

        ///////////////////////////////////////
        InetSocketAddress server = new InetSocketAddress(ip, 80);
        int[] times = new int[3];
        int index = -1;
        int dropped = 0;
        for (int i = 0; i < 3 && !stopped; i++) {
            long start = System.currentTimeMillis();
            try {
                connect(server, 20 * 1000);
            } catch (IOException e) {
                e.printStackTrace();
                int code = NotReach;
                if (e instanceof SocketTimeoutException) {
                    code = TimeOut;
                }
                if (i == 0) {
                    //TODO tcpping 失败
                    return null;
                } else {
                    dropped++;
                }
            }
            long end = System.currentTimeMillis();
            int t = (int) (end - start);
            times[i] = t;
            index = i;
            try {
                if (!stopped && 100 > t && t > 0) {
                    Thread.sleep(100 - t);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (index == -1) {
            //TODO tcpping 失败
            return null;
        }
        // TODO: 17/6/8  解析 tcpping
        parseTcpPingResult(times,index,ip,dropped,result);

//        LogUtils.d(TAG,"-----tcp result : " + result.toString());

        /////////////////////////////////////////////////
        int hop = 1;
        TraceRoute.Result r = new TraceRoute.Result(ip);
        Process p;
        while (hop < MaxHop && !stopped) {
            long t1 = System.currentTimeMillis();
            try {
                p = executePingCmd(ip, hop);
            } catch (IOException e) {
                e.printStackTrace();
                // TODO: 17/6/8 tr 失败
                break;
            }
            long t2 = System.currentTimeMillis();
            String pingtOutput = getPingtOutput(p);
            if (str.length() == 0) {
                // TODO: 17/6/8 tr 失败
                break;
            }
            Matcher m = traceMatcher(pingtOutput);

            StringBuilder lineBuffer = new StringBuilder(256);
            lineBuffer.append(hop).append(".");
            if (m.find()) {
                r.append(printNormal(m, (t2 - t1) / 2, lineBuffer));
            } else {
                Matcher matchPingIp = ipMatcher(pingtOutput);
                if (matchPingIp.find()) {
                    r.append(printEnd(matchPingIp, pingtOutput, lineBuffer));
                    break;
                } else {
                    lineBuffer.append("\t\t * \t");
                }
            }
            hop++;
        }
        // TODO: 17/6/8 解析tr
        result.setTrIp(ip);
        result.setTrCode(200);
        result.setTrContent(r.content());

//        LogUtils.d(TAG,"------tr result : " + result.toString());

        /////////////////////////////////////////////////
        String ips[] = DNS.local();
        String dip = DNS.check();
        String dnsr = "localDns : " + Arrays.toString(ips) + ";dns : " + dip;
        result.setDnsRecords(dnsr);

        /////////////////////////////////////////////////
        long start = System.currentTimeMillis();
        try {
            URL u = new URL("http://"+address);
            HttpURLConnection httpConn = (HttpURLConnection) u.openConnection();
            httpConn.setConnectTimeout(10000);
            httpConn.setReadTimeout(20000);
            int responseCode = httpConn.getResponseCode();

            Map<String, List<String>> headers = httpConn.getHeaderFields();
            InputStream is = httpConn.getInputStream();
            int len = httpConn.getContentLength();
            len = len > MAX || len < 0 ? MAX : len;
            byte[] data = new byte[len];
            int read = is.read(data);
            long duration = System.currentTimeMillis() - start;
            is.close();
            if (read <= 0) {
                result.setHttpCode(responseCode);
                result.setHttpIp(ip);
                result.setHttpDuration(duration);
                result.setHttpBodySize(0);
            }else if (read < data.length) {
                byte[] b = new byte[read];
                System.arraycopy(data, 0, b, 0, read);
                result.setHttpCode(responseCode);
                result.setHttpIp(ip);
                result.setHttpDuration(duration);
                result.setHttpBodySize(0);
            }

            //上报
            sendRequest(getUrl(),result.toJsonString());

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            long duration = System.currentTimeMillis() - start;
            result.setHttpCode(-1);
            result.setHttpIp(ip);
            result.setHttpDuration(duration);
            result.setHttpBodySize(0);
            return result;
        }
    }

    private boolean sendRequest(String url, String content) {
        LogUtils.d(TAG, "------url = " + url + "\ncontent = " + content);

        HttpURLConnection httpConn;
        try {
            httpConn = (HttpURLConnection) new URL(url).openConnection();
        } catch (IOException e) {
            LogUtils.e(TAG,e.toString());
            return false;
        } catch (Exception e) {
            LogUtils.e(TAG,e.toString());
            return false;
        }
        httpConn.setConnectTimeout(3000);
        httpConn.setReadTimeout(10000);
        try {
            httpConn.setRequestMethod("POST");
        } catch (ProtocolException e) {
            LogUtils.e(TAG,e.toString());
            return false;
        }
        httpConn.setRequestProperty("Content-Type", "application/json");
        httpConn.setRequestProperty("Accept-Encoding", "identity");

        try {
            byte[] bytes = content.getBytes();
            if (bytes == null) {
                return false;
            }
            httpConn.getOutputStream().write(content.getBytes());
            httpConn.getOutputStream().flush();
        } catch (IOException e) {
            LogUtils.e(TAG,e.toString());
            return false;
        } catch (Exception e) {
            LogUtils.e(TAG,e.toString());
            return false;
        }
        int responseCode = 0;
        try {
            responseCode = httpConn.getResponseCode();
        } catch (IOException e) {
            LogUtils.e(TAG,e.toString());
            return false;
        }
        if (responseCode != 201) {
            return false;
        }
        int length = httpConn.getContentLength();
        if (length == 0) {
            return false;
        } else if (length < 0) {
            length = 16 * 1024;
        }
        InputStream is;
        try {
            is = httpConn.getInputStream();
        } catch (IOException e) {
            LogUtils.e(TAG,e.toString());
            return false;
        } catch (Exception e) {
            LogUtils.e(TAG,e.toString());
            return false;
        }
        byte[] data = new byte[length];
        int read = 0;
        try {
            read = is.read(data);
        } catch (IOException e) {
            LogUtils.e(TAG,e.toString());
            return false;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                LogUtils.e(TAG,e.toString());
                return false;
            }
        }
        if (read <= 0) {
            return false;
        }
        return true;
    }

    private TelemetryBean parseTcpPingResult(int[] times, int index, String ip, int dropped, TelemetryBean result) {
        int sum = 0;
        int min = 1000000;
        int max = 0;
        for (int i = 0; i <= index; i++) {
            int t = times[i];
            if (t > max) {
                max = t;
            }
            if (t < min) {
                min = t;
            }
            sum += t;
        }
        result.setTcpIp(ip);
        result.setTcpMaxTime(max);
        result.setTcpMinTime(min);
        result.setTcpAvgTime(sum/(index+1));
        result.setTcpTotalTime(sum);
        result.setTcpLoss(dropped);
        result.setTcpStddev(0);
        result.setTcpCount(index+1);
        return result;
    }

    private TelemetryBean parsePingResult(String str, TelemetryBean result) {
        String lastLinePrefix = "rtt min/avg/max/mdev = ";
        String packetWords = " packets transmitted";
        String receivedWords = " received";
        String totalTime = " time ";
        String[] rs = str.split("\n");
        try {
            for (String s : rs) {
                if (s.contains(packetWords)) {
                    String[] l = s.split(",");
                    if (l.length != 4) {
                        continue;
                    }
                    int count = -1;
                    int sent = -1;
                    if (l[0].length() > packetWords.length()) {
                        String s2 = l[0].substring(0, l[0].length() - packetWords.length());
                        count = Integer.parseInt(s2);
                    }
                    if (l[1].length() > receivedWords.length()) {
                        String s3 = l[1].substring(0, l[1].length() - receivedWords.length());
                        sent = Integer.parseInt(s3.trim());
                    }
                    if (l[3].length() > totalTime.length()){
                        String s3 = l[3].substring(totalTime.length(),l[3].length()-2);
                        result.setPingTotalTime(Integer.parseInt(s3));
                    }
                    result.setPingCount(count);
                    result.setPingLoss(count - sent);
                } else if (s.contains(lastLinePrefix)) {
                    String s2 = s.substring(lastLinePrefix.length(), s.length() - 3);
                    String[] l = s2.split("/");
                    if (l.length != 4) {
                        continue;
                    }
                    result.setPingMinRtt(Float.parseFloat(trimNoneDigital(l[0])));
                    result.setPingAvgRtt(Float.parseFloat(trimNoneDigital(l[1])));
                    result.setPingMaxRtt(Float.parseFloat(trimNoneDigital(l[2])));
                    result.setPingStddev(Float.parseFloat(trimNoneDigital(l[3])));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getPingtOutput(Process process) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                process.getInputStream()));
        String line;
        StringBuilder text = new StringBuilder();
        try {
            while ((line = reader.readLine()) != null) {
                text.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        process.destroy();
        return text.toString();
    }

    private String printNormal(Matcher m, long time, StringBuilder lineBuffer) {
        String pingIp = getIpFromTraceMatcher(m);
        lineBuffer.append(pingIp);
        lineBuffer.append(time); // 近似值
        lineBuffer.append("ms");

        return lineBuffer.toString();
    }

    private String printEnd(Matcher m, String out, StringBuilder lineBuffer) {
        String pingIp = m.group();
        Matcher matcherTime = timeMatcher(out);
        if (matcherTime.find()) {
            String time = matcherTime.group();
            lineBuffer.append(pingIp);
            lineBuffer.append(time);
            return lineBuffer.toString();
        }
        return "";
    }

    private Process executePingCmd(String host, int hop) throws IOException {
        String command = "ping -n -c 1 -t " + hop + " " + host;
        return getRuntime().exec(command);
    }
    
    private void connect(InetSocketAddress socketAddress, int timeOut) throws IOException {
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(socketAddress, timeOut);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getUrl(){
        return "http://hriygkee.bq.cloudappl.com/v1/net_diag/test";
    }

    public interface Callback {
        void complete(TelemetryBean r);
    }

    @Override
    public void stop() {
        stopped = true;
    }
}
