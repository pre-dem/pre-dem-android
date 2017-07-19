package qiniu.predem.android.block;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.qiniu.android.common.FixedZone;
import com.qiniu.android.common.Zone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import qiniu.predem.android.bean.AppBean;
import qiniu.predem.android.config.Configuration;
import qiniu.predem.android.http.HttpURLConnectionBuilder;
import qiniu.predem.android.util.LogUtils;
import qiniu.predem.android.util.ToolUtil;

import static android.R.id.list;
import static qiniu.predem.android.block.BlockPrinter.ACTION_ANR;
import static qiniu.predem.android.block.BlockPrinter.ACTION_BLOCK;
import static qiniu.predem.android.config.FileConfig.FIELD_REPORT_UUID;
import static qiniu.predem.android.config.FileConfig.FILELD_CRASH_CONTENT;
import static qiniu.predem.android.config.FileConfig.FILELD_CRASH_TIME;
import static qiniu.predem.android.config.FileConfig.FILELD_START_TIME;

/**
 * Created by fengcunhan on 16/1/19.
 */
public class TraceInfoCatcher extends Thread {
    private static final String TAG="StackInfoCatcher";

    ////////
    private volatile int _tick = 0;
    private final Handler _uiHandler = new Handler(Looper.getMainLooper());
    private final int _timeoutInterval;
    private final Runnable _ticker = new Runnable() {
        @Override
        public void run() {
            _tick = (_tick + 1) % Integer.MAX_VALUE;
        }
    };
    ///////

    private static final int SIZE=1024;
    private boolean stop = false;
    private long mLastTime = 0;
    private List<TraceInfo> mList = new ArrayList<>(SIZE);
    private Context mContext;
    private BroadcastReceiver mBroadcastReceiver;
    private boolean submitting = false;


    public TraceInfoCatcher(Context context){
        this.mContext=context;

        //使用自定义printer
        context.getMainLooper().setMessageLogging(new BlockPrinter(context));

        //卡顿时间差
        _timeoutInterval= 2000;

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //block
                if (intent.getAction().equals(ACTION_BLOCK)) {
                    long endTime = intent.getLongExtra(BlockPrinter.EXTRA_FINISH_TIME, 0);
                    long startTime = intent.getLongExtra(BlockPrinter.EXTRA_START_TIME, 0);
                    TraceInfo info = getInfoByTime(endTime, startTime);
                    if (null != info) {
                        //send reqeust
                        sendRequest(info);
                    } else {
                        LogUtils.e(TAG, "------no block line find");
                    }
                }
            }
        };
        manager.registerReceiver(mBroadcastReceiver, new IntentFilter(ACTION_BLOCK));
    }

    @Override
    public void run() {
        int lastTick;
        while(!stop){
            lastTick = _tick;
            _uiHandler.post(_ticker);
            try {
                Thread.sleep(_timeoutInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }

            if (_tick == lastTick){
                //产生卡顿，获取stackInfo
                mLastTime = System.currentTimeMillis();
                TraceInfo info = new TraceInfo();
                info.mTime = mLastTime;
                info.mLog = stackTraceToString(Looper.getMainLooper().getThread().getStackTrace());
                mList.add(info);
            }
            if(mList.size()>SIZE){
                mList.remove(0);
            }
        }
    }

    public TraceInfo getInfoByTime(long endTime,long startTime){
        for(TraceInfo info : mList){
            if(info.mTime >= startTime && info.mTime<=endTime){
                return info;
            }
        }
        return null;
    }

    public String stackTraceToString(StackTraceElement[] elements){
        StringBuilder result = new StringBuilder();
        if(null!=elements && elements.length>0){
            for (int i = 0; i < elements.length ; i++) {
                result.append("\tat ");
                result.append(elements[i].toString());
                result.append("\n");
            }

        }
        return result.toString();
    }

    public void sendRequest(final TraceInfo info){
        if (!submitting) {
            submitting = true;
            new Thread() {
                @Override
                public void run() {
                    //1、get uptoken
                    HttpURLConnection urlConnection = null;
                    InputStream is = null;
                    try {
                        String md5 = ToolUtil.getStringMd5(info.mLog);
                        urlConnection = new HttpURLConnectionBuilder(Configuration.getUpToken() + "?md5=" + md5)
                                .setRequestMethod("GET")
                                .build();

                        int responseCode = urlConnection.getResponseCode();
                        String token = null;
                        String key = null;
                        if (responseCode == 200) {
                            try {
                                is = urlConnection.getInputStream();
                                byte[] data = new byte[8 * 1024];
                                is.read(data);
                                String content = new String(data);
                                JSONObject jsonObject = new JSONObject(content);
                                token = jsonObject.optString("token");
                                key = jsonObject.optString("key");
                            } catch (Exception e) {
                                LogUtils.e(TAG, "------" + e.toString());
                            } finally {
                                if (is != null) {
                                    is.close();
                                }
                            }
                        }
                        if (token == null) {
                            return;
                        }

                        //2、上传信息到七牛云
                        Zone zone = FixedZone.zone0;
                        com.qiniu.android.storage.Configuration config = new com.qiniu.android.storage.Configuration.Builder().zone(zone).build();
                        UploadManager uploadManager = new UploadManager(config);
                        uploadManager.put(info.mLog.getBytes(), key, token, new UpCompletionHandler() {
                            @Override
                            public void complete(final String key, ResponseInfo info, JSONObject response) {
                                if (info.isOK()) {
                                    new Thread(){
                                        @Override
                                        public void run() {
                                            //上报数据
                                            HttpURLConnection url = null;
                                            boolean successful = false;
                                            try {
                                                JSONObject parameters = new JSONObject();

                                                parameters.put("app_bundle_id", AppBean.APP_PACKAGE);
                                                parameters.put("app_name",AppBean.APP_NAME);
                                                parameters.put("app_version",AppBean.APP_VERSION);
                                                parameters.put("device_model",AppBean.PHONE_MODEL);
                                                parameters.put("os_platform","a");
                                                parameters.put("os_version",AppBean.ANDROID_VERSION);
                                                parameters.put("os_build",AppBean.ANDROID_BUILD);
                                                parameters.put("sdk_version",AppBean.SDK_VERSION);
                                                parameters.put("sdk_id",AppBean.SDK_NAME);
                                                parameters.put("device_id",AppBean.DEVICE_IDENTIFIER);
                                                parameters.put("report_uuid","");
                                                parameters.put("crash_log_key",key);
                                                parameters.put("manufacturer",AppBean.PHONE_MANUFACTURER);
                                                parameters.put("start_time","");
                                                parameters.put("crash_time","");

                                                url = new HttpURLConnectionBuilder(Configuration.getCrashUrl())
                                                        .setRequestMethod("POST")
                                                        .setHeader("Content-Type","application/json")
                                                        .setRequestBody(parameters.toString())
                                                        .build();

                                                int responseCode = url.getResponseCode();
                                            }catch (Exception e){
                                                LogUtils.e(TAG,"----"+e.toString());
                                                e.printStackTrace();
                                            } finally {
                                                if (url != null) {
                                                    url.disconnect();
                                                }
                                            }
                                        }
                                    }.start();
                                }else{
                                    return;
                                }
                            }
                        }, null);
                        submitting = false;
                    } catch (Exception e) {
                        LogUtils.e(TAG, "-----" + e.toString());
                        e.printStackTrace();
                    } finally {
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                    }
                }
                }.start();
            }
    }

    public void stopTrace(){
        stop=true;
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
    }
}
