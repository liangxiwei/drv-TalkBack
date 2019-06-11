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

public class SettingPhotoActivity extends BaseDialogActivity {

    @BindView(R.id.imgClose)
    ImageView imgClose;
    @BindView(R.id.rlAddSet)
    RelativeLayout rlAddSet;
    @BindView(R.id.tvNumSet)
    TextView tvNumSet;
    @BindView(R.id.rlReduceSet)
    RelativeLayout rlReduceSet;
    @BindView(R.id.tvtext)
    TextView tvtext;
    @BindView(R.id.rlAddTime)
    RelativeLayout rlAddTime;
    @BindView(R.id.tvNumTime)
    TextView tvNumTime;
    @BindView(R.id.rlReduceTime)
    RelativeLayout rlReduceTime;
    @BindView(R.id.btnSure)
    Button btnSure;

    String[] dataSet = {"1", "2", "3", "4", "5"};
    ArrayList<String> dataSetList = new ArrayList<>();
    int numSet = 0;


    String[] dataTime = {"1", "2", "3", "4", "5"};
    ArrayList<String> dataListTime = new ArrayList<>();
    int numTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_photo);
        ButterKnife.bind(this);
        getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        dataSetList = new ArrayList<>();
        dataSetList.addAll(Arrays.asList(dataSet));
        numSet = 0;

        dataListTime = new ArrayList<>();
        dataListTime.addAll(Arrays.asList(dataTime));
        numTime = 0;

    }

    @OnClick({R.id.imgClose, R.id.rlAddSet, R.id.rlReduceSet, R.id.rlAddTime, R.id.rlReduceTime, R.id.btnSure})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgClose:
                finish();
                break;
            case R.id.rlAddSet://连拍++
                if (numSet == 5) {
                    return;
                }
                numSet++;
                tvNumSet.setText(numSet + "");
                break;
            case R.id.rlReduceSet://连拍--
                if (numSet == 0) {
                    return;
                }
                numSet--;
                tvNumSet.setText(numSet + "");
                break;
            case R.id.rlAddTime://连拍时间++
                if (numTime == 5) {
                    return;
                }
                numTime++;
                tvNumTime.setText(numTime + "");
                break;
            case R.id.rlReduceTime://连拍时间--
                if (numTime == 0) {
                    return;
                }
                numTime--;
                tvNumTime.setText(numTime + "");
                break;
            case R.id.btnSure:
                break;
            default:
                break;
        }
    }
}
