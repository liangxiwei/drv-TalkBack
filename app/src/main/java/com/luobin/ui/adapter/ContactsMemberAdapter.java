package com.luobin.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.TeamMemberInfo;
import com.luobin.dvr.R;

import java.util.ArrayList;
import java.util.List;

public class ContactsMemberAdapter extends BaseAdapter {

    List<TeamMemberInfo> list ;
    Context context;

    public ContactsMemberAdapter(List<TeamMemberInfo> list, Context context) {
        if (list == null){
            list = new ArrayList<>();
        }
        this.list = list;
        this.context = context;
    }

    public void setData(List<TeamMemberInfo> list){
        if (list == null){
            list = new ArrayList<>();
        }
        this.list = list;
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

        holder.memberName.setText(list.get(position).getUserName());
        Bitmap bitmap = GlobalImg.getImage(context, list.get(position).getUserPhone());
        holder.memberIcon.setImageBitmap(bitmap);

        return view;
    }



    class ViewHolder{
        TextView memberName;
        ImageView memberIcon;

    }

}
