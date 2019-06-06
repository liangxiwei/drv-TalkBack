package com.example.jrd48.chat;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsMessage;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.SmsProcesser;
import com.example.jrd48.service.protocol.root.UserRegProcesser;
import com.luobin.dvr.R;

public class CodeActivity extends BaseActivity {
    private Button send;
	private Button registration;
	private TimeCount time;
	private EditText code;
	private MessageReceiver m;
	private Intent intent;
    private ProgressDialog m_pDialog;
    private Handler mHandler = new Handler();
    //    protected PermissionUtil mPermissionUtil;
    int MY_PERMISSIONS_REQUEST_READ_SMS = 10043;
    private Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            m_pDialog.show();
        }
    };


    protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.code_layout);

		Toolbarset();

		time = new TimeCount(60000, 1000);
		send = (Button) findViewById(R.id.btn_code);
		registration = (Button) findViewById(R.id.registration);
		code = (EditText) findViewById(R.id.code);

        //********************************************弹窗设置****************************************************
        //创建ProgressDialog对象
        m_pDialog = new ProgressDialog(CodeActivity.this,R.style.CustomDialog);
        // 设置进度条风格，风格为圆形，旋转的
        m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // 设置ProgressDialog 提示信息
        m_pDialog.setMessage("请稍等...");
        // 设置ProgressDialog 的进度条是否不明确
        m_pDialog.setIndeterminate(false);
        // 设置ProgressDialog 是否可以按退回按键取消
        m_pDialog.setCancelable(false);
        //********************************************弹窗设置****************************************************

        intent = getIntent();
		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

                m_pDialog.show();

				ProtoMessage.SmsCodeReq.Builder builder= ProtoMessage.SmsCodeReq.newBuilder();
				builder.setPhoneNum(intent.getStringExtra("textPhonenumber"));
				builder.setCodeType(0);
				MyService.start(CodeActivity.this, ProtoMessage.Cmd.cmdSmsCode.getNumber(), builder.build());

                IntentFilter filter = new IntentFilter();
                filter.addAction(SmsProcesser.ACTION);
                new TimeoutBroadcast(CodeActivity.this, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
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
                        }
                    }
                });
			}
		});
		registration.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

                if(code.getText().toString().equals("")){
                    ToastR.setToast(CodeActivity.this, "请输入验证码");
                }else{
                    mHandler.postDelayed(mShowProgress, 500);
                    registration.setClickable(false);
                    registration .setBackgroundResource(R.drawable.reg0);
                    ProtoMessage.UserReg.Builder builder= ProtoMessage.UserReg.newBuilder();
                    builder.setPhoneNum(intent.getStringExtra("textPhonenumber"));
                    builder.setPassword(intent.getStringExtra("textPassword"));
                    builder.setSmsCode(code.getText().toString());
                    MyService.start(CodeActivity.this, ProtoMessage.Cmd.cmdReg.getNumber(), builder.build());

                    IntentFilter filter = new IntentFilter();
                    filter.addAction(UserRegProcesser.ACTION);
                    new TimeoutBroadcast(CodeActivity.this, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
                        @Override
                        public void onTimeout() {
                            mHandler.removeCallbacks(mShowProgress);
                            m_pDialog.dismiss();
                            ToastR.setToast(CodeActivity.this, "连接超时");
                            registration.setBackgroundResource(R.drawable.btn_reg);
                            registration.setClickable(true);
                        }
                        @Override
                        public void onGot(Intent i) {
                            mHandler.removeCallbacks(mShowProgress);
                            m_pDialog.dismiss();
                            if(i.getIntExtra("error_code",-1)==
                                    ProtoMessage.ErrorCode.OK.getNumber()){
                                ToastR.setToast(CodeActivity.this, "注册成功");
                                Intent intent = new Intent();
                                intent.putExtra("data", 1);
                                setResult(RESULT_OK,intent);
                                finish();
                            }else {
//                                ToastR.setToast(CodeActivity.this, "注册失败");
                                registration.setBackgroundResource(R.drawable.btn_reg);
                                registration.setClickable(true);
                                new ResponseErrorProcesser(CodeActivity.this, i.getIntExtra("error_code", -1));
                            }
                        }
                    });
                }
			}
		});

        checkReadSMSPrimisson();
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

    private void Toolbarset() {
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
//        toolbar.setNavigationIcon(R.drawable.btn_back);//设置Navigatiicon 图标
        toolbar.setTitle(null);
        ((TextView)toolbar.findViewById(R.id.custom_title)).setText("获取验证码");
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        int i = 0;
        intent.putExtra("data", i);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void startTime() {
        code.setText("");
        ToastR.setToast(CodeActivity.this, "短信已发送");
        time.start();
    }

    public void fail() {
        send.setText("获取验证码");
        send.setBackgroundResource(R.drawable.btn_code);
        send.setClickable(true);
        ToastR.setToast(CodeActivity.this, "获取验证码失败，请重新获取");
    }

    @Override
    protected void onPause() {

        super.onPause();
    }


    private void checkReadSMSPrimisson() {
//        mPermissionUtil = PermissionUtil.getInstance();
//        if (mPermissionUtil.isOverMarshmallow()) {
//            mPermissionUtil.requestPermission(this, MY_PERMISSIONS_REQUEST_READ_SMS, this, PermissionUtil.PERMISSIONS_READ_SMS, PermissionUtil.TYPE);
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
//        ToastR.setToastLong(CodeActivity.this, "应用读取短信权限已经拒绝，请到手机管家或者系统设置里允许权限");
//    }
//
//    @Override
//    public void onPermissionFail(String failType) {
//        ToastR.setToast(CodeActivity.this, "应用没有读取短信权限，请授权！");
//    }

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
				code.setText(fullMessage.substring(13,17));
                code.setSelection(code.length());
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        registration.performClick();
                    }
                }, 300);
                if (m != null) {
                    unregisterReceiver(m);
                }
            }
			//ToastR.setToast(CodeActivity.this, fullMessage);
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
        	send.setClickable(false);
			send.setBackgroundResource(R.drawable.sendcode2);
        	send.setText(millisUntilFinished / 1000 + "秒后可重新发送");
        }

        @Override
        public void onFinish() {
        	send.setText("重新获取验证码");
			send.setBackgroundResource(R.drawable.btn_code);
        	send.setClickable(true);
        }
    }


}
