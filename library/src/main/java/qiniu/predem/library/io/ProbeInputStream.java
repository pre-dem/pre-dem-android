package qiniu.predem.library.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import qiniu.predem.library.bean.LogBean;
import qiniu.predem.library.config.StreamConfig;
import qiniu.predem.library.file.LogFileManager;

/**
 * Created by Misty on 5/18/17.
 */

public class ProbeInputStream extends InputStream {
    private static final String TAG = "ProbeInputStream";

//    protected static final ExecutorService executor = Executors.newFixedThreadPool(2);

    protected long timeout;
//    protected boolean isFirstPacket;
    protected boolean replied;
    protected AtomicBoolean isFinish;
    protected Runnable runTimeOut;

    protected InputStream source;
    protected LogBean record;

//    protected boolean doReport = true;

    private static final List<ProbeInputStream> mPool = new LinkedList<>();

    public static ProbeInputStream obtain(InputStream is, LogBean record) {
        if (mPool.size() > 0) {
            synchronized (mPool) {
                if (mPool.size() > 0) {
                    ProbeInputStream obj = mPool.get(0);
                    mPool.remove(0);
                    obj.init(is, record, StreamConfig.DEFAULT_TIMEOUT);
                    return obj;
                }
            }
        }
        return new ProbeInputStream(is, record, StreamConfig.DEFAULT_TIMEOUT);
    }

    public void release() {
        synchronized (mPool) {
            this.source = null;
            if (mPool.size() < 256) mPool.add(this);
        }
    }

    protected ProbeInputStream(InputStream is, LogBean record) {
        this(is, record, StreamConfig.DEFAULT_TIMEOUT);
    }

    protected ProbeInputStream(InputStream is, final LogBean record, long timeout) {
        init(is, record, timeout);

        runTimeOut = new Runnable() {
            @Override
            public void run() {
                while (replied) {
                    replied = false;
                    try {
                        Thread.sleep(ProbeInputStream.this.timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (isFinish.compareAndSet(false, true)) {
                    //TODO 提交数据
                    record.setEndTimestamp(System.currentTimeMillis());
//                    LogUtils.d(TAG,"-----log info : " + record.toJsonString());
                    LogFileManager.getInstance().addReportContent(record.toString());
                }
            }
        };
    }

    protected void init(InputStream is, LogBean record, long timeout) {
//        this.doReport = true;
        this.timeout = timeout;
//        isFirstPacket = true;
        replied = false;
        isFinish = new AtomicBoolean(false);

        this.source = is;
        this.record = record;

    }

//    protected void checkFirstPacket() {
//        if (isFirstPacket) {
//            isFirstPacket = false;
//
//            replied = true;
//            executor.execute(runTimeOut);
//        }
//    }

    protected synchronized void checkFinish(long result) {
        if (!isFinish.get()) {
            if (result >= 0) {
                replied = true;
            } else {
                if (isFinish.compareAndSet(false, true)) {
                    //TODO 提交数据
                    record.setEndTimestamp(System.currentTimeMillis());
//                    LogUtils.d(TAG,"-----log info : " + record.toString());
                    LogFileManager.getInstance().addReportContent(record.toString());
                }
            }
        }
    }

    @Override
    public int read() throws IOException {
        int result = -1;
        try {
            result = source.read();
        } catch (IOException e) {
            // 提交数据
            LogFileManager.getInstance().addReportContent(record.toString());
            throw e;
        }
        record.setDataLength(record.getDataLength() + result);
        checkFinish(result);
        return result;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int result = -1;
        try {
            result = source.read(b);
        } catch (IOException e) {
            // 提交数据
            LogFileManager.getInstance().addReportContent(record.toString());
            throw e;
        }
        //获取content-length
        record.setDataLength(record.getDataLength() + result);
        checkFinish(result);
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = source.read(b, off, len);
        //获取content-length
        record.setDataLength(record.getDataLength() + result);
        checkFinish(result);
        return result;
    }

    public InputStream getSource() {
        return source;
    }

    @Override
    public int available() throws IOException {
        return source.available();
    }

    @Override
    public void close() throws IOException {
        source.close();
    }

    @Override
    public void mark(int readlimit) {
        source.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return source.markSupported();
    }

    @Override
    public synchronized void reset() throws IOException {
        source.reset();
    }

    @Override
    public long skip(long byteCount) throws IOException {
        return source.skip(byteCount);
    }

    @Override
    public boolean equals(Object o) {
        return source.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return source.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.release();
    }
}
