package com.luobin.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StyleRes;
import android.view.View;
import android.view.WindowManager;


public abstract class BaseDialog extends Dialog {
    Context context;
    // 设置布局
    public abstract View initView(Context context);

    public BaseDialog(Context context) {
        super(context);
        this.context = context;
    }

    public BaseDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
    }

    public BaseDialog(Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       View view = initView(context);
       setContentView(view);
    }

    /**
     * 设置宽和高
     * @param width
     * @param height
     */
    public void setSize(float width,float height){
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        params.width =(int)width;
        params.height = (int)height;
        this.getWindow().setAttributes(params);
    }

    /**
     *  点击空白不消失
     */
    protected void unClickDissmiss(){
        this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
    }

}
