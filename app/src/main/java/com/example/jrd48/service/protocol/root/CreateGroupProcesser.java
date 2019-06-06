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

public class CreateGroupProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.CreateGroupProcesser";

    public CreateGroupProcesser(Context context) {
        super(context);
    }
    @Override
    public void onGot(byte[] data) {
        Log.i("chat", "收到建群应答.");
        Intent i = new Intent(ACTION);
        try {
            ProtoMessage.TeamInfo re = ProtoMessage.TeamInfo.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            if (re == null) {
                throw new Exception("unknown response.");
            }
            i.putExtra("error_code", re.getErrorCode());
            if (re.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {
                Log.i("chat", "获得建群组正确应答");
                // TODO: 这里处理添加 其他正确的数据
                // i.putExtra(other useful value);

            }else {
                Log.i("chat", "建群错误码: " + re.getErrorCode());
            }

        }catch (Exception e) {
            e.printStackTrace();
            i.putExtra("error_code", ProtoMessage.ErrorCode.UNKNOWN_VALUE);
        }

        context.sendBroadcast(i);
    }

    @Override
    public void onSent() {

    }
}
