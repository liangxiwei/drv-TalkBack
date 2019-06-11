package com.luobin.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.luobin.dvr.R;
import com.luobin.ui.ScreenUtils;
import com.luobin.ui.adapter.SexSelectAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class SexSelectDialog extends BaseDialog {
    @BindView(R.id.tvTitleName)
    TextView tvTitleName;
    @BindView(R.id.listView)
    ListView listView;
    SexSelectAdapter adapter;

    List<SexSelectBean> list = new ArrayList<>();

    public SexSelectDialog(Context context) {
        super(context);
    }

    @Override
    public View initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_sex, null);
        setSize(ScreenUtils.Dp2Px(context, 300), LinearLayout.LayoutParams.WRAP_CONTENT);
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        tvTitleName.setText("设置性别");
        String[] sexs = new String[]{"男","女","未设置"};

        list.clear();
        list.add(new SexSelectBean("男",true));
        list.add(new SexSelectBean("女",false));
        list.add(new SexSelectBean("未设置",false));

        adapter = new SexSelectAdapter(list,context);


        return view;
    }


    public class SexSelectBean{
        String name;
        boolean isSelect;

        public SexSelectBean(String name, boolean isSelect) {
            this.name = name;
            this.isSelect = isSelect;
        }

        public boolean isSelect() {
            return isSelect;
        }

        public void setSelect(boolean select) {
            isSelect = select;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
