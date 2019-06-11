package com.luobin.ui;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.FirstActivity;
import com.example.jrd48.chat.TeamMemberInfo;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.protocol.root.VoiceStartProcesser;
import com.luobin.dvr.DvrService;
import com.luobin.dvr.R;
import com.luobin.ui.adapter.ContactsMemberAdapter;
import com.luobin.voice.VoiceHandler;
import com.example.jrd48.service.proto_gen.ProtoMessage;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.lake.librestreaming.client.RESClient;

public class TalkRoomActivity extends BaseActivity {

    @BindView(R.id.btn_return)
    Button btnReturn;
    @BindView(R.id.prefix_camera)
    Button prefixCamera;
    @BindView(R.id.rear_camera)
    Button rearCamera;
    @BindView(R.id.picture_in_picture)
    Button pictureInPicture;
    @BindView(R.id.voice)
    Button voice;
    @BindView(R.id.goto_map)
    Button gotoMap;
    @BindView(R.id.do_not_disturb)
    Button doNotDisturb;
    @BindView(R.id.group_name)
    TextView groupName;
    @BindView(R.id.btn_add)
    Button btnAdd;
    @BindView(R.id.lv_member)
    ListView lvMember;

    boolean single = false;

    public static final String TAG = "pangtao";

    ContactsMemberAdapter memberAdapter;
    String mGroupName = "";
    private int callType = 0;// 0 代表不需要，1代表接收呼叫，2代表发起呼叫
    private long teamId = -1;
    ArrayList<TeamMemberInfo> memberList;
    PowerManager.WakeLock wakeLock = null;
    String linkmanPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalStatus.setFirstCreating(true);
        GlobalStatus.setIsFirstPause(false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk_room);
        ButterKnife.bind(this);
        VoiceHandler.doVoiceAction(mContext,false);
        memberList = getIntent().getParcelableArrayListExtra("memberList");
        mGroupName = getIntent().getStringExtra("group_name ");
        teamId = getIntent().getLongExtra("team_id",-1);
        callType = getIntent().getIntExtra("callType", 0);

        if (teamId == -1 ){
            single = true;
        }else{
            single = false;
        }

        Log.d(TAG,"callType = " + callType);
        if (memberList != null){
            memberAdapter = new ContactsMemberAdapter(memberList,mContext);
            lvMember.setAdapter(memberAdapter);
            groupName.setText(mGroupName);
        }

        if (!ConnUtil.isConnected(this)) {
            ToastR.setToast(this, "连接服务器失败，请检查网络连接");
            finish();
            return;
        }

    }

    private void InitiateCall(){

        ProtoMessage.ChatRoomMsg chatRoomMsg = GlobalStatus.getChatRoomMsg();
        if (chatRoomMsg != null && callType == 0) {

        }else if (callType == 1) {

        }else if (callType == 2) {
            ProtoMessage.StartVoiceMsg.Builder builder = ProtoMessage.StartVoiceMsg.newBuilder();
            if (teamId == -1) {
                builder.setToUserPhone(linkmanPhone);
            } else {
                builder.setTeamID(teamId);
            }
            GlobalStatus.setChatRoomtempId(0);
            GlobalStatus.setIsStartRooming(true);
            MyService.start(mContext, ProtoMessage.Cmd.cmdStartVoice.getNumber(), builder.build());
            IntentFilter filter = new IntentFilter();
            filter.addAction(VoiceStartProcesser.ACTION);
            Log.i("pocdemo", "start voice action, want room id...");
            final TimeoutBroadcast b = new TimeoutBroadcast(mContext, filter, getBroadcastManager());

            b.startReceiver(10, new ITimeoutBroadcast() {
                @Override
                public void onTimeout() {
                    // stop&start thread after startVoice failed
                    sendBroadcastAsUser(new Intent("com.erobbing.ACTION_STOP_THREAD"), UserHandle.ALL);
                    ToastR.setToast(mContext, "发起呼叫失败");
                    // closeRoom(true);
                    Log.v(FirstActivity.class.getSimpleName(), "startVoice timeout");
                    sendBroadcastAsUser(new Intent("com.erobbing.ACTION_START_THREAD"), UserHandle.ALL);
                }

                @Override
                public void onGot(Intent i) {
                    Log.v("pocdemo", "VoiceStartProcesser onGot");
                    if (i.getIntExtra("error_code", -1) ==
                            ProtoMessage.ErrorCode.OK.getNumber()) {
                        long roomId = i.getLongExtra("room_id", -1);

                        if(GlobalStatus.isPttBroadCast() && !GlobalStatus.isPttKeyDown()){
                            Intent service = new Intent(mContext, MyService.class);
                            service.putExtra("ptt_key_action", true);
                            startService(service);
                        }


                    } else {
                        ToastR.setToast(mContext, "呼叫失败");

                    }
                }
            });
        }

    }



    @OnClick({R.id.btn_return, R.id.prefix_camera, R.id.rear_camera, R.id.picture_in_picture, R.id.voice, R.id.goto_map, R.id.do_not_disturb, R.id.btn_add})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_return:
                finish();
                break;
            case R.id.prefix_camera:
                break;
            case R.id.rear_camera:
                break;
            case R.id.picture_in_picture:
                break;
            case R.id.voice:
                voice.setTextColor(mContext.getResources().getColor(R.color.match_btn_bg));
                GlobalStatus.setIsVideo(false);
                DvrService.start(MyApplication.getContext(), RESClient.ACTION_STOP_RTMP, null);
                DvrService.start(MyApplication.getContext(), RESClient.ACTION_STOP_PLAY, null);
                InitiateCall();
                break;
            case R.id.goto_map:
                // 按下 PTT键
                PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "pocdemo");
                wakeLock.setReferenceCounted(false);
                wakeLock.acquire(60 * 1000); // 最多1分钟

                GlobalStatus.setPttKeyDown(true);
                VoiceHandler.speakBeginAndRecording(mContext);
                break;
            case R.id.do_not_disturb:
                //抬起操作
                try {
                    // 弹起 PTT键
                    GlobalStatus.setPttKeyDown(false);
                    VoiceHandler.speakEndAndRecroding(mContext);
                } finally {
                    if (wakeLock != null) {
                        wakeLock.release();
                    }
                }
                break;
            case R.id.btn_add:

                break;
        }
    }
}
