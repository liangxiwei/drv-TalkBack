package com.luobin.ui.trajectory.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.luobin.dvr.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2019-06-29.
 */

public class TrajectoryDownAdapter extends BaseAdapter {
    ArrayList<String> list = new ArrayList<>();
    private Context context = null;


    public TrajectoryDownAdapter(Context context, ArrayList<String> list) {
        this.list = list;
        this.context = context;
    }


    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_tra_down, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.imgHead)
        ImageView imgHead;
        @BindView(R.id.tvName)
        TextView tvName;
        @BindView(R.id.tvTime)
        TextView tvTime;
        @BindView(R.id.tvContext)
        TextView tvContext;
        @BindView(R.id.tvSize)
        TextView tvSize;
        @BindView(R.id.cbItem)
        CheckBox cbItem;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
