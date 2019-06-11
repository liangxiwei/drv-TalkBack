package com.luobin.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.CircleImageView;
import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.PicTool.LetterTileDrawable;
import com.example.jrd48.chat.SQLite.CommonUtil;
import com.example.jrd48.chat.SQLite.TeamMemberHelper;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.example.jrd48.chat.friend.FriendsDetailsActivity;
import com.example.jrd48.chat.group.MsgTool;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.ApplyFriendProcesser;
import com.example.jrd48.service.protocol.root.DeleteFriendProcesser;
import com.example.jrd48.service.protocol.root.FriendsListProcesser;
import com.example.jrd48.service.protocol.root.SetFriendInfoProcesser;
import com.luobin.dvr.R;
import com.luobin.model.SearchStrangers;
import com.luobin.myinfor.MyInforActivity;
import com.luobin.search.friends.map.TeamMemberLocationActivity;
import com.luobin.utils.ButtonUtils;
import com.luobin.widget.PromptDialog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FriendDetailsDialogActivity extends BaseActivity {

    @BindView(R.id.btn_return)
    Button btnReturn;
    @BindView(R.id.iv_head)
    CircleImageView ivHead;
    @BindView(R.id.tv_user_name)
    TextView tvUserName;
    @BindView(R.id.tv_autograph)
    TextView tvAutograph;
    @BindView(R.id.tv_plate_number)
    TextView tvPlateNumber;
    @BindView(R.id.tv_location)
    TextView tvLocation;
    @BindView(R.id.tv_home)
    TextView tvHome;
    @BindView(R.id.tv_industry)
    TextView tvIndustry;
    @BindView(R.id.tv_hobby)
    TextView tvHobby;
    @BindView(R.id.btn_map)
    Button btnMap;
    @BindView(R.id.btn_trail)
    Button btnTrail;
    @BindView(R.id.btn_add_friend)
    Button btnAddFriend;


    String userPhone;
    @BindView(R.id.tv_sex)
    TextView tvSex;
    @BindView(R.id.btn_delete_friend)
    Button btnDeteleFriend;
    private String myPhone;
    private String userName;
    private long teamID = -1;
    private AppliedFriends appliedFriend;
    private List<AppliedFriends> listMembersCache;
    SearchStrangers mSearchStrangers;
    ProtoMessage.UserInfo userInfo;
    boolean isFriends = false;

    public static final String TAG = "FriendDetailsDialogActivity";

    private ProgressDialog m_pDialog;
    boolean checkDialog = true;
//    boolean showDialog = true;

    public static  int REQUST_TYPE = 0;
    public static final  int ADD_FRIEND = 1;
    public static final int DELETE_MEMBER = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_details_dialog);
        ButterKnife.bind(this);
        getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        initProgress();
        getIntentMsg();
        initData();

    }

    private void refreshUI() {
        if (isFriends) { // 如果是好友
            userPhone = appliedFriend.getPhoneNum();
            userName = appliedFriend.getUserName();
            if (appliedFriend != null) {
                if (TextUtils.isEmpty(userName)) {
                    userName = "未设置";
                }
                tvUserName.setText(userName);

                String remark = appliedFriend.getNickName();
                if (remark == null || remark.equals("")) {
                    remark = appliedFriend.getUserName();
                }
                Bitmap bmp = FriendFaceUtill.getUserFace(mContext, userPhone);
                if (bmp != null) {
                    ivHead.setImageBitmap(bmp);
                } else {
                    LetterTileDrawable drawable = new LetterTileDrawable(this.getResources());
                    drawable.setContactDetails(remark, remark);
                    Bitmap bmp1 = FriendFaceUtill.drawableToBitmap(drawable);
                    ivHead.setImageBitmap(bmp1);
                }

                userPhone = appliedFriend.getPhoneNum();
                if (appliedFriend.getUserSex() == ProtoMessage.Sex.male_VALUE) {
                    tvSex.setText("<男 " + userPhone + ">");
                } else if (appliedFriend.getUserSex() == ProtoMessage.Sex.female_VALUE) {
                    tvSex.setText("<女 " + userPhone + ">");
                } else {
                    tvSex.setText("<未设置 " + userPhone + ">");
                }

                if (TextUtils.isEmpty(appliedFriend.getCarNum())) {
                    tvPlateNumber.setText("车牌：未绑定");
                } else {
                    tvPlateNumber.setText("车牌：" + appliedFriend.getCarNum() + "");
                }

                setMyLocalMsg(appliedFriend.getProv(), appliedFriend.getCity(), appliedFriend.getTown());
            }
            btnAddFriend.setVisibility(View.GONE);
            btnDeteleFriend.setVisibility(View.VISIBLE);
        } else {
            // 如果不是好友
            if (userInfo != null) {
                if (TextUtils.isEmpty(userName)) {
                    userName = "未设置";
                }
                userPhone = userInfo.getPhoneNum();
                tvUserName.setText(userName);

                Bitmap bmp = FriendFaceUtill.getUserFace(mContext, userPhone);
                ivHead.setImageBitmap(bmp);

                if (userInfo.getUserSex() == ProtoMessage.Sex.male_VALUE) {
                    tvSex.setText("<男 " + userPhone + ">");
                } else if (userInfo.getUserSex() == ProtoMessage.Sex.female_VALUE) {
                    tvSex.setText("<女 " + userPhone + ">");
                } else {
                    tvSex.setText("<未设置 " + userPhone + ">");
                }

                if (TextUtils.isEmpty(userInfo.getCarNum())) {
                    tvPlateNumber.setText("车牌：未绑定");
                } else {
                    tvPlateNumber.setText("车牌：" + userInfo.getCarNum() + "");
                }
                setMyLocalMsg(userInfo.getProv(), userInfo.getCity(), userInfo.getTown());
            }else if (mSearchStrangers != null){
                userPhone = mSearchStrangers.getPhoneNum();
                userName = mSearchStrangers.getUserName();
                if (TextUtils.isEmpty(userName)) {
                    userName = "未设置";
                }
                tvUserName.setText(userName);

                Bitmap bmp = FriendFaceUtill.getUserFace(mContext, userPhone);
                ivHead.setImageBitmap(bmp);

                if (mSearchStrangers.getUserSex() == ProtoMessage.Sex.male_VALUE) {
                    tvSex.setText("<男 " + userPhone + ">");
                } else if (mSearchStrangers.getUserSex() == ProtoMessage.Sex.female_VALUE) {
                    tvSex.setText("<女 " + userPhone + ">");
                } else {
                    tvSex.setText("<未设置 " + userPhone + ">");
                }

                if (TextUtils.isEmpty(mSearchStrangers.getCarNum())) {
                    tvPlateNumber.setText("车牌：未绑定");
                } else {
                    tvPlateNumber.setText("车牌：" + mSearchStrangers.getCarNum() + "");
                }
                setMyLocalMsg(mSearchStrangers.getProv(), mSearchStrangers.getCity(), mSearchStrangers.getTown());

            }

            if (userPhone != myPhone){
                btnAddFriend.setVisibility(View.VISIBLE);
                btnDeteleFriend.setVisibility(View.GONE);
            }else{
                btnAddFriend.setVisibility(View.GONE);
                btnDeteleFriend.setVisibility(View.GONE);
            }


        }
    }


    @OnClick({R.id.btn_return, R.id.btn_map, R.id.btn_trail, R.id.btn_add_friend,R.id.btn_delete_friend})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_return:
                finish();
                break;
            case R.id.btn_map:
                Intent mapIntent = new Intent(mContext, TeamMemberLocationActivity.class);
                if (appliedFriend != null){
                    mapIntent.putExtra("linkmanSex", appliedFriend.getUserSex());
                    mapIntent.putExtra("linkmanName", appliedFriend.getUserName());
                    mapIntent.putExtra("linkmanPhone", appliedFriend.getPhoneNum());
                    if (appliedFriend.getNickName() != null && appliedFriend.getNickName().length() > 0) {
                        mapIntent.putExtra("linkNickName", appliedFriend.getNickName());
                    } else {
                        mapIntent.putExtra("linkNickName", appliedFriend.getUserName());
                    }
                }else if (userInfo != null){
                    mapIntent.putExtra("linkmanSex", userInfo.getUserSex());
                    mapIntent.putExtra("linkmanName", userInfo.getUserName());
                    mapIntent.putExtra("linkmanPhone", userInfo.getPhoneNum());
                    if (userInfo.getNickName() != null && userInfo.getNickName().length() > 0) {
                        mapIntent.putExtra("linkNickName", userInfo.getNickName());
                    } else {
                        mapIntent.putExtra("linkNickName", userInfo.getUserName());
                    }
                }else if (mSearchStrangers != null){
                    mapIntent.putExtra("linkmanSex", mSearchStrangers.getUserSex());
                    mapIntent.putExtra("linkmanName", mSearchStrangers.getUserName());
                    mapIntent.putExtra("linkmanPhone", mSearchStrangers.getPhoneNum());
                    if (mSearchStrangers.getNickName() != null && mSearchStrangers.getNickName().length() > 0) {
                        mapIntent.putExtra("linkNickName", mSearchStrangers.getNickName());
                    } else {
                        mapIntent.putExtra("linkNickName", mSearchStrangers.getUserName());
                    }
                }

                startActivity(mapIntent);
                break;
            case R.id.btn_trail:
                break;
            case R.id.btn_add_friend:
                if (userPhone != myPhone) {
                    toAddLinkMan();
                } else {
                    ToastR.setToast(mContext, "不能添加自己");
                }
                break;
            case R.id.btn_delete_friend:
                deleteFriendDialog();
                break;

    }
    }


    private void toAddLinkMan() {
        LayoutInflater factory = LayoutInflater.from(mContext);// 提示框
        final View view = factory.inflate(R.layout.applied_add_linkman, null);// 这里必须是final的
        final TextView userName = (TextView) view.findViewById(R.id.user_name);
        final EditText nickName = (EditText) view.findViewById(R.id.nick_name);
        final EditText etMsg = (EditText) view.findViewById(R.id.et_msg);
        final TextView ok = (TextView) view.findViewById(R.id.ok);
        final TextView cancel = (TextView) view.findViewById(R.id.cancel);

        if (!TextUtils.isEmpty(this.userName)) {
            userName.setText(this.userName);
          /*  String name = teamMemberInfo.getNickName();
            if (TextUtils.isEmpty(name)) {
                name = teamMemberInfo.getUserName();
            }
            nickName.setText(name);
            nickName.setSelection(nickName.length());*/
        } else {
            userName.setText(this.userPhone);
            nickName.setText(this.userPhone);
        }
        SharedPreferences preferences = getSharedPreferences("token", mContext.MODE_PRIVATE);
        String myPhone = preferences.getString("phone", "");
        String userNameMe = preferences.getString("name", myPhone);
        etMsg.setText("我是" + userNameMe + ",请求加您为好友，谢谢。");
        final AlertDialog dialog = new AlertDialog.Builder(mContext)// 提示框标题
                .setView(view)

                /*.setPositiveButton("确定", // 提示框的两个按钮
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                    }

                })*/
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                                ButtonUtils.changeLeftOrRight(true);
                                return true;
                            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                                ButtonUtils.changeLeftOrRight(false);
                                return true;
                            }
                        }
                        return false;
                    }
                })
//                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//
//                    }
//                })
                .create();

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = etMsg.getText().toString().trim();
                String remark = nickName.getText().toString().trim();
                if (remark.length() <= 0) {
                    ToastR.setToast(mContext, "备注输入不能为空");
                    return;
                }
                if (msg.length() <= 0) {
                    ToastR.setToast(mContext, "验证信息输入不能为空");
                    return;
                }
                Log.d("jim", "12:" + remark);
                if (remark.length() > GlobalStatus.MAX_TEXT_COUNT) {
                    ToastR.setToast(mContext, "备注输入过长（最大只能设置16个字符）");
//                            remark = remark.substring(0,16);
//                            Log.d("jim","123:"+remark);
                    return;
                }
                addFriendsRequest(remark, msg);
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * 加好友网络请求
     *
     * @param remark
     * @param msg    phoneNum
     */
    private void addFriendsRequest(final String remark, String msg) {
        showMyDialog(ADD_FRIEND);
        ProtoMessage.ApplyFriend.Builder builder = ProtoMessage.ApplyFriend.newBuilder();
        builder.setFriendPhoneNum(this.userPhone);
        builder.setApplyInfo(msg);
        builder.setApplyRemark(remark);
        MyService.start(mContext, ProtoMessage.Cmd.cmdApplyFriend.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(ApplyFriendProcesser.ACTION);
        new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

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
                    refreshUI();
                } else {
                    new ResponseErrorProcesser(mContext, i.getIntExtra("error_code", -1));
                }
            }
        });
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


    private void initProgress() {
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
                        case ADD_FRIEND:
                            ToastR.setToast(mContext, "取消添加好友");
                            break;
                        case DELETE_MEMBER:
                            ToastR.setToast(mContext, "取消删除好友");
                            break;

                    }

                }
            }
        });
    }


    private void getIntentMsg() {
        Intent intent = getIntent();
        userPhone = intent.getStringExtra("userPhone");
        userName = intent.getStringExtra("userName");
        teamID = intent.getLongExtra("teamID", -1);
        appliedFriend = intent.getParcelableExtra("appliedFriends");
        mSearchStrangers = (SearchStrangers) intent.getSerializableExtra("user_msg");
        SharedPreferences preferences = getSharedPreferences("token", mContext.MODE_PRIVATE);
        myPhone = preferences.getString("phone", "");

        if (appliedFriend == null) {
            if (teamID < 0){
                return;
            }
            TeamMemberHelper teamMemberHelper = new TeamMemberHelper(mContext, teamID + "TeamMember.dp", null);
            SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
            String selection = "user_phone = ? ";
            String[] selectionArgs = new String[]{userPhone};
            Cursor c = db.query("LinkmanMember", null, selection, selectionArgs, null, null, null);
            if (c == null || c.getCount() < 1) {
                userInfo = null;
            } else {
                c.moveToFirst();
                ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
                builder.setPhoneNum(myPhone);
                builder.setCarID(CommonUtil.makeNotNull(c.getString(c.getColumnIndex(DBHelperFriendsList.CAR_ID))));//String
                builder.setUserSex(c.getInt(c.getColumnIndex("userSex")));
                builder.setCity(CommonUtil.makeNotNull(c.getString(c.getColumnIndex(DBHelperFriendsList.CITY))));//String
                builder.setProv(CommonUtil.makeNotNull(c.getString(c.getColumnIndex(DBHelperFriendsList.PROV))));//String
                builder.setTown(CommonUtil.makeNotNull(c.getString(c.getColumnIndex(DBHelperFriendsList.TOWN))));//String
                builder.setBirthday(CommonUtil.makeNotNull(c.getString(c.getColumnIndex(DBHelperFriendsList.BIRTHDAY))));//Long
                builder.setCarNum(CommonUtil.makeNotNull(c.getString(c.getColumnIndex(DBHelperFriendsList.CAR_NUM))));//String
                builder.setCarType1(CommonUtil.makeNotNull(c.getString(c.getColumnIndex(DBHelperFriendsList.CAR_BAND))));//String
                builder.setCarType2(CommonUtil.makeNotNull(c.getString(c.getColumnIndex(DBHelperFriendsList.CAR_TYPE2))));//String
                builder.setCarType3(CommonUtil.makeNotNull(c.getString(c.getColumnIndex(DBHelperFriendsList.CAR_TYPE3))));//String

                userInfo = builder.build();
            }

            if (c != null) {
                c.close();
                db.close();
            }
        } else {
            isFriends = true;
        }

    }

    private void initData() {
        if (appliedFriend == null) {
            try {
                DBManagerFriendsList db = new DBManagerFriendsList(mContext, DBTableName.getTableName(mContext, DBHelperFriendsList.NAME));
                listMembersCache = db.getFriends(false);
                db.closeDB();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (listMembersCache != null && listMembersCache.size() > 0) {
                for (AppliedFriends vm : listMembersCache) {
                    Log.d(TAG, "vm.getPhoneNum() = " + vm.getPhoneNum());
                    Log.d(TAG, "userPhone = " + userPhone);
                    if (userPhone.equals(vm.getPhoneNum())) {
                        appliedFriend = vm;
                        isFriends = true;
                        break;
                    }
                }
                refreshUI();
            } else {
                Log.d(TAG, "listMembersCache = null");
                loadFriendsForNet();
            }
        } else {
            refreshUI();
        }
    }


    private void loadFriendsForNet() {
        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
        MyService.start(mContext, ProtoMessage.Cmd.cmdGetFriendList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(FriendsListProcesser.ACTION);
        new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {

                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    try {
                        GlobalImg.clear();
                        DBManagerFriendsList db = new DBManagerFriendsList(mContext, DBTableName.getTableName(mContext, DBHelperFriendsList.NAME));
                        listMembersCache = db.getFriends(false);
                        db.closeDB();
                        if (listMembersCache != null && listMembersCache.size() > 0) {
                            for (AppliedFriends vm : listMembersCache) {
                                if (userPhone.equals(vm.getPhoneNum())) {
                                    appliedFriend = vm;
                                    isFriends = true;
                                    break;
                                }
                            }
                        }
                        refreshUI();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {

                }
            }
        });
    }


    private void setMyLocalMsg(String province, String city, String town) {
        city = (city == null ? "" : city.trim());
        town = (town == null ? "" : town.trim());
        province = (province == null ? "" : province.trim());
        String st = " ";
        String str1 = "不限";
        String str2 = " ";
        String str3 = " ";
        String str = "";
        if (TextUtils.isEmpty(province) && TextUtils.isEmpty(city) && TextUtils.isEmpty(town)) {
            str = MyInforActivity.NOT_SET;

        } else if (city.length() <= 0 && town.length() > 0) {
            str1 = MyInforActivity.OTHER;
            str2 = MyInforActivity.OTHER;
            str3 = town;
            str = town;
        } else if (province.length() > 0 && city.length() > 0 && province.equals(city)) {
            str = city + "-" + town;
            str1 = province;
            str2 = city;
            str3 = town;
        } else if (province.length() > 0 && city.length() <= 0 && town.length() <= 0) {
            str = province;
        } else {
            str = province + "-" + city + "-" + town;
            str1 = province;
            str2 = city;
            str3 = town;
        }

        if (str.contains("0")) {
            str1 = "不限";
            str2 = "";
            str3 = "";
        }

        tvLocation.setText("所在地：" + str);
    }

    /**
     * 删除好友提示框
     */
    public void deleteFriendDialog() {

        PromptDialog dialog = new PromptDialog(mContext);
        dialog.show();
        dialog.setTitle("提示：");
        dialog.setMessage("确定要删除 " + userName + " ？");
        dialog.setOkListener("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                deleteFriend();
            }
        });
        dialog.setCancelListener("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


    }

    /*
    删除好友
     */
    public void deleteFriend() {
        showMyDialog(DELETE_MEMBER);
        if(appliedFriend == null)
            return;
        ProtoMessage.DeleteFriend.Builder builder = ProtoMessage.DeleteFriend.newBuilder();
        builder.setFriendPhoneNum(appliedFriend.getPhoneNum());
        MyService.start(mContext, ProtoMessage.Cmd.cmdDeleteFriend.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(DeleteFriendProcesser.ACTION);
        new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
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
                    MsgTool.deleteFriendsMsg(mContext, appliedFriend.getPhoneNum());

                    Intent intent = new Intent();
                    intent.putExtra("data", 2);
                    setResult(RESULT_OK, intent);
                    finish();

                    ToastR.setToast(mContext, "删除好友成功");
                } else {
                    new ResponseErrorProcesser(mContext, i.getIntExtra("error_code", -1));
                }
            }
        });
    }


}
