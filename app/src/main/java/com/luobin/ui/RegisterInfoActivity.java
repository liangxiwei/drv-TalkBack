package com.luobin.ui;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.filterfw.io.TextGraphReader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.CustomListener;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.bigkoo.pickerview.view.TimePickerView;
import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.BaseActivity;

import com.example.jrd48.chat.ImageTool;
import com.example.jrd48.chat.PicTool.LetterTileDrawable;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.example.jrd48.chat.permission.PermissionUtil;
import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.SetUserInfoProcesser;
import com.example.jrd48.service.protocol.root.UserInfoProcesser;
import com.kyleduo.switchbutton.SwitchButton;

import com.luobin.dvr.R;
import com.luobin.model.CarBrands;
import com.luobin.model.CarFirstType;
import com.luobin.model.City;
import com.luobin.model.County;
import com.luobin.model.Province;
import com.luobin.model.ViewAllCarParam;

import com.luobin.search.friends.car.DBManagerCarList;
import com.luobin.tool.FileUtils;
import com.luobin.tool.GlobalImg;
import com.luobin.tool.MyCarBrands;
import com.luobin.tool.MyInforTool;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author wangjunjie
 * 2019/3/25
 */
public class RegisterInfoActivity extends BaseDialogActivity implements
        PermissionUtil.PermissionCallBack, SelectInterestAdapter.OnRecyclerViewItemClickListener {


    /**
     * 调用相机
     */
    public static final int TAKE_PHOTO = 4;
    /**
     * 调用图库
     */
    private static final int PICTURE = 3;
    private static final int RESULT_MSG = 5;

    int MY_PERMISSIONS_REQUEST_CAMERA = 10011;
    int MY_PERMISSIONS_REQUEST_WRITE = 10033;

    public static final String NO_SET = "不限";
    public static final String NOT_SET = "未设置";
    public static final String OTHER = "其他";
    public static final String SET_CITY = "set_city";
    private final String BIRTHDAY = "birthday";
    private final String LOCATION = "location";

    /**
     * 头像图片
     */
    @BindView(R.id.imgHead)
    ImageView imgHead;

    /**
     * 修改我
     */
    @BindView(R.id.btnMyEdit)
    Button btnMyEdit;

    /**
     * 昵称
     */
    @BindView(R.id.tvName)
    TextView tvName;

    /**
     * 昵称开关
     */
    @BindView(R.id.sbtnName)
    SwitchButton sbtnName;

    /**
     * 个性签名
     */
    @BindView(R.id.tvSign)
    TextView tvSign;

    /**
     * 个性签名开关
     */
    @BindView(R.id.sbtnSign)
    SwitchButton sbtnSign;

    /**
     * 性别
     */
    @BindView(R.id.tvSex)
    TextView tvSex;

    /**
     * 性别开关
     */
    @BindView(R.id.sbtnSex)
    SwitchButton sbtnSex;

    /**
     * 手机号
     */
    @BindView(R.id.tvPhone)
    TextView tvPhone;
    /**
     * 手机号开关
     */
    @BindView(R.id.sbtnPhone)
    SwitchButton sbtnPhone;
    /**
     * 车牌号
     */
    @BindView(R.id.tvCarNo)
    TextView tvCarNo;
    /**
     * 车牌号开关
     */
    @BindView(R.id.sbtnCarNo)
    SwitchButton sbtnCarNo;
    /**
     * GPS
     */
    @BindView(R.id.tvGps)
    TextView tvGps;
    /**
     * GPS开关
     */
    @BindView(R.id.sbtnGps)
    SwitchButton sbtnGps;
    /**
     * 选填信息修改
     */
    @BindView(R.id.btnCarEdit)
    Button btnCarEdit;
    /**
     * 车型
     */
    @BindView(R.id.tvCarModels)
    TextView tvCarModels;
    @BindView(R.id.btnCarModels)
    Button btnCarModels;
    /**
     * 出生年月
     */
    @BindView(R.id.tvBirth)
    TextView tvBirth;
    @BindView(R.id.btnBirth)
    Button btnBirth;
    /**
     * 所在地
     */
    @BindView(R.id.tvLocation)
    TextView tvLocation;
    @BindView(R.id.btnLocation)
    Button btnLocation;
    /**
     * 家乡
     */
    @BindView(R.id.tvHome)
    TextView tvHome;
    @BindView(R.id.btnHome)
    Button btnHome;
    /**
     * 行业
     */
    @BindView(R.id.tvIndustry)
    TextView tvIndustry;
    @BindView(R.id.btnIndustry)
    Button btnIndustry;
    /**
     * 兴趣爱好
     */
    @BindView(R.id.tvInterest)
    TextView tvInterest;
    @BindView(R.id.btnInterest)
    Button btnInterest;

    /**
     * 关闭
     */
    @BindView(R.id.btnExit)
    Button btnExit;

    @BindView(R.id.imgClose)
    ImageView imgClose;

    /**
     * 选填的修改
     */
    boolean selectCarIsShow = true;

    /**
     * 个人信息的修改
     */
    boolean selectMyInfoIsShow = false;

    /**
     * 权限
     */
    protected PermissionUtil mPermissionUtil;
    /**
     * 弹窗
     */
    private ProgressDialog dialog;
    /**
     * 上下文对象
     */
    private Context mContext;

    /**
     * 类似实体类
     */
    private MyInforTool myInforTool;
    private ViewAllCarParam param;

    private String[] headItems = new String[]{"拍照", "从手机相册选择"};

    private String mCarID;

    /**
     * 图片地址
     */
    String mCurrentPhotoPath;
    /**
     * 手机号
     */
    String myPhone;


    private int sexdatashow = 0;

    String  tuichu ="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_info);
        getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);

        ButterKnife.bind(this);

        mContext = this;

        tuichu =getIntent().getStringExtra("tuichu");

        if (tuichu==null||"".equals(tuichu)){
            //TODO 推出按钮的展示
            btnExit.setVisibility(View.GONE);
        }else{
            btnExit.setVisibility(View.VISIBLE);
            btnExit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    logoutDialog(mContext);
                }
            });
        }





        waitDialog();
        initView();
        initData();


        new Thread() {
            @Override
            public void run() {
                handler.sendEmptyMessage(1);
            }
        }.start();


    }

    private void initData() {
        mPermissionUtil = PermissionUtil.getInstance();

        myInforTool = new MyInforTool(mContext, true);

        //初始化
        setInfor();
        //获取个人信息
        getInfor();
        initView();
        SharedPreferences preferences1 = getSharedPreferences("token", Context.MODE_PRIVATE);
        myPhone = preferences1.getString("phone", "");

    }

    private void initView() {

        selectBtnShow(false);
    }

    /**
     * 等待弹窗
     */
    private void waitDialog() {
        //创建ProgressDialog对象，自定义style
        dialog = new ProgressDialog(RegisterInfoActivity.this, R.style.CustomDialog);
        // 设置进度条风格，风格为圆形，旋转的
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // 设置ProgressDialog 提示信息
        dialog.setMessage("正在获取数据，请稍等...");
        // 设置ProgressDialog 的进度条是否不明确
        dialog.setIndeterminate(false);
        // 设置ProgressDialog 是否可以按退回按键取消
        dialog.setCancelable(true);
    }


    /**
     * 个人信息修改 选填修改 和关闭
     *
     * @param view
     */
    @OnClick({R.id.btnMyEdit, R.id.btnCarEdit, R.id.imgClose, R.id.imgHead})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnMyEdit:
                break;
            case R.id.btnCarEdit:
                selectBtnShow(selectCarIsShow);
                break;
            case R.id.imgClose:
                if (!checkHasEmpty()) {
                    finish();
                }
                break;
            case R.id.imgHead:
                if (!checkNetWork()) {
                    return;
                }
                toSetHead();
                break;
            default:
                break;
        }
    }


    private static ArrayList<String> options1Items = null;
    private static ArrayList<ArrayList<String>> options2Items = null;
    protected static ArrayList<ArrayList<ArrayList<String>>> options3Items = null;
    /**
     * 别的选择控件
     */
    OptionsPickerView pvOptions = null;
    /**
     * 时间选择控件
     */
    TimePickerView tpTime = null;


    /**
     * 选填信息各项点击
     */
    @OnClick({R.id.btnCarModels, R.id.btnBirth,
            R.id.btnLocation, R.id.btnHome,
            R.id.btnIndustry, R.id.btnInterest})
    void selectCarClick(View view) {
        switch (view.getId()) {
            case R.id.btnCarModels:

                selectTypeDialog(2, DIALOG_TYPE.CARTYPE);
                break;
            case R.id.btnBirth:
                timeDialog();
                break;
            case R.id.btnLocation:
                setPickerData();
                selectTypeDialog(2, DIALOG_TYPE.ADDRESS);

                break;
            case R.id.btnHome:
                setPickerData();
                selectTypeDialog(2, DIALOG_TYPE.HOME);
                break;
            case R.id.btnIndustry:
                selectTypeDialog(1, DIALOG_TYPE.INDUSTRY);
                break;
            case R.id.btnInterest:
                selectInterestDialog();
                break;
            default:
                break;
        }

    }


    /**
     * 个人信息各项点击
     */
    @OnClick({R.id.tvName, R.id.tvSign, R.id.tvSex,
            R.id.tvPhone, R.id.tvCarNo})
    void myInfoClick(View view) {
        switch (view.getId()) {
            case R.id.tvName:
                if (!checkNetWork()) {
                    return;
                }
                inputDialog("昵称", NAME, InputTextDialog.Type.TEXT);
                break;
            case R.id.tvSign:
                if (!checkNetWork()) {
                    return;
                }
                inputDialog("个性签名", SIGN, InputTextDialog.Type.TEXT);
                break;
            case R.id.tvSex:
                if (!checkNetWork()) {
                    return;
                }
                sexDialog();
                break;
            case R.id.tvCarNo:
                if (!checkNetWork()) {
                    return;
                }
                inputDialog("车牌号", CAR_NO, InputTextDialog.Type.TEXT);
                break;
            default:
                break;
        }

    }


    /**
     * 选填的btn 展示
     */
    private void selectBtnShow(boolean isShow) {
        if (isShow) {
            selectCarIsShow = false;
            btnCarModels.setVisibility(View.VISIBLE);
            btnBirth.setVisibility(View.VISIBLE);
            btnLocation.setVisibility(View.VISIBLE);
            btnHome.setVisibility(View.VISIBLE);
            btnIndustry.setVisibility(View.VISIBLE);
            btnInterest.setVisibility(View.VISIBLE);
        } else {
            selectCarIsShow = true;
            btnCarModels.setVisibility(View.GONE);
            btnBirth.setVisibility(View.GONE);
            btnLocation.setVisibility(View.GONE);
            btnHome.setVisibility(View.GONE);
            btnIndustry.setVisibility(View.GONE);
            btnInterest.setVisibility(View.GONE);

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
    protected void onPause() {

        if (simplelistdialog != null && simplelistdialog.isShowing()) {
            simplelistdialog.dismiss();
        }
        super.onPause();
    }



    @Override
    public void onPermissionReject(String strMessage) {
        if (strMessage.equals(PermissionUtil.PERMISSIONS_STORAGE)) {
            ToastR.setToastLong(RegisterInfoActivity.this, "应用存储权限已经拒绝，请到手机管家或者系统设置里授权");
        } else if (strMessage.equals(PermissionUtil.PERMISSIONS_CAMERA)) {
            ToastR.setToastLong(RegisterInfoActivity.this, "应用拍照权限已经拒绝，请到手机管家或者系统设置里授权");
        }
    }

    @Override
    public void onPermissionFail(String failType) {
        if (failType.equals(PermissionUtil.PERMISSIONS_STORAGE)) {
            ToastR.setToastLong(RegisterInfoActivity.this, "应用存储权限授权失败");
        } else if (failType.equals(PermissionUtil.PERMISSIONS_CAMERA)) {
            ToastR.setToastLong(RegisterInfoActivity.this, "应用拍照权限授权失败");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE) {
            mPermissionUtil.requestResult(RegisterInfoActivity.this, permissions, grantResults,
                    RegisterInfoActivity.this, PermissionUtil.PERMISSIONS_STORAGE);
        } else if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            mPermissionUtil.requestResult(RegisterInfoActivity.this, permissions, grantResults,
                    RegisterInfoActivity.this, PermissionUtil.PERMISSIONS_CAMERA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                //调用剪切程序
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap mImageBitmap;
                        Bundle extras = data.getExtras();
                        mImageBitmap = (Bitmap) extras.get("data");

                        Bitmap bitmapCompress = FileUtils.comp(mImageBitmap);
                        if (bitmapCompress == null) {
                            deleteMyFile(mCurrentPhotoPath);
                            return;
                        }
                        Bitmap bitmapSquare = FileUtils.ImageCrop(bitmapCompress);
                        setHead(bitmapSquare);
                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    }
                }
                break;
            case PICTURE:
                //打开图库图片后，发送
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    //（因为4.4以后图片Uri发生了变化）通过文件工具类 对uri进行解析得到图片路径
                    String image = FileUtils.getUriPath(this, uri);
                    Bitmap bitmapCompress = FileUtils.comp(BitmapFactory.decodeFile(image));
                    if (bitmapCompress == null) {
                        ToastR.setToastLong(RegisterInfoActivity.this, "图片选择失败,请检查图片是否正常！");
                        return;
                    }
                    Bitmap bitmapSquare = FileUtils.ImageCrop(bitmapCompress);
                    setHead(bitmapSquare);
                }

                break;
            default:
                break;

        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    getCarList();
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 设置head头像弹窗
     */
    public void headDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置头像");
        builder.setItems(headItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {
                    case 0:
                        if (PermissionUtil.isOverMarshmallow()) {
                            mPermissionUtil.requestPermission(RegisterInfoActivity.this, MY_PERMISSIONS_REQUEST_CAMERA,
                                    RegisterInfoActivity.this,
                                    PermissionUtil.PERMISSIONS_CAMERA, PermissionUtil.PERMISSIONS_CAMERA);
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

    /**
     * 调用系统相机
     */
    public void openCamera() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePictureIntent, TAKE_PHOTO);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("image", e.getMessage());
            ToastR.setToast(this, "请打开摄像头的使用权限！");

        }
    }


    /**
     * 通过系统图库选择图片发送
     */
    public void openPicture() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICTURE);
    }

    /**
     * 保存压缩好的Bitmap格式的头像图片
     */
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

    /**
     * 质量压缩方法
     *
     * @param image
     * @return
     */
    public static byte[] compressImage(Bitmap image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            int options = 100;
            int subtract;
            // 循环判断如果压缩后图片是否大于90kb,大于继续压缩
            while (baos.toByteArray().length / 1024 > 90) {
                subtract = setSubstractSize(baos.toByteArray().length / 1024);
                // 重置baos即清空baos
                baos.reset();
                //每次都减少10
                options -= subtract;
                if (options <= 0) {
                    options = 10;
                }
                // 这里压缩options%，把压缩后的数据存放到baos中
                image.compress(Bitmap.CompressFormat.JPEG, options, baos);

            }
            return baos.toByteArray();
        } catch (Throwable e) {
            e.printStackTrace();
            Log.w("tttt", "MyInforActivity compressImage Throwable:" + e.getMessage());
            return null;
        }
    }

    /**
     * 根据图片的大小设置压缩的比例，提高速度
     *
     * @param imageMB
     * @return
     */
    private static int setSubstractSize(int imageMB) {

        if (imageMB > 1000) {
            return 60;
        } else if (imageMB > 750) {
            return 40;
        } else if (imageMB > 500) {
            return 20;
        } else {
            return 10;
        }

    }

    /**
     * 圆形图片
     */

    private Bitmap getUserFace(Bitmap bmp, String str) {
        try {
            if (bmp == null) {
//                return null;
                LetterTileDrawable drawable;
                drawable = new LetterTileDrawable(getResources());
                String nameStr = TextUtils.isEmpty(str) ? "1" : str;
                drawable.setContactDetails(nameStr, nameStr);
                bmp = FriendFaceUtill.drawableToBitmap(drawable);
            }
            // 将图片缩小
            Bitmap bitmap1 = Bitmap.createScaledBitmap(bmp, 150, 150,
                    false);
            // 图片头像变成圆型
            bmp = ImageTool.toRoundBitmap(bitmap1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;
    }


    byte[] bitmapByte = null;

    /**
     * 设置头像
     */
    private void setHead(final Bitmap image) {
        if (image == null) {
            ToastR.setToast(mContext, "未获取到图片");
            return;
        }
        if (dialog != null) {
            dialog.show();
        }

        //压缩图片到90kb
        bitmapByte = compressImage(image);
        if (bitmapByte == null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            bitmapByte = baos.toByteArray();
        }
        ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
        builder.setUserPic(com.google.protobuf.ByteString.copyFrom(bitmapByte));
        MyService.start(RegisterInfoActivity.this, ProtoMessage.Cmd.cmdSetMyInfo.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(SetUserInfoProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(RegisterInfoActivity.this, filter, getBroadcastManager());

        b.startReceiver(BaseDialogActivity.REQUEST_TIME_OUT, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                if (dialog != null) {
                    dialog.dismiss();
                }
                ToastR.setToast(RegisterInfoActivity.this, "连接超时，请检查网络是否连接(或者网络质量差)");
            }

            @Override
            public void onGot(Intent i) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                int code = i.getIntExtra("error_code", -1);
                if (code == ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(RegisterInfoActivity.this, "设置成功");
                    FriendFaceUtill.saveFriendFaceImg(myInforTool.getUserName(), myPhone, bitmapByte, mContext);
                    GlobalImg.reloadImg(mContext, myPhone);
                    Bitmap bitmap = GlobalImg.getImage(mContext, myPhone);
                    imgHead.setImageBitmap(getUserFace(bitmap, myInforTool.getUserName()));
                } else {
                    new ResponseErrorProcesser(RegisterInfoActivity.this, code);
                }
            }
        });
    }

    private String[] sexShow = new String[]{"男", "女", "未设置"};
    private String[] sexItems = new String[]{"未设置", "男", "女", "未设置"};
    int sexdefualt;
    private int sexdata = 0;

    /**
     * 设置性别弹窗
     */
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
                ToastR.setToast(RegisterInfoActivity.this, "你选择了：" + sexItems[sexdefualt + 1]);
                sexdata = sexdefualt + 1;
                setSex(sexdata, sexdefualt);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog simplechoicedialog = builder.create();
        simplechoicedialog.show();
    }


    /**
     * 设置性别
     */
    private void setSex(final int sexdata, final int sexdefual) {
        ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
        builder.setUserSex(sexdata);
      /*  builder.setInterest("兴趣");
        builder.setCareer("行业");
        builder.setSignature("备注");*/
        MyService.start(RegisterInfoActivity.this, ProtoMessage.Cmd.cmdSetMyInfo.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(SetUserInfoProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(RegisterInfoActivity.this, filter, getBroadcastManager());

        b.startReceiver(BaseDialogActivity.REQUEST_TIME_OUT, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(RegisterInfoActivity.this, "设置失败");
            }

            @Override
            public void onGot(Intent i) {

                int code = i.getIntExtra("error_code", -1);
                if (code == ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(RegisterInfoActivity.this, "设置成功");
                    sexdatashow = sexdefual;
                    tvSex.setText(sexItems[sexdata] + "");
                    SharedPreferences preference = mContext.getSharedPreferences("token", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preference.edit();
                    //int
                    editor.putInt("user_sex", sexdata);
                    editor.commit();

                } else {

                    new ResponseErrorProcesser(RegisterInfoActivity.this, code);
                }
            }
        });
    }

    /**
     * 设置昵称
     */
    private void setName(final String name, final boolean isNickName) {
        ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
        if (isNickName) {
            builder.setNickName(name);
        } else {
            builder.setUserName(name);
        }
        MyService.start(RegisterInfoActivity.this, ProtoMessage.Cmd.cmdSetMyInfo.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(SetUserInfoProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(RegisterInfoActivity.this, filter, getBroadcastManager());

        b.startReceiver(BaseDialogActivity.REQUEST_TIME_OUT, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(RegisterInfoActivity.this, "设置失败");
            }

            @Override
            public void onGot(Intent i) {
//                mHandler.postDelayed(mShowProgress, 500);
//                mHandler.removeCallbacks(mShowProgress);
//                m_pDialog.dismiss();
                int code = i.getIntExtra("error_code", -1);
                if (code == ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(RegisterInfoActivity.this, "设置成功");

                    SharedPreferences preference = mContext.getSharedPreferences("token", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preference.edit();
                    if (isNickName) {
                        tvName.setText(name);
                        //int
                        editor.putString("nick_name", name);
                    } else {
                        tvName.setText(name);
                        //int
                        editor.putString("name", name);
                    }
                    editor.commit();
                } else if (code == ProtoMessage.ErrorCode.NAME_TOO_SHORT_VALUE) {
                    ToastR.setToastLong(RegisterInfoActivity.this, "昵称输入太短（2-10个汉字/4-32个字符）");
                } else {
                    ToastR.setToast(RegisterInfoActivity.this, "设置失败");
                    new ResponseErrorProcesser(RegisterInfoActivity.this, code);
                }
            }
        });
    }

    /**
     * 获取个人信息
     */
    private void getInfor() {
        if (!ConnUtil.isConnected(this)) {
            Log.w("network", "当前没有网络（请检查网络是否连接）");
            return;
        }
        dialog.show();
        ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
        MyService.start(RegisterInfoActivity.this, ProtoMessage.Cmd.cmdGetMyInfo.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(UserInfoProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(RegisterInfoActivity.this, filter, getBroadcastManager());
        b.startReceiver(BaseDialogActivity.REQUEST_TIME_OUT, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ShowMyinfo();
                ToastR.setToast(RegisterInfoActivity.this, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                ShowMyinfo();
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK_VALUE) {
                    mCarID = i.getStringExtra("car_id");
                    if (myInforTool == null) {
                        myInforTool = new MyInforTool(mContext, true);
                    } else {
                        myInforTool.load();
                    }
                    setInfor();
                } else {
                    new ResponseErrorProcesser(RegisterInfoActivity.this, i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void ShowMyinfo() {
        dialog.dismiss();
    }

    /**
     * 设置个人数据
     */
    private void setInfor() {
        //TODO 设置个人数据 从服务器上拉取的


        if (TextUtils.isEmpty(myInforTool.getBirthday()) || myInforTool.getBirthday().compareTo("1900-01-01") <= 0) {
            tvSign.setText("未设置");
        } else {
            tvSign.setText(myInforTool.getBirthday());
        }
        // myPhone 18909980686
        Bitmap bmp = GlobalImg.getImage(RegisterInfoActivity.this, myPhone);
        imgHead.setImageBitmap(getUserFace(bmp, myInforTool.getUserName() + ""));
        tvName.setText(myInforTool.getUserName() + "");
        sexdata = myInforTool.getUserSex();
        if (sexdata == 0) {
            sexdatashow = 2;
        } else {
            sexdatashow = sexdata - 1;
        }

        sexdefualt = sexdatashow;

        tvPhone.setText(myPhone);
        tvSex.setText(sexItems[sexdata] + "");

        //TODO 天坑
        sbtnName.setChecked(!myInforTool.isSwitchNickName());
        sbtnSex.setChecked(!myInforTool.isSwitchSex());
        sbtnPhone.setChecked(!myInforTool.isSwitchPhoneNumber());
        sbtnCarNo.setChecked(!myInforTool.isSwitchCarNumber());
        sbtnGps.setChecked(!myInforTool.isSwitchLocationUpload());

        //TODO 需要新增
        sbtnSign.setChecked(!myInforTool.isSwitchCarBrand());

    }


    private static final String NAME = "name";
    private static final String SIGN = "sign";
    private static final String PHONE = "phone";
    private static final String CAR_NO = "carNo";

    /**
     * 输入框
     *
     * @param title 标题
     * @param type  分类
     * @param etype enum
     */
    private void inputDialog(String title, final String type, InputTextDialog.Type etype) {
        InputTextDialog signDialog = new InputTextDialog(this, title, etype,
                new InputTextDialog.GetPasswordListener() {
                    @Override
                    public void getPassword(String password) {
                        switch (type) {
                            case NAME:
                                if (password.length() < GlobalStatus.MIN_TEXT_BYTE_COUNT) {
                                    ToastR.setToastLong(RegisterInfoActivity.this, "昵称输入太短（2-10个汉字/4-32个字符）");
                                } else if (password.length() > GlobalStatus.MAX_TEXT_BYTE_COUNT) {
                                    ToastR.setToastLong(RegisterInfoActivity.this, "昵称输入过长（2-10个汉字/4-32个字符）");
                                } else {
                                    setName(password, false);
                                }
                                break;
                            case SIGN:
                                tvSign.setText(password);
                                break;
                            case CAR_NO:

                                tvCarNo.setText(password);
                                break;
                            default:
                                break;
                        }
                    }
                });
        signDialog.show();

    }

    private void toSetHead() {
        if (PermissionUtil.isOverMarshmallow()) {
            mPermissionUtil.requestPermission(RegisterInfoActivity.this, MY_PERMISSIONS_REQUEST_WRITE,
                    RegisterInfoActivity.this, PermissionUtil.PERMISSIONS_STORAGE,
                    PermissionUtil.PERMISSIONS_STORAGE);
        } else {
            headDialog();
        }
    }


    /**
     * 判断网络是否连接
     */
    private boolean checkNetWork() {
        boolean hasNetwork = ConnUtil.isConnected(RegisterInfoActivity.this);
        if (!hasNetwork) {
            ToastR.setToast(RegisterInfoActivity.this, getResources().getString(R.string.network_show));
        }
        return hasNetwork;
    }


    /**
     * 初始化选项数据 3级列表的城市
     */
    public void setPickerData() {
//        if (options1Items == null) {
        options1Items = new ArrayList();
        options2Items = new ArrayList<>();
        options3Items = new ArrayList();

        ArrayList<Province> data = new ArrayList<Province>();
        try {
            String json;
//            if (mType.equals(MyInforActivity.SET_CITY)) {
//                json = convertString(.getAssets().open("cityand.json"), "utf-8");
//            } else {
//                json = convertString(context.getAssets().open("city.json"), "utf-8");
//            }
            json = convertString(this.getAssets().open("cityand.json"), "utf-8");
            data.addAll(JSON.parseArray(json, Province.class));
        } catch (Exception e) {
            Log.e("helper", " get data error:" + e.getMessage());
            e.printStackTrace();
        }
        //添加省
        for (int x = 0; x < data.size(); x++) {
            Province pro = data.get(x);
            options1Items.add(pro.getAreaName());
            ArrayList<City> cities = pro.getCities();
            ArrayList<String> xCities = new ArrayList<String>();
            ArrayList<ArrayList<String>> xCounties = new ArrayList<>();
            int citySize = cities.size();
            //添加地市
            for (int y = 0; y < citySize; y++) {
                City cit = cities.get(y);
                xCities.add(cit.getAreaName());
                ArrayList<County> counties = cit.getCounties();
                ArrayList<String> yCounties = new ArrayList<String>();
                int countySize = counties.size();
                //添加区县
                if (countySize == 0) {
//                    if (mType.equals(MyInforActivity.SET_CITY)) {
//                        yCounties.add(cit.getAreaName());
//                    } else {
//                        yCounties.add("");
//                    }
                    yCounties.add(cit.getAreaName());
                } else {
                    for (int z = 0; z < countySize; z++) {
                        yCounties.add(counties.get(z).getAreaName());
                    }
                }
                xCounties.add(yCounties);
            }
//            if (mType.equals(MyInforActivity.SET_CITY)) {
//            } else {
//                if (xCities.size() <= 0) {
//                    xCities.add("");
//                    xCounties.add(xCities);
//                }
//            }

            options2Items.add(xCities);
            options3Items.add(xCounties);
        }


    }

    public String convertString(InputStream is, String charset) {
        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            is.close();
        } catch (IOException e) {
            Log.e("convertString", e.toString());
        }
        return sb.toString();
    }


    /**
     * 时间dialog
     */
    void timeDialog() {
        //系统当前时间
        Calendar selectedDate = Calendar.getInstance();
        Calendar startDate = Calendar.getInstance();
        startDate.set(startDate.get(Calendar.YEAR) - 100, startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DAY_OF_MONTH));
        Calendar endDate = Calendar.getInstance();


        tpTime = new TimePickerBuilder(this, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View view) {
                tvBirth.setText(getTime(date));
                //TODO 设置出生年月

            }
        }).setLayoutRes(R.layout.dialog_select_time, new CustomListener() {
            @Override
            public void customLayout(View view) {
                TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);
                tvTitle.setText("出生年月");
                Button btnSure = (Button) view.findViewById(R.id.btnSure);
                btnSure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tpTime.returnData();
                        tpTime.dismiss();
                    }
                });


            }
        })
                .isCyclic(true)
                .setDate(selectedDate)
                .setRangDate(startDate, endDate)
                .setDividerColor(getResources().getColor(R.color.dialog_color_line))
                //设置选中项文字颜色
                .setTextColorCenter(getResources().getColor(R.color.text_color_90white))
                .setContentTextSize(20)
                .setType(new boolean[]{true, true, false, false, false, false})
                .setLabel("", "", "", "", "", "秒")
                .build();
        Dialog mDialog = tpTime.getDialog();
        if (mDialog != null) {

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM);

            params.leftMargin = 0;
            params.rightMargin = 0;
            tpTime.getDialogContainerLayout().setLayoutParams(params);

            Window dialogWindow = mDialog.getWindow();
            if (dialogWindow != null) {
                //修改动画样式
                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);
                //改成Bottom,底部显示
                dialogWindow.setGravity(Gravity.BOTTOM);
                dialogWindow.setDimAmount(0.1f);
            }
        }
        tpTime.show();

    }

    /**
     * 转时间格式
     *
     * @param date
     * @return
     */
    private String getTime(Date date) {
        //可根据需要自行截取数据显示
        Log.d("getTime()", "choice date millis: " + date.getTime());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        return format.format(date);
    }

    @Override
    public void onItemClick(List<InterestBean> interestBeans) {

        //TODO 设置兴趣爱好
        String interestName = "";
        for (int a = 0; a < interestBeans.size(); a++) {
            if (interestBeans.get(a).isChecked()) {
                interestName += interestBeans.get(a).getName() + ",";
            }

        }
        tvInterest.setText(interestName);

    }


    public enum DIALOG_TYPE {
        //车型
        CARTYPE,
        //ADDRESS
        ADDRESS,
        //行业
        INDUSTRY,
        //家乡
        HOME,

    }


    private DIALOG_TYPE type;

    void selectTypeDialog(final int num, final DIALOG_TYPE type) {
        pvOptions = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                //返回的分别是三个级别的选中位置
                String one = "";
                String tx = "";
                String two = "";
                if (type == DIALOG_TYPE.CARTYPE) {
                    one = car1items.get(options1);
                    tx = "";

                    if (num == 1) {
                        tx = one;
                    } else if (num == 2) {
                        two = car2items.get(options1).get(options2);
                        tx = one + two;
                    }
                } else {
                    one = options1Items.get(options1);
                    tx = "";

                    if (num == 1) {
                        tx = one;
                    } else if (num == 2) {
                        two = options2Items.get(options1).get(options2);
                        tx = one + two;
                    }

                }

                if (type == DIALOG_TYPE.CARTYPE) {
                    tvCarModels.setText(tx);
                    //TODO 在这里进行发送数据
                } else if (type == DIALOG_TYPE.ADDRESS) {
                    tvLocation.setText(tx);
                    //TODO 在这里进行发送数据
                } else if (type == DIALOG_TYPE.INDUSTRY) {
                    tvIndustry.setText(tx);
                    //TODO 在这里进行发送数据
                } else if (type == DIALOG_TYPE.HOME) {
                    tvHome.setText(tx);
                }


            }
        }).setLayoutRes(R.layout.dialog_select, new CustomListener() {
            @Override
            public void customLayout(View v) {
                TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);

                if (type == DIALOG_TYPE.CARTYPE) {
                    tvTitle.setText("车型");
                } else if (type == DIALOG_TYPE.ADDRESS) {
                    tvTitle.setText("所在地");
                } else if (type == DIALOG_TYPE.INDUSTRY) {
                    tvTitle.setText("行业");
                } else if (type == DIALOG_TYPE.HOME) {
                    tvTitle.setText("家乡");
                }

                Button btnSure = (Button) v.findViewById(R.id.btnSure);
                btnSure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pvOptions.returnData();
                        pvOptions.dismiss();


                    }
                });

            }
        })
                .setDividerColor(getResources().getColor(R.color.dialog_color_line))
                //设置选中项文字颜色
                .setTextColorCenter(getResources().getColor(R.color.text_color_90white))
                .setContentTextSize(20)
                .build();

        if (num == 1) {
            //TODO 行业假数据 测试数据坑死爹
            options1Items = new ArrayList<>();
            String[] testData =  {"java开发-假数据","C开发-假数据",
                    "产品经理-假数据","android开发-假数据"
                    ,"ios开发-假数据","RN开发-假数据"
                    ,"前端开发-假数据"};
            for (String a :Arrays.asList(testData)){
                options1Items.add(a);
            }

            //二级选择器*/
            pvOptions.setPicker(options1Items);
        } else if (num == 2) {
            //二级选择器*/
            if (type == DIALOG_TYPE.CARTYPE) {

                if (car1items == null || car1items.size() < 1) {
                    ToastR.setToast(mContext, "数据为空正在获取汽车品牌");
                    getCarList();
                    return;
                }

                pvOptions.setPicker(car1items, car2items);
            } else {
                pvOptions.setPicker(options1Items, options2Items);
            }
        }


        pvOptions.show();
    }


    /**
     * CarBrands 车数据
     */
    ArrayList<CarInfoBean_New> carDataNew = new ArrayList<>();
    ArrayList<CarFirstType> carFirstData = new ArrayList<>();
    ArrayList<String> data = new ArrayList<>();
    private static ArrayList<String> car1items = new ArrayList<>();
    private static ArrayList<ArrayList<String>> car2items = new ArrayList<>();

    /**
     * 获取车辆型号
     */
    private void getCarList() {
        car1items = new ArrayList<>();
        car2items = new ArrayList<ArrayList<String>>();

        DBManagerCarList carListDBM = new DBManagerCarList(this);

        ArrayList<CarBrands> carData = MyCarBrands.getData();
        if (carData == null || carData.size() <= 0) {
            carData = carListDBM.getCarBrandsList(false);
        }

        carListDBM.closeDB();

        for (CarBrands carBrands : carData) {
            CarInfoBean_New carInfoBean_new = new CarInfoBean_New();
            carInfoBean_new.setCarBrandID(carBrands.getCarBrandID());
            carInfoBean_new.setCarBrandName(carBrands.getCarBrandName());
            carInfoBean_new.setVersion(carBrands.getVersion());
            carDataNew.add(carInfoBean_new);
            car1items.add(carBrands.getCarBrandName());
        }


        for (int i = 0; i < carDataNew.size(); i++) {
            carFirstData = new DBManagerCarList(this).getCarFristTypesList(carDataNew.get(i).getCarBrandID());
            CarInfoBean_New carInfoBean = new CarInfoBean_New();
            carInfoBean.setCarFirstTypes(carFirstData);
            carInfoBean.setVersion(carDataNew.get(i).getVersion());
            carInfoBean.setCarBrandName(carDataNew.get(i).getCarBrandName());
            carInfoBean.setCarBrandID(carDataNew.get(i).getCarBrandID());
            carDataNew.set(i, carInfoBean);

        }

        for (int i = 0; i < carDataNew.size(); i++) {
            data = new ArrayList<>();

            for (int i1 = 0; i1 < carDataNew.get(i).getCarFirstTypes().size(); i1++) {
                data.add(carDataNew.get(i).getCarFirstTypes().get(i1).getCarFirstTypeName());
                Log.i("data is Show >", carDataNew.get(i).getCarFirstTypes().get(i1).getCarFirstTypeName());
            }
            car2items.add(data);

        }
    }

    String[] list = {"汽车", "旅游", "动漫", "影视",
            "时尚", "音乐", "体育", "美食",
            "摄影", "宠物", "钓鱼", "工艺",
            "手工", "游戏"};

    List<InterestBean> interestList = new ArrayList<>();

    /**
     * 兴趣爱好
     */
    private void selectInterestDialog() {
        interestList = new ArrayList<>();
        for (int a = 0; a < Arrays.asList(list).size(); a++) {
            InterestBean interestBean = new InterestBean();
            interestBean.setName(Arrays.asList(list).get(a));
            interestBean.setChecked(false);
            interestList.add(interestBean);
        }

        SelectInterestDialog selectInterestDialog = new SelectInterestDialog(this
                , interestList);

        selectInterestDialog.show();

    }

//    private boolean checkHasEmpty(){
//        boolean isEmpty = false;
//        if(nickName.getText().equals(myPhone)) {
//            ToastR.setToast(mContext,"请设置昵称");
//            isEmpty = true;
//        } else if (TextUtils.isEmpty(myPlateNumber.getText()) || myPlateNumber.getText().equals(NOT_SET)){
//            ToastR.setToast(mContext,"请输入车牌号");
//            isEmpty = true;
//        } else if (TextUtils.isEmpty(myCarBrand.getText()) || myCarBrand.getText().equals(NOT_SET)){
//            ToastR.setToast(mContext,"请选择车品牌");
//            isEmpty = true;
//        } else if (TextUtils.isEmpty(myCarType.getText()) || myCarType.getText().equals(NOT_SET)){
//            ToastR.setToast(mContext,"请选择车型");
//            isEmpty = true;
//        }
//        return isEmpty;
//    }

    private boolean checkHasEmpty() {
        boolean isEmpty = false;
        if (tvName.getText().equals(myPhone)) {
            ToastR.setToast(mContext, "请设置昵称");
            isEmpty = true;
        } else if (TextUtils.isEmpty(tvSign.getText()) || tvSign.getText().equals(NOT_SET)) {
            ToastR.setToast(mContext, "请输入个性签名");
            isEmpty = true;
        } else if (TextUtils.isEmpty(tvSex.getText()) || tvSex.getText().equals(NOT_SET)) {
            ToastR.setToast(mContext, "请选择性别");
            isEmpty = true;
        }  else if (TextUtils.isEmpty(tvCarNo.getText()) || tvCarNo.getText().equals(NOT_SET)) {
            ToastR.setToast(mContext, "请输入车牌号");
            isEmpty = true;
        }
        return isEmpty;
    }


    @Override
    public void onBackPressed() {
        if (!checkHasEmpty()) {
            finish();
        }
    }


    /**
     * 兴趣初始化
     */
    private void initItemX() {

    }

    AlertDialog simplelistdialog;
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



}


