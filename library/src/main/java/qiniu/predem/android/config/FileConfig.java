package qiniu.predem.android.config;

import android.content.Context;

import java.io.File;

import qiniu.predem.android.util.LogUtils;

/**
 * Created by Misty on 17/6/15.
 */

public class FileConfig {
    private static final String TAG = "FileConfig";

    /**
     * Path where crash logs and temporary files are stored.
     */
    public static String FILES_PATH = null;

    public static final int MAX_LOG_FILE_SIZE = 64 * 1024;//每个文件的最大值
    public static final int MAX_LOG_FILE_NUM = 100;//最大文件数

    /**
     * http monitor logs
     */
    public static final String QOS_FILE_PREFIX = "dem_log_";
    public static final String INDEX_FILE_NAME = QOS_FILE_PREFIX + "index.json";
    public static final String KEY_READ_FILE_INDEX = "read_file_index";
    public static final String KEY_READ_FILE_POSITION = "read_file_position";
    public static final String KEY_WRITE_FILE_INDEX = "write_file_index";
    public static final String KEY_WRITE_FILE_POSITION = "write_file_position";
    public static final String CACHE_FILE_NAME = QOS_FILE_PREFIX + "cache";
    public static final String LOG_FILE_BASE_NAME = QOS_FILE_PREFIX + "log.";

    /**
     * crash logs
     */
    public static final String FIELD_APP_PACKAGE = "Package";
    public static final String FIELD_APP_VERSION_CODE = "Version Code";
    public static final String FIELD_CRASH_REPORTER_KEY = "CrashReporter Key";
    public static final String FIELD_APP_START_DATE = "Start Date";
    public static final String FIELD_APP_CRASH_DATE = "Date";
    public static final String FIELD_OS_VERSION = "Android";
    public static final String FIELD_OS_BUILD = "Android Build";
    public static final String FIELD_DEVICE_MANUFACTURER = "Manufacturer";
    public static final String FIELD_DEVICE_MODEL = "Model";
    public static final String FIELD_APP_VERSION_NAME = "Version Name";
    public static final String FIELD_THREAD_NAME = "Thread";

    public static void loadFilesPath(Context context){
        if (context != null){
            try {
                File file = context.getFilesDir();
                if (file != null){
                    FILES_PATH = file.getAbsolutePath();
                }
            }catch (Exception e){
                LogUtils.e(TAG,"Exception thrown when accessing the files dir:"+e.toString());
                e.printStackTrace();
            }
        }
    }
}
