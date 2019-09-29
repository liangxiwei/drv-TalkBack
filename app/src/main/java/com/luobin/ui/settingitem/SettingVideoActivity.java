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
import com.luobin.dvr.DvrConfig;

public class SettingVideoActivity extends BaseDialogActivity {

    @BindView(R.id.imgClose)
    ImageView imgClose;
    @BindView(R.id.btnSure)
    Button btnSure;


    String[] dataNum = {"10", "15", "30", "60"};

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
		mVodeoTakeDurationNum = DvrConfig.getVideoDuration()/1000;//convert to second
		//(int)SharedPreferencesUtils.get(this, SharedPreferencesUtils.TAKE_VIDEO_DURATION, 9);

		mVideoTakeDurationPicher.refreshByNewDisplayedValues(dataNum);
		mVideoTakeDurationPicher.setMaxValue(3);
		mVideoTakeDurationPicher.setMinValue(0);

		int index = 0;
		for(int i = 0; i<dataNum.length; i++){
			if(mVodeoTakeDurationNum == Integer.parseInt(dataNum[i])){
				index = i;
				break;
			}
		}

		Log.d("rs", "mVodeoTakeDurationNum->"+mVodeoTakeDurationNum+", index:"+index);
		mVideoTakeDurationPicher.setValue(index);//默认时长
    }

    @OnClick({R.id.imgClose,R.id.btnSure})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgClose:
                finish();
                break;
            case R.id.btnSure:
				int selectPosition = mVideoTakeDurationPicher.getValue();
							
				Log.d("rs", "set mVodeoTakeDurationNum:"+selectPosition);
				mVodeoTakeDurationNum = Integer.parseInt(dataNum[selectPosition]);
				//SharedPreferencesUtils.put(this,SharedPreferencesUtils.TAKE_VIDEO_DURATION,mVodeoTakeDurationNum);
				int duration = mVodeoTakeDurationNum * 1000;//convert to ms
                Log.d("rs", "set duration:"+duration);
				DvrConfig.setVideoDuration(duration);
				finish();
                break;
            default:
                break;
        }
    }

}
