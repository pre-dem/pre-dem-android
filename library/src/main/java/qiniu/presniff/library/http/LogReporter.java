package qiniu.presniff.library.http;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.zip.GZIPOutputStream;

import qiniu.presniff.library.config.NetConfig;
import qiniu.presniff.library.file.LogFileManager;
import qiniu.presniff.library.util.LogUtils;

/**
 * Created by Misty on 5/19/17.
 */

public class LogReporter {
    private static final String TAG = "LogReporter";

    private static final int MSG_WHAT_REPORT = 1;
    private static final int MSG_WHAT_BYEBYE = 2;

    private static final int reportIntervalTime = 1 * 60 * 1000;
    private static final int MSG_BYEBYTE_DELAY = 10; //ms
    private static final int MIN_REPORT_INTERVAL = 10 * 1000; //ms

    private boolean DebugON = false;

    private Handler mReportHandler;
    private HandlerThread mHandlerThread;
    private LogFileManager mLogFileManager;
    private Object lockReporter = new Object();

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_REPORT:
                    LogUtils.d(TAG,"-------onReportMessage(true)");
                    onReportMessage(true);
                    break;
                case MSG_WHAT_BYEBYE:
                    onByeByeMessage();
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private LogReporter(){}

    public static LogReporter getInstance(){
        return LogReporterHolder.instance;
    }

    public void initialize(Context context) {
        if (mHandlerThread != null) {
            return;
        }
        mLogFileManager = LogFileManager.getInstance();
        mLogFileManager.initialize(context.getApplicationContext());
        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();

        mReportHandler = new Handler(mHandlerThread.getLooper(), mCallback);
        mReportHandler.sendEmptyMessageDelayed(MSG_WHAT_REPORT, reportIntervalTime);
    }

    private void onReportMessage(boolean again) {
        String report = mLogFileManager.getReportContent();
//        LogUtils.d(TAG,"-------report : " + report);
        if (report != null && sendRequest(NetConfig.PostPath, report)) {
            mLogFileManager.setReportSuccess();
        }
        if (again && mReportHandler != null) {
            mReportHandler.sendEmptyMessageDelayed(MSG_WHAT_REPORT, reportIntervalTime);
        }
    }

    private void onByeByeMessage() {
        if (mHandlerThread == null) {
            return;
        }

        mReportHandler.removeCallbacksAndMessages(null);
        synchronized (lockReporter) {
            mReportHandler = null;
        }

        // report the last messages before exit
        onReportMessage(false);
        mHandlerThread.quit();
        mHandlerThread = null;
        mLogFileManager.destroy();
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
        httpConn.setRequestProperty("Content-Type", DebugON ? "application/octet-stream" : "application/x-gzip");
        httpConn.setRequestProperty("Accept-Encoding", "identity");
        httpConn.setRequestProperty("Content-Encoding","gzip");

        try {
            byte[] bytes = content.getBytes();
            if (bytes == null) {
                return false;
            }
            if (DebugON) {
                httpConn.getOutputStream().write(bytes);
            } else {
//                LogUtils.d(TAG,"-------compressed");
                ByteArrayOutputStream compressed = new ByteArrayOutputStream();
                GZIPOutputStream gzip = new GZIPOutputStream(compressed);
                gzip.write(bytes);
                gzip.close();
                httpConn.getOutputStream().write(compressed.toByteArray());
            }
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
        if (responseCode != HttpURLConnection.HTTP_OK) {
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
        String responseData = new String(data);
        responseData = responseData.trim();
        LogUtils.d(TAG, "-------"+responseData);
//        try {
//            JSONObject res = new JSONObject(responseData);
//            int reportInterval = res.optInt("reportInterval");
//            int recordInterval = res.optInt("sampleInterval");
//            handleIntervalTime(url, reportInterval * 1000, recordInterval * 1000);
//        } catch (Exception e) {
//            LogUtils.e(TAG,e.toString());
//            return false;
//        }
        return true;
    }

    public void destroy() {
        if (mHandlerThread == null) {
            return;
        }
        mReportHandler.removeCallbacksAndMessages(null);
        mReportHandler.sendEmptyMessageDelayed(MSG_WHAT_BYEBYE, MSG_BYEBYTE_DELAY);
    }

    private static class LogReporterHolder {
        public final static LogReporter instance = new LogReporter();
    }
}
