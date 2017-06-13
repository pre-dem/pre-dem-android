package qiniu.presniff.library.telemetry;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import qiniu.presniff.library.telemetry.output.Output;
import qiniu.presniff.library.telemetry.output.Task;
import qiniu.presniff.library.util.AsyncRun;

/**
 * Created by Misty on 17/6/7.
 */

public class HttpPing implements Task {
    private static final String TAG = "HttpPing";

    private static final int MAX = 64 * 1024;

//    private final Output out;
    private final String url;
    private final Callback complete;
    private volatile boolean stopped;

    private HttpPing(String url, final Callback complete) {
//        this.out = out;
        this.url = url;
        this.complete = complete;
        this.stopped = false;
    }

    public static Task start(String url, Callback complete) {
        final HttpPing h = new HttpPing(url, complete);
        AsyncRun.runInBack(new Runnable() {
            @Override
            public void run() {
                h.run();
            }
        });
        return h;
    }

    private void run() {
        long start = System.currentTimeMillis();
        try {
            URL u = new URL(url);
            HttpURLConnection httpConn = (HttpURLConnection) u.openConnection();
            httpConn.setConnectTimeout(10000);
            httpConn.setReadTimeout(20000);
            int responseCode = httpConn.getResponseCode();

            Map<String, List<String>> headers = httpConn.getHeaderFields();
            InputStream is = httpConn.getInputStream();
            int len = httpConn.getContentLength();
            len = len > MAX || len < 0 ? MAX : len;
            byte[] data = new byte[len];
            int read = is.read(data);
            long duration = System.currentTimeMillis() - start;
            is.close();
            if (read <= 0) {
                Result r = new Result(responseCode, headers, null, (int) duration, "no body");
                this.complete.complete(r);
                return;
            }
            if (read < data.length) {
                byte[] b = new byte[read];
                System.arraycopy(data, 0, b, 0, read);
                Result r = new Result(responseCode, headers, b, (int) duration, "no body");
                this.complete.complete(r);

            }
        } catch (IOException e) {
            e.printStackTrace();
            long duration = System.currentTimeMillis() - start;
            Result r = new Result(-1, null, null, (int) duration, e.getMessage());
            this.complete.complete(r);
        }
    }

    @Override
    public void stop() {
        stopped = true;
    }

    public interface Callback {
        void complete(Result result);
    }

    public static class Result {
        public final int code;
        public final Map<String, List<String>> headers;
        public final byte[] body;
        public final int duration;
        public final String errorMessage;

        private Result(int code,
                       Map<String, List<String>> headers, byte[] body, int duration, String errorMessage) {
            this.code = code;
            this.headers = headers;
            this.body = body;
            this.duration = duration;
            this.errorMessage = errorMessage;
        }
    }
}
