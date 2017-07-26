package qiniu.predem.android;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import qiniu.predem.android.util.ToolUtil;

/**
 * Created by Misty on 17/7/21.
 */
@RunWith(AndroidJUnit4.class)
public class ToolUtilTest {
    private static final String TAG = "ToolUtilTest";

    @Test
    public void testGetUUID(){
        Context appContext = InstrumentationRegistry.getTargetContext();
        String uuid = ToolUtil.generateUUID(appContext);
        System.out.println("-----uuid : " + uuid);
    }
}
