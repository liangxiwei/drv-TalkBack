package com.example.jrd48.chat.group;

import android.app.AlertDialog;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.AllTeamMember;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.FirstActivity;
import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.ImageTool;
import com.example.jrd48.chat.MainActivity;
import com.example.jrd48.chat.PicTool.LetterTileDrawable;
import com.example.jrd48.chat.SQLite.SQLiteTool;
import com.example.jrd48.chat.SQLite.TeamMemberHelper;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.TabFragmentLinkmans;
import com.example.jrd48.chat.TeamMemberInfo;
import com.example.jrd48.chat.TeamMemberInfoList;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.User;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.AppliedFriendsList;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.chat.search.Cn2Spell;
import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.ConnectionChangeReceiver;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.ApplyFriendProcesser;
import com.example.jrd48.service.protocol.root.AssignTeamAdminProcesser;
import com.example.jrd48.service.protocol.root.ChangeTeamMemberNickNameProcesser;
import com.example.jrd48.service.protocol.root.DeleteTeamMemberProcesser;
import com.example.jrd48.service.protocol.root.DismissTeamProcesser;
import com.example.jrd48.service.protocol.root.GetFriendsStatusProcesser;
import com.example.jrd48.service.protocol.root.LiveVideoCallProcesser;
import com.example.jrd48.service.protocol.root.ModifyTeamMemberPriorityProcesser;
import com.example.jrd48.service.protocol.root.NotifyProcesser;
import com.example.jrd48.service.protocol.root.ReceiverProcesser;
import com.example.jrd48.service.protocol.root.SearchFriendProcesser;
import com.example.jrd48.service.protocol.root.SetFriendInfoProcesser;
import com.example.jrd48.service.protocol.root.TeamMemberProcesser;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.luobin.dvr.R;
import com.luobin.model.CallState;
import com.luobin.search.friends.map.TeamMemberLocationActivity;
import com.luobin.tool.OnlineSetTool;
import com.luobin.ui.VideoOrVoiceDialog;
import com.example.jrd48.chat.CircleImageView;
import com.luobin.utils.ButtonUtils;

import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2017/1/16.
 */

public class ShowAllTeamMemberActivity extends BaseActivity implements View.OnClickListener {
    String TAG = "teammember";
    Context mContext;
    private PullToRefreshListView mPullRefreshListView;
    private ListView mListView;
    private allTeamAdapter adapterU;
    private List<AllTeamMember> userList = new ArrayList<>();
    private ArrayList<TeamMemberInfo> allTeamMemberInfos;
    ArrayList<TeamMemberInfo> localTeamMemberInfo;
    private long teamID;
    private String teamName;
    int teamRole;
    private String myPhone;
    private String userNameMe;
    private int type;
    private TextView mTeamName;
    private TextView tvTeamPriority;
    private TextView tvTeamDesc;
    private RelativeLayout mRLTeamDesc;
    private Button mQuitOrDismiss;
    private int role;
    String phone;
    private int size;
    private String typeStr;
    private String changeNickName = "nickName";
    private String changePermission = "permission";
    private String changeMemberPriority = "memberPriority";
    TeamMemberInfo mTeamMemberInfo = null;
    private IntentFilter filter;
    private GetMsgReceiiver getMsgReceiiver;
    List<AppliedFriends> listMembersCache;
    public static String deleteTeam = "删除并退出";
    public static String dismissTeam = "解散";

    private ProgressDialog m_pDialog;
    boolean checkDialog = true;
    boolean showDialog = true;
    private int REQUST_TYPE = 0;
    private int DELETE_FRIEND = 1;
    private int SET_PRIORITY = 2;
    private int SET_MANAGER = 3;
    private int MODIFY_REMARK = 4;
    private int DISMISS_TEAM = 5;
    private int DELETE_TEAM = 6;
    private int GET_TEAM_MEMBER = 7;
    private int ADD_FRIEND = 8;
    public static final int REFRESH_ALL_REMARK = 9;
    public static final int MODIFY_TEAM_MSG = 10;
    public static final int TEAM_MAP = 11;
    TeamInfo mTeamInfo;
    private static final boolean isSHowOnline = false;//show chat
    boolean isShowTalkMember = false;
    //好友在线状态
    Set<String> onlineSet = new HashSet<String>();
    //好友讲话状态
    Set<String> onChatSet = new HashSet<String>();
    public static int REFRESH = 1;
    public static int NOREFRESH = 0;
    public static int INTENT_TYPE = 4;
    public static int DEFAULT = 0;
    int teamType;
    int mTeamType;
    private MyConnectionChangeReceiver myConnectionChangeReceiver;
    private ChatStatusReceiver chatStatusReceiver;
    private boolean isGetFriendStatus;

    List<TeamMemberInfo> mTMInfo = new ArrayList<TeamMemberInfo>();
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Log.d("ttt","获取群成员");
                    getGroupMan(teamID,false);
                    break;
            }

//            super.handleMessage(msg);
        }
    };
//    List<TeamMemberInfo> mTMInfoNew = new ArrayList<TeamMemberInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_member_activity);
        mContext = this;
        getIntentMsg();
        if(isSHowOnline){
            onlineSet.clear();
            onlineSet = OnlineSetTool.getOnlineSet();
            onlineSet.add(myPhone);
        }else{
            if(isShowTalkMember){
                onChatSet = GlobalStatus.getChatList();
            }else{
                onChatSet.clear();
            }
            Log.d(TAG,"isShowTalkMember= "+isShowTalkMember+",onChatSet size ="+onChatSet.size());
        }
        getlistMembersCache();
        initBroadCase();
        initView();
        readSql();
    }

    private void initBroadCase() {
        //设置长时间监听
        filter = new IntentFilter();
        getMsgReceiiver = new GetMsgReceiiver();
        filter.addAction(ReceiverProcesser.ACTION);
        registerReceiver(getMsgReceiiver, filter);

        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.FRIEND_ACTION);
        filter.setPriority(Integer.MAX_VALUE);
        registerReceiver(myReceiver, filter);

        IntentFilter filterDelete = new IntentFilter();
        filterDelete.addAction("ACTION.refreshTeamList");
        registerReceiver(refreshTeamReceiver, filterDelete);

        //注册好友在线状态变化广播
        IntentFilter filterStatus = new IntentFilter();
        filterStatus.addAction(NotifyProcesser.FRIEND_STATUS_ACTION);
        registerReceiver(friendStatus, filterStatus);

        //网络变化广播
        IntentFilter filterNetWork = new IntentFilter();
        myConnectionChangeReceiver = new MyConnectionChangeReceiver();
        filterNetWork.addAction(ConnectionChangeReceiver.NETWORK_CHANGE_ACTION);
        registerReceiver(myConnectionChangeReceiver, filterNetWork);

        //状态变化广播
        IntentFilter filt = new IntentFilter();
        chatStatusReceiver = new ChatStatusReceiver();
        filt.addAction("NotifyProcesser.ChatStatus");
        filt.addAction(GlobalStatus.NOTIFY_CALL_ACTION);
        registerReceiver(chatStatusReceiver, filt);

    }

    private void getlistMembersCache() {
        //获取好友列表
        DBManagerFriendsList db = new DBManagerFriendsList(mContext, DBTableName.getTableName(mContext, DBHelperFriendsList.NAME));
        listMembersCache = db.getFriends(false);
        db.closeDB();

    }

    private void getIntentMsg() {
        Intent intent = getIntent();
        mTeamInfo = intent.getParcelableExtra("team_desc");
        teamType = intent.getIntExtra("team_type", DEFAULT);
        if (mTeamInfo == null) {
            finish();
        }
        teamName = mTeamInfo.getTeamName();
        teamID = mTeamInfo.getTeamID();
        mTeamType = mTeamInfo.getTeamType();
        type = mTeamInfo.getMemberRole();
        localTeamMemberInfo = getTeamMember(teamID);
        size = localTeamMemberInfo.size();
        SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        myPhone = preferences.getString("phone", "");
        userNameMe = preferences.getString("name", myPhone);

//        tvTeamPriority.setText(mTeamInfo.getTeamPriority() + "");
//        tvTeamDesc.setText(mTeamInfo.getTeamDesc());
        if (GlobalStatus.equalTeamID(mTeamInfo.getTeamID())) {
            isShowTalkMember = true;
        } else {
            isShowTalkMember = false;
        }
        setIntent(intent);
    }

    private void initView() {
//        mQuitOrDismiss = (Button) findViewById(R.id.btn_team_quit_or_dismiss);
        mTeamName = (TextView) findViewById(R.id.tv_team_name);
        tvTeamPriority = (TextView) findViewById(R.id.tv_team_priority);
        tvTeamDesc = (TextView) findViewById(R.id.tv_team_desc);
        mRLTeamDesc = (RelativeLayout) findViewById(R.id.team_desc);
        mRLTeamDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (type != ProtoMessage.TeamRole.memberOnly_VALUE) {
                    toModifyTeamMsg();
                }
            }
        });
        if (type != ProtoMessage.TeamRole.memberOnly_VALUE) {
//            mRLTeamDesc.setVisibility(View.VISIBLE);
        }
        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.gv_picture);
        mListView = mPullRefreshListView.getRefreshableView();
        adapterU = new allTeamAdapter(this, userList,mTeamType);
        mListView.setAdapter(adapterU);
        mListView.setSelector(R.drawable.dvr_listview_background);
        mListView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                int position = mListView.getSelectedItemPosition();
                Log.e(TAG,"keyCode:"+keyCode+",event:"+event.getAction() + ",position" + position);
                if(keyCode ==  KeyEvent.KEYCODE_F6 && event.getAction() == KeyEvent.ACTION_DOWN && position > 0){
                    String str = userList.get(position - 1).getPhone();
                    String name = userList.get(position - 1).getName();
                    String nickName = userList.get(position - 1).getNickName();
                    if (str == null || str.length() <= 0) {
//                        onItemType(userList.get(position - 1).getName());
                        return false;
                    }
                    if (!name.equals(AllTeamMember.ADDITIONAL) && !name.equals(AllTeamMember.ADD)) {
                        AllTeamMember tm = userList.get(position - 1);
                        toCall(tm);
                    }
                }
                return false;
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String str = userList.get(position - 1).getPhone();
                String name = userList.get(position - 1).getName();
                String nickName = userList.get(position - 1).getNickName();
                if (str == null || str.length() <= 0) {
                    if (mTeamType == ProtoMessage.TeamType.teamRandom.getNumber()){
                        ToastR.setToast(mContext,"当前为海聊群，不可邀请好友添加");
                    } else {
                        onItemType(userList.get(position - 1).getName());
                    }
                }
                if (name.equals(AllTeamMember.ADDITIONAL) && nickName.equals(AllTeamMember.ADDITIONAL)){
                    String str1 = "";
                    if (role == ProtoMessage.TeamRole.Owner_VALUE) {
                        str1 = getResources().getText(R.string.team_dismiss).toString();
                    } else {
                        str1 = getResources().getText(R.string.team_delete_and_quit).toString();
                    }
                    dismissOrQuit(str1);
                } else if (name.equals(AllTeamMember.ADDITIONAL) &&
                        !nickName.equals(AllTeamMember.ADDITIONAL)) {
                    AllTeamMember mTeamMemberInfo = new AllTeamMember();
                    mTeamMemberInfo.setName(name);
                    mTeamMemberInfo.setPhone(str);
                    mTeamMemberInfo.setNickName(nickName);
                    if (nickName.equals(AllTeamMember.MYNICKNAME)) {
                        for (TeamMemberInfo tm : allTeamMemberInfos) {
                            if (str.equals(tm.getUserPhone())) {
                                String s = tm.getNickName();
                                if (s == null || s.length() <= 0) {
                                    s = tm.getUserName();
                                }
                                mTeamMemberInfo.setNickName(s);
                                break;
                            }
                        }
                    } else if (!nickName.equals(AllTeamMember.MYNICKNAME)) {
                        mTeamMemberInfo.setNickName(nickName);
                    }
                    if (nickName.equals(AllTeamMember.LOOK_MAP)){
                        Intent intent = new Intent(ShowAllTeamMemberActivity.this, TeamMemberLocationActivity.class);
                        intent.putExtra("team_id", mTeamInfo.getTeamID());
                        intent.putExtra("type", mTeamInfo.getMemberRole());
                        intent.putExtra("desc", mTeamInfo.getTeamDesc());
                        intent.putExtra("priority", mTeamInfo.getTeamPriority());
                        intent.putExtra("team_name", mTeamInfo.getTeamName());
                        startActivityForResult(intent,TEAM_MAP);
                    }else {
                        showSetFriendInfoDialog(mTeamMemberInfo);
                    }
                } else if (!name.equals(AllTeamMember.ADDITIONAL) && !name.equals(AllTeamMember.ADD)) {
                    showMenuDilog(userList.get(position - 1));
                }
            }
        });

        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                showDialog = false;
                getGroupMan(teamID,false);
            }
        });
        registerForContextMenu(mListView);

        //********************************************弹窗设置****************************************************
        //创建ProgressDialog对象
        m_pDialog = new ProgressDialog(this,ProgressDialog.THEME_HOLO_LIGHT);
        // 设置进度条风格，风格为圆形，旋转的
        m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // 设置ProgressDialog 提示信息
        m_pDialog.setMessage("请稍等...");
        // 设置ProgressDialog 的进度条是否不明确
        m_pDialog.setIndeterminate(false);
        // 设置ProgressDialog 是否可以按退回按键取消
        m_pDialog.setCancelable(true);
        m_pDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (checkDialog) {
                    getBroadcastManager().stopAll();
                    switch (REQUST_TYPE) {
                        case 1:
                            ToastR.setToast(mContext, "取消删除好友");
                            break;
                        case 2:
                            ToastR.setToast(mContext, "取消设置话权");
                            break;
                        case 3:
                            ToastR.setToast(mContext, "取消设置管理员");
                            break;
                        case 4:
                            ToastR.setToast(mContext, "取消修改昵称");
                            break;
                        case 5:
                            ToastR.setToast(mContext, "取消解散群组");
                            break;
                        case 6:
                            ToastR.setToast(mContext, "取消删除并退出群组");
                            break;
                        case 7:
                            if (showDialog) {
                                ToastR.setToast(mContext, "取消获取群成员");
                            }
                            break;
                        case 8:
                            ToastR.setToast(mContext, "取消添加好友");
                            break;
                    }

                }
            }
        });

    }

    private void toModifyTeamMsg() {
        Intent i = new Intent(mContext, ModifyTeamMsgActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("modify_team_msg", mTeamInfo);
        i.putExtras(bundle);
        startActivityForResult(i, MODIFY_TEAM_MSG);
    }

    /*
       显示菜单列表
    */
    private void showMenuDilog(final AllTeamMember tm) {

        TeamMemberInfo info = new TeamMemberInfo();
        info.setUserName(tm.getName());
        info.setRole(tm.getRole());
        info.setNickName(tm.getNickName());
        info.setMemberPriority(tm.getMemberPriority());
        info.setUserPhone(tm.getPhone());

        Intent intent = new Intent(mContext, GroupMemberDetailsActivity.class);
        Bundle bundle = new Bundle();

        bundle.putParcelable("team_detail", info);
        intent.putExtra("type", REFRESH_ALL_REMARK);
        intent.putExtra("my_phone", myPhone);
        intent.putExtra("teamID", teamID);
        intent.putExtra("role", mTeamInfo.getMemberRole());
        intent.putExtras(bundle);
        startActivityForResult(intent, REFRESH_ALL_REMARK);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case REFRESH_ALL_REMARK:
                if (resultCode == RESULT_OK) {
                    switch (data.getIntExtra("data", 0)) {
                        case 1:
                            isFresh = true;
                            TeamMemberInfo teamMemberInfo = data.getParcelableExtra("team_detail_all");
                            updateLocalData(teamMemberInfo);
                            break;
                        case 2:
                            Intent intent = new Intent();
                            intent.putExtra("data", 2);
                            setResult(RESULT_OK, intent);
                            finish();
                            break;
                        case 3:
                            isFresh = true;
                            String phone = data.getStringExtra("phone");
                            TeamMemberInfo tm = new TeamMemberInfo();
                            tm.setUserPhone(phone);
                            refreshLocalData(tm);
                            break;
                        default:
                            String phonenumber = data.getStringExtra("phone");
                            TeamMemberInfo info = data.getParcelableExtra("team_refresh");
                            if (!TextUtils.isEmpty(phonenumber) && info != null){
                                isFresh = true;
                                updateLocalData(info);
                            }
                            break;
                    }

                }
                break;
            case MODIFY_TEAM_MSG:
                if (resultCode == RESULT_OK) {
                    if (data.getIntExtra("data", 0) == 1) {
                        isFresh = true;
                        teamName = data.getStringExtra("teamName");
                        String teamDesc = data.getStringExtra("teamDesc");
                        int priority = data.getIntExtra("priority", 0);
                        mTeamInfo.setTeamName(teamName);
                        mTeamInfo.setTeamPriority(priority);
                        mTeamInfo.setTeamDesc(teamDesc);
                        refreshTeamName();
                        tvTeamDesc.setText(data.getStringExtra("teamDesc"));
                        tvTeamPriority.setText(data.getIntExtra("priority", 0) + "");
//                        TeamInfo teamInfo = data.getParcelableExtra("modify_team");
                    }
                }
                break;
            case TEAM_MAP:
                if (resultCode == RESULT_OK) {
                    if (data.getIntExtra("data", 0) == 1) {
                        getlistMembersCache();
                        ArrayList<TeamMemberInfo> mTeamMemberInfoList = data.getParcelableArrayListExtra("team_detail_map");
                        size = mTeamMemberInfoList.size();
                        convertViewTeamMember(mTeamMemberInfoList,true);
                        refreshTeamName();
                        refreshStatus();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void refreshStatus(){
        if(adapterU != null)
            adapterU.clearOnlineStatus();
        if(isSHowOnline){
            if (onlineSet.size() > 0) {
                Iterator setList = onlineSet.iterator();
                while(setList.hasNext()) {
                    String phone = (String) setList.next();
                    setAdapterItemStatus(phone);
                }
            }
        }else{
            if (onChatSet.size() > 0) {
                Iterator setList = onChatSet.iterator();
                while (setList.hasNext()) {
                    String phone = (String) setList.next();
                    setAdapterItemStatus(phone);
                }
            }
        }
        allTeamSort();
    }


    private void updateLocalData(TeamMemberInfo teamMemberInfo) {
        for (AllTeamMember at : userList) {
            if (at.getPhone().equals(teamMemberInfo.getUserPhone())&& teamMemberInfo.getUserName().equals(at.getName())) {
                if (TextUtils.isEmpty(teamMemberInfo.getNickName())){
                    at.setNickName(teamMemberInfo.getUserName());
                }else {
                    at.setNickName(teamMemberInfo.getNickName());
                }
                at.setRole(teamMemberInfo.getRole());
                at.setMemberPriority(teamMemberInfo.getMemberPriority());
                break;
            }
        }
        for (AllTeamMember at : userList) {
            if (!at.getNickName().equals(AllTeamMember.ADDITIONAL) && at.getName().equals(AllTeamMember.ADDITIONAL) && teamMemberInfo.getUserPhone().equals(at.getPhone())) {
                at.setNickName(teamMemberInfo.getNickName());
                break;
            }
        }
        refreshStatus();
        /*Iterator<AllTeamMember> iterator = userList.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            AllTeamMember k = iterator.next();
            if (k.getName().equals(AllTeamMember.ADD) || k.getName().equals(AllTeamMember.ADDITIONAL)) {
                i++;
                iterator.remove();
                if (i == 4) {
                    break;
                }
            }
        }*/
        // 排序(实现了中英文混排)
//        AllTeamPinyinComparator comparator = new AllTeamPinyinComparator();
//        Collections.sort(userList, comparator);
       /* String str = AllTeamMember.MYNICKNAME;
        for (AllTeamMember at : userList) {
            if (at.getPhone().equals(myPhone)) {
                str = at.getNickName();
                break;
            }
        }

        userT(AllTeamMember.ADD, 0, "", 0, 0, "", -1);

//            if (phone.equals(myPhone)){
//                str = remark;
//            }
        userT(AllTeamMember.ADDITIONAL, 0, myPhone, 0, 0, str, -2);
        userT(AllTeamMember.ADDITIONAL, 0, myPhone, 0, 0, AllTeamMember.LOOK_MAP, -3);
        userT(AllTeamMember.ADDITIONAL, 0, myPhone, 0, 0, AllTeamMember.ADDITIONAL, -4);

        adapterU.notifyDataSetChanged();*/
    }

    private void showChangePriorityDilog(final AllTeamMember tm) {

        new ModifyPriorityPrompt().dialogModifyPriorityRequest(mContext, "话权", tm.getMemberPriority(), new ModifyPrioritytListener() {
            @Override
            public void onOk(int data) {
                toChangePriority(data, tm);
            }
        });

    }

    private void showDeleteDialog(final TeamMemberInfo tm) {
//		cmdDeleteTeamMember
        String msg = tm.getUserName();
        if (msg == null || msg.equals("")) {
            msg = tm.getUserPhone();
        }
        new AlertDialog.Builder(mContext).setTitle("提示：")// 提示框标题
                .setMessage("确定要删除" + msg + "?").setPositiveButton("确定", // 提示框的两个按钮
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deleteTeamMember(tm);
                        dialog.dismiss();
                    }

                }).setNegativeButton("取消", new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();

    }
    //*****************************************设置群成员显示********************************************

    /**
     * 删除群成员
     *
     * @param tm
     */
    public void deleteTeamMember(final TeamMemberInfo tm) {
        showMyDialog(DELETE_FRIEND);
        ProtoMessage.AcceptTeam.Builder builder = ProtoMessage.AcceptTeam.newBuilder();
        builder.setTeamID(teamID);
        builder.setPhoneNum(tm.getUserPhone());
        MyService.start(mContext, ProtoMessage.Cmd.cmdDeleteTeamMember.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(DeleteTeamMemberProcesser.ACTION);
        new TimeoutBroadcast(ShowAllTeamMemberActivity.this, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                dismissDialogShow();
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                dismissDialogShow();
                int code = i.getIntExtra("error_code", -1);
                if (code ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    refreshLocalData(tm);
                    ToastR.setToast(mContext, "删除成员成功");
                    //	ToastR.setToast(FirstActivity.this, "获取群成员成功");
                } else {
                    Log.e(TAG," delete code:"+code);
                    new ResponseErrorProcesser(mContext, code);
                }
            }
        });
    }

    private void refreshLocalData(TeamMemberInfo tm) {
        int k = -1;
        int i = -1;
        for (TeamMemberInfo mte : allTeamMemberInfos) {
            ++i;
            if (tm.getUserPhone().equals(mte.getUserPhone())) {
                k = i;
                break;
            }
        }
        if (k >= 0) {
            allTeamMemberInfos.remove(k);
            convertViewTeamMember(allTeamMemberInfos,true);
        }
        refreshStatus();
    }

    /*
    设置话权
     */
    private void toChangePriority(final int priority, final AllTeamMember tm) {
        showMyDialog(SET_PRIORITY);
        ProtoMessage.TeamMember.Builder builder = ProtoMessage.TeamMember.newBuilder();
        builder.setUserPhone(tm.getPhone());
        builder.setMemberPriority(priority);
        builder.setTeamID(teamID);
        MyService.start(mContext, ProtoMessage.Cmd.cmdModifyTeamMemberPriority.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(ModifyTeamMemberPriorityProcesser.ACTION);
        new TimeoutBroadcast(ShowAllTeamMemberActivity.this, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                dismissDialogShow();
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                dismissDialogShow();
                int code = i.getIntExtra("error_code", -1);
                if (code ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(mContext, "话权修改成功");
                    updateLocalData("", tm.getPhone(), changeMemberPriority, priority);
                } else {
                    Log.e(TAG," changePriority code:"+code);
                    new ResponseErrorProcesser(mContext, code);
                }
            }
        });

    }

    //***********************设置权限*******************************
    private void setPermissions() {
        LayoutInflater factory = LayoutInflater.from(mContext);// 提示框
        final View view = factory.inflate(R.layout.set_member_permissions, null);
        final RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
        final RadioButton radioMember = (RadioButton) view.findViewById(R.id.radio_member);
        final RadioButton radioManager = (RadioButton) view.findViewById(R.id.radio_manager);
        if (mTeamMemberInfo == null) {
            return;
        }
        if (mTeamMemberInfo.getRole() == ProtoMessage.TeamRole.memberOnly_VALUE) {
            radioMember.setChecked(true);
        } else {
            radioManager.setChecked(true);
        }

        new AlertDialog.Builder(mContext).setTitle("设置管理员")// 提示框标题
                .setView(view).setPositiveButton("确定", // 提示框的两个按钮
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (radioMember.isChecked()) {
                            teamRole = ProtoMessage.TeamRole.memberOnly_VALUE;
                        } else if (radioManager.isChecked()) {
                            teamRole = ProtoMessage.TeamRole.Manager_VALUE;
                        }
                        toSendChangePermission(teamRole, mTeamMemberInfo);
                        dialog.dismiss();
                    }

                }).setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }
    //**********************退出键判断***********************************

    //设置管理权限
    private void toSendChangePermission(final int role, final TeamMemberInfo info) {
        showMyDialog(SET_MANAGER);
        ProtoMessage.AssignTeamAdmin.Builder builder = ProtoMessage.AssignTeamAdmin.newBuilder();
        builder.setTeamID(teamID);
        builder.setPhoneNum(info.getUserPhone());
        builder.setAdmin(role);
        MyService.start(mContext, ProtoMessage.Cmd.cmdAssignTeamAdmin.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(AssignTeamAdminProcesser.ACTION);
        new TimeoutBroadcast(ShowAllTeamMemberActivity.this, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                dismissDialogShow();
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                dismissDialogShow();
                int code = i.getIntExtra("error_code", -1);
                if (code ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    mTeamMemberInfo.setRole(role);
                    updateLocalData("", info.getUserPhone(), changePermission, role);
                    ToastR.setToast(mContext, "修改权限成功");
                } else {
                    Log.e(TAG," changePermission code:"+code);
                    new ResponseErrorProcesser(mContext, code);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        GlobalStatus.setIsFirstPause(false);
        refreshTeamName();
        refrshChatStatus();
    }


    //获取好友状态
    private void loadFriendStatus() {

        if (ConnUtil.isConnected(mContext)) {
            Log.d(TAG, "获取全部好友在线状态");
            ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
            MyService.start(mContext, ProtoMessage.Cmd.cmdGetFriendsStatus.getNumber(), builder.build());
            IntentFilter filter = new IntentFilter();
            filter.addAction(GetFriendsStatusProcesser.ACTION);
            new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

                @Override
                public void onTimeout() {
                    Log.w(TAG,"获取好友在线状态连接超时");
                    if (!isGetFriendStatus){
                        isGetFriendStatus = true;
                        loadFriendStatus();
                    }
                    //  ToastR.setToast(getContext(), "连接超时");
                }

                @Override
                public void onGot(Intent i) {
                    int code = i.getIntExtra("error_code", -1);
                    if (code == ProtoMessage.ErrorCode.OK.getNumber()) {
//                        ToastR.setToast(getContext(), "获取在线好友成功");
                        ProtoMessage.MsgFriendsStatus re =
                                (ProtoMessage.MsgFriendsStatus) i.getSerializableExtra(GetFriendsStatusProcesser.STATUS_KEY);
                        Log.d(TAG,"获取好友在线状态成功："+re.getFriendsList());
                        OnlineSetTool.addList(re.getFriendsList());
                        refreshLocalFriendStatus(re);
                    } else {
                        Log.e(TAG,"error code:"+code);
                        if (code == ProtoMessage.ErrorCode.NOT_LOGIN_VALUE && !isGetFriendStatus){
                            isGetFriendStatus = true;
                            loadFriendStatus();
                        }
                      //  fail(code);
                    }
                }
            });
        }
    }

    private void refreshLocalFriendStatus(ProtoMessage.MsgFriendsStatus re) {
        onlineSet.clear();
        adapterU.clearOnlineStatus();

        if (re.getFriendsList().size() > 0) {
            for (int i = 0; i < re.getFriendsList().size(); i++) {
                String phone = re.getFriendsList().get(i).getPhoneNum();
                onlineSet.add(phone);
                setAdapterItemStatus(phone);

            }
        }
        onlineSet.add(myPhone);
        setAdapterItemStatus(myPhone);
        allTeamSort();
    }

    private void setAdapterItemStatus(String phone) {
        AllTeamMember x = adapterU.getItemByPhoneNum(phone);
        if(isSHowOnline){
            if (x != null) {
                x.setOnline(true);
                Log.d(TAG, "在线好友状态：" + phone);
            } else {
                Log.w(TAG, "好友在线状态，未找到号码 phone:"+phone);
            }
        }else{
            if (x != null) {
                x.setOnChat(true);
                Log.d(TAG, "对讲中好友状态：" + phone);
            } else {
                Log.w(TAG, "对讲中好友状态，未找到号码 phone:"+phone);
            }
        }
    }

    private void allTeamSort() {
        if (adapterU.getList() != null) {
            final AllTeamPinyinComparator comparator = new AllTeamPinyinComparator();
            Collections.sort(adapterU.getList(), new Comparator<AllTeamMember>() {
                @Override
                public int compare(AllTeamMember x, AllTeamMember y) {
                    if(isSHowOnline){
                        if (x.isOnline()!=y.isOnline()){
                            if (x.isOnline()){
                                return -1;
                            } else {
                                return 1;
                            }
                        }
                    }else{
                        if (x.isOnChat()!=y.isOnChat()){
                            if (x.isOnChat()){
                                return -1;
                            } else {
                                return 1;
                            }
                        }
                    }

                    return comparator.compare(x, y);
                }
            });
        }
        adapterU.notifyDataSetChanged();
    }

    private BroadcastReceiver friendStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "收到好友在线广播 。。。。");
            String phone = intent.getStringExtra(NotifyProcesser.NUMBER);
            boolean online = intent.getBooleanExtra(NotifyProcesser.ONLINE_KEY, false);
            if (phone != null) {

                if (online) {
                    onlineSet.add(phone);
                } else {
                    onlineSet.remove(phone);
                }
                Log.d(TAG, "收到好友在线广播: " + phone + ", online: " + online);
                AllTeamMember x = adapterU.getItemByPhoneNum(phone);
                if (x != null) {
                    x.setOnline(online);
                    allTeamSort();
                } else {
                    Log.w(TAG, "not find in list: " + phone);
                }
            } else {
                Log.w(TAG, "获取号码为空");
            }
        }
    };


    private void refreshTeamName() {
        WindowManager wm = getWindowManager();
        int width = wm.getDefaultDisplay().getWidth();

        CharSequence ellipsizeStr = null;
        if (size > 0) {
            String memberCount;
            int onLine = GlobalStatus.getOnlineCount();
            if(isShowTalkMember && onLine > 0){
                memberCount = " (" + onLine + "/" + size + ")";
            }else{
                memberCount = " (" + size + ")";
            }
            float countWidth = getTextWidth(mContext,memberCount,mTeamName.getPaint()) + 10;
            ellipsizeStr =  TextUtils.ellipsize(teamName, mTeamName.getPaint(), width - countWidth, TextUtils.TruncateAt.END);
            mTeamName.setText(ellipsizeStr + memberCount);
        } else {
            ellipsizeStr =  TextUtils.ellipsize(teamName, mTeamName.getPaint(), width - 10, TextUtils.TruncateAt.END);
            mTeamName.setText(ellipsizeStr);
        }
    }

    public float getTextWidth(Context context, String text, TextPaint paint){
        return paint.measureText(text);
    }

    @Override
    protected void onPause() {
        GlobalStatus.setIsFirstPause(true);
        super.onPause();
        refreshComplete();
    }

    private void onItemType(String str) {
        if (str.equals(User.ADD)) {

//            //获取好友列表
//            DBManagerFriendsList db = new DBManagerFriendsList(mContext, DBTableName.getTableName(mContext, DBHelperFriendsList.NAME));
//            List<AppliedFriends> list = db.getFriends(false);
//            db.closeDB();

            AppliedFriendsList afList = new AppliedFriendsList();
            List<AppliedFriends> appliedList = new ArrayList<AppliedFriends>();
            for (AppliedFriends info : listMembersCache) {
                int t = 0;
                for (int i = 0; i < allTeamMemberInfos.size(); i++) {
                    if (!allTeamMemberInfos.get(i).getUserPhone().equals(info.getPhoneNum())) {
                        ++t;
                        if (t == allTeamMemberInfos.size()) {
                            appliedList.add(info);
                        }
                    } else {
                        break;
                    }
                }
            }
            if (appliedList.size() <= 0) {
                ToastR.setToast(getApplicationContext(), "请添加新的好友再来邀请好友");
            } else {
                afList.setAppliedFriends(appliedList);
                sendIntentMsg(afList);
            }
        }
    }

    private void sendIntentMsg(AppliedFriendsList afList) {

        Intent i = new Intent(mContext, InviteJoinGroupActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("apply_member", afList);
        i.putExtras(bundle);
        i.putExtra("teamID", teamID);
        startActivity(i);
        //finish();
    }

    /**
     * 按钮onClick事件重写
     *
     * @param view
     */
    public void back(View view) {
        resultData();
    }

    /**
     * 重写返回键功能
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // 这里重写返回键
            resultData();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    boolean isFresh = false;

    public void resultData() {
        //刷新返回群聊界面
        if (isFresh && teamType == INTENT_TYPE) {
            Intent intent = new Intent();
            intent.putExtra("refresh", REFRESH);
            setResult(RESULT_OK, intent);
        }
        //不刷新返回群聊界面
        if (!isFresh && teamType == INTENT_TYPE) {
            Intent intent = new Intent();
            intent.putExtra("refresh", NOREFRESH);
            setResult(RESULT_OK, intent);
        }

        finish();
    }

    private void readSql() {
        getGroupMan(teamID,true);
        convertViewTeamMember(localTeamMemberInfo,true);
    }

    public void getGroupMan(final long id,final boolean isShow) {
        if (!ConnUtil.isConnected(this)) {
            ToastR.setToast(this,"当前没有网络，请检查网络是否连接！");
            OnlineSetTool.removeAll();
            if (mPullRefreshListView != null) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshComplete();
                    }
                }, 1000);
            }
            return;
        }
        if (isShow) {
            REQUST_TYPE = GET_TEAM_MEMBER;
            checkDialog = true;
//            m_pDialog.show();
        }
        ProtoMessage.AcceptTeam.Builder builder = ProtoMessage.AcceptTeam.newBuilder();
        builder.setTeamID(id);
        MyService.start(mContext, ProtoMessage.Cmd.cmdGetTeamMember.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(TeamMemberProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(ShowAllTeamMemberActivity.this, filter, getBroadcastManager());
        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                refreshComplete();
                if (isShow) {
                    dismissDialog();
                }
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                refreshComplete();
                if (isShow) {
                    dismissDialog();
                }
                int code = i.getIntExtra("error_code", -1);
                if (code ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
//                    TeamMemberInfoList list = i.getParcelableExtra("get_teamMember_list");
                    getlistMembersCache();
                    ArrayList<TeamMemberInfo> teamMemberInfos = SQLiteTool.getAllTeamMembers(mContext, id);
                    size = teamMemberInfos.size();
                    convertViewTeamMember(teamMemberInfos,true);
                    refreshTeamName();
                    if (userList != null && userList.size() > 0) {
                        if(isSHowOnline){
                            loadFriendStatus();
                        }else {
                            refrshChatStatus();
                        }
                    }
                    //	ToastR.setToast(FirstActivity.this, "获取群成员成功");
                } else {
                    Log.e(TAG," groupMan code:"+code);
                    new ResponseErrorProcesser(mContext, code);
                }
            }
        });
    }

    private void refreshComplete() {
        if (mPullRefreshListView != null) {
            mPullRefreshListView.onRefreshComplete();
        }
    }

    private void dismissDialog() {
        showDialog = true;
        checkDialog = false;
        m_pDialog.dismiss();
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        // listAdapter.getCount()返回数据项的数目
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }

    private void convertViewTeamMember(ArrayList<TeamMemberInfo> teamMemberInfos,boolean isFresh) {
        if (teamMemberInfos == null || teamMemberInfos.size() <= 0){
            return;
        }
        userList.clear();
        allTeamMemberInfos = teamMemberInfos;//保存群成员列表
        String memberName;
        int sex;
        boolean isFriend;
        for (TeamMemberInfo in : teamMemberInfos) {
            memberName = null;
            sex = 0;
            isFriend = false;
            if (listMembersCache.size() > 0) {
                for (AppliedFriends appliedFriends : listMembersCache) {
                    if (appliedFriends.getPhoneNum().equals(in.getUserPhone())) {
                        memberName = appliedFriends.getNickName();
                        sex = appliedFriends.getUserSex();
                        isFriend = true;
                        break;
                    }
                }
            }

            if (myPhone.equals(in.getUserPhone())) {
                mTeamInfo.setMemberRole(in.getRole());
                type = in.getRole();
            }

            if (memberName == null || memberName.equals("")) {
                memberName = in.getNickName();
            }
            if (memberName == null || memberName.equals("")) {
                memberName = in.getUserName();
            }
            if (memberName == null || memberName.equals("")) {
                memberName = in.getUserPhone();
            }
            Bitmap bitmap = GlobalImg.getImage(mContext, in.getUserPhone());
            if (bitmap == null) {
//                getUserFace(in.getUserPhone());
            }
            int status = ProtoMessage.ChatStatus.csOk_VALUE;
            userT(in.getUserName(),isFriend, status, in.getUserPhone(), sex, in.getMemberPriority(), memberName, in.getRole());

        }
        // 排序(实现了中英文混排)
//        AllTeamPinyinComparator comparator = new AllTeamPinyinComparator();
//        Collections.sort(userList, comparator);

        userT(AllTeamMember.ADD,false, 0, "", 0, 0, "", -1);
        userT(AllTeamMember.ADDITIONAL,false, 0, myPhone, 0, 0, AllTeamMember.MYNICKNAME, -2);
        userT(AllTeamMember.ADDITIONAL,false, 0, myPhone, 0, 0, AllTeamMember.LOOK_MAP, -3);
        userT(AllTeamMember.ADDITIONAL, false,0, myPhone, 0, 0, AllTeamMember.ADDITIONAL, -4);

        mTMInfo.clear();
        for (TeamMemberInfo te : allTeamMemberInfos) {
            for (AppliedFriends af : listMembersCache) {
                if (af.getPhoneNum().equals(te.getUserPhone())) {
                    mTMInfo.add(te);
                    break;
                }
            }
        }

        sortListByCompate();
//        adapterU.notifyDataSetChanged();
        if (isFresh) {
            adapterU.refresh(userList);
        }
    }
    /**
     * // 排序(实现了中英文混排)
     * */
    private void sortListByCompate() {
        final AllTeamPinyinComparator comparator = new AllTeamPinyinComparator();
        Collections.sort(userList, new Comparator<AllTeamMember>() {
            @Override
            public int compare(AllTeamMember x, AllTeamMember y) {
                if(isSHowOnline){
                    if (x.isOnline()!=y.isOnline()){
                        if (x.isOnline()){
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                }else{
                    if (x.isOnChat()!=y.isOnChat()){
                        if (x.isOnChat()){
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                }
                return comparator.compare(x, y);
            }
        });
    }

    public void userT(String name,boolean isFriend, int state, String phone, int sex, int memberPriority, String nickName, int role) {
        AllTeamMember user = new AllTeamMember(state,isFriend, name, phone, sex, memberPriority, nickName, role);
        if (role >= 0 && !TextUtils.isEmpty(phone)){
            if(isSHowOnline){
                if(onlineSet.contains(phone)){
                    user.setOnline(true);
                }else {
                    user.setOnline(false);
                }
            }else {
                if(onChatSet.contains(phone)){
                    user.setOnChat(true);
                }else{
                    user.setOnChat(false);
                }
            }
        }else {
            user.setOnline(false);
            user.setOnChat(false);
        }
        userList.add(user);
    }

    public void getUserFace(String memberName) {
        ProtoMessage.SearchUser.Builder builder = ProtoMessage.SearchUser.newBuilder();
        builder.setPhoneNum(memberName);
        builder.setOnlyPhoneNum(true);
        MyService.start(mContext, ProtoMessage.Cmd.cmdSearchUser.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(SearchFriendProcesser.ACTION);
        new TimeoutBroadcast(ShowAllTeamMemberActivity.this, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
            }

            @Override
            public void onGot(Intent i) {
                int code = i.getIntExtra("error_code", -1);
                if (code ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    AppliedFriends aply = i.getParcelableExtra("search_user");
                    if (aply.getPhoneNum() == null || aply.getPhoneNum().length() <= 0) {
//                        ToastR.setToast(mContext, "未找到该用户");
                    } else {
//                        msgDB_Team();
                        adapterU.notifyDataSetChanged();
                    }
                } else {
                    Log.e(TAG," userFace code:"+code);
                 //   new ResponseErrorProcesser(mContext, code);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            default:
                break;
        }
    }

    public class allTeamAdapter extends BaseAdapter {

        int friendsIndex = 0;
        private List<AllTeamMember> mArrayList;
        private Context context;
        private LayoutInflater layoutInflater;
        int mTeamType;

        public allTeamAdapter(Context context, List<AllTeamMember> mFriend , int teamType) {
            layoutInflater = LayoutInflater.from(context);
            this.context = context;
            this.mArrayList = mFriend;
            mTeamType = teamType;
        }

        public List<AllTeamMember> getList() {
            return mArrayList;
        }

        // 刷新适配器
        public void refresh(List<AllTeamMember> mArryFriend) {
            this.mArrayList = mArryFriend;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            int count = 0;
            if (null != mArrayList) {
                count = mArrayList.size();
            }
            return count;
        }

        public AllTeamMember getItemByPhoneNum(String phoneNum){
            for (int i=0; mArrayList!=null && i<mArrayList.size(); ++i){
                if (mArrayList.get(i).getPhone().equals(phoneNum)) {
//                    Log.d("jim","getItemByPhoneNum list size"+mArrayList.size());
                    return mArrayList.get(i);
                }
            }
            return null;
        }

        public void clearOnlineStatus(){
//            Log.d("jim","clearOnlineStatus list size "+(mArrayList == null ?null:mArrayList.size()));
            for (int i=0; mArrayList!=null && i<mArrayList.size(); ++i){
//                Log.d("jim","clearOnlineStatus phone number "+mArrayList.get(i).getPhone());
                mArrayList.get(i).setOnline(false);
                mArrayList.get(i).setOnChat(false);
            }
        }

        @Override
        public AllTeamMember getItem(int position) {
            AllTeamMember item = null;
            if (null != mArrayList) {
                item = mArrayList.get(position);
            }
            return item;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final AllTeamMember user = mArrayList.get(position);
            final ViewHolder holder = new ViewHolder();
            View view;
            if (user.getName().equals(AllTeamMember.ADD)) {
                view = layoutInflater.inflate(R.layout.member_singleuser_add, null);
                holder.imageInviteAdd = (ImageView) view.findViewById(R.id.iv_invite_add);
                if (mTeamType == ProtoMessage.TeamType.teamRandom.getNumber()){
                    holder.imageInviteAdd.setImageDrawable(getDrawable(R.drawable.ic_add_button_not_clickable));
                } else {
                    holder.imageInviteAdd.setImageDrawable(getDrawable(R.drawable.ic_add_button));
                }
                view.setTag(null);
                return view;
            } else if (user.getName().equals(AllTeamMember.ADDITIONAL)) {
                //TODO 在添加一个 查看地图的xml
                convertView = layoutInflater.inflate(R.layout.member_singleuser_delete, null);
                holder.mRelativeLayout = (RelativeLayout) convertView.findViewById(R.id.rl_nick_name);
                holder.mButton = (TextView) convertView.findViewById(R.id.btn_team_quit_or_dismiss);
                holder.nickName = (TextView) convertView.findViewById(R.id.tv_nick_name);
                holder.mLinearLayoutMapLook = (RelativeLayout) convertView.findViewById(R.id.look_map_layout);
                convertView.setTag(null);
                TeamMemberInfo info = null;
                for (TeamMemberInfo tm : allTeamMemberInfos) {
                    if (tm.getUserPhone().equals(user.getPhone())) {
                        info = tm;
                        break;
                    }
                }
                if (info != null && info.getUserPhone().length() > 0) {

                    if (user.getNickName().equals(AllTeamMember.ADDITIONAL)) {
                        if (info.getRole() == ProtoMessage.TeamRole.Owner_VALUE) {
                            role = ProtoMessage.TeamRole.Owner_VALUE;
                            holder.mButton.setText(getResources().getText(R.string.team_dismiss));
                        } else {
                            role = 0;
                            holder.mButton.setText(getResources().getText(R.string.team_delete_and_quit));
                        }
                        holder.mRelativeLayout.setVisibility(View.GONE);
                        holder.mLinearLayoutMapLook.setVisibility(View.GONE);
                        holder.mButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dismissOrQuit(holder.mButton.getText().toString());
                            }
                        });


                    } else if (user.getNickName().equals(AllTeamMember.LOOK_MAP)) {
                        holder.mButton.setVisibility(View.GONE);
                        holder.mRelativeLayout.setVisibility(View.GONE);
                        holder.mLinearLayoutMapLook.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //TODO  地图查看
                                Intent intent = new Intent(ShowAllTeamMemberActivity.this, TeamMemberLocationActivity.class);
                                intent.putExtra("team_id", mTeamInfo.getTeamID());
                                intent.putExtra("type", mTeamInfo.getMemberRole());
                                intent.putExtra("desc", mTeamInfo.getTeamDesc());
                                intent.putExtra("priority", mTeamInfo.getTeamPriority());
                                String name = mTeamInfo.getTeamName();
                                intent.putExtra("team_name", name);
                                startActivityForResult(intent,TEAM_MAP);

                            }
                        });

                    }else {
                        holder.mLinearLayoutMapLook.setVisibility(View.GONE);
                        holder.mButton.setVisibility(View.GONE);
                        if (user.getNickName().equals(AllTeamMember.MYNICKNAME)) {
                            String name = info.getNickName();
                            if (name.length() <= 0) {
                                name = info.getUserName();
                            }
                            if (name.length() <= 0) {
                                name = info.getUserPhone();
                            }
                            holder.nickName.setText(name);
                        } else {
                            holder.nickName.setText(user.getNickName());
                        }
                    }
                }
                return convertView;

            } else if (!user.getName().equals(AllTeamMember.ADD) && !user.getName().equals(AllTeamMember.ADDITIONAL)) {
                convertView = layoutInflater.inflate(R.layout.member_singleuser, null);
                holder.name = (TextView) convertView.findViewById(R.id.user_name);
                holder.role = (TextView) convertView.findViewById(R.id.user_role);
//                holder.discourseCompetence = (TextView) convertView.findViewById(R.id.user_discourse_competence);
                holder.phone = (TextView) convertView.findViewById(R.id.user_phone);
                holder.image = (ImageView) convertView.findViewById(R.id.userImage);
                holder.btnChangeOrAdd = (Button) convertView.findViewById(R.id.btn_change_or_add);
                holder.callLink = (CircleImageView) convertView.findViewById(R.id.call_link);
                holder.callClick = (LinearLayout) convertView.findViewById(R.id.call_click);
//                convertView.setTag(holder);

//            } else {
//                holder = (ViewHolder) convertView.getTag();
//            }
                if (user == null) {
                    return null;
                }
//                holder.phone.setText(user.getPhone());
                if (user.getRole() == ProtoMessage.TeamRole.Owner_VALUE) {
                    holder.role.setText("群主 (" + user.getMemberPriority() + ")");
                } else if (user.getRole() == ProtoMessage.TeamRole.Manager_VALUE) {
                    holder.role.setText("管理员 (" + user.getMemberPriority() + ")");
                } else {
                    holder.role.setText("群成员 (" + user.getMemberPriority() + ")");
                }

//                holder.image.setAlpha(1f);
//                holder.name.setTextColor(0xff888888);
//                Log.d("jim","user.getNickName():"+user.getNickName()+" user.getName():"+user.getName());
                if (!TextUtils.isEmpty(user.getNickName()) && !user.getNickName().equals(user.getName())) {
                    holder.name.setText(user.getNickName() + " (" + user.getName() + ")");
                }else {
                    holder.name.setText(user.getName());
                }

                Bitmap bitmap = getFace(GlobalImg.getImage(mContext, user.getPhone()));
                if (bitmap == null) {
//                holder.image.setImageResource(R.drawable.default_useravatar);
                    getUserFace(user.getPhone());
                    String str = "1";
                    if (user.getNickName() != null && user.getNickName().trim().length() >= 0 && !user.getNickName().equals(user.getName())){
                        str = user.getNickName();
                    } else if (!TextUtils.isEmpty(user.getName()) && !user.getName().equals(user.getPhone())){
                        str = user.getName();
                    }
                    LetterTileDrawable drawable = new LetterTileDrawable(context.getResources());
                    drawable.setContactDetails(str, str);
                    Bitmap bmp = FriendFaceUtill.drawableToBitmap(drawable);
                    holder.image.setImageBitmap(bmp);

                } else {
                    holder.image.setImageBitmap(bitmap);
                }

//                Log.d("jim","name:"+user.getName()+" isFriend:"+user.isFriend());
                if (user.isFriend()){
                    if(isSHowOnline){
                        if(user.isOnline()){
                            holder.callClick.setVisibility(View.VISIBLE);
                            holder.callLink.setVisibility(View.VISIBLE);
                        }else{
                            holder.callClick.setVisibility(View.INVISIBLE);
                            holder.callLink.setVisibility(View.INVISIBLE);
                        }
                    }else {
                        if(user.isOnChat()){
                            holder.callClick.setVisibility(View.VISIBLE);
                            holder.callLink.setVisibility(View.VISIBLE);
                        }else{
                            holder.callClick.setVisibility(View.INVISIBLE);
                            holder.callLink.setVisibility(View.INVISIBLE);
                        }
                    }

                } else {
                    holder.callClick.setVisibility(View.INVISIBLE);
                    holder.callLink.setVisibility(View.INVISIBLE);
                }
                if(isSHowOnline){
                    if (user.isOnline()) {
                        holder.image.setAlpha(255);
                        holder.name.setTextColor(context.getResources().getColor(R.color.white));
    //                    holder.callClick.setVisibility(View.VISIBLE);
                    } else {
                        holder.image.setAlpha(125);
                        holder.name.setTextColor(context.getResources().getColor(R.color.text_color));
    //                    holder.callClick.setVisibility(View.INVISIBLE);
                    }
                }else{
                    if (user.isOnChat()) {
                        holder.image.setAlpha(255);
                        holder.name.setTextColor(context.getResources().getColor(R.color.white));
                        //                    holder.callClick.setVisibility(View.VISIBLE);
                    } else {
                        holder.image.setAlpha(125);
                        holder.name.setTextColor(context.getResources().getColor(R.color.text_color));
                        //                    holder.callClick.setVisibility(View.INVISIBLE);
                    }

                }
                holder.callClick.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //toCall(user);
//                    context.startActivity(intent);
                    }
                });

                CallState callState = GlobalStatus.getCallCallStatus().get(String.valueOf(0) + user.getPhone());
                if (GlobalStatus.equalPhone(user.getPhone())) {
                    holder.callLink.setImageResource(R.drawable.calling);
                } else if(callState != null && callState.getState() == GlobalStatus.STATE_CALL){
                    holder.callLink.setImageResource(R.drawable.img_other_talk);
                } else {
                    holder.callLink.setImageResource(R.drawable.btn_call);
                }
                holder.callLink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        toCall(user);
                    }
                });

                if (mTMInfo.size() > 0) {
                    for (TeamMemberInfo te : mTMInfo) {
                        if (te.getUserPhone().equals(user.getPhone())) {
//                            holder.btnChangeOrAdd.setVisibility(View.INVISIBLE);
//                            holder.btnChangeOrAdd.setText(FirstActivity.change);
                            typeStr = FirstActivity.change;
                            break;
                        }
                    }
                }
                if (myPhone.equals(user.getPhone())) {
                    holder.btnChangeOrAdd.setVisibility(View.INVISIBLE);
                    holder.btnChangeOrAdd.setText(FirstActivity.change);
                    typeStr = FirstActivity.change;
                }
//                if (!holder.btnChangeOrAdd.getText().toString().equals(FirstActivity.change)) {
//                    holder.btnChangeOrAdd.setText(FirstActivity.add);
//                    holder.btnChangeOrAdd.setVisibility(View.INVISIBLE);
//                    typeStr = FirstActivity.add;
//                }
                holder.btnChangeOrAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        toRoadConditionQuery(user.getPhone());


//                        if (holder.btnChangeOrAdd.getText().toString().equals(FirstActivity.add)) {
//                            toAddLinkMan(user);
//                        } else {
//                            showSetFriendInfoDialog(user);
//                        }

                    }


                });

            }
            return convertView;
        }

        public class ViewHolder {
            // public TextView tvNumber;
            TextView name;
            TextView role;
            TextView tvLine;
            TextView phone;
            ImageView image;
            ImageView imageInviteAdd;
            TextView nickName;
            RelativeLayout mRelativeLayout;
            RelativeLayout mLinearLayoutMapLook;
            TextView mButton;
            Button btnChangeOrAdd;
            CircleImageView callLink;
            LinearLayout callClick;
        }
    }

    private void toCall(AllTeamMember user) {
        AppliedFriends appliedFriends = null;
        try {
            DBManagerFriendsList db = new DBManagerFriendsList(mContext, DBTableName.getTableName(mContext, DBHelperFriendsList.NAME));
            appliedFriends = db.getFriend(user.getPhone());
            db.closeDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (appliedFriends != null) {
            Intent intent = new Intent(mContext, FirstActivity.class);
            intent.putExtra("data", 1);
            CallState callState = GlobalStatus.getCallCallStatus().get(String.valueOf(0) + appliedFriends.getPhoneNum());
            if (GlobalStatus.equalPhone(appliedFriends.getPhoneNum())) {
                intent.putExtra("callType", 0);
            } else if (callState != null && callState.getState() == GlobalStatus.STATE_CALL) {
                intent.putExtra("callType", 1);
            } else {
                intent.putExtra("callType", 2);
            }
            String friendName = appliedFriends.getNickName();
            if (friendName == null || friendName.equals("")) {
                friendName = appliedFriends.getUserName();
            }
            if (friendName == null || friendName.equals("")) {
                friendName = appliedFriends.getPhoneNum();
            }
            intent.putExtra("linkmanName", friendName);
            intent.putExtra("linkmanPhone", appliedFriends.getPhoneNum());
            VideoOrVoiceDialog dialog = new VideoOrVoiceDialog(mContext, intent);
            dialog.show();
        }else if(user.getPhone().equals(myPhone)){
            ToastR.setToast(mContext,"当前选择为本机用户");
        } else {
//            ToastR.setToast(mContext,"当前用户不是你的好友");
            //TODO 当前不是好友关系 发送对讲请求

            Intent intent = new Intent(mContext, FirstActivity.class);
            intent.putExtra("data", 1);
            CallState callState = GlobalStatus.getCallCallStatus().get(String.valueOf(0) + user.getPhone());
            if (GlobalStatus.equalPhone(user.getPhone())) {
                intent.putExtra("callType", 0);
            } else if (callState != null && callState.getState() == GlobalStatus.STATE_CALL) {
                intent.putExtra("callType", 1);
            } else {
                intent.putExtra("callType", 2);
            }
            String friendName = user.getNickName();
            if (friendName == null || friendName.equals("")) {
                friendName = user.getName();
            }
            if (friendName == null || friendName.equals("")) {
                friendName = user.getPhone();
            }
            intent.putExtra("linkmanName", friendName);
            intent.putExtra("linkmanPhone", user.getPhone());
            VideoOrVoiceDialog dialog = new VideoOrVoiceDialog(mContext, intent);
            dialog.show();
        }
    }

    private Bitmap getFace(Bitmap bmp) {
        try {
            if (bmp == null) {
                return null;
            }

            Bitmap bitmap1 = Bitmap.createScaledBitmap(bmp, 150, 150,
                    false); // 将图片缩小
            ImageTool ll = new ImageTool(); // 图片头像变成圆型
            bmp = ll.toRoundBitmap(bitmap1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;

    }

    //查询路况
    private void toRoadConditionQuery(String phone) {

        ProtoMessage.StartVoiceMsg.Builder builder = ProtoMessage.StartVoiceMsg.newBuilder();
        builder.setToUserPhone(phone);
        MyService.start(mContext, ProtoMessage.Cmd.cmdLiveVideoCall.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(LiveVideoCallProcesser.ACTION);
        new TimeoutBroadcast(ShowAllTeamMemberActivity.this, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                int code = i.getIntExtra("error_code", -1);
                if (code ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(mContext, "路况查询请求成功");
                } else {
                    Log.e(TAG," roadConditionQuery code:"+code);
                    new ResponseErrorProcesser(mContext, code);
                }
            }
        });
    }


    private void showSetFriendInfoDialog(final AllTeamMember mUser) {
        String str = "";
        String name = "";
        if (mUser.getPhone().equals(myPhone)) {
            str = "设置我在群中的昵称";
            name = mUser.getNickName();
        } else {
            str = "好友备注修改";
            name = mUser.getNickName();
        }
        LayoutInflater factory = LayoutInflater.from(mContext);// 提示框
        final View view = factory.inflate(R.layout.friend_request_editbox_layout, null);// 这里必须是final的
        final TextView remark = (TextView) view.findViewById(R.id.tv_remark_name);
        final EditText editRemark = (EditText) view.findViewById(R.id.remark_editText);// 获得输入框对象
        editRemark.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > GlobalStatus.MAX_TEXT_COUNT){
                    editRemark.setText(s.subSequence(0,16));
                    editRemark.setSelection(editRemark.length());
                    ToastR.setToast(mContext,"最大只能设置16个字符");
                }
            }
        });
        final RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.rl_msg);
        rl.setVisibility(View.GONE);
        remark.setText("用户名");
        editRemark.setText(name);
        editRemark.setSelection(editRemark.length());// 将光标追踪到内容的最后
        new AlertDialog.Builder(mContext).setTitle(str)// 提示框标题
                .setView(view).setPositiveButton("确定", // 提示框的两个按钮
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String remark = editRemark.getText().toString().trim();
                        if (remark.length() < 2) {
                            ToastR.setToast(mContext, "输入必须大于等于2位");
                            return;
                        }
                        if (mUser.getPhone().equals(myPhone)) {
                            toChangeMemberRemark(remark, mUser.getPhone());
                        } else {
                            setFriendInfo(mUser.getPhone(), remark);
                        }
                        dialog.dismiss();
                    }

                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                }).create().show();
    }

    /*
       修改自己群名称备注
     */
    private void toChangeMemberRemark(final String remark, final String phone) {
        showMyDialog(MODIFY_REMARK);
        ProtoMessage.TeamInfo.Builder builder = ProtoMessage.TeamInfo.newBuilder();
        builder.setMyTeamName(remark);
        builder.setTeamID(teamID);
        MyService.start(mContext, ProtoMessage.Cmd.cmdChangeMyTeamName.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(ChangeTeamMemberNickNameProcesser.ACTION);
        new TimeoutBroadcast(ShowAllTeamMemberActivity.this, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                dismissDialogShow();
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                dismissDialogShow();
                int code = i.getIntExtra("error_code", -1);
                if (code ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(mContext, "修改成员名称成功");
                    onChangeTeamInfoNickName(remark, phone);
                    updateLocalData(remark, phone, changeNickName, 0);
                } else {
                    Log.e(TAG," changeMemberRemark code:"+code);
                    new ResponseErrorProcesser(mContext, code);
                }
            }
        });
    }

    /*
        设置好友昵称
     */
    private void setFriendInfo(final String phone, final String remark) {
        showMyDialog(MODIFY_REMARK);
        ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
        builder.setNickName(remark);
        builder.setPhoneNum(phone);
        MyService.start(mContext, ProtoMessage.Cmd.cmdSetFriendInfo.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(SetFriendInfoProcesser.ACTION);
        new TimeoutBroadcast(ShowAllTeamMemberActivity.this, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                dismissDialogShow();
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                dismissDialogShow();
                int code = i.getIntExtra("error_code", -1);
                if (code ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    try {

                        onChangeNickName(remark, phone);
                        updateLocalData(remark, phone, changeNickName, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e(TAG," friendInfo code:"+code);
                    new ResponseErrorProcesser(mContext, code);
                }
            }
        });

    }

    /*
    更新本地数据
     */
    private void updateLocalData(String remark, String phone, String typeStr, int roleOrPriority) {
        if (typeStr.equals(changeNickName)) {
            for (AllTeamMember at : userList) {
                if (at.getPhone().equals(phone)) {
                    at.setNickName(remark);
                    break;
                }
            }
            for (AllTeamMember at : userList) {
                if (!at.getNickName().equals(AllTeamMember.ADDITIONAL) && at.getName().equals(AllTeamMember.ADDITIONAL) && phone.equals(at.getPhone())) {
                    at.setNickName(remark);
                    break;
                }
            }
         /*   Iterator<AllTeamMember> iterator = userList.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                AllTeamMember k = iterator.next();
                if (k.getName().equals(AllTeamMember.ADD) || k.getName().equals(AllTeamMember.ADDITIONAL)) {
                    i++;
                    iterator.remove();
                    if (i == 4) {
                        break;
                    }
                }
            }
            // 排序(实现了中英文混排)
            AllTeamPinyinComparator comparator = new AllTeamPinyinComparator();
            Collections.sort(userList, comparator);

            String str = AllTeamMember.MYNICKNAME;
            for (AllTeamMember at : userList) {
                if (at.getPhone().equals(myPhone)) {
                    str = at.getNickName();
                    break;
                }
            }

            userT(AllTeamMember.ADD, 0, "", 0, 0, "", 0);

//            if (phone.equals(myPhone)){
//                str = remark;
//            }
            userT(AllTeamMember.ADDITIONAL, 0, myPhone, 0, 0, str, 0);
            userT(AllTeamMember.ADDITIONAL, 0, myPhone, 0, 0, AllTeamMember.LOOK_MAP, 0);
            userT(AllTeamMember.ADDITIONAL, 0, myPhone, 0, 0, AllTeamMember.ADDITIONAL, 0);
*/
        } else if (typeStr.equals(changePermission)) {
            for (AllTeamMember at : userList) {
                if (at.getPhone().equals(phone)) {
                    at.setRole(roleOrPriority);
                    break;
                }
            }
        } else if (typeStr.equals(changeMemberPriority)) {
            for (AllTeamMember at : userList) {
                if (at.getPhone().equals(phone)) {
                    at.setMemberPriority(roleOrPriority);
                    break;
                }
            }
        }
        allTeamSort();
    }

    private void onChangeTeamInfoNickName(String remark, String phone) {
        TeamMemberHelper teamMemberHelper = new TeamMemberHelper(mContext, teamID + "TeamMember.dp", null);
        SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_phone", phone);
        values.put("nick_name", remark);
        db.update("LinkmanMember", values,"user_phone = ?",new String[]{phone});
        db.close();
    }

    private void onChangeNickName(String remark, String phone) {
        AppliedFriends mAppliedFriends = null;
        for (AppliedFriends af : listMembersCache) {
            if (af.getPhoneNum().equals(phone)) {
                mAppliedFriends = af;
                mAppliedFriends.setNickName(remark);
                break;
            }
        }
        //更新好友数据库
        DBManagerFriendsList db = new DBManagerFriendsList(mContext, DBTableName.getTableName(mContext, DBHelperFriendsList.NAME));
        db.updateFriendNickName(mAppliedFriends);
        db.closeDB();
        //联系人界面提示修改
        SharedPreferencesUtils.put(this, "friend_list_changed", true);
    }

    /**
     * 加好友网络请求
     *
     * @param remark
     * @param msg    phoneNum
     */
    private void addFriendsRequest(String remark, String msg, String phone) {
        showMyDialog(ADD_FRIEND);
        ProtoMessage.ApplyFriend.Builder builder = ProtoMessage.ApplyFriend.newBuilder();
        builder.setFriendPhoneNum(phone);
        builder.setApplyInfo(msg);
        builder.setApplyRemark(remark);
        MyService.start(mContext, ProtoMessage.Cmd.cmdApplyFriend.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(ApplyFriendProcesser.ACTION);
        new TimeoutBroadcast(ShowAllTeamMemberActivity.this, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                dismissDialogShow();
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                dismissDialogShow();
                int code = i.getIntExtra("error_code", -1);
                if (code ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(mContext, "请求成功，等待对方回应");
                } else {
                    Log.e(TAG," friendsRequest code:"+code);
                    new ResponseErrorProcesser(mContext, code);
                }
            }
        });
    }

    private void dismissOrQuit(String str) {
        if (str.equals(getResources().getText(R.string.team_dismiss))) {
            dismissOrQuitTeamDialog(dismissTeam);
        } else {
            dismissOrQuitTeamDialog(deleteTeam);
        }
    }

    private void dismissOrQuitTeamDialog(final String type) {
        new AlertDialog.Builder(mContext).setTitle("提示：")// 提示框标题
                .setMessage("确定要" + type + "[" + teamName + "]群组?").setPositiveButton("确定", // 提示框的两个按钮
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (type.equals(deleteTeam)) {
                            groupQuit();
                        } else {
                            groupDismiss();
                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton("取消", new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();

    }

    /**
     * 解散群组
     */
    private void groupDismiss() {
        showMyDialog(DISMISS_TEAM);
        ProtoMessage.AcceptTeam.Builder builder = ProtoMessage.AcceptTeam.newBuilder();
        builder.setTeamID(teamID);
        MyService.start(mContext, ProtoMessage.Cmd.cmdDismissTeam.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(DismissTeamProcesser.ACTION);
        new TimeoutBroadcast(ShowAllTeamMemberActivity.this, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                dismissDialogShow();
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                dismissDialogShow();
                int code = i.getIntExtra("error_code", -1);
                if (code ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    MsgTool.deleteTeamMsg(mContext, teamID);
                    ToastR.setToast(mContext, "删除群组成功");
                    Intent intent = new Intent();
                    intent.putExtra("data", 2);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Log.e(TAG," groupDismiss code:"+code);
                    new ResponseErrorProcesser(mContext, code);
                }
            }
        });
    }

    /**
     * 退出群组
     */
    private void groupQuit() {
        showMyDialog(DELETE_TEAM);
        ProtoMessage.ApplyTeam.Builder builder = ProtoMessage.ApplyTeam.newBuilder();
        builder.setTeamID(teamID);
        MyService.start(mContext, ProtoMessage.Cmd.cmdQuitTeam.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(DismissTeamProcesser.ACTION);
        new TimeoutBroadcast(ShowAllTeamMemberActivity.this, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                dismissDialogShow();
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                dismissDialogShow();
                int code = i.getIntExtra("error_code", -1);
                if (code ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    MsgTool.deleteTeamMsg(mContext, teamID);
                    ToastR.setToast(mContext, "退出群组成功");
                    Intent intent = new Intent();
                    intent.putExtra("data", 2);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Log.e(TAG," groupQuit code:"+code);
                    new ResponseErrorProcesser(mContext, code);
                }
            }
        });
    }

    public ArrayList<TeamMemberInfo> getTeamMember(Long teamId) {
        TeamMemberHelper teamMemberHelper = new TeamMemberHelper(mContext, teamId + "TeamMember.dp", null);
        ArrayList<TeamMemberInfo> teamMemberInfo = new ArrayList<TeamMemberInfo>();
        SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
        try {
            Cursor c = db.query("LinkmanMember", null, null, null, null, null, null);
            try {
                TeamMemberInfo af = null;
                while (c.moveToNext()) {
                    af = new TeamMemberInfo();
                    af.setUserName(c.getString(c.getColumnIndex("user_name")));
                    af.setUserPhone(c.getString(c.getColumnIndex("user_phone")));
                    af.setNickName(c.getString(c.getColumnIndex("nick_name")));
                    af.setRole(c.getInt(c.getColumnIndex("role")));
                    af.setMemberPriority(c.getInt(c.getColumnIndex("member_priority")));
                    teamMemberInfo.add(af);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();
            }
        } finally {
            db.close();
        }
        return teamMemberInfo;
    }

    private BroadcastReceiver refreshTeamReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long teamid = intent.getLongExtra("teamid", 0);
            if (intent.hasExtra("singout") && (teamid == teamID)) {
                finish();
            }
        }
    };

    class GetMsgReceiiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra("group",0L);
            if (intent.getIntExtra("msg_type", -1) == 4  && id == teamID) {
                handler.removeCallbacks(mRefreshTeamRunnable);
                handler.postDelayed(mRefreshTeamRunnable, 2000);
            }
        }
    }

    private Runnable mRefreshTeamRunnable = new Runnable() {
        @Override
        public void run() {
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };


    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String deletePhone = intent.getStringExtra("phone");
            String str = intent.getStringExtra("delete");
            if (deletePhone != null && deletePhone.length() > 0) {
                getlistMembersCache();
                convertViewTeamMember(allTeamMemberInfos,false);
            }
        }
    };


    @Override
    protected void onDestroy() {

        try {
            if (refreshTeamReceiver != null) {
                unregisterReceiver(refreshTeamReceiver);
            }

            if (getMsgReceiiver != null) {
                unregisterReceiver(getMsgReceiiver);
            }
            if (myConnectionChangeReceiver != null){
                unregisterReceiver(myConnectionChangeReceiver);
            }

            if (chatStatusReceiver != null) {
                unregisterReceiver(chatStatusReceiver);
            }

            if (myReceiver != null) {
                unregisterReceiver(myReceiver);
            }
            if (friendStatus != null) {
                unregisterReceiver(friendStatus);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    //网络监听广播
    class MyConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            isGetFriendStatus = false;
            boolean connect = intent.getBooleanExtra(ConnectionChangeReceiver.NETWORK_CHANGE_KEY,false);
            Log.d(TAG,"all team member ConnectionChange :"+connect);
            if (connect){
                if(isSHowOnline){
                    loadFriendStatus();
                }else {
                    refrshChatStatus();
                }
            } else {
                if (adapterU != null) {
                    adapterU.clearOnlineStatus();
                    allTeamSort();
                }
            }
        }
    }

    class ChatStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(GlobalStatus.NOTIFY_CALL_ACTION)) {
                if (adapterU != null) {
                    adapterU.notifyDataSetChanged();
                }
            } else if (intent.hasExtra("chat_status")) {
                if (adapterU != null) {
                    adapterU.notifyDataSetChanged();
                }
            }
            refrshChatStatus();
        }
    }

    private void refrshChatStatus() {
        if(isShowTalkMember){
            refreshTeamName();
            onChatSet = GlobalStatus.getChatList();
        }else{
            onChatSet.clear();
        }
        refreshStatus();
    }

    public void dismissDialogShow() {
//        checkDialog = false;
//        m_pDialog.dismiss();
    }

    public void showMyDialog(int type) {
//        REQUST_TYPE = type;
//        checkDialog = true;
//        m_pDialog.show();
    }

    class AllTeamPinyinComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            AllTeamMember contact1 = (AllTeamMember) o1;
            AllTeamMember contact2 = (AllTeamMember) o2;

            if (contact1.getRole()<0 || contact2.getRole()<0) {
                return contact2.getRole() - contact1.getRole();
            }

            String str = contact1.getNickName();
            String str3 = contact2.getNickName();

            String str1 = Cn2Spell.getPinYin(str);
            String str2 = Cn2Spell.getPinYin(str3);
//        Log.i("log", "str1:  " + str1 + "-----------------str2:  " + str2);
            int flag = str1.compareTo(str2);

            return flag;
        }
    }
}

