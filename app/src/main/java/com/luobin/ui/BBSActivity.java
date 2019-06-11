package com.luobin.ui;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.FirstActivity;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.TeamInfoList;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.ApplyGroupProcesser;
import com.example.jrd48.service.protocol.root.BBSListProcesser;
import com.example.jrd48.service.protocol.root.GroupsListProcesser;
import com.example.jrd48.service.protocol.root.TrackListProcesser;
import com.luobin.dvr.R;
import com.luobin.model.CallState;
import com.luobin.ui.adapter.BBSAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.jrd48.chat.crash.MyApplication.getContext;

public class BBSActivity extends BaseActivity {

    @BindView(R.id.bbs_listview)
    ListView bbsListview;
    BBSAdapter bbsAdapter;
    List<TeamInfo> bbsList = new ArrayList<>();
    String userPhone = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbs);
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        userPhone = preferences.getString("phone", "");
        initData();
    }

    private void initData(){
       /* ProtoMessage.MsgTrackQuery.Builder builder = ProtoMessage.MsgTrackQuery.newBuilder();
        SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        builder.setUserPhone(preferences.getString("phone", ""));
        MyService.start(getContext(), ProtoMessage.Cmd.cmdGetTrackList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(TrackListProcesser.ACTION);
        new TimeoutBroadcast(getContext(), filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(getContext(), "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                Log.d("pangtao","onGot ");

                *//*if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    TeamInfoList list = i.getParcelableExtra("get_bbs_list");
                    bbsList = list.getmTeamInfo();
                    Log.d("pangtao","bbslist = " + bbsList.size());
                    bbsAdapter = new BBSAdapter(bbsList,getContext());
                    bbsListview.setAdapter(bbsAdapter);

                } else {
                    fail(i.getIntExtra("error_code", -1));
                }*//*
            }
        });*/

        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
        MyService.start(getContext(), ProtoMessage.Cmd.cmdGetBBSList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(BBSListProcesser.ACTION);
        new TimeoutBroadcast(getContext(), filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(getContext(), "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    TeamInfoList list = i.getParcelableExtra("get_bbs_list");
                    bbsList = list.getmTeamInfo();
                    Log.d("pangtao","bbslist = " + bbsList.size());
                    bbsAdapter = new BBSAdapter(bbsList,getContext());
                    bbsListview.setAdapter(bbsAdapter);
                    bbsListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            //加入群组，跳转到对讲界面
                            applyBBS(bbsList.get(position));
                        }
                    });
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }


    private void applyBBS(final TeamInfo info){
        ProtoMessage.ApplyTeam.Builder builder = ProtoMessage.ApplyTeam.newBuilder();
        builder.setTeamID(info.getTeamID());
        MyService.start(mContext, ProtoMessage.Cmd.cmdApplyTeam.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(ApplyGroupProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(mContext, filter, getBroadcastManager());

        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {

                ToastR.setToast(mContext, "加入海聊群失败，请检查网络");
            }

            @Override
            public void onGot(Intent intent) {
                int errorCode = intent.getIntExtra("error_code", -1);
                if (errorCode == ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(mContext, "已加入海聊群");
                    jumpFirstActivity(info);
                } else {
                    fail(intent.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void jumpFirstActivity(TeamInfo info){
        Intent intent = new Intent(getContext(), FirstActivity.class);
        intent.putExtra("data", 1);
        CallState callState = GlobalStatus.getCallCallStatus().get(String.valueOf(1) + info.getTeamID());
        if (GlobalStatus.equalTeamID(info.getTeamID())) {
            intent.putExtra("callType", 0);
        } else if (callState != null && callState.getState() == GlobalStatus.STATE_CALL) {
            intent.putExtra("callType", 1);
        } else {
            intent.putExtra("callType", 2);
        }
        intent.putExtra("group", info.getTeamID());
        intent.putExtra("type", info.getMemberRole());
        intent.putExtra("group_name", info.getTeamName());
        intent.putExtra("isBBS",true);
        VideoOrVoiceDialog dialog = new VideoOrVoiceDialog(getContext(), intent);
        dialog.show();
    }

    public void fail(int i) {
        new ResponseErrorProcesser(getContext(), i);
    }
}
