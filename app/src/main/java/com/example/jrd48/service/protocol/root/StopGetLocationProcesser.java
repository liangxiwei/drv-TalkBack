package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;

import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by Administrator on 2017/3/15 0015.
 */

public class StopGetLocationProcesser extends CommonProcesser {

    private final String TAG = "StopGetLocationProcesser";
    public final static String ACTION = "ACTION.StopGetLocationProcesser";

    public StopGetLocationProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(byte[] data) {
        try {
            ProtoMessage.CommonResp resp = ProtoMessage.CommonResp.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            Intent i = new Intent(ACTION);
            i.putExtra("error_code", resp.getErrorCode());
            context.sendBroadcast(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSent() {

    }
}
