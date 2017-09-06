package qiniu.predem.android.http;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.net.HttpURLConnection;

import qiniu.predem.android.config.Configuration;
import qiniu.predem.android.util.FileUtil;
import qiniu.predem.android.util.Functions;

import qiniu.predem.android.util.HttpURLConnectionBuilder;
import qiniu.predem.android.util.LogUtils;


/**
 * Created by Misty on 17/6/15.
 */

public class HttpMonitorManager {
    private static final String TAG = "HttpMonitorManager";

    private static final int MSG_WHAT_REPORT = 1;
    private static final int MSG_WHAT_BYEBYE = 2;
    private static final int MSG_BYEBYTE_DELAY = 10; //ms
    private static final int reportIntervalTime = 10 * 1000;

    private static boolean initialized = false;

    private Handler mReportHandler;
    private HandlerThread mHandlerThread;
    private FileUtil mLogFileManager;
    private Context mContext;

    private Object lockReporter = new Object();

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (!Functions.isBackground(mContext)) {
                onReportMessage(true);
            } else {
                onByeByeMessage();
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
        mContext = context;

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
        if (report != null && sendRequest(Configuration.getHttpUrl(), report)) {
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

        try {
            HttpURLConnection httpConn = new HttpURLConnectionBuilder(url)
                    .setRequestMethod("POST")
                    .setHeader("Content-Type", "application/x-gzip")
                    .setHeader("Content-Encoding", "gzip")
                    .setRequestBody(content)
                    .setGzip(true)
                    .build();

            int responseCode = httpConn.getResponseCode();
            boolean successful = (responseCode == HttpURLConnection.HTTP_ACCEPTED || responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK);
            return successful;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
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
