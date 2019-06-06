package com.example.jrd48.chat;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by jrd48 on 2016/12/13.
 */

public class PoskeepListView extends ListView {
    public PoskeepListView(Context context) {
        super(context);
    }
    public PoskeepListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public PoskeepListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        setSelection(getCount());
    }
}
