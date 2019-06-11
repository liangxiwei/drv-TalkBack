package com.luobin.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import com.luobin.dvr.R;


public class NoBGStatusBar extends FrameLayout {

    public NoBGStatusBar(Context context) {
        super(context);
        initView(context);
    }

    public NoBGStatusBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public NoBGStatusBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public NoBGStatusBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(final Context context){
        View view = LayoutInflater.from(context).inflate(R.layout.no_bg_status_bar,null);
        this.addView(view);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName("com.erobbing.gallery","com.erobbing.gallery.activity.WifiDialogActivity");
                context.startActivity(intent);
            }
        });
    }

}
