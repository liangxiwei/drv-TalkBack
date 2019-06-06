package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;
import com.google.protobuf.InvalidProtocolBufferException;
import com.luobin.model.StrangerLocationStatus;

import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/15 0015.
 */

public class GetCarLocationProcesser extends CommonProcesser {

    private final String TAG = "GetCarLocationProcesser";
    public final static String ACTION = "com.example.jrd48.service.protocol.root.action.GetCarLocationProcesser";

    public GetCarLocationProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(byte[] data) {
        Intent intent = new Intent(ACTION);
        try {
            ProtoMessage.MsgSearchAround resp = ProtoMessage.MsgSearchAround.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            if (resp == null || resp.getErrorCode() != ProtoMessage.ErrorCode.OK_VALUE) {
                intent.putExtra("error_code", resp.getErrorCode());
                Log.i("chat", "错误码: " + resp.getErrorCode());
            } else {
                intent.putExtra("error_code", resp.getErrorCode());
                List<ProtoMessage.LocationMsg> locationMsgLists = resp.getLocationsList();
                ArrayList<StrangerLocationStatus> list = new ArrayList<StrangerLocationStatus>();

                Log.i("GetCarLocationProcesser", "--------------GetCarLocationProcesser----------" + locationMsgLists.size());
                for (int i = 0; i < locationMsgLists.size(); i++) {

                    StrangerLocationStatus friendLocation = new StrangerLocationStatus();

                    friendLocation.setLat(locationMsgLists.get(i).getLat());
                    friendLocation.setLng(locationMsgLists.get(i).getLng());
                    friendLocation.setRadius(locationMsgLists.get(i).getRadius());
                    friendLocation.setPhoneNum(locationMsgLists.get(i).getPhoneNum());
                    friendLocation.setIsAccurate(locationMsgLists.get(i).getIsAccurate());
                    friendLocation.setTime(locationMsgLists.get(i).getTime());
                    friendLocation.setLatlngType(locationMsgLists.get(i).getLatlngType());
                    friendLocation.setUserName(locationMsgLists.get(i).getUserName());
                    Log.i("GetCarLocationProcesser", "--------------setLat setLng---------------" + locationMsgLists.get(i).getLat() + "   " + locationMsgLists.get(i).getLng() + "  " + locationMsgLists.get(i).getPhoneNum());
                    list.add(friendLocation);
                }
                Log.i("GetCarLocationProcesser", "--------------list---------------" + list.size());
                intent.putExtra("stranger_location", (Serializable) list);
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            intent.putExtra("error_code", ProtoMessage.ErrorCode.UNKNOWN_VALUE);
        }
        context.sendBroadcast(intent);
    }

    @Override
    public void onSent() {

    }
}
