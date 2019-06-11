package com.luobin.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.jrd48.chat.group.TeamInfo;
import com.luobin.dvr.R;

import java.util.List;

public class BBSAdapter extends BaseAdapter {

    List<TeamInfo> list;
    Context context;

    public BBSAdapter(List<TeamInfo> list, Context context) {
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
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_bbs,null);
            viewHolder.name = (TextView) convertView.findViewById(R.id.bbs_name);
            viewHolder.theme = (TextView) convertView.findViewById(R.id.bbs_theme);
            viewHolder.city = (TextView) convertView.findViewById(R.id.bbs_city);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        TeamInfo teamInfo = list.get(position);
        viewHolder.name.setText(teamInfo.getTeamName());
        viewHolder.theme.setText(teamInfo.getTeamDesc());
        return convertView;
    }

    class ViewHolder{
        TextView name,theme,city;
    }
}
