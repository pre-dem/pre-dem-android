package qiniu.predem.android.http;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;
import qiniu.predem.android.bean.LogBean;
import qiniu.predem.android.util.FileUtil;

/**
 * Created by Misty on 17/6/15.
 */

public class ProbeResponse3 extends ResponseBody {
    private static final String TAG = "ProbeResponse3";
    private static final List<ProbeResponse3> mPool = new LinkedList<>();
    private ResponseBody responseBody;
    private BufferedSource bufferedSource;
    private LogBean record;

    private ProbeResponse3(ResponseBody responseBody, LogBean record) {
        this.responseBody = responseBody;
        this.record = record;
    }

    public static ProbeResponse3 obtain(ResponseBody responseBody, LogBean record) {
        if (mPool.size() > 0) {
            synchronized (mPool) {
                if (mPool.size() > 0) {
                    ProbeResponse3 obj = mPool.get(0);
                    mPool.remove(0);
                    obj.init(responseBody, record);
                    return obj;
                }
            }
        }
        return new ProbeResponse3(responseBody, record);
    }

    protected void init(ResponseBody responseBody, LogBean record) {
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
                if (bytesRead == -1) {
                    // TODO: 17/6/13
                    record.setEndTimestamp(System.currentTimeMillis());
                    FileUtil.getInstance().addReportContent(record.toString());
                } else {
                    totalBytes += bytesRead;
                    record.setDataLength(totalBytes);
                }
                return bytesRead;
            }
        };
    }
}
