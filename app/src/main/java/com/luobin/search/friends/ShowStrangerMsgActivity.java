package com.luobin.search.friends;

import android.app.ProgressDialog;
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
import com.example.jrd48.chat.friend.AddFriendPrompt;
import com.example.jrd48.chat.friend.AddFriendPromptListener;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.ApplyFriendProcesser;
import com.example.jrd48.service.protocol.root.SearchStrangerProcesser;
import com.luobin.dvr.R;
import com.luobin.model.SearchStrangers;
import com.luobin.model.StrangerLocationStatus;

import java.util.List;

public class ShowStrangerMsgActivity extends BaseActivity implements OnClickListener {
    private TextView tvName;
    private TextView tvPhone;
    private TextView tvProvince;
    private TextView tvBirthday;
    private TextView tvSex;
    private TextView tvCarBand;
    private TextView tvCarType;
    private ImageView mImageShow;
    private Button btnSure;
    private String userName;
    private int requstCode;
    String phoneNum;
    long userId;
    String userNameMe;   //自己用户名，或手机号码
    String mStrNoValue = "未设置";
    String carNumber;
    private String mSex;
    private String mBirthday;
    private String mMyLocation;
    private String mBrand;
    TextView tvCarNumber;
    private String mType;
    private String[] sexItems = new String[]{"未设置", "男", "女", "未设置"};
    List<AppliedFriends> mFriend;
    SearchStrangers mSearchStrangers;
    StrangerLocationStatus mStrangerLocationStatus;
    private boolean isFriend = false;
    AppliedFriends mAppliedFriends;
    private Context mContext;
    private ProgressDialog m_pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mContext = this;
        initProgress();
        loadUserFriendsFromCache();
        getMsg();

    }

    private void initProgress() {
        //创建ProgressDialog对象
        m_pDialog = new ProgressDialog(this, R.style.CustomDialog);
        // 设置进度条风格，风格为圆形，旋转的
        m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // 设置ProgressDialog 提示信息
        m_pDialog.setMessage("请稍等,正在加载数据...");
        // 设置ProgressDialog 的进度条是否不明确
        m_pDialog.setIndeterminate(false);
        // 设置ProgressDialog 是否可以按退回按键取消
        m_pDialog.setCancelable(false);
    }

    /**
     * 控件初始化
     */
    private void initControl() {
        tvCarNumber = (TextView)findViewById(R.id.tv_car_number);
        tvPhone = (TextView) findViewById(R.id.tv_phone);
        tvName = (TextView) findViewById(R.id.tv_friend_name);
        btnSure = (Button) findViewById(R.id.btn_sendmsg);
        tvSex = (TextView) findViewById(R.id.tv_set_sex);
        mImageShow = (ImageView) findViewById(R.id.image_show);
        btnSure.setOnClickListener(this);
        tvBirthday = (TextView) findViewById(R.id.tv_set_birthday);
        tvProvince = (TextView) findViewById(R.id.tv_province);
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
//        if (requstCode == DistanceSearchReturnActivity.REQUST) {
//            Intent intent = new Intent();
//            intent.putExtra("data", 0);
//            setResult(SearchReturnActivity.NO_REQUST, intent);
//        }
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
        requstCode = intent.getIntExtra("requestCode",0);
        mStrangerLocationStatus = (StrangerLocationStatus) intent.getSerializableExtra("user_info");
        if (mStrangerLocationStatus == null) {
            finish();
        }
        for (AppliedFriends friends : mFriend) {
            if (friends.getPhoneNum().equals(mStrangerLocationStatus.getPhoneNum())) {
                isFriend = true;
                mAppliedFriends = friends;
                break;
            }
        }
        if (!isFriend) {
            toSearchStranger();
        } else {
            toUpdateData(isFriend);
        }
    }

    private void toSearchStranger() {
        m_pDialog.show();
        ProtoMessage.SearchUser.Builder builder = ProtoMessage.SearchUser.newBuilder();
        builder.setPhoneNum(mStrangerLocationStatus.getPhoneNum());
        builder.setOnlyPhoneNum(true);
        MyService.start(mContext, ProtoMessage.Cmd.cmdSearchUser2.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(SearchStrangerProcesser.ACTION);
        new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                m_pDialog.dismiss();
                ToastR.setToast(mContext, "连接超时，网络质量差");
                finish();
            }

            @Override
            public void onGot(Intent i) {
                m_pDialog.dismiss();
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    SearchStrangers searchFriends = (SearchStrangers) i.getSerializableExtra("user_info");
                    if (searchFriends.getPhoneNum() == null || searchFriends.getPhoneNum().length() <= 0) {
                        ToastR.setToast(mContext, "未找到相关用户");
                    } else {
                        mSearchStrangers = searchFriends;
                        toUpdateData(isFriend);
                    }
                } else {
                    new ResponseErrorProcesser(mContext, i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void toUpdateData(boolean isMyFriends) {
        String city;
        String town;
        String prov;
        if (isMyFriends) {
            if (mAppliedFriends.getUserName() != null && mAppliedFriends.getUserName().length() > 0) {
                userName = mAppliedFriends.getUserName();
            } else {
                userName = mStrNoValue;
            }
            phoneNum = mAppliedFriends.getPhoneNum();
            mSex = sexItems[mAppliedFriends.getUserSex()];
            if (!TextUtils.isEmpty(mAppliedFriends.getBirthday()) && mAppliedFriends.getBirthday().compareTo("1900-01-01") > 0) {
                mBirthday = mAppliedFriends.getBirthday();
            } else {
                mBirthday = mStrNoValue;
            }
            city = (mAppliedFriends.getCity() == null ? "" : mAppliedFriends.getCity()).toString().trim();
            town = (mAppliedFriends.getTown() == null ? "" : mAppliedFriends.getTown()).toString().trim();
            prov = (mAppliedFriends.getProv() == null ? "" : mAppliedFriends.getProv()).toString().trim();
            if (mAppliedFriends.getCarBand() != null && mAppliedFriends.getCarBand().length() > 0) {
                mBrand = mAppliedFriends.getCarBand();
            } else {
                mBrand = mStrNoValue;
            }
            if (mAppliedFriends.getCarType2() != null && mAppliedFriends.getCarType2().length() > 0) {
                mType = mAppliedFriends.getCarType2();
            } else {
                mType = mStrNoValue;
            }

            if (mAppliedFriends.getCarNum() != null && mAppliedFriends.getCarNum().length() > 0) {
                carNumber = mAppliedFriends.getCarNum();
            } else {
                carNumber = mStrNoValue;
            }

        } else {
            if (mSearchStrangers.getUserName() != null && mSearchStrangers.getUserName().length() > 0) {
                userName = mSearchStrangers.getUserName();
            } else {
                userName = mStrNoValue;
            }
            phoneNum = mSearchStrangers.getPhoneNum();
            mSex = sexItems[mSearchStrangers.getUserSex()];
            if (!TextUtils.isEmpty(mSearchStrangers.getBirthday()) && mSearchStrangers.getBirthday().compareTo("1900-01-01") > 0) {
                mBirthday = mSearchStrangers.getBirthday();
            } else {
                mBirthday = mStrNoValue;
            }
            city = (mSearchStrangers.getCity() == null ? "" : mSearchStrangers.getCity()).toString().trim();
            town = (mSearchStrangers.getTown() == null ? "" : mSearchStrangers.getTown()).toString().trim();
            prov = (mSearchStrangers.getProv() == null ? "" : mSearchStrangers.getProv()).toString().trim();
            if (mSearchStrangers.getCarType1() != null && mSearchStrangers.getCarType1().length() > 0) {
                mBrand = mSearchStrangers.getCarType1();
            } else {
                mBrand = mStrNoValue;
            }
            if (mSearchStrangers.getCarType2() != null && mSearchStrangers.getCarType2().length() > 0) {
                mType = mSearchStrangers.getCarType2();
            } else {
                mType = mStrNoValue;
            }
            if (mSearchStrangers.getCarNum() != null && mSearchStrangers.getCarNum().length() > 0) {
                carNumber = mSearchStrangers.getCarNum();
            } else {
                carNumber = mStrNoValue;
            }
        }
        setLocation(city, town, prov);
        setContentView(R.layout.stranger_msg);
        initControl();
        setLocalMsg();
    }

    private void setLocation(String city, String town, String prov) {

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
    }

    private void setLocalMsg() {
        tvBirthday.setText(mBirthday);
        tvProvince.setText(mMyLocation);
        tvSex.setText(mSex);
        tvCarBand.setText(mBrand);
        tvCarType.setText(mType);
        tvCarNumber.setText(carNumber);
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
        String phone = "";
        tvName.setText(userName);
        if (isFriend) {
            btnSure.setText("已经是好友");
            btnSure.setEnabled(false);
            phone = phoneNum;
            String carNum = null;
            try {
                DBManagerFriendsList db = new DBManagerFriendsList(mContext, DBTableName.getTableName(mContext, DBHelperFriendsList.NAME));
                carNum = db.getCarNum(phoneNum);
                db.closeDB();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(carNum)){
                tvCarNumber.setText(carNum);
            }
        } else {
            btnSure.setText("申请加为好友");
            String str = phoneNum.substring(0, 3);
            String str1 = phoneNum.substring(8, phoneNum.length());
            phone = str + "*****" + str1;
            if (userName.equals(phoneNum)){
                tvName.setText(phone);
            }
        }

//		tvCity.setText(mStrNoValue);
//		tvProvince.setText(mStrNoValue);
        tvPhone.setText(phone);
        Bitmap bitmap = GlobalImg.getImage(ShowStrangerMsgActivity.this, phoneNum);
        if (bitmap == null) {
            String name = "1";
            if (!userName.equals(mStrNoValue)) {
                name = userName;
            }
            LetterTileDrawable drawable = new LetterTileDrawable(getResources());
            drawable.setContactDetails(name, name);
            Bitmap bmp = FriendFaceUtill.drawableToBitmap(drawable);
            mImageShow.setImageBitmap(bmp);
        } else {
            mImageShow.setImageBitmap(GlobalImg.getImage(ShowStrangerMsgActivity.this, phoneNum));
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
            if (GlobalImg.getImage(ShowStrangerMsgActivity.this, param.getPhoneNum()) == null) {
                Log.e("UserFace", "UserFace is null");
                bmp = BitmapFactory.decodeResource(ShowStrangerMsgActivity.this.getResources(), R.drawable.default_useravatar);
            } else {
                bmp = GlobalImg.getImage(ShowStrangerMsgActivity.this, param.getPhoneNum());
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
                        ToastR.setToast(ShowStrangerMsgActivity.this, "信息输入不能为空");
                    }else {
                        ToastR.setToast(ShowStrangerMsgActivity.this, "备注输入过长（最大只能设置16个字符）");
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
        MyService.start(ShowStrangerMsgActivity.this, ProtoMessage.Cmd.cmdApplyFriend.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(ApplyFriendProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(ShowStrangerMsgActivity.this, filter, getBroadcastManager());

        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                ToastR.setToast(ShowStrangerMsgActivity.this, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    successBack(phoneNum);
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    public void successBack(String phoneNum) {
        ToastR.setToast(ShowStrangerMsgActivity.this, "请求成功，等待对方回应");
        searchSuccess(phoneNum);
    }

    public void fail(int i) {
        new ResponseErrorProcesser(ShowStrangerMsgActivity.this, i);
    }

    /**
     * 添加成功关闭该Activity
     */
    protected void searchSuccess(String phoneNum) {
//        if (requstCode == DistanceSearchReturnActivity.REQUST) {
//            Intent intent = new Intent();
//            intent.putExtra("number", phoneNum);
//            setResult(SearchReturnActivity.REQUST, intent);
//        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}