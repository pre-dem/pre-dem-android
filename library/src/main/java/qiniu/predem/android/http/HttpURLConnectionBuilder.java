package qiniu.predem.android.http;

import android.annotation.SuppressLint;
import android.os.Build;
import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import qiniu.predem.android.bean.AppBean;

/**
 * Created by Misty on 17/6/15.
 */

public class HttpURLConnectionBuilder {
    private static final String TAG = "HttpURLConnectionBuilder";

    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final long FORM_FIELD_LIMIT = 4 * 1024 * 1024;
    private static final int DEFAULT_TIMEOUT = 2 * 60 * 1000;
    private final String mUrlString;
    private final Map<String, String> mHeaders;
    private String mRequestMethod;
    private String mRequestBody;
    private int mTimeout = DEFAULT_TIMEOUT;

    public HttpURLConnectionBuilder(String urlString) {
        mUrlString = urlString;
        mHeaders = new HashMap<>();
        mHeaders.put("User-Agent", AppBean.SDK_USER_AGENT);
    }

    private static String getFormString(Map<String, String> params, String charset) throws UnsupportedEncodingException {
        List<String> protoList = new ArrayList<String>();
        for (String key : params.keySet()) {
            String value = params.get(key);
            key = URLEncoder.encode(key, charset);
            value = URLEncoder.encode(value, charset);
            protoList.add(key + "=" + value);
        }
        return TextUtils.join("&", protoList);
    }

    public HttpURLConnectionBuilder setRequestMethod(String requestMethod) {
        mRequestMethod = requestMethod;
        return this;
    }

    public HttpURLConnectionBuilder writeFormFields(Map<String, String> fields) {

        for (String key : fields.keySet()) {
            String value = fields.get(key);
            if (value.length() > FORM_FIELD_LIMIT) {
                throw new IllegalArgumentException("Form field " + key + " size too large: " + value.length() + " - max allowed: " + FORM_FIELD_LIMIT);
            }
        }

        try {
            String formString = getFormString(fields, DEFAULT_CHARSET);
            setHeader("Content-Type", "application/x-www-form-urlencoded");
            setRequestBody(formString);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public HttpURLConnectionBuilder setRequestBody(String requestBody) {
        mRequestBody = requestBody;
        return this;
    }

    public HttpURLConnectionBuilder setHeader(String name, String value) {
        mHeaders.put(name, value);
        return this;
    }

    @SuppressLint("ObsoleteSdkInt")
    public HttpURLConnection build() throws IOException {
        HttpURLConnection connection;
        URL url = new URL(mUrlString);
        connection = (HttpURLConnection) url.openConnection();

        connection.setConnectTimeout(mTimeout);
        connection.setReadTimeout(mTimeout);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD) {
            connection.setRequestProperty("Connection", "close");
        }

        if (!TextUtils.isEmpty(mRequestMethod)) {
            connection.setRequestMethod(mRequestMethod);
            if (!TextUtils.isEmpty(mRequestBody) || mRequestMethod.equalsIgnoreCase("POST") || mRequestMethod.equalsIgnoreCase("PUT")) {
                connection.setDoOutput(true);
            }
        }

        for (String name : mHeaders.keySet()) {
            connection.setRequestProperty(name, mHeaders.get(name));
        }

        if (!TextUtils.isEmpty(mRequestBody)) {
            OutputStream outputStream = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, DEFAULT_CHARSET));
            writer.write(mRequestBody);
            writer.flush();
            writer.close();
        }

        return connection;
    }
}
