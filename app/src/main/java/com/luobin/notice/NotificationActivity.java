package com.luobin.notice;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.ImageTool;
import com.example.jrd48.chat.MainActivity;
import com.example.jrd48.chat.PicTool.LetterTileDrawable;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.AppliedFriendsList;
import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.example.jrd48.chat.friend.FriendsAndTeams;
import com.example.jrd48.chat.group.AppliedTeams;
import com.example.jrd48.chat.group.AppliedTeamsList;
import com.example.jrd48.chat.group.CreateGroupActivity;
import com.example.jrd48.chat.receiver.NotifyFriendBroadcast;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyBroadcastReceiver;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.AcceptFriendProcesser;
import com.example.jrd48.service.protocol.root.AcceptGroupProcesser;
import com.example.jrd48.service.protocol.root.AppliedGroupListProcesser;
import com.example.jrd48.service.protocol.root.AppliedListProcesser;
import com.example.jrd48.service.protocol.root.GetFriendInfoProcesser;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.luobin.dvr.R;
import com.luobin.ui.BaseDialogActivity;
import com.luobin.ui.VideoOrVoiceDialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationActivity extends BaseDialogActivity implements OnClickListener {
    private PullToRefreshListView mPullRefreshListView;
    ListView listView;
    private FriendRequestAdapter adapter;
    //    private ImageView mImageView;
    private Handler mHandler = new Handler();
    //    private RelativeLayout mRelativeLayout;
//    private View mView;
//    private EditText etSearch;
    private TextView tvNotification;
    Button btnCreateGroup;
    String getEditText;
    Context mContext;
    private static int listSize = 0;
    NotifyFriendBroadcast mNotifyFriendBroadcast;
    List<ViewFriendsMsg> mFriend = new ArrayList<ViewFriendsMsg>();
    List<AppliedTeams> teamList = new ArrayList<AppliedTeams>();
    List<FriendsAndTeams> mFriendsAndTeams = new ArrayList<FriendsAndTeams>();
    private ProgressDialog m_pDialog;
    private boolean checkPullRefresh = false;
    public static String friendType = "好友申请";
    public static String teamType = "群组申请和添加";
    private String agreeMatch = "Agree";
    ImageView imgClose;
    class ViewFriendsMsg {
        public FriendsAndTeams friends;

        public ViewFriendsMsg() {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_activity);
        getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        mContext = this;
        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_request_in_refresh_list);
        listView = mPullRefreshListView.getRefreshableView();

        tvNotification = (TextView) findViewById(R.id.tv_no_notification);

        adapter = new FriendRequestAdapter(this, mFriendsAndTeams);
        listView.setAdapter(adapter);
        listView.setSelector(R.drawable.submenu_default);

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
        m_pDialog.setCancelable(false);
        //********************************************弹窗设置****************************************************
        initBroadcast();


        mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                checkPullRefresh = true;
                loadFriendsListFromNet(mContext);
//                loadGroupListFromNet();

            }
        });
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                acceptOrRefuseDialog(position);
            }
        });

        imgClose = (ImageView) findViewById(R.id.imgClose);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void acceptOrRefuseDialog(final int position) {
        AcceptOrRefuseDialog dialog = new AcceptOrRefuseDialog(mContext, new AcceptOrRefuseInterface() {
            @Override
            public void accept() {
                dialogFriendsRequest(position-1,adapter.getItem(position-1));
            }

            @Override
            public void refuse() {
                refuseAdd(position-1,adapter.getItem(position-1));
            }
        });
        dialog.show();
    }

    private void initBroadcast() {
        //注册获取申请加好友广播
        mNotifyFriendBroadcast = new NotifyFriendBroadcast(mContext);
        mNotifyFriendBroadcast.setReceiver(new MyBroadcastReceiver() {
            @Override
            protected void onReceiveParam(String str) {
                loadFriendsListFromNet(mContext);
//                loadGroupListFromNet();
            }
        });
        mNotifyFriendBroadcast.start();

    }

    @Override
    protected void onResume() {
        super.onResume();
//        loadFriendsFromCache();
        loadFriendsListFromNet(mContext);
//        loadGroupListFromNet();
    }

    private void loadGroupListFromNet() {
        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
        MyService.start(mContext, ProtoMessage.Cmd.cmdAppliedTeamList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppliedGroupListProcesser.ACTION);
        new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                if (mPullRefreshListView != null)
                    mPullRefreshListView.onRefreshComplete();
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (mPullRefreshListView != null) {
                    mPullRefreshListView.onRefreshComplete();
                }
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    AppliedTeamsList list = i.getParcelableExtra("get_applied_group_list");
                    if (list != null && list.getAppliedTeams() != null && list.getAppliedTeams().size() > 0) {
                        convertViewGroupList(teamList, list.getAppliedTeams());
                        refreshListData(mFriendsAndTeams);
                        if (checkPullRefresh == true) {
                            ToastR.setToast(mContext, "获取群组成功");
                            checkPullRefresh = false;
                        }
                    } else {
                        listSize = MainActivity.MENU_ZERO;
                        if (mFriendsAndTeams.size() <= 0) {
                            isShowView(mFriendsAndTeams.size());
                            if (checkPullRefresh == true) {
                                ToastR.setToast(mContext, "没有相关的列表信息");
                                checkPullRefresh = false;
                            }
                        } else {
                            refreshListData(mFriendsAndTeams);
                        }
                    }
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }


    private void convertViewGroupList(List<AppliedTeams> teamList, List<AppliedTeams> appliedTeams) {
//        teamList.clear();
        if (appliedTeams.size() > 0) {
//            FriendsAndTeams aat = new FriendsAndTeams();
//            aat.setType(teamType);
//            aat.setUserName(teamType);
//            mFriendsAndTeams.add(aat);
            for (AppliedTeams c : appliedTeams) {
                AppliedTeams temp = new AppliedTeams();
                temp = c;
                FriendsAndTeams at = new FriendsAndTeams();
                at.setTeamID(c.getTeamID());
                at.setTeamName(c.getTeamName());
                at.setTypePic(teamType);
                at.setUserPhone(c.getUserPhone());
                at.setUserName(c.getUserName());
                at.setApplyType(c.getApplyType());
                at.setInviteUserName(c.getInviteUserName());
                at.setInviteUserPhone(c.getInviteUserPhone());
                mFriendsAndTeams.add(at);
//                teamList.add(temp);
            }
        }
    }


    /**
     * 按钮onClick事件重写
     *
     * @param v
     */
    public void back(View v) {
        Intent intent = new Intent();
        setResult(listSize, intent);
        finish();
    }

    /**
     * 重写返回键功能
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Intent intent = new Intent();
            setResult(listSize, intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    /**
     * 获取请求好友列表
     *
     * @param context
     */
    public void loadFriendsListFromNet(Context context) {

        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
        MyService.start(mContext, ProtoMessage.Cmd.cmdAppliedList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppliedListProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(mContext, filter, getBroadcastManager());
        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                if (mPullRefreshListView != null) {
                    mPullRefreshListView.onRefreshComplete();
                }
                ToastR.setToast(mContext, "连接超时");
                isShowView(mFriendsAndTeams.size());
            }

            @Override
            public void onGot(Intent i) {
                if (mPullRefreshListView != null) {
                    mPullRefreshListView.onRefreshComplete();
                }
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    AppliedFriendsList list = i.getParcelableExtra("get_applied_msg");
                    if (list.getAppliedFriends().size() <= 0) {
//                        if (checkPullRefresh == true) {
//                            ToastR.setToast(mContext, "目前没有好友邀请的列表信息");
//                            checkPullRefresh = false;
//                        }
                        mFriendsAndTeams.clear();
                        listSize = MainActivity.MENU_ZERO;
                        isShowView(mFriendsAndTeams.size());
                    } else {
                        convertViewFriendList(mFriend, list.getAppliedFriends());

//                        setList();
//                        if (checkPullRefresh == true) {
//                            ToastR.setToast(mContext, "获取好友成功");
//                            checkPullRefresh = false;
//                        }
                    }
                } else {
                    fail(i.getIntExtra("error_code", -1));
                    isShowView(mFriendsAndTeams.size());
                }
            }
        });

    }

    public void fail(int i) {
        new ResponseErrorProcesser(mContext, i);
    }


    /**
     * 缓存数据到本地
     *
     * @param mFriend2
     * @param list
     */
    public void convertViewFriendList(List<ViewFriendsMsg> mFriend2, List<AppliedFriends> list) {
        mFriendsAndTeams.clear();
        if (list.size() > 0) {
//            FriendsAndTeams aat = new FriendsAndTeams();
//            aat.setType(friendType);
//            aat.setUserName(friendType);
//            mFriendsAndTeams.add(aat);
            for (AppliedFriends c : list) {

                FriendsAndTeams at = new FriendsAndTeams();
                at.setPhoneNum(c.getPhoneNum());
                at.setUserName(c.getUserName());
                at.setTypePic(friendType);
                at.setNickName(c.getNickName());
                at.setFriendStar(c.getFriendStar());
                at.setUserPic(c.getUserPic());
                at.setUserSex(c.getUserSex());
                at.setApplyInfo(c.getApplyInfo());
                mFriendsAndTeams.add(at);
            }
        }
        refreshListData(mFriendsAndTeams);
    }


    /**
     * 自定义DbAdapter
     */

    public class FriendRequestAdapter extends BaseAdapter {

        private List<FriendsAndTeams> mArrayList;
        private Context context;
        private LayoutInflater layoutInflater;

        public List<FriendsAndTeams> getList() {
            return mArrayList;
        }

        // 刷新适配器
        public void refresh(List<FriendsAndTeams> mArryFriend) {
            this.mArrayList = mArryFriend;
            notifyDataSetChanged();
        }

        public FriendRequestAdapter(Context context, List<FriendsAndTeams> mFriend) {
            layoutInflater = LayoutInflater.from(context);
            this.context = context;
            this.mArrayList = mFriend;
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
        public FriendsAndTeams getItem(int position) {
            FriendsAndTeams item = null;
            if (null != mArrayList) {
                item = mArrayList.get(position);
            }
            return item;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        int friendsIndex = 0;

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final FriendsAndTeams friendsMsg = mArrayList.get(position);

            ViewHolder holder = new ViewHolder();
            ;

            if (friendsMsg.getType() != null && friendsMsg.getType().length() > 0) {
                convertView = LayoutInflater.from(context).inflate(R.layout.notification_activity_index, null);
                convertView.setBackgroundColor(context.getResources().getColor(R.color.search_bg));
                holder.indexTv = (TextView) convertView.findViewById(R.id.indexTv);
                holder.indexTv.setText(friendsMsg.getUserName());
            } else {
                convertView = layoutInflater.inflate(R.layout.notification_activity_list_item, null);
                holder.tvFriendName = (TextView) convertView.findViewById(R.id.request_in_text_friend_name);
                holder.tvFriendMsg = (TextView) convertView.findViewById(R.id.request_in_text_friend_msg);
                holder.btnRefuse = (Button) convertView.findViewById(R.id.request_in_refuse);
                holder.btnAccept = (Button) convertView.findViewById(R.id.request_in_accept);
                holder.mShowImage = (ImageView) convertView.findViewById(R.id.request_in_image);
                if (friendsMsg == null) {
                    return null;
                }
                Log.i("getusername", friendsMsg.getNickName() + "--" + friendsMsg.getUserName() + "--" + friendsMsg.getUserName());
                Bitmap bitmap = getUserFace(friendsMsg,context);
                if (friendsMsg.getTypePic().equals(teamType)) {
                    holder.mShowImage.setImageResource(R.drawable.setting2);
                } else {
                    if (bitmap == null) {
                        LetterTileDrawable drawable = new LetterTileDrawable(context.getResources());
                        if (friendsMsg.getUserName() != null && friendsMsg.getUserName().length() > 0) {
                            drawable.setContactDetails(friendsMsg.getUserName(), friendsMsg.getUserName());
                        } else {
                            drawable.setContactDetails("1", "1");
                        }
                        Bitmap mBitmap = FriendFaceUtill.drawableToBitmap(drawable);
                        holder.mShowImage.setImageBitmap(mBitmap);
                    } else {
                        holder.mShowImage.setImageBitmap(bitmap);
                    }
                }
                if (friendsMsg.getTypePic().equals(teamType)) {
                    final int type = friendsMsg.getApplyType();
                    holder.tvFriendName.setText(friendsMsg.getTeamName());
                    if (type == ProtoMessage.ApplyTeamType.attApply_VALUE) {
                        SpannableStringBuilder style = new SpannableStringBuilder(friendsMsg.getUserName() + "申请加入" + friendsMsg.getTeamName() + "群组");
                        style.setSpan(new ForegroundColorSpan(Color.WHITE), 0, friendsMsg.getUserName().length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                        style.setSpan(new StyleSpan(Typeface.BOLD), 0, friendsMsg.getUserName().length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                        holder.tvFriendMsg.setText(style);
                    } else {
                        SpannableStringBuilder style = new SpannableStringBuilder(friendsMsg.getUserName() + "邀请您加入" + friendsMsg.getTeamName() + "群组");
                        style.setSpan(new ForegroundColorSpan(Color.WHITE), 0, friendsMsg.getUserName().length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                        style.setSpan(new StyleSpan(Typeface.BOLD), 0, friendsMsg.getUserName().length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                        holder.tvFriendMsg.setText(style);
                    }
                } else {
                    if (friendsMsg.getApplyInfo() != null && friendsMsg.getApplyInfo().contains(friendsMsg.getUserName())) {
                        SpannableStringBuilder style = new SpannableStringBuilder(friendsMsg.getApplyInfo());
                        style.setSpan(new ForegroundColorSpan(Color.WHITE), 2, friendsMsg.getUserName().length() + 2, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                        style.setSpan(new StyleSpan(Typeface.BOLD), 2, friendsMsg.getUserName().length() + 2, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                        holder.tvFriendMsg.setText(style);
                    }else if(friendsMsg.getApplyInfo() != null){
                        holder.tvFriendMsg.setText(friendsMsg.getApplyInfo());
                    }
                    holder.tvFriendName.setText(friendsMsg.getUserName());
                }
                holder.btnRefuse.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (friendsMsg.getTypePic().equals(teamType)) {
                            refuseAddGroup(position, friendsMsg);
                        } else {
                            refuseAdd(position, friendsMsg);
                        }
                    }
                });
                holder.btnAccept.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (friendsMsg.getTypePic().equals(teamType)) {
                            accepGroupsRequest(position, friendsMsg, friendsMsg.getApplyType());
                        } else {
                            dialogFriendsRequest(position, friendsMsg);
                        }
                    }
                });
            }

            return convertView;
        }

        public class ViewHolder {
            // public TextView tvNumber;
            public TextView tvFriendName;
            public TextView tvFriendMsg;
            public Button btnRefuse;
            public Button btnAccept;
            public ImageView mShowImage;
            public TextView indexTv;
        }

    }

    /**
     * 获取好友头像
     *
     * @param param
     * @return
     */
    public Bitmap getUserFace(FriendsAndTeams param, Context context) {
        Bitmap bmp = null;
        try {
            if (param != null && param.getUserPic() != null && param.getUserPic().length > 0) {
                Log.w("pocdemo", "pic length: " + param.getUserPic().length);
                try {
                    bmp = BitmapFactory.decodeByteArray(param.getUserPic(), 0, param.getUserPic().length);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (bmp == null) {
                Log.e("UserFace", "UserFace is null");
                LetterTileDrawable drawable = new LetterTileDrawable(context.getResources());
                if (param.getUserName() != null && param.getUserName().length() > 0) {
                    drawable.setContactDetails(param.getUserName(), param.getUserName());
                } else {
                    drawable.setContactDetails("1", "1");
                }
                bmp = FriendFaceUtill.drawableToBitmap(drawable);
            }


            Bitmap bitmap1 = Bitmap.createScaledBitmap(bmp, 96, 96,
                    false); // 将图片缩小
            ImageTool ll = new ImageTool(); // 图片头像变成圆型
            bmp = ll.toRoundBitmap(bitmap1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;
    }

    /**
     * 添加备注弹框
     *
     * @param position
     */
    private void dialogFriendsRequest(final int position, final FriendsAndTeams friendsMsg) {
        LayoutInflater factory = LayoutInflater.from(mContext);// 提示框
        final View view = factory.inflate(R.layout.accept_friend_editbox_layout, null);// 这里必须是final的
        final EditText editRemark = (EditText) view.findViewById(R.id.remark_editText);// 获得输入框对象
        editRemark.setText(friendsMsg.getUserName());
        editRemark.setSelection(editRemark.length());
        new AlertDialog.Builder(mContext).setTitle("设置备注")// 提示框标题
                .setView(view).setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String remark = editRemark.getText().toString();
                        if (remark.length() > GlobalStatus.MAX_TEXT_COUNT){
                            ToastR.setToast(mContext, "备注输入过长（最大只能设置16个字符）");
                        }else {
                            acceptAdd(position, friendsMsg, remark, friendsMsg.getPhoneNum());
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

    /**
     * 接受请求
     *
     * @param position
     * @param friendsMsg
     */
    public void acceptAdd(final int position, final FriendsAndTeams friendsMsg, String remark, String phoneNumber) {
        refuseOrAcceptFriend(position, friendsMsg, 0, remark);
    }


    /**
     * 拒绝请求弹框
     *
     * @param position
     * @param friendsMsg
     */
    public void refuseAdd(final int position, final FriendsAndTeams friendsMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext); // 先得到构造器

        builder.setMessage("确定拒绝将" + friendsMsg.getPhoneNum() + "加为好友？").setTitle("提示：").setPositiveButton("确定", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                deleteRequest(position, friendsMsg);
            }
        }).setNegativeButton("取消", new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();

    }

    public void refuseOrAcceptFriend(final int position, final FriendsAndTeams friendsMsg, final int type, String remark) {
        ProtoMessage.AcceptFriend.Builder builder = ProtoMessage.AcceptFriend.newBuilder();
        builder.setFriendPhoneNum(friendsMsg.getPhoneNum());
        builder.setAcceptType(type);
        if (type == ProtoMessage.AcceptType.atAccept_VALUE) {
            builder.setFriendRemark(remark);
        }
        MyService.start(mContext, ProtoMessage.Cmd.cmdAcceptFriend.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(AcceptFriendProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(mContext, filter, getBroadcastManager());
        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                ToastR.setToast(mContext, "连接超时");
//				m_pDialog.dismiss();
            }

            @Override
            public void onGot(Intent i) {
//				m_pDialog.dismiss();
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    backSuccess(type, friendsMsg.getPhoneNum(), friendsMsg, position);
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    //拒绝和接受失败提示
    public void backFail(int type) {
        if (type == 1) {
            ToastR.setToast(mContext, "拒绝失败");
        } else {
            ToastR.setToast(mContext, "接受失败");
        }
    }

    //拒绝和接受成功提示
    public void backSuccess(int type, String phone, final FriendsAndTeams friendsMsg, final int position) {
        if (type == 1) {
            clearListorCache(position, friendsMsg);
            ToastR.setToast(mContext, "拒绝成功");
        } else {
//            new MsgTool().getAcceptInfo(phone, mContext,mBroadcastManger);
            getNewFriend(phone, mContext, friendsMsg, position);
//            mContext.sendBroadcast(new Intent(MainActivity.FRIEND_ACTION));
            ToastR.setToast(mContext, "接受成功");
        }
    }


    public void getNewFriend(final String phone, final Context context, final FriendsAndTeams friendsMsg, final int position) {
        ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
        builder.setPhoneNum(phone);
        MyService.start(context, ProtoMessage.Cmd.cmdGetFriendInfo.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(GetFriendInfoProcesser.ACTION);
        new TimeoutBroadcast(context, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    GlobalImg.reloadImg(context, phone);
                    Intent intent = new Intent(MainActivity.FRIEND_ACTION);
                    intent.putExtra("phone", phone);
                    context.sendBroadcast(intent);
                    clearListorCache(position, friendsMsg);
                }
            }
        });
    }

    /**
     * 拒绝请求网络连接
     *
     * @param position
     * @param friendsMsg
     */

    public void deleteRequest(final int position, final FriendsAndTeams friendsMsg) {

        refuseOrAcceptFriend(position, friendsMsg, 1, "");
    }


    /*
    拒绝添加组
     */
    private void refuseAddGroup(int position, FriendsAndTeams friendsMsg) {
        String remark = "";
        refuseOrAcceptTeam(position, friendsMsg, ProtoMessage.AcceptType.atDeny_VALUE, remark);

    }

    /*
        接受添加组
         */
    private void accepGroupsRequest(final int position, final FriendsAndTeams friendsMsg, int teamType) {
        String msg;
        if (teamType == ProtoMessage.ApplyTeamType.attApply_VALUE) {
            msg = "允许" + friendsMsg.getUserName() + "加入" + friendsMsg.getTeamName() + "群组";
        } else {
            msg = "同意加入" + friendsMsg.getTeamName() + "群组";
        }

        new AlertDialog.Builder(mContext).setTitle("提示：")// 提示框标题
                .setMessage(msg).setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String remark = "";
                        //= editRemark.getText().toString().trim();
                        refuseOrAcceptTeam(position, friendsMsg, ProtoMessage.AcceptType.atAccept_VALUE, remark);
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

    public void refuseOrAcceptTeam(final int position, final FriendsAndTeams friendsMsg, final int type, String remark) {
        ProtoMessage.AcceptTeam.Builder builder = ProtoMessage.AcceptTeam.newBuilder();
        builder.setTeamID(friendsMsg.getTeamID());
        builder.setAcceptType(type);
        builder.setApplyType(friendsMsg.getApplyType());
        if (friendsMsg.getApplyType() == ProtoMessage.ApplyTeamType.attApply_VALUE) {
            builder.setPhoneNum(friendsMsg.getUserPhone());
        } else {
            SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
            String myPhone = preferences.getString("phone", "");
            builder.setPhoneNum(myPhone);
        }
        MyService.start(mContext, ProtoMessage.Cmd.cmdAcceptTeam.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(AcceptGroupProcesser.ACTION);
        new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
//                    backSuccess(type);
                    clearListorCache(position, friendsMsg);
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    /**
     * 清除缓存数据
     *
     * @param position
     * @param friendsMsg
     */
    protected void clearListorCache(int position, FriendsAndTeams friendsMsg) {
        List<FriendsAndTeams> data = adapter.getList();
        int k = -1;
        int i = -1;
        if (friendsMsg.getTypePic().equals(friendType)) {
            for (FriendsAndTeams c : data) {
                ++i;
                if (c.getPhoneNum() == friendsMsg.getPhoneNum()) {
                    k = i;
                    doDeleteCarByCarID(friendsMsg);
                    break;
                }
            }
        } else if (friendsMsg.getTypePic().equals(teamType)) {
            for (FriendsAndTeams c : data) {
                ++i;
                if (c.getTeamID() == friendsMsg.getTeamID()) {
                    k = i;
                    doDeleteCarByCarID(friendsMsg);
                    break;
                }
            }
        }
        if (k >= 0) {
            data.remove(k);
            checkData(data);
        }

    }

    private void checkData(List<FriendsAndTeams> data) {
        int i = 0;
        int t = 0;
        for (FriendsAndTeams af : data) {
            if (af.getTypePic() != null && af.getTypePic().equals(friendType)) {
                i++;
            }
            if (af.getTypePic() != null && af.getTypePic().equals(teamType)) {
                t++;
            }
        }
        if (i == 0) {
            Iterator<FriendsAndTeams> iterator = data.iterator();
            while (iterator.hasNext()) {
                FriendsAndTeams k = iterator.next();
                if (k.getType() != null && k.getType().equals(friendType)) {
                    iterator.remove();
                    break;
                }
            }
        }
        if (t == 0) {
            Iterator<FriendsAndTeams> iterator = data.iterator();
            while (iterator.hasNext()) {
                FriendsAndTeams k = iterator.next();
                if (k.getType() != null && k.getType().equals(teamType)) {
                    iterator.remove();
                    break;
                }
            }
        }
        if (data.size() == 0) {
            listSize = MainActivity.MENU_ZERO;
            Intent intent = new Intent();
            setResult(listSize, intent);
            finish();
        }
        refreshListData(data);
    }

    /**
     * 删除缓存的朋友信息
     *
     * @param friendsMsg
     */
    private void doDeleteCarByCarID(FriendsAndTeams friendsMsg) {
//		GlobalData.getInstance().removeFriendList(friendsMsg.friends.getUserID());
    }

    public void isShowView(int size) {
        if (size > 0) {
            tvNotification.setVisibility(View.GONE);
        } else {
            tvNotification.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 刷新列表
     *
     * @param data
     */
    private void refreshListData(List<FriendsAndTeams> data) {
        isShowView(data.size());
        try {
            listSize = data.size();
            if (adapter == null) {
                Log.d("CHAT", "Excepton: refreshListData: Adapter is null!");
            } else {
                adapter.refresh(data);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_create_group:
                Intent i = new Intent(mContext, CreateGroupActivity.class);
                startActivityForResult(i, 2);
                break;
            // case R.id.im_search:
            // break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        switch (requestCode) {
            case 2:
                if (resultCode == RESULT_OK) {
                    if (data.getIntExtra("data", 0) == 1) {
                        Intent intent = new Intent();
                        setResult(listSize, intent);
                        finish();
                    }
                    if (data.getIntExtra("data", 0) == 0) {
                    }
                }
                break;
            default:
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPullRefreshListView != null) {
            mPullRefreshListView.onRefreshComplete();
        }
    }

    @Override
    protected void onDestroy() {
        if (mNotifyFriendBroadcast != null) {
            mNotifyFriendBroadcast.stop();
        }
        super.onDestroy();
    }
}
