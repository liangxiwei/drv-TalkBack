package com.luobin.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jrd48.chat.CircleImageView;
import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.Team;
import com.example.jrd48.chat.TeamMemberInfo;
import com.luobin.dvr.R;
import com.luobin.ui.ScreenUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContactsGroupAdapter extends BaseAdapter {

    List<Team> list ;
    Context context;
    HashMap<Long,List<TeamMemberInfo>> allMemberList;

    public ContactsGroupAdapter(List<Team> list,HashMap<Long,List<TeamMemberInfo>> allMemberList,Context context) {
        this.list = list;
        this.context = context;
        this.allMemberList = allMemberList;
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
        if (list.get(position).isSelect()){
            view.setBackgroundResource(R.drawable.bg_select_stroke);
        }else{
            view.setBackgroundResource(R.drawable.bg_no_select_stroke);
        }
        holder.groupName.setText(list.get(position).getLinkmanName());

        List<TeamMemberInfo> memberInfos = allMemberList.get(list.get(position).getTeamID());

        initIcon(holder.groupIcon,memberInfos);

        return view;
    }

    private void initIcon(LinearLayout groupIcon,List<TeamMemberInfo> memberInfos){
        groupIcon.removeAllViews();
        if (memberInfos != null){
            int memberSize = memberInfos.size();
            if (memberSize > 8)
                memberSize = 8;
            for (int i = 0; i < memberSize; i++){
                TeamMemberInfo memberInfo = memberInfos.get(i);
                Bitmap bitmap = GlobalImg.getImage(context, memberInfo.getUserPhone());
                CircleImageView  icon = new CircleImageView(context);
                icon.setScaleType(ImageView.ScaleType.FIT_XY);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ScreenUtils.Dp2Px(context,30), ScreenUtils.Dp2Px(context,30));
                params.rightMargin = 20;
                icon.setLayoutParams(params);
                icon.setImageBitmap(bitmap);
                groupIcon.addView(icon);
            }
        }
    }

    class ViewHolder{
        TextView groupName;
        LinearLayout groupIcon;

    }

}
