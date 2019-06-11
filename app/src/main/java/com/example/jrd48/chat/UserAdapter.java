package com.example.jrd48.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.luobin.dvr.R;

import java.util.List;

public class UserAdapter extends ArrayAdapter<User> {

    private int resourceId;

    public UserAdapter(Context context, int textViewResourceId, List<User> objects) {
        super(context, textViewResourceId, objects);
        // TODO Auto-generated constructor stub
        resourceId = textViewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        User user = getItem(position);
        ViewHolder viewHolder;
        AnimationDrawable AniDraw;
        if (convertView == null || convertView.getTag() == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.state = (ImageView) view.findViewById(R.id.userState);
            viewHolder.name = (TextView) view.findViewById(R.id.userName);
            viewHolder.image = (ImageView) view.findViewById(R.id.userImage);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        if (user.getState() == ProtoMessage.ChatStatus.csSpeaking_VALUE) {
            viewHolder.image.setAlpha(1f);
            viewHolder.state.setBackgroundResource(R.drawable.speaking);
            viewHolder.state.setVisibility(View.VISIBLE);
            AniDraw = (AnimationDrawable) viewHolder.state.getBackground();
            AniDraw.start();
            //viewHolder.name.setTextColor(0xff303F9F);
        } else if (user.getState() == ProtoMessage.ChatStatus.csOk_VALUE) {
            viewHolder.image.setAlpha(1f);
            viewHolder.state.setVisibility(View.GONE);
            viewHolder.name.setTextColor(0xffffffff);
        } else if (user.getState() == ProtoMessage.ChatStatus.csNotIn_VALUE) {
            viewHolder.image.setAlpha(0.5f);
            viewHolder.state.setBackgroundResource(R.drawable.off_line);
            viewHolder.state.setVisibility(View.VISIBLE);
            viewHolder.name.setTextColor(0xff888888);
        } else if (user.getState() == ProtoMessage.ChatStatus.csOffline_VALUE) {
            viewHolder.state.setBackgroundResource(R.drawable.unknow);
            viewHolder.state.setVisibility(View.VISIBLE);
            viewHolder.name.setTextColor(0xff888888);
            viewHolder.image.setAlpha(0.5f);
        }
        viewHolder.name.setText(user.getName());
        Bitmap bitmap = GlobalImg.getImage(getContext(), user.getPhone());
        // GlobalImg.getImage(getContext(), user.getPhone());
        if (bitmap == null) {
            ((FirstActivity) getContext()).getUserFace(user.getPhone());
            if (user.getUserSex() == ProtoMessage.Sex.female_VALUE) {
                viewHolder.image.setImageResource(R.drawable.woman);
            } else {
                viewHolder.image.setImageResource(R.drawable.man);
            }
        } else {
            viewHolder.image.setImageBitmap(bitmap);
        }
			/*viewHolder.image.setOnLongClickListener(new View.OnLongClickListener(){
				@Override
				public boolean onLongClick(View view) {
					return false;
				}
			});*/
        return view;

    }

    class ViewHolder {
        ImageView state;
        TextView name;
        ImageView image;
    }

}
