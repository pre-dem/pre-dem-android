package qiniu.predem.android.diagnosis;

import android.content.Context;

import com.qiniu.android.netdiag.DNS;
import com.qiniu.android.netdiag.HttpPing;
import com.qiniu.android.netdiag.Ping;
import com.qiniu.android.netdiag.TcpPing;
import com.qiniu.android.netdiag.TraceRoute;

import java.io.IOException;
import java.net.HttpURLConnection;

import qiniu.predem.android.DEMManager;
import qiniu.predem.android.bean.NetDiagBean;
import qiniu.predem.android.config.Configuration;
import qiniu.predem.android.http.HttpURLConnectionBuilder;
import qiniu.predem.android.util.LogUtils;

/**
 * Created by Misty on 2017/8/7.
 */

public class NetDiagnosis {
    private static final String TAG = "MyNetDiagnosis";

    private String domain;
    private String url;
    private DEMManager.NetDiagCallback complete;
    private volatile boolean stopped;
    private Context mContext;
    private int count;
    private NetDiagBean bean;

    private NetDiagnosis() {

    }

    public static NetDiagnosis getInstance(){
        return MyNetDiagnosisHolder.instance;
    }

    private static class MyNetDiagnosisHolder {
        public final static NetDiagnosis instance = new NetDiagnosis();
    }

    public void start(Context context, String domain, String url, final DEMManager.NetDiagCallback complete) {
        if (domain == null || url == null || complete == null){
            return ;
        }
        this.mContext = context;
        this.domain = domain;
        this.url = url;
        this.complete = complete;
        this.stopped = false;
        count = 0;
        bean = new NetDiagBean();

        Ping.start(domain, 10, new DiagnosisLogger(), new Ping.Callback() {
            @Override
            public void complete(Ping.Result r) {
                count++;
                bean.setPingResult(r);
                if (count == 5){
                    complete.complete(bean);
                    sendRequest(bean.toJsonString());
                }
            }
        });
        TcpPing.start(domain, new DiagnosisLogger(), new TcpPing.Callback() {
            @Override
            public void complete(TcpPing.Result r) {
                count++;
                bean.setTcpResult(r);
                if (count == 5){
                    complete.complete(bean);
                    sendRequest(bean.toJsonString());
                }
            }
        });

        TraceRoute.start(domain, new DiagnosisLogger(), new TraceRoute.Callback() {
            @Override
            public void complete(TraceRoute.Result r) {
                count++;
                bean.setTrResult(r);
                if (count == 5){
                    complete.complete(bean);
                    sendRequest(bean.toJsonString());
                }
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                String s = DNS.check();
                count++;
                bean.setDnsResult(s);
                if (count == 5){
                    complete.complete(bean);
                    sendRequest(bean.toJsonString());
                }
            }
        }).start();

        HttpPing.start(url, new DiagnosisLogger(), new HttpPing.Callback() {
            @Override
            public void complete(HttpPing.Result result) {
                count++;
                bean.setHttpResult(result);
                if (count == 5){
                    complete.complete(bean);
                    sendRequest(bean.toJsonString());
                }
            }
        });
    }

    private void sendRequest(String content) {
        LogUtils.d(TAG, "content = " + content);

        HttpURLConnection httpConn;
        try {
            httpConn = new HttpURLConnectionBuilder(Configuration.getDiagnosisUrl())
                    .setRequestMethod("POST")
                    .setHeader("Content-Type","application/json")
                    .setRequestBody(content)
                    .build();

            int responseCode = httpConn.getResponseCode();
            boolean successful = (responseCode == HttpURLConnection.HTTP_ACCEPTED || responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK);
            LogUtils.d(TAG,"-----diagnosis report result " + successful);
        } catch (IOException e) {
            LogUtils.e(TAG, e.toString());
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
        }
    }
}
