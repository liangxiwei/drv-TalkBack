package com.luobin.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import com.luobin.dvr.R;

/**
 * Created by Administrator on 2017/3/30.
 */

public class RemindDialog extends ProgressDialog {
    private String contentText;
    private TextView content;
    public RemindDialog(Context context) {
        super(context);
    }

    public RemindDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(getContext());
    }

    private void init(Context context) {
        setContentView(R.layout.remind_dialog);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);
    }

    @Override
    public void show() {
        super.show();
        content = (TextView) findViewById(R.id.tv_load_dialog);
        if(content != null){
            content.setText(contentText);
        }
    }

    @Override
    public void setMessage(CharSequence message) {
//        super.setMessage(message);
        contentText = message.toString();
    }
}
