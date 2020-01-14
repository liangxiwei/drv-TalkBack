package com.example.jrd48.chat.wiget;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.luobin.dvr.R;

/**
 * Created by zhangzhaolei on 2019/11/28.
 */

public class TransparentProgressDialog {

    public static Dialog createLoadingDialog(Context context, String msg) {

        View view = LayoutInflater.from(context).inflate(
                R.layout.trans_progress_dialog, null);
        LinearLayout layout = (LinearLayout) view
                .findViewById(R.id.dialog_view);
        //ImageView img = (ImageView) view.findViewById(R.id.img);
        TextView tipText = (TextView) view.findViewById(R.id.tipTextView);
        tipText.setText(context.getResources().getString(R.string.progress_waiting_to_enter_bbs_title));

        /*Animation animation = AnimationUtils.loadAnimation(context,
                R.anim.dialog_load_animation);
        img.startAnimation(animation);
        tipText.setText(msg);*/

        Dialog loadingDialog = new Dialog(context, R.style.loadingDialog);
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        return loadingDialog;
    }
}
