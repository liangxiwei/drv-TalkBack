package com.luobin.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.FirstActivity;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.bean.Track;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.chat.group.MsgTool;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.TeamInfoList;
import com.example.jrd48.chat.location.Utils;
import com.example.jrd48.chat.wiget.TransparentProgressDialog;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.ApplyGroupProcesser;
import com.example.jrd48.service.protocol.root.BBSListProcesser;
import com.example.jrd48.service.protocol.root.DismissTeamProcesser;
import com.example.jrd48.service.protocol.root.DownloadTrackProcesser;
import com.example.jrd48.service.protocol.root.GroupsListProcesser;
import com.example.jrd48.service.protocol.root.TrackListProcesser;
import com.example.jrd48.service.protocol.root.UploadTrackProcesser;
import com.luobin.dvr.R;
import com.luobin.model.CallState;
import com.luobin.ui.adapter.BBSAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.jrd48.chat.crash.MyApplication.getContext;

public class BBSActivity extends BaseActivity {

    //@BindView(R.id.bbs_listview1)
    //ListView bbsListview1;
    //@BindView(R.id.bbs_listview2)
    //ListView bbsListview2;
    @BindView(R.id.bbs_gridview)
    GridView mBBSGridView;
    @BindView(R.id.btn_return)
    Button btnReturn;
    BBSAdapter bbsAdapter1;
    BBSAdapter bbsAdapter2;
    BBSAdapter mBBSAdapter;
    List<TeamInfo> bbsList1 = new ArrayList<>();
    List<TeamInfo> bbsList2 = new ArrayList<>();
    List<TeamInfo> mBBSList = new ArrayList<>();
    private static final int MSG_ENTER_BBS = 1;
    private static final int MSG_JUMP_FIRSTACTIVITY = 2;
    private static final int MSG_PROGRESS_CANCEL = 3;
    private static final int MSG_PROGRESS_DISMISS = 4;
    String userPhone = "";
    private static final String TAG = "BBSActivity";
    private ProgressDialog mProgressDialog;
    private Dialog mTransparentProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbs);
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        userPhone = preferences.getString("phone", "");
        initData();
    }

    @Override
    protected void onPause() {
        GlobalStatus.setBBSPageActivated(false);
        super.onPause();
    }

    @OnClick(R.id.btn_return)
    public void onBtnReturnClick(View view) {
        finish();
    }

    private void initData() {
        GlobalStatus.setBBSPageActivated(true);
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
                    List<TeamInfo> tempList = list.getmTeamInfo();
                    mBBSList = list.getmTeamInfo();
                    mBBSAdapter = new BBSAdapter(mBBSList, MyApplication.getContext());
                    mBBSGridView.setAdapter(mBBSAdapter);
                    mBBSGridView.setSelector(R.drawable.tab_list_item_selector);
                    mBBSGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                            groupQuit(mBBSList.get(position));
                            //加入群组，跳转到对讲界面
                            mHandler.removeMessages(MSG_ENTER_BBS);
                            Message msg = mHandler.obtainMessage();
                            msg.what = MSG_ENTER_BBS;
                            Bundle bundle = new Bundle();
                            bundle.putInt("bbs_grid_position", position);
                            msg.setData(bundle);
                            mHandler.sendMessageDelayed(msg, 500);
                            //avoid ptt behabior before bbs is ready
                            SharedPreferencesUtils.put(getContext(), "ptt_not_allowed", true);
                            //applyBBS(bbsList1.get(position));
                        }
                    });
                    /*if (tempList != null) {
                        int length = tempList.size() / 2 + tempList.size() % 2;
                        int index = 0;
                        for (TeamInfo info : tempList) {
                            if (index < length) {
                                bbsList1.add(info);
                            } else {
                                bbsList2.add(info);
                            }
                            index = index + 1;
                        }
                    }
                    bbsAdapter1 = new BBSAdapter(bbsList1,getContext());
                    bbsListview1.setAdapter(bbsAdapter1);
					bbsListview1.setSelector(R.drawable.tab_list_item_selector);
                    bbsListview1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            //加入群组，跳转到对讲界面
                            applyBBS(bbsList1.get(position));
                        }
                    });
                    bbsListview1.setOnKeyListener(new View.OnKeyListener() {
                        @Override
                        public boolean onKey(View view, int i, KeyEvent keyEvent) {
                            if (!bbsListview1.hasFocus()){
                                Log.i("BBSActivity","not focusable");
                                return false;
                            }
                            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getRepeatCount() == 0) {

                            }
                            return false;
                        }
                    });
                    bbsAdapter2 = new BBSAdapter(bbsList2,getContext());
                    bbsListview2.setAdapter(bbsAdapter2);
					bbsListview2.setSelector(R.drawable.tab_list_item_selector);
                    bbsListview2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            //加入群组，跳转到对讲界面
                            applyBBS(bbsList2.get(position));
                        }
                    });
                    bbsListview2.setOnKeyListener(new View.OnKeyListener() {
                        @Override
                        public boolean onKey(View view, int i, KeyEvent keyEvent) {
                            if (!bbsListview2.hasFocus()){
                                Log.i("BBSActivity","not focusable");
                                return false;
                            }
                            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getRepeatCount() == 0) {

                            }
                            return false;
                        }
                    });*/
                    Intent respIntent = new Intent();
                    respIntent.putExtra("data", 0);
                    setResult(RESULT_OK, respIntent);
                    //finish();
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }


    private void applyBBS(final TeamInfo info) {
        showProgressDialog();
        ProtoMessage.ApplyTeam.Builder builder = ProtoMessage.ApplyTeam.newBuilder();
        builder.setTeamID(info.getTeamID());
        Log.d(TAG, "applyBBS-info.getTeamID()=" + info.getTeamID().toString());
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
                Log.d(TAG, "applyBBS-errorCode=" + errorCode);
                if (errorCode == ProtoMessage.ErrorCode.OK.getNumber()) {
                    //ToastR.setToast(mContext, "已进入BBS");
                    mHandler.removeMessages(MSG_JUMP_FIRSTACTIVITY);
                    Message msg = mHandler.obtainMessage();
                    msg.what = MSG_JUMP_FIRSTACTIVITY;
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("teaminfo_bbs", info);
                    msg.setData(bundle);
                    mHandler.sendMessageDelayed(msg, 600);
                    //jumpFirstActivity(info);
                } else {
                    ToastR.setToast(mContext, "加入海聊群失败！");
                    fail(intent.getIntExtra("error_code", -1));
                    groupQuit(info);
                }
            }
        });
    }

    private void groupQuit(final TeamInfo info) {
        ProtoMessage.ApplyTeam.Builder builder = ProtoMessage.ApplyTeam.newBuilder();
        builder.setTeamID(info.getTeamID());
        MyService.start(mContext, ProtoMessage.Cmd.cmdQuitTeam.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(DismissTeamProcesser.ACTION);
        new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    MsgTool.deleteTeamMsg(mContext, info.getTeamID());
                    //if (!isBBS){
                    ToastR.setToast(mContext, "退出群组成功");
                    //finish();
                    //}
                    Log.d(TAG, "groupQuit = success");
                    GlobalStatus.setOldChat(0, "", 0);
                    GlobalStatus.clearChatRoomMsg();
                    SharedPreferencesUtils.put(MyApplication.getContext(), "cur_teamId", "");
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void jumpFirstActivity(TeamInfo info) {
        Intent intent = new Intent(getContext(), FirstActivity.class);
        intent.putExtra("data", 1);
        Log.d(TAG, "jumpFirstActivity.TeamInfo.group="
                + info.getGroupID() + "--getTeamName="
                + info.getTeamName() + "-getTeamID=" + info.getTeamID());
        CallState callState = GlobalStatus.getCallCallStatus().get(String.valueOf(1) + info.getTeamID());
        if (GlobalStatus.equalTeamID(info.getTeamID())) {
            //intent.putExtra("callType", 0);
            intent.putExtra("callType", 2);
        } else if (callState != null && callState.getState() == GlobalStatus.STATE_CALL) {
            intent.putExtra("callType", 1);
        } else {
            intent.putExtra("callType", 2);
        }
        intent.putExtra("group", info.getTeamID());
        intent.putExtra("type", info.getMemberRole());
        intent.putExtra("group_name", info.getTeamName());
        intent.putExtra("isBBS", true);
        VideoOrVoiceDialog dialog = new VideoOrVoiceDialog(getContext(), intent);
        dialog.show();
        mHandler.sendEmptyMessageDelayed(MSG_PROGRESS_CANCEL, 1000);
    }

    public void fail(int i) {
        new ResponseErrorProcesser(getContext(), i);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Log.d(TAG, "mHandler what = " + msg.what);
            switch (msg.what) {
                case MSG_ENTER_BBS:
                    applyBBS(mBBSList.get(msg.getData().getInt("bbs_grid_position")));
                    break;
                case MSG_JUMP_FIRSTACTIVITY:
                    jumpFirstActivity((TeamInfo) msg.getData().getParcelable("teaminfo_bbs"));
                    break;
                case MSG_PROGRESS_CANCEL:
                    Log.d(TAG, "cancelProgressDialog");
                    hideProgressDialog();
                    SharedPreferencesUtils.put(getContext(), "ptt_not_allowed", false);
                    break;
                case MSG_PROGRESS_DISMISS:
                    Log.d(TAG, "dimissProgressDialog");
                    hideProgressDialog();
                    break;
            }
        }
    };

    private void showProgressDialog() {
        Log.d(TAG, "showProgressDialog");
        /*mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setTitle(getResources().getString(R.string.progress_waiting_to_enter_bbs_title));
        mProgressDialog.setMessage(getResources().getString(R.string.progress_waiting_to_enter_bbs_msg));
        mProgressDialog.show();*/
        if (mTransparentProgressDialog == null) {
            mTransparentProgressDialog = TransparentProgressDialog.createLoadingDialog(this, "loading");
            mTransparentProgressDialog.show();
            mHandler.sendEmptyMessageDelayed(MSG_PROGRESS_DISMISS, 10000);
        }
    }

    private void hideProgressDialog() {
        if (mTransparentProgressDialog != null) {
            mTransparentProgressDialog.dismiss();
            mTransparentProgressDialog = null;
        }
    }
}
