package com.example.jrd48.chat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jrd48.chat.permission.PermissionUtil;
import com.example.jrd48.chat.permission.PermissonManager;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.LoginProcesser;
import com.luobin.dvr.R;
import com.luobin.ui.DvrMainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class LoginActivity extends BaseActivity implements PermissionUtil.PermissionCallBack {
	private TextView registration;
	private TextView forgetPassword;
	private Button login;
	private EditText account;
	private EditText password;
    private ProgressDialog m_pDialog;
	protected PermissionUtil mPermissionUtil;
	private Button btnPasswordSee;
	private Boolean passwordVisible = true;
	private static final int PERMISSION_CODE = 99912;
	private Handler mHandler = new Handler();
	List<String> mList;
	List<String> mDeniedPermissionList = new ArrayList<String>();
	List<ImageView> mImageViewList = new ArrayList<ImageView>();
	List<RelativeLayout> mRelativeLayoutList = new ArrayList<RelativeLayout>();
	List<PermissonManager> mPermissonManagerList = new ArrayList<PermissonManager>();

	private Runnable mShowProgress = new Runnable() {
		@Override
		public void run() {
			m_pDialog.show();
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		login = (Button)findViewById(R.id.login);
		registration = (TextView) findViewById(R.id.registration);
		forgetPassword = (TextView) findViewById(R.id.tv_forget_pass_word);
		account = (EditText) findViewById(R.id.account);
		password = (EditText) findViewById(R.id.password);
		btnPasswordSee = (Button) findViewById(R.id.passwordsee);
		btnPasswordSee.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (passwordVisible) {
					//显示密码
					password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
					password.setSelection(password.length());
					btnPasswordSee.setBackgroundResource(R.drawable.passwordsee2);
					passwordVisible = false;
				} else {
					//隐藏密码
					password.setTransformationMethod(PasswordTransformationMethod.getInstance());
					password.setSelection(password.length());
					btnPasswordSee.setBackgroundResource(R.drawable.passwordsee1);
					passwordVisible = true;
				}
			}
		});

		forgetPassword.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
				startActivity(intent);
			}
		});

        //********************************************弹窗设置****************************************************
        //创建ProgressDialog对象
        m_pDialog = new ProgressDialog(LoginActivity.this,R.style.CustomDialog);
        // 设置进度条风格，风格为圆形，旋转的
        m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // 设置ProgressDialog 提示信息
        m_pDialog.setMessage("正在登录,请稍等...");
        // 设置ProgressDialog 的进度条是否不明确
        m_pDialog.setIndeterminate(false);
        // 设置ProgressDialog 是否可以按退回按键取消
        m_pDialog.setCancelable(false);
        //********************************************弹窗设置****************************************************

		registration.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
			startActivity(intent);
			}
		});
		login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!checkPhoneNum()) {
					ToastR.setToastLong(LoginActivity.this, "手机号输入错误（只能是数字，不能含空格）");
					return;
				}
				mHandler.postDelayed(mShowProgress, 500);
				login.setClickable(false);
				ProtoMessage.UserLogin.Builder builder= ProtoMessage.UserLogin.newBuilder();
				builder.setPhoneNum(account.getText().toString());
				builder.setPassword(password.getText().toString());
				MyService.start(LoginActivity.this, ProtoMessage.Cmd.cmdLogin.getNumber(), builder.build());

				IntentFilter filter = new IntentFilter();
				filter.addAction(LoginProcesser.ACTION);
                new TimeoutBroadcast(LoginActivity.this, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
                    @Override
					public void onTimeout() {
						mHandler.removeCallbacks(mShowProgress);
						login.setClickable(true);
                        m_pDialog.dismiss();
						MyService.restart(LoginActivity.this);
						ToastR.setToast(LoginActivity.this, "连接超时,请重新登录");
					}
					@Override
					public void onGot(Intent i) {
						mHandler.removeCallbacks(mShowProgress);
						login.setClickable(true);
						m_pDialog.dismiss();
						if(i.getIntExtra("error_code",-1)==
								ProtoMessage.ErrorCode.OK.getNumber()){
							ToastR.setToast(LoginActivity.this, "登录成功");

                            SharedPreferences preferences=getSharedPreferences("token", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor=preferences.edit();
                            editor.putString("token", i.getStringExtra("token"));
                            editor.putString("phone", account.getText().toString());
                            editor.commit();

							Intent intent = new Intent(LoginActivity.this,DvrMainActivity.class);
							startActivity(intent);
							overridePendingTransition(R.anim.scale, R.anim.scale2);
							finish();
						} else {
							ToastR.setToast(LoginActivity.this, "登录失败");
							new ResponseErrorProcesser(LoginActivity.this, i.getIntExtra("error_code", -1));
						}
					}
				});
			}
		});
		initData();
//		requestAllPermisson();
	}

	private boolean checkPhoneNum() {
		boolean isPhoneNum = false;
		try {
			String regExp = "^((13[0-9])|(15[^4])|(18[0-9])|(17[0-8])|(147,145))\\d{8}$";
			Pattern p = Pattern.compile(regExp);
			Matcher m = p.matcher(account.getText().toString());
			isPhoneNum = m.matches();
		} catch (PatternSyntaxException e) {
			e.printStackTrace();
		}
		return isPhoneNum;
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

	private void dialogShow() {
		new AlertDialog.Builder(LoginActivity.this).setTitle("提示")// 提示框标题
				.setMessage("您尚有权限未允许，可能会影响到后面应用的使用，确认退出？")
				.setPositiveButton("确定", // 提示框的两个按钮
						new android.content.DialogInterface.OnClickListener() {
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
//                else if(i == mDeniedPermissionList.size()){
//                    pm.getmImageView().setImageResource(R.drawable.allow);
//                }
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		mPermissionUtil.requestResult(this, permissions, grantResults, this, PermissionUtil.TYPE);
	}

	@Override
	public void onBackPressed(){
		moveTaskToBack(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}


//	private DialogShowUtils.PermissionDialogCancelListener mCancelListener = new DialogShowUtils.PermissionDialogCancelListener() {
//
//		@Override
//		public void onCancelPressed() {
//			ToastR.setToast(LoginActivity.this,"取消权限设置");
//		}
//	};
//	private DialogShowUtils.PermissionDialogOKListener mOkListener = new DialogShowUtils.PermissionDialogOKListener() {
//		@Override
//		public void onOKPressed() {
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//				if (!Settings.System.canWrite(LoginActivity.this)) {
//					Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//					intent.setData(Uri.parse("package:" + getPackageName()));
//					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					startActivity(intent);
//				}
//			}
//		}
//	};

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
		ToastR.setToast(LoginActivity.this, "权限设置失败");
	}
}
