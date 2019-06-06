package com.luobin.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.ChangePasswordProcesser;
import com.example.jrd48.service.protocol.root.SmsProcesser;
import com.example.jrd48.service.protocol.root.UserRegProcesser;
import com.hp.hpl.sparta.Text;
import com.luobin.dvr.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author wangjunjie
 */
public class RegisterAndForGetActivity extends BaseActivity {


    @BindView(R.id.tvTitle)
    TextView tvTitle;

    @BindView(R.id.edPhone)
    EditText edPhone;

    @BindView(R.id.edCode)
    EditText edCode;

    @BindView(R.id.edPass)
    EditText edPass;

    @BindView(R.id.btnSure)
    Button btnSure;


    @BindView(R.id.tvSendCode)
    TextView tvSendCode;


    private ProgressDialog dialog;
    private Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            dialog.show();
        }
    };


    private TimeCount time;
    private MessageReceiver m;
    private Handler mHandler = new Handler();


    String phone = "";
    String code = "";
    String pass = "";


    /**
     * 注册
     */
    public static final String REGISTER = "register";

    /**
     * 忘记
     */
    public static final String FORGET = "forget";

    /**
     * 注册or忘记
     */
    boolean regOrFor = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        String typeClass = getIntent().getStringExtra("type");

        if (REGISTER.equals(typeClass)) {
            tvTitle.setText("注册账号");
            regOrFor = true;
        } else if (FORGET.equals(typeClass)) {
            tvTitle.setText("忘记密码");
            regOrFor = false;
        }


        time = new TimeCount(60000, 1000);
        initView();
    }

    private void initView() {

        //********************************************弹窗设置****************************************************
        //创建ProgressDialog对象
        dialog = new ProgressDialog(this, R.style.CustomDialog);
        // 设置进度条风格，风格为圆形，旋转的
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // 设置ProgressDialog 提示信息
        dialog.setMessage("请稍等...");
        // 设置ProgressDialog 的进度条是否不明确
        dialog.setIndeterminate(false);
        // 设置ProgressDialog 是否可以按退回按键取消
        dialog.setCancelable(false);
        //********************************************弹窗设置****************************************************

    }

    /**
     * 发送验证码
     */
    @OnClick(R.id.tvSendCode)
    public void onViewClicked() {
        phone = edPhone.getText().toString();

        if (phone.length() < 1) {
            ToastR.setToast(this, "请输入手机号！");
            return;
        } else if (phone.length() < 11) {
            ToastR.setToast(this, "请输入正确的手机号！");
            return;
        }

        if (regOrFor) {
            dialog.show();

            ProtoMessage.SmsCodeReq.Builder builder = ProtoMessage.SmsCodeReq.newBuilder();
            builder.setPhoneNum(phone);
            builder.setCodeType(ProtoMessage.SmsCodeType.smsCodeReg_VALUE);
            // 确保Socket已经启动
            MyService.start(RegisterAndForGetActivity.this);
            MyService.start(RegisterAndForGetActivity.this, ProtoMessage.Cmd.cmdSmsCode.getNumber(), builder.build());

            IntentFilter filter = new IntentFilter();
            filter.addAction(SmsProcesser.ACTION);
            new TimeoutBroadcast(RegisterAndForGetActivity.this, filter, getBroadcastManager()).startReceiver(BaseActivity.REQUEST_TIME_OUT, new ITimeoutBroadcast() {
                @Override
                public void onTimeout() {
                    ToastR.setToast(RegisterAndForGetActivity.this, "获取验证码超时");
                    dialog.dismiss();
                }

                @Override
                public void onGot(Intent i) {
                    dialog.dismiss();
                    int code = i.getIntExtra("error_code", -1);
                    if (code == ProtoMessage.ErrorCode.OK.getNumber()) {
                        startTime();
                    } else {
                        fail(code);
                    }
                }
            });
        } else {

            //TODO 忘记密码

            mHandler.postDelayed(mDelayProgressRunnable, 1000);

            tvSendCode.setEnabled(true);
            edCode.setHint("请输入短信验证码");
            ProtoMessage.SmsCodeReq.Builder builder = ProtoMessage.SmsCodeReq.newBuilder();
            builder.setPhoneNum(phone);
            builder.setCodeType(ProtoMessage.SmsCodeType.smsCodeReset_VALUE);

            // 确保Socket已经启动
            MyService.start(RegisterAndForGetActivity.this);

            MyService.start(RegisterAndForGetActivity.this, ProtoMessage.Cmd.cmdSmsCode.getNumber(), builder.build());

            IntentFilter filter = new IntentFilter();
            filter.addAction(SmsProcesser.ACTION);
            new TimeoutBroadcast(RegisterAndForGetActivity.this, filter, getBroadcastManager()).startReceiver(BaseActivity.REQUEST_TIME_OUT, new ITimeoutBroadcast() {
                @Override
                public void onTimeout() {

                    tvSendCode.setText("获取验证码");
                    tvSendCode.setClickable(true);

                    ToastR.setToast(RegisterAndForGetActivity.this, "获取验证码超时");
                    mHandler.removeCallbacks(mDelayProgressRunnable);
                    try {
                        dialog.dismiss();
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onGot(Intent i) {
                    mHandler.removeCallbacks(mDelayProgressRunnable);
                    try {
                        dialog.dismiss();
                    } catch (Exception e) {
                    }

                    int code = i.getIntExtra("error_code", -1);
                    if (code == ProtoMessage.ErrorCode.OK.getNumber()) {
                        startTime();
                    } else {
                        fail(code);
                        switch (code) {
                            case ProtoMessage.ErrorCode.PHONE_NOT_FOUND_VALUE:
                                ToastR.setToast(RegisterAndForGetActivity.this, "该手机号尚未注册");
                                break;
                            default:
                                new ResponseErrorProcesser(RegisterAndForGetActivity.this, code);
                        }
                    }
                }
            });


        }
    }


    private Runnable mDelayProgressRunnable = new Runnable() {
        @Override
        public void run() {
            dialog.show();
        }
    };


    public void startTime() {
        edCode.setText("");
        ToastR.setToast(RegisterAndForGetActivity.this, "短信已发送");
        time.start();
    }

    public void fail(int code) {
        tvSendCode.setText("获取验证码");
        tvSendCode.setClickable(true);
        new ResponseErrorProcesser(RegisterAndForGetActivity.this, code);
    }


    /**
     * 注册账号
     */
    private void register() {


        mHandler.postDelayed(mShowProgress, 500);
        btnSure.setClickable(false);
        ProtoMessage.UserReg.Builder builder = ProtoMessage.UserReg.newBuilder();
        builder.setPhoneNum(phone);
        builder.setPassword(pass);
        builder.setSmsCode(code);
        MyService.start(RegisterAndForGetActivity.this, ProtoMessage.Cmd.cmdReg.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(UserRegProcesser.ACTION);
        new TimeoutBroadcast(RegisterAndForGetActivity.this, filter, getBroadcastManager()).startReceiver(BaseActivity.REQUEST_TIME_OUT, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                mHandler.removeCallbacks(mShowProgress);
                dialog.dismiss();
                ToastR.setToast(RegisterAndForGetActivity.this, "连接超时");
                btnSure.setClickable(true);
            }

            @Override
            public void onGot(Intent i) {
                mHandler.removeCallbacks(mShowProgress);
                dialog.dismiss();
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(RegisterAndForGetActivity.this, "注册成功");
                    finish();
                } else {
//                                ToastR.setToast(RegisterCodeActivity.this, "注册失败");
//                    btnSure.setBackgroundResource(R.drawable.btn_reg);
                    btnSure.setClickable(true);
                    new ResponseErrorProcesser(RegisterAndForGetActivity.this, i.getIntExtra("error_code", -1));
                }
            }
        });

    }

    /**
     * 忘记密码
     */
    private void forget() {
        checkData();

        checkCode();
    }

    /**
     * 检查数据
     *
     * @return
     */
    private boolean checkData() {

        phone = edPhone.getText().toString();
        code = edCode.getText().toString();
        pass = edPass.getText().toString();
        if (phone.length() < 1) {
            ToastR.setToast(this, "请输入手机号！");
            return false;
        } else if (phone.length() < 11) {
            ToastR.setToast(this, "请输入正确的手机号！");
            return false;
        } else if (code.length() < 1) {
            ToastR.setToastLong(this, "请输入验证码");
            return false;
        } else if (pass.length() < 4) {
            ToastR.setToastLong(this, "请设置密码为大于等于4位的数字或者字符");
            return false;
        }

        return true;
    }

    @OnClick(R.id.btnSure)
    public void btnSureClick() {

        if (regOrFor) {
            checkData();
            register();
        } else {
            forget();
        }

    }

    class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Bundle bundle = intent.getExtras();
            Object[] pdus = (Object[]) bundle.get("pdus");
            SmsMessage[] messages = new SmsMessage[pdus.length];
            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }

            /*获取短信号码
            String address = messages[0].getOriginatingAddress();
			ToastR.setToast(RegisterCodeActivity.this, address);*/

            String fullMessage = "";
            String verify1 = "【MyChat对讲】";
            String verify2 = "您正在进行MyChat对讲注册身份验证";
            for (SmsMessage message : messages) {

                fullMessage += message.getMessageBody();
            }
            if (fullMessage.substring(0, 10).equals(verify1)) {
                edCode.setText(fullMessage.substring(13, 17));
                edCode.setSelection(edCode.length());
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnSure.performClick();
                    }
                }, 300);
                if (m != null) {
                    unregisterReceiver(m);
                }
            }
        }
    }


    class TimeCount extends CountDownTimer {

        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
            m = new MessageReceiver();
            registerReceiver(m, intentFilter);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            tvSendCode.setClickable(false);
//            tvSendCode.setBackgroundResource(R.drawable.sendcode2);
            tvSendCode.setText(millisUntilFinished / 1000 + "秒后可重新发送");
        }

        @Override
        public void onFinish() {
            tvSendCode.setText("重新获取验证码");
            tvSendCode.setClickable(true);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if (m != null) {
                unregisterReceiver(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }


    /**
     * 检查短信是否过期
     */
    private void checkCode() {

        ProtoMessage.SmsCodeReq.Builder builder = ProtoMessage.SmsCodeReq.newBuilder();
        builder.setPhoneNum(phone);
        builder.setCodeType(ProtoMessage.SmsCodeType.smsCodeCheck_VALUE);
        builder.setCode(code);

        MyService.start(RegisterAndForGetActivity.this, ProtoMessage.Cmd.cmdSmsCode.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(SmsProcesser.ACTION);
        new TimeoutBroadcast(RegisterAndForGetActivity.this, filter, getBroadcastManager()).startReceiver(BaseActivity.REQUEST_TIME_OUT, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(RegisterAndForGetActivity.this, "检查验证码超时");
            }

            @Override
            public void onGot(Intent i) {
                int code = i.getIntExtra("error_code", -1);
                switch (code) {
                    case ProtoMessage.ErrorCode.OK_VALUE:
                        //TODO 短信验证通过
                        forgetNewPass();
                        break;
                    case ProtoMessage.ErrorCode.SMS_CODE_NOT_MATCH_VALUE:
                        ToastR.setToastLong(RegisterAndForGetActivity.this, "输入的短信验证码不匹配");
                        break;
                    default:
                        new ResponseErrorProcesser(RegisterAndForGetActivity.this, code);
                        break;
                }
            }
        });
    }

    /**
     * 修改新密码
     */

    private void forgetNewPass() {
        Log.i("发送服务器", "发送服务器");
        mHandler.postDelayed(mShowProgress, 500);
        btnSure.setClickable(false);

        ProtoMessage.ChangePasswordBySms.Builder builder = ProtoMessage.ChangePasswordBySms.newBuilder();
        builder.setSmscode(code);
        builder.setPassword(pass);
        // 确保Socket已经启动
        MyService.start(RegisterAndForGetActivity.this);
        MyService.start(RegisterAndForGetActivity.this, ProtoMessage.Cmd.cmdChangePasswordBySms.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(ChangePasswordProcesser.ACTION);
        new TimeoutBroadcast(RegisterAndForGetActivity.this, filter, getBroadcastManager()).startReceiver(BaseActivity.REQUEST_TIME_OUT, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(RegisterAndForGetActivity.this, "连接超时");
                mHandler.removeCallbacks(mShowProgress);
                btnSure.setClickable(true);

                dialog.dismiss();
            }

            @Override
            public void onGot(Intent i) {
                mHandler.removeCallbacks(mShowProgress);
                btnSure.setClickable(true);
                dialog.dismiss();
                int code = i.getIntExtra("error_code", -1);
                Log.d("ttt", "code:" + code);
                if (code == ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(RegisterAndForGetActivity.this, "修改成功");

                    finish();
                } else {
                    new ResponseErrorProcesser(RegisterAndForGetActivity.this, code);
                    ToastR.setToast(RegisterAndForGetActivity.this, "修改失败");

                }
            }
        });

    }


}
