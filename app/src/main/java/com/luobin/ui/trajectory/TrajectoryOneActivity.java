package com.luobin.ui.trajectory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.jrd48.chat.BaseActivity;
import com.luobin.dvr.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

//TODO 轨迹查询1
public class TrajectoryOneActivity extends BaseActivity  {

    @BindView(R.id.btnBack)
    Button btnBack;
    @BindView(R.id.tvRecord)
    TextView tvRecord;
    @BindView(R.id.tvQuery)
    TextView tvQuery;
    @BindView(R.id.tvStorage)
    TextView tvStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trajectory_one);
        ButterKnife.bind(this);


    }


    @OnClick({R.id.btnBack, R.id.tvRecord, R.id.tvQuery, R.id.tvStorage})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.tvRecord:
                //TODO 轨迹记录
                break;
            case R.id.tvQuery:
                //TODO 轨迹查询
                startActivity(new Intent(this,QueryActivity.class));
                break;
            case R.id.tvStorage:
                //TODO 轨迹存储
                break;
        }
    }
}
