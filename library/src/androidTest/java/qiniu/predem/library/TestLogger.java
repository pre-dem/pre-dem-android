package qiniu.predem.library;

import qiniu.predem.library.diagnosis.Output;

/**
 * Created by Misty on 17/6/7.
 */

public class TestLogger implements Output {

    @Override
    public void write(String line) {
        System.out.println(line);
    }
}
