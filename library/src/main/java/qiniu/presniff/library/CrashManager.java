package qiniu.presniff.library;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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

import qiniu.presniff.library.bean.CrashMetaData;
import qiniu.presniff.library.config.ConstantConfig;
import qiniu.presniff.library.config.NetConfig;
import qiniu.presniff.library.exception.ExceptionHandler;
import qiniu.presniff.library.http.HttpURLConnectionBuilder;
import qiniu.presniff.library.listener.CrashManagerListener;
import qiniu.presniff.library.util.LogUtils;
import qiniu.presniff.library.util.ManifestUtil;
import qiniu.presniff.library.util.NetworkUtil;

/**
 * Created by Misty on 5/22/17.
 */

public class CrashManager {
    private static final String TAG = "CrashManager";

    /**
     * Shared preferences key for always send dialog button.
     */
    private static final String ALWAYS_SEND_KEY = "always_send_crash_reports";

    private static final int STACK_TRACES_FOUND_NONE = 0;
    private static final int STACK_TRACES_FOUND_NEW = 1;
    private static final int STACK_TRACES_FOUND_CONFIRMED = 2;

    /**
     * Stack traces are currently submitted
     */
    private static boolean submitting = false;

    private static long initializeTimestamp;
    /**
     * App identifier from CrashSDK.
     */
    private static String identifier = null;
    /**
     * URL of CrashSDK service.
     */
    private static String urlString = null;

    private static boolean didCrashInLastSession = false;

    public static void register(Context context){
        String appIdentifier = ManifestUtil.getAppIdentifier(context);
        if (TextUtils.isEmpty(appIdentifier)) {
            throw new IllegalArgumentException("HockeyApp app identifier was not configured correctly in manifest or build configuration.");
        }
        register(context, appIdentifier);
    }

    public static void register(Context context, String appIdentifier){
        register(context, NetConfig.CRASH_URL, appIdentifier, null);
    }

    public static void register(Context context, String appIdentifier, CrashManagerListener listener) {
        register(context, NetConfig.CRASH_URL, appIdentifier, listener);
    }

    public static void register(Context context, String urlString, String appIdentifier, CrashManagerListener listener) {
        initialize(context, urlString, appIdentifier, listener, false);
        execute(context, listener);
    }

    public static void initialize(Context context, String appIdentifier, CrashManagerListener listener){
        initialize(context, NetConfig.CRASH_URL, appIdentifier, listener, true);
    }

    public static void initialize(Context context, String urlString, String appIdentifier, CrashManagerListener listener){
        initialize(context, urlString, appIdentifier, listener, true);
    }

    public static void initialize(Context context, String urlString, String appIdentifier, CrashManagerListener listener, boolean registerHandler){
        if (context != null){
            if (CrashManager.initializeTimestamp == 0){
                CrashManager.initializeTimestamp = System.currentTimeMillis();
            }
            CrashManager.urlString = urlString;
            CrashManager.identifier = ManifestUtil.sanitizeAppIdentifier(appIdentifier);
            CrashManager.didCrashInLastSession = false;

            ConstantConfig.loadFromContext(context);

            if (CrashManager.identifier == null){
                CrashManager.identifier = ConstantConfig.APP_PACKAGE;
            }

            if (registerHandler) {
                Boolean ignoreDefaultHandler = (listener != null) && (listener.ignoreDefaultHandler());
                WeakReference<Context> weakContext = new WeakReference<Context>(context);
                registerHandler(weakContext, listener, ignoreDefaultHandler);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static void execute(Context context, CrashManagerListener listener) {
        Boolean ignoreDefaultHandler = (listener != null) && (listener.ignoreDefaultHandler());
        WeakReference<Context> weakContext = new WeakReference<Context>(context);

        int foundOrSend = hasStackTraces(weakContext);
        if (foundOrSend == STACK_TRACES_FOUND_NEW) {
            didCrashInLastSession = true;
            Boolean autoSend = !(context instanceof Activity);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            autoSend |= prefs.getBoolean(ALWAYS_SEND_KEY, false);

            if (listener != null) {
                autoSend |= listener.shouldAutoUploadCrashes();
                autoSend |= listener.onCrashesFound();

                listener.onNewCrashesFound();
            }

//            if (!autoSend) {
//                showDialog(weakContext, listener, ignoreDefaultHandler);
//            } else {
                sendCrashes(weakContext, listener, ignoreDefaultHandler);
//            }
        } else if (foundOrSend == STACK_TRACES_FOUND_CONFIRMED) {
            if (listener != null) {
                listener.onConfirmedCrashesFound();
            }

            sendCrashes(weakContext, listener, ignoreDefaultHandler);
        } else {
            registerHandler(weakContext, listener, ignoreDefaultHandler);
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
                SharedPreferences preferences = context.getSharedPreferences("HockeySDK", Context.MODE_PRIVATE);
                result = Arrays.asList(preferences.getString("ConfirmedFilenames", "").split("\\|"));
            }
        }
        return result;
    }

    private static String[] searchForStackTraces() {
        if (ConstantConfig.FILES_PATH != null) {
            LogUtils.d(TAG, "Looking for exceptions in: " + ConstantConfig.FILES_PATH);

            // Try to create the files folder if it doesn't exist
            File dir = new File(ConstantConfig.FILES_PATH + "/");
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
            LogUtils.d(TAG,"Can't search for exception as file path is null.");
            return null;
        }
    }

    /**
     * Starts thread to send crashes to HockeyApp, then registers the exception
     * handler.
     */
    private static void sendCrashes(final WeakReference<Context> weakContext, final CrashManagerListener listener, final boolean ignoreDefaultHandler) {
        sendCrashes(weakContext, listener, ignoreDefaultHandler, null);
    }

    /**
     * Starts thread to send crashes to HockeyApp, then registers the exception
     * handler.
     */
    private static void sendCrashes(final WeakReference<Context> weakContext, final CrashManagerListener listener, final boolean ignoreDefaultHandler, final CrashMetaData crashMetaData) {
        saveConfirmedStackTraces(weakContext);
        registerHandler(weakContext, listener, ignoreDefaultHandler);

        Context ctx = weakContext.get();
        if (ctx != null && !NetworkUtil.isConnectedToNetwork(ctx)) {
            // Not connected to network, not trying to submit stack traces
            listener.onCrashesNotSent();
            return;
        }

        if (!submitting) {
            submitting = true;

            new Thread() {
                @Override
                public void run() {
                    submitStackTraces(weakContext, listener, crashMetaData);
                    submitting = false;
                }
            }.start();
        }
    }

    /**
     * Submits all stack traces in the files dir to HockeyApp.
     *
     * @param weakContext The context to use. Usually your Activity object.
     * @param listener    Implement for callback functions.
     */
    public static void submitStackTraces(WeakReference<Context> weakContext, CrashManagerListener listener) {
        submitStackTraces(weakContext, listener, null);
    }

    /**
     * Submits all stack traces in the files dir to HockeyApp.
     *
     * @param weakContext   The context to use. Usually your Activity object.
     * @param listener      Implement for callback functions.
     * @param crashMetaData The crashMetaData, provided by the user.
     */
    public static void submitStackTraces(WeakReference<Context> weakContext, CrashManagerListener listener, CrashMetaData crashMetaData) {
        String[] list = searchForStackTraces();
        Boolean successful = false;

        if ((list != null) && (list.length > 0)) {
            LogUtils.e(TAG,"-----Found " + list.length + " stacktrace(s).");

            for (int index = 0; index < list.length; index++) {
                HttpURLConnection urlConnection = null;
                try {
                    // Read contents of stack trace
                    String filename = list[index];
                    String stacktrace = contentsOfFile(weakContext, filename);
                    if (stacktrace.length() > 0) {
                        // Transmit stack trace with POST request

                        LogUtils.e(TAG,"----Transmitting crash data: \n" + stacktrace);

                        // Retrieve user ID and contact information if given
                        String userID = contentsOfFile(weakContext, filename.replace(".stacktrace", ".user"));
                        String contact = contentsOfFile(weakContext, filename.replace(".stacktrace", ".contact"));

                        if (crashMetaData != null) {
                            final String crashMetaDataUserID = crashMetaData.getUserID();
                            if (!TextUtils.isEmpty(crashMetaDataUserID)) {
                                userID = crashMetaDataUserID;
                            }
                            final String crashMetaDataContact = crashMetaData.getUserEmail();
                            if (!TextUtils.isEmpty(crashMetaDataContact)) {
                                contact = crashMetaDataContact;
                            }
                        }

                        // Append application log to user provided description if present, if not, just send application log
                        final String applicationLog = contentsOfFile(weakContext, filename.replace(".stacktrace", ".description"));
                        String description = crashMetaData != null ? crashMetaData.getUserDescription() : "";
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
                        parameters.put("sdk", ConstantConfig.SDK_NAME);
                        parameters.put("sdk_version", BuildConfig.VERSION_NAME);

                        urlConnection = new HttpURLConnectionBuilder(getURLString())
                                .setRequestMethod("POST")
                                .writeFormFields(parameters)
                                .build();

                        int responseCode = urlConnection.getResponseCode();

                        LogUtils.e(TAG,"-----code : " + responseCode);

                        successful = (responseCode == HttpURLConnection.HTTP_ACCEPTED || responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (successful) {
                        LogUtils.d(TAG,"-----Transmission succeeded");
                        deleteStackTrace(weakContext, list[index]);

                        if (listener != null) {
                            listener.onCrashesSent();
                            deleteRetryCounter(weakContext, list[index], listener.getMaxRetryAttempts());
                        }
                    } else {
                        LogUtils.d(TAG,"-----Transmission failed, will retry on next register() call");
                        if (listener != null) {
                            listener.onCrashesNotSent();
                            updateRetryCounter(weakContext, list[index], listener.getMaxRetryAttempts());
                        }
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
     * Registers the exception handler.
     */
    private static void registerHandler(WeakReference<Context> weakContext, CrashManagerListener listener, boolean ignoreDefaultHandler) {
        if (!TextUtils.isEmpty(ConstantConfig.APP_PACKAGE)) {
            // Get current handler
            Thread.UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
            if (currentHandler != null) {
                LogUtils.d(TAG,"Current handler class = " + currentHandler.getClass().getName());
            }

            // Update listener if already registered, otherwise set new handler
            if (currentHandler instanceof ExceptionHandler) {
                ((ExceptionHandler) currentHandler).setListener(listener);
            } else {
                Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(currentHandler, listener, ignoreDefaultHandler));
            }
        } else {
            LogUtils.d(TAG,"Exception handler not set because version or package is null.");
        }
    }

    /**
     * Shows a dialog to ask the user whether he wants to send crash reports to
     * HockeyApp or delete them.
     */
//    private static void showDialog(final WeakReference<Context> weakContext, final CrashManagerListener listener, final boolean ignoreDefaultHandler) {
//        Context context = null;
//        if (weakContext != null) {
//            context = weakContext.get();
//        }
//
//        if (context == null) {
//            return;
//        }
//
//        if (listener != null && listener.onHandleAlertView()) {
//            return;
//        }
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        String alertTitle = getAlertTitle(context);
//        builder.setTitle(alertTitle);
//        builder.setMessage(R.string.crash_dialog_message);
//
//        builder.setNegativeButton(R.string.crash_dialog_negative_button, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                handleUserInput(CrashManagerUserInput.CrashManagerUserInputDontSend, null, listener, weakContext, ignoreDefaultHandler);
//            }
//        });
//
//        builder.setNeutralButton(R.string.crash_dialog_neutral_button, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                handleUserInput(CrashManagerUserInput.CrashManagerUserInputAlwaysSend, null, listener, weakContext, ignoreDefaultHandler);
//            }
//        });
//
//        builder.setPositiveButton(R.string.crash_dialog_positive_button, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                handleUserInput(CrashManagerUserInput.CrashManagerUserInputSend, null, listener,
//                        weakContext, ignoreDefaultHandler);
//            }
//        });
//
//        builder.create().show();
//    }

    public static long getInitializeTimestamp() {
        return initializeTimestamp;
    }

    /**
     * Returns the complete URL for the HockeyApp API.
     */
    private static String getURLString() {
//        String str = urlString + "api/2/apps/"+ identifier + "/crashes/";
        String str = urlString + "api/1/a/apps/" + identifier + "/crashes";
        LogUtils.d(TAG,"-----url : " + str);
        return str;
    }
}
