package com.example.jrd48.chat.friend;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.ImageTool;
import com.example.jrd48.chat.PicTool.LetterTileDrawable;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.ApplyFriendProcesser;
import com.luobin.dvr.R;
import com.luobin.model.SearchStrangers;
import com.luobin.search.friends.SearchReturnActivity;

import java.util.List;

public class ShowFriendMsgActivity extends BaseActivity implements OnClickListener {
    private TextView tvName;
    private TextView tvPhone;
    private TextView tvProvince;
    private TextView tvBirthday;
    private TextView tvSex;
    private TextView tvCarBand;
    private TextView tvCarType;
    TextView tvCarNumber;
    private ImageView mImageShow;
    private Button btnSure;
    private String userName;
    private String fromActivity;
    String phoneNum;
    long userId;
    String userNameMe;   //自己用户名，或手机号码
    String mStrNoValue = "未设置";
    private String[] sexItems = new String[]{"未设置", "男", "女", "未设置"};
    List<AppliedFriends> mFriend;
    SearchStrangers mSearchStrangers;
    private String mMyLocation;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_msg);
        mContext = this;
        loadUserFriendsFromCache();
        initControl();
        getMsg();
    }


    /**
     * 控件初始化
     */
    private void initControl() {
        tvCarNumber = (TextView)findViewById(R.id.tv_car_number);
        tvPhone = (TextView) findViewById(R.id.tv_phone);
        tvProvince = (TextView) findViewById(R.id.tv_province);
        tvBirthday = (TextView) findViewById(R.id.tv_set_birthday);
        tvName = (TextView) findViewById(R.id.tv_friend_name);
        btnSure = (Button) findViewById(R.id.btn_sendmsg);
        tvSex = (TextView) findViewById(R.id.tv_set_sex);
        mImageShow = (ImageView) findViewById(R.id.image_show);
        btnSure.setOnClickListener(this);
        tvCarBand = (TextView) findViewById(R.id.car_set_brand);
        tvCarType = (TextView) findViewById(R.id.tv_set_type);

    }

    /**
     * 按钮onClick事件重写
     *
     * @param view
     */
    public void back(View view) {
        result();
    }

    private void result() {
        if (fromActivity == null || fromActivity.equals("")) {
            Intent intent = new Intent();
            intent.putExtra("data", 0);
            setResult(SearchReturnActivity.NO_REQUST, intent);

        } else if (fromActivity.equals("new")) {
            Intent intent = new Intent();
            intent.putExtra("data", 0);
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    /**
     * 重写返回键功能
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // 这里重写返回键
            result();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 获取本地朋友列表缓存信息
     */
    private void loadUserFriendsFromCache() {
        try {
            DBManagerFriendsList db = new DBManagerFriendsList(this, DBTableName.getTableName(this, DBHelperFriendsList.NAME));
            mFriend = db.getFriends(false);
            db.closeDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取Activity传来的信息
     */
    private void getMsg() {
        Intent intent = getIntent();
        mSearchStrangers = (SearchStrangers) intent.getSerializableExtra("user_msg");
        if (mSearchStrangers != null) {
            fromActivity = intent.getStringExtra("activity");
        } else {
            mSearchStrangers = (SearchStrangers) intent.getSerializableExtra("user_info");
        }
        if (!TextUtils.isEmpty(mSearchStrangers.getUserName())) {
            if (mSearchStrangers.getUserName().equals(mSearchStrangers.getPhoneNum())) {
                String number = mSearchStrangers.getPhoneNum();
                String str = number.substring(0, 3);
                String str1 = number.substring(8, number.length());
                userName = str + "*****" + str1;
            } else {
                userName = mSearchStrangers.getUserName();
            }
        } else {
            userName = mStrNoValue;
        }
        phoneNum = mSearchStrangers.getPhoneNum();
        if (!TextUtils.isEmpty(mSearchStrangers.getBirthday()) && mSearchStrangers.getBirthday().compareTo("1900-01-01") > 0) {
            tvBirthday.setText(mSearchStrangers.getBirthday());
        } else {
            tvBirthday.setText(mStrNoValue);
        }
        String city = (mSearchStrangers.getCity() == null ? "" : mSearchStrangers.getCity()).toString().trim();
        String town = (mSearchStrangers.getTown() == null ? "" : mSearchStrangers.getTown()).toString().trim();
        String prov = (mSearchStrangers.getProv() == null ? "" : mSearchStrangers.getProv()).toString().trim();
        city = city.replace("0", "");
        town = town.replace("0", "");
        prov = prov.replace("0", "");
        if (prov.length() > 0 && city.length() > 0 && town.length() <= 0) {
            mMyLocation = prov + "-" + city;
        } else if (city.length() > 0 && town.length() > 0) {
            if (prov.equals(city)) {
                mMyLocation = city + "-" + town;
            } else {
                mMyLocation = prov + "-" + city + "-" + town;
            }
        } else if (city.length() <= 0 && town.length() <= 0) {
            mMyLocation = prov;
        } else if (prov.length() <= 0 && city.length() > 0) {
            mMyLocation = city;
        }else if (prov.length() <= 0 && city.length() <= 0 && town.length() > 0) {
            mMyLocation = town;
        }
        if (mMyLocation == null || mMyLocation.length() <= 0) {
            mMyLocation = mStrNoValue;
        }
        tvProvince.setText(mMyLocation);
        if (mSearchStrangers.getCarNum() == null || mSearchStrangers.getCarNum().length() <= 0) {
            tvCarNumber.setText("未设置");
        } else {
            tvCarNumber.setText(mSearchStrangers.getCarNum());
        }
        tvSex.setText(sexItems[mSearchStrangers.getUserSex()]);
        if (mSearchStrangers.getCarType1().length() > 0) {
            tvCarBand.setText(mSearchStrangers.getCarType1());
        } else {
            tvCarBand.setText(mStrNoValue);
        }

        if (mSearchStrangers.getCarType2().length() > 0) {
            tvCarType.setText(mSearchStrangers.getCarType2());
        } else {
            tvCarType.setText(mStrNoValue);
        }
        // userId = param.getUserID();
//		userNameMe = GlobalData.getInstance().getUserInfo().getUserName();

        SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        String myPhone = preferences.getString("phone", "");
        userNameMe = preferences.getString("name", myPhone);
        boolean checkFriends = false;
        for (AppliedFriends friend : mFriend) {
            if (friend.getPhoneNum().equals(phoneNum)) {
                checkFriends = true;
                break;
            }
        }

        if (checkFriends){
            String carNum = null;
            try {
                DBManagerFriendsList db = new DBManagerFriendsList(mContext, DBTableName.getTableName(mContext, DBHelperFriendsList.NAME));
                carNum = db.getCarNum(phoneNum);
                db.closeDB();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (TextUtils.isEmpty(carNum)){
                carNum = mSearchStrangers.getCarNum();
            }
            setCarNum(carNum);
        } else {
            setCarNum(mSearchStrangers.getCarNum());
        }
        if (myPhone.equals(phoneNum)) {
            btnSure.setText("用户本人");
            btnSure.setEnabled(false);
        } else if (checkFriends == true) {
            btnSure.setText("已经是好友");
            btnSure.setEnabled(false);
        } else {
            btnSure.setText("申请加为好友");
        }
        tvName.setText(userName);
//		tvCity.setText(mStrNoValue);
//		tvProvince.setText(mStrNoValue);
        if (checkFriends){
            tvPhone.setText(phoneNum);
        }else {
            String str = phoneNum.substring(0, 3);
            String str1 = phoneNum.substring(8, phoneNum.length());
            tvPhone.setText(str + "*****" + str1);
        }
        if (GlobalImg.getImage(ShowFriendMsgActivity.this, phoneNum) == null) {
            LetterTileDrawable drawable = new LetterTileDrawable(ShowFriendMsgActivity.this.getResources());
            drawable.setContactDetails(userName, userName);
            Bitmap bmp = FriendFaceUtill.drawableToBitmap(drawable);
            mImageShow.setImageBitmap(bmp);
        } else {
            mImageShow.setImageBitmap(GlobalImg.getImage(ShowFriendMsgActivity.this, phoneNum));
        }
    }

    private void setCarNum(String carNum) {
        if (TextUtils.isEmpty(carNum)) {
            tvCarNumber.setText("未设置");
        } else {
            tvCarNumber.setText(carNum);
        }
    }

    /**
     * 获取好友头像
     *
     * @param param
     * @return
     */
    public Bitmap getUserFace(AppliedFriends param) {
        Bitmap bmp = null;
        try {
            if (GlobalImg.getImage(ShowFriendMsgActivity.this, param.getPhoneNum()) == null) {
                Log.e("UserFace", "UserFace is null");
                bmp = BitmapFactory.decodeResource(ShowFriendMsgActivity.this.getResources(), R.drawable.default_useravatar);
            } else {
                bmp = GlobalImg.getImage(ShowFriendMsgActivity.this, param.getPhoneNum());
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

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_sendmsg:
                dialogFriendsRequest();
                break;
            default:
                break;
        }

    }

    /**
     * 添加好友提示框
     */
    private void dialogFriendsRequest() {
        try {
            String msg = "，请求加您为好友，谢谢。";
            AddFriendPrompt.dialogFriendsRequest(this, msg, userName, userNameMe, new AddFriendPromptListener() {
                @Override
                public void onOk(String remark, String msg) {
                    addFriendsRequest(remark, msg);
                }

                @Override
                public void onFail(String typ) {
                    if (typ.equals(AddFriendPrompt.TYP)) {
                        ToastR.setToast(ShowFriendMsgActivity.this, "信息输入不能为空");
                    }else {
                        ToastR.setToast(ShowFriendMsgActivity.this, "备注输入过长（最大只能设置16个字符）");
                    }
                }
            });
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 加好友网络请求
     *
     * @param remark
     * @param msg    phoneNum
     */
    private void addFriendsRequest(String remark, String msg) {
        ProtoMessage.ApplyFriend.Builder builder = ProtoMessage.ApplyFriend.newBuilder();
        builder.setFriendPhoneNum(phoneNum);
        builder.setApplyInfo(msg);
        builder.setApplyRemark(remark);
        MyService.start(ShowFriendMsgActivity.this, ProtoMessage.Cmd.cmdApplyFriend.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(ApplyFriendProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(ShowFriendMsgActivity.this, filter, getBroadcastManager());

        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                ToastR.setToast(ShowFriendMsgActivity.this, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    successBack();
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    public void successBack() {
        ToastR.setToast(ShowFriendMsgActivity.this, "请求成功，等待对方回应");
        searchSuccess();
    }

    public void fail(int i) {
        new ResponseErrorProcesser(ShowFriendMsgActivity.this, i);
    }

    /**
     * 添加成功关闭该Activity
     */
    protected void searchSuccess() {
        if (fromActivity == null || fromActivity.equals("")) {
            Intent intent = new Intent();
            intent.putExtra("number", mSearchStrangers.getPhoneNum());
            setResult(SearchReturnActivity.REQUST, intent);
            finish();
        } else if (fromActivity.equals("new")) {
            Intent intent = new Intent();
            intent.putExtra("data", 1);
            setResult(RESULT_OK, intent);
            finish();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}