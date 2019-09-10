package com.luobin.ui;

import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jrd48.chat.ToastR;
import com.luobin.dvr.R;
import com.luobin.widget.BaseDialog;

import android.text.InputFilter;



public class InputTextDialog extends BaseDialog {

    TextView tvTitleName;
    TextView passwordCancel, passwordSure;
    EditText editPassword;
    GetPasswordListener sureLisitener;

    String titleName = "";


    private Type type;

	private int inputLengthLimit = 0;


    public enum Type {
        //文本
        TEXT,
        //数字
        NUMBER,
        //密码
        PASS,
    }


    public InputTextDialog(Context context, String titleName, Type type,
                           GetPasswordListener sureLisitener) {
        super(context);
        this.sureLisitener = sureLisitener;
        this.titleName = titleName;
        this.type = type;
    }

	//rs added for length limit
	public InputTextDialog(Context context, String titleName, Type type, int limit,
                           GetPasswordListener sureLisitener) {
        super(context);
        this.sureLisitener = sureLisitener;
        this.titleName = titleName;
        this.type = type;
		this.inputLengthLimit = limit;
    }

	//end

    protected InputTextDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    protected InputTextDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    public View initView(final Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_password, null);
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        setSize(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        unClickDissmiss();

        tvTitleName = (TextView) view.findViewById(R.id.tvTitleName);
        editPassword = (EditText) view.findViewById(R.id.edit_password);
        passwordCancel = (TextView) view.findViewById(R.id.password_cancel);


        //文本类型
        if (type == Type.PASS) {
            editPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else if (type == Type.NUMBER) {
            editPassword.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else if (type == Type.TEXT) {
            editPassword.setInputType(InputType.TYPE_CLASS_TEXT);
        }
		
       //rs added for limit lenth
		if(inputLengthLimit > 0){    		
			editPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(inputLengthLimit)});
		}
		//end
		
        editPassword.setHint("请输入" + titleName);

        passwordCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        tvTitleName.setText("设置"+titleName);

        passwordSure = (TextView) view.findViewById(R.id.password_sure);
        passwordSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = editPassword.getText().toString();
                if (!TextUtils.isEmpty(password)) {
                    if (sureLisitener != null) {
                        sureLisitener.getPassword(password);
                    }
                    dismiss();
                } else {

                    ToastR.setToast(context, "请输入" + titleName);

                }


            }
        });
        return view;
    }

    public interface GetPasswordListener {
        void getPassword(String password);
    }


}