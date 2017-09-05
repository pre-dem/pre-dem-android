package qiniu.predem.android.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;
import okio.Options;
import okio.Sink;
import okio.Timeout;
import qiniu.predem.android.bean.LogBean;
import qiniu.predem.android.config.Configuration;
import qiniu.predem.android.util.FileUtil;

/**
 * Created by Misty on 2017/8/16.
 */

public class ProbeBufferedSource implements BufferedSource {
    protected static final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final static String TAG = "ProbeBufferedSource";
    private static final List<ProbeBufferedSource> mPool = new LinkedList<>();
    protected boolean cancelSubmit = false;
    protected long timeout;
    protected boolean isFirstPacket;
    protected boolean replied;
    protected AtomicBoolean isFinish;
    protected Runnable runTimeOut;
    protected BufferedSource source;
    protected LogBean record;

    protected ProbeBufferedSource(BufferedSource bufferedSource, LogBean record) {
        this(bufferedSource, record, Configuration.DEFAULT_TIMEOUT);
    }

    protected ProbeBufferedSource(BufferedSource bufferedSource, LogBean record, long timeout) {
        init(bufferedSource, record, timeout);
    }

    public static ProbeBufferedSource obtain(BufferedSource source, LogBean record) {
        if (mPool.size() > 0) {
            synchronized (mPool) {
                if (mPool.size() > 0) {
                    ProbeBufferedSource obj = mPool.get(0);
                    mPool.remove(0);
                    obj.init(source, record, Configuration.DEFAULT_TIMEOUT);
                    return obj;
                }
            }
        }
        return new ProbeBufferedSource(source, record, Configuration.DEFAULT_TIMEOUT);
    }

    public void release() {
        synchronized (mPool) {
            this.source = null;
            if (mPool.size() < 256) mPool.add(this);
        }
    }

    protected void init(BufferedSource bufferedSource, final LogBean record, long timeout) {
        this.timeout = timeout;
        isFirstPacket = true;
        replied = false;
        isFinish = new AtomicBoolean(false);
        runTimeOut = new Runnable() {
            @Override
            public void run() {
                while (replied) {
                    replied = false;
                    try {
                        Thread.sleep(ProbeBufferedSource.this.timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (isFinish.compareAndSet(false, true)) {
                    if (!cancelSubmit) {
                        FileUtil.getInstance().addReportContent(record.toString());
                    }
                }
            }
        };

        this.source = bufferedSource;
        this.record = record;
    }

    protected void checkFirstPacket() {
        if (isFirstPacket) {
            isFirstPacket = false;
            record.setEndTimestamp(System.currentTimeMillis());
            replied = true;
            executor.execute(runTimeOut);
        }
    }

    protected void checkReadFinish(long result) {
        if (!isFinish.get()) {
            if (result >= 0) {
                replied = true;
                record.setEndTimestamp(System.currentTimeMillis());
            } else {
                cancelSubmit = true;
            }
        }
    }

    protected void onExceptionSubmit(Exception e) {
        if (cancelSubmit) {
            return;
        }
        FileUtil.getInstance().addReportContent(record.toString());
    }

    protected void updateSize() {
        record.setDataLength(record.getDataLength() + 1);
    }

    protected void updateSize(long size) {
        record.setDataLength(record.getDataLength() + size);
    }

    protected void updateSize(String str) {
        record.setDataLength(record.getDataLength() + str.length());
    }

    protected void updateSize(byte[] buf) {
        record.setDataLength(record.getDataLength() + buf.length);
    }

    protected void updateSize(ByteString buf) {
        record.setDataLength(record.getDataLength() + buf.size());
    }

    protected void updateSize(Buffer buf) {
        record.setDataLength(record.getDataLength() + buf.size());
    }

    @Override
    public Buffer buffer() {
        return source.buffer();
    }

    @Override
    public boolean exhausted() throws IOException {
        return source.exhausted();
    }

    @Override
    public void require(long byteCount) throws IOException {
        source.require(byteCount);
    }

    @Override
    public boolean request(long byteCount) throws IOException {
        return source.request(byteCount);
    }

    @Override
    public byte readByte() throws IOException {
        byte result;
        try {
            result = source.readByte();
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize();
        checkReadFinish(result);
        return result;
    }

    @Override
    public short readShort() throws IOException {
        short result;
        try {
            result = source.readShort();
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize();
        checkReadFinish(result);
        return result;
    }

    @Override
    public short readShortLe() throws IOException {
        short result;
        try {
            result = source.readShortLe();
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize();
        checkReadFinish(result);
        return result;
    }

    @Override
    public int readInt() throws IOException {
        int result;
        try {
            result = source.readInt();
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize();
        checkReadFinish(result);
        return result;
    }

    @Override
    public int readIntLe() throws IOException {
        int result;
        try {
            result = source.readIntLe();
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize();
        checkReadFinish(result);
        return result;
    }

    @Override
    public long readLong() throws IOException {
        long result;
        try {
            result = source.readLong();
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize();
        checkReadFinish(result);
        return result;
    }

    @Override
    public long readLongLe() throws IOException {
        long result;
        try {
            result = source.readLongLe();
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize();
        checkReadFinish(result);
        return result;
    }

    @Override
    public long readDecimalLong() throws IOException {
        long result;
        try {
            result = source.readDecimalLong();
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize();
        checkReadFinish(result);
        return result;
    }

    @Override
    public long readHexadecimalUnsignedLong() throws IOException {
        long result;
        try {
            result = source.readHexadecimalUnsignedLong();
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize();
        checkReadFinish(result);
        return result;
    }

    @Override
    public void skip(long byteCount) throws IOException {
        try {
            source.skip(byteCount);
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize(byteCount);
    }

    @Override
    public ByteString readByteString() throws IOException {
        ByteString result;
        try {
            result = source.readByteString();
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize(result);
        checkReadFinish(result.size());
        return result;
    }

    @Override
    public ByteString readByteString(long byteCount) throws IOException {
        ByteString result;
        try {
            result = source.readByteString(byteCount);
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize(result);
        checkReadFinish(result.size());
        return result;
    }

    @Override
    public int select(Options options) throws IOException {
        return source.select(options);
    }

    @Override
    public byte[] readByteArray() throws IOException {
        byte[] result;
        try {
            result = source.readByteArray();
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize(result);
        checkReadFinish(result.length);
        return result;
    }

    @Override
    public byte[] readByteArray(long byteCount) throws IOException {
        byte[] result;
        try {
            result = source.readByteArray(byteCount);
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize(result);
        checkReadFinish(result.length);
        return result;
    }

    @Override
    public int read(byte[] sink) throws IOException {
        int result;
        try {
            result = source.read(sink);
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize(result);
        checkReadFinish(result);
        return result;
    }

    @Override
    public void readFully(byte[] sink) throws IOException {
        try {
            source.readFully(sink);
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize(sink);
    }

    @Override
    public int read(byte[] sink, int offset, int byteCount) throws IOException {
        int result;
        try {
            result = source.read(sink, offset, byteCount);
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize(result);
        checkReadFinish(result);
        return result;
    }

    @Override
    public void readFully(Buffer sink, long byteCount) throws IOException {
        try {
            source.readFully(sink, byteCount);
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize(sink);
    }

    @Override
    public long readAll(Sink sink) throws IOException {
        long result;
        try {
            result = source.readAll(sink);
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize(result);
        checkReadFinish(result);
        return result;
    }

    @Override
    public String readUtf8() throws IOException {
        String result;
        try {
            result = source.readUtf8();
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize(result);
        checkReadFinish(result.length());
        return result;
    }

    @Override
    public String readUtf8(long byteCount) throws IOException {
        String result;
        try {
            result = source.readUtf8(byteCount);
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize(result);
        checkReadFinish(result.length());
        return result;
    }

    @Override
    public String readUtf8Line() throws IOException {
        String result;
        try {
            result = source.readUtf8Line();
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize(result);
        checkReadFinish(result.length());
        return result;
    }

    @Override
    public String readUtf8LineStrict() throws IOException {
        String result;
        try {
            result = source.readUtf8LineStrict();
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize(result);
        checkReadFinish(result.length());
        return result;
    }

    @Override
    public String readUtf8LineStrict(long limit) throws IOException {
        String result;
        try {
            result = source.readUtf8LineStrict(limit);
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize(result);
        checkReadFinish(result.length());
        return result;
    }

    @Override
    public int readUtf8CodePoint() throws IOException {
        int result;
        try {
            result = source.readUtf8CodePoint();
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize(result);
        checkReadFinish(result);
        return result;
    }

    @Override
    public String readString(Charset charset) throws IOException {
        String result;
        try {
            result = source.readString(charset);
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize(result);
        checkReadFinish(result.length());
        return result;
    }

    @Override
    public String readString(long byteCount, Charset charset) throws IOException {
        String result;
        try {
            result = source.readString(byteCount, charset);
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize(result);
        checkReadFinish(result.length());
        return result;
    }

    @Override
    public long indexOf(byte b) throws IOException {
        return source.indexOf(b);
    }

    @Override
    public long indexOf(byte b, long fromIndex) throws IOException {
        return source.indexOf(b, fromIndex);
    }

    @Override
    public long indexOf(ByteString bytes) throws IOException {
        return source.indexOf(bytes);
    }

    @Override
    public long indexOf(ByteString bytes, long fromIndex) throws IOException {
        return source.indexOf(bytes, fromIndex);
    }

    @Override
    public long indexOf(byte b, long fromIndex, long toIndex) throws IOException {
        return source.indexOf(b, fromIndex, toIndex);
    }

    @Override
    public long indexOfElement(ByteString targetBytes) throws IOException {
        return source.indexOfElement(targetBytes);
    }

    @Override
    public long indexOfElement(ByteString targetBytes, long fromIndex) throws IOException {
        return source.indexOfElement(targetBytes, fromIndex);
    }

    @Override
    public boolean rangeEquals(long offset, ByteString bytes) throws IOException {
        return source.rangeEquals(offset, bytes);
    }

    @Override
    public boolean rangeEquals(long offset, ByteString bytes, int bytesOffset, int byteCount) throws IOException {
        return source.rangeEquals(offset, bytes, bytesOffset, byteCount);
    }

    @Override
    public InputStream inputStream() {
        cancelSubmit = true;
        return new ProbeInputStream(source.inputStream(), record);
    }

    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        long result;
        try {
            result = source.read(sink, byteCount);
        } catch (IOException e) {
            onExceptionSubmit(e);
            throw e;
        }
        checkFirstPacket();
        updateSize(result);
        checkReadFinish(result);
        return result;
    }

    @Override
    public Timeout timeout() {
        return source.timeout();
    }

    @Override
    public void close() throws IOException {
        source.close();
    }

    public BufferedSource getSource() {
        return source;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.release();
    }

    @Override
    public boolean equals(Object o) {
        return source.equals(o);
    }

    @Override
    public int hashCode() {
        return source.hashCode();
    }

    @Override
    public String toString() {
        return source.toString();
    }
}
