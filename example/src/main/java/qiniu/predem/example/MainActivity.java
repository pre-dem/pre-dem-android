package qiniu.predem.example;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import qiniu.predem.android.DEMManager;
import qiniu.predem.android.bean.NetDiagBean;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    private final String Name = "ApmDemo";

    private Button http_btn;
    private Button okhttp3_btn;
    private Button okhttp2_btn;
    private Button crash_btn;
    private Button webview_btn;
    private Button diagnosis_btn;
    private Button anr_btn;
    private Button lag_btn;
    private Button logcat_btn;
    private Button custom_btn;

    private OkhttpTwoThread okhttpTwoThread;
    private OkhttpThreeThread okhttpThreeThread;

    private String appKey;
    private String domain;
    private Context mContext;

    /**
     * 产生一个crash
     */
    private static void fakeCrashReport() {
        RuntimeException exception = new RuntimeException("Just a test exception " + System.currentTimeMillis());
        throw exception;
    }

    /**
     * 产生一个ANR
     */
    private static void SleepAMinute(long t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this.getApplicationContext();

        initView();
        http_btn.setOnClickListener(this);
        okhttp2_btn.setOnClickListener(this);
        okhttp3_btn.setOnClickListener(this);
        webview_btn.setOnClickListener(this);
        crash_btn.setOnClickListener(this);
        anr_btn.setOnClickListener(this);
        lag_btn.setOnClickListener(this);
        diagnosis_btn.setOnClickListener(this);
        custom_btn.setOnClickListener(this);
        logcat_btn.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sh = getSharedPreferences(Name, MODE_PRIVATE);
        appKey = sh.getString("APP_KEY", null);
        domain = sh.getString("DOMAIN", null);
        if (appKey != null && !appKey.isEmpty()) {
            //
            DEMManager.start(domain, appKey, mContext);
        } else {
            showCustomizeDialog();
        }
    }

    private void initView() {
        http_btn = (Button) findViewById(R.id.http_btn);
        okhttp3_btn = (Button) findViewById(R.id.okhttp3_btn);
        okhttp2_btn = (Button) findViewById(R.id.okhttp2_btn);
        webview_btn = (Button) findViewById(R.id.webview_btn);
        crash_btn = (Button) findViewById(R.id.crash_btn);
        anr_btn = (Button) findViewById(R.id.anr_btn);
        lag_btn = (Button) findViewById(R.id.lag_btn);
        diagnosis_btn = (Button) findViewById(R.id.diagnosis_btn);
        custom_btn = (Button) findViewById(R.id.custom_btn);
        logcat_btn = (Button) findViewById(R.id.logcat_btn);

        okhttpTwoThread = new OkhttpTwoThread();
        okhttpThreeThread = new OkhttpThreeThread();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.http_btn:
                new HttpUrlConnectionThread("http://www.baidu.com", "GET").start();
                new HttpUrlConnectionThread("https://www.163.com", null).start();
                new HttpUrlConnectionThread("http://www.qq.com", null).start();
                new HttpUrlConnectionThread("https://www.qiniu.com", null).start();
                new HttpUrlConnectionThread("http://www.taobao.com", null).start();
                new HttpUrlConnectionThread("http://www.alipay.com", null).start();
                break;
            case R.id.okhttp2_btn:
                try {
                    OkhttpTwoThread.run("http://www.baidu.com");
                    OkhttpTwoThread.run("https://www.163.com");
                    OkhttpTwoThread.run("http://www.qq.com");
                    OkhttpTwoThread.run("https://www.qiniu.com");
                    OkhttpTwoThread.run("http://www.taobao.com");
                    OkhttpTwoThread.run("http://www.alipay.com");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.okhttp3_btn:
                try {
                    OkhttpThreeThread.run("http://www.baidu.com");
                    OkhttpThreeThread.run("https://www.163.com");
                    OkhttpThreeThread.run("http://www.qq.com");
                    OkhttpThreeThread.run("https://www.qiniu.com");
                    OkhttpThreeThread.run("http://www.taobao.com");
                    OkhttpThreeThread.run("http://www.alipay.com");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.webview_btn:
                Intent intent = new Intent(this, WebViewActivity.class);
                intent.putExtra("extr_url", "http://www.taobao.com");
                startActivity(intent);
                break;
            case R.id.crash_btn:
                fakeCrashReport();
                break;
            case R.id.anr_btn:
                SleepAMinute(25 * 1000);
                break;
            case R.id.lag_btn:
                SleepAMinute(4 * 1000);
                break;
            case R.id.diagnosis_btn:
                diagnosis_btn.setEnabled(false);
                DEMManager.netDiag("www.baidu.com", "http://www.baidu.com", new DEMManager.NetDiagCallback() {
                    @Override
                    public void complete(NetDiagBean bean) {
                        AsyncRun.runInMain(new Runnable() {
                            @Override
                            public void run() {
                                diagnosis_btn.setEnabled(true);
                            }
                        });
                        Log.d(TAG, "-----diagnosis info : " + bean.toString());
                    }
                });
                break;
            case R.id.custom_btn:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("product_name", "惊艳Plus");
                            jsonObject.put("MAC地址", "00e08f0025dd");
                            jsonObject.put("ip", "1.49.69.237");
                            jsonObject.put("访问时间", "2017/8/11  PM 8:09:18");

                            DEMManager.trackEvent("viewDidLoadEvent", jsonObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;

            case R.id.logcat_btn:
                DEMManager.startLogging(Log.VERBOSE);
                for (int i = 0; i < 10; i++) {
                    DEMManager.d(TAG, "------ " + i);
                }
                DEMManager.stopLogging();
                break;
        }
    }

    private void showCustomizeDialog() {
        AlertDialog.Builder customizeDialog =
                new AlertDialog.Builder(MainActivity.this);
        final View dialogView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.activity_dialog, null);
        customizeDialog.setTitle("请输入AppKey和Domain");
        customizeDialog.setView(dialogView);
        customizeDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 获取EditView中的输入内容
                        EditText appKeyField = (EditText) dialogView.findViewById(R.id.app_key_field);
                        appKey = appKeyField.getText().toString().trim();
                        EditText domainField = (EditText) dialogView.findViewById(R.id.domain_field);
                        domain = domainField.getText().toString().trim();
                        if (appKey == null || appKey.isEmpty() || domain == null || domain.isEmpty()) {
                            Toast.makeText(MainActivity.this, "app key或domain为空，数据无法上报到服务端, 临时使用默认配置", Toast.LENGTH_SHORT).show();
//                            DEMManager.start(Conf.DOMAIN, Conf.APP_KEY, mContext);
                        } else {
                            SharedPreferences sh = getSharedPreferences(Name, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sh.edit();
                            editor.putString("APP_KEY", appKey);
                            editor.putString("DOMAIN", domain);
                            editor.apply();
                            DEMManager.start(domain, appKey, mContext);
                        }
                    }
                });
        customizeDialog.show();
    }
}
