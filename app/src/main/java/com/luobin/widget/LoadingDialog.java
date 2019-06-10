package com.luobin.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.luobin.dvr.R;

public class LoadingDialog extends BaseDialog {

    protected LoadingDialog(Context context) {
        super(context);

    }

    protected LoadingDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public LoadingDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    public View initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading,null);
        this.setCanceledOnTouchOutside(false);
        return view;
    }

}
