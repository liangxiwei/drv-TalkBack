package com.luobin.ui.settingitem;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.luobin.dvr.R;
import com.luobin.ui.BaseDialogActivity;
import com.luobin.ui.SelectInterestAdapter;
import com.luobin.ui.settingitem.adapter.SetDrawVideoAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingDrawVideoActivity extends BaseDialogActivity {

    @BindView(R.id.imgClose)
    ImageView imgClose;

    @BindView(R.id.rvList)
    RecyclerView rvList;

    @BindView(R.id.btnSure)
    Button btnSure;

    SetDrawVideoAdapter adapter = null;

    ArrayList<String> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_draw_video);
        ButterKnife.bind(this);
        getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);

        initView();
    }


    private void initView() {
        data = new ArrayList<>();
        data.add("0");
        data.add("1");
        data.add("2");
        data.add("3");
        adapter = new SetDrawVideoAdapter(this, data);
//布局管理器对象 参数1.上下文 2.规定一行显示几列的参数常量
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        //设置RecycleView显示的方向是水平还是垂直 GridLayout.HORIZONTAL水平  GridLayout.VERTICAL默认垂直
        gridLayoutManager.setOrientation(GridLayout.VERTICAL);
        //设置布局管理器， 参数gridLayoutManager对象
        rvList.setLayoutManager(gridLayoutManager);
        rvList.setAdapter(adapter);
    }


    @OnClick({R.id.imgClose, R.id.btnSure})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgClose:
                finish();
                break;
            case R.id.btnSure:
                break;
            default:
                break;
        }
    }
}
