package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;
import com.luobin.model.SearchStrangers;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by Administrator on 2016/12/5.
 */

public class SearchStrangerProcesser extends CommonProcesser {
    public final static String ACTION = "com.example.jrd48.service.protocol.root.action.SearchStrangerProcesser";

    public SearchStrangerProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(byte[] data) {
        //Log.i("chat", "获得查询好友应答: " + HexTools.byteArrayToHex(data));
        Intent i = new Intent(ACTION);
        try {
            ProtoMessage.UserInfo re = ProtoMessage.UserInfo.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            if (re == null) {
//                ProtoMessage.CommonResp resp = ProtoMessage.CommonResp.parseFrom(ArrayUtils.subarray(data, 4, data.length));
//                i.putExtra("error_code", resp.getErrorCode());
                throw new Exception("unknown response.");
            } else {
                i.putExtra("error_code", re.getErrorCode());
                if (re.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {
                    Log.i("chat", "获得查询好友正确应答");
                    // TODO: 这里处理添加 其他正确的数据
                    SearchStrangers searchFriends = new SearchStrangers();
//                  searchFriends.setCarID(list.getCarID());
                    searchFriends.setCarNum(re.getCarNum());
                    searchFriends.setCarType1(re.getCarType1());
                    searchFriends.setCarType2(re.getCarType2());
                    searchFriends.setCarType3(re.getCarType3());
                    searchFriends.setApplyInfo(re.getApplyInfo());
                    searchFriends.setProv(re.getProv());
                    searchFriends.setCity(re.getCity());
                    searchFriends.setTown(re.getTown());
                    searchFriends.setPhoneNum(re.getPhoneNum());
                    searchFriends.setBirthday(re.getBirthday());
                    searchFriends.setUserSex(re.getUserSex());
                    searchFriends.setUserName(re.getUserName());
                    searchFriends.setNickName(re.getNickName());
                    searchFriends.setFriendStar(re.getFriendStar());

                    FriendFaceUtill.saveFriendFaceImg(re.getUserName(), re.getPhoneNum(), re.getUserPic().toByteArray(), context);
                    GlobalImg.getImage(context,re.getPhoneNum());
//                    af.setUserPic(re.getUserPic().toByteArray());
//                    af.setUserSex(re.getUserSex());
                    i.putExtra("user_info", searchFriends);
                } else {
                    Log.i("chat", "查询好友错误码1: " + re.getErrorCode());
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
