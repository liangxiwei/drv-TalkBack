package com.luobin.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.jrd48.chat.crash.MyApplication;
import com.luobin.dvr.R;

/**
 * Created by Administrator on 2017/8/24.
 */

public class MaxHeightListView extends ListView {
    private int maxHeight = MyApplication.getContext().getResources().getDimensionPixelOffset(R.dimen.notify_layout_height);
    public MaxHeightListView(Context context) {
        super(context);
    }

    public MaxHeightListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MaxHeightListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        setViewHeightBasedOnChildren();
    }


    public void setViewHeightBasedOnChildren() {
        ListAdapter listAdapter = this.getAdapter();
        if (listAdapter == null) {
            return;
        }
        // int h = 10;
        // int itemHeight = BdUtilHelper.getDimens(getContext(), R.dimen.ds30);
        int sumHeight = 0;
        int size = listAdapter.getCount();


        for (int i = 0; i < size; i++) {
            View v = listAdapter.getView(i, null, this);
            v.measure(0, 0);
            sumHeight += v.getMeasuredHeight();
        }


        if (sumHeight > maxHeight) {
            sumHeight = maxHeight;
        }
        android.view.ViewGroup.LayoutParams params = this.getLayoutParams();
        // this.getLayoutParams();
        params.height = sumHeight;


        this.setLayoutParams(params);
    }


    public int getMaxHeight() {
        return maxHeight;
    }


    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }
}
