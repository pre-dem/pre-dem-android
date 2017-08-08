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

import org.json.JSONArray;
import org.json.JSONObject;

import qiniu.predem.android.DEMManager;
import qiniu.predem.android.bean.NetDiagBean;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    private final String Name = "ApmDemo";
    private final String APP_KEY = "appkey";

    private Button http_btn;
    private Button okhttp3_btn;
    private Button okhttp2_btn;
    private Button crash_btn;
    private Button webview_btn;
    private Button diagnosis_btn;
    private Button anr_btn;
    private Button custom_btn;

    //
    private OkhttpTwoThread okhttpTwoThread;
    private OkhttpThreeThread okhttpThreeThread;

    //appkey
    private String appKey;
    private Context mContext;

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
        diagnosis_btn.setOnClickListener(this);
        custom_btn.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sh = getSharedPreferences(Name,MODE_PRIVATE);
        appKey = sh.getString(APP_KEY,null);
        if (appKey != null && !appKey.isEmpty()){
            //
            DEMManager.start("jkbkolos.bq.cloudappl.com", appKey, mContext);
        }else{
            showCustomizeDialog();
        }
    }

    private void initView(){
        http_btn = (Button) findViewById(R.id.http_btn);
        okhttp3_btn = (Button) findViewById(R.id.okhttp3_btn);
        okhttp2_btn = (Button) findViewById(R.id.okhttp2_btn);
        webview_btn = (Button) findViewById(R.id.webview_btn);
        crash_btn = (Button) findViewById(R.id.crash_btn);
        anr_btn = (Button) findViewById(R.id.anr_btn);
        diagnosis_btn = (Button) findViewById(R.id.diagnosis_btn);
        custom_btn = (Button)findViewById(R.id.custom_btn);

        okhttpTwoThread = new OkhttpTwoThread();
        okhttpThreeThread = new OkhttpThreeThread();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.http_btn:
                new HttpUrlConnectionThread("http://www.baidu.com","GET").start();
                new HttpUrlConnectionThread("https://www.163.com",null).start();
                new HttpUrlConnectionThread("http://www.qq.com",null).start();
                new HttpUrlConnectionThread("https://www.qiniu.com",null).start();
                new HttpUrlConnectionThread("http://www.taobao.com",null).start();
                new HttpUrlConnectionThread("http://www.alipay.com",null).start();
                break;
            case R.id.okhttp2_btn:
                try {
                    okhttpTwoThread.run("http://www.baidu.com");
                    okhttpTwoThread.run("https://www.163.com");
                    okhttpTwoThread.run("http://www.qq.com");
                    okhttpTwoThread.run("https://www.qiniu.com");
                    okhttpTwoThread.run("http://www.taobao.com");
                    okhttpTwoThread.run("http://www.alipay.com");
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case R.id.okhttp3_btn:
                try {
                    okhttpThreeThread.run("http://www.baidu.com");
                    okhttpThreeThread.run("https://www.163.com");
                    okhttpThreeThread.run("http://www.qq.com");
                    okhttpThreeThread.run("https://www.qiniu.com");
                    okhttpThreeThread.run("http://www.taobao.com");
                    okhttpThreeThread.run("http://www.alipay.com");
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case R.id.webview_btn:
                Intent intent = new Intent(this,WebViewActivity.class);
                intent.putExtra("extr_url", "http://www.taobao.com");
                startActivity(intent);
                break;
            case R.id.crash_btn:
                fakeCrashReport();
                break;
            case R.id.anr_btn:
                SleepAMinute();
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
                        Log.d(TAG,"-----diagnosis info : " + bean.toString());
                    }
                });
                break;
            case R.id.custom_btn:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //[{"hellonum":7,"helloKey":"worldValue"}]
                            JSONArray json = new JSONArray();
                            JSONObject jsonObject1 = new JSONObject();
                            jsonObject1.put("hellonum",7);
                            jsonObject1.put("helloKey","worldValue");
                            json.put(jsonObject1);

                            DEMManager.trackEvent("viewDidLoadEvent",json);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
        }
    }

    /**
     * 产生一个crash
     */
    private static void fakeCrashReport() {
        RuntimeException exception = new RuntimeException("Just a test exception");
        throw exception;
    }

    /**
     * 产生一个ANR
     */
    private static void SleepAMinute() {
        try {
            Thread.sleep(25 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void showCustomizeDialog() {
        AlertDialog.Builder customizeDialog =
                new AlertDialog.Builder(MainActivity.this);
        final View dialogView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.activity_dialog,null);
        customizeDialog.setTitle("请输入AppKey");
        customizeDialog.setView(dialogView);
        customizeDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 获取EditView中的输入内容
                        EditText edit_text = (EditText) dialogView.findViewById(R.id.edit_text);
                        appKey = edit_text.getText().toString().trim();
                        if (appKey == null || appKey.isEmpty()){
                            Toast.makeText(MainActivity.this, "appKey为空，数据无法上报到服务端", Toast.LENGTH_SHORT).show();
                        }else{
                            SharedPreferences sh = getSharedPreferences(Name,MODE_PRIVATE);
                            SharedPreferences.Editor editor = sh.edit();
                            editor.putString(APP_KEY,appKey);
                            editor.apply();
                            //jkbkolos.bq.cloudappl.com
                            DEMManager.start("jkbkolos.bq.cloudappl.com", appKey, mContext);
                        }
                    }
                });
        customizeDialog.show();
    }
}
