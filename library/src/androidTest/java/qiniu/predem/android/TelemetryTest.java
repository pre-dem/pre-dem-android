package qiniu.predem.android;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

import qiniu.predem.android.diagnosis.NetDiagnosis;
import qiniu.predem.android.util.LogUtils;

/**
 * Created by Misty on 17/6/7.
 */
@RunWith(AndroidJUnit4.class)
public class TelemetryTest {
    private static final String TAG = "TelemetryTest";

//    private Ping.Result pingResult;
//    private TcpPing.Result tcpPingResult;
//    private HttpPing.Result httpResult;

//    @Test
//    public void pingTest() throws InterruptedException{
//        final CountDownLatch c = new CountDownLatch(1);
//        Task t = Ping.start("www.baidu.com", 10, new Ping.Callback() {
//            @Override
//            public void complete(Ping.Result r) {
//                pingResult = r;
//                c.countDown();
//            }
//        });
//        c.await(200, TimeUnit.SECONDS);
//        Assert.assertNotNull(pingResult.result);
//        LogUtils.d(TAG,"-----ping_ip:"+pingResult.ip);
//        LogUtils.d(TAG,"-----ping_size:"+pingResult.size);
//        LogUtils.d(TAG,"-----ping_max_rtt:"+pingResult.max);
//        LogUtils.d(TAG,"-----ping_min_rtt:"+pingResult.min);
//        LogUtils.d(TAG,"-----ping_avg_rtt:"+pingResult.avg);
//        LogUtils.d(TAG,"-----ping_loss:"+pingResult.dropped);
//        LogUtils.d(TAG,"-----ping_count:"+pingResult.count);
//        LogUtils.d(TAG,"-----ping_total_time:"+pingResult.time);
//        LogUtils.d(TAG,"-----ping_stddev:"+pingResult.stddev);
//    }

//    @Test
//    public void tcpPingTest() throws InterruptedException{
//        final CountDownLatch c = new CountDownLatch(1);
//        TcpPing.start("www.baidu.com", new TcpPing.Callback() {
//            @Override
//            public void complete(TcpPing.Result r) {
//                tcpPingResult = r;
//                c.countDown();
//            }
//        });
//        c.await(200, TimeUnit.SECONDS);
//        Assert.assertEquals(0, tcpPingResult.code);
//        Assert.assertTrue(tcpPingResult.avgTime >= tcpPingResult.minTime &&
//                tcpPingResult.maxTime >= tcpPingResult.avgTime);
//        Assert.assertEquals(3, tcpPingResult.count);
//        Assert.assertTrue(tcpPingResult.ip.length() >= 8);
//
//        LogUtils.d(TAG,"------tcp_code:"+tcpPingResult.code);
//        LogUtils.d(TAG,"------tcp_ip:"+tcpPingResult.ip);
//        LogUtils.d(TAG,"------tcp_max_time:"+tcpPingResult.maxTime);
//        LogUtils.d(TAG,"------tcp_min_time:"+tcpPingResult.minTime);
//        LogUtils.d(TAG,"------tcp_avg_time:"+tcpPingResult.avgTime);
//        LogUtils.d(TAG,"------tcp_loss:"+tcpPingResult.dropped);
//        LogUtils.d(TAG,"------tcp_count:"+tcpPingResult.count);
//        LogUtils.d(TAG,"------tcp_total_time:"+tcpPingResult.totalTime);
//        LogUtils.d(TAG,"------tcp_stddev:"+tcpPingResult.stddevTime);
//    }

//    @Test
//    public void traceroutTest() throws InterruptedException{
//        final ArrayList<TraceRoute.Result> l = new ArrayList<>();
//        final CountDownLatch c = new CountDownLatch(1);
//        Task t = TraceRoute.start("www.baidu.com", new Output() {
//            @Override
//            public void write(String line) {
//                System.out.println("test> " + line);
//            }
//        }, new TraceRoute.Callback() {
//            @Override
//            public void complete(TraceRoute.Result r) {
//                System.out.println(r.content());
//                l.add(r);
//                c.countDown();
//            }
//        });
//        c.await(200, TimeUnit.SECONDS);
//        Assert.assertEquals(l.size(), 1);
//        Assert.assertNotNull(l.get(0).content());
//
//        LogUtils.d(TAG,"-----tr_ip:"+l.get(0).ip);
//        LogUtils.d(TAG,"-----tr_content:"+l.get(0).content());
//    }

//    @Test
//    public void httpPingTest() throws InterruptedException {
//        final CountDownLatch c = new CountDownLatch(1);
//
//        Context appContext = InstrumentationRegistry.getTargetContext();
//        DEMManager.init(appContext);
//
//        HttpPing.start("http://www.baidu.com", new HttpPing.Callback() {
//            @Override
//            public void complete(HttpPing.Result result) {
//                httpResult = result;
//                c.countDown();
//
//            }
//        });
//        c.await(100, TimeUnit.SECONDS);
//        Assert.assertEquals(200, httpResult.code);
//        Assert.assertTrue(httpResult.duration > 0);
//        Assert.assertTrue(httpResult.headers.size() > 0);
//        Assert.assertTrue(httpResult.body.length > 0);
//
//        LogUtils.d(TAG,"-----http_code:"+httpResult.code);
//        LogUtils.d(TAG,"-----http_ip:");
//        LogUtils.d(TAG,"-----http_duration:"+httpResult.duration);
//        LogUtils.d(TAG,"-----http_body_size:"+httpResult.body.length);
//    }

//    @Test
//    public void dnsTest(){
//        String[] ip = DNS.local();
//        Assert.assertNotNull(ip);
//        Assert.assertTrue(!"".equals(ip[0]));
//        LogUtils.d(TAG,"-----local dns : " + ip[0]);

//        String s = DNS.check();
//        Assert.assertNotNull(s);
//        Assert.assertTrue(!"".equals(s));
//        LogUtils.d(TAG,"------check dns : " + s);
//    }

    @Test
    public void TelemetryTest() throws InterruptedException {
        Context appContext = InstrumentationRegistry.getTargetContext();
        final CountDownLatch c = new CountDownLatch(1);
        NetDiagnosis.start(appContext, "www.baidu.com","http://www.baidu.com", new NetDiagnosis.Callback() {
            @Override
            public void complete(boolean isSuccessful, Exception e) {
                LogUtils.d(TAG,"-----"+isSuccessful);
                System.out.println(isSuccessful);
                c.countDown();
            }
        });
//        c.await(200,TimeUnit.SECONDS);
    }

    @Test
    public void IpAddressTest() throws UnknownHostException {
        InetAddress i = InetAddress.getByName("www.baidu.com");
        LogUtils.d(TAG,"-----ip : " + i.getHostAddress());
    }
}
