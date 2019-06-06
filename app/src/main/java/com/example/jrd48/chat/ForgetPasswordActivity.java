package com.example.jrd48.chat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.jrd48.chat.permission.PermissionUtil;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.SmsProcesser;
import com.luobin.dvr.R;

/**
 * Created by Administrator on 2016/12/2.
 */

public class ForgetPasswordActivity extends BaseActivity implements View.OnClickListener {
    private Button btnCode;
    private Button btnNext;
    private EditText etPhoneNumber;
    private EditText etCode;
    private boolean inputPhoneOk;
    private boolean inputMsgOk;
    private TimeCount time;
    private Handler mHandler = new Handler();
    private ProgressDialog m_pDialog;
    //    protected PermissionUtil mPermissionUtil;
    int MY_PERMISSIONS_REQUEST_READ_SMS = 10042;
    private MessageReceiver messageReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_code_layout);
        Toolbarset();
        initView();
        checkReadSMSPrimisson();
    }
    private void initView() {
        etPhoneNumber = (EditText) findViewById(R.id.et_phonenumber);
        etCode = (EditText) findViewById(R.id.et_code);
        etCode.setEnabled(false);
        btnCode = (Button) findViewById(R.id.btn_code);
        btnCode.setOnClickListener(this);
        btnCode.setClickable(false);
        btnCode.setBackgroundResource(R.drawable.reg0);
        btnNext = (Button) findViewById(R.id.btn_next);
        btnNext.setOnClickListener(this);
        btnNext.setClickable(false);
        btnNext.setBackgroundResource(R.drawable.reg0);
        TextChange textChange = new TextChange();
        etPhoneNumber.addTextChangedListener(textChange);
        etCode.addTextChangedListener(textChange);
        time = new TimeCount(60000, 1000);
        MyDialog(); //创建等待弹窗
    }
    public void  MyDialog(){
        //创建ProgressDialog对象
        m_pDialog = new ProgressDialog(ForgetPasswordActivity.this,R.style.CustomDialog);
        // 设置进度条风格，风格为圆形，旋转的
        m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // 设置ProgressDialog 提示信息
        m_pDialog.setMessage("请稍等...");
        // 设置ProgressDialog 的进度条是否不明确
        m_pDialog.setIndeterminate(false);
        // 设置ProgressDialog 是否可以按退回按键取消
        m_pDialog.setCancelable(false);
    }

    @Override
    public void onBackPressed(){
        finish();
    }

    private void Toolbarset() {
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
//        toolbar.setNavigationIcon(R.drawable.btn_back);//设置Navigatiicon 图标
        toolbar.setTitle(null);
        ((TextView)toolbar.findViewById(R.id.custom_title)).setText("忘记密码");
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_code:
                getSms();
                break;
            case R.id.btn_next:
                toResetPassword();
                break;
        }
    }

    private void checkReadSMSPrimisson() {
//        mPermissionUtil = PermissionUtil.getInstance();
//        if (mPermissionUtil.isOverMarshmallow()) {
//            mPermissionUtil.requestPermission(this, MY_PERMISSIONS_REQUEST_READ_SMS, this, PermissionUtil.PERMISSIONS_READ_SMS, PermissionUtil.PERMISSIONS_READ_SMS);
//        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        mPermissionUtil.requestResult(this, permissions, grantResults, this, PermissionUtil.PERMISSIONS_READ_SMS);
//    }
//
//    @Override
//    public void onPermissionSuccess(String type) {
//    }
//
//    @Override
//    public void onPermissionReject(String strMessage) {
//        ToastR.setToastLong(ForgetPasswordActivity.this, "应用读取短信权限已经拒绝，请到手机管家或者系统设置里授权");
//    }
//
//    @Override
//    public void onPermissionFail(String failType) {
//        ToastR.setToast(ForgetPasswordActivity.this, "应用没有读取短信权限，请授权！");
//    }


    private void getSms() {
        m_pDialog.show();
        etCode.setEnabled(true);
        etCode.setHint("请输入短信验证码");
        ProtoMessage.SmsCodeReq.Builder builder = ProtoMessage.SmsCodeReq.newBuilder();
        builder.setPhoneNum(etPhoneNumber.getText().toString());
        builder.setCodeType(1);
        MyService.start(ForgetPasswordActivity.this, ProtoMessage.Cmd.cmdSmsCode.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(SmsProcesser.ACTION);
        new TimeoutBroadcast(ForgetPasswordActivity.this, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                fail();
                m_pDialog.dismiss();
            }
            @Override
            public void onGot(Intent i) {
                m_pDialog.dismiss();
                if(i.getIntExtra("error_code",-1)==
                        ProtoMessage.ErrorCode.OK.getNumber()){
                    startTime();
                }else {
                    fail();
                    new ResponseErrorProcesser(ForgetPasswordActivity.this, i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void toResetPassword() {
        Intent intent = new Intent(ForgetPasswordActivity.this, ResetPasswordActivity.class);
        intent.putExtra("userPhone", etPhoneNumber.getText().toString().trim());
        intent.putExtra("smsCode", etCode.getText().toString().trim());
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        switch (requestCode){
            case 2:
                if(resultCode == RESULT_OK)
                {
                    if(data.getIntExtra("data",0)==1)
                    {
                        finish();
                    }
                    if(data.getIntExtra("data",0)==0)
                    {
                        etCode.setText("");
                    }
                }
                break;
            default:
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if (messageReceiver != null) {
                unregisterReceiver(messageReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public void startTime() {
        etCode.setText("");
        ToastR.setToast(ForgetPasswordActivity.this, "短信已发送");
        time.start();
    }

    public void fail() {
        btnCode.setText("重新获取验证码");
        btnCode.setBackgroundResource(R.drawable.btn_code);
        btnCode.setClickable(true);
        ToastR.setToast(ForgetPasswordActivity.this, "获取验证码失败，请重新获取");
    }

    class TextChange implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (etCode.length() >= 4) {
                inputMsgOk = true;
            } else {
                inputMsgOk = false;
            }
            if (etPhoneNumber.length() == 11) {
                inputPhoneOk = true;
            } else {
                inputPhoneOk = false;
            }
            if (inputPhoneOk) {
                btnCode.setClickable(true);
                btnCode.setBackgroundResource(R.drawable.btn_code);
                if (inputMsgOk) {
                    btnNext.setClickable(true);
                    btnNext.setBackgroundResource(R.drawable.btn_reg);
                } else {
                    btnNext.setClickable(false);
                    btnNext.setBackgroundResource(R.drawable.reg0);
                }
            } else {
                btnCode.setClickable(false);
                btnCode.setBackgroundResource(R.drawable.reg0);
                if (inputMsgOk) {
                    btnNext.setClickable(true);
                    btnNext.setBackgroundResource(R.drawable.btn_reg);
                } else {
                    btnNext.setClickable(false);
                    btnNext.setBackgroundResource(R.drawable.reg0);
                }
            }

        }
    }

    class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Bundle bundle = intent.getExtras();
            Object[] pdus = (Object[])bundle.get("pdus");
            SmsMessage[] messages = new SmsMessage[pdus.length];
            for(int i = 0; i < messages.length;i++){
                messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
            }
			/*获取短信号码
			String address = messages[0].getOriginatingAddress();
			ToastR.setToast(CodeActivity.this, address);*/
            String fullMessage ="";
            String verify1 = "【MyChat对讲】";
            String verify2 = "您正在进行MyChat对讲注册身份验证";
            for(SmsMessage message : messages){

                fullMessage += message.getMessageBody();
            }
            if(fullMessage.substring(0,10).equals(verify1)){
                etCode.setText(fullMessage.substring(13,17));
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnNext.performClick();
                    }
                }, 300);
//                if (messageReceiver != null) {
//                    unregisterReceiver(messageReceiver);
//                }
            }
            //ToastR.setToast(CodeActivity.this, fullMessage);
        }

    }

    class TimeCount extends CountDownTimer {

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
            messageReceiver = new MessageReceiver();
            registerReceiver(messageReceiver, intentFilter);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            btnCode.setClickable(false);
            btnCode.setBackgroundResource(R.drawable.sendcode2);
            btnCode.setText(millisUntilFinished / 1000 + "秒后可重新发送");
        }

        @Override
        public void onFinish() {
            btnCode.setText("重新获取验证码");
            btnCode.setBackgroundResource(R.drawable.btn_code);
            btnCode.setClickable(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
