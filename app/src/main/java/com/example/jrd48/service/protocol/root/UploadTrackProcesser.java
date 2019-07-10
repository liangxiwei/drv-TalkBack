package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.jrd48.chat.bean.Track;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/5.
 */

public class UploadTrackProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.UploadTrackProcesser";
    private Context mContext;

    public UploadTrackProcesser(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void onGot(byte[] data) {
        //Log.i("chat", "获得群列表应答: " + HexTools.byteArrayToHex(data));
        Intent i = new Intent(ACTION);
        try {
            ProtoMessage.MsgTrack re = ProtoMessage.MsgTrack.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            if (re == null) {
//                ProtoMessage.CommonResp resp = ProtoMessage.CommonResp.parseFrom(ArrayUtils.subarray(data, 4, data.length));
//                i.putExtra("error_code", resp.getErrorCode());
                throw new Exception("unknown response.");
            } else {
                i.putExtra("error_code", re.getErrorCode());
                if (re.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {
                    // TODO: 这里处理添加 其他正确的数据
                    Log.d("pangtao", "id = " + re.getTrackId());

                } else {
                    Log.i("chat", "获得群列表错误码: " + re.getErrorCode());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            i.putExtra("error_code", ProtoMessage.ErrorCode.UNKNOWN_VALUE);
        }
        context.sendBroadcast(i);
    }

    @Override
    public void onSent() {

    }

}
