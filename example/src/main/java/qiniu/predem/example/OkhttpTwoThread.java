package qiniu.predem.example;

import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by Misty on 17/6/19.
 */

public class OkhttpTwoThread {
    private static final String TAG = "OkhttpTwoThread";

    private static OkHttpClient client;

    public OkhttpTwoThread() {
        client = new OkHttpClient();
    }

    public static void run(String url) throws Exception {
        //创建okHttpClient对象
        final Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
            }

            @Override
            public void onResponse(final Response response) throws IOException {
<<<<<<< HEAD
                Log.d(TAG,"------okhttp2 : " + response.code());
=======
                LogUtils.d(TAG, "------okhttp2 : " + response.code());
>>>>>>> 320c3d1c9a424e63d93b85b6bf75762fd0ec52cb
                String str = response.body().string();
//                LogUtils.d(TAG,"------" + str);
            }
        });
    }
}
