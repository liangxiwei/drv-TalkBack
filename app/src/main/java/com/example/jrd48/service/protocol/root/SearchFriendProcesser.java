package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by Administrator on 2016/12/5.
 */

public class SearchFriendProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.SearchFriendProcesser";

    public SearchFriendProcesser(Context context) {
        super(context);
    }
    @Override
    public void onGot(byte[] data) {
        //Log.i("chat", "获得查询好友应答: " + HexTools.byteArrayToHex(data));
        Intent i = new Intent(ACTION);
        try {
            ProtoMessage.SearchUser re = ProtoMessage.SearchUser.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            if (re == null){
//                ProtoMessage.CommonResp resp = ProtoMessage.CommonResp.parseFrom(ArrayUtils.subarray(data, 4, data.length));
//                i.putExtra("error_code", resp.getErrorCode());
                throw new Exception("unknown response.");
            }else{
                i.putExtra("error_code", re.getErrorCode());
                if (re.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {
                    Log.i("chat", "获得查询好友正确应答");
                    // TODO: 这里处理添加 其他正确的数据
                    AppliedFriends af = new AppliedFriends();
//                    af.setApplyInfo(re.getApplyInfo());
//                    af.setFriendStar(re.getFriendStar());
//                    af.setNickName(re.getNickName);
                    af.setUserName(re.getUserName());
                    af.setPhoneNum(re.getPhoneNum());

                    FriendFaceUtill.saveFriendFaceImg(re.getUserName(),re.getPhoneNum(), re.getUserPic().toByteArray(), context);
                    GlobalImg.getImage(context,re.getPhoneNum());
//                    af.setUserPic(re.getUserPic().toByteArray());
//                    af.setUserSex(re.getUserSex());
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("search_user", af);
                    i.putExtras(bundle);
                } else {
                    Log.i("chat", "查询好友错误码2: " + re.getErrorCode());
                }
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
