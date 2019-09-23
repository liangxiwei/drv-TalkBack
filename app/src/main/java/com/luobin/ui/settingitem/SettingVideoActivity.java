package com.luobin.ui.settingitem;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.luobin.dvr.R;
import com.luobin.ui.BaseDialogActivity;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

//rs modied for LBCJW-116
import com.example.jrd48.chat.SharedPreferencesUtils;
import android.util.Log;

import cn.carbswang.android.numberpickerview.library.NumberPickerView;

public class SettingVideoActivity extends BaseDialogActivity {

    @BindView(R.id.imgClose)
    ImageView imgClose;
    @BindView(R.id.btnSure)
    Button btnSure;


    String[] dataNum = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

	private NumberPickerView mVideoTakeDurationPicher = null;

    @BindView(R.id.tvtext)
    TextView tvtext;

	int mVodeoTakeDurationNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_video);
        ButterKnife.bind(this);
        getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);

		mVideoTakeDurationPicher = (NumberPickerView) findViewById(R.id.video_duration_picker);
		mVodeoTakeDurationNum = (int)SharedPreferencesUtils.get(this, SharedPreferencesUtils.TAKE_VIDEO_DURATION, 9);

		mVideoTakeDurationPicher.refreshByNewDisplayedValues(dataNum);
		mVideoTakeDurationPicher.setMaxValue(9);
		mVideoTakeDurationPicher.setMinValue(0);
		mVideoTakeDurationPicher.setValue(mVodeoTakeDurationNum);//默认时常
    }

    @OnClick({R.id.imgClose,R.id.btnSure})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgClose:
                finish();
                break;
            case R.id.btnSure:
				mVodeoTakeDurationNum = mVideoTakeDurationPicher.getValue();
							
				Log.d("rs", "set mVodeoTakeDurationNum:"+mVodeoTakeDurationNum);
				SharedPreferencesUtils.put(this,SharedPreferencesUtils.TAKE_VIDEO_DURATION,mVodeoTakeDurationNum);
				finish();
                break;
            default:
                break;
        }
    }

}
