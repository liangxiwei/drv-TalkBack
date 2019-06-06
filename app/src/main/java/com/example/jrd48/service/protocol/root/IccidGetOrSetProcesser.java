package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by Administrator on 2016/12/5.
 */

public class IccidGetOrSetProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.IccidGetOrSetProcesser";

    public IccidGetOrSetProcesser(Context context) {
        super(context);
    }
    @Override
    public void onGot(byte[] data) {
        Log.i("chat", "获得Iccic状态 ");
        try {
            ProtoMessage.MsgIccid resp = ProtoMessage.MsgIccid.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            Intent i = new Intent(ACTION);
            try {
                if (resp == null) {
                    throw new Exception("unknown response.");
                }
                i.putExtra("error_code", resp.getErrorCode());
            } catch (Exception e) {
                e.printStackTrace();
                i.putExtra("error_code", ProtoMessage.ErrorCode.UNKNOWN_VALUE);
            }
            context.sendBroadcast(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSent() {

    }
}
