package com.example.jrd48.chat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.SQLite.TeamMemberHelper;
import com.example.jrd48.chat.group.DBHelperTeamList;
import com.example.jrd48.chat.group.DBManagerTeamList;
import com.example.jrd48.chat.group.GroupMemberDetailsActivity;
import com.example.jrd48.chat.group.MsgTool;
import com.example.jrd48.chat.group.ShowTeamInfoPrompt;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.TeamInfoList;
import com.example.jrd48.chat.group.cache.DBHelperChatTimeList;
import com.example.jrd48.chat.group.cache.DBManagerChatTimeList;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.chat.location.ServiceCheckUserEvent;
import com.example.jrd48.chat.sim.ISimStatusListener;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.AutoCloseProcesser;
import com.example.jrd48.service.protocol.root.DismissTeamProcesser;
import com.example.jrd48.service.protocol.root.GroupsListProcesser;
import com.example.jrd48.service.protocol.root.ReceiverProcesser;
import com.example.jrd48.service.protocol.root.SearchFriendProcesser;
import com.example.jrd48.service.protocol.root.TeamMemberProcesser;
import com.ldoublem.loadingviewlib.view.LVCircularRing;
import com.ldoublem.loadingviewlib.view.LVEatBeans;
import com.luobin.dvr.R;
import com.luobin.model.CallState;
import com.luobin.ui.FriendDetailsDialogActivity;
import com.luobin.ui.TalkRoomActivity;
import com.luobin.ui.VideoOrVoiceDialog;
import com.luobin.ui.adapter.ContactsGroupAdapter;
import com.luobin.ui.adapter.ContactsMemberAdapter;
import com.luobin.widget.LoadingDialog;
import com.luobin.widget.PromptDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static com.example.jrd48.chat.crash.MyApplication.getContext;


/**
 * Created by jrd48
 */

public class  TabFragmentLinkGroup extends BaseLazyFragment {
    private static final String TAG = "TabFragmentLinkGroup";
    int i;
    private ListView groupListView;
    private ListView memberListView;
    private ContactsGroupAdapter groupAdapter;
    private ContactsMemberAdapter memberAdapter;
    TextView tvGroupName;

    private PullRefreshLayout pullRefreshLayout;
    private List<Team> groupList = new ArrayList<>();

    private IntentFilter filterRoom;
    private CloseRoomReceiiver closeRoomReceiiver;
    private boolean run = false;
    private long deletTeamID = -1;
    String myPhone;
    private long TIME_GET_MEMBERS_DELAY = 5 * 1000L;
    private static final int MSG_GET_MEMBERS = 2000;
    private static final int QUIT_TEAM = 0;
    private static final int DELETE_TEAM = 1;
    private static final int UPDATE_UI = 0;

    int repeat = 0;
    int index = 0;
    int selectGroupPosition = 0;
    LoadingDialog loadingDialog;

    public boolean isPullRefresh() {
        return isPullRefresh;
    }

    private boolean isPullRefresh = false;

    HashMap<Long,List<TeamMemberInfo> > allMemberMap = new HashMap();

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case UPDATE_UI: //刷新UI
                    isPullRefresh = false;
                    if (pullRefreshLayout != null)
                        pullRefreshLayout.setRefreshing(false);
                    dissmissLoading();
                    if (!isVisible)
                        return;
                    if (groupAdapter == null){
                        if (groupList.size() > 0){
                            groupAdapter = new ContactsGroupAdapter(groupList,allMemberMap,selectGroupPosition,mContext);
                            groupListView.setAdapter(groupAdapter);
                            memberAdapter = new ContactsMemberAdapter(allMemberMap.get(groupList.get(selectGroupPosition).getTeamID()),mContext);
                            memberListView.setAdapter(memberAdapter);
                            tvGroupName.setText(groupList.get(selectGroupPosition).getLinkmanName());
                        }
                    }else{
                        groupAdapter.seteData(groupList);
                        groupAdapter.notifyDataSetChanged();
                        if (selectGroupPosition >= groupList.size()){
                            selectGroupPosition = groupList.size() -1;
                            if (selectGroupPosition < 0){
                                selectGroupPosition = 0;
                            }
                        }
                        if ( groupList.size() > 0){
                            memberAdapter.setData(allMemberMap.get(groupList.get(selectGroupPosition).getTeamID()));
                            memberAdapter.notifyDataSetChanged();
                        }else{//如果当前没有群组，清空群成员列表
                            memberAdapter.setData(new ArrayList<TeamMemberInfo>());
                            memberAdapter.notifyDataSetChanged();
                        }


                    }
                    break;
            }
        }
    };

    private ChatStatusReceiver chatStatusReceiver;

    //群组变化，创建群组等
    private BroadcastReceiver myTeamReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            deletTeamID = intent.getLongExtra("teamID", -1);
            if (deletTeamID == -1) {
                loadTeamListFromNet();
            } else {
//                deleteSQLite();
                getDBMsg();
            }
        }

    };

    //刷新群，加入退去群组等
    private BroadcastReceiver refreshTeamReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadTeamListFromNet();
        }

    };

    //刷新头像
    private BroadcastReceiver changgeImaReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String phone = intent.getStringExtra(ReceiverProcesser.PHONE_NUMBER);
            if (TextUtils.isEmpty(phone)) {
                return;
            }
            getDBMsg();
        }

    };

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        try {
            if (closeRoomReceiiver != null) {
                getContext().unregisterReceiver(closeRoomReceiiver);
            }
            if (myTeamReceiver != null) {
                getContext().unregisterReceiver(myTeamReceiver);
            }
            if (refreshTeamReceiver != null) {
                getContext().unregisterReceiver(refreshTeamReceiver);
            }
            if (changgeImaReceiver != null) {
                getContext().unregisterReceiver(changgeImaReceiver);
            }
            if (chatStatusReceiver != null) {
                getContext().unregisterReceiver(chatStatusReceiver);
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
       /* if (run) {
            getDBMsg();
        }*/

    }

    @Override
    protected void initPrepare() {

    }

    @Override
    protected void onInvisible() {

    }

    @Override
    protected void initData() {
        filterRoom = new IntentFilter();
        closeRoomReceiiver = new CloseRoomReceiiver();
        filterRoom.addAction(AutoCloseProcesser.ACTION);
        filterRoom.addAction(TeamMemberProcesser.ACTION);
        filterRoom.addAction(SearchFriendProcesser.ACTION);
        getContext().registerReceiver(closeRoomReceiiver, filterRoom);

        IntentFilter filt = new IntentFilter();
        chatStatusReceiver = new ChatStatusReceiver();
        filt.addAction("NotifyProcesser.ChatStatus");
        filt.addAction(GlobalStatus.NOTIFY_CALL_ACTION);
        getContext().registerReceiver(chatStatusReceiver, filt);

        initBroadcast();
        myPhone = getMyPhone();
        if (!run) {
             run = true;

             getDBMsg();
        }
    }

    private void showLoading(){
        if (loadingDialog == null){
            loadingDialog = new LoadingDialog(mContext,R.style.loadingDialog);
        }
        if (!loadingDialog.isShowing())
        loadingDialog.show();
    }

    private void dissmissLoading(){
        if (loadingDialog != null)
            loadingDialog.dismiss();
    }

    @Override
    protected View initView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.demolayout, container,
                false);
       dissmissLoading();
        tvGroupName = (TextView) view.findViewById(R.id.group_name);
        if ((boolean) SharedPreferencesUtils.get(mContext, "group_booting", false)) {
            SharedPreferencesUtils.put(mContext, "group_booting", false);
        }
        pullRefreshLayout = (PullRefreshLayout) view.findViewById(R.id.refresh_layout);
        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadTeamListFromNet(false);
            }
        });

        groupListView = (ListView) view.findViewById(R.id.lv_group);
        memberListView = (ListView) view.findViewById(R.id.lv_member);
        groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectGroupPosition = position;
                groupAdapter.setSelectGroupPosition(position);
                groupAdapter.notifyDataSetChanged();
                tvGroupName.setText(groupList.get(selectGroupPosition).getLinkmanName());
                if (memberAdapter != null){
                    memberAdapter.setData(allMemberMap.get(groupList.get(selectGroupPosition).getTeamID()));
                    memberAdapter.notifyDataSetChanged();
                }else{
                    memberAdapter = new ContactsMemberAdapter(allMemberMap.get(groupList.get(selectGroupPosition).getTeamID()),mContext);
                    memberListView.setAdapter(memberAdapter);
                }

               /* Intent intent = new Intent(context,TalkRoomActivity.class);
                intent.putParcelableArrayListExtra("memberList",memberList);
                intent.putExtra("group_name",groupList.get(position).getLinkmanName());
                intent.putExtra("team_id", groupList.get(position).getTeamID());
                intent.putExtra("type", groupList.get(position).getMemberRole());

                CallState callState = GlobalStatus.getCallCallStatus().get(String.valueOf(1) + groupList.get(position).getTeamID());
                if (GlobalStatus.equalTeamID(groupList.get(position).getTeamID())) {
                    intent.putExtra("callType", 0);
                } else if (callState != null && callState.getState() == GlobalStatus.STATE_CALL) {
                    intent.putExtra("callType", 1);
                } else {
                    intent.putExtra("callType", 2);
                }
                startActivity(intent);*/



              /*  Team msg = groupList.get(position);
                Intent intent = new Intent(getContext(), FirstActivity.class);
                intent.putExtra("data", 1);
                CallState callState = GlobalStatus.getCallCallStatus().get(String.valueOf(1) + msg.getTeamID());
                if (GlobalStatus.equalTeamID(msg.getTeamID())) {
                    intent.putExtra("callType", 0);
                } else if (callState != null && callState.getState() == GlobalStatus.STATE_CALL) {
                    intent.putExtra("callType", 1);
                } else {
                    intent.putExtra("callType", 2);
                }
                intent.putExtra("group", msg.getTeamID());
                intent.putExtra("type", msg.getMemberRole());
                intent.putExtra("group_name", msg.getLinkmanName());
                VideoOrVoiceDialog dialog = new VideoOrVoiceDialog(getContext(), intent);
                dialog.show();*/
            }
        });


        groupListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                Team team = groupList.get(position);

               if (team.getMemberRole() == ProtoMessage.TeamRole.Owner_VALUE){
                   //如果是群主，解散群
                   deleteTeamDialog(team.getTeamID(),DELETE_TEAM,team.getLinkmanName());
               }else{
                   //不是群主，退出群
                   deleteTeamDialog(team.getTeamID(),QUIT_TEAM,team.getLinkmanName());
               }


                return true;
            }
        });

        memberListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mContext, FriendDetailsDialogActivity.class);
                Bundle bundle = new Bundle();
                List<TeamMemberInfo> teamMemberInfos = memberAdapter.getData();
                TeamMemberInfo teamMemberInfo = teamMemberInfos.get(position);
                bundle.putString("userPhone", teamMemberInfo.getUserPhone());
                bundle.putString("userName", teamMemberInfo.getUserName());
                intent.putExtra("teamID",groupList.get(selectGroupPosition).getTeamID());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });


        return view;
    }



    /**
     * 获取群组
     */
    private void loadTeamListFromNet(boolean isLoad) {
        if (isLoad){
            showLoading();
        }
        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
        MyService.start(getContext(), ProtoMessage.Cmd.cmdGetTeamList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(GroupsListProcesser.ACTION);
        new TimeoutBroadcast(getContext(), filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                isPullRefresh = false;
                if (pullRefreshLayout != null)
                    pullRefreshLayout.setRefreshing(false);
                ToastR.setToast(getContext(), "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    TeamInfoList list = i.getParcelableExtra("get_group_list");
                    SharedPreferencesUtils.put(getContext(), "data_init", true);
                    convertViewGroupList(list.getmTeamInfo());
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }


    /**
     * 获取群组
     */
    private void loadTeamListFromNet() {
        isPullRefresh = true;
        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
        MyService.start(getContext(), ProtoMessage.Cmd.cmdGetTeamList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(GroupsListProcesser.ACTION);
        new TimeoutBroadcast(getContext(), filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                isPullRefresh = false;
                if (pullRefreshLayout != null)
                    pullRefreshLayout.setRefreshing(false);
                ToastR.setToast(getContext(), "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    //ToastR.setToast(getContext(), "获取群组成功");
                    TeamInfoList list = i.getParcelableExtra("get_group_list");
                    SharedPreferencesUtils.put(getContext(), "data_init", true);
                    convertViewGroupList(list.getmTeamInfo());
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });

    }

    //视频通话状态更新
    class ChatStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(GlobalStatus.NOTIFY_CALL_ACTION)) {

            } else if (intent.hasExtra("chat_status")) {

            }
        }
    }

    public void fail(int i) {
        new ResponseErrorProcesser(getContext(), i);
    }

    /**
     * 退群或者解散群提示框
     */
    public void deleteTeamDialog(final long teamId, final int type, final String teamName) {
        String str = "退出";
        if (type == DELETE_TEAM) {
            str = "解散";
        }

        PromptDialog promptDialog = new PromptDialog(getContext());
        promptDialog.show();
        promptDialog.setTitle("提示：");
        promptDialog.setMessage("确定要" + str + " " + teamName + " 群组？");
        promptDialog.setOkListener(str, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (type == DELETE_TEAM) {
                    groupDelete(teamId);
                } else {
                    groupQuit(teamId);
                }
                dialog.dismiss();
            }
        });
        promptDialog.setCancelListener("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

      /*  AlertDialog.Builder builder = new AlertDialog.Builder(getContext()); // 先得到构造器
        builder.setMessage("确定要" + str + " " + teamName + " 群组？").setTitle("提示：").setPositiveButton("确定", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (type == DELETE_TEAM) {
                    groupDelete(teamId);
                } else {
                    groupQuit(teamId);
                }

            }
        }).setNegativeButton("取消", new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();*/

    }

    /**
     * 退出群组
     */
    private void groupQuit(final long teamId) {
        deletTeamID = teamId;
        ProtoMessage.ApplyTeam.Builder builder = ProtoMessage.ApplyTeam.newBuilder();
        builder.setTeamID(teamId);
        MyService.start(getContext(), ProtoMessage.Cmd.cmdQuitTeam.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(DismissTeamProcesser.ACTION);
        new TimeoutBroadcast(getContext(), filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(getContext(), "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    deleteSQLite();
                    refreshLocalData(teamId);
                    ToastR.setToast(getContext(), "退出群组成功");
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    /**
     * 删除/解散群组
     */
    private void groupDelete(final long l) {
        deletTeamID = l;
        ProtoMessage.AcceptTeam.Builder builder = ProtoMessage.AcceptTeam.newBuilder();
        builder.setTeamID(l);
        MyService.start(getContext(), ProtoMessage.Cmd.cmdDismissTeam.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(DismissTeamProcesser.ACTION);
        new TimeoutBroadcast(getContext(), filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(getContext(), "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    deleteSQLite();
                    refreshLocalData(l);
                    ToastR.setToast(getContext(), "删除群组成功");
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void refreshLocalData(long l) {
        Team team;
        for (int i = 0; i < groupList.size();i++){
             team  = groupList.get(i);
            if (team.getTeamID() == l){
                groupList.remove(team);
                if (selectGroupPosition > groupList.size() -1){
                    selectGroupPosition = groupList.size() -1;
                    if (selectGroupPosition < 0 ){
                        selectGroupPosition = 0;
                    }
                }

               if (groupAdapter != null){
                   groupAdapter.seteData(groupList);
                   groupAdapter.notifyDataSetChanged();
               }

                if (allMemberMap.containsKey(l)){
                    allMemberMap.remove(l);
                    memberAdapter.setData(allMemberMap.get(groupList.get(selectGroupPosition)));
                }
               break;
            }
        }




    }

    /**
     * 数据库有数据，送数据库取，否则从网络取
     */
    private void getDBMsg() {
        isPullRefresh = true;
        try {
            DBManagerTeamList db = new DBManagerTeamList(getContext(), true, DBTableName.getTableName(getContext(), DBHelperTeamList.NAME));
            List<TeamInfo> mTeamInfo = db.getTeams();
            db.closeDB();
            showLoading();
            if (mTeamInfo.size() <= 0) {
                Log.i(ServiceCheckUserEvent.TAG, "get team list = 0");
                loadTeamListFromNet();
            } else {
                convertViewGroupList( mTeamInfo);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.TEAM_ACTION);
        filter.setPriority(Integer.MAX_VALUE);
        getContext().registerReceiver(myTeamReceiver, filter);
        filter = new IntentFilter();
        filter.addAction("ACTION.refreshTeamList");
        getContext().registerReceiver(refreshTeamReceiver, filter);
        filter = new IntentFilter();
        filter.addAction("ACTION.changeImage");
        getContext().registerReceiver(changgeImaReceiver, filter);

    }



    private void deleteSQLite() {
        MsgTool.deleteTeamMsg(getContext(), deletTeamID);
        deletTeamID = -1;
    }

    private void convertViewGroupList(List<TeamInfo> teamInfos) {
        DBManagerChatTimeList chatDB = new DBManagerChatTimeList(getContext(), true, DBTableName.getTableName(getContext(), DBHelperChatTimeList.NAME));
        Map<Long, Long> timeList = chatDB.getTimeList();
        chatDB.closeDB();
        String typedesc;
        String name;
        groupList.clear();
        for (TeamInfo in : teamInfos) {
            int i = in.getMemberRole();
            typedesc = in.getTeamDesc();
            name = in.getTeamName();
            boolean top = isTop(in);
            if (in.getTeamType() == ProtoMessage.TeamType.teamRandom.getNumber()) {
                top = true;
            }
            Team msg = new Team(name, typedesc, in.getTeamID(), in.getMemberRole(), top, in.getTeamType());
            groupList.add(msg);
        }

        // 排序(实现了中英文混排)
        AllTeamPinyinComparator comparator = new AllTeamPinyinComparator(timeList);
        Collections.sort(groupList, comparator);

        getMembersData(groupList);

    }

    private boolean isTop(TeamInfo in) {
        return (boolean) SharedPreferencesUtils.get(getContext(), myPhone + in.getTeamID(), false);
    }

    private void setTop(long l, boolean top) {
        SharedPreferencesUtils.put(getContext(), myPhone + l, top);
    }

    @NonNull
    private String getMyPhone() {
        SharedPreferences preferences = getContext().getSharedPreferences("token", Context.MODE_PRIVATE);
        return preferences.getString("phone", "");
    }


    private void showTeamInfoDilog(List<TeamInfo> allTeamInfos, int id) {
        new ShowTeamInfoPrompt().dialogSTeamInfo(getMyActivity(), allTeamInfos.get(id));
    }

    /**
     * 获取所有群成员
     * @param mTeamInfo
     */
    private void getMembersData(final List<Team> mTeamInfo) {
        repeat = mTeamInfo.size()-1;
        index = 0;
        allMemberMap.clear();
        //观察者  循环获取所有群成员列表
        Observable<List<TeamMemberInfo>> observable = Observable.create(new ObservableOnSubscribe<List<TeamMemberInfo>>() {
            @Override
            public void subscribe(final ObservableEmitter<List<TeamMemberInfo>> emitter) throws Exception {
                Observable.interval(1, TimeUnit.SECONDS).subscribe(new Observer<Long>() {
                    Disposable observableDisposable;
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG,"Observable onSubscribe");
                        observableDisposable = d;
                    }

                    @Override
                    public void onNext(Long aLong) {
                        Log.d(TAG,"Observable onNext index = " + index);
                        if (index > repeat ){
                            Log.d(TAG,"index == repeat onComplete" );
                            emitter.onComplete();
                            observableDisposable.dispose();
                        }else{
                            final Team teamInfo = mTeamInfo.get(index);
                            TeamMemberHelper teamMemberHelper = new TeamMemberHelper(getContext(), teamInfo.getTeamID() + "TeamMember.dp", null);
                            SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
                            final Cursor cursor = db.query("LinkmanMember", null, null, null, null, null, null);
                            Log.d("searchTeamMember", "cursor=" + cursor.getCount());
                            if (cursor == null || cursor.getCount() == 0) {
                                ProtoMessage.AcceptTeam.Builder builder = ProtoMessage.AcceptTeam.newBuilder();
                                builder.setTeamID(teamInfo.getTeamID());
                                MyService.start(mContext, ProtoMessage.Cmd.cmdGetTeamMember.getNumber(), builder.build());
                                IntentFilter filter = new IntentFilter();
                                filter.addAction(TeamMemberProcesser.ACTION);
                                final TimeoutBroadcast b = new TimeoutBroadcast(mContext, filter, getBroadcastManager());
                                b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
                                    @Override
                                    public void onTimeout() {
                                        //ToastR.setToast(mContext, "连接超时");
                                    }
                                    @Override
                                    public void onGot(Intent i) {
                                        Log.d(TAG, "getGroupMan onGot ");
                                        int code = i.getIntExtra("error_code", -1);
                                        if (code ==
                                                ProtoMessage.ErrorCode.OK.getNumber()) {
                                            TeamMemberHelper teamMemberHelper = new TeamMemberHelper(getContext(), teamInfo.getTeamID() + "TeamMember.dp", null);
                                            SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
                                            final Cursor mCursor = db.query("LinkmanMember", null, null, null, null, null, null);
                                            if(mCursor!=null){
                                                List<TeamMemberInfo> memberInfos = new ArrayList<>();
                                                if (mCursor.moveToFirst()){
                                                    do{
                                                        String  user_phone = mCursor.getString(mCursor.getColumnIndex("user_phone"));
                                                        String  user_name = mCursor.getString(mCursor.getColumnIndex("user_name"));
                                                        String  nick_name = mCursor.getString(mCursor.getColumnIndex("nick_name"));
                                                        int  role = mCursor.getInt(mCursor.getColumnIndex("role"));
                                                        int  member_priority = mCursor.getInt(mCursor.getColumnIndex("member_priority"));

                                                        TeamMemberInfo memberInfo = new TeamMemberInfo();
                                                        memberInfo.setUserPhone(user_phone);
                                                        memberInfo.setUserName(user_name);
                                                        memberInfo.setNickName(nick_name);
                                                        memberInfo.setRole(role);
                                                        memberInfo.setMemberPriority(member_priority);
                                                        memberInfos.add(memberInfo);
                                                    }while(mCursor.moveToNext());
                                                }
                                                emitter.onNext(memberInfos);
                                            }

                                        } else {
                                            Log.e(TAG, "getGroupMan groupMan code:" + code);
                                            new ResponseErrorProcesser(mContext, code);
                                        }
                                    }
                                });
                            }else{
                                if(cursor!=null){
                                    List<TeamMemberInfo> memberInfos = new ArrayList<>();

                                    if (cursor.moveToFirst()){
                                        do{
                                            String  user_phone = cursor.getString(cursor.getColumnIndex("user_phone"));
                                            String  user_name = cursor.getString(cursor.getColumnIndex("user_name"));
                                            String  nick_name = cursor.getString(cursor.getColumnIndex("nick_name"));
                                            int  role = cursor.getInt(cursor.getColumnIndex("role"));
                                            int  member_priority = cursor.getInt(cursor.getColumnIndex("member_priority"));

                                            TeamMemberInfo memberInfo = new TeamMemberInfo();
                                            memberInfo.setUserPhone(user_phone);
                                            memberInfo.setUserName(user_name);
                                            memberInfo.setNickName(nick_name);
                                            memberInfo.setRole(role);
                                            memberInfo.setMemberPriority(member_priority);
                                            memberInfos.add(memberInfo);
                                        }while(cursor.moveToNext());
                                        emitter.onNext(memberInfos);
                                    }

                                }
                            }

                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        emitter.onError(e);
                        if (observableDisposable != null)
                            observableDisposable.dispose();
                    }

                    @Override
                    public void onComplete() {
                        if (observableDisposable != null)
                            observableDisposable.dispose();
                    }
                });

            }
        });

        Observer<List<TeamMemberInfo>> observer = new Observer<List<TeamMemberInfo>>() {
            Disposable observerDisposable;
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG,"Observer onSubscribe");
                observerDisposable = d;
            }

            @Override
            public void onNext(List<TeamMemberInfo> teamMemberInfo) {
                Log.d(TAG,"teamMemberInfo =" + teamMemberInfo.size());
                allMemberMap.put(mTeamInfo.get(index).getTeamID(),teamMemberInfo);
                index++;
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,"onError e =" + e.toString());
                if (mHandler!= null){
                    mHandler.sendEmptyMessage(UPDATE_UI);
                }
            }

            @Override
            public void onComplete() {
                Log.d(TAG,"onComplete");
                if (mHandler!= null){
                    mHandler.sendEmptyMessage(UPDATE_UI);
                }
                if (observerDisposable!= null){
                    observerDisposable.dispose();
                }
                index = 0;
            }
        };
        observable.subscribe(observer);
    }

    class CloseRoomReceiiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    private class AllTeamPinyinComparator implements Comparator {
        private Map<Long, Long> chatTime;

        public AllTeamPinyinComparator(Map<Long, Long> time) {
            chatTime = time;
        }

        @Override
        public int compare(Object o1, Object o2) {

            Team contact1 = (Team) o1;
            Team contact2 = (Team) o2;
            long time1 = 0;
            long time2 = 0;
            if (!Build.PRODUCT.contains("LB1728")) {
                if (chatTime != null) {
                    if (chatTime.get(contact1.getTeamID()) != null) {
                        time1 = chatTime.get(contact1.getTeamID());
                    }

                    if (chatTime.get(contact2.getTeamID()) != null) {
                        time2 = chatTime.get(contact2.getTeamID());
                    }

                    if (time1 != 0 || time2 != 0) {
                        if (time1 < time2) {
                            return 1;
                        } else if (time1 == time2) {
                            return 0;
                        } else {
                            return -1;
                        }
                    }
//                Log.e("wsTest","id:" + contact1.getTeamID());
//                Log.e("wsTest","time1:" + time1);
//                Log.e("wsTest","id:" + contact2.getTeamID());
//                Log.e("wsTest","time2:" + time2);
                } else {
//                Log.e("wsTest","chatTime == null");
                }
            }
            String str = contact1.getLinkmanName();
            String str3 = contact2.getLinkmanName();

//            String str1 = Cn2Spell.getPinYin(str);
//            String str2 = Cn2Spell.getPinYin(str3);
            String str1 = ChineseToHanYuPYTest.convertChineseToPinyin(str, false);
            String str2 = ChineseToHanYuPYTest.convertChineseToPinyin(str3, false);
//            Log.e("wsTest", "str1:  " + str1 + "-----------------str2:  " + str2 + "   str 和 str3:   " + str + "---" + str3);
            int flag = str1.compareTo(str2);
//            Log.e("wsTest", "flag:" + flag);

            return flag;
        }
    }


    @Override
    public void onStop() {
        super.onStop();

    }
}