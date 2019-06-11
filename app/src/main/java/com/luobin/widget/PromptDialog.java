package com.luobin.widget;

import android.content.Context;
import android.support.annotation.StyleRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.luobin.dvr.R;
import com.luobin.utils.ScreenUtil;


/**
 * Created by Administrator on 2017/8/9.
 */

public class PromptDialog extends BaseDialog {

    TextView title,message,cancel,ok;
    RelativeLayout messageView;

    public PromptDialog(Context context) {
        super(context);
    }

    protected PromptDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    protected PromptDialog(Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    @Override
    public View initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.promptdialog,null);
        setContentView(view);
        setSize(ScreenUtil.getScreenWidth(context)/3*2, ViewGroup.LayoutParams.WRAP_CONTENT); // 设置dialog大小
        unClickDissmiss();
        title = (TextView) view.findViewById(R.id.tv_title);
        message = (TextView) view.findViewById(R.id.message);
        cancel = (TextView) view.findViewById(R.id.cancel);
        ok = (TextView) view.findViewById(R.id.ok);
        messageView = (RelativeLayout) view.findViewById(R.id.rel_messageView);
        return view;
    }

    public void setView(View view){
        messageView.removeAllViews();
        messageView.addView(view);
    }

    public void setTitle(String title){
        this.title.setText(title);
    }

    public void setMessage(String message){
        this.message.setText(message);
    }

    public void setCancelListener(String text, final OnClickListener onClickListener){
        cancel.setText(text);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onClick(PromptDialog.this,1);
            }
        });
    }

    public void setOkListener(String text,final OnClickListener onClickListener){
        ok.setText(text);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onClick(PromptDialog.this,2);
            }
        });
    }



}
