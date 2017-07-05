package qiniu.predem.example;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import qiniu.predem.android.DEMManager;
import qiniu.predem.android.util.AsyncRun;
import qiniu.predem.android.util.LogUtils;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private NetworkAsyncTask networkAsyncTask;

    private Button http_btn;
    private Button crash_btn;
    private Button okhttp3_btn;
    private Button okhttp2_btn;
    private Button webview_btn;
    private Button diagnosis_btn;
    private Button anr_btn;

    private WebView webView;

    private Context mContext;

    /**
     * 产生一个ANR
     */
    private static void SleepAMinute() {
        try {
            Thread.sleep(20 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 产生一个错误
     */
    private static void fakeCrashReport() {
        RuntimeException exception = new RuntimeException("Just a test exception");
        throw exception;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final OkhttpThreeThread okhttp3Client = new OkhttpThreeThread();
        final OkhttpTwoThread okhttp2Client = new OkhttpTwoThread();

        mContext = this.getApplicationContext();

        http_btn = (Button) findViewById(R.id.http_btn);
        crash_btn = (Button) findViewById(R.id.crash_btn);
        okhttp3_btn = (Button) findViewById(R.id.okhttp3_btn);
        okhttp2_btn = (Button) findViewById(R.id.okhttp2_btn);
        diagnosis_btn = (Button) findViewById(R.id.diagnosis_btn);
        anr_btn = (Button) findViewById(R.id.anr_btn);
        webview_btn = (Button) findViewById(R.id.webview_btn);

        webView = (WebView) findViewById(R.id.webview1);

        crash_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fakeCrashReport();
            }
        });

        http_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                http_btn.setEnabled(false);
                networkAsyncTask = new NetworkAsyncTask();
                networkAsyncTask.execute("NETWORK_GET");
//                networkAsyncTask.setUrl("https","www.163.com").setPort(9096).execute("NETWORK_GET");
//                networkAsyncTask.setUrl("http","www.qq.com").setPort(9097).execute("NETWORK_GET");
//                networkAsyncTask.setUrl("https","www.qiniu.com").setPort(9098).execute("NETWORK_GET");
//                networkAsyncTask.setUrl("http","www.taobao.com").setPort(9099).execute("NETWORK_GET");
//                networkAsyncTask.setUrl("http","www.alipay.com").setPort(9100).execute("NETWORK_GET");
            }
        });

        okhttp3_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    okhttp3Client.run("http://www.baidu.com");
                    okhttp3Client.run("https://www.163.com");
                    okhttp3Client.run("http://www.qq.com");
                    okhttp3Client.run("https://www.qiniu.com");
                    okhttp3Client.run("http://www.taobao.com");
                    okhttp3Client.run("http://www.alipay.com");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        okhttp2_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    okhttp2Client.run("http://www.baidu.com");
                    okhttp2Client.run("https://www.163.com");
                    okhttp2Client.run("http://www.qq.com");
                    okhttp2Client.run("https://www.qiniu.com");
                    okhttp2Client.run("http://www.taobao.com");
                    okhttp2Client.run("http://www.alipay.com");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        webview_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.setVisibility(View.VISIBLE);
                webView.loadUrl("http://www.baidu.com");
            }
        });

        diagnosis_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DEMManager.netDiag("www.baidu.com", "http://www.baidu.com", new DEMManager.NetDiagCallback() {
                    @Override
                    public void complete(boolean isSuccessful, final Exception e) {
                        LogUtils.d(TAG, "-------net diagnosis : " + isSuccessful);
                        if (isSuccessful) {
                            AsyncRun.runInMain(new Runnable() {
                                @Override
                                public void run() {
                                    showDialog("Successful!");
                                }
                            });
                        } else {
                            AsyncRun.runInMain(new Runnable() {
                                @Override
                                public void run() {
                                    showDialog(e.toString());
                                }
                            });
                        }
                    }
                });
            }
        });

        anr_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 17/6/16生成一个ANR
                SleepAMinute();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        DEMManager.start("hriygkee.bq.cloudappl.com", "9a9c127726b746e5b5fa7fc816a17407", this.getApplicationContext());
    }

    @Override
    protected void onStop() {
        super.onStop();
//        DEMManager.unInit();
    }

    private void showDialog(final String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);        //先得到构造器
        builder.setTitle("提示");                                         //设置标题
        builder.setMessage(content);       //设置内容
        builder.setIcon(R.mipmap.ic_launcher);   //自定义图标
        builder.setCancelable(false);           //设置是否能点击，对话框的其他区域取消

        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {     //设置其确认按钮和监听事件
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //  which，是哪一个按钮被触发
                dialog.dismiss();
            }
        });

        builder.create();       //创建对话框
        builder.show();         //显示对话框
    }

    //用于进行网络请求的AsyncTask
    class NetworkAsyncTask extends AsyncTask<String, Integer, Map<String, Object>> {
        //NETWORK_GET表示发送GET请求
        public static final String NETWORK_GET = "NETWORK_GET";

        @Override
        protected Map<String, Object> doInBackground(String... params) {
            Map<String, Object> result = new HashMap<>();
            URL url = null;//请求的URL地址
            HttpURLConnection conn = null;
            String requestHeader = null;//请求头
            byte[] requestBody = null;//请求体
            String responseHeader = null;//响应头
            byte[] responseBody = null;//响应体
            String action = params[0];//http请求的操作类型

            try {
                if (NETWORK_GET.equals(action)) {
                    //发送GET请求
                    url = new URL("http://www.baidu.com");
                    conn = (HttpURLConnection) url.openConnection();
                    //HttpURLConnection默认就是用GET发送请求，所以下面的setRequestMethod可以省略
                    conn.setRequestMethod("GET");
                    //HttpURLConnection默认也支持从服务端读取结果流，所以下面的setDoInput也可以省略
                    conn.setDoInput(true);
                    //用setRequestProperty方法设置一个自定义的请求头:action，由于后端判断
                    conn.setRequestProperty("action", NETWORK_GET);
                    //禁用网络缓存
                    conn.setUseCaches(false);
                    //获取请求头
                    requestHeader = getReqeustHeader(conn);
                    //在对各种参数配置完成后，通过调用connect方法建立TCP连接，但是并未真正获取数据
                    //conn.connect()方法不必显式调用，当调用conn.getInputStream()方法时内部也会自动调用connect方法
                    conn.connect();
                    //调用getInputStream方法后，服务端才会收到请求，并阻塞式地接收服务端返回的数据
                    InputStream is = conn.getInputStream();
                    //将InputStream转换成byte数组,getBytesByInputStream会关闭输入流
                    responseBody = getBytesByInputStream(is);
                    //获取响应头
                    responseHeader = getResponseHeader(conn);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //最后将conn断开连接
                if (conn != null) {
                    conn.disconnect();
                }
            }

            result.put("url", url.toString());
            result.put("action", action);
            result.put("requestHeader", requestHeader);
            result.put("requestBody", requestBody);
            result.put("responseHeader", responseHeader);
            result.put("responseBody", responseBody);
            return result;
        }

        @Override
        protected void onPostExecute(Map<String, Object> result) {
            super.onPostExecute(result);
            http_btn.setEnabled(true);
        }

        //读取请求头
        private String getReqeustHeader(HttpURLConnection conn) {
            //https://github.com/square/okhttp/blob/master/okhttp-urlconnection/src/main/java/okhttp3/internal/huc/HttpURLConnectionImpl.java#L236
            Map<String, List<String>> requestHeaderMap = conn.getRequestProperties();
            Iterator<String> requestHeaderIterator = requestHeaderMap.keySet().iterator();
            StringBuilder sbRequestHeader = new StringBuilder();
            while (requestHeaderIterator.hasNext()) {
                String requestHeaderKey = requestHeaderIterator.next();
                String requestHeaderValue = conn.getRequestProperty(requestHeaderKey);
                sbRequestHeader.append(requestHeaderKey);
                sbRequestHeader.append(":");
                sbRequestHeader.append(requestHeaderValue);
                sbRequestHeader.append("\n");
            }
            return sbRequestHeader.toString();
        }

        //读取响应头
        private String getResponseHeader(HttpURLConnection conn) {
            Map<String, List<String>> responseHeaderMap = conn.getHeaderFields();
            int size = responseHeaderMap.size();
            StringBuilder sbResponseHeader = new StringBuilder();
            for (int i = 0; i < size; i++) {
                String responseHeaderKey = conn.getHeaderFieldKey(i);
                String responseHeaderValue = conn.getHeaderField(i);
                sbResponseHeader.append(responseHeaderKey);
                sbResponseHeader.append(":");
                sbResponseHeader.append(responseHeaderValue);
                sbResponseHeader.append("\n");
            }
            return sbResponseHeader.toString();
        }

        //从InputStream中读取数据，转换成byte数组，最后关闭InputStream
        private byte[] getBytesByInputStream(InputStream is) {
            byte[] bytes = null;
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(baos);
            byte[] buffer = new byte[1024 * 8];
            int length = 0;
            try {
                while ((length = bis.read(buffer)) > 0) {
                    bos.write(buffer, 0, length);
                }
                bos.flush();
                bytes = baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return bytes;
        }
    }
}
