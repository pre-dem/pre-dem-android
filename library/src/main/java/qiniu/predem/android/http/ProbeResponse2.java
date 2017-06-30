package qiniu.predem.android.http;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;
import qiniu.predem.android.bean.LogBean;
import qiniu.predem.android.util.FileUtil;

/**
 * Created by Misty on 17/6/19.
 */

public class ProbeResponse2 extends ResponseBody {
    private static final String TAG = "ProbeResponse2";

    private ResponseBody responseBody;
    private BufferedSource bufferedSource;
    private LogBean record;

    private static final List<ProbeResponse2> mPool = new LinkedList<>();

    public static ProbeResponse2 obtain(ResponseBody responseBody, LogBean record) {
        if (mPool.size() > 0) {
            synchronized (mPool) {
                if (mPool.size() > 0) {
                    ProbeResponse2 obj = mPool.get(0);
                    mPool.remove(0);
                    obj.init(responseBody, record);
                    return obj;
                }
            }
        }
        return new ProbeResponse2(responseBody, record);
    }

    protected void init(ResponseBody responseBody, LogBean record) {
        this.responseBody = responseBody;
        this.record = record;

    }

    private ProbeResponse2(ResponseBody responseBody, LogBean record){
        this.responseBody = responseBody;
        this.record = record;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() throws IOException {
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
                    FileUtil.getInstance().addReportContent(record.toString());
                }else{
                    totalBytes += bytesRead;
                    record.setDataLength(totalBytes);
                }
                return bytesRead;
            }
        };
    }
}
