package com.luobin.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.luobin.dvr.R;

import java.util.List;

public class ContactsMemberAdapter extends BaseAdapter {

    List<String> list ;
    Context context;

    public ContactsMemberAdapter(List<String> list, Context context) {
        this.list = list;
        this.context = context;
    }


    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder ;
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.adapter_member,null);
            holder = new ViewHolder();
            holder.memberIcon = (ImageView) view.findViewById(R.id.memberIcon);
            holder.memberName = (TextView) view.findViewById(R.id.memberName);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }

       /* holder.memberName.setText(list.get(position));
        holder.memberIcon.setBackgroundResource(R.drawable.icon);*/

        return view;
    }



    class ViewHolder{
        TextView memberName;
        ImageView memberIcon;

    }

}
