package com.luobin.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.TeamMemberInfo;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.luobin.dvr.R;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import java.util.ArrayList;
import java.util.List;

public class ContactsMemberAdapter extends BaseAdapter {

    List<TeamMemberInfo> list ;
    List<AppliedFriends> listFriendMembers;
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

    public List<TeamMemberInfo> getData(){
        return list;
    }

    public void setAppliedFriends(List<AppliedFriends> list) {
        listFriendMembers = list;
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
            holder.memberRole = (TextView) view.findViewById(R.id.memberRole);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }

        TeamMemberInfo memberInfo = list.get(position);
        String name = null;
        if (listFriendMembers != null && listFriendMembers.size() > 0) {
            for (AppliedFriends appliedFriends : listFriendMembers) {
                if (appliedFriends.getPhoneNum().equals(memberInfo.getUserPhone())) {
                    name = appliedFriends.getNickName();
                    break;
                }
            }
        }
        if (TextUtils.isEmpty(name)) {
            if (!TextUtils.isEmpty(memberInfo.getNickName())) {
                holder.memberName.setText(memberInfo.getNickName());
            } else {
                holder.memberName.setText(memberInfo.getUserName());
            }
        } else {
            holder.memberName.setText(name);
        }

        Bitmap bitmap = GlobalImg.getImage(context, memberInfo.getUserPhone());
        holder.memberIcon.setImageBitmap(bitmap);

       /* if (memberInfo.isSelect()){
            view.setBackgroundResource(R.drawable.bg_select_stroke);
        }else{
            view.setBackgroundResource(R.drawable.bg_no_select_stroke);
        }*/

        if (memberInfo.getRole() == ProtoMessage.TeamRole.Owner_VALUE) {
            holder.memberRole.setText( "群主 (" + memberInfo.getMemberPriority() + ")");
        } else if (memberInfo.getRole() == ProtoMessage.TeamRole.Manager_VALUE) {
            holder.memberRole.setText( "管理员 (" + memberInfo.getMemberPriority() + ")");
        } else {
            holder.memberRole.setText( "群成员 (" + memberInfo.getMemberPriority() + ")");
        }
        return view;
    }

    class ViewHolder{
        TextView memberName,memberRole;
        ImageView memberIcon;

    }
}
