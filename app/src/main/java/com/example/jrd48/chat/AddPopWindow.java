package com.example.jrd48.chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.example.jrd48.chat.search.SearchActivity;
import com.luobin.dvr.R;

/**
 * popupWindow
 *
 * @author wwj
 */
public class AddPopWindow extends PopupWindow {
    private View conentView;
    boolean isMenuShow = false;

    public AddPopWindow(final Activity context, OnClickListener itemsOnClick) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        conentView = inflater.inflate(R.layout.popup_dialog, null);

        Point outSize = new Point();
        context.getWindowManager().getDefaultDisplay().getSize(outSize);
        int h = outSize.y;
        int w = outSize.x;

        this.setContentView(conentView);
        this.setWidth(w / 2);
        this.setHeight(LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setOutsideTouchable(true);


        this.update();
        ColorDrawable dw = new ColorDrawable(Color.WHITE);
        this.setBackgroundDrawable(dw);
        this.setAnimationStyle(R.style.AnimationPreview);


        LinearLayout addFriendLayout = (LinearLayout) conentView
                .findViewById(R.id.layout_add_friend);
        LinearLayout myTrackLayout = (LinearLayout) conentView
                .findViewById(R.id.layout_create_team);
        LinearLayout groupManagementLayout = (LinearLayout) conentView
                .findViewById(R.id.layout_group_management);
        LinearLayout exitLayout = (LinearLayout) conentView
                .findViewById(R.id.layout_exit);
        addFriendLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                AddPopWindow.this.dismiss();
            }
        });

        addFriendLayout.setFocusableInTouchMode(true);
        addFriendLayout.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MENU) {
                    if (isMenuShow) {
                        AddPopWindow.this.dismiss();
                    }
                }
                return false;
            }
        });
        groupManagementLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                AddPopWindow.this.dismiss();
            }
        });
        exitLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                AddPopWindow.this.dismiss();
            }
        });

        myTrackLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AddPopWindow.this.dismiss();
            }
        });
        addFriendLayout.setOnClickListener(itemsOnClick);
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
            this.showAsDropDown(parent, ViewUtil.dip2px(context, 169), -parent.getHeight() - ViewUtil.dip2px(context, 1));
        } else {
            this.dismiss();
        }
    }

}
