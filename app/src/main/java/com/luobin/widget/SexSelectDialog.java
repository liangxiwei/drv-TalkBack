package com.luobin.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
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

    SexSelectAdapter adapter;

    List<SexSelectBean> list = new ArrayList<>();

    TextView tvTitleName;
    ListView listView;
    TextView passwordCancel;
    TextView passwordSure;

    int sex =0;

    public void setSexListener(SexListener sexListener) {
        this.sexListener = sexListener;
    }

    SexListener sexListener;

    public SexSelectDialog(Context context) {
        super(context);
    }

    @Override
    public View initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_sex, null);
        setSize(ScreenUtils.Dp2Px(context, 300), LinearLayout.LayoutParams.WRAP_CONTENT);
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        tvTitleName = (TextView) view.findViewById(R.id.tvTitleName);
        listView = (ListView) view.findViewById(R.id.listView);
        passwordCancel = (TextView) view.findViewById(R.id.password_cancel);
        passwordCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        passwordSure = (TextView) view.findViewById(R.id.password_sure);
        passwordSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sexListener != null){
                    sexListener.onSelect(sex);
                }
            }
        });


        tvTitleName.setText("设置性别");

        list.clear();
        list.add(new SexSelectBean("男", true));
        list.add(new SexSelectBean("女", false));
        list.add(new SexSelectBean("未设置", false));
        adapter = new SexSelectAdapter(list, context);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sex =position;
            }
        });
        return view;
    }


    public class SexSelectBean {
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


     public interface SexListener{
        void onSelect(int position);
    }


}
