package com.example.jrd48.service.protocol.root;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.example.jrd48.chat.group.sqlite.DataBaseTool;
import com.example.jrd48.chat.group.sqlite.TeamMemberInfoManager;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取所有群成员数据
 * Created by Administrator on 2016/12/9 0009.
 */

public class GetTeamMemberListProcesser extends CommonProcesser {

    private final String TAG = "GetTeamMemberListProces";
    public final static String ACTION = "ACTION.GetTeamMemberListProcesser";
    private Context context;
    //所有
    public GetTeamMemberListProcesser(Context context){
        super(context);
        this.context = context;
    }

    @Override
    public void onGot(final byte[] data) {

        new AsyncTask<String, Integer, Integer>() {
            @Override
            protected Integer doInBackground(String... strings) {
                synchronized (GetTeamMemberListProcesser.class) {
                    Intent i = new Intent(ACTION);
                    try {
                        ProtoMessage.TeamMemberList teamMemberListList = ProtoMessage.TeamMemberList.parseFrom(ArrayUtils.subarray(data, 4, data.length));
                        if (teamMemberListList == null || teamMemberListList.getErrorCode() != ProtoMessage.ErrorCode.OK_VALUE) {
                            Log.i(TAG, "got resp code: " + teamMemberListList.getErrorCode());
                            i.putExtra("error_code", teamMemberListList.getErrorCode());
                        } else {
                            i.putExtra("error_code", ProtoMessage.ErrorCode.OK.getNumber());
//                            List<Integer> list = new ArrayList<>();
//                            ProtoMessage.UserInfo userInfo = teamMemberListList.getUserInfo();
                            List<TeamMemberInfoManager> infoList = new ArrayList<>();
                            for (int t = 0;t<teamMemberListList.getMembersCount();t++){
//                                Log.d("tttt",teamMemberListList.getMembers(t).getTeamID()+"    "+teamMemberListList.getMembers(t).getUserName());
                                ProtoMessage.TeamMember teamInfo = teamMemberListList.getMembers(t);
                                if (t == 0) {
                                    infoList.add(new TeamMemberInfoManager(teamInfo,teamInfo.getTeamID()));// 100根据自己需求调整
                                } else {
                                    boolean isIn = false;
                                    for (TeamMemberInfoManager item : infoList) {
                                        if (item.getTeamId() == teamInfo.getTeamID()) {
                                            item.addTeamMemberInfo(teamInfo);
                                            isIn = true;
                                            break;
                                        }
                                    }
                                    if (!isIn) {
                                        infoList.add(new TeamMemberInfoManager(teamInfo,teamInfo.getTeamID()));
                                    }
                                }
                            }
                            //将群成员分别保存在对应的群组里
                            for (TeamMemberInfoManager item:infoList){
                                DataBaseTool.saveData(item.getTeamMemberInfoList(),item.getTeamId(),context);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        i.putExtra("error_code", ProtoMessage.ErrorCode.UNKNOWN_VALUE);
                    }
                    context.sendBroadcast(i);
                    return null;
                }
            }
        }.execute("");

    }

    @Override
    public void onSent() {

    }
}
