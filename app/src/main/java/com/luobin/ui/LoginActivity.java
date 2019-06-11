package com.luobin.ui;


import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.chat.permission.PermissionUtil;
import com.example.jrd48.chat.permission.PermissonManager;
import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.LoginProcesser;
import com.luobin.dvr.R;
import com.luobin.model.CarBrands;
import com.luobin.search.friends.car.CheckAndUpdateCarTypeThread;
import com.luobin.search.friends.car.DBManagerCarList;
import com.luobin.tool.MyInforTool;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * @author wangjunjie
 */
public class LoginActivity extends BaseDialogActivity implements PermissionUtil.PermissionCallBack {

    /**
     * 关闭
     */
    @BindView(R.id.imgClose)
    ImageView imgClose;

    /**
     * 账号
     */
    @BindView(R.id.edName)
    EditText edName;

    /**
     * 密码
     */
    @BindView(R.id.edPass)
    EditText edPass;

    /**
     * 注册
     */
    @BindView(R.id.tvRegister)
    TextView tvRegister;

    /**
     * 登录
     */
    @BindView(R.id.tvLogin)
    TextView tvLogin;

    /**
     * 忘记密码
     */
    @BindView(R.id.tvForget)
    TextView tvForget;

    /**
     * 权限
     */
    protected PermissionUtil mPermissionUtil;

    /**
     * dialog
     */
    private ProgressDialog dialog;


    List<String> mList;
    List<String> mDeniedPermissionList = new ArrayList<>();
    List<ImageView> mImageViewList = new ArrayList<>();
    List<RelativeLayout> mRelativeLayoutList = new ArrayList<>();
    List<PermissonManager> mPermissonManagerList = new ArrayList<>();


    private Handler mHandler = new Handler();


    /**
     * 保持服务
     */
    boolean isKeepService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);

        ButterKnife.bind(this);
        initData();
        initDialog();
    }


    @Override
    protected void onResume() {
        super.onResume();
        isKeepService = false;
        MyService.start(LoginActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isKeepService) {
            //TODO 设置服务
            // MyService.stopService(LoginActivity.this);
            isKeepService = false;
        }
    }


    private void initDialog() {

        //********************************************弹窗设置****************************************************
        //创建ProgressDialog对象
        dialog = new ProgressDialog(LoginActivity.this, R.style.CustomDialog);
        // 设置进度条风格，风格为圆形，旋转的
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // 设置ProgressDialog 提示信息
        dialog.setMessage("正在登录,请稍等...");
        // 设置ProgressDialog 的进度条是否不明确
        dialog.setIndeterminate(false);
        // 设置ProgressDialog 是否可以按退回按键取消
        dialog.setCancelable(false);
        //********************************************弹窗设置****************************************************

    }

    private void initData() {
        mPermissionUtil = PermissionUtil.getInstance();
        mList = mPermissionUtil.getPermissionsList();
        mPermissonManagerList.clear();
        for (String str : mList) {
            PermissonManager pm = new PermissonManager();
            pm.setPermisson(str);
            mPermissonManagerList.add(pm);
        }

    }


    /**
     * dialog 展示
     */
    private Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            dialog.show();
        }
    };


    /**
     * 检查手机号 10位
     */
    private boolean checkPhoneNum() {
        boolean isPhoneNum = false;
        try {
            String phone = edName.getText().toString().trim();
            if (phone.length() >= 10) {
                isPhoneNum = true;
            }
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
        }
        return isPhoneNum;
    }


    @OnClick({R.id.imgClose, R.id.tvRegister, R.id.tvLogin, R.id.tvForget})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgClose:
                finish();
                break;
            case R.id.tvRegister:

                Intent intent = new Intent(this, RegisterAndForGetActivity.class);
                intent.putExtra("type", RegisterAndForGetActivity.REGISTER);
                startActivity(intent);

                break;
            case R.id.tvLogin:
                login();
                break;
            case R.id.tvForget:
                Intent intentFprget = new Intent(this, RegisterAndForGetActivity.class);
                intentFprget.putExtra("type", RegisterAndForGetActivity.FORGET);
                startActivity(intentFprget);

                break;
            default:
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        mPermissionUtil.requestResult(this, permissions, grantResults, this, PermissionUtil.TYPE);
    }

    @Override
    public void onPermissionSuccess(String type) {
        requestAllPermisson();
    }

    @Override
    public void onPermissionReject(String strMessage) {
        ToastR.setToastLong(LoginActivity.this, "该权限已经拒绝，请到手机管家或者系统设置里授权");

    }

    @Override
    public void onPermissionFail(String failType) {

    }


    private void requestAllPermisson() {
        if (mPermissionUtil.checkPermissions(this)) {
            showPermissionDialog();
        }
    }

    private void showPermissionDialog() {
        final AlertDialog dlg = new AlertDialog.Builder(LoginActivity.this).create();
        dlg.setCancelable(false);
        dlg.show();
        Window window = dlg.getWindow();
        window.setContentView(R.layout.permisson_managet_activity);

        final ImageView mIVReadContacts = (ImageView) window.findViewById(R.id.iv_read_contacts);
        final ImageView mIVSMS = (ImageView) window.findViewById(R.id.iv_icon_location);
        final ImageView mIVStorage = (ImageView) window.findViewById(R.id.iv_icon_storage);
        final ImageView mIVCamera = (ImageView) window.findViewById(R.id.iv_icon_camera);
        final ImageView mIVRecordAudio = (ImageView) window.findViewById(R.id.iv_icon_record_audio);
        final Button btnCancel = (Button) window.findViewById(R.id.btn_cancel_dialog);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogShow();
                dlg.cancel();
            }
        });
        final RelativeLayout mRLReadContacts = (RelativeLayout) window.findViewById(R.id.rl_read_contacts);
        mRLReadContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPermissionUtil.requestPermission(LoginActivity.this, PermissionUtil.MY_READ_CONTACTS, LoginActivity.this,
                        PermissionUtil.PERMISSIONS_READ_CONTACTS, PermissionUtil.PERMISSIONS_READ_CONTACTS);
                dlg.cancel();
            }
        });
        final RelativeLayout mRLLocation = (RelativeLayout) window.findViewById(R.id.rl_location);
        mRLLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPermissionUtil.requestPermission(LoginActivity.this, PermissionUtil.MY_READ_LOCATION, LoginActivity.this,
                        PermissionUtil.PERMISSIONS_LOCATION, PermissionUtil.PERMISSIONS_LOCATION);
                dlg.cancel();
            }
        });
        final RelativeLayout mRLStorage = (RelativeLayout) window.findViewById(R.id.rl_storage);
        mRLStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPermissionUtil.requestPermission(LoginActivity.this, PermissionUtil.MY_WRITE_EXTERNAL_STORAGE, LoginActivity.this,
                        PermissionUtil.PERMISSIONS_STORAGE, PermissionUtil.PERMISSIONS_STORAGE);
                dlg.cancel();
            }
        });
        final RelativeLayout mRLCamera = (RelativeLayout) window.findViewById(R.id.rl_camera);
        mRLCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPermissionUtil.requestPermission(LoginActivity.this, PermissionUtil.MY_CAMERA, LoginActivity.this,
                        PermissionUtil.PERMISSIONS_CAMERA, PermissionUtil.PERMISSIONS_CAMERA);
                dlg.cancel();
            }
        });
        final RelativeLayout mRLRecordAudio = (RelativeLayout) window.findViewById(R.id.rl_record_audio);
        mRLRecordAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPermissionUtil.requestPermission(LoginActivity.this, PermissionUtil.MY_RECORD_AUDIO, LoginActivity.this,
                        PermissionUtil.PERMISSIONS_RECORD_AUDIO, PermissionUtil.PERMISSIONS_RECORD_AUDIO);
                dlg.cancel();
            }
        });
        mImageViewList.clear();
        mImageViewList.add(mIVReadContacts);
        mImageViewList.add(mIVSMS);
        mImageViewList.add(mIVStorage);
        mImageViewList.add(mIVCamera);
        mImageViewList.add(mIVRecordAudio);
        mRelativeLayoutList.clear();
        mRelativeLayoutList.add(mRLReadContacts);
        mRelativeLayoutList.add(mRLLocation);
        mRelativeLayoutList.add(mRLStorage);
        mRelativeLayoutList.add(mRLCamera);
        mRelativeLayoutList.add(mRLRecordAudio);
        for (int i = 0; i < mImageViewList.size(); i++) {
            mPermissonManagerList.get(i).setmImageView(mImageViewList.get(i));
            mPermissonManagerList.get(i).setmRelativeLayout(mRelativeLayoutList.get(i));
        }
        changeImageView();
    }


    private void changeImageView() {
        mDeniedPermissionList = mPermissionUtil.findDeniedPermissions(LoginActivity.this, mList);
        for (PermissonManager pm : mPermissonManagerList) {
            int i = 0;
            for (String str : mDeniedPermissionList) {
                i++;
                if (str.equals(pm.getPermisson())) {
                    pm.getmRelativeLayout().setVisibility(View.VISIBLE);
                    pm.getmImageView().setImageResource(R.drawable.prohibit);
                    break;
                }
            }
        }
    }

    private void dialogShow() {
        // 提示框标题
        new AlertDialog.Builder(LoginActivity.this, AlertDialog.THEME_HOLO_LIGHT).setTitle("提示")
                .setMessage("您尚有权限未允许，可能会影响到后面应用的使用，确认退出？")
                // 提示框的两个按钮
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestAllPermisson();
                        dialog.dismiss();
                    }
                }).create().show();
    }

    private void login() {
        if (!checkPhoneNum()) {
            ToastR.setToastLong(LoginActivity.this, "手机号输入错误");
            return;
        }
        if (TextUtils.isEmpty(edPass.getText().toString())) {
            ToastR.setToastLong(LoginActivity.this, "请输入密码");
            return;
        }
        if (!ConnUtil.isConnected(LoginActivity.this)) {
            ToastR.setToast(LoginActivity.this, getResources().getString(R.string.network_show));
            return;
        }
        // 确保Socket已经启动

        mHandler.postDelayed(mShowProgress, 500);
        tvLogin.setClickable(false);
        ProtoMessage.UserLogin.Builder builder = ProtoMessage.UserLogin.newBuilder();
        builder.setPhoneNum(edName.getText().toString());
        builder.setPassword(edPass.getText().toString());
        builder.setAppType(ProtoMessage.AppType.appCar_VALUE);

        MyService.start(LoginActivity.this, ProtoMessage.Cmd.cmdLogin.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(LoginProcesser.ACTION);
        new TimeoutBroadcast(LoginActivity.this, filter, getBroadcastManager()).startReceiver(5, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                mHandler.removeCallbacks(mShowProgress);
                tvLogin.setClickable(true);
                dialog.dismiss();
                MyService.start(LoginActivity.this);
                ToastR.setToast(LoginActivity.this, "连接超时,请重新登录");
            }

            @Override
            public void onGot(Intent i) {
                mHandler.removeCallbacks(mShowProgress);
                tvLogin.setClickable(true);
                dialog.dismiss();
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    isKeepService = true;
                    ToastR.setToast(LoginActivity.this, "登录成功");

                    SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("token", i.getStringExtra("token"));
                    editor.putString("phone", edName.getText().toString());
                    editor.commit();
                    DBManagerCarList carListDB = new DBManagerCarList(LoginActivity.this);
                    try {
                        ArrayList<CarBrands> date = carListDB.getCarBrandsList(true);
                        if (date == null || date.size() <= 0) {
                            new CheckAndUpdateCarTypeThread((MyApplication) getApplication()).start();
                        } else {
                            for (CarBrands carBrands : date) {
                                if (carBrands.getCarBrandID() == 16) {
                                    String carBrandName = carBrands.getCarBrandName();
                                    if (!TextUtils.isEmpty(carBrandName) && carBrandName.equals("布嘉迪")) {
                                        carListDB.modifyCarBrand(carBrands.getCarBrandID(), "布加迪");
                                    }
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        carListDB.closeDB();
                    }


                    Intent intent = new Intent(LoginActivity.this, DvrMainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.scale, R.anim.scale2);
                    finish();
                } else {
                    new ResponseErrorProcesser(LoginActivity.this, i.getIntExtra("error_code", -1));
                }
            }

        });
    }
}


