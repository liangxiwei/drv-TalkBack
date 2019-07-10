package com.luobin.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.luobin.dvr.R;

public class FirstStatusBar extends FrameLayout {

    public FirstStatusBar(Context context) {
        super(context);
        initView(context);
    }

    public FirstStatusBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public FirstStatusBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public FirstStatusBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(final Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.status_bar, null);
        this.addView(view);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName("com.erobbing.gallery", "com.erobbing.gallery.activity.WifiDialogActivity");
                context.startActivity(intent);
            }
        });
    }

}
