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

public class ChangeTeamMemberNickNameProcesser extends CommonProcesser{
    public final static String ACTION = "ACTION.ChangeTeamMemberNickNameProcesser";

    public ChangeTeamMemberNickNameProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(byte[] data) {
        try {
            ProtoMessage.CommonResp resp = ProtoMessage.CommonResp.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            Log.i("pocdemo", "获取修改我在群组里昵称应答: " + resp.getErrorCode());
            Intent i = new Intent(ACTION);
            i.putExtra("error_code", resp.getErrorCode());
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
