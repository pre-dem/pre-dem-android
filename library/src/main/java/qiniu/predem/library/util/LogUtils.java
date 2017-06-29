package qiniu.predem.library.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by Misty on 5/18/17.
 */

public class LogUtils {
    /** 控制打印级别 在level级别之上才可以被打印出来 */
    public static int LEVEL = 1;
    /** 打印级别为V，对应Log.v*/
    public final static int V = 1;
    /** 打印级别为W，对应Log.w*/
    public final static int W = 2;
    /** 打印级别为I，对应Log.i*/
    public final static int I = 3;
    /** 打印级别为D，对应Log.d*/
    public final static int D = 4;
    /** 打印级别为E，对应Log.e*/
    public final static int E = 5;
    /** 最高级别打印，强制性打印，LEVEL无法关闭。 */
    private final static int P = Integer.MAX_VALUE;
    /** 打印修饰符号*/
    private static final String _L = "[";
    private static final String _R = "]";
    /** 是否同步输出到本地日志文件 */
    private static boolean IS_SYNS = false;
    /** 打印日志保存路径*/
    private static String LOG_FILE_DIR = "";
    /** 生成一个日期文件名格式的日式格式对象*/
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
    /** 是否新建一个日志文件。*/
    private static boolean IF_START_NEWLOG = true;
    /** 保存创建的文件路径 */
    private static String CURRENT_LOG_NAME = "";
    /** 针对天数级别的。如果当天已经存在一个LOG了，而使用者需要新开一个LOG，那么将计数 */
    private static int FILE_LOG_COUNT = 0;
    /** 单个日志的最大的容量,如果一个日志太大了，打开会影响效率*/
    private static int LOG_MAX_SIZE = 6 * 1024 * 1024;
    /** 检测文件目的地址是否正确 */
    private static Pattern pattern = Pattern.compile("(\\w+/)+");
    /** Debug模式，在Debug模式下才打印日志*/
    private static boolean DEBUG_MODE = true;

    /**
     * 动态关闭日志, 默认打开(关闭打印输出和文件输出)
     * <p>Title: closeLog0
     * <p>Description:
     */
    public static final void closeLog(){
        LogUtils.LEVEL = 10;
    }

    /**
     * 动态打开日志
     * <p>Title: openLog
     * <p>Description:
     */
    public static final void openLog(){
        LogUtils.LEVEL = 1;
    }

    /** 设置是否同步记录信息或者异常到日志文件。*/
    public static final void setSyns(boolean flag) {
        synchronized (LogUtils.class){
            IS_SYNS = flag;
        }
    }
    /** 开启一个新的LOG */
    public static final void startNewLog() {
        IF_START_NEWLOG = true;
    }
    /**
     * 打印信息
     * @param message
     */
    public static final void i(String message) {
        if (LEVEL <= I ) {
            if (DEBUG_MODE) {
                Log.i(getTag(message), message);
            }
            if (IS_SYNS) {
                LogFile.writeLog(message);
            }
        }
    }

    public static final void i(Exception exp) {
        if (LEVEL <= I) {
            if (DEBUG_MODE) {
                Log.i(getTag(exp),getMessage(exp));
            }
            if (IS_SYNS) {
                LogFile.writeLog(exp);
            }
        }
    }

    /**
     * 打印信息
     * @param message
     */
    public static final void i(String tag, String message) {
        if (LEVEL <= I ) {
            if (DEBUG_MODE) {
                Log.i(tag, message);
            }
            if (IS_SYNS) {
                LogFile.writeLog(message);
            }
        }
    }

    public static final void e(String message) {
        if (LEVEL <= E) {
            if (DEBUG_MODE) {
                Log.e(getTag(message), message);
            }
            if (IS_SYNS) {
                LogFile.writeLog(message);
            }
        }
    }

    public static final void e(Exception exp) {
        if (LEVEL <= E) {
            if (DEBUG_MODE) {
                Log.e(getTag(exp),getMessage(exp));
            }
            if (IS_SYNS) {
                LogFile.writeLog(exp);
            }
        }
    }

    public static final void e(String tag, String message) {
        if (LEVEL <= E) {
            if (DEBUG_MODE) {
                Log.e(tag, message);
            }
            if (IS_SYNS) {
                LogFile.writeLog(message);
            }
        }
    }

    public static final void w(String message) {
        if (LEVEL <= W) {
            if (DEBUG_MODE) {
                Log.w(getTag(message), message);
            }
            if (IS_SYNS) {
                LogFile.writeLog(message);
            }
        }
    }

    public static final void w(Exception exp) {
        if (LEVEL <= W) {
            if (DEBUG_MODE) {
                Log.w(getTag(exp),getMessage(exp));
            }
            if (IS_SYNS) {
                LogFile.writeLog(exp);
            }
        }
    }

    public static final void w(String tag, String message) {
        if (LEVEL <= W) {
            if (DEBUG_MODE) {
                Log.w(tag, message);
            }
            if (IS_SYNS) {
                LogFile.writeLog(message);
            }
        }
    }

    public static final void v(String message) {
        if (LEVEL <= V) {
            if (DEBUG_MODE) {
                Log.v(getTag(message), message);
            }
            if (IS_SYNS) {
                LogFile.writeLog(message);
            }
        }
    }

    public static final void v(Exception exp) {
        if (LEVEL <= V) {
            if (DEBUG_MODE) {
                Log.v(getTag(exp),getMessage(exp));
            }
            if (IS_SYNS) {
                LogFile.writeLog(exp);
            }
        }
    }

    public static final void v(String tag, String message) {
        if (LEVEL <= V) {
            if (DEBUG_MODE) {
                Log.v(tag, message);
            }
            if (IS_SYNS) {
                LogFile.writeLog(message);
            }
        }
    }

    public static final void d(String message) {
        if (LEVEL <= D) {
            if (DEBUG_MODE) {
                Log.d(getTag(message), message);
            }
            if (IS_SYNS) {
                LogFile.writeLog(message);
            }
        }
    }

    public static final void d(Exception exp) {
        if (LEVEL <= D) {
            if (DEBUG_MODE) {
                Log.d(getTag(exp),getMessage(exp));
            }
            if (IS_SYNS) {
                LogFile.writeLog(exp);
            }
        }
    }

    public static final void d(String tag, String message) {
        if (LEVEL <= D) {
            if (DEBUG_MODE) {
                Log.d(tag, message);
            }
            if (IS_SYNS) {
                LogFile.writeLog(message);
            }
        }
    }


    /**
     * 强制打印信息
     * @param message
     */
    public static final void print(String message) {
        if (LEVEL <= P) {
            if (DEBUG_MODE) {
                Log.e(getTag(message), message);
            }
            if (IS_SYNS){
                LogFile.writeLog(message);
            }
        }
    }
    /**
     * 强制打印异常
     * @param exp
     */
    public static final void print(Exception exp) {
        if (LEVEL <= P) {
            if (DEBUG_MODE) {
                Log.e(getTag(exp), getMessage(exp));
            }
            if (IS_SYNS){
                LogFile.writeLog(exp);
            }
        }
    }
    /** 获取一个Tag打印标签
     * @param msg
     * @return
     * @since JDK 1.5
     */
    private static String getTag(String msg) {
        if (msg != null) {
            //since jdk 1.5
            if (Thread.currentThread().getStackTrace().length > 0) {
                String name = Thread.currentThread().getStackTrace()[0].getClassName();
                return _L + name.substring(name.lastIndexOf(".") + 1) + _R;
            }
        }
        return _L + "null" + _R;
    }
    /**
     * 跟据变量获取一个打印的标签。
     * @param exp
     * @return
     */
    private static String getTag(Exception exp) {
        if (exp != null) {
            if (exp.getStackTrace().length > 0) {
                String name = exp.getStackTrace()[0].getClassName();
                return _L + name.substring(name.lastIndexOf(".") + 1) + _R;
            }
            return _L + "exception" + _R;
        }
        return _L + "null" + _R;
    }
    /**
     * 获取Exception的简便异常信息
     * @param exp
     * @return
     */
    private static String getMessage(Exception exp) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] element =  exp.getStackTrace();
        int n = 0;
        sb.append("\n");
        sb.append(exp.toString());
        sb.append("\n");
        for (StackTraceElement e : element) {
            sb.append(e.getClassName());
            sb.append(".");
            sb.append(e.getMethodName());
            sb.append("[");
            sb.append(e.getLineNumber());
            sb.append("]");
            sb.append("\n");
            n++;
            if (n >= 2) break;
        }
        if (exp.getCause() != null) {
            sb.append("Caused by: ");
            sb.append(exp.getMessage());
        }
        return sb.toString();
    }

    /** 自定义保存文件路径，如果是多重路径，请以xxx/xxx/xxx 形式的‘文件夹’
     *  @param  path : 文件夹名称
     *  * [将自动以当前时间为文件名拼接成完整路径。请慎用]
     *  */
    public final static void setLogFilePath(String path) {
        String url = SDcardUtil.getPath() + File.separator + path;
        boolean flag = pattern.matcher(url).matches();
        if (flag) {
            LOG_FILE_DIR = url;
        } else {
            LogFile.print("the url is not match file`s dir");
        }
    }

    /** 设置默认路径，以包名为格式的文件夹*/
    public final static void setDefaultFilePath(Context context) {
        String pkName = context.getPackageName().replaceAll("\\.", "\\/");
        setLogFilePath(pkName);
    }
    /** 获取时间字符串 */
    private static String getCurrTimeDir() {
        return sdf.format(new Date());
    }

    /** LOG定制类。
     *  输出LOG到日志。
     */
    private static class LogFile {
        /** 内部强制性打印使用。区分print ， 是为了解决无限循环打印exception*/
        private static void print(String msg) {
            if (LEVEL <= P) {
                Log.e(getTag(msg), msg);
            }
        }
        /**
         * 打印信息
         * @param message
         */
        public static synchronized void writeLog(String message) {
            File f = getFile();
            if (f != null) {
                try {
                    FileWriter fw = new FileWriter(f , true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.append("\n");
                    bw.append(message);
                    bw.append("\n");
                    bw.flush();
                    bw.close();
                    fw.close();
                } catch (IOException e) {
                    print("writeLog error, " + e.getMessage());
                }
            } else {
                print("writeLog error, due to the file dir is error");
            }
        }
        /**
         * 打印异常
         * @param exp
         */
        public static synchronized void writeLog(Exception exp) {
            File f = getFile();
            if (f != null) {
                try {
                    FileWriter fw = new FileWriter(f , true);
                    PrintWriter pw = new PrintWriter(fw);
                    pw.append("\n");
                    exp.printStackTrace(pw);
                    pw.flush();
                    pw.close();
                    fw.close();
                } catch (IOException e) {
                    print("writeLog error, " + e.getMessage());
                }
            } else {
                print("writeLog error, due to the file dir is error");
            }
        }
        /**
         * 获取文件
         * @return
         */
        private static final File getFile() {
            if ("".equals(LOG_FILE_DIR)) {
                return null;
            }
            synchronized (LogUtils.class) {
                if (!IF_START_NEWLOG) {
                    File currFile = new File(CURRENT_LOG_NAME);
                    if (currFile.length() >= LOG_MAX_SIZE) {
                        IF_START_NEWLOG = true;
                        return getFile();
                    }
                    return currFile;
                }
                File f = new File(LOG_FILE_DIR);
                if (!f.exists()) {
                    f.mkdirs();
                }
                File file = new File(f.getAbsolutePath() + File.separator + getCurrTimeDir() + ".log");
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                        FILE_LOG_COUNT = 0;
                        IF_START_NEWLOG = false;
                        CURRENT_LOG_NAME = file.getAbsolutePath();
                    } catch (IOException e) {
                        print("createFile error , " + e.getMessage());
                    }
                } else {
                    //已经存在了
                    if (IF_START_NEWLOG) {
                        FILE_LOG_COUNT ++;
                        return new File(f.getAbsolutePath() + File.separator + getCurrTimeDir() + "_" + FILE_LOG_COUNT+ ".log");
                    }
                }
                return file;
            }
        }
    }

    /**
     * SD卡管理器
     */
    private static class SDcardUtil {
        /** 获取Path*/
        public static String getPath() {
            if (isMounted()) {
                return Environment.getExternalStorageDirectory().getPath();
            }
            LogFile.print("please check if sd card is not mounted");
            return "";
        }
        /** 判断SD卡是否mounted*/
        @SuppressLint("NewApi")
        public static boolean isMounted() {
            return Environment.isExternalStorageEmulated();
        }

    }
}
