package com.example.jrd48.chat.friend;

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
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.FirstActivity;
import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.MainActivity;
import com.example.jrd48.chat.PicTool.LetterTileDrawable;
import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.protocol.root.GetFriendInfoProcesser;
import com.luobin.dvr.R;
import com.example.jrd48.chat.SQLite.MsgRecordHelper;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.group.MsgTool;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.chat.receiver.InitDataBroadcast;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.DeleteFriendProcesser;
import com.example.jrd48.service.protocol.root.SetFriendInfoProcesser;
import com.luobin.myinfor.MyInforActivity;
import com.luobin.search.friends.map.TeamMemberLocationActivity;
import com.luobin.utils.MyInforTool;

/**
 * Created by Administrator on 2017/3/2.
 */

public class FriendsDetailsActivity extends BaseActivity implements View.OnClickListener {
    Context context;
    RelativeLayout rlRemark;
    RelativeLayout rlLookMapLayout;
    TextView tvRemark;
    TextView tvPhone;
    TextView tvSex;
    TextView tvUserName;
    Button btnSendMsg;
    Button btnDeleteFriend;
    String remark;
    ImageView ivBack;
    ImageView imageView;

    private TextView mTVMyLocation;
    private TextView mTVMyBirthday;
    private TextView myCarId;
    private TextView myPlateNumber;
    private TextView myCarType;
    private TextView myCarBrand;
    public int type;
    private int SET_FRIEND_STAR = 1;
    AppliedFriends appliedFriend;
    public static int LINKMAN = 2;
    public static int CHART = 3;

    private ProgressDialog m_pDialog;
    boolean checkDialog = true;
    private int REQUST_TYPE = 0;
    private int DELETE_FRIEND = 1;
    private int SET_FRIEND_REMARK = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_details);
        context = this;
        initView();
        getIntentMsg();
        InitBroadcast();
        initProgress();
        getFriendDetails();
    }

    private void getFriendDetails() {
        if (!ConnUtil.isConnected(FriendsDetailsActivity.this)){
            return;
        }
        ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
        builder.setPhoneNum(appliedFriend.getPhoneNum());
        MyService.start(context, ProtoMessage.Cmd.cmdGetFriendInfo.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(GetFriendInfoProcesser.ACTION);
        new TimeoutBroadcast(context, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
            }

            @Override
            public void onGot(Intent i) {
                GlobalImg.reloadImg(context, appliedFriend.getPhoneNum());
                DBManagerFriendsList db = new DBManagerFriendsList(context, DBTableName.getTableName(context, DBHelperFriendsList.NAME));
                appliedFriend = db.getFriend(appliedFriend.getPhoneNum());
//                Log.i("helper1", "12 获得单个好友信息 PhoneNum: " + appliedFriend.getPhoneNum() + "  CarType1:"+appliedFriend.getCarBand()+" CarType2:"+appliedFriend.getCarType2()+" CarType3"+appliedFriend.getCarType3()
//                        +" UserName:"+appliedFriend.getUserName()+" UserSex:"+appliedFriend.getUserSex()+" CarNum:"+appliedFriend.getCarNum());
                db.closeDB();
                initData();
            }
        });
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
                            ToastR.setToast(context, "取消删除好友");
                            break;
                        case 2:
                            ToastR.setToast(context, "取消修改备注");
                            break;
                    }

                }
            }
        });
    }

    private void InitBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.FRIEND_ACTION);
        filter.setPriority(Integer.MAX_VALUE);
        registerReceiver(myReceiver, filter);
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String deletePhone = intent.getStringExtra("phone");
            String str = intent.getStringExtra("delete");
            if (deletePhone != null && str != null && str.length() > 0 && deletePhone.equals(appliedFriend.getPhoneNum())) {
                finish();
            }
        }
    };

    private void initView() {
        rlRemark = (RelativeLayout) findViewById(R.id.rl_set_remark);
        rlRemark.setOnClickListener(this);
        rlLookMapLayout = (RelativeLayout)findViewById(R.id.look_map_layout);
        rlLookMapLayout.setOnClickListener(this);
        tvRemark = (TextView) findViewById(R.id.tv_friend_name);
        tvPhone = (TextView) findViewById(R.id.tv_phone);
        tvSex = (TextView) findViewById(R.id.tv_sex);
        tvUserName = (TextView) findViewById(R.id.tv_user_name);
        btnSendMsg = (Button) findViewById(R.id.btn_send_msg);
        btnSendMsg.setOnClickListener(this);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);
        imageView = (ImageView) findViewById(R.id.image_show);
        btnDeleteFriend = (Button) findViewById(R.id.btn_delete_friend);
        btnDeleteFriend.setOnClickListener(this);

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
        appliedFriend = intent.getParcelableExtra("friend_detail");
        type = intent.getIntExtra("type", 2);
        if (appliedFriend.getPhoneNum().length() <= 0) {
            return;
        }
        initData();
    }

    private void initData() {

        if (appliedFriend.getUserName() != null && !appliedFriend.getUserName().equals("")) {
            tvUserName.setText(appliedFriend.getUserName());
        } else {
            tvUserName.setText("未设置");
        }

        remark = appliedFriend.getNickName();
        if (remark == null || remark.equals("")) {
            remark = appliedFriend.getUserName();
        }
        tvRemark.setText(remark);
        Bitmap bmp = FriendFaceUtill.getUserFace(context, appliedFriend.getPhoneNum());
        if (bmp != null) {
            imageView.setImageBitmap(bmp);
        } else {
            LetterTileDrawable drawable = new LetterTileDrawable(context.getResources());
            drawable.setContactDetails(remark, remark);
            Bitmap bmp1 = FriendFaceUtill.drawableToBitmap(drawable);
            imageView.setImageBitmap(bmp1);
        }
        tvPhone.setText(appliedFriend.getPhoneNum());

        if (appliedFriend.getUserSex() == ProtoMessage.Sex.male_VALUE) {
            tvSex.setText("男");
        } else if (appliedFriend.getUserSex() == ProtoMessage.Sex.female_VALUE) {
            tvSex.setText("女");
        } else {
            tvSex.setText("未设置");
        }

        setMyLocalMsg(appliedFriend.getProv(), appliedFriend.getCity(), appliedFriend.getTown());

//        String time = DateFormatUtil.longToString(myInforTool.getBirthday(),DateFormatUtil.formatType);
        String birthDay = appliedFriend.getBirthday();
        if(TextUtils.isEmpty(birthDay) || birthDay.compareTo("1900-01-01") <= 0){
            mTVMyBirthday.setText("未设置");
        } else {
            mTVMyBirthday.setText(birthDay);
        }
        refreshCarData();
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

    private void refreshCarData() {
        if(TextUtils.isEmpty(appliedFriend.getCarID())){
            myCarId.setText("未绑定");
        } else {
            myCarId.setText(appliedFriend.getCarID() + "");
        }

        if(TextUtils.isEmpty(appliedFriend.getCarNum())){
            myPlateNumber.setText("未设置");
        } else {
            myPlateNumber.setText(appliedFriend.getCarNum() + "");
        }

        if(TextUtils.isEmpty(appliedFriend.getCarBand())){
            myCarBrand.setText("未设置");
        } else {
            myCarBrand.setText(appliedFriend.getCarBand() + "");
        }

        if(TextUtils.isEmpty(appliedFriend.getCarType2())){
            myCarType.setText("未设置");
        } else {
            myCarType.setText(appliedFriend.getCarType2()+ "");
        }
    }

    /**
     * 重写返回键功能
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            resultActivity("");
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_set_remark:
                showSetFriendInfoDialog();
                break;
            case R.id.look_map_layout:
                //TODO 查看地图
                Intent intent1 = new Intent(FriendsDetailsActivity.this, TeamMemberLocationActivity.class);
                intent1.putExtra("linkmanSex", appliedFriend.getUserSex());
                intent1.putExtra("linkmanName", appliedFriend.getUserName());
                intent1.putExtra("linkmanPhone", appliedFriend.getPhoneNum());
                if (appliedFriend.getNickName() != null && appliedFriend.getNickName().length() > 0) {
                    intent1.putExtra("linkNickName", appliedFriend.getNickName());
                } else {
                    intent1.putExtra("linkNickName", appliedFriend.getUserName());
                }
                startActivity(intent1);
                break;
            case R.id.iv_back:
                resultActivity("");
                break;
            case R.id.btn_delete_friend:
                deleteFriendDialog();
                break;
            case R.id.btn_send_msg:
                if (type == LINKMAN) {
                    SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
                    String myPhone = preferences.getString("phone", "");
                    MsgRecordHelper msgRecordHelper = new MsgRecordHelper(context, myPhone + "MsgShow.dp", null);
                    SQLiteDatabase db = msgRecordHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("new_msg", 0);
                    db.update("Msg", values, "phone = ? and msg_from = ?", new String[]{appliedFriend.getPhoneNum(), 0 + ""});
                    db.close();
                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    nm.cancel(0);//消除对应ID的通知
                    new InitDataBroadcast(context).sendBroadcast("");

                    Intent intent = new Intent(context, FirstActivity.class);
                    intent.putExtra("data", 0);
                    intent.putExtra("linkmanSex", appliedFriend.getUserSex());
                    intent.putExtra("linkmanName", remark);
                    intent.putExtra("linkmanPhone", appliedFriend.getPhoneNum());
                    startActivity(intent);
                    finish();
                } else {
                    resultActivity("send_msg");
                }
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


    /**
     * 删除好友提示框
     */
    public void deleteFriendDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context); // 先得到构造器

        builder.setMessage("确定要删除 " + remark + " ？").setTitle("提示：").setPositiveButton("确定", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                deleteFriend();
            }
        }).setNegativeButton("取消", new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();

    }

    /*
    删除好友
     */
    public void deleteFriend() {
        showMyDialog(DELETE_FRIEND);
        if(appliedFriend == null)
            return;
        ProtoMessage.DeleteFriend.Builder builder = ProtoMessage.DeleteFriend.newBuilder();
        builder.setFriendPhoneNum(appliedFriend.getPhoneNum());
        MyService.start(context, ProtoMessage.Cmd.cmdDeleteFriend.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(DeleteFriendProcesser.ACTION);
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
                    MsgTool.deleteFriendsMsg(context, appliedFriend.getPhoneNum());

                    Intent intent = new Intent();
                    intent.putExtra("data", 2);
                    setResult(RESULT_OK, intent);
                    finish();

                    ToastR.setToast(context, "删除好友成功");
                } else {
                    new ResponseErrorProcesser(context, i.getIntExtra("error_code", -1));
                }
            }
        });
    }


    public void resultActivity(String msg) {
        if (type == FirstActivity.REFRESH_REMARK) {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putParcelable("friend_detail", appliedFriend);
            if (msg.length() > 0) {
                intent.putExtra("map", 2);
            }
            intent.putExtra("data", 1);
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    private void showSetFriendInfoDialog() {
        LayoutInflater factory = LayoutInflater.from(context);// 提示框
        final View view = factory.inflate(R.layout.friend_request_editbox_layout, null);// 这里必须是final的
        final TextView remark = (TextView) view.findViewById(R.id.tv_remark_name);
//        final TextView msg = (TextView)view.findViewById(R.id.tv_msg);
//        final EditText edit = (EditText) view.findViewById(R.id.msg_editText);// 获得输入框对象
        final EditText editRemark = (EditText) view.findViewById(R.id.remark_editText);// 获得输入框对象
        final RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.rl_msg);
        rl.setVisibility(View.GONE);
        remark.setText("备注");

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
                    ToastR.setToast(context,"最大只能设置16个字符");
                }
            }
        });
        editRemark.setText(appliedFriend.getNickName());
        editRemark.setSelection(editRemark.length());// 将光标追踪到内容的最后

        final String oldName = appliedFriend.getNickName();
        new AlertDialog.Builder(context).setTitle("备注修改")// 提示框标题
                .setView(view).setPositiveButton("确定", // 提示框的两个按钮
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String re = editRemark.getText().toString();
                        if (re.length() > GlobalStatus.MAX_TEXT_COUNT){
                            ToastR.setToast(context, "备注输入过长（最大只能设置16个字符）");
                        } else {
                            if (oldName.equals(re) == false) {
                                SharedPreferencesUtils.put(context, "friend_list_changed", true);
                            }
                            setFriendInfo(re);
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

    private void setFriendInfo(final String re) {
        if (re.length() <= 1) {
            ToastR.setToast(context, "输入字符/汉字必须是两位或者两位以上");
            return;
        }
        showMyDialog(SET_FRIEND_REMARK);
        ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
        builder.setNickName(re);
        builder.setPhoneNum(appliedFriend.getPhoneNum());
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
                        if (TextUtils.isEmpty(re)){
                            remark = appliedFriend.getUserName();
                        } else {
                            remark = re;
                        }
                        appliedFriend.setNickName(remark);
                        tvRemark.setText(remark);
                        DBManagerFriendsList db = new DBManagerFriendsList(context, DBTableName.getTableName(context, DBHelperFriendsList.NAME));
                        db.updateFriendNickName(appliedFriend);
                        db.closeDB();
                        SharedPreferencesUtils.put(context, "friend_list_changed", true);
                        ToastR.setToast(context, "好友备注设置成功");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    new ResponseErrorProcesser(context, i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        try {

            if (myReceiver != null) {
                unregisterReceiver(myReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
