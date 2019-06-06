package com.luobin.dvr.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.luobin.dvr.R;
import com.luobin.dvr.model.SettingItem;

import java.util.List;

public class SettingsLeftAdapter extends BaseAdapter {

    private Context context;
    private int currentPosition;
    private List<SettingItem> mList = null;

    public SettingsLeftAdapter(Context context, List<SettingItem> list) {
        super();
        this.context = context;
        mList = list;
    }

    public void setSelect(int position) {
        currentPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHold vh = null;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.settings_item_left, null);
            vh = new ViewHold();
            convertView.setTag(vh);
            vh.name = (TextView) convertView.findViewById(R.id.tv_left_name);
            vh.value = (TextView) convertView.findViewById(R.id.tv_left_value);
            vh.iv_select = (ImageView) convertView.findViewById(R.id.iv_select);
        } else {
            vh = (ViewHold) convertView.getTag();
        }
        if (position == currentPosition) {
            vh.name.setTextColor(context.getResources().getColor(R.color.settings_select_text));
            vh.value.setTextColor(context.getResources().getColor(R.color.settings_select_text));
            vh.iv_select.setVisibility(View.VISIBLE);
        } else {
            vh.name.setTextColor(Color.WHITE);
            vh.value.setTextColor(Color.WHITE);
            vh.iv_select.setVisibility(View.INVISIBLE);
        }
        vh.name.setText(mList.get(position).getName());
        vh.value.setText(mList.get(position).getCurValue());
        return convertView;
    }

    public class ViewHold {
        TextView name;
        TextView value;
        ImageView iv_select;
    }
}