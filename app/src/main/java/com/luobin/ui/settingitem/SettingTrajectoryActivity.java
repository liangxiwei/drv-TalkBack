package com.luobin.ui.settingitem;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

public class SettingTrajectoryActivity extends BaseDialogActivity {

    @BindView(R.id.imgClose)
    ImageView imgClose;

    @BindView(R.id.rlAdd)
    RelativeLayout rlAdd;

    @BindView(R.id.tvNum)
    TextView tvNum;

    @BindView(R.id.rlReduce)
    RelativeLayout rlReduce;

    @BindView(R.id.cbSave)
    CheckBox cbSave;

    @BindView(R.id.btnSure)
    Button btnSure;


    String[] dataNum = {"1", "2", "3", "4", "5"};
    ArrayList<String> dataList = new ArrayList<>();
    int num = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_trajectory);
        ButterKnife.bind(this);
        getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);

        dataList = new ArrayList<>();
        dataList.addAll(Arrays.asList(dataNum));
        num = 0;
        initView();
    }


    private void initView() {
        cbSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {


            }
        });
    }

    @OnClick({R.id.imgClose, R.id.rlAdd, R.id.rlReduce, R.id.btnSure})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgClose:
                finish();
                break;
            case R.id.rlAdd://++
                if (num == 5) {
                    return;
                }
                num++;
                tvNum.setText(num + "分钟");
                break;
            case R.id.rlReduce://--
                if (num == 0) {
                    return;
                }
                num--;
                tvNum.setText(num + "分钟");
                break;
            case R.id.btnSure:

                break;
            default:
                break;
        }
    }
}
