package com.luobin.ui.trajectory;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jrd48.chat.BaseActivity;
import com.luobin.dvr.R;
import com.luobin.ui.trajectory.adapter.CloudTrajectoryAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CloudTrajectoryActivity extends BaseActivity {

    @BindView(R.id.btnBack)
    Button btnBack;
    @BindView(R.id.tvMyShare)
    TextView tvMyShare;
    @BindView(R.id.tvMyDown)
    TextView tvMyDown;
    @BindView(R.id.tvDel)
    TextView tvDel;
    @BindView(R.id.tvMap)
    TextView tvMap;
    @BindView(R.id.tvSend)
    TextView tvSend;
    @BindView(R.id.list)
    ListView list;
    @BindView(R.id.tvSure)
    TextView tvSure;
    @BindView(R.id.my)
    LinearLayout my;
    @BindView(R.id.text)
    RelativeLayout text;
    @BindView(R.id.tvShare)
    TextView tvShare;
    @BindView(R.id.rlSearch)
    RelativeLayout rlSearch;
    @BindView(R.id.line)
    TextView line;


    private CloudTrajectoryAdapter adapter = null;

    ArrayList<String> listData = new ArrayList<>();

    //TODO 云轨级  我的下载和 我的分享

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_trajectory);
        ButterKnife.bind(this);
//TODO 进入时 选择第一个
        //        tvMyDown.setBackground(getResources().getDrawable(R.drawable.bg_panel_setting));
//        tvMyShare.setBackground(getResources().getDrawable(R.drawable.bg_panel_setting));
        shareOrDown(true);


        adapter = new CloudTrajectoryAdapter(this, listData, 0);
        list.setAdapter(adapter);


    }


    private void initView() {

    }

    private void shareOrDown(boolean isShareOrDown) {

        if (isShareOrDown) {
            tvMyShare.setBackground(getResources().getDrawable(R.color.noColor));
            tvMyDown.setBackground(getResources().getDrawable(R.drawable.bg_panel_setting));
            tvShare.setVisibility(View.VISIBLE);
        } else {
            tvMyShare.setBackground(getResources().getDrawable(R.drawable.bg_panel_setting));
            tvMyDown.setBackground(getResources().getDrawable(R.color.noColor));
            tvShare.setVisibility(View.GONE);

        }

    }


    @OnClick({R.id.btnBack, R.id.tvMyShare, R.id.tvMyDown, R.id.tvDel, R.id.tvMap, R.id.tvSend, R.id.tvSure, R.id.rlSearch})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                //TODO  返回
                break;
            case R.id.tvMyShare:
                shareOrDown(true);
                //TODO 我的分享
                break;
            case R.id.tvMyDown:
                shareOrDown(false);
                //TODO 我的下载
                break;
            case R.id.tvDel:
                //TODO 删除
                break;
            case R.id.tvMap:
                //TODO 地图
                break;
            case R.id.tvSend:
                //TODO 发送朋友
                break;
            case R.id.tvSure:
                //TODO 确定
                break;
            case R.id.rlSearch:
                //TODO 轨迹查询
                break;
        }
    }


}
