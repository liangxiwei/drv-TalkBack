package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.jrd48.chat.ToastR;
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

public class GetFriendInfoProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.GetFriendInfoProcesser";

    public GetFriendInfoProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(byte[] data) {
        //Log.i("chat", "获得新好友应答: " + HexTools.byteArrayToHex(data));
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
                    Log.i("chat", "获得单个好友信息正确应答");
                    // TODO: 这里处理添加 其他正确的数据
                    AppliedFriendsList afList = new AppliedFriendsList();
                    List<AppliedFriends> list = new ArrayList<AppliedFriends>();
                    AppliedFriends af = new AppliedFriends();
                    af.setApplyInfo(re.getApplyInfo());
                    af.setFriendStar(re.getFriendStar() ? 1 : 0);
                    af.setNickName(re.getNickName());
                    if (re.getUserName() != null && re.getUserName().equals("-")){
                        af.setUserName(re.getPhoneNum());
                    } else {
                        af.setUserName(re.getUserName());
                    }
                    af.setPhoneNum(re.getPhoneNum());
                    af.setUserPic(re.getUserPic().toByteArray());
                    af.setUserSex(re.getUserSex());


                    af.setCarID(re.getCarID());//String
                    af.setCity(re.getCity());//String
                    af.setProv(re.getProv());//String
                    af.setTown(re.getTown());//String
                    af.setBirthday(re.getBirthday());//Long
                    af.setCarNum(re.getCarNum());//String
                    af.setCarBand(re.getCarType1());//String
                    af.setCarType2(re.getCarType2());//String
                    af.setCarType3(re.getCarType3());//String

                    list.add(af);
                    afList.setAppliedFriends(list);
                    //   Bundle bundle = new Bundle();
                    //  bundle.putParcelable("get_friends_list", "ok");
                    // i.putExtras(bundle);

                    DBManagerFriendsList db = new DBManagerFriendsList(context, DBTableName.getTableName(context, DBHelperFriendsList.NAME));
                    db.add(afList.getAppliedFriends(), false);
                    db.closeDB();

                 //   ToastR.setToast(context,"你已经与" + af.getUserName() + "成为好友");
                } else {
                    Log.i("chat", "获得单个好友信息错误码: " + re.getErrorCode());
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
