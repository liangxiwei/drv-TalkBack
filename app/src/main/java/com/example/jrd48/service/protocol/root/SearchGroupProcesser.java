package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.TeamInfoList;
import com.example.jrd48.service.HexTools;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/5.
 */

public class SearchGroupProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.SearchGroupProcesser";

    public SearchGroupProcesser(Context context) {
        super(context);
    }
    @Override
    public void onGot(byte[] data) {
        Log.i("chat", "获得查询群组应答: ");
        Intent i = new Intent(ACTION);
        try {
            ProtoMessage.TeamList re = ProtoMessage.TeamList.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            if (re == null ){
//                ProtoMessage.CommonResp resp = ProtoMessage.CommonResp.parseFrom(ArrayUtils.subarray(data, 4, data.length));
//                i.putExtra("error_code", resp.getErrorCode());
                throw new Exception("unknown response.");
            } else {
                i.putExtra("error_code", re.getErrorCode());
                if (re.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {
                    Log.i("chat", "获得查询群组正确应答");
                    // TODO: 这里处理添加 其他正确的数据
                    List<ProtoMessage.TeamInfo> team = re.getTeamsList();
                    TeamInfoList afList = new TeamInfoList();
                    List<TeamInfo> list = new ArrayList<TeamInfo>();
                    if (team.size() > 0) {
                        for (ProtoMessage.TeamInfo te : team) {
                            TeamInfo at = new TeamInfo();
                            at.setTeamID(te.getTeamID());
                            at.setTeamName(te.getTeamName());
                            at.setMemberRole(te.getMemberRole());
                            at.setGroupID(te.getGroupID());
                            at.setTeamType(te.getTeamType());
                            at.setTeamDesc(te.getTeamDesc());
                            at.setTeamPriority(te.getTeamPriority());
                            list.add(at);
                        }
                        afList.setmTeamInfo(list);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("get_seach_group_list", afList);
                        i.putExtras(bundle);
                    } else {
                        Log.i("chat", "查询群组错误码: " + re.getErrorCode());
                    }
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
