package qiniu.predem.library;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import qiniu.predem.library.crash.ExceptionHandler;

/**
 * Created by Misty on 5/24/17.
 */
@RunWith(AndroidJUnit4.class)
public class CrashReportTest extends AndroidTestCase {
    private static final String TAG = "CrashReportTest";
    private static final String APP_KEY = "9a9c127726b746e5b5fa7fc816a17407";

    @Before
    public void setUp(){
        CrashManager.register(InstrumentationRegistry.getTargetContext());
    }

    @Test
    public void testCrash(){
//        fakeCrashReport();
//        assertNotNull(ConstantConfig.FILES_PATH);
//        CrashManager.register(InstrumentationRegistry.getTargetContext(), APP_KEY);
//        Log.d(TAG,"------Crash Detail Info : " + CrashManager.getLastCrashDetails());
//        assertTrue(CrashManager.isDidCrashInLastSession());
//        assertNotNull(CrashManager.getLastCrashDetails());
    }

    private static void fakeCrashReport() {
        Throwable tr = new RuntimeException("Just a test exception");
        ExceptionHandler.saveException(tr, Thread.currentThread());
    }
}
