package qiniu.predem.android.logcat;

import android.content.Context;
import android.util.Log;

import com.qiniu.android.common.FixedZone;
import com.qiniu.android.common.Zone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import qiniu.predem.android.bean.AppBean;
import qiniu.predem.android.config.Configuration;
import qiniu.predem.android.util.Functions;
import qiniu.predem.android.util.HttpURLConnectionBuilder;
import qiniu.predem.android.util.LogUtils;
import static qiniu.predem.android.config.FileConfig.KEY_READ_FILE_INDEX;
import static qiniu.predem.android.config.FileConfig.KEY_READ_FILE_POSITION;
import static qiniu.predem.android.config.FileConfig.KEY_WRITE_FILE_INDEX;
import static qiniu.predem.android.config.FileConfig.KEY_WRITE_FILE_POSITION;
import static qiniu.predem.android.config.FileConfig.LOGCAT_FILE_BASE_NAME;
import static qiniu.predem.android.config.FileConfig.LOGCAT_FILE_PREFIX;
import static qiniu.predem.android.config.FileConfig.LOGCAT_INDEX_FILE_NAME;
import static qiniu.predem.android.config.FileConfig.MAX_LOG_FILE_NUM;
import static qiniu.predem.android.config.FileConfig.MAX_LOG_FILE_SIZE;

/**
 * Created by Misty on 2017/9/20.
 */

public class PrintLogger {
    private static final String TAG = "PrintLogger";

    /**
     * always synchronized with the ‘index.json’
     */
    private long mReadFileIndex = 0;
    private long mReadFilePosition = 0;
    private long mWriteFileIndex = 0;
    private long mWriteFilePosition = 0;

    private static volatile PrintLogger instance = null;
    private SimpleDateFormat mFormat = null;
    private WriteThread mThread = null;
    private Context mContext;

    private String mCachedReportContent;

    private long mStartTime;
    private long mEndTime;

    private PrintLogger(Context context){
        mThread = new WriteThread(context);
        mFormat = new SimpleDateFormat("MM-dd HH:mm:ss:SS");
        mThread.start();

        this.mContext = context;
    }

    //单例模式
    public static PrintLogger getInstance(Context context) {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new PrintLogger(context);
                }
            }
        }
        return instance;
    }

    synchronized void Log(String tag, String str) {
        String time = mFormat.format(new Date());
        mThread.enqueue(time + " " + tag + " " + str);
    }

    void openLogs(){
        mStartTime = System.currentTimeMillis();
        parseIndexFile();
        submitLogcat();
    }

    void closeLogs(){
        mEndTime = System.currentTimeMillis();
        submitLogcat();
    }

    private void submitLogcat(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mReadFileIndex <= mWriteFileIndex){
                    for (long i = mReadFileIndex; i <= mWriteFileIndex; i++){
                        //TODO
                        sendRequest(i);
                    }
                }
            }
        }).start();
    }

    private void sendRequest(long readFileIndex){
        try {
            String content = getReportContent(readFileIndex);
            if (content == null){
                return;
            }
            final String filename = LOGCAT_FILE_BASE_NAME + readFileIndex;
            String md5 = Functions.getStringMd5(content);
            HttpURLConnection urlConnection = new HttpURLConnectionBuilder(Configuration.getLogcatUpToken() + "?md5=" + md5)
                    .setRequestMethod("GET")
                    .build();
            int responseCode = urlConnection.getResponseCode();
            String token = null;
            String key = null;
            InputStream is = null;
            if (responseCode == 200) {
                try {
                    is = urlConnection.getInputStream();
                    byte[] data = new byte[8 * 1024];
                    is.read(data);
                    String resp = new String(data);
                    JSONObject jsonObject = new JSONObject(resp);
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

            Zone zone = FixedZone.zone0;
            com.qiniu.android.storage.Configuration configuration = new com.qiniu.android.storage.Configuration.Builder().zone(zone).build();
            UploadManager uploadManager = new UploadManager(configuration);
            uploadManager.put(content.getBytes(), key, token, new UpCompletionHandler() {
                @Override
                public void complete(final String key, ResponseInfo info, JSONObject response) {
                    Log.d(TAG,"------upload result " + info.toString());
                    if (info.isOK()) {
                        new Thread() {
                            @Override
                            public void run() {
                                report(filename, key);
                            }
                        }.start();
                    } else {
                        return;
                    }
                }
            }, null);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void report(String filename, String key){
        //3、上报服务器
        HttpURLConnection url = null;
        boolean successful = false;
        try {
            JSONObject parameters = new JSONObject();

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
            parameters.put("manufacturer", AppBean.PHONE_MANUFACTURER);
            parameters.put("start_time", mStartTime);
            parameters.put("end_time", mEndTime);
            parameters.put("log_key", key);
            parameters.put("log_tags","");
            parameters.put("error_count",0);

            url = new HttpURLConnectionBuilder(Configuration.getLogcatUrl())
                    .setRequestMethod("POST")
                    .setHeader("Content-Type", "application/json")
                    .setRequestBody(parameters.toString())
                    .build();

            int responseCode = url.getResponseCode();

            successful = (responseCode == HttpURLConnection.HTTP_ACCEPTED || responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            LogUtils.e(TAG, "----" + e.toString());
            e.printStackTrace();
        } finally {
            if (url != null) {
                url.disconnect();
            }
            if (successful) {
                mContext.deleteFile(filename);
            } else {
                LogUtils.d("-----Transmission failed, will retry on next register() call");
            }
        }
    }

    private String getReportContent(long readFileIndex) {
        if (mCachedReportContent != null) {
            return mCachedReportContent;
        }

        if (readFileIndex == mWriteFileIndex && mReadFilePosition == mWriteFilePosition) {
            return null;
        }

        synchronized (this) {
            String filename = LOGCAT_FILE_BASE_NAME + readFileIndex;
            Log.d(TAG,"------filename " + filename);
            mCachedReportContent = readFileOnce(filename, mReadFilePosition);
            if (mCachedReportContent == null) {
                return null;
            }

            mReadFilePosition += mCachedReportContent.length();
            if (mReadFileIndex < mWriteFileIndex && readFileIndex != mReadFileIndex) {
                mReadFileIndex = readFileIndex + 1 ;
                mReadFilePosition = 0;
            }
            updateIndexFile();
            // delete the file
            mContext.deleteFile(LOGCAT_FILE_BASE_NAME + readFileIndex);
        }
        return mCachedReportContent;
    }

    protected boolean addReportContent(String content) {
        // so many log files unreported, maybe the server closed forever
        if (mWriteFileIndex - mReadFileIndex > MAX_LOG_FILE_NUM) {
            return false;
        }

        synchronized (this) {
            String filename = LOGCAT_FILE_BASE_NAME + mWriteFileIndex;
            if (!writeFileOnce(filename, content, Context.MODE_PRIVATE + Context.MODE_APPEND)) {
                return false;
            }

            mWriteFilePosition += content.length();
            if (mWriteFilePosition >= MAX_LOG_FILE_SIZE) {
                mWriteFileIndex++;
                mWriteFilePosition = 0;
            }
            updateIndexFile();
        }
        return true;
    }

    private boolean updateIndexFile() {
        try {
            JSONObject json = new JSONObject();
            json.put(KEY_READ_FILE_INDEX, mReadFileIndex);
            json.put(KEY_READ_FILE_POSITION, mReadFilePosition);
            json.put(KEY_WRITE_FILE_INDEX, mWriteFileIndex);
            json.put(KEY_WRITE_FILE_POSITION, mWriteFilePosition);
            return writeFileOnce(LOGCAT_INDEX_FILE_NAME, json.toString(), Context.MODE_PRIVATE);
        } catch (JSONException e) {
            LogUtils.e(TAG, "-----" + e.toString());
            e.printStackTrace();
        }
        return false;
    }

    private boolean writeFileOnce(String filename, String content, int mode) {
        FileOutputStream output = null;
        BufferedWriter writer = null;
        try {
            output = mContext.openFileOutput(filename, mode);
            writer = new BufferedWriter(new OutputStreamWriter(output));
            writer.write(content);
            writer.write("\n");
            writer.close();
            return true;
        } catch (FileNotFoundException e) {
            LogUtils.e(TAG, "------" + e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            LogUtils.e(TAG, "------" + e.toString());
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            LogUtils.d(TAG, "------" + e.toString());
            e.printStackTrace();
        } finally {
            closeSilently(output);
            closeSilently(writer);
        }
        return false;
    }

    /**
     * 解析 index 文件
     * @return
     */
    private boolean parseIndexFile() {
        try {
            String content = readFileOnce(LOGCAT_INDEX_FILE_NAME, 0);
            if (content == null) {
                deleteAllQosFiles();
                return false;
            }
            JSONObject json = new JSONObject(String.valueOf(content));
            mReadFileIndex = json.getLong(KEY_READ_FILE_INDEX);
            mReadFilePosition = json.getLong(KEY_READ_FILE_POSITION);
            mWriteFileIndex = json.getLong(KEY_WRITE_FILE_INDEX);
            mWriteFilePosition = json.getLong(KEY_WRITE_FILE_POSITION);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        deleteAllQosFiles();
        return false;
    }

    private String readFileOnce(String filename, long offset) {
        FileInputStream input = null;
        BufferedReader reader = null;
        try {
            input = mContext.openFileInput(filename);
            reader = new BufferedReader(new InputStreamReader(input));
            reader.skip(offset);
            StringBuilder builder = new StringBuilder();
            String content;
            while ((content = reader.readLine()) != null) {
                builder.append(content);
                builder.append("\n");
            }
            String result = builder.toString();
            Log.d(TAG,"----result " + result);
            if ("".equals(result)) {
                return null;
            }
            return result;
        } catch (FileNotFoundException e) {
            Log.e(TAG,"-----" + e.toString());
        } catch (IOException e) {
            Log.e(TAG,"-----" + e.toString());
        } catch (OutOfMemoryError e) {
            Log.e(TAG,"-----" + e.toString());
        } finally {
            closeSilently(input);
            closeSilently(reader);
        }
        return null;
    }

    private void closeSilently(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteAllQosFiles() {
        String[] files = mContext.fileList();
        for (String file : files) {
            if (file.startsWith(LOGCAT_FILE_PREFIX)) {
                mContext.deleteFile(file);
            }
        }
    }
}
