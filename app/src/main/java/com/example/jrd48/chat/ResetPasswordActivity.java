package com.example.jrd48.chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.ChangePasswordProcesser;
import com.luobin.dvr.R;

/**
 * Created by Administrator on 2016/12/2.
 */

public class ResetPasswordActivity extends BaseActivity {
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private Button btnSubmit;
    private boolean inputMsgOk;
    private ProgressDialog m_pDialog;
    private String smsCode;
    private String userPhone;
    private Handler mHandler = new Handler();
    private Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            m_pDialog.show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password_layout);
        Toolbarset();
        initView();
        getIntentMsg();
    }

    private void Toolbarset() {
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        //        toolbar.setNavigationIcon(R.drawable.btn_back);//设置Navigatiicon 图标
        toolbar.setTitle(null);
        ((TextView)toolbar.findViewById(R.id.custom_title)).setText("重置密码");
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//              Intent intent = new Intent(ResetPasswordActivity.this, ForgetPasswordActivity.class);
//              startActivity(intent);
//              finish();
//            }
//        });
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent();
        intent.putExtra("data", 0);
        setResult(RESULT_OK,intent);
        finish();
    }

    private void initView() {
        etNewPassword = (EditText)findViewById(R.id.et_new_password);
        etConfirmPassword = (EditText)findViewById(R.id.et_confirm_password);

        TextChange textChange = new TextChange();
        etNewPassword.addTextChangedListener(textChange);
        etConfirmPassword.addTextChangedListener(textChange);

        btnSubmit = (Button)findViewById(R.id.btn_submit);
        btnSubmit.setClickable(false);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkInputMsg();
            }
        });

        //********************************************弹窗设置****************************************************
        //创建ProgressDialog对象
        m_pDialog = new ProgressDialog(ResetPasswordActivity.this,R.style.CustomDialog);
        // 设置进度条风格，风格为圆形，旋转的
        m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // 设置ProgressDialog 提示信息
        m_pDialog.setMessage("请稍等...");
        // 设置ProgressDialog 的进度条是否不明确
        m_pDialog.setIndeterminate(false);
        // 设置ProgressDialog 是否可以按退回按键取消
        m_pDialog.setCancelable(false);
        //********************************************弹窗设置****************************************************
    }

    private void checkInputMsg() {
        String newPassword = etNewPassword.getText().toString().trim();
        String ensure = etConfirmPassword.getText().toString().trim();

            if (newPassword.length() >= 4) {
                if (checkPassword(newPassword, ensure)) {
                    toResetPassword(newPassword);
                }
            }else {
                ToastR.setToastLong(ResetPasswordActivity.this, "请设置密码为大于等于4位的数字或者字符");
            }

    }

    private  void toResetPassword(String newPassword){
        Log.i("发送服务器","发送服务器");
        mHandler.postDelayed(mShowProgress, 500);
        btnSubmit.setClickable(false);
        btnSubmit.setBackgroundResource(R.drawable.reg0);

        ProtoMessage.ChangePasswordBySms.Builder builder= ProtoMessage.ChangePasswordBySms.newBuilder();
        builder.setSmscode(smsCode);
        builder.setPassword(newPassword);
        MyService.start(ResetPasswordActivity.this, ProtoMessage.Cmd.cmdChangePasswordBySms.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(ChangePasswordProcesser.ACTION);
        new TimeoutBroadcast(ResetPasswordActivity.this, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(ResetPasswordActivity.this, "连接超时");
                mHandler.removeCallbacks(mShowProgress);
                btnSubmit.setClickable(true);
                btnSubmit.setBackgroundResource(R.drawable.btn_reg);
                m_pDialog.dismiss();
            }
            @Override
            public void onGot(Intent i) {
                mHandler.removeCallbacks(mShowProgress);
                btnSubmit.setClickable(true);
                btnSubmit.setBackgroundResource(R.drawable.btn_reg);
                m_pDialog.dismiss();
                if(i.getIntExtra("error_code",-1)==
                        ProtoMessage.ErrorCode.OK.getNumber()){
                    ToastR.setToast(ResetPasswordActivity.this, "修改成功");
                    Intent intent = new Intent();
                    intent.putExtra("data", 1);
                    setResult(RESULT_OK,intent);
                    finish();
                }else {
                    new ResponseErrorProcesser(ResetPasswordActivity.this, i.getIntExtra("error_code", -1));
                    ToastR.setToast(ResetPasswordActivity.this, "修改失败");
                    Intent intent = new Intent();
                    intent.putExtra("data", 2);
                    setResult(RESULT_OK,intent);
                    finish();
                }
            }
        });
    }

    private boolean checkPassword(String newPassword, String ensure) {
        // TODO Auto-generated method stub
        if (newPassword.equals(ensure)) {
            return true;
        } else {
            ToastR.setToast(ResetPasswordActivity.this, "提示：两次密码输入不一致，请重新输入");
            return false;
        }
    }
    public void getIntentMsg() {
        Intent intent = getIntent();
        this.smsCode = intent.getStringExtra("smsCode");
        this.userPhone = intent.getStringExtra("userPhone");
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
            if (etNewPassword.length() >= 4 && etConfirmPassword.length() >= 4) {
                inputMsgOk = true;
            } else {
                inputMsgOk = false;
            }
            if (inputMsgOk) {
                btnSubmit.setClickable(true);
                btnSubmit.setBackgroundResource(R.drawable.btn_reg);
            } else {
                btnSubmit.setClickable(false);
                btnSubmit.setBackgroundResource(R.drawable.reg0);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
