package qiniu.predem.android.diagnosis;

import com.qiniu.android.netdiag.Output;

import qiniu.predem.android.util.LogUtils;

/**
 * Created by Misty on 2017/8/7.
 */

public class DiagnosisLogger implements Output {

    @Override
    public void write(String line) {
        LogUtils.i(line);
    }
}
