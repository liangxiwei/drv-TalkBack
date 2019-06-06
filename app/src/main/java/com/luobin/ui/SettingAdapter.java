package com.luobin.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.luobin.dvr.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author wangjunjie
 */
public class SettingAdapter extends BaseAdapter {

    private Context context = null;

    private List<String> data = new ArrayList<>();

    public SettingAdapter(Context context, List<String> data) {
        this.data = data;
        this.context = context;

    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public String getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {

        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_setting_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.btnSure.setText(data.get(position));


        return convertView;
    }


    class ViewHolder {
        @BindView(R.id.btnSure)
        Button btnSure;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
