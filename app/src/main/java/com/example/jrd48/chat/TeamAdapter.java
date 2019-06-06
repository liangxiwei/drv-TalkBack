package com.example.jrd48.chat;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.SQLite.TeamMemberHelper;
import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.example.jrd48.chat.wiget.PuzzleView;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.luobin.dvr.R;
import com.luobin.model.CallState;
import com.luobin.ui.VideoOrVoiceDialog;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jrd48
 */

class TeamAdapter extends ArrayAdapter<Team> {
    private int resourceId;
    TeamAdapter(Context context, int resource, List<Team> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }
    @NonNull
    public View getView(int position, View convertView, ViewGroup parent){
        View view;
        final Team msg = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.linkmanImageg = (PuzzleView) view.findViewById(R.id.linkman_image_g);
            viewHolder.role = (ImageView) view.findViewById(R.id.role);
            viewHolder.linkmanName = (TextView) view.findViewById(R.id.linkman_name);
            viewHolder.linkmanDesc = (TextView) view.findViewById(R.id.linkman_desc);
            viewHolder.callLink = (CircleImageView) view.findViewById(R.id.call_link);
            viewHolder.callClick = (LinearLayout) view.findViewById(R.id.call_click);
            viewHolder.background = (LinearLayout) view.findViewById(R.id.ll_bg);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (TeamAdapter.ViewHolder) view.getTag();
        }
        try {
            TeamMemberHelper teamMemberHelper = new TeamMemberHelper(getContext(), msg.getTeamID() + "TeamMember.dp", null);
            SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
            Cursor cursor = db.query("LinkmanMember", null, null, null, null, null, null);
            ArrayList<Bitmap> mBmps = new ArrayList<Bitmap>();
            int i = 0;
            int j = 0;
            if (cursor.moveToLast()) {
                String phone;
                Bitmap head;
                do {
                    if (i >= 9) {
                        break;
                    }
                    phone = cursor.getString(cursor.getColumnIndex("user_phone"));
                    head = GlobalImg.getImage(getContext(), phone);
                    if (head != null) {
                        mBmps.add(head);
                        j++;
                    } else {
                        mBmps.add(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.man));
                    }
                    ++i;
                } while (cursor.moveToPrevious());
                try {
                    viewHolder.linkmanImageg.setBackgroundResource(R.color.teamHeadBackColor);
                    viewHolder.linkmanImageg.setImageBitmaps(mBmps);
                    Bitmap bitmap = viewHolder.linkmanImageg.saveImage(360, 360);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bitmapByte = baos.toByteArray();
                    Bitmap bitmap1 = GlobalImg.getImage(getContext(), "team" + msg.getTeamID());
                    if (bitmap1 == null && (j == cursor.getCount() || j == 9)) {
                        FriendFaceUtill.saveFriendFaceImg(msg.getLinkmanName(), "team" + msg.getTeamID(), bitmapByte, getContext());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    viewHolder.linkmanImageg.setBackgroundResource(R.drawable.group);
                }
            } else {
                mBmps.add(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.nocolor));
                viewHolder.linkmanImageg.setImageBitmaps(mBmps);
                viewHolder.linkmanImageg.setBackgroundResource(R.drawable.group);
            }
            cursor.close();
            db.close();
            viewHolder.linkmanImageg.setVisibility(View.VISIBLE);
            viewHolder.callClick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /*Intent intent = new Intent(getContext(), FirstActivity.class);
                    intent.putExtra("data", 1);
                    CallState callState = GlobalStatus.getCallCallStatus().get(String.valueOf(1) + msg.getTeamID());
                    if (GlobalStatus.equalTeamID(msg.getTeamID())) {
                        intent.putExtra("callType", 0);
                    } else if (callState != null && callState.getState() == GlobalStatus.STATE_CALL) {
                        intent.putExtra("callType", 1);
                    } else {
                        intent.putExtra("callType", 2);
                    }
                    intent.putExtra("group", msg.getTeamID());
                    intent.putExtra("type", msg.getMemberRole());
                    intent.putExtra("group_name", msg.getLinkmanName());
                    VideoOrVoiceDialog dialog = new VideoOrVoiceDialog(getContext(), intent);
                    dialog.show();*/
//                getContext().startActivity(intent);
                }
            });

            if (msg.getMemberRole() == ProtoMessage.TeamRole.Owner_VALUE) {
                viewHolder.role.setImageResource(R.drawable.host);
            } else if (msg.getMemberRole() == ProtoMessage.TeamRole.Manager_VALUE) {
                viewHolder.role.setImageResource(R.drawable.manager);
            } else {
                viewHolder.role.setImageBitmap(null);
            }

//        if (msg.isTop()) {
//            viewHolder.background.setBackground(getContext().getResources().getDrawable(R.drawable.listview_item_top_change));
//        } else {
//            viewHolder.background.setBackground(getContext().getResources().getDrawable(R.drawable.listview_item_change));
//        }
            viewHolder.linkmanName.setText(msg.getLinkmanName());
            if(msg.getTeamType() == ProtoMessage.TeamType.teamRandom_VALUE){
                if(msg.getLinkmanName().contains("海聊群")){
                    viewHolder.linkmanName.setText("海聊群");
                }
            }
            viewHolder.linkmanDesc.setText(msg.getLinkmanDesc());

            // 判断当前是否是对讲状态，是则显示挂断按钮，否则仍然保持绿色呼叫按钮
            CallState callState = GlobalStatus.getCallCallStatus().get(String.valueOf(1) + msg.getTeamID());
            if (GlobalStatus.equalTeamID(msg.getTeamID())) {
                viewHolder.callLink.setImageResource(R.drawable.calling);
            } else if (callState != null && callState.getState() == GlobalStatus.STATE_CALL) {
                viewHolder.callLink.setImageResource(R.drawable.img_other_talk);
            } else {
                viewHolder.callLink.setImageResource(R.drawable.btn_call);
            }
            viewHolder.callLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), FirstActivity.class);
                    intent.putExtra("data", 1);
                    CallState callState = GlobalStatus.getCallCallStatus().get(String.valueOf(1) + msg.getTeamID());
                    if (GlobalStatus.equalTeamID(msg.getTeamID())) {
                        intent.putExtra("callType", 0);
                    } else if (callState != null && callState.getState() == GlobalStatus.STATE_CALL) {
                        intent.putExtra("callType", 1);
                    } else {
                        intent.putExtra("callType", 2);
                    }
                    intent.putExtra("group", msg.getTeamID());
                    intent.putExtra("type", msg.getMemberRole());
                    intent.putExtra("group_name", msg.getLinkmanName());
                    VideoOrVoiceDialog dialog = new VideoOrVoiceDialog(getContext(), intent);
                    dialog.show();
                }
            });
            return view;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    private class ViewHolder{
        PuzzleView linkmanImageg;
        TextView linkmanName;
        TextView linkmanDesc;
        CircleImageView callLink;
        LinearLayout background;
        LinearLayout callClick;
        ImageView role;
    }
}
