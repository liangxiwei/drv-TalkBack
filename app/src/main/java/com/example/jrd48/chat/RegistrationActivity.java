package com.example.jrd48.chat;




import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.luobin.dvr.R;


public class RegistrationActivity extends BaseActivity{
	private Button btnPasswordSee;
	private Button btnRegistration;
	private EditText textPhonenumber;
	private EditText textPassword;
	private Boolean passwordVisible = true;
	private Boolean phoneNumberOk = false;
	private Boolean passwordOk = false;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration_layout);
		Toolbarset();

		btnPasswordSee = (Button) findViewById(R.id.passwordsee);
		textPassword = (EditText) findViewById(R.id.password);
		
		textPhonenumber = (EditText) findViewById(R.id.phonenumber);
		btnRegistration = (Button) findViewById(R.id.registration);
		
		
		btnRegistration.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(textPhonenumber.length()==11){
					if (textPassword.length() >= 4) {
						Intent intent = new Intent(RegistrationActivity.this, CodeActivity.class);
						intent.putExtra("textPhonenumber", textPhonenumber.getText().toString());
						intent.putExtra("textPassword", textPassword.getText().toString());
						startActivityForResult(intent, 1);
					}else{
						ToastR.setToastLong(RegistrationActivity.this, "请设置密码为大于等于4位的数字或者字符");
					}
				}else{
					ToastR.setToastLong(RegistrationActivity.this, "请输入正确的手机号码");
				}
			}
		});
		btnRegistration.setClickable(false);
		textPhonenumber.addTextChangedListener(new TextWatcher(){
			
			@Override
			public void afterTextChanged(Editable s) {
				if(textPhonenumber.length()>=11){
					phoneNumberOk =true;
				}else{
					phoneNumberOk =false;
				}
				if(passwordOk && phoneNumberOk){
					btnRegistration.setClickable(true);
					btnRegistration.setBackgroundResource(R.drawable.btn_reg);
				}else{
					btnRegistration.setClickable(false);
					btnRegistration.setBackgroundResource(R.drawable.reg0);
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}});
		textPassword.addTextChangedListener(new TextWatcher(){
		
		@Override
		public void afterTextChanged(Editable s) {
			if(textPassword.length()>=4){
				passwordOk =true;
			}else{
				passwordOk =false;
			}
			if(passwordOk && phoneNumberOk){
				btnRegistration.setClickable(true);
				btnRegistration.setBackgroundResource(R.drawable.btn_reg);
			}else{
				btnRegistration.setClickable(false);
				btnRegistration.setBackgroundResource(R.drawable.reg0);
			}
		}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}});

		btnPasswordSee.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(passwordVisible){
                    //显示密码
					textPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
					textPassword.setSelection(textPassword.length());
					btnPasswordSee.setBackgroundResource(R.drawable.passwordsee2);
					passwordVisible = false;
                }else{
                    //隐藏密码
                	textPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
					textPassword.setSelection(textPassword.length());
					btnPasswordSee.setBackgroundResource(R.drawable.passwordsee1);
                	passwordVisible = true;
                }
			}
			});
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		switch (requestCode){
			case 1:
				if(resultCode == RESULT_OK)
				{
					if(data.getIntExtra("data",0)==1)
					{
						finish();
					}
				}
				break;
			default:
		}
	}

	private void Toolbarset(){
		Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
//        toolbar.setNavigationIcon(R.drawable.btn_back);//设置Navigatiicon 图标
		toolbar.setTitle(null);
		((TextView)toolbar.findViewById(R.id.custom_title)).setText("注册");
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
	}
	
}
