package qiniu.predem.android.logcat;

import android.content.Context;

import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;

import qiniu.predem.android.config.FileConfig;

/**
 * Created by Misty on 2017/9/20.
 */

public class WriteThread extends Thread {
    private static final String TAG = "WriteThread";

    private boolean isRunning = false;
    private String filePath = null;
    private String indexFilePath = null;
    private Object lock = new Object();
    private ConcurrentLinkedQueue<String> mQueue = new ConcurrentLinkedQueue<String>();

    private Context mContext;

    public WriteThread(Context context) {
        filePath = FileConfig.FILES_PATH + File.separator + "dem_logcat_log.";
        indexFilePath = FileConfig.FILES_PATH + File.separator + "dem_logcat_index.json";
        isRunning = true;
        this.mContext = context;
    }

    //将需要写入文本的字符串添加到队列中.线程休眠时,再唤醒线程写入文件
    public void enqueue(String str) {
        mQueue.add(str);
        if (isRunning() == false) {
            awake();
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void awake() {
        synchronized (lock) {
            lock.notify();
        }
    }

    @Override
    public void run() {
        while (true) {
            synchronized (lock) {
                isRunning = true;
                while (!mQueue.isEmpty()) {
                    try {
                        //pop出队列的头字符串写入到文件中
                        recordStringLog(mQueue.poll());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                isRunning = false;
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void recordStringLog(String content) {
        //TODO 写文件
        PrintLogger.getInstance(mContext).addReportContent(content);
    }
}
