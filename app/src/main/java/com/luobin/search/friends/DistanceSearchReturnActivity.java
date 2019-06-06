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

import com.example.jrd48.GlobalStatus;
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
import com.example.jrd48.service.protocol.root.GetCarLocationProcesser;
import com.example.jrd48.service.protocol.root.LiveVideoCallProcesser;
import com.example.jrd48.service.protocol.root.ModifyTeamMemberPriorityProcesser;
import com.example.jrd48.service.protocol.root.SearchFriendProcesser;
import com.example.jrd48.service.protocol.root.SetFriendInfoProcesser;
import com.example.jrd48.service.protocol.root.TeamMemberProcesser;
import com.example.jrd48.service.protocol.root.TypeSearchFriendsProcesser;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.luobin.dvr.R;
import com.luobin.model.StrangerLocationStatus;
import com.luobin.tool.RadiusTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2017/1/16.
 */

public class DistanceSearchReturnActivity extends BaseActivity implements View.OnClickListener {
    Context mContext;
    private PullToRefreshListView mPullRefreshListView;
    private ListView mListView;
    private DistanceSearchFriendSAdapter adapterU;
    private List<AllTeamMember> userList = new ArrayList<>();
    private String userNameMe;
    List<AppliedFriends> listMembersCache;
    private TextView mTextTitle;

    private ProgressDialog m_pDialog;
    boolean checkDialog = true;
    boolean showDialog = true;
    private int REQUST_TYPE = 0;
    private int ADD_FRIEND = 8;
    public static final int SHOW_MSG = 14;
    public static final int REQUST = 15;
    public static final int NO_REQUST = 16;
    private List<StrangerLocationStatus> strangerLocationStatusesList ;
    private StrangerLocationStatus mStrangerLocationStatus;
    List<TeamMemberInfo> mTMInfo = new ArrayList<TeamMemberInfo>();
    private double latitude;
    private double longitude;
    private int radius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_return_activity);
        mContext = this;
        getIntentMsg();
        initView();
    }


    private void getlistMembersCache() {
        //获取好友列表
        DBManagerFriendsList db = new DBManagerFriendsList(mContext, DBTableName.getTableName(mContext, DBHelperFriendsList.NAME));
        listMembersCache = db.getFriends(false);
        db.closeDB();

    }

    private void getIntentMsg() {
        Intent intent = getIntent();
        strangerLocationStatusesList = (ArrayList<StrangerLocationStatus>) intent.getSerializableExtra("stranger_location");
        latitude = intent.getDoubleExtra("latitude",0);
        longitude = intent.getDoubleExtra("longitude",0);
        radius = intent.getIntExtra("radius",100);
        // 排序(实现了中英文混排)
        AllTeamPinyinComparator comparator = new AllTeamPinyinComparator();
        Collections.sort(strangerLocationStatusesList, comparator);
        setIntent(null);
    }

    private void initView() {
//        mQuitOrDismiss = (Button) findViewById(R.id.btn_team_quit_or_dismiss);
        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.gv_picture);
        mListView = mPullRefreshListView.getRefreshableView();
        mTextTitle = (TextView) findViewById(R.id.tv_show_msg);
        adapterU = new DistanceSearchFriendSAdapter(this, strangerLocationStatusesList);
        mListView.setAdapter(adapterU);
        mListView.setSelector(R.drawable.submenu_default);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Intent intent = new Intent(DistanceSearchReturnActivity.this, ShowStrangerMsgActivity.class);
                intent.putExtra("user_info", strangerLocationStatusesList.get(position - 1));
                intent.putExtra("requestCode",REQUST);
                startActivityForResult(intent,SHOW_MSG);

            }
        });

        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                showDialog = false;
                toGetOtherLocation();
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
                        case 8:
                            ToastR.setToast(mContext, "取消添加好友");
                            break;
                    }

                }
            }
        });

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        switch (requestCode) {
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

    private void updateLocalData(String phone) {

        Iterator<StrangerLocationStatus> iterator = strangerLocationStatusesList.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            StrangerLocationStatus k = iterator.next();
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
        Collections.sort(strangerLocationStatusesList, comparator);

        adapterU.refresh(strangerLocationStatusesList);
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


    boolean isFresh = false;


    private void toGetOtherLocation() {

        ProtoMessage.MsgSearchAround.Builder builder = ProtoMessage.MsgSearchAround.newBuilder();
        builder.setLat(latitude);
        builder.setLng(longitude);
        builder.setRadius(radius*1000);  //半径(米)
        builder.setPos(1);// 批次：0,第一批，1第二批，...， 每次100个
        //TODO 获取定位位置
        MyService.start(mContext, ProtoMessage.Cmd.cmdSearchAround.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(GetCarLocationProcesser.ACTION);
        new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                if (mPullRefreshListView != null) {
                    mPullRefreshListView.onRefreshComplete();
                }
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent intent) {
                if (mPullRefreshListView != null) {
                    mPullRefreshListView.onRefreshComplete();
                }
                int errorCode = intent.getIntExtra("error_code", -1);
                if (errorCode == ProtoMessage.ErrorCode.OK.getNumber()) {
                    strangerLocationStatusesList = (ArrayList<StrangerLocationStatus>) intent.getSerializableExtra("stranger_location");
//                    adapterU.refresh(strangerLocationStatusesList);
                    if (strangerLocationStatusesList.size() > 0) {
                        AllTeamPinyinComparator comparator = new AllTeamPinyinComparator();
                        Collections.sort(strangerLocationStatusesList, comparator);
                        adapterU.refresh(strangerLocationStatusesList);
                    } else {
                        adapterU.refresh(strangerLocationStatusesList);
//                        ToastR.setToastLong(mContext, "未找到对应的陌生人");
                    }

                } else {
                    ToastR.setToast(mContext, "刷新失败: 错误码 " + errorCode);
                }
            }
        });


    }



    public void userT(String name,boolean isFriend,int state, String phone, int sex, int memberPriority, String nickName, int role) {
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
        new TimeoutBroadcast(DistanceSearchReturnActivity.this, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

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
                    Log.e("jim","distance userFace  code:"+i.getIntExtra("error_code", -1));
                   // new ResponseErrorProcesser(mContext, i.getIntExtra("error_code", -1));
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

    public class DistanceSearchFriendSAdapter extends BaseAdapter {

        int friendsIndex = 0;
        private List<StrangerLocationStatus> mArrayList;
        private Context context;
        private LayoutInflater layoutInflater;

        public DistanceSearchFriendSAdapter(Context context, List<StrangerLocationStatus> mFriend) {
            layoutInflater = LayoutInflater.from(context);
            this.context = context;
            this.mArrayList = mFriend;
            refreshTitle(mFriend.size());
        }

        public List<StrangerLocationStatus> getList() {
            return mArrayList;
        }

        // 刷新适配器
        public void refresh(List<StrangerLocationStatus> mArryFriend) {
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
        public StrangerLocationStatus getItem(int position) {
            StrangerLocationStatus item = null;
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
            final StrangerLocationStatus user = mArrayList.get(position);
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.search_strangers_item, null);
                holder.name = (TextView) convertView.findViewById(R.id.search_listitem_name);
                holder.phone = (TextView) convertView.findViewById(R.id.search_listitem_count);
                holder.image = (ImageView) convertView.findViewById(R.id.search_circle_image);
                holder.mButtonAddFriend = (Button) convertView.findViewById(R.id.btn_add_friend);
                holder.mButtonRequest = (Button) convertView.findViewById(R.id.btn_request);

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

            Bitmap bitmap = GlobalImg.getImage(context, user.getPhoneNum());
            if (bitmap == null) {
                LetterTileDrawable drawable = new LetterTileDrawable(getResources());
                if (user.getUserName() != null && user.getUserName().length() > 0) {
                    drawable.setContactDetails(user.getUserName(), user.getUserName());
                } else {
                    drawable.setContactDetails("1", "1");
                }
                Bitmap bmp = FriendFaceUtill.drawableToBitmap(drawable);
                holder.image.setImageBitmap(bmp);
            } else {
                holder.image.setImageBitmap(bitmap);
            }

            holder.mButtonAddFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO 添加好友
                }
            });

            holder.mButtonRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO 请求查看路况
                    toRoadConditionQuery(user.getPhoneNum());
                }
            });

            holder.name.setText(user.getUserName() + "");
            String number = user.getPhoneNum();
            String str = number.substring(0, 3);
            String str1 = number.substring(8, number.length());
            int radius = user.getRadius() <= 1 ? 1 : user.getRadius();
            String radiu = RadiusTool.getRadius(radius);
            holder.phone.setText(str + "*****" + str1 + "\n"+radiu);
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
            Button mButtonAddFriend;
            Button mButtonRequest;
        }
    }

    //查询路况
    private void toRoadConditionQuery(String phone) {

        ProtoMessage.StartVoiceMsg.Builder builder = ProtoMessage.StartVoiceMsg.newBuilder();
        builder.setToUserPhone(phone);
        MyService.start(mContext, ProtoMessage.Cmd.cmdLiveVideoCall.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(LiveVideoCallProcesser.ACTION);
        new TimeoutBroadcast(DistanceSearchReturnActivity.this, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
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


    private void toAddLinkMan(final AllTeamMember mUser) {

        LayoutInflater factory = LayoutInflater.from(mContext);// 提示框
        final View view = factory.inflate(R.layout.applied_add_linkman, null);// 这里必须是final的
        final TextView userName = (TextView) view.findViewById(R.id.user_name);
        final EditText nickName = (EditText) view.findViewById(R.id.nick_name);
        final EditText etMsg = (EditText) view.findViewById(R.id.et_msg);
        if (mUser.getName() != null && !mUser.getName().equals("")) {
            userName.setText(mUser.getName());
            nickName.setText(mUser.getNickName());
            nickName.setSelection(nickName.length());
        } else {
            userName.setText("未设置");
            nickName.setText(mUser.getNickName());
            nickName.setSelection(nickName.length());
        }

        etMsg.setText("我是" + userNameMe + ",请求加您为好友，谢谢。");
        //new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT)// 提示框标题
        new AlertDialog.Builder(mContext)// 提示框标题
                .setView(view).setPositiveButton("确定", // 提示框的两个按钮
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String msg = etMsg.getText().toString().trim();
                        String remark = nickName.getText().toString().trim();
                        if (remark.length()<= 0){
                            ToastR.setToast(mContext, "备注输入不能为空");
                            return;
                        }
                        if (msg.length() <= 0){
                            ToastR.setToast(mContext, "验证信息输入不能为空");
                            return;
                        }
                        if (remark.length() > GlobalStatus.MAX_TEXT_COUNT) {
                            ToastR.setToast(mContext, "备注输入过长（最大只能设置16个字符）");
                            return;
                        }
                        addFriendsRequest(remark, msg, mUser.getPhone());
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
        new TimeoutBroadcast(DistanceSearchReturnActivity.this, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

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

    public void back(View view) {
        finish();
    }
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
            StrangerLocationStatus contact1 = (StrangerLocationStatus) o1;
            StrangerLocationStatus contact2 = (StrangerLocationStatus) o2;

            int flag = 0;
            if (contact1.getRadius() > contact2.getRadius()) {
                flag = 1;
            } else if (contact1.getRadius() == contact2.getRadius()) {
                flag = 0;
            } else {
                flag = -1;
            }
            return flag;
        }
    }
}

