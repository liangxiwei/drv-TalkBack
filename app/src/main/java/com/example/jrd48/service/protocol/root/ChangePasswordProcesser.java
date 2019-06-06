package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.jrd48.service.HexTools;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;
import com.example.jrd48.service.protocol.Data;
import com.example.jrd48.service.protocol.IByteable;

import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;

/**
 * Created by quhuabo on 2016/11/19 0019.
 */

public class ChangePasswordProcesser extends CommonProcesser{
    public final static String ACTION = "ACTION.ChangePasswordProcesser";

    public ChangePasswordProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(byte[] data) {
        try {
            ProtoMessage.CommonResp resp = ProtoMessage.CommonResp.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            Log.i("pocdemo", "got resp code: "+resp.getErrorCode());
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

            // TODO: 0 OK，其他值，失败
            //
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSent() {
        Log.i("chat", "pack sent: sms");
    }
}
