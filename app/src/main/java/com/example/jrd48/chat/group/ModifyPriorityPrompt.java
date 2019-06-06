package com.example.jrd48.chat.group;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.luobin.dvr.R;
import com.luobin.utils.ButtonUtils;

/**
 * Created by Administrator on 2016/12/27.
 */

public class ModifyPriorityPrompt {



    public void dialogModifyPriorityRequest(Context context, final String title, final int defualtData, final ModifyPrioritytListener func) {

        final AlertDialog dlg = new AlertDialog.Builder(context).create();
        dlg.setCancelable(true);
        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                if(keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        ButtonUtils.changeLeftOrRight(true);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        ButtonUtils.changeLeftOrRight(false);
                        return true;
                    }
                }
                return false;
            }
        });
        dlg.show();
        Window window = dlg.getWindow();
        window.setContentView(R.layout.dialog);
        Button b1 = (Button) window.findViewById(R.id.button1);
        Button b2 = (Button) window.findViewById(R.id.button2);
        TextView tvTitle = (TextView) window.findViewById(R.id.tv_title);
        if (title.length() > 0) {
            tvTitle.setText("话权设置：");
        }
        final NumberPicker np = (NumberPicker) window.findViewById(R.id.numberPicker1);
        np.setMaxValue(15); // max value 100
        np.setMinValue(0);   // min value 0
//        if (defualtData == 0) {
//            np.setValue(1);
//        } else {
        np.setValue(defualtData);
//        }
        np.setWrapSelectorWheel(false);
        np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                func.onOk(np.getValue());
                dlg.cancel();
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.cancel();
            }
        });
    }


}
