package qiniu.predem.android;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import qiniu.predem.android.util.ManifestUtil;


/**
 * Created by Misty on 17/6/5.
 */
@RunWith(AndroidJUnit4.class)
public class UtilsTest {
    private static final String TAG = "UtilsTest";

    @Test
    public void testManifestAppDomain(){
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        String appId = ManifestUtil.getDomain(appContext);
        Assert.assertEquals(appId, "hriygkee.bq.cloudappl.com");
    }

    @Test
    public void testManifestAppKey(){
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        String appKey = ManifestUtil.getAppKey(appContext);
        Assert.assertEquals(appKey, "test");
    }
}
