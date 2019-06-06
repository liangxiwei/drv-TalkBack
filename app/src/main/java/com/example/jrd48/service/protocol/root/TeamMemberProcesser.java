package com.example.jrd48.service.protocol.root;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.SQLite.TeamMemberHelper;
import com.example.jrd48.chat.TeamMemberInfo;
import com.example.jrd48.chat.TeamMemberInfoList;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.example.jrd48.chat.group.MsgTool;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.CommonProcesser;
import com.luobin.dvr.R;

import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/5.
 */

public class TeamMemberProcesser extends CommonProcesser {
    public final static String ACTION = "ACTION.TeamMemberProcesser";
    public final static String KEY = "get_teamMember_msg";
    public final static String USER_INFO = "userInfo";

    public TeamMemberProcesser(Context context) {
        super(context);
    }
    @Override
    public void onGot(final byte[] data) {
        new AsyncTask<String, Integer, Integer>() {
            @Override
            protected Integer doInBackground(String... strings) {
                synchronized (TeamMemberProcesser.class) {
                    try {
                        ProtoMessage.TeamMemberList re = ProtoMessage.TeamMemberList.parseFrom(ArrayUtils.subarray(data, 4, data.length));
                        Intent i = new Intent(ACTION);
                        try {
                            if (re == null) {
                                throw new Exception("unknown response.");
                            }
                            i.putExtra("error_code", re.getErrorCode());
                            if (re.getErrorCode() == ProtoMessage.ErrorCode.OK_VALUE) {
                                Log.i("chat", "获得正确应答");
                                // TODO: 这里处理添加 其他正确的数据
                                // i.putExtra(other useful value);
                                List<ProtoMessage.TeamMember> team = re.getMembersList();
                                List<TeamMemberInfo> list = new ArrayList<TeamMemberInfo>();
                                if (re.getRequestFlag() == 1){
                                    ProtoMessage.UserInfo userInfo = null;
                                    for (ProtoMessage.TeamMember te : team) {
                                        userInfo = te.getUserInfo();
                                        TeamMemberInfo at = new TeamMemberInfo();
                                        at.setMemberPriority(te.getMemberPriority());
                                        at.setNickName(te.getNickName());
                                        at.setRole(te.getRole());
                                        if (te.getUserName() != null && te.getUserName().equals("-")){
                                            at.setUserName(te.getUserPhone());
                                        } else {
                                            at.setUserName(te.getUserName());
                                        }
                                        at.setUserPhone(te.getUserPhone());
                                        list.add(at);
                                        //更新数据库
                                        MsgTool.updateMemberMsg(context,at,userInfo,re.getTeamID());
                                    }
                                    TeamMemberInfoList afList = new TeamMemberInfoList();
                                    afList.setmTeamMemberInfo(list);
                                    Bundle bundle = new Bundle();
                                    bundle.putParcelable(KEY, afList);
                                    i.putExtra(USER_INFO,userInfo);
                                    i.putExtras(bundle);
                                } else {
                                    TeamMemberHelper teamMemberHelper = new TeamMemberHelper(context, re.getTeamID() + "TeamMember.dp", null);
                                    SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
                                    db.delete("LinkmanMember", null, null);
                                    ContentValues values = new ContentValues();
                                    for (ProtoMessage.TeamMember te : team) {
                                        TeamMemberInfo at = new TeamMemberInfo();
                                        String userPhone = te.getUserPhone();
                                        String userName = te.getUserName();
                                        String nickName = te.getNickName();
                                        if (userName != null && userName.equals("-")){
                                            userName = userPhone;
                                        }
                                        int role = te.getRole();
                                        int memberPriority = te.getMemberPriority();

                                        at.setMemberPriority(memberPriority);
                                        at.setNickName(nickName);
                                        at.setRole(role);
                                        at.setUserName(userName);
                                        at.setUserPhone(userPhone);
                                        list.add(at);
                                        values.put("user_phone", userPhone);
                                        values.put("user_name", userName);
                                        values.put("nick_name", nickName);
                                        values.put("role", role);
                                        values.put("member_priority", memberPriority);
                                        ProtoMessage.UserInfo userInfo = te.getUserInfo();
                                        values.put("userSex", userInfo.getUserSex());
                                        values.put(DBHelperFriendsList.CAR_ID, userInfo.getCarID());
                                        values.put(DBHelperFriendsList.CITY, userInfo.getCity());
                                        values.put(DBHelperFriendsList.PROV, userInfo.getProv());
                                        values.put(DBHelperFriendsList.TOWN, userInfo.getTown());
                                        values.put(DBHelperFriendsList.BIRTHDAY, userInfo.getBirthday());
                                        values.put(DBHelperFriendsList.CAR_NUM, userInfo.getCarNum());
                                        values.put(DBHelperFriendsList.CAR_BAND, userInfo.getCarType1());
                                        values.put(DBHelperFriendsList.CAR_TYPE2, userInfo.getCarType2());
                                        values.put(DBHelperFriendsList.CAR_TYPE3, userInfo.getCarType3());
                                        db.insert("LinkmanMember", null, values);
                                        Log.i("wsDvr", "TeamMember UserInfo:" + te.getUserInfo());
                                    }
                                    db.close();
                                }
            //                    afList.setmTeamMemberInfo(list);
            //                    Bundle bundle = new Bundle();
            //                    bundle.putParcelable("get_teamMember_list", afList);
            //                    i.putExtras(bundle);
                            } else {
                                Log.i("chat", "错误码: " + re.getErrorCode());
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            i.putExtra("error_code", ProtoMessage.ErrorCode.UNKNOWN_VALUE);
                        }
                        context.sendBroadcast(i);

                        // TODO: 0 OK，其他值，失败
                        //
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }
        }.execute("");
    }

    @Override
    public void onSent() {

    }
}
