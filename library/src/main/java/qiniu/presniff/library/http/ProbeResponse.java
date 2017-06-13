package qiniu.presniff.library.http;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;
import qiniu.presniff.library.bean.LogBean;
import qiniu.presniff.library.util.LogUtils;

/**
 * Created by Misty on 17/6/8.
 */

public class ProbeResponse extends ResponseBody {
    private static final String TAG = "ProbeResponse";

    private ResponseBody responseBody;
    private BufferedSource bufferedSource;
    private LogBean record;

    private static final List<ProbeResponse> mPool = new LinkedList<>();

    public static ProbeResponse obtain(ResponseBody responseBody, LogBean record) {
        if (mPool.size() > 0) {
            synchronized (mPool) {
                if (mPool.size() > 0) {
                    ProbeResponse obj = mPool.get(0);
                    mPool.remove(0);
                    obj.init(responseBody, record);
                    return obj;
                }
            }
        }
        return new ProbeResponse(responseBody, record);
    }

    protected void init(ResponseBody responseBody, LogBean record) {
        this.responseBody = responseBody;
        this.record = record;

    }

    private ProbeResponse(ResponseBody responseBody, LogBean record){
        this.responseBody = responseBody;
        this.record = record;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytes = 0L;
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                if (bytesRead == -1){
                    // TODO: 17/6/13
                    record.setEndTimestamp(System.currentTimeMillis());
                    LogUtils.d(TAG,"------info : " + record.toJsonString());
                }else{
                    totalBytes += bytesRead;
                    record.setDataLength(totalBytes);
                }
                return bytesRead;
            }
        };
    }
}
