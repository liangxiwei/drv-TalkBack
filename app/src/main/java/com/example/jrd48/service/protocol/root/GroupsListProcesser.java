package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.jrd48.chat.group.DBHelperTeamList;
import com.example.jrd48.chat.group.DBManagerTeamList;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.TeamInfoList;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/5.
 */

public class GroupsListProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.GroupsListProcesser";
    private Context mContext;

    public GroupsListProcesser(Context context) {
        super(context);
        mContext = context;
    }
    @Override
    public void onGot(byte[] data) {
        //Log.i("chat", "获得群列表应答: " + HexTools.byteArrayToHex(data));
        Intent i = new Intent(ACTION);
        try {
            ProtoMessage.TeamList re = ProtoMessage.TeamList.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            if (re == null) {
//                ProtoMessage.CommonResp resp = ProtoMessage.CommonResp.parseFrom(ArrayUtils.subarray(data, 4, data.length));
//                i.putExtra("error_code", resp.getErrorCode());
                throw new Exception("unknown response.");
            } else {
                i.putExtra("error_code", re.getErrorCode());
                if (re.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {
                    Log.i("chat", "获得群列表正确应答");
                    // TODO: 这里处理添加 其他正确的数据
                    List<ProtoMessage.TeamInfo> team = re.getTeamsList();
                    TeamInfoList afList = new TeamInfoList();
                    List<TeamInfo> list = new ArrayList<TeamInfo>();
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

                    DBManagerTeamList db = new DBManagerTeamList(mContext, DBTableName.getTableName(mContext, DBHelperTeamList.NAME));
                    try {
                        db.add(list);
                    } catch (Exception e){
                        e.printStackTrace();
                    } finally {
                        db.closeDB();
                    }

                    Bundle bundle = new Bundle();
                    bundle.putParcelable("get_group_list", afList);
                    i.putExtras(bundle);
                } else {
                    Log.i("chat", "获得群列表错误码: " + re.getErrorCode());
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
