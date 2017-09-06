/*
 * **
 *   @(#)StackTraceInfo.java 2016-01-19
 *
 *  Copyright 2000-2016 by Koudai Corporation.
 *
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Koudai Corporation ("Confidential Information"). You
 *  shall not disclose such Confidential Information and shall use
 *  it only in accordance with the terms of the license agreement
 *  you entered into with Koudai.
 *
 * *
 */

package qiniu.predem.android.block;

import org.json.JSONObject;

/**
 * Created by fengcunhan on 16/1/19.
 */
public class TraceInfo {
    public long mStartTime;
    public long mEndTime;
    public String mLog;

    public String toJsonString() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("startTime", mStartTime);
            jsonObject.put("endTime", mEndTime);
            jsonObject.put("info", mLog);
            return jsonObject.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
