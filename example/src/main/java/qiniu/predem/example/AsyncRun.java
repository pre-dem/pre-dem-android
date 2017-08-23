package qiniu.predem.example;

import android.os.Handler;
import android.os.Looper;


/**
 * Created by Misty on 2017/8/8.
 */

public class AsyncRun {
    public static void runInMain(Runnable r) {
        Handler h = new Handler(Looper.getMainLooper());
        h.post(r);
    }

}
