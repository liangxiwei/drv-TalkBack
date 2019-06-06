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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.luobin.dvr.R;

/**
 * popupWindow
 *
 * @author wwj
 */
public class GroupPopWindow extends PopupWindow {
    private View conentView;
    boolean isMenuShow = false;
    public static String linkMan = "linkMan";
    public static String member = "member";
    public static String manager = "manager";
    public static String owner = "owner";
    public boolean hangup = false;//判断是否呼起
    private String type = "";

    public GroupPopWindow(final Activity context, OnClickListener itemsOnClick, String type, boolean hangup) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        conentView = inflater.inflate(R.layout.chat_popup_dialog, null);
        this.hangup = hangup;
        this.type = type;
        Point outSize = new Point();
        context.getWindowManager().getDefaultDisplay().getSize(outSize);
        int h = outSize.y;
        int w = outSize.x;

        this.setContentView(conentView);
        this.setWidth(w / 2);
        this.setHeight(LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setOutsideTouchable(true);


        ColorDrawable dw = new ColorDrawable(Color.WHITE);
        this.setBackgroundDrawable(dw);
        this.setAnimationStyle(R.style.AnimationPreview);


        LinearLayout layoutModifyTeam = (LinearLayout) conentView
                .findViewById(R.id.layout_modify_team);
        LinearLayout layoutSeeAllMember = (LinearLayout) conentView
                .findViewById(R.id.layout_see_all_member);
        LinearLayout layoutDismissTeam = (LinearLayout) conentView
                .findViewById(R.id.layout_dismiss_team);
        LinearLayout layoutDangUp = (LinearLayout) conentView
                .findViewById(R.id.layout_hang_up);

        ImageView ivModifyTeam = (ImageView) conentView
                .findViewById(R.id.iv_modify_team);
        ImageView ivSeeAllMember = (ImageView) conentView
                .findViewById(R.id.iv_see_all_member);
        ImageView ivDismissTeam = (ImageView) conentView
                .findViewById(R.id.iv_dismiss_team);

        TextView tvModifyTeam = (TextView) conentView
                .findViewById(R.id.tv_modify_team);
        TextView tvSeeAllMember = (TextView) conentView
                .findViewById(R.id.tv_see_all_member);
        TextView tvDismissTeam = (TextView) conentView
                .findViewById(R.id.tv_dismiss_team);
        TextView tvLineOne = (TextView) conentView
                .findViewById(R.id.tv_line_one);
        TextView tvLineTwo = (TextView) conentView
                .findViewById(R.id.tv_line_two);
        TextView tvLineThree = (TextView) conentView
                .findViewById(R.id.tv_line_three);
        if (!hangup) {
            tvLineThree.setVisibility(View.GONE);
            layoutDangUp.setVisibility(View.GONE);
        }
        if (type.equals(linkMan)) {
            layoutModifyTeam.setVisibility(View.GONE);
            layoutSeeAllMember.setVisibility(View.GONE);
            tvLineOne.setVisibility(View.GONE);
            tvLineTwo.setVisibility(View.GONE);
            tvDismissTeam.setText("删除该联系人");
        } else {
            layoutModifyTeam.setVisibility(View.GONE);
            layoutDismissTeam.setVisibility(View.GONE);
            tvLineOne.setVisibility(View.GONE);
            tvLineTwo.setVisibility(View.GONE);
           /* if (type.equals(owner)) {
//                ivModifyTeam.setImageResource(R.drawable.modify);
//                tvModifyTeam.setText("修改群信息");
//                ivDismissTeam.setImageResource(R.drawable.dismiss);
//                tvDismissTeam.setText("解散该群");
            } else if (type.equals(member)) {
                ivModifyTeam.setImageResource(R.drawable.see);
                tvModifyTeam.setText("查看群信息");
                ivDismissTeam.setImageResource(R.drawable.exit);
                tvDismissTeam.setText("退出该群");
            } else {
                ivModifyTeam.setImageResource(R.drawable.modify);
                tvModifyTeam.setText("修改群信息");
                ivDismissTeam.setImageResource(R.drawable.exit);
                tvDismissTeam.setText("退出该群");
            }*/
        }


        layoutModifyTeam.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                GroupPopWindow.this.dismiss();
            }
        });

        layoutModifyTeam.setFocusableInTouchMode(true);
        layoutModifyTeam.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MENU) {
                    if (isMenuShow) {
                        GroupPopWindow.this.dismiss();
                    }
                }
                return false;
            }
        });
        layoutDismissTeam.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                GroupPopWindow.this.dismiss();
            }
        });
        layoutDangUp.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                GroupPopWindow.this.dismiss();
            }
        });

        layoutSeeAllMember.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                GroupPopWindow.this.dismiss();
            }
        });
        layoutModifyTeam.setOnClickListener(itemsOnClick);
        layoutSeeAllMember.setOnClickListener(itemsOnClick);
        layoutDismissTeam.setOnClickListener(itemsOnClick);
        layoutDangUp.setOnClickListener(itemsOnClick);
        this.update();
    }

    /**
     * popupWindow
     *
     * @param parent
     */
    public void showPopupWindow(View parent, Context context) {
        if (!this.isShowing()) {
            this.isMenuShow = true;

            this.showAsDropDown(parent, -ViewUtil.dip2px(context, 154), ViewUtil.dip2px(context, 11));
        } else {
            this.dismiss();
        }
    }

}
