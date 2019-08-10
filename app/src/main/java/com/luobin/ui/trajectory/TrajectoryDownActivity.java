package com.luobin.ui.trajectory;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jrd48.chat.BaseActivity;
import com.luobin.dvr.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


//TODO 轨迹1


public class TrajectoryDownActivity extends BaseActivity {

    @BindView(R.id.btnBack)
    Button btnBack;
    @BindView(R.id.list)
    ListView list;
    @BindView(R.id.tvSure)
    TextView tvSure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trajectory_down);
        ButterKnife.bind(this);
    }


    @OnClick({R.id.btnBack, R.id.tvSure})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                //TODO 返回
                break;
            case R.id.tvSure:
                //TODO 确认
                break;
        }
    }
}
