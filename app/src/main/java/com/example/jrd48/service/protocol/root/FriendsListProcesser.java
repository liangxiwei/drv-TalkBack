package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.AppliedFriendsList;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.service.notify.NotifyManager;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/5.
 */

public class FriendsListProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.FriendsListProcesser";

    public FriendsListProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(byte[] data) {
        //Log.i("chat", "获得好友列表应答: " + HexTools.byteArrayToHex(data));
        Intent i = new Intent(ACTION);
        try {
            ProtoMessage.FriendList re = ProtoMessage.FriendList.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            //Log.i("AppliedListProcesser", "got applied  code: "+re.getFriendsList() );
            if (re == null) {
//                ProtoMessage.CommonResp resp = ProtoMessage.CommonResp.parseFrom(ArrayUtils.subarray(data, 4, data.length));
//                i.putExtra("error_code", resp.getErrorCode());
                throw new Exception("unknown response.");
            } else {
                i.putExtra("error_code", re.getErrorCode());
                if (re.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {
                    Log.i("chat", "获得好友列表正确应答");
                    // TODO: 这里处理添加 其他正确的数据
                    List<ProtoMessage.UserInfo> info = re.getFriendsList();
                    AppliedFriendsList afList = new AppliedFriendsList();
                    List<AppliedFriends> list = new ArrayList<AppliedFriends>();
                    for (ProtoMessage.UserInfo u : info) {
                        AppliedFriends af = new AppliedFriends();
                        af.setApplyInfo(u.getApplyInfo());
                        af.setFriendStar(u.getFriendStar() ? 1 : 0);
                        af.setNickName(u.getNickName());
                        if (u.getUserName() != null && u.getUserName().equals("-")){
                            af.setUserName(u.getPhoneNum());
                        }else {
                            af.setUserName(u.getUserName());
                        }
                        af.setPhoneNum(u.getPhoneNum());
                        af.setUserPic(u.getUserPic().toByteArray());
                        af.setUserSex(u.getUserSex());

                        af.setCarID(u.getCarID());//String
                        af.setCity(u.getCity());//String
                        af.setProv(u.getProv());//String
                        af.setTown(u.getTown());//String
                        af.setBirthday(u.getBirthday());//Long
                        af.setCarNum(u.getCarNum());//String
                        af.setCarBand(u.getCarType1());//String
                        af.setCarType2(u.getCarType2());//String
                        af.setCarType3(u.getCarType3());//String
                        list.add(af);

                        NotifyManager.getInstance().changeName(u.getPhoneNum(),af);
                    }
                    afList.setAppliedFriends(list);
                    //   Bundle bundle = new Bundle();
                    //  bundle.putParcelable("get_friends_list", "ok");
                    // i.putExtras(bundle);

                    DBManagerFriendsList db = new DBManagerFriendsList(context, DBTableName.getTableName(context, DBHelperFriendsList.NAME));
                    db.add(afList.getAppliedFriends(), true);
                    db.closeDB();

                } else {
                    Log.i("chat", "获得好友列表错误码: " + re.getErrorCode());
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
