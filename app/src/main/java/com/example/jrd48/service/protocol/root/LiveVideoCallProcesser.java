package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by Administrator on 2017/3/15.
 */

public class LiveVideoCallProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.LiveVideoCallProcesser";

    public LiveVideoCallProcesser(Context context) {
        super(context);
    }
    @Override
    public void onGot(byte[] data) {
        try {
            ProtoMessage.StartVoiceMsg resp = ProtoMessage.StartVoiceMsg.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            Log.e("VideoCall",resp.toString());
            Intent i = new Intent(ACTION);
            i.putExtra("error_code", resp.getErrorCode());
            context.sendBroadcast(i);

            // TODO: 0 OK，其他值，失败
            //
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSent() {

    }
}
