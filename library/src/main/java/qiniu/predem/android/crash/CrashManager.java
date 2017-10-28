package qiniu.predem.android.crash;

import android.content.Context;
import android.text.TextUtils;

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
import java.util.Arrays;
import java.util.List;

import qiniu.predem.android.bean.AppBean;
import qiniu.predem.android.config.Configuration;
import qiniu.predem.android.config.FileConfig;
import qiniu.predem.android.util.Functions;
import qiniu.predem.android.util.HttpURLConnectionBuilder;
import qiniu.predem.android.util.LogUtils;
import qiniu.predem.android.util.NetworkUtil;
import qiniu.predem.android.util.SharedPreUtil;

import static qiniu.predem.android.config.FileConfig.FIELD_REPORT_UUID;
import static qiniu.predem.android.config.FileConfig.FILELD_CRASH_CONTENT;
import static qiniu.predem.android.config.FileConfig.FILELD_CRASH_TIME;
import static qiniu.predem.android.config.FileConfig.FILELD_START_TIME;

/**
 * Created by Misty on 17/6/15.
 */

public final class CrashManager {
    private static final String TAG = "CrashManager";
    private static final int STACK_TRACES_FOUND_NONE = 0;
    private static final int STACK_TRACES_FOUND_NEW = 1;
    private static final int STACK_TRACES_FOUND_CONFIRMED = 2;
    /**
     * Stack traces are currently submitted
     */
    private static boolean submitting = false;
    private static long initializeTimestamp;

    /**
     * Registers new crash manager and handles existing crash logs.
     * App Identifier is read from configuration values in AndroidManifest.xml
     *
     * @param context
     */
    public static void register(Context context) {
        initialize(context);
        execute(context);
    }

    public static void initialize(Context context) {
        if (context != null) {
            if (CrashManager.initializeTimestamp == 0) {
                CrashManager.initializeTimestamp = System.currentTimeMillis();
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static void execute(Context context) {
        WeakReference<Context> weakContext = new WeakReference<Context>(context);

        int foundOrSend = hasStackTraces(weakContext);
        if (foundOrSend == STACK_TRACES_FOUND_NEW || foundOrSend == STACK_TRACES_FOUND_CONFIRMED) {
            sendCrashes(weakContext, false);
        } else {
            registerHandler(weakContext, false);
        }
    }

    public static int hasStackTraces(WeakReference<Context> weakContext) {
        String[] filenames = searchForStackTraces();
        List<String> confirmedFilenames = null;
        int result = STACK_TRACES_FOUND_NONE;
        if ((filenames != null) && (filenames.length > 0)) {
            try {
                confirmedFilenames = getConfirmedFilenames(weakContext);
            } catch (Exception e) {
                LogUtils.e(TAG, e.toString());
                e.printStackTrace();
            }

            if (confirmedFilenames != null) {
                result = STACK_TRACES_FOUND_CONFIRMED;

                for (String filename : filenames) {
                    if (!confirmedFilenames.contains(filename)) {
                        result = STACK_TRACES_FOUND_NEW;
                        break;
                    }
                }
            } else {
                result = STACK_TRACES_FOUND_NEW;
            }
        }
        return result;
    }

    private static List<String> getConfirmedFilenames(WeakReference<Context> weakContext) {
        List<String> result = null;
        if (weakContext != null) {
            Context context = weakContext.get();
            if (context != null) {
                String[] res = SharedPreUtil.getCrashConfirmedFilenames(context).split("\\|");
                result = Arrays.asList(res);
            }
        }
        return result;
    }

    private static String[] searchForStackTraces() {
        if (FileConfig.FILES_PATH != null) {
            LogUtils.d("Looking for exceptions in: " + FileConfig.FILES_PATH);

            File dir = new File(FileConfig.FILES_PATH + "/");
            boolean created = dir.mkdir();
            if (!created && !dir.exists()) {
                return new String[0];
            }

            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".stacktrace");
                }
            };
            return dir.list(filter);
        } else {
            LogUtils.d("Can't search for exception as file path is null.");
            return null;
        }
    }

    /**
     * Registers the exception handler.
     */
    private static void registerHandler(WeakReference<Context> weakContext, boolean ignoreDefaultHandler) {
        if (!TextUtils.isEmpty(AppBean.APP_VERSION) && !TextUtils.isEmpty(AppBean.APP_PACKAGE)) {
            // Get current handler
            Thread.UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
            if (currentHandler != null) {
                LogUtils.d("Current handler class = " + currentHandler.getClass().getName());
            }

            Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(currentHandler, ignoreDefaultHandler));
        } else {
            LogUtils.d("Exception handler not set because version or package is null.");
        }
    }

    /**
     * Starts thread to send crashes to HockeyApp, then registers the exception
     * handler.
     */
    private static void sendCrashes(final WeakReference<Context> weakContext, final boolean ignoreDefaultHandler) {
//        saveConfirmedStackTraces(weakContext);
        registerHandler(weakContext, ignoreDefaultHandler);

        Context ctx = weakContext.get();
        if (ctx != null && !NetworkUtil.isConnectedToNetwork(ctx)) {
            return;
        }

        if (!submitting) {
            submitting = true;
            new Thread() {
                @Override
                public void run() {
                    submitStackTraces(weakContext);
                    submitting = false;
                }
            }.start();
        }
    }

    /**
     * Submits all stack traces in the files dir to HockeyApp.
     *
     * @param weakContext The context to use. Usually your Activity object.
     */
    public static void submitStackTraces(final WeakReference<Context> weakContext) {
        final String[] list = searchForStackTraces();

        if ((list != null) && (list.length > 0)) {
            for (int index = 0; index < list.length; index++) {
                HttpURLConnection urlConnection = null;
                InputStream is = null;
                try {
                    String filename = list[index];
                    String stacktrace = contentsOfFile(weakContext, filename);
                    if (stacktrace.length() > 0) {
                        JSONObject crashBean = new JSONObject(stacktrace);
                        //1、getuptoken
                        String crash = crashBean.optString(FILELD_CRASH_CONTENT);
                        String md5 = Functions.getStringMd5(crash);
                        urlConnection = new HttpURLConnectionBuilder(Configuration.getCrashUpToken() + "?md5=" + md5)
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
                        final String ls = list[index];
                        final JSONObject bean = crashBean;
                        Zone zone = FixedZone.zone0;
                        com.qiniu.android.storage.Configuration config = new com.qiniu.android.storage.Configuration.Builder().zone(zone).build();
                        UploadManager uploadManager = Functions.getUploadManager();
                        uploadManager.put(crash.getBytes(), key, token, new UpCompletionHandler() {
                            @Override
                            public void complete(final String key, ResponseInfo info, JSONObject response) {
                                if (info.isOK()) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            sendRequest(weakContext, key, bean, ls);
                                        }
                                    }.start();
                                } else {
                                    return;
                                }
                            }
                        }, null);
                    }
                } catch (Exception e) {
                    LogUtils.e(TAG, "-----" + e.toString());
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        }
    }

    private static void sendRequest(WeakReference<Context> weakContext, String key, JSONObject bean, String list) {
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
            parameters.put("sdk_id", AppBean.SDK_ID);
            parameters.put("device_id", AppBean.DEVICE_IDENTIFIER);
            parameters.put("tag", AppBean.APP_TAG);
            parameters.put("report_uuid", bean.optString(FIELD_REPORT_UUID));
            parameters.put("crash_log_key", key);
            parameters.put("manufacturer", AppBean.PHONE_MANUFACTURER);
            parameters.put("start_time", bean.optString(FILELD_START_TIME));
            parameters.put("crash_time", bean.optString(FILELD_CRASH_TIME));

            url = new HttpURLConnectionBuilder(Configuration.getCrashUrl())
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
                deleteStackTrace(weakContext, list);
            } else {
                LogUtils.d("-----Transmission failed, will retry on next register() call");
            }
        }
    }

    /**
     * Saves the list of the stack traces' file names in shared preferences.
     */
    private static void saveConfirmedStackTraces(WeakReference<Context> weakContext) {
        Context context = null;
        if (weakContext != null) {
            context = weakContext.get();
            if (context != null) {
                try {
                    String[] filenames = searchForStackTraces();
                    SharedPreUtil.setCrashConfirmedFilenames(context, joinArray(filenames, "|"));
                } catch (Exception e) {
                    // Just in case, we catch all exceptions here
                }
            }
        }
    }

    /**
     * Returns a string created by each element of the array, separated by
     * delimiter.
     */
    private static String joinArray(String[] array, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        for (int index = 0; index < array.length; index++) {
            buffer.append(array[index]);
            if (index < array.length - 1) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
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

                String user = filename.replace(".stacktrace", ".user");
                context.deleteFile(user);

                String contact = filename.replace(".stacktrace", ".contact");
                context.deleteFile(contact);

                String description = filename.replace(".stacktrace", ".description");
                context.deleteFile(description);

                SharedPreUtil.removeCrashConfirmedFilenames(context);
            }
        }
    }

    public static long getInitializeTimestamp() {
        return initializeTimestamp;
    }
}