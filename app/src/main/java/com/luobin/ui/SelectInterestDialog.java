package com.luobin.ui;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;

import com.luobin.dvr.R;
import com.luobin.widget.BaseDialog;

import java.util.ArrayList;
import java.util.List;

public class SelectInterestDialog extends BaseDialog {
    private Context context = null;
    private List<InterestBean> data = new ArrayList<>();
    private SelectInterestAdapter adapter = null;

    public SelectInterestAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(SelectInterestAdapter adapter) {
        this.adapter = adapter;
    }


    public SelectInterestDialog(Context context, List<InterestBean> data) {
        super(context);
        this.context = context;
        this.context = context;
        this.data = data;
        adapter = new SelectInterestAdapter(context, data);

    }

    public SelectInterestDialog(Context context) {
        super(context);
        this.context = context;
    }

    protected SelectInterestDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    protected SelectInterestDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    public View initView(Context context) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_select_interest, null);
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        Window window = this.getWindow();
        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rvList);
        Button btnSure = (Button) view.findViewById(R.id.btnSure);

        //布局管理器对象 参数1.上下文 2.规定一行显示几列的参数常量
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 4);
        //设置RecycleView显示的方向是水平还是垂直 GridLayout.HORIZONTAL水平  GridLayout.VERTICAL默认垂直
        gridLayoutManager.setOrientation(GridLayout.VERTICAL);
        //设置布局管理器， 参数gridLayoutManager对象
        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.setAdapter(adapter);
        btnSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();

            }
        });
        return view;
    }


}
