package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by quhuabo on 2016/11/19 0019.
 */

public class DownloadProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.DownloadProcesser";

    public DownloadProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(byte[] data) {
        try {
            ProtoMessage.MsgAttachment resp = ProtoMessage.MsgAttachment.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            Log.i("pocdemo", "下载应答: " + resp.getErrorCode() + ", resp msg_id:" + resp.getMsgID());
            Intent i = new Intent(ACTION);

            i.putExtra("resp", resp);
            i.putExtra("error_code", resp.getErrorCode());
            context.sendBroadcast(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSent() {
        Log.i("chat", "pack sent: sms");
    }
}
