package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.jrd48.chat.TeamMemberInfo;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.AppliedFriendsList;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.service.HexTools;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/5.
 */

public class AssignTeamAdminProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.AssignTeamAdminProcesser";

    public AssignTeamAdminProcesser(Context context) {
        super(context);
    }
    @Override
    public void onGot(byte[] data) {
//        Log.i("chat", "获得修改群成员管理权限应答: " + HexTools.byteArrayToHex(data));
//        try {
//            ProtoMessage.CommonResp resp = ProtoMessage.CommonResp.parseFrom(ArrayUtils.subarray(data, 4, data.length));
//            Log.i("chat", "获得修改群成员管理权限校验码: " + resp.getErrorCode());
//            Intent i = new Intent(ACTION);
//            i.putExtra("error_code", resp.getErrorCode());
//            context.sendBroadcast(i);
//
//            // TODO: 0 OK，其他值，失败
//            //
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

        Intent i = new Intent(ACTION);
        try {
            ProtoMessage.AssignTeamAdmin re = ProtoMessage.AssignTeamAdmin.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            if (re == null) {
//                ProtoMessage.CommonResp resp = ProtoMessage.CommonResp.parseFrom(ArrayUtils.subarray(data, 4, data.length));
//                i.putExtra("error_code", resp.getErrorCode());
                throw new Exception("unknown response.");
            } else {
                i.putExtra("error_code", re.getErrorCode());
                if (re.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {
                    Log.i("chat", "获得修改群成员管理权限应答");
                    // TODO: 这里处理添加 其他正确的数据
                    TeamMemberInfo teamMemberInfo = new TeamMemberInfo();
                    teamMemberInfo.setRole(re.getAdmin());
                    teamMemberInfo.setUserPhone(re.getPhoneNum());
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("get_team_member_info", teamMemberInfo);
                    i.putExtras(bundle);
                } else {
                    Log.i("chat", "获得修改群成员管理权限错误码: " + re.getErrorCode());
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
