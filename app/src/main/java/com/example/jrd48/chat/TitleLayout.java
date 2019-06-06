package com.example.jrd48.chat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.luobin.dvr.R;

public class TitleLayout extends LinearLayout{
	public TitleLayout(Context context,AttributeSet attrs){
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.title, this);
		Button TBack = (Button) findViewById(R.id.title_back);
		TBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((FirstActivity) getContext()).onBackPressed();
				//((Activity) getContext()).finish();
			}
		});
	}
}
