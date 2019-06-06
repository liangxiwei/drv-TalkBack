package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.jrd48.service.HexTools;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by Administrator on 2016/12/5.
 */

public class AcceptFriendProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.AcceptFriendProcesser";

    public AcceptFriendProcesser(Context context) {
        super(context);
    }
    @Override
    public void onGot(byte[] data) {
        Log.i("chat", "获得拒绝或者接受好友邀请应答: ");
        try {
            ProtoMessage.CommonResp resp = ProtoMessage.CommonResp.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            Log.i("chat", "获得拒绝或者接受好友邀请校验码: " + resp.getErrorCode());

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

    }
}
