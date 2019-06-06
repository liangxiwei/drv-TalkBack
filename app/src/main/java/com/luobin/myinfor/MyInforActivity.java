package com.luobin.myinfor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.FileUtils;
import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.ImageTool;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.example.jrd48.chat.permission.PermissionUtil;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.notify.NotifyManager;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.ReceiverProcesser;
import com.example.jrd48.service.protocol.root.SetUserInfoProcesser;
import com.example.jrd48.service.protocol.root.UserInfoProcesser;
import com.luobin.dvr.DvrService;
import com.luobin.dvr.R;
import com.luobin.model.ViewAllCarParam;
import com.luobin.search.friends.city.picverview.CharacterPickerWindow;
import com.luobin.tool.MyCarBrands;
import com.luobin.ui.DvrMainActivity;
import com.luobin.utils.ButtonUtils;
import com.luobin.utils.MyInforTool;
import com.luobin.utils.VideoRoadUtils;
import com.luobin.utils.ZXingUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import me.lake.librestreaming.client.RESClient;

/**
 * Created by jrd48 on 2016/12/6.
 */

public class MyInforActivity extends BaseActivity implements PermissionUtil.PermissionCallBack, View.OnLongClickListener, View.OnClickListener {
    //调用相机
    public static final int TAKE_PHOTO = 4;
    //调用图库
    private static final int PICTURE = 3;
    private static final int RESULT_MSG = 2;
    protected PermissionUtil mPermissionUtil;
    int MY_PERMISSIONS_REQUEST_CAMERA = 10011;
    int MY_PERMISSIONS_REQUEST_WRITE = 10033;
    public static final String NO_SET = "不限";
    public static final String NOT_SET = "未设置";
    public static final String OTHER = "其他";
    public static final String SET_CITY = "set_city";
    private final String BIRTHDAY = "birthday";
    private final String LOCATION = "location";
    private final String CAR_NUMBER = "carnumber";
    private final String CAR_BRAND = "carbrand";
    private final String CAR_TYPE = "cartype";
    private Uri imageUri;
    //弹窗
    private ProgressDialog m_pDialog;
    private String[] sexItems = new String[]{"未设置", "男", "女", "未设置"};
    private String[] sexShow = new String[]{"男", "女", "未设置"};
    private String[] headItems = new String[]{"拍一张设置为头像", "选一张设置为头像"};
    private int sexdata = 0;
    private int sexdatashow = 0;
    private ImageView ivBack;
    private ImageView headImage;
    private TextView sex;
    private TextView mTVMyLocation;
    private TextView mTVMyBirthday;
    private TextView mTVMyMachineHost;
    private TextView mTVMyPhoneNumber;

    private TextView myCarId;
    private TextView myPlateNumber;
    private TextView myCarType;
    private TextView myCarBrand;
    private Button ok;
    private Button codeImage;
    private PopupWindow popupWindow;
    private Button logout;
    private LinearLayout buttonLayout;
    private MyInforTool myInforTool;

    private Handler mHandler = new Handler();
    private Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            m_pDialog.show();
        }
    };
    private Context mContext;
    private boolean isLogin;
    private LinearLayout headLayout;
    private RelativeLayout nameLayout;
    private LinearLayout sexLayout;
    private LinearLayout myLocation;
    private LinearLayout myBirthday;
    private LinearLayout myPlateNumberLayout;
    private LinearLayout myCarTypeLayout;
    private LinearLayout myCarBrandLayout;
    private Date today = null;
    private String datePickMsg = null;
    private String mBirthday = null;
    private String mCarID;
    private boolean isSetCarBrand;
    CharacterPickerWindow window;
    String mCurrentPhotoPath;
    private final int selectBands = 1, selectTypes = 2;
    int sexdefualt;
    private ViewAllCarParam param;
    private ProgressDialog myDialog;
    AlertDialog simplelistdialog;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_infor);
        mContext = this;
        waitDialog();

        isLogin = getIntent().getBooleanExtra("login", false);
        mPermissionUtil = PermissionUtil.getInstance();
        myInforTool = new MyInforTool(mContext, true);
        init();
        initBroadcast();
        showCheckAndUpdateCarTypePrompt();
//        initCarBrandsData();
    }

    private void initBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("ACTION.changeImage");
        registerReceiver(changgeImaReceiver, filter);
        registerReceiver(CheckAndUpdateCarTypeCompleteReceiver, new IntentFilter("CheckAndUpdateCarTypeComplete"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (changgeImaReceiver != null) {
                unregisterReceiver(changgeImaReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        unregisterReceiver(CheckAndUpdateCarTypeCompleteReceiver);
    }

    //初始化
    private void init() {
        headLayout = (LinearLayout) findViewById(R.id.head_layout);
        nameLayout = (RelativeLayout) findViewById(R.id.machine_host_layout);
        sexLayout = (LinearLayout) findViewById(R.id.sex_layout);
        myLocation = (LinearLayout) findViewById(R.id.my_location_layout);
        myBirthday = (LinearLayout) findViewById(R.id.my_birthday_layout);
        myPlateNumberLayout = (LinearLayout) findViewById(R.id.my_plate_number_layout);
        myCarTypeLayout = (LinearLayout) findViewById(R.id.my_car_type_layout);
        myCarBrandLayout = (LinearLayout) findViewById(R.id.my_car_brand_layout);
        headLayout.setOnClickListener(this);
        nameLayout.setOnClickListener(this);
        sexLayout.setOnClickListener(this);
        myLocation.setOnClickListener(this);
        myLocation.setOnLongClickListener(this);
        myBirthday.setOnClickListener(this);
        myBirthday.setOnLongClickListener(this);
        myPlateNumberLayout.setOnClickListener(this);
        myPlateNumberLayout.setOnLongClickListener(this);
        myCarTypeLayout.setOnClickListener(this);
        myCarTypeLayout.setOnLongClickListener(this);
        myCarBrandLayout.setOnClickListener(this);
        myCarBrandLayout.setOnLongClickListener(this);

        ivBack = (ImageView) findViewById(R.id.iv_back);
        headImage = (ImageView) findViewById(R.id.head_image);
        ivBack.setOnClickListener(this);
        sex = (TextView) findViewById(R.id.sex);
        mTVMyLocation = (TextView) findViewById(R.id.my_location);
        mTVMyBirthday = (TextView) findViewById(R.id.my_birthday);
        mTVMyMachineHost = (TextView) findViewById(R.id.my_machine_host);
        mTVMyPhoneNumber = (TextView) findViewById(R.id.my_phone_number);

        //车机部分内容
//        myCarId = (TextView) findViewById(R.id.my_car_id);
        myPlateNumber = (TextView) findViewById(R.id.my_plate_number);
        myCarType = (TextView) findViewById(R.id.my_car_type);
        myCarBrand = (TextView) findViewById(R.id.my_car_brand);
//        myCarId.setText(carId);
        ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivBack.performClick();
            }
        });
        buttonLayout = (LinearLayout) findViewById(R.id.logined_layout);
        codeImage = (Button) findViewById(R.id.code_image);
        codeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow();
            }
        });
        logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutDialog(mContext);
            }
        });
//        ButtonUtils.setViewScale(ok);
        if (isLogin) {
            ok.setVisibility(View.VISIBLE);
            buttonLayout.setVisibility(View.GONE);
        } else {
            ok.setVisibility(View.GONE);
            buttonLayout.setVisibility(View.VISIBLE);
        }

        setInfor();
        getInfor();//获取个人信息
    }
    private void logoutDialog(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.toast_tip));
        builder.setMessage(context.getResources().getString(R.string.toast_logout));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent("com.example.jrd48.chat.FORCE_OFFLINE");//强制下线功能
                intent.putExtra("toast", false);
                sendBroadcast(intent);
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        if (simplelistdialog != null && simplelistdialog.isShowing()) {
            simplelistdialog.dismiss();
        }
        simplelistdialog = builder.create();
        simplelistdialog.show();
    }
    private void setInfor() {
//        setMyLocalMsg(myInforTool.getProv(), myInforTool.getCity(), myInforTool.getTown());
        String birthDay = myInforTool.getBirthday();
        if (TextUtils.isEmpty(birthDay) || birthDay.compareTo("1900-01-01") <= 0) {
            mTVMyBirthday.setText("未设置");
        } else {
            mTVMyBirthday.setText(birthDay);
        }
        Bitmap bmp = GlobalImg.getImage(MyInforActivity.this, myInforTool.getPhone());
        headImage.setImageBitmap(getUserFace(bmp));
        mTVMyMachineHost.setText(myInforTool.getUserName() + "");
        sexdata = myInforTool.getUserSex();
        if (sexdata == 0) {
            sexdatashow = 2;
        } else {
            sexdatashow = sexdata - 1;
        }
        sexdefualt = sexdatashow;
        mTVMyPhoneNumber.setText(myInforTool.getPhone());
        sex.setText(sexItems[sexdata] + "");
        refreshCarData();
    }

    private void refreshCarData() {
        myInforTool = new MyInforTool(mContext, true);

        if (myInforTool.getCarID() != null && myInforTool.getCarID().trim().length() > 0) {
            //  myCarId.setText(myInforTool.getCarID());
        } else {
            //   myCarId.setText(NOT_SET);
        }
        if (myInforTool.getCarNum() != null && myInforTool.getCarNum().trim().length() > 0) {
            myPlateNumber.setText(myInforTool.getCarNum());
        } else {
            myPlateNumber.setText(NOT_SET);
        }
        if (myInforTool.getCarBand() != null && myInforTool.getCarBand().trim().length() > 0) {
            myCarBrand.setText(myInforTool.getCarBand());
        } else {
            myCarBrand.setText(NOT_SET);
        }
        if (myInforTool.getCarType2() != null && myInforTool.getCarType2().trim().length() > 0) {
            myCarType.setText(myInforTool.getCarType2());
        } else {
            myCarType.setText(NOT_SET);
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
            str = NOT_SET;
        } else if (city.length() <= 0 && town.length() > 0) {
            str1 = OTHER;
            str2 = OTHER;
            str3 = town;
            str = town;
        } else if (province.length() > 0 && city.length() <= 0 && town.length() <= 0) {
            str1 = OTHER;
            str2 = OTHER;
            str3 = province;
            str = province;
        } else if (province.length() > 0 && city.length() > 0 && province.equals(city)) {
            str = city + "-" + town;
            str1 = province;
            str2 = city;
            str3 = town;
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

    /*    window = OptionsWindowHelper.builder(str1, str2, str3, SET_CITY, MyInforActivity.this, new OptionsWindowHelper.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(String province, String city, String area) {
//                Toast.makeText(MyInforActivity.this, province + "," + city + "," + area, Toast.LENGTH_SHORT).show();
                setMyLocationText(province, city, area);
            }
        });*/

    }

    private void setMyLocationText(String province, String city, String town) {
        String st = " ";
        String mProvince = st;
        String mCity = st;
        String mTown = st;
        String str = "";
        if (province.equals(NO_SET) || city.equals(NO_SET)) {
            mProvince = province;
            str = province;
        } else if (town.equals(NO_SET)) {
            mProvince = province;
            mCity = city;
            str = province + "-" + city;
        } else if (province.equals(OTHER)) {
            mProvince = town;
            str = town;
        } else {
            if (town == null || town.length() <= 0) {
                mProvince = province;
                mCity = city;
                str = province + "-" + city;
            } else {
                mProvince = province;
                mCity = city;
                mTown = town;
                if (province.equals(city)) {
                    str = city + "-" + town;
                } else {
                    str = province + "-" + city + "-" + town;
                }
            }
        }
        toSetLocation(mProvince, mCity, mTown, str);

    }

    private void toSetLocation(final String province, final String city, final String town, final String str) {
        try {
            ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
            builder.setProv(province);
            builder.setCity(city);
            builder.setTown(town);
            MyService.start(MyInforActivity.this, ProtoMessage.Cmd.cmdSetMyInfo.getNumber(), builder.build());
            IntentFilter filter = new IntentFilter();
            filter.addAction(SetUserInfoProcesser.ACTION);
            final TimeoutBroadcast b = new TimeoutBroadcast(MyInforActivity.this, filter, getBroadcastManager());

            b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
                @Override
                public void onTimeout() {
                    ToastR.setToast(MyInforActivity.this, "设置失败");
                }

                @Override
                public void onGot(Intent i) {
//                mHandler.removeCallbacks(mShowProgress);
                    m_pDialog.dismiss();
                    int code = i.getIntExtra("error_code", -1);
                    if (code == ProtoMessage.ErrorCode.OK.getNumber()) {
                        ToastR.setToast(MyInforActivity.this, "设置成功");
                        mTVMyLocation.setText(str);
                        SharedPreferences preference = mContext.getSharedPreferences("token", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preference.edit();
                        if (city.equals("-")) {
                            editor.putString("city", "");//String
                            editor.putString("prov", "");//String
                            editor.putString("town", "");//String
                        } else {
                            editor.putString("city", city);//String
                            editor.putString("prov", province);//String
                            editor.putString("town", town);//String
                        }
                        editor.commit();
//                    mHandler.postDelayed(mShowProgress, 500);
                    } else {
//                    ToastR.setToast(MyInforActivity.this, "设置失败");
                        new ResponseErrorProcesser(MyInforActivity.this, code);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    // ********************************************等待弹窗****************************************************
    private void waitDialog() {
        //创建ProgressDialog对象，自定义style
        m_pDialog = new ProgressDialog(MyInforActivity.this, R.style.CustomDialog);
        // 设置进度条风格，风格为圆形，旋转的
        m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // 设置ProgressDialog 提示信息
        m_pDialog.setMessage("请稍等...正在刷新数据");
        // 设置ProgressDialog 的进度条是否不明确
        m_pDialog.setIndeterminate(false);
        // 设置ProgressDialog 是否可以按退回按键取消
        m_pDialog.setCancelable(true);
    }

    //****************************调用系统相机******************************
    //调用相机

    public void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, TAKE_PHOTO);
        } catch (Exception e) {
            e.printStackTrace();
            ToastR.setToast(this, "请打开摄像头的使用权限！");

            if (mPermissionUtil.isOverMarshmallow()) {
                mPermissionUtil.requestPermission1(this, MY_PERMISSIONS_REQUEST_CAMERA, this, PermissionUtil.PERMISSIONS_CAMERA, PermissionUtil.PERMISSIONS_CAMERA);
            }
        }
    }
    //****************************调用系统相机******************************


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE) {
            mPermissionUtil.requestResult(MyInforActivity.this, permissions, grantResults,
                    MyInforActivity.this, PermissionUtil.PERMISSIONS_STORAGE);
        } else if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            mPermissionUtil.requestResult(MyInforActivity.this, permissions, grantResults,
                    MyInforActivity.this, PermissionUtil.PERMISSIONS_CAMERA);
        }
    }

    @Override
    public void onPermissionSuccess(String type) {
        if (type.equals(PermissionUtil.PERMISSIONS_STORAGE)) {
            headDialog();
        } else if (type.equals(PermissionUtil.PERMISSIONS_CAMERA)) {
            openCamera();
        }
    }

    @Override
    public void onPermissionReject(String strMessage) {
        if (strMessage.equals(PermissionUtil.PERMISSIONS_STORAGE)) {
            ToastR.setToastLong(MyInforActivity.this, "应用存储权限已经拒绝，请到手机管家或者系统设置里授权");
        } else if (strMessage.equals(PermissionUtil.PERMISSIONS_CAMERA)) {
            ToastR.setToastLong(MyInforActivity.this, "应用拍照权限已经拒绝，请到手机管家或者系统设置里授权");
        }
    }

    @Override
    public void onPermissionFail(String failType) {
        if (failType.equals(PermissionUtil.PERMISSIONS_STORAGE)) {
            ToastR.setToastLong(MyInforActivity.this, "应用存储权限授权失败");
        } else if (failType.equals(PermissionUtil.PERMISSIONS_CAMERA)) {
            ToastR.setToastLong(MyInforActivity.this, "应用拍照权限授权失败");
        }
    }

    //*********************************显示个人信息*********************************
    private void ShowMyinfo() {
        mHandler.removeCallbacks(mShowProgress);
        m_pDialog.dismiss();
        myInforTool = new MyInforTool(mContext, true);
        setInfor();
//        headLayout.setVisibility(View.VISIBLE);
//        nameLayout.setVisibility(View.VISIBLE);
//        sexLayout.setVisibility(View.VISIBLE);
    }
    //*********************************显示个人信息*********************************

    //***********************************获取个人信息*************************************
    private void getInfor() {
        ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
        MyService.start(MyInforActivity.this, ProtoMessage.Cmd.cmdGetMyInfo.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(UserInfoProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(MyInforActivity.this, filter, getBroadcastManager());
        b.startReceiver(15, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(MyInforActivity.this, "连接超时");
                ShowMyinfo();
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK_VALUE) {
                    String address = ReceiverProcesser.getMyDataRoot(MyInforActivity.this) + "/" + myInforTool.getPhone();
                    String img_path = address + "/head2.jpg";
                    mCarID = i.getStringExtra("car_id");
                    SharedPreferencesUtils.put(MyInforActivity.this, "car_id", mCarID);
                    Log.i("chatjrd", "读取头像地址:" + img_path);
                    try {
                        Log.i("chatjrd", "读取头像成功：" + img_path);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.w("pocdemo", "read img fail: " + img_path + " ---- error: " + e.getMessage());
                    }
                    ShowMyinfo();
                    if (myInforTool == null) {
                        myInforTool = new MyInforTool(mContext, true);
                    } else {
                        myInforTool.load();
                    }
                    setInfor();

                } else {
                    new ResponseErrorProcesser(MyInforActivity.this, i.getIntExtra("error_code", -1));
                }

            }
        });
    }

    //***********************************获取个人信息*************************************
    //圆形图片
    private Bitmap getUserFace(Bitmap bmp) {
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

    @Override
    protected void onPause() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
        if (simplelistdialog != null && simplelistdialog.isShowing()) {
            simplelistdialog.dismiss();
        }
        super.onPause();
    }

    @Override
    public boolean onLongClick(View view) {
       /* switch (view.getId()) {
            case R.id.my_birthday_layout:
                if (!mTVMyBirthday.getText().equals(NOT_SET)) {
                    ClearData(BIRTHDAY, "出生日期");
                }
                break;
            case R.id.my_location_layout:
                if (!mTVMyLocation.getText().equals(NOT_SET)) {
                    ClearData(LOCATION, "所在地");
                }
                break;
            case R.id.my_plate_number_layout:
                if (!myPlateNumber.getText().equals(NOT_SET)) {
                    ClearData(CAR_NUMBER, "车牌号");
                }
                break;
            case R.id.my_car_brand_layout:
                if (!myCarBrand.getText().equals(NOT_SET)) {
                    ClearData(CAR_BRAND, "车品牌");
                }
                break;
            case R.id.my_car_type_layout:
                if (!myCarType.getText().equals(NOT_SET)) {
                    ClearData(CAR_TYPE, "车型");
                }
                break;
            default:
                break;
        }*/
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                Intent intent = new Intent(mContext, DvrMainActivity.class);
                startActivity(intent);
                finish();
                break;

         /*   case R.id.my_birthday_layout:
                showBirthdayDialog();
                break;
            case R.id.my_location_layout:
                window.showAtLocation(view, Gravity.BOTTOM, 0, 0);
                break;
            case R.id.sex_layout:
                sexDialog();
                break;
            case R.id.machine_host_layout:
                nameDialog(false);
                break;
            case R.id.head_layout:
//                toSetHead();
                break;
            case R.id.my_plate_number_layout:
                plateNumberDialog();
                break;
            case R.id.my_car_brand_layout:
                if (isSetCarBrand){
                    Intent bandIntent = new Intent(MyInforActivity.this, SelectTypeActivity.class);
                    bandIntent.putExtra("requestCode", selectBands);
                    bandIntent.putExtra("requestBand", selectBands);
                    startActivityForResult(bandIntent, selectBands);
                }else {
                    showTaost();
                }
                break;
            case R.id.my_car_type_layout:
                if (isSetCarBrand){
                    Intent nextIntent = new Intent(MyInforActivity.this, SelectTypeActivity.class);
                    if (myInforTool.getCarBand() == null || TextUtils.isEmpty(myInforTool.getCarBand())) {
                        nextIntent.putExtra("requestCode", selectBands);
                        startActivityForResult(nextIntent, selectBands);
                    } else {
                        int fatherCode = new DBManagerCarList(mContext)
                                .getCarBandId(myInforTool.getCarBand());
                        if (fatherCode == -1) {
                            nextIntent.putExtra("requestCode", selectBands);
                            startActivityForResult(nextIntent, selectBands);
                        } else {
                            nextIntent.putExtra("requestCode", selectTypes);
                            nextIntent.putExtra("fatherCode", fatherCode);
                            startActivityForResult(nextIntent, selectTypes);
                        }
                    }
                }else {
                    showTaost();
                }
                break;*/
//            case R.id.tv_car_msg:
//                Intent intent = new Intent(MyInforActivity.this, EditVehicleActivity.class);
//                intent.putExtra("carid", mCarID);
//                startActivityForResult(intent, RESULT_MSG);
//                break;
            default:
                break;
        }
    }

    public void sexDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置性别");
        //设置单选列表项，默认选中第二项
        builder.setSingleChoiceItems(sexShow, sexdatashow, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                sexdefualt = which;
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                ToastR.setToast(MyInforActivity.this, "你选择了：" + sexItems[sexdefualt + 1]);
                sexdata = sexdefualt + 1;
                setSex(sexdata, sexdefualt);
            }
        });

        AlertDialog simplechoicedialog = builder.create();
        simplechoicedialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    ButtonUtils.changeLeftOrRight(true);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    ButtonUtils.changeLeftOrRight(false);
                    return true;
                }
                return false;
            }
        });
        simplechoicedialog.show();
    }


    //*********************************设置性别************************************
    private void setSex(final int sexdata, final int sexdefual) {
        ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
        builder.setUserSex(sexdata);
        MyService.start(MyInforActivity.this, ProtoMessage.Cmd.cmdSetMyInfo.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(SetUserInfoProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(MyInforActivity.this, filter, getBroadcastManager());

        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(MyInforActivity.this, "设置失败");
            }

            @Override
            public void onGot(Intent i) {
                mHandler.removeCallbacks(mShowProgress);
                m_pDialog.dismiss();
                int code = i.getIntExtra("error_code", -1);
                if (code == ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(MyInforActivity.this, "设置成功");
                    sexdatashow = sexdefual;
                    sex.setText(sexItems[sexdata] + "");
                    SharedPreferences preference = mContext.getSharedPreferences("token", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preference.edit();
                    editor.putInt("user_sex", sexdata);//int
                    editor.commit();

                } else {
//                    ToastR.setToast(MyInforActivity.this, "设置失败");
                    new ResponseErrorProcesser(MyInforActivity.this, code);
                }
            }
        });
    }

    //******************************设置昵称弹窗******************************
    public void nameDialog(final boolean isEditNick) {
        //获取xml布局文件对象
        LayoutInflater factory = LayoutInflater.from(MyInforActivity.this);// 提示框
        final View view = factory.inflate(R.layout.setname_layout, null);// 这里必须是final的
        final EditText name = (EditText) view.findViewById(R.id.set_name);// 获得输入框对象

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > GlobalStatus.MAX_TEXT_COUNT) {
                    name.setText(s.subSequence(0, 16));
                    name.setSelection(name.length());
                    ToastR.setToast(mContext, "最大只能设置16个字符");
                }
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (isEditNick) {
            builder.setTitle("设置昵称");
        } else {
            name.setHint("请输入要设置的机主名称");
            builder.setTitle("设置机主名称");
        }

        if (this.mTVMyMachineHost.getText().toString().length() > 0 && !isEditNick) {
            name.setText(this.mTVMyMachineHost.getText().toString());
            name.setSelection(name.length());
        }

        name.setSelection(name.length());
        //设置对话框显示的View组件
        builder.setView(view);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

                if (name.getText().toString().length() < 2) {
                    // 条件不成立不能关闭 AlertDialog 窗口
                    ToastR.setToastLong(MyInforActivity.this, "名称输入太短（汉字至少2位或者数字、字母至少4位）");
                } else {
                    setName(name.getText().toString(), isEditNick);
//                    ToastR.setToast(MyInforActivity.this, "设置昵称为：" + name.getText().toString());
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
//                ToastR.setToast(MyInforActivity.this, "你取消了昵称");
            }
        });
        //builder创建对话框对象AlertDialog
        AlertDialog viewdialog = builder.create();
        viewdialog.show();
    }

    private void setName(final String name, final boolean isNickName) {
        ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
        if (isNickName) {
            builder.setNickName(name);
        } else {
            builder.setUserName(name);
        }
        MyService.start(MyInforActivity.this, ProtoMessage.Cmd.cmdSetMyInfo.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(SetUserInfoProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(MyInforActivity.this, filter, getBroadcastManager());

        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(MyInforActivity.this, "设置失败");
            }

            @Override
            public void onGot(Intent i) {
//                mHandler.postDelayed(mShowProgress, 500);
//                mHandler.removeCallbacks(mShowProgress);
//                m_pDialog.dismiss();
                int code = i.getIntExtra("error_code", -1);
                if (code == ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(MyInforActivity.this, "设置成功");

                    SharedPreferences preference = mContext.getSharedPreferences("token", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preference.edit();
                    if (isNickName) {
                        // nickName.setText(name);
                        editor.putString("nick_name", name);//int
                    } else {
                        mTVMyMachineHost.setText(name);
                        editor.putString("name", name);//int
                    }
                    editor.commit();
//                    if (mTVMyMachineHost.getText().equals(myPhone)) {
//                        mTextViewShow.setVisibility(View.VISIBLE);
//                    } else {
//                        mTextViewShow.setVisibility(View.GONE);
//                    }

                } else if (code == ProtoMessage.ErrorCode.NAME_TOO_SHORT_VALUE) {
                    ToastR.setToastLong(MyInforActivity.this, "名称输入太短（汉字至少2位或者数字、字母至少4位）");
                } else {
//                    ToastR.setToast(MyInforActivity.this, "设置失败");
                    new ResponseErrorProcesser(MyInforActivity.this, code);
                }
            }
        });
    }

    /**
     * 设置生日
     */
    private void showBirthdayDialog() {
        final AlertDialog dlg = new AlertDialog.Builder(this).create();
        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    ButtonUtils.changeLeftOrRight(true);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    ButtonUtils.changeLeftOrRight(false);
                    return true;
                }
                return false;
            }
        });
        dlg.show();
        Window window = dlg.getWindow();
        window.setContentView(R.layout.set_birthday);
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.j_timepopwindow_anim_style);
        final DatePicker.OnDateChangedListener OnDatePickChanged = new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String moth = String.valueOf(monthOfYear + 1);
                String day = String.valueOf(dayOfMonth);
                String mstr;
                mstr = setBrithday(year, dayOfMonth, moth, day);
                datePickMsg = mstr;
//                mTVMyBirthday.setText(mstr);
            }
        };

        final DatePicker datePicker = (DatePicker) window.findViewById(R.id.birthday_Picker);
//        RobotUsers info = GlobalData.getInstance().getUserInfo();
//
//        datePicker.setCalendarViewShown(false);
        Date birthday = null;
//        if (birthday.toString().equals("1900-01-01")) {
//            birthday = null;
//        }
        String text = mTVMyBirthday.getText().toString();
        Calendar calendar = Calendar.getInstance();
        if (today == null) {
            today = new Date();
        }
        calendar.setTime(today);
        final int todayYear = calendar.get(Calendar.YEAR);
        final int todayMonth = calendar.get(Calendar.MONTH);
        final int todayDay = calendar.get(Calendar.DAY_OF_MONTH);
        if (text == null || text.length() <= 0 || text.equals("未设置")) {

            datePicker.init(todayYear, todayMonth, todayDay, OnDatePickChanged);
        } else {

//            String str = toStringFmt(birthday, "yyyy-MM-dd");
            String szYear = text.substring(0, text.indexOf("-"));
            String szMonth = text.substring(5, 7);
            String szDate = text.substring(8, text.length());
            int YEAR = Integer.parseInt(szYear);
            int MONTH = Integer.parseInt(szMonth);
            int DATE = Integer.parseInt(szDate);
            datePicker.init(YEAR, MONTH - 1, DATE, OnDatePickChanged);
        }

        Button sure = (Button) window.findViewById(R.id.birthday_sure_btn);
        sure.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SdCardPath")
            public void onClick(View v) {
                String mbrithday = null;
                if (datePickMsg == null) {
                    if (mBirthday == null || mBirthday.equals("1900-01-01")) {
                        mbrithday = setBrithday(todayYear, todayDay, String.valueOf(todayMonth + 1), String.valueOf(todayDay));
                    } else {
                        mbrithday = mBirthday;
                    }
                } else {
                    mbrithday = datePickMsg;
                }
                toSetBirthdayMsg(mbrithday);
                dlg.cancel();
            }
        });
        Button cancel = (Button) window.findViewById(R.id.birthday_cancel_btn);
        cancel.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SdCardPath")
            public void onClick(View v) {
                dlg.cancel();
                return;
            }
        });

    }

    public String setBrithday(int year, int dayOfMonth, String moth, String day) {
        String mstr;
        if (moth.length() == 1 && day.length() == 1) {
            mstr = year + "-" + "0" + moth + "-" + "0" + dayOfMonth;
        } else if (moth.length() == 1) {
            mstr = year + "-" + "0" + moth + "-" + dayOfMonth;
        } else if (day.length() == 1) {
            mstr = year + "-" + moth + "-" + "0" + dayOfMonth;
        } else {
            mstr = year + "-" + moth + "-" + dayOfMonth;
        }
        return mstr;
    }

    private void toSetBirthdayMsg(final String mybrithday) {

        try {
            ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
//            long time = DateFormatUtil.stringToLong(mybrithday, DateFormatUtil.formatType1);
            builder.setBirthday(mybrithday);
            MyService.start(MyInforActivity.this, ProtoMessage.Cmd.cmdSetMyInfo.getNumber(), builder.build());
            IntentFilter filter = new IntentFilter();
            filter.addAction(SetUserInfoProcesser.ACTION);
            final TimeoutBroadcast b = new TimeoutBroadcast(MyInforActivity.this, filter, getBroadcastManager());

            b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
                @Override
                public void onTimeout() {
                    ToastR.setToast(MyInforActivity.this, "设置失败");
                }

                @Override
                public void onGot(Intent i) {
//                mHandler.removeCallbacks(mShowProgress);
                    m_pDialog.dismiss();
                    int code = i.getIntExtra("error_code", -1);
                    if (code == ProtoMessage.ErrorCode.OK.getNumber()) {
                        ToastR.setToast(MyInforActivity.this, "设置成功");
                        SharedPreferences preference = mContext.getSharedPreferences("token", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preference.edit();
                        if (mybrithday.equals("-")) {
                            editor.putString("birthday", "");
                            mTVMyBirthday.setText(NOT_SET);
                        } else {
                            mTVMyBirthday.setText(mybrithday);
                            editor.putString("birthday", mybrithday);
                        }
                        editor.commit();

//                    mHandler.postDelayed(mShowProgress, 500);
                    } else {
//                    ToastR.setToast(MyInforActivity.this, "设置失败");
                        new ResponseErrorProcesser(MyInforActivity.this, code);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void plateNumberDialog() {
        //获取xml布局文件对象
        LayoutInflater factory = LayoutInflater.from(mContext);// 提示框
        final View view = factory.inflate(R.layout.setname_layout, null);// 这里必须是final的
        final EditText name = (EditText) view.findViewById(R.id.set_name);// 获得输入框对象

        boolean isSetText = true;
        if (myPlateNumber.getText().toString().equals("未设置")) {
            isSetText = false;
        }
        name.setHint("请输入要设置的车牌号");
        if (myPlateNumber.getText().toString().length() > 0 && isSetText) {
            name.setText(myPlateNumber.getText().toString());
            name.setSelection(name.length());
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("设置车牌号");
        //设置对话框显示的View组件
        builder.setView(view);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                String nameText = name.getText().toString();
                if (nameText.length() <= GlobalStatus.MIN_NUMBER_NAME_COUNT) {
                    ToastR.setToastLong(mContext, "车牌号输入太短（最小4个字符）");
                } else if (nameText.length() > GlobalStatus.MAX_PLATE_NUMBER) {
                    ToastR.setToastLong(mContext, "车牌号输入过长（最大只能设置9个字符）");
                } else {
                    setplateNumber(nameText);
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        //builder创建对话框对象AlertDialog
        android.app.AlertDialog viewdialog = builder.create();
        viewdialog.show();
    }


    private void setplateNumber(final String carPlateNumber) {
        ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
        builder.setCarNum(carPlateNumber);
        MyService.start(mContext, ProtoMessage.Cmd.cmdSetMyInfo.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(SetUserInfoProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(mContext, filter, getBroadcastManager());

        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(mContext, "设置失败");
            }

            @Override
            public void onGot(Intent i) {
                int code = i.getIntExtra("error_code", -1);
                if (code == ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(mContext, "设置成功");
//                    getInfor();
                    SharedPreferences preference = mContext.getSharedPreferences("token", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preference.edit();
                    if (carPlateNumber.equals("-")) {
                        myPlateNumber.setText(NOT_SET);
                        editor.putString("car_num", "");//String
                    } else {
                        myPlateNumber.setText(carPlateNumber);
                        editor.putString("car_num", carPlateNumber);//String
                    }
                    editor.commit();

                } else {
//                    ToastR.setToast(mContext, "设置失败");
                    new ResponseErrorProcesser(mContext, code);
                }
            }
        });
    }

    private void toSetHead() {
        if (mPermissionUtil.isOverMarshmallow()) {
            mPermissionUtil.requestPermission1(MyInforActivity.this, MY_PERMISSIONS_REQUEST_WRITE, MyInforActivity.this, PermissionUtil.PERMISSIONS_STORAGE, PermissionUtil.PERMISSIONS_STORAGE);
        } else {
            headDialog();
        }
    }

    //处理返回信息
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case TAKE_PHOTO://调用剪切程序
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap mImageBitmap;
//                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                            mImageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
//                        } else {
                        Bundle extras = data.getExtras();
                        mImageBitmap = (Bitmap) extras.get("data");
//                        }
                        Bitmap bitmapCompress = comp(mImageBitmap);
                        if (bitmapCompress == null) {
                            deleteMyFile(mCurrentPhotoPath);
                            return;
                        }
                        Bitmap bitmapSquare = ImageCrop(bitmapCompress);
                        setHead(bitmapSquare);
                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    }
                }
                break;
            case PICTURE://打开图库图片后，发送
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    String image = FileUtils.getUriPath(this, uri); //（因为4.4以后图片Uri发生了变化）通过文件工具类 对uri进行解析得到图片路径
                    Bitmap bitmapCompress = comp(BitmapFactory.decodeFile(image));
                    if (bitmapCompress == null) {
                        ToastR.setToastLong(mContext, "图片选择失败,请检查图片是否正常！");
                        return;
                    }
                    Bitmap bitmapSquare = ImageCrop(bitmapCompress);
                    setHead(bitmapSquare);
                }

                break;

            case selectBands:
                if (param == null) {
                    param = new ViewAllCarParam();
                }
                String brand = data.getStringExtra("carBandText");
                param.setCarBrandText(brand);
                int requestBand = data.getIntExtra("requestBand", 0);
                if (requestBand == 0) {
                    param.setCarTypeText(data.getStringExtra("carTypeText"));
                    param.setCarTypeId(data.getIntExtra("carTypeId", 0));
                } else {
                    param.setCarTypeText("");
                    param.setCarTypeId(0);
                }
                toEditBrandAndCarType(param, true, requestBand);
                break;
            case selectTypes:
                if (param == null) {
                    param = new ViewAllCarParam();
                }
                param.setCarTypeText(data.getStringExtra("carTypeText"));
//                myCarType.setText(param.getCarTypeText());
                param.setCarTypeId(data.getIntExtra("carTypeId", 0));
                toEditBrandAndCarType(param, false, 0);
                break;

            default:
                break;

        }
    }

    private void setHead(final Bitmap image) {
        if (image == null) {
            ToastR.setToast(mContext, "未获取到图片");
            return;
        }
        if (m_pDialog != null) {
            m_pDialog.show();
        }
        ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bitmapByte = baos.toByteArray();
        Log.d("image", bitmapByte.length + "");
        builder.setUserPic(com.google.protobuf.ByteString.copyFrom(bitmapByte));
        MyService.start(MyInforActivity.this, ProtoMessage.Cmd.cmdSetMyInfo.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(SetUserInfoProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(MyInforActivity.this, filter, getBroadcastManager());

        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                if (m_pDialog != null) {
                    m_pDialog.dismiss();
                }
                ToastR.setToast(MyInforActivity.this, "连接超时，请检查网络是否连接(或者网络质量差)");
            }

            @Override
            public void onGot(Intent i) {
                if (m_pDialog != null) {
                    m_pDialog.dismiss();
                }
                int code = i.getIntExtra("error_code", -1);
                if (code == ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(MyInforActivity.this, "设置成功");
                    headImage.setImageBitmap(getUserFace(image));
//                    mHandler.postDelayed(mShowProgress, 500);
                    FriendFaceUtill.saveFriendBitmapFaceImg(myInforTool.getUserName(), myInforTool.getPhone(), image, mContext);
                    GlobalImg.reloadImg(mContext, myInforTool.getPhone());
                    saveMyBitmap(image);
                } else {
                    new ResponseErrorProcesser(MyInforActivity.this, code);
                }
            }
        });
    }

    public void saveMyBitmap(Bitmap mBitmap) {
        try {
            String address = ReceiverProcesser.getMyDataRoot(mContext) + "/" + myInforTool.getPhone() + "/";
            File outputDir = new File(address);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            //Log.i("chatjrd","保存头像地址:"+address);

            File outputImage = new File(address, "head2.jpg");
            Log.i("pocdemo", "save head img path: " + outputImage);
            if (!outputImage.exists()) {
                outputImage.createNewFile();
            }
            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(outputImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            try {
                fOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        deleteMyFile(mCurrentPhotoPath);
    }

    private void deleteMyFile(String mCurrentPhotoPath) {
        if (mCurrentPhotoPath == null) {
            return;
        }
        try {
            File file = new File(mCurrentPhotoPath);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("image", "delete file fail");
        }
    }

    public void headDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置头像");
        builder.setItems(headItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {
                    case 0:
                        if (mPermissionUtil.isOverMarshmallow()) {
                            mPermissionUtil.requestPermission1(MyInforActivity.this, MY_PERMISSIONS_REQUEST_CAMERA,
                                    MyInforActivity.this, PermissionUtil.PERMISSIONS_CAMERA, PermissionUtil.PERMISSIONS_CAMERA);
                        } else {
                            openCamera();
                        }
                        break;
                    case 1:
                        openPicture();
                        break;
                    default:
                        break;
                }
            }
        });
        AlertDialog simplelistdialog = builder.create();
        simplelistdialog.show();
    }

    public void openPicture() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        /*打开文件intent.setAction(Intent.ACTION_OPEN_DOCUMENT);*/
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICTURE);
    }

    //****************************压缩图片******************************
    private Bitmap comp(Bitmap image) {
        if (image == null) {
//            throw new RuntimeException("图片打开失败！");
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if (baos.toByteArray().length / 1024 > 1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = (newOpts.outWidth) / 200;//be=1表示不缩放
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return bitmap;//压缩好比例大小后再进行质量压缩
    }
    //****************************压缩图片******************************

    //*************************剪成正方形********************************
    private Bitmap ImageCrop(Bitmap bitmap) {
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

        int wh = w > h ? h : w;// 裁切后所取的正方形区域边长

        int retX = w > h ? (w - h) / 2 : 0;//基于原图，取正方形左上角x坐标
        int retY = w > h ? 0 : (h - w) / 2;

        //下面这句是关键
        try {
            return Bitmap.createBitmap(bitmap, retX, retY, wh, wh, null, false);
        } catch (Exception e) {
            return null;
        }
    }

    private void initCarBrandsData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MyCarBrands.getInstance(mContext);
            }
        }).start();
    }

    private BroadcastReceiver CheckAndUpdateCarTypeCompleteReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            isSetCarBrand = true;
        }

    };

    private BroadcastReceiver changgeImaReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String phone = intent.getStringExtra(ReceiverProcesser.PHONE_NUMBER);
            if (TextUtils.isEmpty(phone)) {
                return;
            }
            if (phone.equals(myInforTool.getPhone())) {
                Log.d("jim", "刷新个人信息");
                Bitmap bmp = GlobalImg.getImage(MyInforActivity.this, phone);
                headImage.setImageBitmap(getUserFace(bmp));
//                getInfor();
            }
        }

    };

    private void showCheckAndUpdateCarTypePrompt() {
        if ((int) SharedPreferencesUtils.get(mContext, "type_code", 1) == 1) {
            isSetCarBrand = false;
        } else {
            isSetCarBrand = true;
        }
    }

    private void showTaost() {
        ToastR.setToastLong(mContext, "正在加载数据，请稍候...");
    }

    private String carType1;
    private String carType2 = "";

    private void toEditBrandAndCarType(final ViewAllCarParam param, final boolean isEditCarBrand, final int requestBand) {
        setDialog("请稍候...");
        ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
        if (requestBand == 0) {
            carType1 = param.getCarTypeText();
//            int endIndex = carType.indexOf(" ");
//            carType1 = carType.substring(0, endIndex);
//            carType2 = carType.substring(endIndex, carType.length());
            if (isEditCarBrand) {
                builder.setCarType1(param.getCarBrandText());
            }
            builder.setCarType2(carType1);
            builder.setCarType3("-");
        } else {
            builder.setCarType1(param.getCarBrandText());
            builder.setCarType2("-");
            builder.setCarType3("-");
            carType1 = "";
            carType2 = "";
        }
        MyService.start(mContext, ProtoMessage.Cmd.cmdSetMyInfo.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(SetUserInfoProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(mContext, filter, getBroadcastManager());

        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(mContext, "设置失败");
                if (myDialog != null) {
                    myDialog.cancel();
                }
            }

            @Override
            public void onGot(Intent i) {
                int code = i.getIntExtra("error_code", -1);
                if (myDialog != null) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            myDialog.cancel();
                        }
                    }, 500);
                }
                if (code == ProtoMessage.ErrorCode.OK.getNumber()) {
                    SharedPreferences preference = mContext.getSharedPreferences("token", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preference.edit();
                    if (isEditCarBrand) {
                        editor.putString("car_band", param.getCarBrandText());//String
                        myCarBrand.setText(param.getCarBrandText());
                    }
                    editor.putString("car_type2", carType1);//String
                    editor.putString("car_type3", carType2);//String
                    editor.commit();
                    if (requestBand == 0) {
                        myCarType.setText(param.getCarTypeText());
                    } else {
                        myCarType.setText(NOT_SET);
                    }

                    if (myInforTool != null) {
                        myInforTool.load();
                    } else {
                        myInforTool = new MyInforTool(mContext, true);
                    }
                } else {
                    new ResponseErrorProcesser(mContext, code);
                }
            }
        });

    }

    private void setDialog(String msg) {
        myDialog = new ProgressDialog(this, R.style.CustomDialog);
        myDialog.setMessage(msg);
        myDialog.setCancelable(false);
        myDialog.setIndeterminate(false);
        myDialog.show();
    }

    public void ClearData(final String type, final String str) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext); // 先得到构造器
        builder.setMessage("是否要清除之前设置的" + str + "数据？").setTitle("提示：").setPositiveButton("确定", new android.app.AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (type.equals(BIRTHDAY)) {
                    toSetBirthdayMsg("-");
                } else if (type.equals(LOCATION)) {
                    toSetLocation("-", "-", "-", NOT_SET);
                } else if (type.equals(CAR_NUMBER)) {
                    setplateNumber("-");
                } else if (type.equals(CAR_BRAND)) {
                    clearBrandOrCarType("-", "-", "-", true);
                } else if (type.equals(CAR_TYPE)) {
                    clearBrandOrCarType("", "-", "-", false);
                }
//                toUnbind();
            }
        }).setNegativeButton("取消", new android.app.AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();

    }

    private void clearBrandOrCarType(final String type1, String type2, String type3, final boolean isEditCarBrand) {
        ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
        if (isEditCarBrand) {
            builder.setCarType1(type1);
        }
        builder.setCarType2(type2);
        builder.setCarType3(type3);
        MyService.start(mContext, ProtoMessage.Cmd.cmdSetMyInfo.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(SetUserInfoProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(mContext, filter, getBroadcastManager());

        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(mContext, "设置失败");
            }

            @Override
            public void onGot(Intent i) {
                int code = i.getIntExtra("error_code", -1);
                if (code == ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(mContext, "设置成功");
//                    getInfor();
                    SharedPreferences preference = mContext.getSharedPreferences("token", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preference.edit();
                    if (isEditCarBrand) {
                        if (type1.equals("-")) {
                            editor.putString("car_band", "");//String
                            myCarBrand.setText(NOT_SET);
                        }
                    }
                    editor.putString("car_type2", "");//String
                    editor.putString("car_type3", "");//String
                    editor.commit();

                    myCarType.setText(NOT_SET);

                } else {
//                    ToastR.setToast(mContext, "设置失败");
                    new ResponseErrorProcesser(mContext, code);
                }
            }
        });

    }

    public void showPopupWindow() {
        if (popupWindow == null) {
            View contentView = LayoutInflater.from(this).inflate(R.layout.popup_layout, null);
            popupWindow = new PopupWindow(contentView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
            popupWindow.setContentView(contentView);
            ImageView code = (ImageView) contentView.findViewById(R.id.code_image);

            Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.icon_helper);
            Bitmap bitmap = ZXingUtils.createQRImage("http://poc.erobbing.com/CarRegister",
                    getResources().getIntArray(R.array.default_zxing_size)[0],
                    getResources().getIntArray(R.array.default_zxing_size)[1],
                    logo);
            code.setImageBitmap(bitmap);
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.alpha = 1f;
                    getWindow().setAttributes(params);
                }
            });
            // 需要设置一下此参数，点击外边可消失
            popupWindow.setBackgroundDrawable(new BitmapDrawable());
            // 设置点击窗口外边窗口消失
            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(false);
        }

        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
        } else {
            popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.alpha = 0.5f;
            getWindow().setAttributes(params);
        }
    }

    @Override
    public void onBackPressed() {
        if(popupWindow != null && popupWindow.isShowing()){
            popupWindow.dismiss();
        } else {
            super.onBackPressed();
        }
    }
}
