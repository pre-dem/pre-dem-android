package qiniu.predem.android;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import qiniu.predem.android.config.Configuration;

/**
 * Created by Misty on 2017/11/1.
 */
@RunWith(AndroidJUnit4.class)
public class ConfigurationTest extends AndroidTestCase {
    private static final String TAG = "ConfigurationTest";

    @Test
    public void testBaseUrl(){
        Context appContext = InstrumentationRegistry.getTargetContext();
        Configuration.init(appContext,"appkeyappkey","domain.com");
        Log.d(TAG,"-----baseUrl : " + Configuration.getConfigUrl());
    }
}
