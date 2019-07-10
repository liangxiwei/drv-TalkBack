package com.luobin.ui;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.TabFragmentLinkmans;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.group.InviteJoinGroupActivity;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.chat.search.PinyinComparator;
import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.ApplyGroupProcesser;
import com.example.jrd48.service.protocol.root.FriendsListProcesser;
import com.luobin.dvr.R;
import com.luobin.tool.OnlineSetTool;
import com.luobin.ui.adapter.SelectMemberAdapter;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelectMemberActivity extends BaseDialogActivity {

    ListView selectMemberListView;
    Button ok;
    Context context;
    List<AppliedFriends> selectMemberList = new ArrayList<>();
    SelectMemberAdapter adapter;
    ImageView imgClose;
    long teamID;
    public static final int APPLYTEAM = 100102;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_member);
        getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        context = this;
        teamID = getIntent().getLongExtra("teamID",-1);

        if (teamID == -1){
            ToastR.setToast(this,"群ID错误");
            finish();
        }

        initView();
        initData();
    }


    private void initView(){
        selectMemberListView = (ListView) findViewById(R.id.member_select_listview);
        imgClose = (ImageView) findViewById(R.id.imgClose);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ok = (Button) findViewById(R.id.btn_ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter != null){
                    applyMember(adapter.getSelect());
                }
            }
        });
    }

    private void initData(){
        loadFriendsListFromNet();
    }




    /*
   获取好友
    */
    public void loadFriendsListFromNet() {
        if (!ConnUtil.isConnected(context)) {
            Log.w("drv", "没有网络");
            OnlineSetTool.removeAll();
            return;
        }
        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
        MyService.start(context, ProtoMessage.Cmd.cmdGetFriendList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(FriendsListProcesser.ACTION);
        new TimeoutBroadcast(context, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                ToastR.setToast(context, "连接超时");
            }

            @Override
            public void onGot(Intent i) {

                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    try {
                        GlobalImg.clear();
                        DBManagerFriendsList db = new DBManagerFriendsList(context, DBTableName.getTableName(context, DBHelperFriendsList.NAME));
                        selectMemberList = db.getFriends(false);

                        for (AppliedFriends c : selectMemberList) {
                            if (TextUtils.isEmpty(c.getNickName())) {
                                c.setNickName(c.getUserName());
                            }
                        }
                        // 排序(实现了中英文混排)
                        PinyinComparator comparator = new PinyinComparator();
                        Collections.sort(selectMemberList, comparator);
                        if (adapter == null){
                            adapter = new SelectMemberAdapter(selectMemberList,context);
                            selectMemberListView.setAdapter(adapter);
                        }else{
                            adapter.setData(selectMemberList);
                            adapter.notifyDataSetChanged();
                        }
                        db.closeDB();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void applyMember(final List<AppliedFriends> friend) {

        for (AppliedFriends appliedFriends : friend){
            Log.d("applyMember","name = " + appliedFriends.getPhoneNum());
        }

        ProtoMessage.ApplyTeam.Builder builder = ProtoMessage.ApplyTeam.newBuilder();
        builder.setTeamID(teamID);
        for (int i = 0; i < friend.size(); i++) {
            builder.addPhoneList(friend.get(i).getPhoneNum());
        }
        MyService.start(context, ProtoMessage.Cmd.cmdApplyTeam.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(ApplyGroupProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(context, filter, getBroadcastManager());

        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {

                ToastR.setToast(context, "连接超时");
            }

            @Override
            public void onGot(Intent intent) {
                int errorCode = intent.getIntExtra("error_code", -1);
                if (errorCode == ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(context, "邀请成功");
                    setResult(APPLYTEAM);
                    finish();
                } else {
                    fail(intent.getIntExtra("error_code", -1));
                }
            }
        });
    }

    public void fail(int i) {
        new ResponseErrorProcesser(context, i);
    }
}
