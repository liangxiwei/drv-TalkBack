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

public class CloudTrajectoryAdapter extends BaseAdapter {
    ArrayList<String> list = new ArrayList<>();
    private Context context = null;

    private int NumActivity = 0;


    public CloudTrajectoryAdapter(Context context, ArrayList<String> list, int NumActivity) {
        this.list = list;
        this.context = context;
        this.NumActivity = NumActivity;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_cloud_tra, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (NumActivity == 0) {

        } else if (NumActivity == 1) {

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
        @BindView(R.id.cbItem)
        CheckBox cbItem;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
