package qiniu.predem.android.block;

import android.content.Context;
import android.content.Intent;
//import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Printer;


/**
 * Created by Misty on 17/7/6.
 */

public class BlockPrinter implements Printer {
    public static final String ACTION_BLOCK = "qiniu.predem.android.block";
    public static final String ACTION_ANR = "qiniu.predem.android.anr";
    //    public static final String ACTION_ANR="android.intent.action.ANR";
    public static final String EXTRA_START_TIME = "block_start_time";
    public static final String EXTRA_FINISH_TIME = "block_end_time";
    private final static String TAG = "BlockPrintter";
    private static final String START_TAG = ">>>>> Dispatching to";
    private static final String FINISH_TAG = "<<<<< Finished to";

    private static final int START = 0;
    private static final int FINISH = 1;
    private static final int UNKONW = 2;

    private boolean mStartedPrinting = false;
    private Context mContext;
    private long mStartTimeMillis;
    private long mBlockThresholdMillis = 1000;

    public BlockPrinter(Context context) {
        this.mContext = context;
    }

    @Override
    public void println(String s) {
        switch (isStart(s)) {
            case START:
                mStartTimeMillis = System.currentTimeMillis();
                mStartedPrinting = true;
                break;
            case FINISH:
                long endTime = System.currentTimeMillis();
                mStartedPrinting = false;
                if (isBlock(endTime)) {
                    notifyBlockEvent(endTime, mStartTimeMillis);
                }
                break;
            default:
        }
    }

    private int isStart(String x) {
        if (!TextUtils.isEmpty(x)) {
            if (x.startsWith(START_TAG)) {
                return START;
            } else if (x.startsWith(FINISH_TAG)) {
                return FINISH;
            }
        }
        return UNKONW;
    }

    private boolean isBlock(long endTime) {
        return (endTime - mStartTimeMillis) > mBlockThresholdMillis;// && (endTime - mStartTimeMillis) < mANRThresholdMillis;
    }

    private void notifyBlockEvent(long endTime, long startTime) {
//        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
        Intent intent = new Intent();
        intent.setAction(ACTION_BLOCK);
        intent.putExtra(EXTRA_START_TIME, startTime);
        intent.putExtra(EXTRA_FINISH_TIME, endTime);
//        manager.sendBroadcast(intent);
    }
}

