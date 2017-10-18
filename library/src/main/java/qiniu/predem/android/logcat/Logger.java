package qiniu.predem.android.logcat;

import android.content.Context;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * Created by Misty on 2017/9/19.
 */

public class Logger {
    /**
     * log's level
     */
    public final static int V = Log.VERBOSE;
    public final static int W = Log.WARN;
    public final static int I = Log.INFO;
    public final static int D = Log.DEBUG;
    public final static int E = Log.ERROR;
    private final static int P = Integer.MAX_VALUE;

    /**
     * print level
     */
    private static int LEVEL = 0;
    private static Context mContext;

    public static void openLogs(Context context, int level) {
        LEVEL = level;
        mContext = context;
        PrintLogger.getInstance(mContext).openLogs();
    }

    public static void closeLogs() {
        LEVEL = P;
        PrintLogger.getInstance(mContext).closeLogs();
    }

    public static void i(String tag, String msg) {
        Log.i(tag, msg);
        if (LEVEL < I) {
            PrintLogger.getInstance(mContext).Log(tag, msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        Log.i(tag, msg, tr);
        if (LEVEL < I) {
            PrintLogger.getInstance(mContext).Log(tag, getStackTraceString(msg, tr));
        }
    }

    public static void v(String tag, String msg) {
        Log.v(tag, msg);
        if (LEVEL < V) {
            PrintLogger.getInstance(mContext).Log(tag, msg);
        }
    }

    public static void v(String tag, String msg, Throwable tr) {
        Log.v(tag, msg, tr);
        if (LEVEL < V) {
            PrintLogger.getInstance(mContext).Log(tag, getStackTraceString(msg, tr));
        }
    }

    public static void d(String tag, String msg) {
        Log.d(tag, msg);
        if (LEVEL < D) {
            PrintLogger.getInstance(mContext).Log(tag, msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        Log.d(tag, msg, tr);
        if (LEVEL < D) {
            PrintLogger.getInstance(mContext).Log(tag, getStackTraceString(msg, tr));
        }
    }

    public static void w(String tag, String msg) {
        Log.w(tag, msg);
        if (LEVEL < W) {
            PrintLogger.getInstance(mContext).Log(tag, msg);
        }
    }

    public static void w(String tag, Throwable tr) {
        Log.w(tag, tr);
        if (LEVEL < W) {
            PrintLogger.getInstance(mContext).Log(tag, getStackTraceString("", tr));
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        Log.w(tag, msg, tr);
        if (LEVEL < W) {
            PrintLogger.getInstance(mContext).Log(tag, getStackTraceString(msg, tr));
        }
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
        if (LEVEL < E) {
            PrintLogger.getInstance(mContext).Log(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        Log.e(tag, msg, tr);
        if (LEVEL < E) {
            PrintLogger.getInstance(mContext).Log(tag, getStackTraceString(msg, tr));
        }
    }

    public static void wtf(String tag, String msg) {
        Log.wtf(tag, msg);
        if (LEVEL < W) {
            PrintLogger.getInstance(mContext).Log(tag, msg);
        }
    }

    public static void wtf(String tag, Throwable tr) {
        Log.wtf(tag, tr);
        if (LEVEL < W) {
            PrintLogger.getInstance(mContext).Log(tag, getStackTraceString("", tr));
        }
    }

    public static void wtf(String tag, String msg, Throwable tr) {
        Log.wtf(tag, msg, tr);
        if (LEVEL < W) {
            PrintLogger.getInstance(mContext).Log(tag, getStackTraceString(msg, tr));
        }
    }

    //获取Exception堆栈
    private static String getStackTraceString(String str, Throwable e) {
        //将Exception的错误信息转换成String
        String log = "";
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            log = str + "\r\n" + sw.toString() + "\r\n";
        } catch (Exception e2) {
            log = str + " fail to print Exception";
        }
        return log;
    }
}