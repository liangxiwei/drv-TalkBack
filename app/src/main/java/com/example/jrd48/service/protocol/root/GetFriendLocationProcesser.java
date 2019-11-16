package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.jrd48.chat.FriendLocationStatus;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;
import com.google.protobuf.InvalidProtocolBufferException;
import com.luobin.dvr.DvrConfig;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/15 0015.
 */

public class GetFriendLocationProcesser extends CommonProcesser {

    public final static String ACTION = "ACTION.GetFriendLocationProcesser";
    private final String TAG = "GetFriendLocation";

    public GetFriendLocationProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(final byte[] data) {

        new AsyncTask<String, Integer, Integer>() {
            @Override
            protected Integer doInBackground(String... strings) {
                synchronized (GetFriendLocationProcesser.class) {

                    Intent intent = new Intent(ACTION);
                    try {
                        ProtoMessage.LocationMsgList resp = ProtoMessage.LocationMsgList.parseFrom(ArrayUtils.subarray(data, 4, data.length));
                        if(resp == null || resp.getErrorCode() != ProtoMessage.ErrorCode.OK_VALUE){
                            intent.putExtra("error_code", resp.getErrorCode());
                            Log.i("chat", "GetFriendLocationProcesser errcode:" + resp.getErrorCode());
                        }else {
                            intent.putExtra("error_code", resp.getErrorCode());
                            List<ProtoMessage.LocationMsg> locationMsgLists = resp.getLocationsList();
                            ArrayList<FriendLocationStatus> list = new ArrayList<FriendLocationStatus>();

                            Log.i("GetFriendLocation", "--------------locationMsgLists----------" + locationMsgLists.size());
                            for(int i = 0; i < locationMsgLists.size(); i++){

                                FriendLocationStatus friendLocation = new FriendLocationStatus();

                                friendLocation.setLat(locationMsgLists.get(i).getLat());
                                friendLocation.setLng(locationMsgLists.get(i).getLng());
                                friendLocation.setRadius(locationMsgLists.get(i).getRadius());
                                friendLocation.setPhoneNum(locationMsgLists.get(i).getPhoneNum());
                                friendLocation.setIsAccurate(locationMsgLists.get(i).getIsAccurate());
                                friendLocation.setTime(locationMsgLists.get(i).getTime());
                                friendLocation.setLatlngType(locationMsgLists.get(i).getLatlngType());
                                Log.i("GetFriendLocation", "--------------setLat setLng---------------" + locationMsgLists.get(i).getLat() + "   " + locationMsgLists.get(i).getLng() + "  " + locationMsgLists.get(i).getPhoneNum());
                                list.add(friendLocation);
                            }
                            Log.i("GetFriendLocation", "--------------list---------------" + list.size());
                            intent.putParcelableArrayListExtra("location", list);
                        }
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                        intent.putExtra("error_code", ProtoMessage.ErrorCode.UNKNOWN_VALUE);
                    }
                    context.sendBroadcast(intent);
                    return null;
                }
            }
        }.execute("");
        /*Message msg = mHandler.obtainMessage();
        msg.what = DvrConfig.MSG_GET_FRIEND_LOCATION_PROCESSOR;
        Bundle bundle = new Bundle();
        bundle.putByteArray("friend_location_data", data);
        msg.setData(bundle);
        mHandler.sendMessage(msg);*/
    }

    @Override
    public void onSent() {

    }

    public Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Log.d(TAG, "mHandler what = " + msg.what);
            switch (msg.what) {
                case DvrConfig.MSG_GET_FRIEND_LOCATION_PROCESSOR:
                    byte[] data = msg.getData().getByteArray("friend_location_data");
                    synchronized (GetFriendLocationProcesser.class) {

                        Intent intent = new Intent(ACTION);
                        try {
                            ProtoMessage.LocationMsgList resp = ProtoMessage.LocationMsgList.parseFrom(ArrayUtils.subarray(data, 4, data.length));
                            if (resp == null || resp.getErrorCode() != ProtoMessage.ErrorCode.OK_VALUE) {
                                intent.putExtra("error_code", resp.getErrorCode());
                                Log.i("chat", "GetFriendLocationProcesser errcode:" + resp.getErrorCode());
                            } else {
                                intent.putExtra("error_code", resp.getErrorCode());
                                List<ProtoMessage.LocationMsg> locationMsgLists = resp.getLocationsList();
                                ArrayList<FriendLocationStatus> list = new ArrayList<FriendLocationStatus>();

                                Log.i("GetFriendLocation", "--------------locationMsgLists----------" + locationMsgLists.size());
                                for (int i = 0; i < locationMsgLists.size(); i++) {

                                    FriendLocationStatus friendLocation = new FriendLocationStatus();

                                    friendLocation.setLat(locationMsgLists.get(i).getLat());
                                    friendLocation.setLng(locationMsgLists.get(i).getLng());
                                    friendLocation.setRadius(locationMsgLists.get(i).getRadius());
                                    friendLocation.setPhoneNum(locationMsgLists.get(i).getPhoneNum());
                                    friendLocation.setIsAccurate(locationMsgLists.get(i).getIsAccurate());
                                    friendLocation.setTime(locationMsgLists.get(i).getTime());
                                    friendLocation.setLatlngType(locationMsgLists.get(i).getLatlngType());
                                    Log.i("GetFriendLocation", "--------------setLat setLng---------------" + locationMsgLists.get(i).getLat() + "   " + locationMsgLists.get(i).getLng() + "  " + locationMsgLists.get(i).getPhoneNum());
                                    list.add(friendLocation);
                                }
                                Log.i("GetFriendLocation", "--------------list---------------" + list.size());
                                intent.putParcelableArrayListExtra("location", list);
                            }
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                            intent.putExtra("error_code", ProtoMessage.ErrorCode.UNKNOWN_VALUE);
                        }
                        context.sendBroadcast(intent);
                    }
                    break;
            }
        }
    };
}
