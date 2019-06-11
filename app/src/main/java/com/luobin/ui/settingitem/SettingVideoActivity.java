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

public class SettingVideoActivity extends BaseDialogActivity {

    @BindView(R.id.imgClose)
    ImageView imgClose;
    @BindView(R.id.tvNum)
    TextView tvNum;
    @BindView(R.id.rlReduce)
    RelativeLayout rlReduce;
    @BindView(R.id.rlSet)
    LinearLayout rlSet;
    @BindView(R.id.btnSure)
    Button btnSure;


    String[] dataNum = {"1", "2", "3", "4", "5"};
    ArrayList<String> dataList = new ArrayList<>();


    @BindView(R.id.tvtext)
    TextView tvtext;

    @BindView(R.id.rlAdd)
    RelativeLayout rlAdd;

    int num = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_video);
        ButterKnife.bind(this);
        getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        dataList = new ArrayList<>();
        dataList.addAll(Arrays.asList(dataNum));
        num = 0;

    }

    @OnClick({R.id.imgClose, R.id.rlReduce, R.id.btnSure, R.id.rlAdd})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgClose:
                finish();
                break;
            case R.id.rlReduce://下
                if (num == 0) {
                    return;
                }
                num--;
                tvNum.setText(num + "秒");
                break;
            case R.id.btnSure:

                break;
            case R.id.rlAdd://上
                if (num == 5) {
                    return;
                }
                num++;
                tvNum.setText(num + "秒");
                break;
            default:
                break;
        }
    }

}
