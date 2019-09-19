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
//import com.example.jrd48.chat.friend.AppliedFriends;
//import com.example.jrd48.chat.friend.ViewFriendsMsg;
import com.luobin.dvr.R;

import java.util.ArrayList;
import java.util.List;

import com.example.jrd48.chat.TeamMemberInfo;
//import android.widget.RadioButton;

import android.util.Log;
import java.util.Map;
import java.util.HashMap;
import android.widget.CompoundButton;


public class SelectMemberToDeleteAdapter extends BaseAdapter {

    List<TeamMemberInfo> list ;
    Context context;

	private Map<Integer, Boolean> map = new HashMap<>();
	private boolean onBind;
	private int checkedPosition = -1;

    public SelectMemberToDeleteAdapter(List<TeamMemberInfo> teamMembers, Context context) {
        if (list == null){
            list = new ArrayList<>();
        }else{
            list.clear();
        }

        for (TeamMemberInfo tMember : teamMembers){
            list.add(tMember);

			//Log.d("rs", "SelectMemberToDeleteAdapter->"+tMember.getUserPhone());
        }
        this.context = context;
    }

    public void setData(List<TeamMemberInfo> teamMembers){
        if (list == null){
            list = new ArrayList<>();
        }else{
            list.clear();
        }

		for (TeamMemberInfo tMember : teamMembers){
            list.add(tMember);
        }
    }

    public TeamMemberInfo  getSelect(){
        TeamMemberInfo selectMember = new TeamMemberInfo();
        for (TeamMemberInfo tmember : list){
            if (tmember.isSelect()){
				selectMember = tmember;
				break;
            }
        }

        return selectMember;
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

		//Log.d("rs", "SelectMemberToDeleteAdapter->getView->position:"+position);
        final ViewHolder holder ;
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.adapter_select_member_to_delete,null);
            holder = new ViewHolder();
            holder.memberIcon = (ImageView) view.findViewById(R.id.memberIcon);
            holder.memberName = (TextView) view.findViewById(R.id.memberName);
            holder.checkBox = (CheckBox) view.findViewById(R.id.checkbox);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        TeamMemberInfo memberInfo = list.get(position);
        holder.memberName.setText(memberInfo.getUserName());
        Bitmap bitmap = GlobalImg.getImage(context, memberInfo.getUserPhone());
        holder.memberIcon.setImageBitmap(bitmap);
		/*rs delete
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
					TeamMemberInfo tMemberSelected = list.get(position);
                	tMemberSelected.setSelect(!tMemberSelected.isSelect());
            }
        });
        */

		//rs added 
		holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked == true) {
						map.clear();
						map.put(position, true);
					} else {
						map.remove(position);
					}

					if (!onBind) {
                		notifyDataSetChanged();
            		}
				}
			});

		onBind = true;
		TeamMemberInfo tMemberSelected = list.get(position);
		if (map != null && map.containsKey(position)) {
			holder.checkBox.setChecked(true);
			tMemberSelected.setSelect(true);
			//Log.d("rs", "selectMemberToDeleteAdapter->set true->position:"+position);
		} else {
			holder.checkBox.setChecked(false);
			tMemberSelected.setSelect(false);
			//Log.d("rs", "selectMemberToDeleteAdapter->set false->position:"+position);
		}
		onBind = false;

		//end
        return view;
    }



    class ViewHolder{
        TextView memberName,memberRole;
        ImageView memberIcon;
        CheckBox checkBox;

    }



}
