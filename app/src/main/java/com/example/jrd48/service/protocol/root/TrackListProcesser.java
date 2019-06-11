package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.TeamInfoList;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/5.
 */

public class TrackListProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.TrackListProcesser";
    private Context mContext;

    public TrackListProcesser(Context context) {
        super(context);
        mContext = context;
    }
    @Override
    public void onGot(byte[] data) {
        //Log.i("chat", "获得群列表应答: " + HexTools.byteArrayToHex(data));
        Intent i = new Intent(ACTION);
        try {
            Log.d("pangtao","onGot");
            ProtoMessage.MsgTrackQuery re = ProtoMessage.MsgTrackQuery.parseFrom(ArrayUtils.subarray(data, 4, data.length));
            if (re == null) {
                Log.d("pangtao","re == null");
//                ProtoMessage.CommonResp resp = ProtoMessage.CommonResp.parseFrom(ArrayUtils.subarray(data, 4, data.length));
//                i.putExtra("error_code", resp.getErrorCode());
                throw new Exception("unknown response.");
            } else {
                i.putExtra("error_code", re.getErrorCode());
                Log.d("pangtao","re.getErrorCode() = "+ re.getErrorCode());
                if (re.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {
                    // TODO: 这里处理添加 其他正确的数据
                    Log.d("pangtao","team = " +re.getTracksList().size());
                  /*  List<ProtoMessage.TeamInfo> team = re.getTeamsList();
                    Log.d("pangtao","team = " + team.size());
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


                    Bundle bundle = new Bundle();
                    bundle.putParcelable("get_bbs_list", afList);
                    i.putExtras(bundle);*/
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
