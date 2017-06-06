package qiniu.presniff.library;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import qiniu.presniff.library.util.ManifestUtil;


/**
 * Created by Misty on 17/6/5.
 */
@RunWith(AndroidJUnit4.class)
public class UtilsTest {
    private static final String TAG = "UtilsTest";

    @Test
    public void testManifestAppId(){
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        String appId = ManifestUtil.getAppId(appContext);
        Assert.assertEquals(appId, "9a9c127726b746e5b5fa7fc816a17407");
    }

    @Test
    public void testManifestAppKey(){
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        String appKey = ManifestUtil.getAppKey(appContext);
        Assert.assertEquals(appKey, "test");
    }
}
