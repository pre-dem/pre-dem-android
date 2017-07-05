package qiniu.predem.example;

import android.app.Application;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import qiniu.predem.android.anr.ANRWatchDog;
import qiniu.predem.android.bean.ANRBean;

/**
 * Created by Misty on 17/6/20.
 */

public class MyApplication extends Application {

    ANRWatchDog anrWatchDog = new ANRWatchDog(2000);

    @Override
    public void onCreate() {
        super.onCreate();
        anrWatchDog.setANRListener(new ANRWatchDog.ANRListener() {
            @Override
            public void onAppNotResponding(ANRBean error) {
                Log.e("ANR-Watchdog", "Detected Application Not Responding!");

                // Some tools like ACRA are serializing the exception, so we must make sure the exception serializes correctly
                try {
                    new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(error);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                Log.i("ANR-Watchdog", "Error was successfully serialized");

                throw error;
            }
        });

        anrWatchDog.start();
    }
}
