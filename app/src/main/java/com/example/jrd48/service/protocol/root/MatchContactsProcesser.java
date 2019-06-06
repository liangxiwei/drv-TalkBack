package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.AppliedFriendsList;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/5.
 */

public class MatchContactsProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.MatchContactsProcesser";

    public MatchContactsProcesser(Context context) {
        super(context);
    }

    @Override
    public void onGot(byte[] data) {
        Intent i = new Intent(ACTION);
        try {
            ProtoMessage.FriendList re = ProtoMessage.FriendList.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            if (re == null) {
                throw new Exception("unknown response.");
            } else {
                i.putExtra("error_code", re.getErrorCode());
                if (re.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {
                    Log.i("chat", "获得匹配号码正确应答");
                    // TODO: 这里处理添加 其他正确的数据
                    List<ProtoMessage.UserInfo> info = re.getFriendsList();
                    AppliedFriendsList afList = new AppliedFriendsList();
                    List<AppliedFriends> list = new ArrayList<AppliedFriends>();
                    for (ProtoMessage.UserInfo u : info) {
                        AppliedFriends af = new AppliedFriends();
                        af.setApplyInfo(u.getApplyInfo());
                        af.setFriendStar(u.getFriendStar() ? 1 : 0);
                        af.setNickName(u.getNickName());
                        af.setUserName(u.getUserName());
                        af.setPhoneNum(u.getPhoneNum());
//                        af.setUserPic(u.getUserPic().toByteArray());
                        af.setUserSex(u.getUserSex());
                        list.add(af);
                        if (u.getUserPic() != null && u.getUserPic().size() > 0) {
                            FriendFaceUtill.saveFriendFaceImg(u.getUserName(),u.getPhoneNum(), u.getUserPic().toByteArray(), context);
                            GlobalImg.getImage(context,u.getPhoneNum());
                        }
                    }
                    afList.setAppliedFriends(list);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("get_match_list", afList);
                    i.putExtras(bundle);

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
