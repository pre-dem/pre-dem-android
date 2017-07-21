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

import qiniu.predem.android.config.Configuration;
import qiniu.predem.android.util.FileUtil;
import qiniu.predem.android.util.LogUtils;
import qiniu.predem.android.util.ToolUtil;

import static android.R.string.ok;

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
    private Context mContext;

    private Object lockReporter = new Object();

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (!ToolUtil.isBackground(mContext)){
                LogUtils.d(TAG,"-----report message");
                onReportMessage(true);
            }else{
                LogUtils.d(TAG,"-----on byebye message");
                onByeByeMessage();
            }
//            switch (msg.what) {
//                case MSG_WHAT_REPORT:
//                    //发送上报数据
//                    onReportMessage(true);
//                    break;
//                case MSG_WHAT_BYEBYE:
//                    //注销上报数据
//                    onByeByeMessage();
//                    break;
//                default:
//                    break;
//            }
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
        HttpURLConnection httpConn;
        try {
            httpConn = (HttpURLConnection) new URL(url).openConnection();
            httpConn.setConnectTimeout(3000);
            httpConn.setReadTimeout(10000);
            httpConn.setRequestMethod("POST");

            httpConn.setRequestProperty("Content-Type", "application/x-gzip");
            httpConn.setRequestProperty("Content-Encoding", "gzip");

            byte[] bytes = content.getBytes("utf-8");
            if (bytes == null) {
                return false;
            }

            ByteArrayOutputStream compressed = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(compressed);
            gzip.write(bytes);
            gzip.close();
            httpConn.getOutputStream().write(compressed.toByteArray());
            httpConn.getOutputStream().flush();

            int responseCode = httpConn.getResponseCode();
            boolean successful = (responseCode == HttpURLConnection.HTTP_ACCEPTED || responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK);
            if (!successful){
                return false;
            }
            return true;
        } catch (IOException e) {
            LogUtils.e(TAG, "----"+e.toString());
            return false;
        } catch (Exception e) {
            LogUtils.e(TAG, "----"+e.toString());
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
