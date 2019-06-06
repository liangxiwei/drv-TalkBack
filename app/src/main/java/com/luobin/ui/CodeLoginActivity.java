package com.luobin.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.permission.PermissionUtil;
import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.ConnectionBroadcast;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyBroadcastReceiver;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.CarRegisterProcesser;
import com.example.jrd48.service.protocol.root.LoginProcesser;
import com.example.jrd48.service.protocol.root.ScanCarOkProcesser;
import com.luobin.dvr.R;
import com.luobin.utils.ZXingUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/8/22.
 */

public class CodeLoginActivity extends BaseActivity implements PermissionUtil.PermissionCallBack{
    private final static String URL = "http://irobbing.com/download/pocdemo/pocdemo.apk";
    private ImageView code;
    private ImageView mInputZxingLoginHint;
    private ImageView mButtonInputZxingLogin;
    private boolean showZXingImage = false;
    private LinearLayout mInputLoginLinearLayout;
    private RelativeLayout mZXingLoginRelativeLayout;
    private EditText mInputLoginPhone;
    private EditText mInputLoginPassword;
    private Button mInputLoginButton;
    private View codeLayout;
    private Context context;
    private TextView noCode;
    private TextView toHint;
    private TextView deviceId;
    private long lastTitleClickTime;
    private long clickCount;
    private ConnectionBroadcast connectionBroadcast;
    protected PermissionUtil mPermissionUtil;
    private RemindDialog remindDialog;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CarRegisterProcesser.ACTION) || intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                showCodeImage();
            } else if(intent.getAction().equals(ScanCarOkProcesser.ACTION)){
                noCode.setText("正在登录");
                noCode.setVisibility(View.VISIBLE);
                code.setVisibility(View.GONE);
                toHint.setVisibility(View.GONE);
                deviceId.setVisibility(View.GONE);
            } else if (intent.getAction().equals(LoginProcesser.ACTION)) {
                if (intent.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {

                  /*  DBManagerCarList carListDB = null;
                    try {
                         carListDB = new DBManagerCarList(CodeLoginActivity.this);
                        if (!carListDB.getHasCarBrand()) {
                            new CheckAndUpdateCarTypeThread((MyApplication) getApplication()).start();
                        }
                    }finally {
                        if (carListDB != null) {
                            carListDB.closeDB();
                        }
                    }*/
                    ToastR.setToast(CodeLoginActivity.this, "登录成功");
                    Intent i = new Intent(CodeLoginActivity.this, DvrMainActivity.class);
                    //i.putExtra("login",true);
                    startActivity(i);
                    overridePendingTransition(R.anim.scale, R.anim.scale2);
                    finish();
                } else {
                    ToastR.setToast(context, "登录失败");
                    new ResponseErrorProcesser(context, intent.getIntExtra("error_code", -1));
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = 1.0f;
        getWindow().setAttributes(lp);
        setContentView(R.layout.login_code);
        context = this;
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
//        toolbar.setNavigationIcon(R.drawable.btn_back);//设置Navigatiicon 图标
        toolbar.setTitle(null);
        ((TextView)toolbar.findViewById(R.id.custom_title)).setText("视频对讲");
        clickCount = 0;
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SystemClock.elapsedRealtime() - lastTitleClickTime < 800){
                    clickCount++;
                    if(clickCount > 7){
                        showClearDialog();
                        clickCount = 0;
                    }
                } else {
                    clickCount = 0;
                }
                lastTitleClickTime = SystemClock.elapsedRealtime();
            }
        });
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });


        noCode = (TextView) findViewById(R.id.no_register_text);
        code = (ImageView) findViewById(R.id.code_image);
        mInputZxingLoginHint = (ImageView) findViewById(R.id.input_zxing_login_hint);
        mInputZxingLoginHint.setOnClickListener(mLoginClickListener);
        mButtonInputZxingLogin = (ImageView) findViewById(R.id.button_input_zxing_login);
        mButtonInputZxingLogin.setOnClickListener(mLoginClickListener);
        mInputLoginLinearLayout = (LinearLayout) findViewById(R.id.input_login_layout);
        mZXingLoginRelativeLayout = (RelativeLayout) findViewById(R.id.zxing_login_layout);
        mInputLoginPhone = (EditText) findViewById(R.id.input_login_phone);
        mInputLoginPassword = (EditText) findViewById(R.id.input_login_password);
        mInputLoginButton = (Button) findViewById(R.id.input_login_button);
        mInputLoginButton.setOnClickListener(mLoginClickListener);
        codeLayout = findViewById(R.id.code_image_layout);
        toHint = (TextView) findViewById(R.id.to_hint);
        deviceId = (TextView) findViewById(R.id.device_id);
        remindDialog = new RemindDialog(context);
        remindDialog.setMessage("正在获取设备号...");
        remindDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
                    finish();
                }
                return false;
            }
        });
        //showCodeImage();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CarRegisterProcesser.ACTION);
        intentFilter.addAction(LoginProcesser.ACTION);
        intentFilter.addAction(ScanCarOkProcesser.ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, intentFilter);

        connectionBroadcast = new ConnectionBroadcast(this);
        connectionBroadcast.setReceiver(new MyBroadcastReceiver() {
            @Override
            protected void onReceiveParam(String str) {
                showCodeImage();
            }
        });
        connectionBroadcast.start();

        requestAllPermisson();
    }

    private void requestAllPermisson() {
        mPermissionUtil = PermissionUtil.getInstance();
        if (mPermissionUtil.checkPermissions(this)) {
            List<String> list = mPermissionUtil.findDeniedPermissions(this,mPermissionUtil.getPermissionsList());
            if(list.size() > 0) {
                mPermissionUtil.requestPermission(this, PermissionUtil.MY_PERMISSIONS_CHECK_ALL, this, list.get(0),list.get(0));
            }
        }
    }

    @Override
    protected void onDestroy() {
        if(remindDialog != null && remindDialog.isShowing()){
            remindDialog.dismiss();
        }
        unregisterReceiver(receiver);
        connectionBroadcast.stop();
        super.onDestroy();
    }

    public void showCodeImage() {
        ProtoMessage.CarRegister carRegister = GlobalStatus.getCarRegister();
        Log.v("wsDvr","carRegister:" + (carRegister == null));
        if (carRegister == null) {
            code.setVisibility(View.GONE);
            noCode.setVisibility(View.VISIBLE);
            if (ConnUtil.isConnected(this)) {
                String imei = ConnUtil.getDeviceId(context);
                if(TextUtils.isEmpty(imei) || imei.length() < 10){
                    noCode.setVisibility(View.GONE);
                    remindDialog.show();
                } else {
                    noCode.setText("正在连接服务器");
                    if(remindDialog.isShowing()){
                        remindDialog.dismiss();
                    }
                }
            } else {
                noCode.setText("请检查网络连接");
                if(remindDialog.isShowing()){
                    remindDialog.dismiss();
                }
            }
        } else {
            codeLayout.post(new Runnable() {
                @Override
                public void run() {
                    ProtoMessage.CarRegister carRegister = GlobalStatus.getCarRegister();
//                    Rect location = new Rect();
//                    codeLayout.getLocalVisibleRect(location);
//                    int size = Math.min(location.width(),location.height());
                    if(carRegister != null) {
                        deviceId.setText("设备ID:" + carRegister.getCarID());
                        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.icon_helper);
                        Bitmap bitmap = ZXingUtils.createQRImage("http://poc.erobbing.com/CarRegister?CARID=[" + carRegister.getTempToken() + "]",
                                getResources().getIntArray(R.array.default_zxing_size)[0],
                                getResources().getIntArray(R.array.default_zxing_size)[1],
                                logo);
                        code.setImageBitmap(bitmap);
                        if(remindDialog.isShowing()){
                            remindDialog.dismiss();
                        }
                        noCode.setVisibility(View.GONE);
                        code.setVisibility(View.VISIBLE);
                    } else {
                        code.setVisibility(View.GONE);
                        noCode.setVisibility(View.VISIBLE);
                        String imei = ConnUtil.getDeviceId(context);
                        if(TextUtils.isEmpty(imei) || imei.length() < 10){
                            noCode.setVisibility(View.GONE);
                            remindDialog.show();
                        } else {
                            noCode.setText("正在连接服务器");
                            if(remindDialog.isShowing()){
                                remindDialog.dismiss();
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mPermissionUtil.requestResult(this, permissions, grantResults, this, PermissionUtil.TYPE);
    }

    @Override
    public void onPermissionSuccess(String type) {
        requestAllPermisson();
    }

    @Override
    public void onPermissionReject(String strMessage) {
        ToastR.setToastLong(CodeLoginActivity.this, "该权限已经拒绝，请到手机管家或者系统设置里授权");
    }

    @Override
    public void onPermissionFail(String failType) {
        ToastR.setToast(CodeLoginActivity.this, "权限设置失败");
    }

    public void showClearDialog(){
        String oldImei = (String) SharedPreferencesUtils.get(context,"custom_imei","");

        if(!TextUtils.isEmpty(oldImei)) {
            ToastR.setToast(CodeLoginActivity.this, "已清理本地保存的imei");
            SharedPreferencesUtils.put(context,"custom_imei","");
        }
    }

    private View.OnClickListener mLoginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.input_login_button:
                    if (TextUtils.isEmpty(mInputLoginPhone.getText()) || TextUtils.isEmpty(mInputLoginPassword.getText())) {
                        ToastR.setToast(CodeLoginActivity.this, getResources().getString(R.string.toast_input_login_phone_pwd_empty));
                    } else if (!isMobileNum(mInputLoginPhone.getText().toString())) {
                        ToastR.setToast(CodeLoginActivity.this, getResources().getString(R.string.toast_input_login_phone_incompatible));
                    } else if (mInputLoginPassword.getText().length() < 4) {
                        ToastR.setToast(CodeLoginActivity.this, getResources().getString(R.string.toast_input_login_pwd_invalid));
                    } else {
                        ProtoMessage.UserLogin.Builder builder = ProtoMessage.UserLogin.newBuilder();
                        builder.setPhoneNum(mInputLoginPhone.getText().toString());
                        builder.setPassword(mInputLoginPassword.getText().toString());
                        builder.setAppType(ProtoMessage.AppType.appCar_VALUE);
                        MyService.start(CodeLoginActivity.this, ProtoMessage.Cmd.cmdLogin.getNumber(), builder.build());

                        IntentFilter filter = new IntentFilter();
                        filter.addAction(LoginProcesser.ACTION);
                        new TimeoutBroadcast(CodeLoginActivity.this, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
                            @Override
                            public void onTimeout() {
                                MyService.restart(CodeLoginActivity.this);
                                ToastR.setToast(CodeLoginActivity.this, getResources().getString(R.string.toast_input_login_timeout));
                            }

                            @Override
                            public void onGot(Intent i) {
                                if (i.getIntExtra("error_code", -1) == ProtoMessage.ErrorCode.OK.getNumber()) {
                                    ToastR.setToast(CodeLoginActivity.this, getResources().getString(R.string.toast_input_login_succeed));

                                    SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("token", i.getStringExtra("token"));
                                    editor.putString("phone", mInputLoginPhone.getText().toString());
                                    editor.commit();

                                    /*Intent intent = new Intent(CodeLoginActivity.this, DvrMainActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.scale, R.anim.scale2);
                                    finish();*/
                                } else {
                                    ToastR.setToast(CodeLoginActivity.this, getResources().getString(R.string.toast_input_login_failed));
                                    new ResponseErrorProcesser(CodeLoginActivity.this, i.getIntExtra("error_code", -1));
                                }
                            }
                        });
                    }
                    break;
                case R.id.button_input_zxing_login:
                    hideKeyboard();
                    if (showZXingImage) {
                        mInputZxingLoginHint.setImageDrawable(getResources().getDrawable(R.drawable.zxing_login_hint));
                        mButtonInputZxingLogin.setImageDrawable(getResources().getDrawable(R.drawable.button_zxing_login));
                        mZXingLoginRelativeLayout.setVisibility(View.GONE);
                        mInputLoginLinearLayout.setVisibility(View.VISIBLE);
                        toHint.setText(getResources().getString(R.string.textview_inout_login_hint));
                        showZXingImage = false;
                    } else {
                        mInputZxingLoginHint.setImageDrawable(getResources().getDrawable(R.drawable.input_login_hint));
                        mButtonInputZxingLogin.setImageDrawable(getResources().getDrawable(R.drawable.button_input_login));
                        mZXingLoginRelativeLayout.setVisibility(View.VISIBLE);
                        mInputLoginLinearLayout.setVisibility(View.GONE);
                        toHint.setText(getResources().getString(R.string.textview_zxing_login_hint));
                        showZXingImage = true;
                    }
                    break;
            }
        }
    };

    /**
     * check mobile number
     *
     * @param mobile
     * @return
     */
    public static boolean isMobileNum(String mobile) {
        String regex = "^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(17[0,3,5-8])|(18[0-9])|166|198|199|(147))\\d{8}$";
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(mobile);
        return m.matches();
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(CodeLoginActivity.this.getWindow().getDecorView().getWindowToken(), 0);
    }
}
