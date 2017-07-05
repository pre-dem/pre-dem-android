package qiniu.predem.android.http;

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

import qiniu.predem.android.config.HttpConfig;
import qiniu.predem.android.util.FileUtil;
import qiniu.predem.android.util.LogUtils;

/**
 * Created by Misty on 17/6/15.
 */

public class HttpMonitorManager {
    private static final String TAG = "HttpMonitorManager";

    private static final int MSG_WHAT_REPORT = 1;
    private static final int MSG_WHAT_BYEBYE = 2;
    private static final int MSG_BYEBYTE_DELAY = 10; //ms
    private static final int reportIntervalTime = 60 * 1000;

    private static boolean initialized = false;

    private Handler mReportHandler;
    private HandlerThread mHandlerThread;
    private FileUtil mLogFileManager;

    private Object lockReporter = new Object();

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_REPORT:
                    //发送上报数据
                    onReportMessage(true);
                    break;
                case MSG_WHAT_BYEBYE:
                    //注销上报数据
                    onByeByeMessage();
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private HttpMonitorManager() {
    }

    public static HttpMonitorManager getInstance() {
        return HttpMonitorManagerHolder.instance;
    }

    public void register(Context context) {
        if (initialized || context == null) {
            return;
        }
        initialized = true;

        initialize(context);
    }

    public void unregister() {
        destroy();
        initialized = false;
    }

    private void initialize(Context context) {
        if (mHandlerThread != null) {
            return;
        }
        mLogFileManager = FileUtil.getInstance();
        mLogFileManager.initialize(context.getApplicationContext());
        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();

        mReportHandler = new Handler(mHandlerThread.getLooper(), mCallback);
        mReportHandler.sendEmptyMessageDelayed(MSG_WHAT_REPORT, reportIntervalTime);
    }

    private void onReportMessage(boolean again) {
        String report = mLogFileManager.getReportContent();
        if (report != null && sendRequest(HttpConfig.getHttpUrl(), report)) {
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
            LogUtils.e(TAG, e.toString());
            return false;
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
            return false;
        }
        httpConn.setConnectTimeout(3000);
        httpConn.setReadTimeout(10000);
        try {
            httpConn.setRequestMethod("POST");
        } catch (ProtocolException e) {
            LogUtils.e(TAG, e.toString());
            return false;
        }
        httpConn.setRequestProperty("Content-Type", "application/x-gzip");
        httpConn.setRequestProperty("Accept-Encoding", "identity");
        httpConn.setRequestProperty("Content-Encoding", "gzip");

        try {
            byte[] bytes = content.getBytes();
            if (bytes == null) {
                return false;
            }

            ByteArrayOutputStream compressed = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(compressed);
            gzip.write(bytes);
            gzip.close();
            httpConn.getOutputStream().write(compressed.toByteArray());
            httpConn.getOutputStream().flush();
        } catch (IOException e) {
            LogUtils.e(TAG, e.toString());
            return false;
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
            return false;
        }
        int responseCode = 0;
        try {
            responseCode = httpConn.getResponseCode();
        } catch (IOException e) {
            LogUtils.e(TAG, e.toString());
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
            LogUtils.e(TAG, e.toString());
            return false;
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
            return false;
        }
        byte[] data = new byte[length];
        int read = 0;
        try {
            read = is.read(data);
        } catch (IOException e) {
            LogUtils.e(TAG, e.toString());
            return false;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                LogUtils.e(TAG, e.toString());
                return false;
            }
        }
        return read > 0;
    }

    private void destroy() {
        if (mHandlerThread == null) {
            return;
        }
        mReportHandler.removeCallbacksAndMessages(null);
        mReportHandler.sendEmptyMessageDelayed(MSG_WHAT_BYEBYE, MSG_BYEBYTE_DELAY);
    }

    private static class HttpMonitorManagerHolder {
        public final static HttpMonitorManager instance = new HttpMonitorManager();
    }
}
