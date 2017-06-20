package qiniu.predem.library;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Misty on 17/6/5.
 */
@RunWith(AndroidJUnit4.class)
public class ManagerTest {
    private static final String TAG = "ManagerTest";

    @Test
    public void testInit(){
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        DEMManager.init(appContext);

//        try {
//            Thread.sleep(4000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        Assert.assertNotNull(DEMManager.getApp());
    }
}
