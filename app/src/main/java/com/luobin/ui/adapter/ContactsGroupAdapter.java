package com.luobin.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jrd48.chat.Team;
import com.luobin.dvr.R;

import java.util.List;

public class ContactsGroupAdapter extends BaseAdapter {

    List<Team> list ;
    Context context;

    public ContactsGroupAdapter(List<Team> list, Context context) {
        this.list = list;
        this.context = context;
    }


    public void seteData(  List<Team> list ){
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
            view = LayoutInflater.from(context).inflate(R.layout.adapter_group,null);
            holder = new ViewHolder();
            holder.groupIcon = (LinearLayout) view.findViewById(R.id.groutIcon);
            holder.groupName = (TextView) view.findViewById(R.id.groupName);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }

        holder.groupName.setText(list.get(position).getLinkmanName());
        initIcon(holder.groupIcon);

        return view;
    }

    private void initIcon(LinearLayout groupIcon){
        groupIcon.removeAllViews();
        for (int i = 0; i < 6; i++){
            ImageView icon = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.rightMargin = 20;
            icon.setLayoutParams(params);
          //  icon.setBackgroundResource(R.drawable.icon);
            groupIcon.addView(icon);
        }
    }

    class ViewHolder{
        TextView groupName;
        LinearLayout groupIcon;

    }

}
