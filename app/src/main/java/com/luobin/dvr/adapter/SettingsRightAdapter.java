package com.luobin.dvr.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.luobin.dvr.R;

import java.util.List;

public class SettingsRightAdapter extends BaseAdapter {
    private Context context;
    private List<String> mList = null;
    private int selectIndex = 0;

    public SettingsRightAdapter(Context context, List<String> list) {
        super();
        this.context = context;
        mList = list;
    }

    public void setSelect(int paramInt) {
        this.selectIndex = paramInt;
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
            convertView = View.inflate(context, R.layout.settings_item_right, null);
            vh = new ViewHold();
            convertView.setTag(vh);
            vh.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
            vh.iv_select = (ImageView) convertView.findViewById(R.id.iv_select);
        } else {
            vh = (ViewHold) convertView.getTag();
        }
        vh.tv_content.setText(mList.get(position));
        if (position == selectIndex) {
            vh.tv_content.setTextColor(context.getResources().getColor(R.color.settings_select_text));
            vh.iv_select.setVisibility(View.VISIBLE);
        } else {
            vh.tv_content.setTextColor(Color.WHITE);
            vh.iv_select.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    public class ViewHold {
        TextView tv_content;
        ImageView iv_select;
    }

}