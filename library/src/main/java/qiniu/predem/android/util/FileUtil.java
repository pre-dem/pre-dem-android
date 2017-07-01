package qiniu.predem.android.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Locale;

import qiniu.predem.android.bean.CrashBean;
import qiniu.predem.android.config.FileConfig;

import static qiniu.predem.android.config.FileConfig.CACHE_FILE_NAME;
import static qiniu.predem.android.config.FileConfig.FIELD_APP_CRASH_DATE;
import static qiniu.predem.android.config.FileConfig.FIELD_APP_PACKAGE;
import static qiniu.predem.android.config.FileConfig.FIELD_APP_START_DATE;
import static qiniu.predem.android.config.FileConfig.FIELD_APP_VERSION_CODE;
import static qiniu.predem.android.config.FileConfig.FIELD_APP_VERSION_NAME;
import static qiniu.predem.android.config.FileConfig.FIELD_CRASH_REPORTER_KEY;
import static qiniu.predem.android.config.FileConfig.FIELD_DEVICE_MANUFACTURER;
import static qiniu.predem.android.config.FileConfig.FIELD_DEVICE_MODEL;
import static qiniu.predem.android.config.FileConfig.FIELD_OS_BUILD;
import static qiniu.predem.android.config.FileConfig.FIELD_OS_VERSION;
import static qiniu.predem.android.config.FileConfig.FIELD_THREAD_NAME;
import static qiniu.predem.android.config.FileConfig.INDEX_FILE_NAME;
import static qiniu.predem.android.config.FileConfig.KEY_READ_FILE_INDEX;
import static qiniu.predem.android.config.FileConfig.KEY_READ_FILE_POSITION;
import static qiniu.predem.android.config.FileConfig.KEY_WRITE_FILE_INDEX;
import static qiniu.predem.android.config.FileConfig.KEY_WRITE_FILE_POSITION;
import static qiniu.predem.android.config.FileConfig.LOG_FILE_BASE_NAME;
import static qiniu.predem.android.config.FileConfig.MAX_LOG_FILE_NUM;
import static qiniu.predem.android.config.FileConfig.MAX_LOG_FILE_SIZE;
import static qiniu.predem.android.config.FileConfig.QOS_FILE_PREFIX;

/**
 * Created by Misty on 17/6/15.
 */

public class FileUtil {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
    private static final String TAG = "FileUtil";
    private static final String FIELD_FORMAT = "Format";
    private static final String FIELD_FORMAT_VALUE = "Xamarin";

    private boolean mIsInitialized = false;
    private Context mContext;

    /**
     * always synchronized with the ‘index.json’
     */
    private long mReadFileIndex = 0;
    private long mReadFilePosition = 0;
    private long mWriteFileIndex = 0;
    private long mWriteFilePosition = 0;

    /**
     * cached content
     */
    private String mCachedReportContent = null;

    private FileUtil() {
    }

    public static FileUtil getInstance() {
        return FileUtilHolder.instance;
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

    private static boolean writeFileOnce(Context context, String filename, String content, int mode) {
        FileOutputStream output = null;
        BufferedWriter writer = null;
        try {
            output = context.openFileOutput(filename, mode);
            writer = new BufferedWriter(new OutputStreamWriter(output));
            writer.write(content);
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

    private boolean isCacheFileExist() {
        String cachePath = FileConfig.FILES_PATH + "/" + CACHE_FILE_NAME;
        return new File(cachePath).exists();
    }

    public boolean addReportContent(String content) {
        if (!mIsInitialized) {
            return false;
        }

        // so many log files unreported, maybe the server closed forever
        if (mWriteFileIndex - mReadFileIndex > MAX_LOG_FILE_NUM) {
            return false;
        }

        synchronized (this) {
            String filename = LOG_FILE_BASE_NAME + mWriteFileIndex;
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

    public String getReportContent() {
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
        return mCachedReportContent;
    }

    public void writeCrashReport(CrashBean crashBean) {
        if (TextUtils.isEmpty(FileConfig.FILES_PATH)) {
            return;
        }
        String path = FileConfig.FILES_PATH + "/" + crashBean.getCrashIdentifier() + ".stacktrace";
        writeCrashReport(path, crashBean);
    }

    public void writeCrashReport(final String path, CrashBean crashBean) {
        LogUtils.d(TAG, "Writing unhandled exception to: " + path);

        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(path));

            writeHeader(writer, FIELD_APP_PACKAGE, crashBean.getAppPackage());
            writeHeader(writer, FIELD_APP_VERSION_CODE, crashBean.getAppPackage());
            writeHeader(writer, FIELD_APP_VERSION_NAME, crashBean.getAppVersionName());
            writeHeader(writer, FIELD_OS_VERSION, crashBean.getOsVersion());
            writeHeader(writer, FIELD_OS_BUILD, crashBean.getOsBuild());
            writeHeader(writer, FIELD_DEVICE_MANUFACTURER, crashBean.getDeviceManufacturer());
            writeHeader(writer, FIELD_DEVICE_MODEL, crashBean.getDeviceModel());
            writeHeader(writer, FIELD_THREAD_NAME, crashBean.getThreadName());
            writeHeader(writer, FIELD_CRASH_REPORTER_KEY, crashBean.getReporterKey());

            writeHeader(writer, FIELD_APP_START_DATE, DATE_FORMAT.format(crashBean.getAppStartDate()));
            writeHeader(writer, FIELD_APP_CRASH_DATE, DATE_FORMAT.format(crashBean.getAppCrashDate()));

            if (crashBean.getXamarinException()) {
                writeHeader(writer, FIELD_FORMAT, FIELD_FORMAT_VALUE);
            }

            writer.write("\n");
            writer.write(crashBean.getThrowableStackTrace());

            writer.flush();

        } catch (IOException e) {
            LogUtils.e(TAG, "Error saving crash report!" + e.toString());
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e1) {
                LogUtils.e(TAG, "Error saving crash report!" + e1.toString());
            }
        }
    }

    private void writeHeader(Writer writer, String name, String value) throws IOException {
        writer.write(name + ": " + value + "\n");
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
            LogUtils.e(TAG, "-----" + e.toString());
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

    private void deleteAllQosFiles() {
        String[] files = mContext.fileList();
        for (String file : files) {
            if (file.startsWith(QOS_FILE_PREFIX)) {
                mContext.deleteFile(file);
            }
        }
    }

    public static class FileUtilHolder {
        @SuppressLint("StaticFieldLeak")
        public final static FileUtil instance = new FileUtil();
    }
}
