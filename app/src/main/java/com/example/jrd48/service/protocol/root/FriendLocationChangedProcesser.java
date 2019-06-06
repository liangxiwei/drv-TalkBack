package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.jrd48.chat.FriendLocationStatus;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;
import com.google.protobuf.InvalidProtocolBufferException;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by Administrator on 2017/3/15 0015.
 */

public class FriendLocationChangedProcesser extends CommonProcesser {

    private final String TAG = "FriendLocationChangedProcesser";
    public final static String ACTION = "ACTION.FriendLocationChangedProcesser";

    public FriendLocationChangedProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(byte[] data) {
        Intent intent = new Intent(ACTION);
        try {
            ProtoMessage.LocationMsg resp = ProtoMessage.LocationMsg.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            if(resp == null || resp.getErrorCode() != ProtoMessage.ErrorCode.OK_VALUE){
                intent.putExtra("error_code", resp.getErrorCode());
                Log.i("chat", "错误码: " + resp.getErrorCode());
            }else {
                FriendLocationStatus friendLocation = new FriendLocationStatus();
                friendLocation.setLat(resp.getLat());
                friendLocation.setLng(resp.getLng());
                friendLocation.setPhoneNum(resp.getPhoneNum());
                friendLocation.setLatlngType(resp.getLatlngType());
                friendLocation.setIsAccurate(resp.getIsAccurate());
                friendLocation.setTime(resp.getTime());
                friendLocation.setRadius(resp.getRadius());
                intent.putExtra("friendLocation", friendLocation);
                context.sendBroadcast(intent);
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSent() {

    }
}
