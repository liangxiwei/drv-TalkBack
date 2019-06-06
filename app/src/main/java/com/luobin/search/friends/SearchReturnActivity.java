package com.luobin.search.friends;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jrd48.chat.AllTeamMember;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.PicTool.LetterTileDrawable;
import com.example.jrd48.chat.SQLite.SQLiteTool;
import com.example.jrd48.chat.SQLite.TeamMemberHelper;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.TeamMemberInfo;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.User;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.AppliedFriendsList;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.example.jrd48.chat.friend.ShowFriendMsgActivity;
import com.example.jrd48.chat.group.GroupMemberDetailsActivity;
import com.example.jrd48.chat.group.InviteJoinGroupActivity;
import com.example.jrd48.chat.group.ModifyPriorityPrompt;
import com.example.jrd48.chat.group.ModifyPrioritytListener;
import com.example.jrd48.chat.group.ModifyTeamMsgActivity;
import com.example.jrd48.chat.group.MsgTool;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.chat.search.Cn2Spell;
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
import com.example.jrd48.service.protocol.root.LiveVideoCallProcesser;
import com.example.jrd48.service.protocol.root.ModifyTeamMemberPriorityProcesser;
import com.example.jrd48.service.protocol.root.SearchFriendProcesser;
import com.example.jrd48.service.protocol.root.SetFriendInfoProcesser;
import com.example.jrd48.service.protocol.root.TeamMemberProcesser;
import com.example.jrd48.service.protocol.root.TypeSearchFriendsProcesser;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.luobin.dvr.R;
import com.luobin.model.SearchFriendsCondition;
import com.luobin.model.SearchStrangers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2017/1/16.
 */

public class SearchReturnActivity extends BaseActivity implements View.OnClickListener {
    Context mContext;
    private PullToRefreshListView mPullRefreshListView;
    private ListView mListView;
    private allSearchFriendSAdapter adapterU;
    private List<AllTeamMember> userList = new ArrayList<>();
    private List<TeamMemberInfo> allTeamMemberInfos;
    List<TeamMemberInfo> localTeamMemberInfo;
    private long teamID;
    private String teamName;
    private String myPhone;
    private String userNameMe;
    private int type;
    String phone;
    private IntentFilter filter;
    List<AppliedFriends> listMembersCache;
    public static String deleteTeam = "删除并退出";
    public static String dismissTeam = "解散";

    private ProgressDialog m_pDialog;
    boolean checkDialog = true;
    boolean showDialog = true;
    private int REQUST_TYPE = 0;
    private int DISMISS_TEAM = 5;
    private int DELETE_TEAM = 6;
    private int ADD_FRIEND = 8;
    public static final int REFRESH_ALL_REMARK = 9;
    TeamInfo mTeamInfo;
    public static int REFRESH = 1;
    public static int NOREFRESH = 0;
    public static int INTENT_TYPE = 4;
    int teamType;

    ArrayList<SearchStrangers> userInfoList;
    private SearchFriendsCondition searchFriendsCondition;
    public static final int SHOW_MSG = 14;
    public static final int REQUST = 15;
    public static final int NO_REQUST = 16;
    private TextView mTextTitle;

    List<TeamMemberInfo> mTMInfo = new ArrayList<TeamMemberInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_return_activity);
        mContext = this;
        getIntentMsg();
        initView();
    }



    private void getIntentMsg() {
        Intent intent = getIntent();
        searchFriendsCondition = (SearchFriendsCondition) intent.getSerializableExtra("conditon");
        userInfoList = (ArrayList<SearchStrangers>) intent.getSerializableExtra("user_info");
        // 排序(实现了中英文混排)
        AllTeamPinyinComparator comparator = new AllTeamPinyinComparator();
        Collections.sort(userInfoList, comparator);
        setIntent(null);
    }

    private void initView() {
//        mQuitOrDismiss = (Button) findViewById(R.id.btn_team_quit_or_dismiss);
        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.gv_picture);
        mListView = mPullRefreshListView.getRefreshableView();
        adapterU = new allSearchFriendSAdapter(this, userInfoList);
        mListView.setAdapter(adapterU);
        mListView.setSelector(R.drawable.submenu_default);
        mTextTitle = (TextView) findViewById(R.id.tv_show_msg);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Intent intent = new Intent(SearchReturnActivity.this, ShowFriendMsgActivity.class);
                intent.putExtra("user_info", userInfoList.get(position - 1));
                startActivityForResult(intent, SHOW_MSG);
//                userInfoList

            }
        });

        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                showDialog = false;
//                getGroupMan(teamID);
                toSearchFriends();
            }
        });
        registerForContextMenu(mListView);

        //********************************************弹窗设置****************************************************
        //创建ProgressDialog对象
        m_pDialog = new ProgressDialog(this, R.style.CustomDialog);
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

    private void refreshTitle(int size){
        if (mTextTitle == null){
            return;
        }
        if (size <= 0){
            mTextTitle.setVisibility(View.VISIBLE);
        } else {
            mTextTitle.setVisibility(View.GONE);
        }
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
//                            updateLocalData(teamMemberInfo);
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
                            break;
                    }

                }
                break;
            case SHOW_MSG:
                if (resultCode == REQUST) {
                    String phone = data.getStringExtra("number");
                    updateLocalData(phone);
                }
                break;
            default:
                break;
        }
    }


    private void updateLocalData(String phone) {

        Iterator<SearchStrangers> iterator = userInfoList.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            SearchStrangers k = iterator.next();
            if (phone.equals(k.getPhoneNum())) {
                i++;
                iterator.remove();
                if (i == 3) {
                    break;
                }
            }
        }
        // 排序(实现了中英文混排)
        AllTeamPinyinComparator comparator = new AllTeamPinyinComparator();
        Collections.sort(userInfoList, comparator);

        adapterU.refresh(userInfoList);
    }



    @Override
    protected void onResume() {
        super.onResume();
//        refreshTeamName();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mPullRefreshListView != null) {
            mPullRefreshListView.onRefreshComplete();
        }
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



    private void toSearchFriends() {

        ProtoMessage.MsgSearchCar.Builder builder = ProtoMessage.MsgSearchCar.newBuilder();
        builder.setProv(searchFriendsCondition.getmProvince());
        builder.setCity(searchFriendsCondition.getmCity());
        builder.setTown(searchFriendsCondition.getmTown());
        builder.setCarType1(searchFriendsCondition.getCarBrand());
        builder.setCarType2(searchFriendsCondition.getCarType1());
        builder.setCarType3(searchFriendsCondition.getCarType2());
        builder.setSex(searchFriendsCondition.getmSex());
        if (searchFriendsCondition.getCarPlateNumber().equals(SearchFriendsByConditionActivity.NOT_SET)) {
            builder.setCarNum("");
        } else {
            builder.setCarNum(searchFriendsCondition.getCarPlateNumber());
        }
        builder.setPos(1);

        MyService.start(mContext, ProtoMessage.Cmd.cmdSearchCar.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(TypeSearchFriendsProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(mContext, filter, getBroadcastManager());
        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(mContext, "连接超时");
                if (mPullRefreshListView != null) {
                    mPullRefreshListView.onRefreshComplete();
                }
            }

            @Override
            public void onGot(Intent i) {
                if (mPullRefreshListView != null) {
                    mPullRefreshListView.onRefreshComplete();
                }
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK_VALUE) {
                    userInfoList = (ArrayList<SearchStrangers>) i.getSerializableExtra("user_info");
                    AllTeamPinyinComparator comparator = new AllTeamPinyinComparator();
                    Collections.sort(userInfoList, comparator);
                    adapterU.refresh(userInfoList);
                } else {
                    new ResponseErrorProcesser(mContext, i.getIntExtra("error_code", -1));
                }

            }
        });
    }


    private void convertViewTeamMember(List<TeamMemberInfo> teamMemberInfos) {
        userList.clear();
        allTeamMemberInfos = teamMemberInfos;//保存群成员列表
        String memberName;
        int sex;

        for (TeamMemberInfo in : teamMemberInfos) {
            memberName = null;
            sex = 0;
            if (listMembersCache.size() > 0) {
                for (AppliedFriends appliedFriends : listMembersCache) {
                    if (appliedFriends.getPhoneNum().equals(in.getUserPhone())) {
                        memberName = appliedFriends.getNickName();
                        sex = appliedFriends.getUserSex();
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
            userT(in.getUserName(),false, status, in.getUserPhone(), sex, in.getMemberPriority(), memberName, in.getRole());
        }
        // 排序(实现了中英文混排)
        AllTeamPinyinComparator comparator = new AllTeamPinyinComparator();
        Collections.sort(userList, comparator);

        userT(AllTeamMember.ADD, false,0, "", 0, 0, "", 0);
        userT(AllTeamMember.ADDITIONAL, false,0, myPhone, 0, 0, AllTeamMember.MYNICKNAME, 0);
        userT(AllTeamMember.ADDITIONAL, false,0, myPhone, 0, 0, AllTeamMember.ADDITIONAL, 0);

        mTMInfo.clear();
        for (TeamMemberInfo te : allTeamMemberInfos) {
            for (AppliedFriends af : listMembersCache) {
                if (af.getPhoneNum().equals(te.getUserPhone())) {
                    mTMInfo.add(te);
                    break;
                }
            }
        }
//        adapterU.refresh(userList);
    }

    public void userT(String name, boolean isFriend,int state, String phone, int sex, int memberPriority, String nickName, int role) {
        AllTeamMember user = new AllTeamMember(state,isFriend, name, phone, sex, memberPriority, nickName, role);
        userList.add(user);
    }

    public void getUserFace(String memberName) {
        ProtoMessage.SearchUser.Builder builder = ProtoMessage.SearchUser.newBuilder();
        builder.setPhoneNum(memberName);
        builder.setOnlyPhoneNum(true);
        MyService.start(mContext, ProtoMessage.Cmd.cmdSearchUser.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(SearchFriendProcesser.ACTION);
        new TimeoutBroadcast(SearchReturnActivity.this, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    AppliedFriends aply = i.getParcelableExtra("search_user");
                    if (aply.getPhoneNum() == null || aply.getPhoneNum().length() <= 0) {
//                        ToastR.setToast(mContext, "未找到该用户");
                    } else {
//                        msgDB_Team();
                        adapterU.notifyDataSetChanged();
                    }
                } else {
                    Log.e("jim","search return userFace  code:"+i.getIntExtra("error_code", -1));
                  //  new ResponseErrorProcesser(mContext, i.getIntExtra("error_code", -1));
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

    public class allSearchFriendSAdapter extends BaseAdapter {

        int friendsIndex = 0;
        private List<SearchStrangers> mArrayList;
        private Context context;
        private LayoutInflater layoutInflater;

        public allSearchFriendSAdapter(Context context, List<SearchStrangers> mFriend) {
            layoutInflater = LayoutInflater.from(context);
            refreshTitle(mFriend.size());
            this.context = context;
            this.mArrayList = mFriend;
        }

        public List<SearchStrangers> getList() {
            return mArrayList;
        }

        // 刷新适配器
        public void refresh(List<SearchStrangers> mArryFriend) {
            this.mArrayList = mArryFriend;
            refreshTitle(mArryFriend.size());
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

        @Override
        public SearchStrangers getItem(int position) {
            SearchStrangers item = null;
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
            final SearchStrangers user = mArrayList.get(position);
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.search_return_item, null);
                holder.name = (TextView) convertView.findViewById(R.id.search_listitem_name);
                holder.phone = (TextView) convertView.findViewById(R.id.search_listitem_count);
                holder.image = (ImageView) convertView.findViewById(R.id.search_circle_image);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
                if (user == null) {
                    return null;
                }
//                holder.phone.setText(user.getPhone());

                holder.image.setAlpha(1f);
//                holder.name.setTextColor(0xff888888);

            Bitmap bitmap = GlobalImg.getImage(mContext, user.getPhoneNum());
                if (bitmap == null) {
//                holder.image.setImageResource(R.drawable.default_useravatar);
                    LetterTileDrawable drawable = new LetterTileDrawable(context.getResources());
                    drawable.setContactDetails(user.getUserName(), user.getUserName());
                    Bitmap bmp = FriendFaceUtill.drawableToBitmap(drawable);
                    holder.image.setImageBitmap(bmp);
                    getUserFace(user.getPhoneNum());

                } else {
                    holder.image.setImageBitmap(bitmap);
                }


            String number = user.getPhoneNum();
            String str = number.substring(0, 3);
            String str1 = number.substring(8, number.length());
            holder.phone.setText(str + "*****" + str1);
            if (user.getUserName().equals(number)) {
                holder.name.setText(str + "*****" + str1);
            }else {
                holder.name.setText(user.getUserName() + "");
            }
            return convertView;
        }

        public class ViewHolder {
            // public TextView tvNumber;
            TextView name;
            TextView discourseCompetence;
            TextView phone;
            ImageView image;
            TextView nickName;
            RelativeLayout mRelativeLayout;
            Button mButton;
            Button btnChangeOrAdd;
        }
    }

    //查询路况
    private void toRoadConditionQuery(String phone) {

        ProtoMessage.StartVoiceMsg.Builder builder = ProtoMessage.StartVoiceMsg.newBuilder();
        builder.setToUserPhone(phone);
        MyService.start(mContext, ProtoMessage.Cmd.cmdLiveVideoCall.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(LiveVideoCallProcesser.ACTION);
        new TimeoutBroadcast(SearchReturnActivity.this, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(mContext, "路况查询请求成功");
                } else {
                    new ResponseErrorProcesser(mContext, i.getIntExtra("error_code", -1));
                }
            }
        });
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
        new TimeoutBroadcast(SearchReturnActivity.this, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                dismissDialogShow();
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                dismissDialogShow();
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(mContext, "请求成功，等待对方回应");
                } else {
                    new ResponseErrorProcesser(mContext, i.getIntExtra("error_code", -1));
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
                new DialogInterface.OnClickListener() {
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
        new TimeoutBroadcast(SearchReturnActivity.this, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                dismissDialogShow();
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                dismissDialogShow();
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    MsgTool.deleteTeamMsg(mContext, teamID);
                    ToastR.setToast(mContext, "删除群组成功");
                    Intent intent = new Intent();
                    intent.putExtra("data", 2);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    new ResponseErrorProcesser(mContext, i.getIntExtra("error_code", -1));
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
        new TimeoutBroadcast(SearchReturnActivity.this, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                dismissDialogShow();
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                dismissDialogShow();
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    MsgTool.deleteTeamMsg(mContext, teamID);
                    ToastR.setToast(mContext, "退出群组成功");
                    Intent intent = new Intent();
                    intent.putExtra("data", 2);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    new ResponseErrorProcesser(mContext, i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    public List<TeamMemberInfo> getTeamMember(Long teamId) {
        TeamMemberHelper teamMemberHelper = new TeamMemberHelper(mContext, teamId + "TeamMember.dp", null);
        List<TeamMemberInfo> teamMemberInfo = new ArrayList<TeamMemberInfo>();
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
//            long teamid = intent.getLongExtra("teamid", 0);
//            if (intent.hasExtra("singout") && (teamid == teamID)) {
//                finish();
//            }
        }
    };



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void dismissDialogShow() {
        checkDialog = false;
        m_pDialog.dismiss();
    }

    public void showMyDialog(int type) {
        REQUST_TYPE = type;
        checkDialog = true;
        m_pDialog.show();
    }

    class AllTeamPinyinComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            SearchStrangers contact1 = (SearchStrangers) o1;
            SearchStrangers contact2 = (SearchStrangers) o2;

            String str = contact1.getUserName();
            String str3 = contact2.getUserName();

            String str1 = Cn2Spell.getPinYin(str);
            String str2 = Cn2Spell.getPinYin(str3);
//        Log.i("log", "str1:  " + str1 + "-----------------str2:  " + str2);
            int flag = str1.compareTo(str2);

            return flag;
        }
    }
}

