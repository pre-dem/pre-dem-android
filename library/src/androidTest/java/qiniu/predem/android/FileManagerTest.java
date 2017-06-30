package qiniu.predem.android;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import qiniu.predem.android.util.FileUtil;

/**
 * Created by Misty on 5/24/17.
 */
@RunWith(AndroidJUnit4.class)
public class FileManagerTest extends AndroidTestCase {
    private static final String TAG = "FileManagerTest";

    private FileUtil fileManager;

    @Before
    public void start() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        fileManager = FileUtil.getInstance();
        fileManager.initialize(appContext.getApplicationContext());
    }

    @Test
    public void writeFileTest() {
        String record = "1\\t-\\t-\\t-\\n-\\tnull\\twww.baidu.com\\t\\nGET\\t115.239.211.112\\t200\\t1496648256522\\n1496648256641\\t1496648256753\\t114\\t2924\\n-1\\t-\\t";
        fileManager.addReportContent(record);
    }

    @Test
    public void readFileTest() {
        String content = fileManager.getReportContent();
//        Assert.assertNotNull(content);
    }
}
