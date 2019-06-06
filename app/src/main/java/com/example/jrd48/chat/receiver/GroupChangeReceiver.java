package com.example.jrd48.chat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.FirstActivity;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.chat.group.DBHelperTeamList;
import com.example.jrd48.chat.group.DBManagerTeamList;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.chat.location.ServiceCheckUserEvent;
import com.example.jrd48.service.ConnUtil;
import com.luobin.dvr.R;
import com.luobin.model.CallState;

import java.util.List;

/**
 * Created by Administrator on 2017/12/29.
 */

public class GroupChangeReceiver extends BroadcastReceiver {
    public final static String TAG = "GroupChangeReceiver";
    private final int SWITCH_PREDEFINE = 0x101;
    private Handler handler;

    @Override
    public void onReceive(Context context, Intent intent) {
        Context mContext = MyApplication.getContext();
        handler = GlobalStatus.getHandler();
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case SWITCH_PREDEFINE:
                            Bundle bundle = msg.getData();
                            switchCall(bundle.getString("option"));
                            break;
                    }
                }
            };
            GlobalStatus.setHandler(handler);
        }
        // TODO Auto-generated method stub
        if (handler.hasMessages(SWITCH_PREDEFINE)) {
            handler.removeMessages(SWITCH_PREDEFINE);
        }
        SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        String myPhone = preferences.getString("phone", "");
        if (ConnUtil.isConnected(mContext) && !TextUtils.isEmpty(myPhone)) {
            Message msg = new Message();
            msg.what = SWITCH_PREDEFINE;
            Bundle bundle = new Bundle();
            if (intent.getAction().equals("com.luobin.dvr.action.ACTION_GROUP_UP")) {
                bundle.putString("option", "up");
            } else if (intent.getAction().equals("com.luobin.dvr.action.ACTION_GROUP_DOWN")) {
                bundle.putString("option", "dn");
            }
            msg.setData(bundle);
            handler.sendMessageDelayed(msg, 300);
        }

    }


    private synchronized void switchCall(String option) {
        Context mContext = MyApplication.getContext();
        long curNumber = (long) SharedPreferencesUtils.get(mContext, "cur_teamId", (long) 0);
        long nextNumber = 0;
        List<Long> predefineTeams = GlobalStatus.getPredefineTeams();
        List<TeamInfo> predefineTeamsInfo = GlobalStatus.getTeamsInfoList();
        TeamInfo teamInfo = null;

        if (predefineTeams.size() > 0) {
            int index = predefineTeams.indexOf(curNumber);
            if (curNumber == 0 || index == -1) {
                nextNumber = predefineTeams.get(0);
                teamInfo = predefineTeamsInfo.get(0);
            } else {
                if (TextUtils.isEmpty(option)) {
                    nextNumber = curNumber;
                    teamInfo = predefineTeamsInfo.get(index);
                } else if (option.equals("up")) {
                    if (index == 0) {
                        nextNumber = predefineTeams.get(predefineTeams.size() - 1);
                        teamInfo = predefineTeamsInfo.get(predefineTeamsInfo.size() - 1);
                    } else {
                        nextNumber = predefineTeams.get(index - 1);
                        teamInfo = predefineTeamsInfo.get(index - 1);
                    }
                } else if (option.equals("dn")) {
                    if (index == predefineTeams.size() - 1) {
                        nextNumber = predefineTeams.get(0);
                        teamInfo = predefineTeamsInfo.get(0);
                    } else {
                        nextNumber = predefineTeams.get(index + 1);
                        teamInfo = predefineTeamsInfo.get(index + 1);
                    }
                }
            }
        }
        Log.v(TAG, "nextNumber" + nextNumber);
        Log.v(TAG, "predefineTeams" + predefineTeams.toString());
        String str = null;
        if (predefineTeams.size() == 0) {
            ToastR.setToast(mContext, mContext.getResources().getString(R.string.no_team));
        } else if (GlobalStatus.getChatRoomMsg() != null && predefineTeams.size() == 1 && GlobalStatus.getChatRoomMsg().getTeamID() == nextNumber) {
            ToastR.setToast(mContext, mContext.getResources().getString(R.string.only_one_team));
        } else if(teamInfo != null){
            Intent intent = new Intent(MyApplication.getContext(),FirstActivity.class);
            intent.putExtra("data", 1);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            CallState callState = GlobalStatus.getCallCallStatus().get(String.valueOf(1) + nextNumber);
            if (GlobalStatus.equalTeamID(nextNumber)) {
                intent.putExtra("callType", 0);
            } else if(callState != null && callState.getState() == GlobalStatus.STATE_CALL){
                intent.putExtra("callType", 1);
            } else {
                intent.putExtra("callType", 2);
            }

            intent.putExtra("group", nextNumber);
            intent.putExtra("type", teamInfo.getMemberRole());
            intent.putExtra("group_name", teamInfo.getTeamName());
            MyApplication.getContext().startActivity(intent);
        } else {
            Log.e(TAG,"teamInfo == null");
        }
    }
}
