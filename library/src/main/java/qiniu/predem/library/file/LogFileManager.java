package qiniu.predem.library.file;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import qiniu.predem.library.util.LogUtils;

/**
 * Created by Misty on 5/19/17.
 */

public class LogFileManager {
    private static final String TAG = "LogFileManager";

    private static final String QOS_FILE_PREFIX = "apm_log_";
    private static final String INDEX_FILE_NAME = QOS_FILE_PREFIX + "index.json";
    private static final String KEY_READ_FILE_INDEX = "read_file_index";
    private static final String KEY_READ_FILE_POSITION = "read_file_position";
    private static final String KEY_WRITE_FILE_INDEX = "write_file_index";
    private static final String KEY_WRITE_FILE_POSITION = "write_file_position";
    // generated if a failed report exists when 'QosFileManager' destroy
    private static final String CACHE_FILE_NAME = QOS_FILE_PREFIX + "cache";
    private static final String LOG_FILE_BASE_NAME = QOS_FILE_PREFIX + "log.";

    private static final int MAX_LOG_FILE_NUM = 100;//最大文件数
    private static final int MAX_LOG_FILE_SIZE = 64 * 1024;//每个文件的最大值

    // always synchronized with the ‘index.json’
    private long mReadFileIndex = 0;
    private long mReadFilePosition = 0;
    private long mWriteFileIndex = 0;
    private long mWriteFilePosition = 0;

    private boolean mIsInitialized = false;
    private Context mContext;
    private String mCachedReportContent = null;

    private LogFileManager(){}

    public static LogFileManager getInstance(){
        return LogFileManagerHolder.instance;
    }

    public void initialize(Context context) {
        if (mIsInitialized) {
            return;
        }
        mContext = context.getApplicationContext();
        parseIndexFile();
        if (isCacheFileExist()) {
            mCachedReportContent = readFileOnce(context, CACHE_FILE_NAME, 0);
        }
        mIsInitialized = true;
    }

    private static String readFileOnce(Context context, String filename, long offset) {
        FileInputStream input = null;
        BufferedReader reader = null;
        try {
            input = context.openFileInput(filename);
            reader = new BufferedReader(new InputStreamReader(input));
            reader.skip(offset);
            StringBuilder builder = new StringBuilder();
            String content;
            while ((content = reader.readLine()) != null) {
                builder.append(content);
                builder.append("\n");
            }
            String result = builder.toString();
            if ("".equals(result)) {
                return null;
            }
            return result;
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            closeSilently(input);
            closeSilently(reader);
        }
        return null;
    }

    private static void closeSilently(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isCacheFileExist() {
        String cachePath = mContext.getFilesDir().getAbsolutePath() + "/" + CACHE_FILE_NAME;
        return new File(cachePath).exists();
    }

    private boolean parseIndexFile() {
        try {
            String content = readFileOnce(mContext, INDEX_FILE_NAME, 0);
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

    private void deleteAllQosFiles() {
        String[] files = mContext.fileList();
        for (String file : files) {
            if (file.startsWith(QOS_FILE_PREFIX)) {
                mContext.deleteFile(file);
            }
        }
    }

    public String getReportContent() {
//        LogUtils.d(TAG,"-------mIsInitialized:"+(!mIsInitialized));
        if (!mIsInitialized) {
            return null;
        }

        if (mCachedReportContent != null) {
            return mCachedReportContent;
        }

        if (mReadFileIndex == mWriteFileIndex && mReadFilePosition == mWriteFilePosition) {
            return null;
        }

        synchronized (this) {
            // whether the file should be deleted after read success
            boolean isDeleteFile = (mReadFileIndex < mWriteFileIndex);

            String filename = LOG_FILE_BASE_NAME + mReadFileIndex;
            mCachedReportContent = readFileOnce(mContext, filename, mReadFilePosition);
            if (mCachedReportContent == null) {
                return null;
            }

            mReadFilePosition += mCachedReportContent.length();
            if (mReadFileIndex < mWriteFileIndex) {
                mReadFileIndex++;
                mReadFilePosition = 0;
            }
            // we don't need care about whether the update failed or not
            // because these four values are always synchronized in memory or in old index.json
            // if `updateIndexFile` failed here, the log won't lose because we have cached the content.
            updateIndexFile();

            // delete the file
            if (isDeleteFile) {
                long fileIndex = mReadFileIndex - 1;
                mContext.deleteFile(LOG_FILE_BASE_NAME + fileIndex);
            }
        }
        String str= "1\\tappName\\tpackageName\\t4.4.4\\nCustom Phone - 4.4.4 - API 19 - 768x1280\\tunknown\\twww.baidu.com\\t-\\nGET\\t115.239.210.27\\t200\\t1495114974995\\n1495114975967\\t1495114976311\\t944\\t3012\\n-1\\t-\\t1\\tappName\\tpackageName\\t4.4.4\\nCustom Phone - 4.4.4 - API 19 - 768x1280\\tunknown\\twww.baidu.com\\t-\\nGET\\t115.239.210.27\\t200\\t1495114974995\\n1495114975967\\t1495114976311\\t944\\t3012\\n-1\\t-\\t";
        return str;//mCachedReportContent;
    }

    public boolean addReportContent(String content) {
//        LogUtils.d(TAG,"------mIsInitialized:"+(!mIsInitialized));
        if (!mIsInitialized) {
            return false;
        }

//        LogUtils.d(TAG,"-----mWriteFileIndex:"+mWriteFileIndex+";mReadFileIndex:"+mReadFileIndex+";"+(mWriteFileIndex-mReadFileIndex));
        // so many log files unreported, maybe the server closed forever
        if (mWriteFileIndex - mReadFileIndex > MAX_LOG_FILE_NUM) {
            return false;
        }

        synchronized (this) {
            String filename = LOG_FILE_BASE_NAME + mWriteFileIndex;
//            LogUtils.d(TAG,"------fileName:"+filename);
            if (!writeFileOnce(mContext, filename, content, Context.MODE_PRIVATE + Context.MODE_APPEND)) {
                return false;
            }

            mWriteFilePosition += content.length();
            if (mWriteFilePosition >= MAX_LOG_FILE_SIZE) {
                mWriteFileIndex++;
                mWriteFilePosition = 0;
            }
            // we don't need care about whether the update failed or not
            // because these four values are always synchronized in memory or in old index.json
            // if `updateIndexFile` failed here, the logs will be covered only if the app restart
            updateIndexFile();
        }
        return true;
    }

    private static boolean writeFileOnce(Context context, String filename, String content, int mode) {
//        LogUtils.d(TAG,"------writeFileOnce()");
        FileOutputStream output = null;
        BufferedWriter writer = null;
        try {
            output = context.openFileOutput(filename, mode);
            writer = new BufferedWriter(new OutputStreamWriter(output));
            writer.write(content);
            writer.close();
            return true;
        } catch (FileNotFoundException e) {
            LogUtils.e(TAG,"------"+e.toString());
        } catch (IOException e) {
//            e.printStackTrace();
            LogUtils.e(TAG,"------"+e.toString());
        } catch (OutOfMemoryError e) {
//            e.printStackTrace();
            LogUtils.d(TAG,"------"+e.toString());
        } finally {
            closeSilently(output);
            closeSilently(writer);
        }
        return false;
    }

    private boolean updateIndexFile() {
        try {
            JSONObject json = new JSONObject();
            json.put(KEY_READ_FILE_INDEX, mReadFileIndex);
            json.put(KEY_READ_FILE_POSITION, mReadFilePosition);
            json.put(KEY_WRITE_FILE_INDEX, mWriteFileIndex);
            json.put(KEY_WRITE_FILE_POSITION, mWriteFilePosition);
            return writeFileOnce(mContext, INDEX_FILE_NAME, json.toString(), Context.MODE_PRIVATE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setReportSuccess() {
        mCachedReportContent = null;
        if (isCacheFileExist()) {
            mContext.deleteFile(CACHE_FILE_NAME);
        }
    }

    public void destroy() {
        if (!mIsInitialized) {
            return;
        }
        if (mCachedReportContent != null) {
            writeFileOnce(mContext, CACHE_FILE_NAME, mCachedReportContent, Context.MODE_PRIVATE);
            mCachedReportContent = null;
        }
        mIsInitialized = false;
    }

    public static class LogFileManagerHolder{
        public final static LogFileManager instance = new LogFileManager();
    }
}
