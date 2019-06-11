package com.luobin.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

public class ScrollListView extends ListView implements AbsListView.OnScrollListener {
    boolean bl_down = true;
    public ScrollListView(Context context) {
        super(context);
        this.setOnScrollListener(this);
    }

    public ScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnScrollListener(this);
    }

    public ScrollListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setOnScrollListener(this);
    }

    //重写这个方法，并且在方法里面请求所有的父控件都不要拦截他的事件
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(!bl_down);
        return super.dispatchTouchEvent(ev);
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(firstVisibleItem == 0) {
            View first_view = view.getChildAt(0);
            if (first_view != null && first_view.getTop() == 0) {
                //已经滚动到顶部了,可以下拉刷新了
                bl_down = true;//
            } else {
                // 未滑动到顶部不让下拉刷新
                bl_down = false;
            }
        }
    }
}
