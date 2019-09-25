package com.luobin.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.ViewFriendsMsg;
import com.luobin.dvr.R;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import java.util.Map;
import java.util.HashMap;
import android.widget.CompoundButton;


public class SelectMemberAdapter extends BaseAdapter {

    List<ViewFriendsMsg> list ;
    Context context;

	private Map<Integer, Boolean> map = new HashMap<>();

    public SelectMemberAdapter(List<AppliedFriends> appliedFriends, Context context) {
        if (list == null){
            list = new ArrayList<>();
        }else{
            list.clear();
        }

        for (AppliedFriends appliedFriend : appliedFriends){
            ViewFriendsMsg viewFriendsMsg = new ViewFriendsMsg();
            viewFriendsMsg.friends = appliedFriend;
            list.add(viewFriendsMsg);
        }
        this.context = context;
    }

    public void setData(List<AppliedFriends> appliedFriends){
        if (list == null){
            list = new ArrayList<>();
        }else{
            list.clear();
        }
        for (AppliedFriends appliedFriend : appliedFriends){
            ViewFriendsMsg viewFriendsMsg = new ViewFriendsMsg();
            viewFriendsMsg.friends = appliedFriend;
            list.add(viewFriendsMsg);
        }
    }

    public List<AppliedFriends>  getSelect(){
        List<AppliedFriends> appliedFriends = new ArrayList<>();
        for (ViewFriendsMsg viewFriendsMsg : list){
            if (viewFriendsMsg.isbChecked()){
                appliedFriends.add(viewFriendsMsg.friends);
            }
        }

        return appliedFriends;
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
    public View getView(final int position, View view, ViewGroup viewGroup) {
        final ViewHolder holder ;
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.adapter_select_member,null);
            holder = new ViewHolder();
            holder.memberIcon = (ImageView) view.findViewById(R.id.memberIcon);
            holder.memberName = (TextView) view.findViewById(R.id.memberName);
            holder.checkBox = (CheckBox) view.findViewById(R.id.checkbox);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        AppliedFriends memberInfo = list.get(position).friends;
        holder.memberName.setText(memberInfo.getUserName());
        Bitmap bitmap = GlobalImg.getImage(context, memberInfo.getPhoneNum());
        holder.memberIcon.setImageBitmap(bitmap);
		//rs modified for select wrong�� LBCJW-157
		/* 
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewFriendsMsg viewFriendsMsg = list.get(position);
                viewFriendsMsg.setbChecked(!viewFriendsMsg.isbChecked());
            }
        });*/
        
		holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							if (isChecked == true) {
								map.put(position, true);
							} else {
								map.remove(position);
							}
							
							ViewFriendsMsg viewFriendsMsg = list.get(position);
							if (map != null && map.containsKey(position)) {
								//holder.checkBox.setChecked(true);
								viewFriendsMsg.setbChecked(true);
								Log.d("rs", "selectMemberAdapter->set true->position:"+position);
							} else {
								//holder.checkBox.setChecked(false);
								viewFriendsMsg.setbChecked(false);
								Log.d("rs", "selectMemberAdapter->set false->position:"+position);
							}
						}
					});
		//end
		
        return view;
    }



    class ViewHolder{
        TextView memberName,memberRole;
        ImageView memberIcon;
        CheckBox checkBox;

    }



}
