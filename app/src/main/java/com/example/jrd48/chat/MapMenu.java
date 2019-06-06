package com.example.jrd48.chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.luobin.dvr.R;

/**
 * popupWindow
 *
 * @author wwj
 */
public class MapMenu extends PopupWindow {
    private View conentView;
    boolean isMenuShow = false;

    public MapMenu(final Activity context, OnClickListener itemsOnClick, boolean isTrack) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        conentView = inflater.inflate(R.layout.map_menu, null);

        Point outSize = new Point();
        context.getWindowManager().getDefaultDisplay().getSize(outSize);
        int h = outSize.y;
        int w = outSize.x;

        this.setContentView(conentView);
        this.setWidth(w / 5 * 2);
        this.setHeight(LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setOutsideTouchable(true);


        this.update();
        ColorDrawable dw = new ColorDrawable(Color.WHITE);
        this.setBackgroundDrawable(dw);
        this.setAnimationStyle(R.style.AnimationPreview);


        LinearLayout memberListLayout = (LinearLayout) conentView
                .findViewById(R.id.layout_member_list);
        LinearLayout myTrackLayout = (LinearLayout) conentView
                .findViewById(R.id.layout_clear_track);
        LinearLayout groupManagementLayout = (LinearLayout) conentView
                .findViewById(R.id.layout_group_management);
        LinearLayout exitLayout = (LinearLayout) conentView
                .findViewById(R.id.layout_exit);
        TextView tvShap = (TextView) conentView.findViewById(R.id.tv_shap);

        //TODO 判断是否显示取消跟踪
      /*  if (isTrack){
            myTrackLayout.setVisibility(View.VISIBLE);
            tvShap.setVisibility(View.VISIBLE);
        } else {
            myTrackLayout.setVisibility(View.GONE);
            tvShap.setVisibility(View.GONE);
        }*/


        memberListLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                MapMenu.this.dismiss();
            }
        });

        memberListLayout.setFocusableInTouchMode(true);
        memberListLayout.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MENU) {
                    if (isMenuShow) {
                        MapMenu.this.dismiss();
                    }
                }
                return false;
            }
        });
        groupManagementLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                MapMenu.this.dismiss();
            }
        });
        exitLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                MapMenu.this.dismiss();
            }
        });

        myTrackLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                MapMenu.this.dismiss();
            }
        });
        memberListLayout.setOnClickListener(itemsOnClick);
        myTrackLayout.setOnClickListener(itemsOnClick);
        groupManagementLayout.setOnClickListener(itemsOnClick);
        exitLayout.setOnClickListener(itemsOnClick);

    }

    /**
     * popupWindow
     *
     * @param parent
     */
    public void showPopupWindow(View parent, Context context) {
        if (!this.isShowing()) {
            this.isMenuShow = true;
            this.showAsDropDown(parent, -ViewUtil.dip2px(context, 50), ViewUtil.dip2px(context, 4));
        } else {
            this.dismiss();
        }
    }

}
