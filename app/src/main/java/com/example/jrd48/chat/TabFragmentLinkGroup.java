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
import com.example.jrd48.service.protocol.root.GroupsListProcesser;
import com.example.jrd48.service.protocol.root.ReceiverProcesser;
import com.example.jrd48.service.protocol.root.SearchFriendProcesser;
import com.example.jrd48.service.protocol.root.TeamMemberProcesser;
import com.luobin.dvr.R;
import com.luobin.ui.adapter.ContactsGroupAdapter;
import com.luobin.ui.adapter.ContactsMemberAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


/**
 * Created by jrd48
 */

public class TabFragmentLinkGroup extends BaseLazyFragment {
    private static final String TAG = "TabFragmentLinkGroup";
    private static int QUIT_TEAM = 0;
    private static int DELETE_TEAM = 1;
    int i;
    private ListView groupListView;
    private ListView memberListView;
    private ContactsGroupAdapter groupAdapter;
    private ContactsMemberAdapter memberAdapter;

    private PullRefreshLayout pullRefreshLayout;
    private List<Team> groupList = new ArrayList<>();

    private TextView tv_wait;
    private ProgressDialog mWaitingProgressDialog;

    private IntentFilter filterRoom;
    private CloseRoomReceiiver closeRoomReceiiver;
    private boolean checkPullRefresh = false;
    private boolean run = false;
    private long deletTeamID = -1;
    String myPhone;
    private long TIME_GET_MEMBERS_DELAY = 5 * 1000L;
    private static final int MSG_GET_MEMBERS = 2000;


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Log.d(TAG, "mHandler what = " + msg.what);
            switch (msg.what) {
                case MSG_GET_MEMBERS:
                    mHandler.removeMessages(MSG_GET_MEMBERS);
                    DBManagerTeamList db = new DBManagerTeamList(getContext(), true, DBTableName.getTableName(getContext(), DBHelperTeamList.NAME));
                    List<TeamInfo> mTeamInfo = db.getTeams();
                    db.closeDB();
                    getMembersData(mTeamInfo);
                    break;
                default:
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
        if (run) {
            getDBMsg();
        }

    }

    @Override
    protected void initPrepare() {

    }

    @Override
    protected void onInvisible() {

    }

    @Override
    protected void initData() {


        tv_wait.setVisibility(View.GONE);
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

    @Override
    protected View initView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.demolayout, container,
                false);
        mWaitingProgressDialog = new ProgressDialog(mContext);
        mWaitingProgressDialog.setMessage(getResources().getString(R.string.waiting_progress_dialog_group_msg));
        mWaitingProgressDialog.setCanceledOnTouchOutside(false);
        mWaitingProgressDialog.setCancelable(false);
        if ((boolean) SharedPreferencesUtils.get(mContext, "group_booting", false)) {
            mWaitingProgressDialog.show();
            SharedPreferencesUtils.put(mContext, "group_booting", false);
        }
        pullRefreshLayout = (PullRefreshLayout) view.findViewById(R.id.refresh_layout);
        tv_wait = (TextView) view.findViewById(R.id.wait);
        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadTeamListFromNet();
            }
        });

        groupListView = (ListView) view.findViewById(R.id.lv_group);
        memberListView = (ListView) view.findViewById(R.id.lv_member);

        return view;
    }

    /**
     * 获取群组
     */
    private void loadTeamListFromNet() {
        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
        MyService.start(getContext(), ProtoMessage.Cmd.cmdGetTeamList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(GroupsListProcesser.ACTION);
        new TimeoutBroadcast(getContext(), filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                if (pullRefreshLayout != null)
                    pullRefreshLayout.setRefreshing(false);
                ToastR.setToast(getContext(), "连接超时");
            }

            @Override
            public void onGot(Intent i) {

                if (pullRefreshLayout != null)
                    pullRefreshLayout.setRefreshing(false);

                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                        ToastR.setToast(getContext(), "获取群组成功");
                        checkPullRefresh = false;
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

  /*  *//**
     * 退群或者解散群提示框
     *//*
    public void deleteTeamDialog(final long teamId, final int type, final String teamName) {
        String str = "退出";
        if (type == DELETE_TEAM) {
            str = "解散";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()); // 先得到构造器
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
        }).create().show();

    }*/

    /**
     * 退出群组
     */
   /* private void groupQuit(final long teamId) {
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
    }*/


    private void getDBMsg() {
        try {
            DBManagerTeamList db = new DBManagerTeamList(getContext(), true, DBTableName.getTableName(getContext(), DBHelperTeamList.NAME));
            List<TeamInfo> mTeamInfo = db.getTeams();
            db.closeDB();
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

    /**
     * 删除/解散群组
     */
  /*  private void groupDelete(final long l) {
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
    }*/

  /*  private void refreshLocalData(long l) {
        int k = -1;
        int i = -1;
        for (TeamInfo mte : allTeamInfos) {
            ++i;
            if (mte.getTeamID() == l) {
                k = i;
                break;
            }
        }
        if (k >= 0) {
            allTeamInfos.remove(k);
            msgList3.remove(k);
        }
        adapter3.notifyDataSetChanged();
    }*/

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

       /* if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(MSG_GET_MEMBERS, TIME_GET_MEMBERS_DELAY);
        }*/



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
    int repeat = 0;
    int index = 0;
    List<List<TeamMemberInfo>> allMemberList = new ArrayList<>();
    private void getMembersData(final List<TeamInfo> mTeamInfo) {
        repeat = mTeamInfo.size()-1;


        Observer<List<TeamMemberInfo>> observer = new Observer<List<TeamMemberInfo>>() {
            @Override
            public void onSubscribe(Disposable d) {
                allMemberList.clear();
            }

            @Override
            public void onNext(List<TeamMemberInfo> teamMemberInfo) {
                Log.d("pangtao","teamMemberInfo =" + teamMemberInfo.size());
                allMemberList.add(teamMemberInfo);
            }

            @Override
            public void onError(Throwable e) {
               Log.d("getMembersData","onError e =" + e.toString());
            }

            @Override
            public void onComplete() {
                if (groupAdapter == null){
                    groupAdapter = new ContactsGroupAdapter(groupList,mContext);
                    groupListView.setAdapter(groupAdapter);
                }else{
                    groupAdapter.seteData(groupList);
                    groupAdapter.notifyDataSetChanged();
                }
            }
        };


        //观察者
        Observable<List<TeamMemberInfo>> observable = Observable.create(new ObservableOnSubscribe<List<TeamMemberInfo>>() {
            @Override
            public void subscribe(final ObservableEmitter<List<TeamMemberInfo>> emitter) throws Exception {
                Observable.interval(100, TimeUnit.SECONDS).subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        Log.d("pangtao","aLong = " + aLong.intValue());
                        if (aLong > repeat){
                            emitter.onComplete();
                        }else{
                            index = aLong.intValue();
                            TeamInfo teamInfo = mTeamInfo.get(index);
                            TeamMemberHelper teamMemberHelper = new TeamMemberHelper(getContext(), teamInfo.getTeamID() + "TeamMember.dp", null);
                            SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
                            Cursor cursor = db.query("LinkmanMember", null, null, null, null, null, null);
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
                                        Log.d("group", "getGroupMan onGot ");
                                        int code = i.getIntExtra("error_code", -1);
                                        if (code ==
                                                ProtoMessage.ErrorCode.OK.getNumber()) {
                                            List<TeamMemberInfo> memberInfos = i.getParcelableArrayListExtra("mTeamInfo");
                                            emitter.onNext(memberInfos);
                                        } else {
                                            Log.e("group", "getGroupMan groupMan code:" + code);
                                            new ResponseErrorProcesser(mContext, code);
                                        }
                                    }
                                });
                            }

                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        emitter.onError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });

            }
        });
        observable.subscribe(observer);


    }

    public class AsyncFetchTeamMemeberTask extends AsyncTask<List<TeamInfo>, Integer, String> {
        public static final String TAG = "AsyncFetchTeamMemeberTask";
        private ISimStatusListener mListener;

        public AsyncFetchTeamMemeberTask() {

        }

        @Override
        protected String doInBackground(List<TeamInfo>... params) {
            if (params != null && params.length > 0) {
                List<TeamInfo> list = params[0];
                try {
                    searchTeamMember(list);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

        }
    }

    public void searchTeamMember(List<TeamInfo> list) {
        for (TeamInfo in : list) {
            TeamMemberHelper teamMemberHelper = new TeamMemberHelper(getContext(), in.getTeamID() + "TeamMember.dp", null);
            SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
            Cursor cursor = db.query("LinkmanMember", null, null, null, null, null, null);
            Log.d("searchTeamMember", "cursor=" + cursor.getCount());
            if (cursor == null || cursor.getCount() == 0) {
                getGroupMan(in.getTeamID());
                break;
            }
        }
        mWaitingProgressDialog.hide();
    }

    public void getGroupMan(final long id) {

        ProtoMessage.AcceptTeam.Builder builder = ProtoMessage.AcceptTeam.newBuilder();
        builder.setTeamID(id);
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
                Log.d("group", "getGroupMan onGot ");
                int code = i.getIntExtra("error_code", -1);
                if (code ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    List<TeamMemberInfo> memberInfos = i.getParcelableArrayListExtra("mTeamInfo");
                    Log.d("pangtao","memberInfos = " + memberInfos.size());
                    /*if (adapter3 != null) {
                        adapter3.notifyDataSetChanged();
                    }*/
                } else {
                    Log.e("group", "getGroupMan groupMan code:" + code);
                    new ResponseErrorProcesser(mContext, code);
                }
            }
        });
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
}