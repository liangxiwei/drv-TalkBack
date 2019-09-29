package com.luobin.ui.settingitem;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.luobin.dvr.R;
import com.luobin.ui.BaseDialogActivity;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

//rs modifed for LBCJW-116
import com.example.jrd48.chat.SharedPreferencesUtils;
import android.util.Log;

import cn.carbswang.android.numberpickerview.library.NumberPickerView;
import com.example.jrd48.GlobalStatus;

public class SettingPhotoActivity extends BaseDialogActivity {

    @BindView(R.id.imgClose)
    ImageView imgClose;
    @BindView(R.id.btnSure)
    Button btnSure;


	String[] takeCounts = {"1", "2", "3", "4", "5"};
	
	String[] takeInterval = {"1", "2", "3", "4", "5"};

	private NumberPickerView mPicTakeCountPicher = null;
	private NumberPickerView mPicTakeIntervalPicher = null;

	int mPicTakeCountNum;
	int mPicTakeInterval;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_photo);
        ButterKnife.bind(this);
        getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);

		mPicTakeCountPicher = (NumberPickerView) findViewById(R.id.picPicker);
    	mPicTakeIntervalPicher = (NumberPickerView) findViewById(R.id.secPicker);

		//mPicTakeCountNum = (int)SharedPreferencesUtils.get(this, SharedPreferencesUtils.TAKE_PIC_COUNT, 0);
		//mPicTakeInterval = (int)SharedPreferencesUtils.get(this, SharedPreferencesUtils.TAKE_PIC_INTERVAL, 9);//rs set deefault interval is 10s
		mPicTakeCountNum = GlobalStatus.getTakePhotoCount(this);
        mPicTakeInterval = GlobalStatus.getTakePhotoIntervalMs(this)/1000;//convert to second

		Log.d("rs", "get mPicTakeCountNum:"+mPicTakeCountNum+", mPicTakeInterval:"+mPicTakeInterval);

		if(mPicTakeCountNum >5){//error number, set to default
			mPicTakeCountNum = 1;
		}

		if(mPicTakeInterval > 5){//error number, set to default
			mPicTakeInterval = 1;
		}
		
		init();
    }

	private void init(){
     	mPicTakeCountPicher.refreshByNewDisplayedValues(takeCounts);
        mPicTakeCountPicher.setMaxValue(4);
        mPicTakeCountPicher.setMinValue(0);
        mPicTakeCountPicher.setValue(mPicTakeCountNum - 1);//默认时间

		mPicTakeIntervalPicher.refreshByNewDisplayedValues(takeInterval);
        mPicTakeIntervalPicher.setMaxValue(4);
        mPicTakeIntervalPicher.setMinValue(0);
        mPicTakeIntervalPicher.setValue(mPicTakeInterval - 1);//默认时间
	}

	

    @OnClick({R.id.imgClose, R.id.btnSure})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgClose:
                finish();
                break;
            
            case R.id.btnSure:
				mPicTakeCountNum = mPicTakeCountPicher.getValue() + 1; //position is from 0
				mPicTakeInterval = mPicTakeIntervalPicher.getValue() + 1; ////position is from 0
			
				Log.d("rs", "set mPicTakeCountNum:"+mPicTakeCountNum+", mPicTakeInterval:"+mPicTakeInterval);
				//SharedPreferencesUtils.put(this,SharedPreferencesUtils.TAKE_PIC_COUNT,mPicTakeCountNum);
				//SharedPreferencesUtils.put(this,SharedPreferencesUtils.TAKE_PIC_INTERVAL,mPicTakeInterval);
				GlobalStatus.setTakePhotoCount(this, mPicTakeCountNum);

				int interval = mPicTakeInterval * 1000;//convert to ms
                GlobalStatus.setTakePhotoIntervalMs(this, interval);
				finish();
                break;
            default:
                break;
        }
    }
}
