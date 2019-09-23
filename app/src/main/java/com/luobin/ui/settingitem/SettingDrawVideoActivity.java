package com.luobin.ui.settingitem;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.example.jrd48.chat.SharedPreferencesUtils;
import com.luobin.dvr.R;
import com.luobin.ui.BaseDialogActivity;
import com.luobin.ui.SelectInterestAdapter;
import com.luobin.ui.settingitem.adapter.SetDrawVideoAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.util.Log;

public class SettingDrawVideoActivity extends BaseDialogActivity {

    @BindView(R.id.imgClose)
    ImageView imgClose;

    @BindView(R.id.rvList)
    RecyclerView rvList;

    @BindView(R.id.btnSure)
    Button btnSure;

    SetDrawVideoAdapter adapter = null;

    ArrayList<String> data = new ArrayList<>();
    String result = "";
    int selectedPosition = 0;
	
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
        data.add("2");
        data.add("3");
        data.add("4");
        data.add("5");
		
		//rs modified for LBCJW-115
		int savedPosition = (int)SharedPreferencesUtils.get(context,"picture_position",0);
		Log.d("rs", "savedPosition:"+savedPosition);
        adapter = new SetDrawVideoAdapter(this, data, savedPosition);
		//end
//布局管理器对象 参数1.上下文 2.规定一行显示几列的参数常量
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        //设置RecycleView显示的方向是水平还是垂直 GridLayout.HORIZONTAL水平  GridLayout.VERTICAL默认垂直
        gridLayoutManager.setOrientation(GridLayout.VERTICAL);
        //设置布局管理器， 参数gridLayoutManager对象
        rvList.setLayoutManager(gridLayoutManager);
        rvList.setAdapter(adapter);
        adapter.setOnItemClickListener(new SetDrawVideoAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(int position, String videoTag) {
                result = videoTag; 
                selectedPosition = position;
            }
        });

    }


    @OnClick({R.id.imgClose, R.id.btnSure})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgClose:
                finish();
                break;
            case R.id.btnSure:
                SharedPreferencesUtils.put(context,"picture",result);
				SharedPreferencesUtils.put(context,"picture_position",(Integer)selectedPosition);
                finish();
                break;
            default:
                break;
        }
    }
}
