package qiniu.predem.android.block;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;

import com.qiniu.android.common.FixedZone;
import com.qiniu.android.common.Zone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import qiniu.predem.android.bean.AppBean;
import qiniu.predem.android.config.Configuration;
import qiniu.predem.android.config.FileConfig;
import qiniu.predem.android.util.HttpURLConnectionBuilder;
import qiniu.predem.android.util.FileUtil;
import qiniu.predem.android.util.Functions;
import qiniu.predem.android.util.LogUtils;

import static qiniu.predem.android.block.BlockPrinter.ACTION_BLOCK;
import static qiniu.predem.android.block.BlockPrinter.EXTRA_FINISH_TIME;
import static qiniu.predem.android.block.BlockPrinter.EXTRA_START_TIME;

/**
 * Created by fengcunhan on 16/1/19.
 */
public class TraceInfoCatcher extends Thread {
    private static final String TAG = "TraceInfoCatcher";
    private static final int SIZE = 1024;
    private final Handler _uiHandler = new Handler(Looper.getMainLooper());
    ////////
    private volatile int _tick = 0;
    private final Runnable _ticker = new Runnable() {
        @Override
        public void run() {
            _tick = (_tick + 1) % Integer.MAX_VALUE;
        }
    };
    ///////
    private int _timeoutInterval;
    private boolean stop = false;
    //    private long mLastTime = 0;
    private List<TraceInfo> mList = new ArrayList<>(SIZE);
    private Context mContext;
    private BroadcastReceiver mBroadcastReceiver;
    private boolean submitting = false;
    private WeakReference<Context> weakContext;
    private String fileName;
    private int count;


    public TraceInfoCatcher(Context context) {
        this.mContext = context;

        //使用自定义printer
        context.getMainLooper().setMessageLogging(new BlockPrinter(context));

        _timeoutInterval = 2000;

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //判断应用是否在前台
                if (Functions.isBackground(context)) {
                    stopTrace();
                    return;
                }
                //block
                if (intent.getAction().equals(ACTION_BLOCK)) {
                    long endTime = intent.getLongExtra(EXTRA_FINISH_TIME, 0);
                    long startTime = intent.getLongExtra(EXTRA_START_TIME, 0);
                    TraceInfo info = getInfoByTime(endTime, startTime);
                    if (info != null && null != info.mLog && !info.mLog.equals("")) {
                        //send reqeust
                        sendRequest(info, Configuration.getLagMonitorUrl(), startTime, endTime);
                    }
                }
            }
        };
        manager.registerReceiver(mBroadcastReceiver, new IntentFilter(ACTION_BLOCK));

        fileName = null;
        count = 0;
        //TODO 检查是否有 anr 文件需要上传
        weakContext = new WeakReference<Context>(mContext);
        hasStackTraces(weakContext);
    }

    /**
     * Returns the content of a file as a string.
     */
    private static String contentsOfFile(WeakReference<Context> weakContext, String filename) {
        Context context = null;
        if (weakContext != null) {
            context = weakContext.get();
            if (context != null) {
                StringBuilder contents = new StringBuilder();
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(context.openFileInput(filename)));
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        contents.append(line);
                        contents.append(System.getProperty("line.separator"));
                    }
                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException ignored) {
                        }
                    }
                }
                return contents.toString();
            }
        }
        return null;
    }

    /**
     * Deletes the give filename and all corresponding files (same name,
     * different extension).
     */
    private static void deleteStackTrace(WeakReference<Context> weakContext, String filename) {
        Context context = null;
        if (weakContext != null) {
            context = weakContext.get();
            if (context != null) {
                context.deleteFile(filename);

                String user = filename.replace(".anr", ".user");
                context.deleteFile(user);

                String contact = filename.replace(".anr", ".contact");
                context.deleteFile(contact);

                String description = filename.replace(".anr", ".description");
                context.deleteFile(description);
            }
        }
    }

    @Override
    public void run() {
        int lastTick;
        while (!stop) {
            lastTick = _tick;
            long startTime = System.currentTimeMillis();
            _uiHandler.post(_ticker);
            try {
                Thread.sleep(_timeoutInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }

            if (_tick == lastTick) {
                //产生ANR，获取stackInfo
                TraceInfo info = new TraceInfo();
                info.mStartTime = startTime;
                info.mEndTime = System.currentTimeMillis();
                info.mLog = stackTraceToString(Looper.getMainLooper().getThread().getStackTrace());
                mList.add(info);
                if (count > 3 && fileName == null) {
                    fileName = UUID.randomUUID().toString();
                    //保存到文件
                    FileUtil.getInstance().writeAnrReport(fileName, info);
                }
                count++;
            } else if (_tick != lastTick && fileName != null) {
                //恢复 删除文件
                deleteStackTrace(weakContext, fileName);
                fileName = null;
                count = 0;
            }

            //进入后台后停止进度
            if (Functions.isBackground(mContext)) {
                stopTrace();
                return;
            }
            if (mList.size() > SIZE) {
                mList.remove(0);
            }
        }
    }

    public void hasStackTraces(WeakReference<Context> weakContext) {
        String[] filenames = searchForStackTraces();
        try {
            if ((filenames != null) && (filenames.length > 0)) {
                for (int i = 0; i < filenames.length; i++) {
                    String content = contentsOfFile(weakContext, filenames[i]);
                    JSONObject jsonObject = new JSONObject(content);

                    TraceInfo traceInfo = new TraceInfo();
                    traceInfo.mStartTime = jsonObject.optLong("startTime");
                    traceInfo.mEndTime = jsonObject.optLong("endTime");
                    traceInfo.mLog = jsonObject.optString("info");
                    //上报数据
                    sendRequest(traceInfo, Configuration.getLagMonitorUrl(), traceInfo.mStartTime, traceInfo.mEndTime);
                    deleteStackTrace(weakContext, filenames[i]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] searchForStackTraces() {
        if (FileConfig.FILES_PATH != null) {
            LogUtils.d("Looking for exceptions in: " + FileConfig.FILES_PATH);

            File dir = new File(FileConfig.FILES_PATH + "/");
            boolean created = dir.mkdir();
            if (!created && !dir.exists()) {
                return new String[0];
            }

            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".anr");
                }
            };
            return dir.list(filter);
        } else {
            LogUtils.d("Can't search for anr as file path is null.");
            return null;
        }
    }

    public TraceInfo getInfoByTime(long endTime, long startTime) {
        for (TraceInfo info : mList) {
            if (info.mStartTime >= startTime && info.mEndTime <= endTime) {
                return info;
            }
        }
        return null;
    }

    public String stackTraceToString(StackTraceElement[] elements) {
        StringBuilder result = new StringBuilder();
        if (null != elements && elements.length > 0) {
            for (int i = 0; i < elements.length; i++) {
                result.append("\tat ");
                result.append(elements[i].toString());
                result.append("\n");
            }

        }
        return result.toString();
    }

    public void sendRequest(final TraceInfo info, final String reportUrl, final long startTime, final long endTime) {
        if (!submitting) {
            submitting = true;
            new Thread() {
                @Override
                public void run() {
                    //1、get uptoken
                    HttpURLConnection urlConnection = null;
                    InputStream is = null;
                    try {
                        String md5 = Functions.getStringMd5(info.mLog);
                        urlConnection = new HttpURLConnectionBuilder(Configuration.getLagMonitorUpToken() + "?md5=" + md5)
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
                                LogUtils.d(TAG, "-----block upload response : " + info.statusCode + ";" + info.error);
                                if (info.isOK()) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            //上报数据
                                            HttpURLConnection url = null;
                                            boolean successful = false;
                                            try {
                                                JSONObject parameters = new JSONObject();

                                                Date start = new Date(startTime);
                                                Date end = new Date(endTime);
                                                FileUtil.DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
                                                parameters.put("app_bundle_id", AppBean.APP_PACKAGE);
                                                parameters.put("app_name", AppBean.APP_NAME);
                                                parameters.put("app_version", AppBean.APP_VERSION);
                                                parameters.put("device_model", AppBean.PHONE_MODEL);
                                                parameters.put("os_platform", AppBean.ANDROID_PLATFORM);
                                                parameters.put("os_version", AppBean.ANDROID_VERSION);
                                                parameters.put("os_build", AppBean.ANDROID_BUILD);
                                                parameters.put("sdk_version", AppBean.SDK_VERSION);
                                                parameters.put("sdk_id", "");
                                                parameters.put("device_id", AppBean.DEVICE_IDENTIFIER);
                                                parameters.put("tag", AppBean.APP_TAG);
                                                parameters.put("report_uuid", UUID.randomUUID().toString());
                                                parameters.put("lag_log_key", key);
                                                parameters.put("manufacturer", AppBean.PHONE_MANUFACTURER);
                                                parameters.put("start_time", FileUtil.DATE_FORMAT.format(start));
                                                parameters.put("lag_time", FileUtil.DATE_FORMAT.format(end));

                                                url = new HttpURLConnectionBuilder(reportUrl)
                                                        .setRequestMethod("POST")
                                                        .setHeader("Content-Type", "application/json")
                                                        .setRequestBody(parameters.toString())
                                                        .build();

                                                int responseCode = url.getResponseCode();
                                                LogUtils.d(TAG, "-------view report code : " + responseCode);
                                            } catch (Exception e) {
                                                LogUtils.e(TAG, "----" + e.toString());
                                                e.printStackTrace();
                                            } finally {
                                                if (url != null) {
                                                    url.disconnect();
                                                }
                                            }
                                        }
                                    }.start();
                                } else {
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

    public void stopTrace() {
        stop = true;
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
    }
}
