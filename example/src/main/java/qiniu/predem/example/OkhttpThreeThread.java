package qiniu.predem.example;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import qiniu.predem.android.util.LogUtils;

/**
 * Created by Misty on 17/6/19.
 */

public class OkhttpThreeThread {
    private static final String TAG = "OkhttpThreeThread";

    private static OkHttpClient client;

    public OkhttpThreeThread(){
        client = new OkHttpClient();
    }

    public static void run(String url) throws Exception{
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                LogUtils.d(TAG,"------"+response.code());
                String str = response.body().string();
//                if (!response.isSuccessful()) {
//                    throw new IOException("Unexpected code " + response);
//                }
//                Headers responseHeaders = response.headers();
//                String respheader = "";
//                for (int i = 0; i < responseHeaders.size(); i++) {
//                    respheader += (responseHeaders.name(i)+":"+responseHeaders.value(i) + "\n");
//                }
//
//                Headers reqeustHeaders = response.request().headers();
//                String reqheader = "";
//                for (int i = 0; i < reqeustHeaders.size(); i++) {
//                    reqheader += (reqeustHeaders.name(i)+":"+reqeustHeaders.value(i) + "\n");
//                }
//
//                String reqBody = response.request().body() == null ? "" : response.request().body().toString();
            }
        });
    }
}
