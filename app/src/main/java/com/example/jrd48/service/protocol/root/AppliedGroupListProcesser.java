package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.jrd48.chat.group.AppliedTeams;
import com.example.jrd48.chat.group.AppliedTeamsList;
import com.example.jrd48.service.HexTools;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/5.
 */

public class AppliedGroupListProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.AppliedGroupListProcesser";

    public AppliedGroupListProcesser(Context context) {
        super(context);
    }
    @Override
    public void onGot(byte[] data) {
        Log.i("chat", "获取邀请我加群或者申请加入我的群应答: ");
        Intent i = new Intent(ACTION);
        try {
            ProtoMessage.AppliedTeamList re = ProtoMessage.AppliedTeamList.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            if (re == null){
//                ProtoMessage.CommonResp resp = ProtoMessage.CommonResp.parseFrom(ArrayUtils.subarray(data, 4, data.length));
//                i.putExtra("error_code", resp.getErrorCode());
                throw new Exception("unknown response.");
            }else{
                i.putExtra("error_code", re.getErrorCode());
                if (re.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {
                    Log.i("chat", "获取邀请我加群或者申请加入我的群正确应答");
                    // TODO: 这里处理添加 其他正确的数据
                    List<ProtoMessage.AppliedTeam> info = re.getApplyList();
                    AppliedTeamsList afList = new AppliedTeamsList();
                    List<AppliedTeams> list = new ArrayList<AppliedTeams>();
                    for (ProtoMessage.AppliedTeam u : info) {
                        AppliedTeams af = new AppliedTeams();
                        af.setUserPhone(u.getUserPhone());
                        af.setTeamID(u.getTeamID());
                        af.setUserName(u.getUserName());
                        af.setApplyType(u.getApplyType());
                        af.setTeamName(u.getTeamName());
                        af.setInviteUserName(0);
                        af.setInviteUserPhone("");
                        list.add(af);
                    }
                    afList.setAppliedTeams(list);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("get_applied_group_list", afList);
                    i.putExtras(bundle);
                } else {
                    Log.i("chat", "邀请或者申请加加群应答错误码: " + re.getErrorCode());
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
