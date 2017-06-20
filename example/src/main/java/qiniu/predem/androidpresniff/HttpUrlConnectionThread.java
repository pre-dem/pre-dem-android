package qiniu.predem.androidpresniff;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import qiniu.predem.library.util.LogUtils;

import static android.R.attr.host;

/**
 * Created by Misty on 17/6/19.
 */

public class HttpUrlConnectionThread extends AsyncTask<String, Integer, Map<String, Object>> {
    private static final String TAG = "HttpUrlConnectionThread";

    //NETWORK_GET表示发送GET请求
    public static final String NETWORK_GET = "NETWORK_GET";
    //NETWORK_POST_KEY_VALUE表示用POST发送键值对数据
    public static final String NETWORK_POST_KEY_VALUE = "NETWORK_POST_KEY_VALUE";
    //NETWORK_POST_XML表示用POST发送XML数据
    public static final String NETWORK_POST_XML = "NETWORK_POST_XML";
    //NETWORK_POST_JSON表示用POST发送JSON数据
    public static final String NETWORK_POST_JSON = "NETWORK_POST_JSON";

    private String url;
    private int port;

    public HttpUrlConnectionThread setUrl(String url){
        this.url = url;
        return this;
    }

    public HttpUrlConnectionThread setPort(int port){
        this.port = port;
        return this;
    }

    @Override
    protected Map<String, Object> doInBackground(String... params) {
        Map<String,Object> result = new HashMap<>();
        URL url = null;//请求的URL地址
        HttpURLConnection conn = null;
        String requestHeader = null;//请求头
        byte[] requestBody = null;//请求体
        String responseHeader = null;//响应头
        byte[] responseBody = null;//响应体
        String action = params[0];//http请求的操作类型

        try {
            if (NETWORK_GET.equals(action)) {
                //发送GET请求
                url = new URL(this.url);
                conn = (HttpURLConnection) url.openConnection();
                //HttpURLConnection默认就是用GET发送请求，所以下面的setRequestMethod可以省略
                conn.setRequestMethod("GET");
                //HttpURLConnection默认也支持从服务端读取结果流，所以下面的setDoInput也可以省略
                conn.setDoInput(true);
                //用setRequestProperty方法设置一个自定义的请求头:action，由于后端判断
                conn.setRequestProperty("action", NETWORK_GET);

                //禁用网络缓存
                conn.setUseCaches(false);
                //获取请求头
                requestHeader = getReqeustHeader(conn);
                //在对各种参数配置完成后，通过调用connect方法建立TCP连接，但是并未真正获取数据
                //conn.connect()方法不必显式调用，当调用conn.getInputStream()方法时内部也会自动调用connect方法
                conn.connect();
                //调用getInputStream方法后，服务端才会收到请求，并阻塞式地接收服务端返回的数据
                InputStream is = conn.getInputStream();
                //将InputStream转换成byte数组,getBytesByInputStream会关闭输入流
                responseBody = getBytesByInputStream(is);
                //获取响应头
                responseHeader = getResponseHeader(conn);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //最后将conn断开连接
            if (conn != null) {
                conn.disconnect();
            }
        }

        result.put("url", url.toString());
        result.put("action", action);
        result.put("requestHeader", requestHeader);
        result.put("requestBody", requestBody);
        result.put("responseHeader", responseHeader);
        result.put("responseBody", responseBody);
        return result;
    }

    @Override
    protected void onPostExecute(Map<String, Object> result) {
        super.onPostExecute(result);
        String url = (String)result.get("url");//请求的URL地址
        String action = (String) result.get("action");//http请求的操作类型
        String requestHeader = (String) result.get("requestHeader");//请求头
        byte[] requestBody = (byte[]) result.get("requestBody");//请求体
        String responseHeader = (String) result.get("responseHeader");//响应头
        byte[] responseBody = (byte[]) result.get("responseBody");//响应体

        //更新tvUrl，显示Url
//        tvUrl.setText(url);

        //更新tvRequestHeader，显示请求头
        if (requestHeader != null) {
//            tvRequestHeader.setText(requestHeader);
        }

        //更新tvRequestBody，显示请求体
        if(requestBody != null){
            try{
                String request = new String(requestBody, "UTF-8");
//                tvRequestBody.setText(request);
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
        }

        //更新tvResponseHeader，显示响应头
        if (responseHeader != null) {
//            tvResponseHeader.setText(responseHeader);
        }

        //更新tvResponseBody，显示响应体
        if (NETWORK_GET.equals(action)) {
            String response = getStringByBytes(responseBody);
//            tvResponseBody.setText(response);
        }
    }

    //读取请求头
    private String getReqeustHeader(HttpURLConnection conn) {
        //https://github.com/square/okhttp/blob/master/okhttp-urlconnection/src/main/java/okhttp3/internal/huc/HttpURLConnectionImpl.java#L236
        Map<String, List<String>> requestHeaderMap = conn.getRequestProperties();
        Iterator<String> requestHeaderIterator = requestHeaderMap.keySet().iterator();
        StringBuilder sbRequestHeader = new StringBuilder();
        while (requestHeaderIterator.hasNext()) {
            String requestHeaderKey = requestHeaderIterator.next();
            String requestHeaderValue = conn.getRequestProperty(requestHeaderKey);
            sbRequestHeader.append(requestHeaderKey);
            sbRequestHeader.append(":");
            sbRequestHeader.append(requestHeaderValue);
            sbRequestHeader.append("\n");
        }
        return sbRequestHeader.toString();
    }

    //读取响应头
    private String getResponseHeader(HttpURLConnection conn) {
        Map<String, List<String>> responseHeaderMap = conn.getHeaderFields();
        int size = responseHeaderMap.size();
        StringBuilder sbResponseHeader = new StringBuilder();
        for(int i = 0; i < size; i++){
            String responseHeaderKey = conn.getHeaderFieldKey(i);
            String responseHeaderValue = conn.getHeaderField(i);
            sbResponseHeader.append(responseHeaderKey);
            sbResponseHeader.append(":");
            sbResponseHeader.append(responseHeaderValue);
            sbResponseHeader.append("\n");
        }
        return sbResponseHeader.toString();
    }

    //根据字节数组构建UTF-8字符串
    private String getStringByBytes(byte[] bytes) {
        String str = "";
        try {
            str = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
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
