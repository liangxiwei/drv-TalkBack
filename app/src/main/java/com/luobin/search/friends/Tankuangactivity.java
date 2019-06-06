package com.luobin.search.friends;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jrd48.chat.BaseActivity;
import com.luobin.dvr.R;

import cn.carbswang.android.numberpickerview.library.NumberPickerView;

/**
 * 弹出距离弹框
 */
public class Tankuangactivity extends BaseActivity {
    private NumberPickerView kpickers;
    private Button sure;
    private Button cancel;
    private int ks = 0;
    String strs[];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.my_number_picker);

        Window win = this.getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;//设置对话框置顶显示
        win.setAttributes(lp);
        try {

            strs = new String[]{"1公里", "5公里", "10公里", "15公里", "20公里", "25公里", "30公里", "35公里", "40公里", "45公里", "50公里", "55公里", "60公里", "65公里", "70公里", "75公里", "80公里", "85公里", "90公里", "95公里", "100公里"};

            kpickers = (NumberPickerView) findViewById(R.id.kpickers);
            sure = (Button) findViewById(R.id.tv_sure);
            cancel = (Button) findViewById(R.id.btn_cancel);
            kpickers.refreshByNewDisplayedValues(strs);
            kpickers.setMaxValue(20);
            kpickers.setMinValue(0);
            kpickers.setValue(ks);//默认时间
            cancel.setFocusable(true);
//            sure.setFocusable(true);
//            kpickers.setFocusable(true);

            sure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int index = kpickers.getValue();
//                    String str = strs[index];
                    int result = index*5 == 0 ? 1:index*5;
//                    Toast.makeText(Tankuangactivity.this, "选择："+result+"公里", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("data",result);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
