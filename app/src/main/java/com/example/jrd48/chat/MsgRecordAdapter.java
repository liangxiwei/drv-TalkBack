package com.example.jrd48.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.SQLite.TeamMemberHelper;
import com.example.jrd48.chat.wiget.PuzzleView;
import com.luobin.dvr.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jrd48 on 2016/11/11.
 */

public class MsgRecordAdapter extends ArrayAdapter<MsgRecord> {
    private int resourceId;
    public MsgRecordAdapter(Context context, int resource, List<MsgRecord> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }
    public View getView(int position,View convertView, ViewGroup parent){
        View view;
        final MsgRecord msg = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.groupName = (TextView) view.findViewById(R.id.group_name);
            viewHolder.recentRecord = (TextView) view.findViewById(R.id.recent_record);
            viewHolder.headImages = (ImageView) view.findViewById(R.id.head_image_s);
            viewHolder.headImageg = (PuzzleView) view.findViewById(R.id.head_image_g);
            viewHolder.recordTime = (TextView) view.findViewById(R.id.record_time);
            viewHolder.callRecord = (Button) view.findViewById(R.id.call_record);
            viewHolder.new_msg = (TextView) view.findViewById(R.id.new_msg);
            viewHolder.background = (LinearLayout) view.findViewById(R.id.background);
            viewHolder.callClick = (LinearLayout) view.findViewById(R.id.call_click);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        String name = null;
        if(msg.getTop() == 0){
            viewHolder.background.setBackground(getContext().getResources().getDrawable(R.drawable.listview_item_change));
        }else{
            viewHolder.background.setBackground(getContext().getResources().getDrawable(R.drawable.listview_item_top_change));
        }
        if(msg.getSg()){
            if (msg.getHeadImage() == null) {
                viewHolder.headImages.setImageResource(R.drawable.default_useravatar);
            } else {
                viewHolder.headImages.setImageBitmap(msg.getHeadImage());
            }
            if (GlobalStatus.equalPhone(msg.getPhone())) {
                viewHolder.callRecord.setBackgroundResource(R.drawable.calling);
            } else {
                viewHolder.callRecord.setBackgroundResource(R.drawable.btn_call);
            }
            viewHolder.headImages.setVisibility(View.VISIBLE);
            viewHolder.headImageg.setVisibility(View.GONE);
        }else{
            String phone;
            TeamMemberHelper teamMemberHelper = new TeamMemberHelper(getContext(), msg.getTeamId() + "TeamMember.dp", null);
            SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
            Cursor cursor = db.query("LinkmanMember", null, null, null, null, null, null);
            int i = 0;
            if (cursor.moveToLast()) {
                Bitmap head;
                ArrayList<Bitmap> mBmps = new ArrayList<Bitmap>();
                do {
                    i++;
                    phone = cursor.getString(cursor.getColumnIndex("user_phone"));
                    if (phone.equals(msg.getPhone())) {
                        name = cursor.getString(cursor.getColumnIndex("nick_name"));
                        if (name == null || name.equals("")) {
                            name = cursor.getString(cursor.getColumnIndex("user_name"));
                        }
                    }
                    head = GlobalImg.getImage(getContext(), phone);
                    if (head != null) {
                        mBmps.add(head);
                    } else {
                        mBmps.add(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.man));
                    }
                    if (i >= 9)
                        break;
                } while (cursor.moveToPrevious());
                try {
                    viewHolder.headImageg.setBackgroundResource(R.color.teamHeadBackColor);
                    viewHolder.headImageg.setImageBitmaps(mBmps);
                } catch (Exception e) {
                    e.printStackTrace();
                    viewHolder.headImageg.setBackgroundResource(R.drawable.group);
                }
            } else {
                viewHolder.headImageg.setBackgroundResource(R.drawable.group);
            }
            SharedPreferences preferences = getContext().getSharedPreferences("token", Context.MODE_PRIVATE);
            String myPhone = preferences.getString("phone", "");
            if (myPhone.equals(msg.getPhone())) {
                name = "我";
            } else {
                if (name == null) {
                    if (cursor.moveToFirst()) {
                        do {
                            phone = cursor.getString(cursor.getColumnIndex("user_phone"));
                            if (phone.equals(msg.getPhone())) {
                                name = cursor.getString(cursor.getColumnIndex("nick_name"));
                                if (name == null || name.equals("")) {
                                    name = cursor.getString(cursor.getColumnIndex("user_name"));
                                }
                                break;
                            }
                        } while (cursor.moveToNext());
                    }
                }
            }


            cursor.close();
            db.close();
            if (GlobalStatus.equalTeamID(msg.getTeamId())) {
                viewHolder.callRecord.setBackgroundResource(R.drawable.calling);
            } else {
                viewHolder.callRecord.setBackgroundResource(R.drawable.btn_call);
            }
            viewHolder.headImageg.setVisibility(View.VISIBLE);
            viewHolder.headImages.setVisibility(View.GONE);
        }
        viewHolder.callClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(),FirstActivity.class);
                intent.putExtra("data", (int)1);
                if (msg.getSg()) {
                    intent.putExtra("linkmanName", msg.getGroupName());
                    intent.putExtra("linkmanPhone", msg.getPhone());
                } else {
                    intent.putExtra("group", msg.getTeamId());
                    intent.putExtra("type", msg.getMemberRole());
                    intent.putExtra("group_name", msg.getGroupName());
                }
                getContext().startActivity(intent);
            }
        });
        if(msg.getNew_msg()==0){
            viewHolder.new_msg.setVisibility(View.GONE);
        }else{
            viewHolder.new_msg.setVisibility(View.VISIBLE);
        }
        viewHolder.new_msg.setText(msg.getNew_msg()+"");
        viewHolder.groupName.setText(msg.getGroupName());
        if (name == null) {
            //Log.i("chatjrd", "name: null");
            viewHolder.recentRecord.setText(msg.getRecentRecord());
        } else {
            if (name.equals("")) {
                //Log.i("chatjrd", "name:无");
                viewHolder.recentRecord.setText(msg.getRecentRecord());
            } else {
                //Log.i("chatjrd", "name:" + name);
                viewHolder.recentRecord.setText(name + "：" + msg.getRecentRecord());
            }
        }

        viewHolder.recordTime.setText(msg.getRecordTime());
        return view;
    }
    class ViewHolder{
        TextView groupName;
        TextView new_msg;
        TextView recentRecord;
        ImageView headImages;
        PuzzleView headImageg;
        TextView recordTime;
        Button callRecord;
        LinearLayout background;
        LinearLayout callClick;
    }
}
