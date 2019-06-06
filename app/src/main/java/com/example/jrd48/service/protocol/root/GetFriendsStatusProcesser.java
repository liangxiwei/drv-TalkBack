package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.AppliedFriendsList;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/5.
 */

public class GetFriendsStatusProcesser extends CommonProcesser {
    public final static String ACTION = "com.example.jrd48.service.protocol.root.action.GetFriendsStatusProcesser";
    public final static String STATUS_KEY = "status";
    public GetFriendsStatusProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(byte[] data) {
        //Log.i("chat", "获得好友列表应答: " + HexTools.byteArrayToHex(data));
        Intent i = new Intent(ACTION);
        try {
            ProtoMessage.MsgFriendsStatus re = ProtoMessage.MsgFriendsStatus.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            //Log.i("AppliedListProcesser", "got applied  code: "+re.getFriendsList() );
            if (re == null) {
//                ProtoMessage.CommonResp resp = ProtoMessage.CommonResp.parseFrom(ArrayUtils.subarray(data, 4, data.length));
//                i.putExtra("error_code", resp.getErrorCode());
                throw new Exception("unknown response.");
            } else {
                i.putExtra("error_code", re.getErrorCode());
                if (re.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {
//                    Log.i("jim", "获得好友在线状态成功");
//                    for (int t = 0;t < re.getFriendsList().size();t++){
//                        Log.d("jim","在线好友状态："+re.getFriendsList().get(t).getPhoneNum());
//                    }
                   // Log.d("jim","在线好友状态："+re.getFriendsList());
                    // TODO: 这里处理添加 其他正确的数据
                    i.putExtra(STATUS_KEY,re);

                } else {
                    Log.i("jim", "获得好友在线状态错误码: " + re.getErrorCode());
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
