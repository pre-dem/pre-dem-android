package qiniu.predem.example;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Misty on 17/6/19.
 */

public class HttpUrlConnectionThread extends Thread {
    private static final String TAG = "HttpUrlConnectionThread";

    private String mUrl;
    private String mMethod;

    HttpUrlConnectionThread(String url, String method){
        this.mUrl = url;
        this.mMethod = method;
    }

    public HttpUrlConnectionThread setUrl(String url){
        this.mUrl = url;
        return this;
    }

    public HttpUrlConnectionThread setMethod(String method){
        this.mMethod = method;
        return this;
    }

    @Override
    public void run() {
        URL url;
        HttpURLConnection httpConnection = null;
        InputStream in = null;
        try {
            url = new URL(mUrl);
            URLConnection URLconnection = url.openConnection();
            httpConnection = (HttpURLConnection) URLconnection;
            httpConnection.setConnectTimeout(3000);
            if (mMethod != null && !mMethod.equals("")){
                httpConnection.setRequestMethod(mMethod);
            }
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d(TAG,"----success " + mUrl);
            } else {
                Log.d(TAG,"----failure " + mUrl);
            }
            in = httpConnection.getInputStream();
            byte[] data = getBytesByInputStream(in);
//            Log.d(TAG,"----response content : " + new String(data));
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpConnection != null){
                httpConnection.disconnect();
            }
            try {
                if (in != null){
                    in.close();
                }
            }catch (Exception e1){
                e1.printStackTrace();
            }
        }
    }

    //从InputStream中读取数据，转换成byte数组，最后关闭InputStream
    private byte[] getBytesByInputStream(InputStream is) {
        byte[] bytes = null;
        BufferedInputStream bis = new BufferedInputStream(is);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(baos);
        byte[] buffer = new byte[1024 * 8];
        int length = 0;
        try {
            while ((length = bis.read(buffer)) > 0) {
                bos.write(buffer, 0, length);
            }
            bos.flush();
            bytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bytes;
    }
}
