package qiniu.predem.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import qiniu.predem.android.bean.AppBean;
import qiniu.predem.android.bean.CrashBean;
import qiniu.predem.android.config.FileConfig;
import qiniu.predem.android.config.HttpConfig;
import qiniu.predem.android.crash.ExceptionHandler;
import qiniu.predem.android.http.HttpURLConnectionBuilder;
import qiniu.predem.android.util.LogUtils;
import qiniu.predem.android.util.NetworkUtil;

/**
 * Created by Misty on 17/6/15.
 */

public class CrashManager {
    private static final String TAG = "CrashManager";
    /**
     * Stack traces are currently submitted
     */
    private static boolean submitting = false;

    private static long initializeTimestamp;

    private static final int STACK_TRACES_FOUND_NONE = 0;
    private static final int STACK_TRACES_FOUND_NEW = 1;
    private static final int STACK_TRACES_FOUND_CONFIRMED = 2;

    /**
     * Registers new crash manager and handles existing crash logs.
     * App Identifier is read from configuration values in AndroidManifest.xml
     * @param context
     */
    public static void register(Context context){
//        String appIdentifier = Util.getAppIdentifier(context);
//        if (TextUtils.isEmpty()) {
//            throw new IllegalArgumentException("HockeyApp app identifier was not configured correctly in manifest or build configuration.");
//        }
        // TODO: 17/6/15 判断appkey是否为null
        initialize(context, false);
        execute(context);
    }

    public static void initialize(Context context, boolean registerHandler){
        if (context != null){
            if (CrashManager.initializeTimestamp == 0){
                CrashManager.initializeTimestamp = System.currentTimeMillis();
            }

            if (registerHandler) {
                Boolean ignoreDefaultHandler = false;//(listener != null) && (listener.ignoreDefaultHandler());
                WeakReference<Context> weakContext = new WeakReference<Context>(context);
                registerHandler(weakContext, ignoreDefaultHandler);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static void execute(Context context) {
        Boolean ignoreDefaultHandler = false;//(listener != null) && (listener.ignoreDefaultHandler());
        WeakReference<Context> weakContext = new WeakReference<Context>(context);

        int foundOrSend = hasStackTraces(weakContext);
        if (foundOrSend == STACK_TRACES_FOUND_NEW || foundOrSend == STACK_TRACES_FOUND_CONFIRMED) {
            sendCrashes(weakContext, ignoreDefaultHandler);
        }else {
            registerHandler(weakContext, ignoreDefaultHandler);
        }
    }

    public static int hasStackTraces(WeakReference<Context> weakContext) {
        //filter .stacktrace files
        String[] filenames = searchForStackTraces();
        List<String> confirmedFilenames = null;
        int result = STACK_TRACES_FOUND_NONE;
        if ((filenames != null) && (filenames.length > 0)) {
            try {
                confirmedFilenames = getConfirmedFilenames(weakContext);
            } catch (Exception e) {
                // Just in case, we catch all exceptions here
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
                SharedPreferences preferences = context.getSharedPreferences("PreDemSDK", Context.MODE_PRIVATE);
                String[] res = preferences.getString("ConfirmedFilenames","").split("\\|");
                result = Arrays.asList(res);
            }
        }
        return result;
    }

    private static String[] searchForStackTraces() {
        if (FileConfig.FILES_PATH != null) {
            LogUtils.d("Looking for exceptions in: " + FileConfig.FILES_PATH);

            // Try to create the files folder if it doesn't exist
            File dir = new File(FileConfig.FILES_PATH + "/");
            boolean created = dir.mkdir();
            if (!created && !dir.exists()) {
                return new String[0];
            }

            // Filter for ".stacktrace" files
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

            // Update listener if already registered, otherwise set new handler
//            if (currentHandler instanceof ExceptionHandler) {
//                ((ExceptionHandler) currentHandler).setListener(null);
//            } else {
                Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(currentHandler, ignoreDefaultHandler));
//            }
        } else {
            LogUtils.d("Exception handler not set because version or package is null.");
        }
    }

    /**
     * Starts thread to send crashes to HockeyApp, then registers the exception
     * handler.
     */
    private static void sendCrashes(final WeakReference<Context> weakContext, final boolean ignoreDefaultHandler) {
        saveConfirmedStackTraces(weakContext);
        registerHandler(weakContext, ignoreDefaultHandler);

        Context ctx = weakContext.get();
        if (ctx != null && !NetworkUtil.isConnectedToNetwork(ctx)) {
            // Not connected to network, not trying to submit stack traces
//            listener.onCrashesNotSent();
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
     * @param weakContext   The context to use. Usually your Activity object.
     */
    public static void submitStackTraces(WeakReference<Context> weakContext) {
        String[] list = searchForStackTraces();
        Boolean successful = false;

        if ((list != null) && (list.length > 0)) {
            LogUtils.e("-----Found " + list.length + " stacktrace(s).");

            for (int index = 0; index < list.length; index++) {
                HttpURLConnection urlConnection = null;
                try {
                    // Read contents of stack trace
                    String filename = list[index];
                    String stacktrace = contentsOfFile(weakContext, filename);
                    if (stacktrace.length() > 0) {
                        // Transmit stack trace with POST request

                        LogUtils.e("----Transmitting crash data: \n" + stacktrace);

                        // Retrieve user ID and contact information if given
                        String userID = contentsOfFile(weakContext, filename.replace(".stacktrace", ".user"));
                        String contact = contentsOfFile(weakContext, filename.replace(".stacktrace", ".contact"));

//                        if (crashMetaData != null) {
//                            final String crashMetaDataUserID = crashMetaData.getUserID();
//                            if (!TextUtils.isEmpty(crashMetaDataUserID)) {
//                                userID = crashMetaDataUserID;
//                            }
//                            final String crashMetaDataContact = crashMetaData.getUserEmail();
//                            if (!TextUtils.isEmpty(crashMetaDataContact)) {
//                                contact = crashMetaDataContact;
//                            }
//                        }

                        // Append application log to user provided description if present, if not, just send application log
                        final String applicationLog = contentsOfFile(weakContext, filename.replace(".stacktrace", ".description"));
                        String description = "";//crashMetaData != null ? crashMetaData.getUserDescription() : "";
                        if (!TextUtils.isEmpty(applicationLog)) {
                            if (!TextUtils.isEmpty(description)) {
                                description = String.format("%s\n\nLog:\n%s", description, applicationLog);
                            } else {
                                description = String.format("Log:\n%s", applicationLog);
                            }
                        }

                        Map<String, String> parameters = new HashMap<String, String>();

                        parameters.put("raw", stacktrace);
                        parameters.put("userID", userID);
                        parameters.put("contact", contact);
                        parameters.put("description", description);
                        parameters.put("sdk", AppBean.SDK_NAME);
                        parameters.put("sdk_version", AppBean.ANDROID_VERSION);

                        urlConnection = new HttpURLConnectionBuilder(HttpConfig.getCrashUrl())
                                .setRequestMethod("POST")
                                .writeFormFields(parameters)
                                .build();

                        int responseCode = urlConnection.getResponseCode();

                        LogUtils.e("-----code : " + responseCode);

                        successful = (responseCode == HttpURLConnection.HTTP_ACCEPTED || responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (successful) {
                        LogUtils.d("-----Transmission succeeded");
                        deleteStackTrace(weakContext, list[index]);
                    } else {
                        LogUtils.d("-----Transmission failed, will retry on next register() call");
                    }
                }
            }
        }
    }

    /**
     * Update the retry attempts count for this crash stacktrace.
     */
    private static void updateRetryCounter(WeakReference<Context> weakContext, String filename, int maxRetryAttempts) {
        if (maxRetryAttempts == -1) {
            return;
        }

        Context context = null;
        if (weakContext != null) {
            context = weakContext.get();
            if (context != null) {
                SharedPreferences preferences = context.getSharedPreferences("HockeySDK", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();

                int retryCounter = preferences.getInt("RETRY_COUNT: " + filename, 0);
                if (retryCounter >= maxRetryAttempts) {
                    deleteStackTrace(weakContext, filename);
                    deleteRetryCounter(weakContext, filename, maxRetryAttempts);
                } else {
                    editor.putInt("RETRY_COUNT: " + filename, retryCounter + 1);
                    editor.apply();
                }
            }
        }
    }

    /**
     * Delete the retry counter if stacktrace is uploaded or retry limit is
     * reached.
     */
    private static void deleteRetryCounter(WeakReference<Context> weakContext, String filename, int maxRetryAttempts) {
        Context context = null;
        if (weakContext != null) {
            context = weakContext.get();
            if (context != null) {
                SharedPreferences preferences = context.getSharedPreferences("HockeySDK", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.remove("RETRY_COUNT: " + filename);
                editor.apply();
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
                    SharedPreferences preferences = context.getSharedPreferences("HockeySDK", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("ConfirmedFilenames", joinArray(filenames, "|"));
                    editor.apply();
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
            }
        }
    }

    /**
     * Deletes all stack traces and meta files from files dir.
     *
     * @param weakContext The context to use. Usually your Activity object.
     */
    public static void deleteStackTraces(WeakReference<Context> weakContext) {
        String[] list = searchForStackTraces();

        if ((list != null) && (list.length > 0)) {
            LogUtils.d("Found " + list.length + " stacktrace(s).");

            for (int index = 0; index < list.length; index++) {
                try {
                    Context context = null;
                    if (weakContext != null) {
                        LogUtils.d("Delete stacktrace " + list[index] + ".");
                        deleteStackTrace(weakContext, list[index]);

                        context = weakContext.get();
                        if (context != null) {
                            context.deleteFile(list[index]);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static long getInitializeTimestamp() {
        return initializeTimestamp;
    }

//    public static boolean didCrashInLastSession() {
////        return didCrashInLastSession;
//    }

    public static CrashBean getLastCrashDetails() {
        if (FileConfig.FILES_PATH == null ) {
            return null;
        }

        File dir = new File(FileConfig.FILES_PATH + "/");
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".stacktrace");
            }
        });

        long lastModification = 0;
        File lastModifiedFile = null;
        CrashBean result = null;
        for (File file : files) {
            if (file.lastModified() > lastModification) {
                lastModification = file.lastModified();
                lastModifiedFile = file;
            }
        }

        if (lastModifiedFile != null && lastModifiedFile.exists()) {
//            try {
//                result = CrashBean.fromFile(lastModifiedFile);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
        }

        return result;
    }
}
