package com.luobin.ui.trajectory;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.luobin.dvr.R;
import com.luobin.ui.BaseDialogActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

//TODO 轨迹查询2
public class QueryActivity extends BaseDialogActivity {

    @BindView(R.id.imgClose)
    ImageView imgClose;
    @BindView(R.id.edID)
    EditText edID;
    @BindView(R.id.rlID)
    RelativeLayout rlID;
    @BindView(R.id.edAddress)
    EditText edAddress;
    @BindView(R.id.rlAddress)
    RelativeLayout rlAddress;
    @BindView(R.id.tvQuery)
    TextView tvQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        ButterKnife.bind(this);
        getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
    }

    @OnClick({R.id.imgClose, R.id.edID, R.id.edAddress, R.id.tvQuery})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgClose:
                onBackPressed();
                break;
            case R.id.edID:
                //TODO ID
                break;
            case R.id.edAddress:
                //TODO 地名
                break;
            case R.id.tvQuery:
                //TODO 查询
                break;
        }
    }
}
