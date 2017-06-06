package qiniu.presniff.library.exception;

import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.UUID;

import qiniu.presniff.library.CrashManager;
import qiniu.presniff.library.bean.CrashBean;
import qiniu.presniff.library.config.ConstantConfig;
import qiniu.presniff.library.listener.CrashManagerListener;
import qiniu.presniff.library.util.LogUtils;

/**
 * Created by Misty on 5/22/17.
 */

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "ExceptionHandler";

    private boolean mIgnoreDefaultHandler = false;
    private CrashManagerListener mCrashManagerListener;
    private Thread.UncaughtExceptionHandler mDefaultExceptionHandler;

    public ExceptionHandler(Thread.UncaughtExceptionHandler defaultExceptionHandler, CrashManagerListener listener, boolean ignoreDefaultHandler){
        mDefaultExceptionHandler = defaultExceptionHandler;
        mIgnoreDefaultHandler = ignoreDefaultHandler;
        mCrashManagerListener = listener;
    }

    public void setListener(CrashManagerListener listener) {
        mCrashManagerListener = listener;
    }

    @Deprecated
    @SuppressWarnings("unused")
    public static void saveException(Throwable exception, CrashManagerListener listener) {
        saveException(exception, null, listener);
    }

    public static void saveException(Throwable exception, Thread thread, CrashManagerListener listener) {
        final Date now = new Date();
        final Date startDate = new Date(CrashManager.getInitializeTimestamp());
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        BufferedWriter writer = null;
        exception.printStackTrace(printWriter);

        String filename = UUID.randomUUID().toString();

        CrashBean crashBean = new CrashBean(filename, exception);
        crashBean.setAppPackage(ConstantConfig.APP_PACKAGE);
        crashBean.setAppVersionCode(ConstantConfig.APP_VERSION);
        crashBean.setAppVersionName(ConstantConfig.APP_VERSION_NAME);
        crashBean.setAppStartDate(startDate);
        crashBean.setAppCrashDate(now);

        if ((listener == null) || (listener.includeDeviceData())) {
            crashBean.setOsVersion(ConstantConfig.ANDROID_VERSION);
            crashBean.setOsBuild(ConstantConfig.ANDROID_BUILD);
            crashBean.setDeviceManufacturer(ConstantConfig.PHONE_MANUFACTURER);
            crashBean.setDeviceModel(ConstantConfig.PHONE_MODEL);
        }

        if (thread != null && ((listener == null) || (listener.includeThreadDetails()))) {
            crashBean.setThreadName(thread.getName() + "-" + thread.getId());
        }

        if (ConstantConfig.CRASH_IDENTIFIER != null && (listener == null || listener.includeDeviceIdentifier())) {
            crashBean.setReporterKey(ConstantConfig.CRASH_IDENTIFIER);
        }


        crashBean.writeCrashReport();

        if (listener != null) {
            try {
                writeValueToFile(limitedString(listener.getUserID()), filename + ".user");
                writeValueToFile(limitedString(listener.getContact()), filename + ".contact");
                writeValueToFile(listener.getDescription(), filename + ".description");
            } catch (IOException e) {
                LogUtils.e(TAG, "Error saving crash meta data!"+e.toString());
            }
        }
    }

    private static void writeValueToFile(String value, String filename) throws IOException {
        if (TextUtils.isEmpty(value)) {
            return;
        }
        BufferedWriter writer = null;
        try {
            String path = ConstantConfig.FILES_PATH + "/" + filename;
            if (!TextUtils.isEmpty(value) && TextUtils.getTrimmedLength(value) > 0) {
                writer = new BufferedWriter(new FileWriter(path));
                writer.write(value);
                writer.flush();
            }
        } catch (IOException e) {
            // TODO: Handle exception here
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static String limitedString(String string) {
        if (!TextUtils.isEmpty(string) && string.length() > 255) {
            string = string.substring(0, 255);
        }
        return string;
    }

    /**
     * Save java exception(s) caught by HockeySDK-Xamarin to disk.
     *
     * @param exception              The native java exception to save.
     * @param managedExceptionString String representation of the full exception including the managed exception.
     * @param thread                 Thread that crashed.
     * @param listener               Custom CrashManager listener instance.
     */
    @SuppressWarnings("unused")
    public static void saveNativeException(Throwable exception, String managedExceptionString, Thread thread, CrashManagerListener listener) {
        // the throwable will a "native" Java exception. In this case managedExceptionString contains the full, "unconverted" exception
        // which contains information about the managed exception, too. We don't want to loose that part. Sadly, passing a managed
        // exception as an additional throwable strips that info, so we pass in the full managed exception as a string
        // and extract the first part that contains the info about the managed code that was calling the java code.
        // In case there is no managedExceptionString, we just forward the java exception
        if (!TextUtils.isEmpty(managedExceptionString)) {
            String[] splits = managedExceptionString.split("--- End of managed exception stack trace ---", 2);
            if (splits != null && splits.length > 0) {
                managedExceptionString = splits[0];
            }
        }

        saveXamarinException(exception, thread, managedExceptionString, false, listener);
    }

    private static void saveXamarinException(Throwable exception, Thread thread, String additionalManagedException, Boolean isManagedException, CrashManagerListener listener) {
        final Date startDate = new Date(CrashManager.getInitializeTimestamp());
        String filename = UUID.randomUUID().toString();
        final Date now = new Date();

        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        if (exception != null) {
            exception.printStackTrace(printWriter);
        }

        CrashBean crashBean = new CrashBean(filename, exception, additionalManagedException, isManagedException);
        crashBean.setAppPackage(ConstantConfig.APP_PACKAGE);
        crashBean.setAppVersionCode(ConstantConfig.APP_VERSION);
        crashBean.setAppVersionName(ConstantConfig.APP_VERSION_NAME);
        crashBean.setAppStartDate(startDate);
        crashBean.setAppCrashDate(now);

        if ((listener == null) || (listener.includeDeviceData())) {
            crashBean.setOsVersion(ConstantConfig.ANDROID_VERSION);
            crashBean.setOsBuild(ConstantConfig.ANDROID_BUILD);
            crashBean.setDeviceManufacturer(ConstantConfig.PHONE_MANUFACTURER);
            crashBean.setDeviceModel(ConstantConfig.PHONE_MODEL);
        }

        if (thread != null && ((listener == null) || (listener.includeThreadDetails()))) {
            crashBean.setThreadName(thread.getName() + "-" + thread.getId());
        }

        if (ConstantConfig.CRASH_IDENTIFIER != null && (listener == null || listener.includeDeviceIdentifier())) {
            crashBean.setReporterKey(ConstantConfig.CRASH_IDENTIFIER);
        }

        crashBean.writeCrashReport();

        if (listener != null) {
            try {
                writeValueToFile(limitedString(listener.getUserID()), filename + ".user");
                writeValueToFile(limitedString(listener.getContact()), filename + ".contact");
                writeValueToFile(listener.getDescription(), filename + ".description");
            } catch (IOException e) {
                LogUtils.e(TAG,"Error saving crash meta data!"+e.toString());
            }

        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable exception) {
        if (ConstantConfig.FILES_PATH == null) {
            // If the files path is null, the exception can't be stored
            // Always call the default handler instead
            mDefaultExceptionHandler.uncaughtException(thread, exception);
        } else {
            saveException(exception, thread, mCrashManagerListener);

            if (!mIgnoreDefaultHandler) {
                mDefaultExceptionHandler.uncaughtException(thread, exception);
            } else {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(10);
            }
        }
    }
}
