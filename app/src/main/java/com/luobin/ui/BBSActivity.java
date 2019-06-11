package com.luobin.ui;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.TeamInfoList;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.GroupsListProcesser;
import com.luobin.dvr.R;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbs);
        ButterKnife.bind(this);
        initData();
    }

    private void initData(){
        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
        MyService.start(getContext(), ProtoMessage.Cmd.cmdGetBBSList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(GroupsListProcesser.ACTION);
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

                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    public void fail(int i) {
        new ResponseErrorProcesser(getContext(), i);
    }
}
