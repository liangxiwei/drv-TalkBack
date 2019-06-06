package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.jrd48.GlobalNotice;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.AppliedFriendsList;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/5.
 */

public class AppliedListProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.AppliedListProcesser";

    public AppliedListProcesser(Context context) {
        super(context);
    }
    @Override
    public void onGot(byte[] data) {
        //Log.i("chat", "获得邀请我加为好友应答: " + HexTools.byteArrayToHex(data));
        Intent i = new Intent(ACTION);
        try {
            ProtoMessage.FriendList re = ProtoMessage.FriendList.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            if (re == null){
//                ProtoMessage.CommonResp resp = ProtoMessage.CommonResp.parseFrom(ArrayUtils.subarray(data, 4, data.length));
//                i.putExtra("error_code", resp.getErrorCode());
                throw new Exception("unknown response.");
            }else{
                i.putExtra("error_code", re.getErrorCode());
                if (re.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {
                    Log.i("chat", "获得邀请我加为好友正确应答");
                    // TODO: 这里处理添加 其他正确的数据
                    List<ProtoMessage.UserInfo> info = re.getFriendsList();
                    AppliedFriendsList afList = new AppliedFriendsList();
                    List<AppliedFriends> list = new ArrayList<AppliedFriends>();
                    for (ProtoMessage.UserInfo u : info) {
                        AppliedFriends af = new AppliedFriends();
                        af.setApplyInfo(u.getApplyInfo());
                        af.setFriendStar((u.getFriendStar()) ? 1 : 0);
                        af.setNickName(u.getNickName());
                        af.setUserName(u.getUserName());
                        af.setPhoneNum(u.getPhoneNum());
                        Log.i("pocdemo", "pic length2: " + u.getUserPic().size());
                        af.setUserPic(u.getUserPic().toByteArray());
                        af.setUserSex(u.getUserSex());
                        list.add(af);
                    }
                    afList.setAppliedFriends(list);
                    GlobalNotice.setNotice(afList);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("get_applied_msg", afList);
                    i.putExtras(bundle);
                } else {
                    Log.i("chat", "获得邀请我加为好友错误码: " + re.getErrorCode());
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
