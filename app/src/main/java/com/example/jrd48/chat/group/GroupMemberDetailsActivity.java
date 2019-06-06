package com.example.jrd48.chat.group;

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
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.FirstActivity;
import com.example.jrd48.chat.MainActivity;
import com.example.jrd48.chat.PicTool.LetterTileDrawable;
import com.example.jrd48.chat.SQLite.CommonUtil;
import com.example.jrd48.chat.TeamMemberInfoList;
import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.protocol.root.TeamMemberProcesser;
import com.luobin.dvr.R;
import com.example.jrd48.chat.SQLite.MsgRecordHelper;
import com.example.jrd48.chat.SQLite.TeamMemberHelper;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.TeamMemberInfo;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.chat.receiver.InitDataBroadcast;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.ApplyFriendProcesser;
import com.example.jrd48.service.protocol.root.AssignTeamAdminProcesser;
import com.example.jrd48.service.protocol.root.ChangeTeamMemberNickNameProcesser;
import com.example.jrd48.service.protocol.root.DeleteTeamMemberProcesser;
import com.example.jrd48.service.protocol.root.ModifyTeamMemberPriorityProcesser;
import com.example.jrd48.service.protocol.root.SetFriendInfoProcesser;
import com.luobin.myinfor.MyInforActivity;
import com.luobin.utils.ButtonUtils;

import java.util.List;

/**
 * Created by Administrator on 2017/3/2.
 */

public class GroupMemberDetailsActivity extends BaseActivity implements View.OnClickListener {

    Context context;
    RelativeLayout rlRemark;
    RelativeLayout rlMemberRole;
    RelativeLayout rlMemberPriority;
    private RelativeLayout rlAddFriend;
    TextView tvRemark;
    TextView tvPhone;
    TextView tvRole;
    TextView tvPriority;
    TextView tvUserName;
    Button btnSendMsg;
    Button btnDelete;
    ImageView ivBack;
    ImageView imageView;

    private TextView tvSex;
    private TextView mTVMyLocation;
    private TextView mTVMyBirthday;
    private TextView myCarId;
    private TextView myPlateNumber;
    private TextView myCarType;
    private TextView myCarBrand;

    public int type;
    public int MyRole;
    private String myPhone;
    private long teamID;
    List<AppliedFriends> listMembersCache;
    private int SET_FRIEND_STAR = 1;
    TeamMemberInfo teamMemberInfo;
    AppliedFriends mAppliedFriends;
    ProtoMessage.UserInfo userInfo;
    boolean isFriends = false;
    public static int CHART_LINK_MAN = 2;
    public static int CHART_TEAM = 3;

    private ProgressDialog m_pDialog;
    boolean checkDialog = true;
//    boolean showDialog = true;

    private int REQUST_TYPE = 0;
    private int DELETE_TEAM_MEMBER = 1;
    private int SET_MEMBER_REMARK = 2;
    private int SET_MY_REMARK = 3;
    private int SET_MEMBER_PRIORITY = 4;
    private int SET_MEMBER_ROLE = 5;
    private int ADD_FRIEND = 6;
    private boolean isRefresh = false;
    boolean isNetworkUpdate = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_member_details);
        context = this;
        initView();
        getIntentMsg();
        getSingleMemberMsg();
        getlistMembersCache();
        initData();
        InitBroadcast();
        initProgress();
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
                        case 1:
                            ToastR.setToast(context, "取消删除群成员");
                            break;
                        case 2:
                            ToastR.setToast(context, "取消修改昵称");
                            break;
                        case 3:
                            ToastR.setToast(context, "取消修改昵称");
                            break;
                        case 4:
                            ToastR.setToast(context, "取消修改话权");
                            break;
                        case 5:
                            ToastR.setToast(context, "取消设置管理员");
                            break;
                        case 6:
                            ToastR.setToast(context, "取消添加好友");
                            break;
                        case 7:
                            ToastR.setToast(context, "取消获取群成员");
                            break;
                        case 8:
                            ToastR.setToast(context, "取消添加好友");
                            break;
                    }

                }
            }
        });
    }


    private void initView() {
        rlRemark = (RelativeLayout) findViewById(R.id.rl_set_remark);
        rlRemark.setOnClickListener(this);
        btnDelete = (Button) findViewById(R.id.btn_delete_friend);
        btnDelete.setOnClickListener(this);
        rlMemberRole = (RelativeLayout) findViewById(R.id.rl_member_role);
        rlMemberRole.setOnClickListener(this);
        rlMemberPriority = (RelativeLayout) findViewById(R.id.rl_member_priority);
        rlMemberPriority.setOnClickListener(this);
        tvRemark = (TextView) findViewById(R.id.tv_friend_name);
        tvPhone = (TextView) findViewById(R.id.tv_phone);
        tvRole = (TextView) findViewById(R.id.tv_role);
        tvPriority = (TextView) findViewById(R.id.tv_priority);
        tvUserName = (TextView) findViewById(R.id.tv_user_name);
        btnSendMsg = (Button) findViewById(R.id.btn_send_msg);
        btnSendMsg.setOnClickListener(this);
        rlAddFriend = (RelativeLayout) findViewById(R.id.rl_add_friend);
        rlAddFriend.setOnClickListener(this);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);
        imageView = (ImageView) findViewById(R.id.image_show);

        tvSex = (TextView) findViewById(R.id.tv_sex);
        mTVMyLocation = (TextView) findViewById(R.id.tv_location);
        mTVMyBirthday = (TextView) findViewById(R.id.tv_birthday);

        //车机部分内容
        myCarId = (TextView) findViewById(R.id.tv_car_id);
        myPlateNumber = (TextView) findViewById(R.id.tv_plate_number);
        myCarType = (TextView) findViewById(R.id.tv_car_type);
        myCarBrand = (TextView) findViewById(R.id.tv_car_brand);
    }

    private void getIntentMsg() {
        Intent intent = getIntent();
        teamMemberInfo = intent.getParcelableExtra("team_detail");
        type = intent.getIntExtra("type", 2);
        teamID = intent.getLongExtra("teamID", 0);
        myPhone = intent.getStringExtra("my_phone");
        MyRole = intent.getIntExtra("role", 0);
        if (teamMemberInfo.getUserPhone().length() <= 0) {
            return;
        }

        TeamMemberHelper teamMemberHelper = new TeamMemberHelper(context, teamID + "TeamMember.dp", null);
        SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
        String selection = "user_phone = ? ";
        String[] selectionArgs = new String[]{teamMemberInfo.getUserPhone()};
        Cursor c = db.query("LinkmanMember", null, selection, selectionArgs, null, null, null);
        if(c == null || c.getCount() < 1){
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

        if(c != null){
            c.close();
            db.close();
        }
        setIntent(null);
    }

    private String getString(String str){
        return str == null ? "":str;
    }

    //获取单个群成员信息
    private void getSingleMemberMsg() {
        if (!ConnUtil.isConnected(this)){
            Log.w("network","当前没有网络（请检查网络是否连接）");
            return;
        }
        ProtoMessage.AcceptTeam.Builder builder = ProtoMessage.AcceptTeam.newBuilder();
        builder.setTeamID(teamID);
        builder.setPhoneNum(teamMemberInfo.getUserPhone());
        MyService.start(context, ProtoMessage.Cmd.cmdGetTeamMember.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(TeamMemberProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(context, filter, getBroadcastManager());
        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                ToastR.setToast(context, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    TeamMemberInfoList list = i.getParcelableExtra(TeamMemberProcesser.KEY);
                    ProtoMessage.UserInfo userInfo1 = (ProtoMessage.UserInfo)i.getSerializableExtra(TeamMemberProcesser.USER_INFO);
                    if (userInfo1 == null){
                    } else {
                        userInfo = userInfo1;
                    }
                    teamMemberInfo = list.getmTeamMemberInfo().get(0);
                    getlistMembersCache();
                    if (mAppliedFriends != null && teamMemberInfo.getUserPhone().equals(mAppliedFriends.getPhoneNum())){
                        if (!TextUtils.isEmpty(mAppliedFriends.getNickName())) {
                            teamMemberInfo.setNickName(mAppliedFriends.getNickName());
                        }
                    }
                    isNetworkUpdate = true;
                    initData();
                } else {
                    //  new ResponseErrorProcesser(context, i.getIntExtra("error_code", -1));
                    Log.e("member","error code:"+i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void getlistMembersCache() {
        //获取好友列表
        try {
            DBManagerFriendsList db = new DBManagerFriendsList(context, DBTableName.getTableName(context, DBHelperFriendsList.NAME));
            listMembersCache = db.getFriends(false);
            db.closeDB();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (AppliedFriends vm : listMembersCache) {
            if (teamMemberInfo.getUserPhone().equals(vm.getPhoneNum())) {
                mAppliedFriends = vm;
                isFriends = true;
                break;
            }
        }
    }

    private void initData() {

        if (teamMemberInfo.getUserName() != null && !teamMemberInfo.getUserName().equals("")) {
            tvUserName.setText(teamMemberInfo.getUserName());
        } else {
            tvUserName.setText("未设置");
        }
        Bitmap bmp = FriendFaceUtill.getUserFace(context, teamMemberInfo.getUserPhone());
        if (bmp != null) {
            imageView.setImageBitmap(bmp);
        } else {
            String str = "1";
            if (teamMemberInfo.getNickName() != null && teamMemberInfo.getNickName().trim().length() >= 0 && !teamMemberInfo.getNickName().equals(teamMemberInfo.getUserName())){
                str = teamMemberInfo.getUserName();
            } else if (!TextUtils.isEmpty(teamMemberInfo.getUserName()) && !teamMemberInfo.getUserName().equals(teamMemberInfo.getUserPhone())){
                str = teamMemberInfo.getUserName();
            }
            LetterTileDrawable drawable = new LetterTileDrawable(context.getResources());
            drawable.setContactDetails(str, str);
            Bitmap bmp1 = FriendFaceUtill.drawableToBitmap(drawable);
            imageView.setImageBitmap(bmp1);
        }
        if (teamMemberInfo.getNickName() != null && !teamMemberInfo.getNickName().equals("")) {
            tvRemark.setText(teamMemberInfo.getNickName());
        } else {
            if (teamMemberInfo.getUserName() != null && !teamMemberInfo.getUserName().equals("")) {
                tvRemark.setText(teamMemberInfo.getUserName());
            } else {
                tvRemark.setText("未设置");
            }
        }
        tvPhone.setText(teamMemberInfo.getUserPhone());
        tvPriority.setText(teamMemberInfo.getMemberPriority() + "");

        if (teamMemberInfo.getRole() == ProtoMessage.TeamRole.Owner_VALUE) {
            tvRole.setText("群主");
        } else if (teamMemberInfo.getRole() == ProtoMessage.TeamRole.Manager_VALUE) {
            tvRole.setText("管理员");
        } else {
            tvRole.setText("群成员");
        }
        boolean isOneself = myPhone.equals(teamMemberInfo.getUserPhone());
        if (isOneself) {
            btnSendMsg.setEnabled(false);
            isFriends = true;
        }
        if (!isFriends) {
            rlRemark.setVisibility(View.GONE);
            btnSendMsg.setVisibility(View.GONE);
            rlAddFriend.setVisibility(View.VISIBLE);
//            btnSendMsg.setText("添加好友");
        } else {
            btnSendMsg.setVisibility(View.GONE);
            rlAddFriend.setVisibility(View.GONE);
            rlRemark.setVisibility(View.VISIBLE);
//            if (isOneself) {
//                btnSendMsg.setText("用户本人");
//            } else {
//                btnSendMsg.setText("发消息");
//                btnSendMsg.setVisibility(View.GONE);
//            }
        }
        boolean isShowDelete = false;
        if (teamMemberInfo.getRole() == ProtoMessage.TeamRole.Owner_VALUE) {
            isShowDelete = false;
        } else if (MyRole == ProtoMessage.TeamRole.Owner_VALUE
                && teamMemberInfo.getRole() == ProtoMessage.TeamRole.Manager_VALUE) {
            isShowDelete = true;
        } else if (MyRole != ProtoMessage.TeamRole.memberOnly_VALUE
                && teamMemberInfo.getRole() == ProtoMessage.TeamRole.memberOnly_VALUE) {
            isShowDelete = true;
        } else {
            isShowDelete = false;
        }
        if (isShowDelete) {
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setText("删除");
        } else {
            if (isOneself && isFriends) {
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.setBackground(getResources().getDrawable(R.drawable.dvr_button_my_background));
                btnDelete.setText("用户本人");

                btnDelete.setEnabled(false);
            } else {
                btnDelete.setVisibility(View.INVISIBLE);
                btnDelete.setEnabled(false);
            }
        }


        if(userInfo != null) {
            if (userInfo.getUserSex() == ProtoMessage.Sex.male_VALUE) {
                tvSex.setText("男");
            } else if (userInfo.getUserSex() == ProtoMessage.Sex.female_VALUE) {
                tvSex.setText("女");
            } else {
                tvSex.setText("未设置");
            }

            setMyLocalMsg(userInfo.getProv(), userInfo.getCity(), userInfo.getTown());

//        String time = DateFormatUtil.longToString(myInforTool.getBirthday(),DateFormatUtil.formatType);
            String birthDay = userInfo.getBirthday();
            if (TextUtils.isEmpty(birthDay) || birthDay.compareTo("1900-01-01") <= 0) {
                mTVMyBirthday.setText("未设置");
            } else {
                mTVMyBirthday.setText(birthDay);
            }
            refreshCarData(userInfo);
        }
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
        } else if(province.length() > 0 && city.length() <= 0 && town.length() <= 0){
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

        mTVMyLocation.setText(str);
    }

    private void refreshCarData(ProtoMessage.UserInfo userInfo) {
        if(TextUtils.isEmpty(userInfo.getCarID())){
            myCarId.setText("未绑定");
        } else {
            myCarId.setText(userInfo.getCarID() + "");
        }

        if(TextUtils.isEmpty(userInfo.getCarNum())){
            myPlateNumber.setText("未设置");
        } else {
            myPlateNumber.setText(userInfo.getCarNum() + "");
        }

        if(TextUtils.isEmpty(userInfo.getCarType1())){
            myCarBrand.setText("未设置");
        } else {
            myCarBrand.setText(userInfo.getCarType1() + "");
        }

        if(TextUtils.isEmpty(userInfo.getCarType2())){
            myCarType.setText("未设置");
        } else {
            myCarType.setText(userInfo.getCarType2()  + "");
        }
    }

    /**
     * 重写返回键功能
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            resultData("");
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_set_remark:
                showChangeMemberRemarkDilog();
                break;
            case R.id.rl_member_priority:
                if (MyRole != ProtoMessage.TeamRole.memberOnly_VALUE) {
                    showChangePriorityDilog();
                }
                break;
            case R.id.rl_member_role:
                boolean isPermissions = false;
                if (MyRole != ProtoMessage.TeamRole.memberOnly_VALUE) {
                    if (MyRole == ProtoMessage.TeamRole.Owner_VALUE) {
                        isPermissions = true;
                    } else if (MyRole == ProtoMessage.TeamRole.Manager_VALUE &&
                            teamMemberInfo.getRole() == ProtoMessage.TeamRole.memberOnly_VALUE) {
                        isPermissions = true;
                    }
                    if (teamMemberInfo.getRole() == ProtoMessage.TeamRole.Owner_VALUE) {
                        isPermissions = false;
                    }
                }

                if (isPermissions) {
                    setPermissions();
                }
                break;
            case R.id.iv_back:
                resultData("");
                break;
            case R.id.btn_send_msg:
                if (!isFriends) {
                    toAddLinkMan();
                } else {
                    toSendMsg();
                }
                break;
            case R.id.rl_add_friend:
                toAddLinkMan();
                break;
            case R.id.btn_delete_friend:
                showDeleteDialog();
                break;
        }
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

    private void showDeleteDialog() {
        String msg = teamMemberInfo.getNickName();
        if (msg == null || msg.equals("")) {
            msg = teamMemberInfo.getUserName();
        }
        new AlertDialog.Builder(context).setTitle("提示：")// 提示框标题
                .setMessage("确定要删除" + msg + "?").setPositiveButton("确定", // 提示框的两个按钮
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deleteTeamMember();
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
     */
    public void deleteTeamMember() {
        showMyDialog(DELETE_TEAM_MEMBER);
        ProtoMessage.AcceptTeam.Builder builder = ProtoMessage.AcceptTeam.newBuilder();
        builder.setTeamID(teamID);
        builder.setPhoneNum(teamMemberInfo.getUserPhone());
        MyService.start(context, ProtoMessage.Cmd.cmdDeleteTeamMember.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(DeleteTeamMemberProcesser.ACTION);
        new TimeoutBroadcast(context, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                dismissDialogShow();
                ToastR.setToast(context, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                dismissDialogShow();
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(context, "删除成员成功");
                    updateDeleteMember();
                    resultDeleteData();
                    //	ToastR.setToast(FirstActivity.this, "获取群成员成功");
                } else {
                    new ResponseErrorProcesser(context, i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    //删除群成员后刷新本地数据
    private void resultDeleteData() {
        Intent intent = new Intent();
        intent.putExtra("phone", teamMemberInfo.getUserPhone());
        intent.putExtra("data", 3);
        setResult(RESULT_OK, intent);
        finish();
    }

    int teamRole;

    //***********************设置权限*******************************
    private void setPermissions() {
        LayoutInflater factory = LayoutInflater.from(context);// 提示框
        final View view = factory.inflate(R.layout.set_member_permissions, null);
        final RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
        final RadioButton radioMember = (RadioButton) view.findViewById(R.id.radio_member);
        final RadioButton radioManager = (RadioButton) view.findViewById(R.id.radio_manager);
        if (teamMemberInfo == null) {
            return;
        }
        if (teamMemberInfo.getRole() == ProtoMessage.TeamRole.memberOnly_VALUE) {
            radioMember.setChecked(true);
        } else {
            radioManager.setChecked(true);
        }

        new AlertDialog.Builder(context).setTitle("设置管理员")// 提示框标题
                .setView(view).setPositiveButton("确定", // 提示框的两个按钮
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (radioMember.isChecked()) {
                            teamRole = ProtoMessage.TeamRole.memberOnly_VALUE;
                        } else if (radioManager.isChecked()) {
                            teamRole = ProtoMessage.TeamRole.Manager_VALUE;
                        }
                        toSendChangePermission(teamRole);
                        dialog.dismiss();
                    }

                }).setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    //设置管理权限
    private void toSendChangePermission(final int role) {
        showMyDialog(SET_MEMBER_ROLE);
        ProtoMessage.AssignTeamAdmin.Builder builder = ProtoMessage.AssignTeamAdmin.newBuilder();
        builder.setTeamID(teamID);
        builder.setPhoneNum(teamMemberInfo.getUserPhone());
        builder.setAdmin(role);
        MyService.start(context, ProtoMessage.Cmd.cmdAssignTeamAdmin.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(AssignTeamAdminProcesser.ACTION);
        new TimeoutBroadcast(context, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                dismissDialogShow();
                ToastR.setToast(context, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                dismissDialogShow();
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(context, "修改权限成功");
                    isRefresh = true;
                    teamMemberInfo.setRole(role);
                    if (teamMemberInfo.getRole() == ProtoMessage.TeamRole.Owner_VALUE) {
                        tvRole.setText("群主");
                    } else if (teamMemberInfo.getRole() == ProtoMessage.TeamRole.Manager_VALUE) {
                        tvRole.setText("管理员");
                    } else {
                        tvRole.setText("群成员");
                    }
                    updateMemberRole();

                } else {
                    new ResponseErrorProcesser(context, i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void showChangePriorityDilog() {

        new ModifyPriorityPrompt().dialogModifyPriorityRequest(context, "话权", teamMemberInfo.getMemberPriority(), new ModifyPrioritytListener() {
            @Override
            public void onOk(int data) {
                toChangePriority(data);
            }
        });

    }

    /*
       设置话权
        */
    private void toChangePriority(final int priority) {
        showMyDialog(SET_MEMBER_PRIORITY);
        ProtoMessage.TeamMember.Builder builder = ProtoMessage.TeamMember.newBuilder();
        builder.setUserPhone(teamMemberInfo.getUserPhone());
        builder.setMemberPriority(priority);
        builder.setTeamID(teamID);
        MyService.start(context, ProtoMessage.Cmd.cmdModifyTeamMemberPriority.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(ModifyTeamMemberPriorityProcesser.ACTION);
        new TimeoutBroadcast(context, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                dismissDialogShow();
                ToastR.setToast(context, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                dismissDialogShow();
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(context, "话权修改成功");
                    isRefresh = true;
                    tvPriority.setText(priority + "");
                    teamMemberInfo.setMemberPriority(priority);
                    updateMemberPriority();
                } else {
                    new ResponseErrorProcesser(context, i.getIntExtra("error_code", -1));
                }
            }
        });

    }


    public void toSendMsg() {

        SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        String myPhone = preferences.getString("phone", "");
        MsgRecordHelper msgRecordHelper = new MsgRecordHelper(context, myPhone + "MsgShow.dp", null);
        SQLiteDatabase db = msgRecordHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("new_msg", 0);
        db.update("Msg", values, "phone = ? and msg_from = ?", new String[]{teamMemberInfo.getUserPhone(), 0 + ""});
        db.close();
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(0);//消除对应ID的通知
        new InitDataBroadcast(context).sendBroadcast("");

        Intent intent = new Intent(context, FirstActivity.class);
        intent.putExtra("data", 0);
        intent.putExtra("linkmanSex", mAppliedFriends.getUserSex());
        String linkmanName = mAppliedFriends.getNickName();
        if (linkmanName == null || linkmanName.equals("")) {
            linkmanName = mAppliedFriends.getUserName();
        }
        intent.putExtra("linkmanName", linkmanName);
        intent.putExtra("linkmanPhone", mAppliedFriends.getPhoneNum());
        startActivity(intent);
        resultData("sendmsg");

    }

    public void resultData(String str) {
        Intent intent = new Intent();
        int strLength = str.length();
        //刷新群聊天界面
        if (strLength <= 0 && type == FirstActivity.REFRESH_TEAM_REMARK && isRefresh) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("team_refresh", teamMemberInfo);
            intent.putExtra("data", 1);
            intent.putExtras(bundle);
        }
        //刷新群成员列表信息
        if (strLength <= 0 && type == ShowAllTeamMemberActivity.REFRESH_ALL_REMARK && isRefresh) {
            Bundle bundle1 = new Bundle();
            bundle1.putParcelable("team_detail_all", teamMemberInfo);
            intent.putExtra("data", 1);
            intent.putExtras(bundle1);
        } else if (isNetworkUpdate) {
            intent.putExtra("phone", teamMemberInfo.getUserPhone());
            Bundle bundle = new Bundle();
            bundle.putParcelable("team_refresh", teamMemberInfo);
            intent.putExtras(bundle);
        }
        //关闭群组相关界面，进人联系人聊天界面
        if (strLength > 0) {
            intent.putExtra("data", 2);
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    /*
   显示修改群名称备注弹框
    */
    private void showChangeMemberRemarkDilog() {
        if (myPhone.equals(teamMemberInfo.getUserPhone())) {
            //修改自己在群里的备注
            LayoutInflater factory = LayoutInflater.from(context);// 提示框
            final View view = factory.inflate(R.layout.change_member_nick_name, null);
            final EditText mEditText = (EditText) view.findViewById(R.id.et_change_nick_name);
            String name = teamMemberInfo.getNickName();
            if (name == null || name.equals("")) {
                name = teamMemberInfo.getUserName();
            } else if (teamMemberInfo.getUserName() == null || teamMemberInfo.getUserName().equals("")) {
                name = teamMemberInfo.getUserPhone();
            }
            mEditText.setText(name);
            mEditText.setSelection(mEditText.length());

            new AlertDialog.Builder(context).setTitle("设置我在群中的名称")// 提示框标题
                    .setView(view).setPositiveButton("确定", // 提示框的两个按钮
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String priority = mEditText.getText().toString().trim();
                            String remark = mEditText.getText().toString().trim();
                            toChangeMemberRemark(remark);
                            dialog.dismiss();
                        }

                    }).setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        } else {
            //修改好友在群里的备注

            showSetFriendInfoDialog();
        }
    }

    /*
       修改自己的群昵称
     */
    private void toChangeMemberRemark(final String remark) {
        showMyDialog(SET_MY_REMARK);
        ProtoMessage.TeamInfo.Builder builder = ProtoMessage.TeamInfo.newBuilder();
        builder.setMyTeamName(remark);
        builder.setTeamID(teamID);
        MyService.start(context, ProtoMessage.Cmd.cmdChangeMyTeamName.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(ChangeTeamMemberNickNameProcesser.ACTION);
        new TimeoutBroadcast(context, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                dismissDialogShow();
                ToastR.setToast(context, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                dismissDialogShow();
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    isRefresh = true;
                    teamMemberInfo.setNickName(remark);
                    tvRemark.setText(remark);
                    updateMemberNickName();
                    ToastR.setToast(context, "修改昵称成功");
                } else {
                    new ResponseErrorProcesser(context, i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void InitBroadcast() {
        IntentFilter filterDelete = new IntentFilter();
        filterDelete.addAction("ACTION.refreshTeamList");
        registerReceiver(refreshTeamReceiver, filterDelete);

        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.FRIEND_ACTION);
        filter.setPriority(Integer.MAX_VALUE);
        registerReceiver(myReceiver, filter);
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
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String deletePhone = intent.getStringExtra("phone");
            String str = intent.getStringExtra("delete");
            if (deletePhone != null && deletePhone.length() > 0 && deletePhone.equals(teamMemberInfo.getUserPhone())) {
                getlistMembersCache();
                if (str != null && str.equals(MainActivity.deleteStr)) {
                    isFriends = false;
                }
                initData();
            }
        }
    };

    private void showSetFriendInfoDialog() {
        String str = "";
        String name = "";
        str = "好友备注修改";
        name = teamMemberInfo.getNickName();

        LayoutInflater factory = LayoutInflater.from(context);// 提示框
        final View view = factory.inflate(R.layout.friend_request_editbox_layout, null);// 这里必须是final的
        final TextView remark = (TextView) view.findViewById(R.id.tv_remark_name);
        final EditText editRemark = (EditText) view.findViewById(R.id.remark_editText);// 获得输入框对象
        final RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.rl_msg);
        rl.setVisibility(View.GONE);
        remark.setText("备注");
        editRemark.setText(name);
        editRemark.setSelection(editRemark.length());// 将光标追踪到内容的最后
        new AlertDialog.Builder(context).setTitle(str)// 提示框标题
                .setView(view).setPositiveButton("确定", // 提示框的两个按钮
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String remark = editRemark.getText().toString();
                        if (remark.length() > GlobalStatus.MAX_TEXT_COUNT) {
                            ToastR.setToast(context, "备注输入过长（最大只能设置16个字符）");
                        } else {
                            setFriendInfo(remark);
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
      设置好友信息
       */
    public void setFriendInfo(final String remark) {
        if (remark.length() <= 1) {
            ToastR.setToast(context, "输入数字必须是两位或者两位以上");
            return;
        }
        showMyDialog(SET_MEMBER_REMARK);
        ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
        builder.setNickName(remark);
        builder.setPhoneNum(teamMemberInfo.getUserPhone());
        MyService.start(context, ProtoMessage.Cmd.cmdSetFriendInfo.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(SetFriendInfoProcesser.ACTION);
        new TimeoutBroadcast(context, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                dismissDialogShow();
                ToastR.setToast(context, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                dismissDialogShow();
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    try {
                        String nackName;
                        if (TextUtils.isEmpty(remark)){
                            nackName = teamMemberInfo.getUserName();
                        } else {
                            nackName = remark;
                        }
                        isRefresh = true;
                        mAppliedFriends.setNickName(nackName);
                        teamMemberInfo.setNickName(nackName);
                        tvRemark.setText(nackName);
//                        updateMemberNickName();
                        DBManagerFriendsList db = new DBManagerFriendsList(context, DBTableName.getTableName(context, DBHelperFriendsList.NAME));
                        db.updateFriendNickName(mAppliedFriends);
                        db.closeDB();

                        onChangeNickName();
                        ToastR.setToast(context, "修改好友昵称成功");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    new ResponseErrorProcesser(context, i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void onChangeNickName() {
        SharedPreferencesUtils.put(this, "friend_list_changed", true);
    }


    public void updateMemberNickName() {
        try {
            TeamMemberHelper teamMemberHelper = new TeamMemberHelper(context, teamID + "TeamMember.dp", null);
            SQLiteDatabase db = teamMemberHelper.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("nick_name", teamMemberInfo.getNickName());
            db.update("LinkmanMember", cv, "user_phone == ?", new String[]{teamMemberInfo.getUserPhone()});
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateMemberRole() {
        try {
            TeamMemberHelper teamMemberHelper = new TeamMemberHelper(context, teamID + "TeamMember.dp", null);
            SQLiteDatabase db = teamMemberHelper.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("role", teamMemberInfo.getRole());
            db.update("LinkmanMember", cv, "user_phone == ?", new String[]{teamMemberInfo.getUserPhone()});
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateMemberPriority() {
        try {
            TeamMemberHelper teamMemberHelper = new TeamMemberHelper(context, teamID + "TeamMember.dp", null);
            SQLiteDatabase db = teamMemberHelper.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("member_priority", teamMemberInfo.getMemberPriority());
            db.update("LinkmanMember", cv, "user_phone == ?", new String[]{teamMemberInfo.getUserPhone()});
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateDeleteMember() {
        try {
            TeamMemberHelper teamMemberHelper = new TeamMemberHelper(context, teamID + "TeamMember.dp", null);
            SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
            db.delete("LinkmanMember", "user_phone == ?", new String[]{teamMemberInfo.getUserPhone()});
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toAddLinkMan() {


        LayoutInflater factory = LayoutInflater.from(context);// 提示框
        final View view = factory.inflate(R.layout.applied_add_linkman, null);// 这里必须是final的
        final TextView userName = (TextView) view.findViewById(R.id.user_name);
        final EditText nickName = (EditText) view.findViewById(R.id.nick_name);
        final EditText etMsg = (EditText) view.findViewById(R.id.et_msg);
        if (!TextUtils.isEmpty(teamMemberInfo.getUserName())) {
            userName.setText(teamMemberInfo.getUserName());
            String name = teamMemberInfo.getNickName();
            if (TextUtils.isEmpty(name)) {
                name = teamMemberInfo.getUserName();
            }
            nickName.setText(name);
            nickName.setSelection(nickName.length());
        } else {
            userName.setText(teamMemberInfo.getUserPhone());
            nickName.setText(teamMemberInfo.getUserPhone());
        }
        SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        String myPhone = preferences.getString("phone", "");
        String userNameMe = preferences.getString("name", myPhone);
        etMsg.setText("我是" + userNameMe + ",请求加您为好友，谢谢。");
        new AlertDialog.Builder(context)// 提示框标题
                .setView(view).setPositiveButton("确定", // 提示框的两个按钮
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String msg = etMsg.getText().toString().trim();
                        String remark = nickName.getText().toString().trim();
                        if (remark.length()<= 0){
                            ToastR.setToast(context, "备注输入不能为空");
                            return;
                        }
                        if (msg.length() <= 0){
                            ToastR.setToast(context, "验证信息输入不能为空");
                            return;
                        }
                        Log.d("jim","12:"+remark);
                        if (remark.length() > GlobalStatus.MAX_TEXT_COUNT) {
                            ToastR.setToast(context, "备注输入过长（最大只能设置16个字符）");
//                            remark = remark.substring(0,16);
//                            Log.d("jim","123:"+remark);
                            return;
                        }
                        addFriendsRequest(remark, msg);
                        dialog.dismiss();
                    }

                })
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                        if(keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
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
                .create().show();

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
        builder.setFriendPhoneNum(teamMemberInfo.getUserPhone());
        builder.setApplyInfo(msg);
        builder.setApplyRemark(remark);
        MyService.start(context, ProtoMessage.Cmd.cmdApplyFriend.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(ApplyFriendProcesser.ACTION);
        new TimeoutBroadcast(context, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                dismissDialogShow();
                ToastR.setToast(context, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                dismissDialogShow();
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {

                    ToastR.setToast(context, "请求成功，等待对方回应");
                    resultData("");

                } else {
                    new ResponseErrorProcesser(context, i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        try {
            if (refreshTeamReceiver != null) {
                unregisterReceiver(refreshTeamReceiver);
            }

            if (myReceiver != null) {
                unregisterReceiver(myReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

}
