package qiniu.predem.android.util;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Misty on 17/6/15.
 */

public class AsyncRun {
    private static final String TAG = "AsyncRun";

    private static final int Max = 64 * 1024;

    public static String httpGetString(String url) throws IOException {
        byte[] b = httpGet(url);
        if (b == null) {
            return null;
        }
        return new String(b);
    }

    public static byte[] httpGet(String url) throws IOException {
        URL u = new URL(url);
        HttpURLConnection httpConn = (HttpURLConnection) u.openConnection();
        httpConn.setConnectTimeout(10000);
        httpConn.setReadTimeout(20000);
        int responseCode = httpConn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            return null;
        }

        int length = httpConn.getContentLength();
        if (length < 0) {
            length = Max;
        }
        if (length > Max) {
            return null;
        }
        InputStream is = httpConn.getInputStream();
        byte[] data = new byte[Max];
        int read = is.read(data);
        is.close();
        if (read <= 0) {
            return null;
        }
        if (read < data.length) {
            byte[] b = new byte[read];
            System.arraycopy(data, 0, b, 0, read);
            return b;
        }
        return data;
    }

    public static void runInMain(Runnable r) {
        Handler h = new Handler(Looper.getMainLooper());
        h.post(r);
    }

    public static void runInBack(Runnable r) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            AsyncTask.execute(r);
        } else {
            new Thread(r).start();
        }
    }
}
