package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;
import com.luobin.model.SearchStrangers;

import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/5.
 */

public class TypeSearchFriendsProcesser extends CommonProcesser {
    public final static String ACTION = "com.example.jrd48.service.protocol.root.action.TypeSearchFriendsProcesser";

    public TypeSearchFriendsProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(byte[] data) {
        //Log.i("chat", "获得查询好友应答: " + HexTools.byteArrayToHex(data));
        Intent i = new Intent(ACTION);
        try {
            ProtoMessage.MsgSearchCar re = ProtoMessage.MsgSearchCar.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            if (re == null) {
//                ProtoMessage.CommonResp resp = ProtoMessage.CommonResp.parseFrom(ArrayUtils.subarray(data, 4, data.length));
//                i.putExtra("error_code", resp.getErrorCode());
                throw new Exception("unknown response.");
            } else {
                i.putExtra("error_code", re.getErrorCode());
                if (re.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {

//                    ProtoMessage.MsgSearchCar.Builder msg = ProtoMessage.MsgSearchCar.newBuilder();
//                    msg.setCarType2(re.getCarType2());
//                    msg.setCarType1(re.getCarType1());
//                    msg.setCarType3(re.getCarType3());
//                    msg.setPos(re.getPos());
//                    msg.setCity(re.getCity());
//                    msg.setTown(re.getTown());

                    ArrayList<SearchStrangers> searchFriendsList = new ArrayList<>();
                    List<ProtoMessage.UserInfo> userInfoList = re.getSearchResult().getFriendsList();
                    for (ProtoMessage.UserInfo list : userInfoList) {
                        FriendFaceUtill.saveFriendFaceImg(list.getUserName(), list.getPhoneNum(), list.getUserPic().toByteArray(), context);
                        SearchStrangers searchFriends = new SearchStrangers();
                        searchFriends.setCarID(list.getCarID());
                        searchFriends.setCarNum(list.getCarNum());
                        searchFriends.setCarType1(list.getCarType1());
                        searchFriends.setCarType2(list.getCarType2());
                        searchFriends.setCarType3(list.getCarType3());
                        searchFriends.setApplyInfo(list.getApplyInfo());
                        searchFriends.setProv(list.getProv());
                        searchFriends.setCity(list.getCity());
                        searchFriends.setTown(list.getTown());
                        searchFriends.setPhoneNum(list.getPhoneNum());
                        searchFriends.setBirthday(list.getBirthday());
                        searchFriends.setUserSex(list.getUserSex());
                        searchFriends.setUserName(list.getUserName());
                        searchFriends.setNickName(list.getNickName());
                        searchFriends.setFriendStar(list.getFriendStar());
                        searchFriendsList.add(searchFriends);
                    }

                    i.putExtra("user_info", (Serializable) searchFriendsList);

                } else {
                    Log.i("chat", "查询好友错误码3: " + re.getErrorCode());
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
