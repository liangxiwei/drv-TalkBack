package com.example.jrd48.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.PicTool.LetterTileDrawable;
import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.luobin.dvr.R;
import com.luobin.model.CallState;
import com.luobin.ui.VideoOrVoiceDialog;

import java.util.List;

/**
 * Created by jrd48
 */

class LinkmansAdapter extends BaseAdapter {
    public static final String TAG = "pocdemo";
    private Context context;
    private List<Linkmans> list;
    private ViewHolder viewHolder;

    LinkmansAdapter(Context context, List<Linkmans> list) {
        this.context = context;
        this.list = list;
    }
    public void refresh(List<Linkmans> list){
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    public Linkmans getItemByPhoneNum(String phoneNum){
        for (int i=0; list!=null && i<list.size(); ++i){
            if (list.get(i).getLinkmanPhone().equals(phoneNum)) {
                Log.d("jim","getItemByPhoneNum list size"+list.size());
                return list.get(i);
            }
        }
        return null;
    }

    public void clearOnlineStatus(){
        Log.d("jim","clearOnlineStatus list size "+(list == null ?null:list.size()));
        for (int i=0; list!=null && i<list.size(); ++i){
            Log.d("jim","clearOnlineStatus phone number "+list.get(i).getLinkmanPhone());
            list.get(i).setOnline(false);
        }
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    public List<Linkmans> getList() {
        return list;
    }

    @Override
    public boolean isEnabled(int position) {
        // TODO Auto-generated method stub
        if (list.get(position).getLinkmanPhone() == null || list.get(position).getLinkmanPhone().equals(""))// 如果是字母索引
            return false;// 表示不能点击
        return super.isEnabled(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Linkmans linkmans = list.get(position);
        String item = list.get(position).getLinkmanName();
        viewHolder = new ViewHolder();
        if (linkmans.getLinkmanPhone() == null || linkmans.getLinkmanPhone().length() <= 0) {
            convertView = LayoutInflater.from(context).inflate(R.layout.index, null);
            viewHolder.indexTv = (TextView) convertView.findViewById(R.id.indexTv);
            viewHolder.indexTv.setText(list.get(position).getLinkmanName());
        } else {
            convertView = LayoutInflater.from(context).inflate(R.layout.link_man, null);
            viewHolder.linkmanImages = (ImageView) convertView.findViewById(R.id.linkman_image_s);
            viewHolder.linkmanName = (TextView) convertView.findViewById(R.id.linkman_name);
            viewHolder.linkmanPhone = (TextView) convertView.findViewById(R.id.linkman_phone);
            viewHolder.callLink = (CircleImageView) convertView.findViewById(R.id.call_link);
            viewHolder.callClick = (LinearLayout) convertView.findViewById(R.id.call_click);
            if (linkmans.getLinkmanPhone() != null) {
                Bitmap bitmap = getUserFace(linkmans);
                if (bitmap == null) {
                    String name = "1";
                    if (!list.get(position).getLinkmanName().equals(list.get(position).getLinkmanPhone())) {
                        name = list.get(position).getLinkmanName();
                    }
                    LetterTileDrawable drawable = new LetterTileDrawable(context.getResources());
                    drawable.setContactDetails(name, name);
                    Bitmap bmp = FriendFaceUtill.drawableToBitmap(drawable);
                    viewHolder.linkmanImages.setImageBitmap(bmp);
                }  else {
                    viewHolder.linkmanImages.setImageBitmap(bitmap);
                }
            }
//            viewHolder.linkmanImages.setImageResource(list.get(position).getLinkmanImage());
            viewHolder.linkmanImages.setVisibility(View.VISIBLE);
            if (linkmans.isOnline()) {
                viewHolder.linkmanImages.setAlpha(255);
                viewHolder.linkmanName.setTextColor(context.getResources().getColor(R.color.white));
                viewHolder.callClick.setVisibility(View.VISIBLE);
                //viewHolder.callClick.setAlpha(255);
            } else {
                viewHolder.linkmanImages.setAlpha(125);
                viewHolder.linkmanName.setTextColor(context.getResources().getColor(R.color.text_color));
                viewHolder.callClick.setVisibility(View.INVISIBLE);
                //viewHolder.callClick.setAlpha(100);
            }

            viewHolder.callClick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /*Intent intent = new Intent(context,FirstActivity.class);
                    intent.putExtra("data", 1);

                    CallState callState = GlobalStatus.getCallCallStatus().get(String.valueOf(0) + linkmans.getLinkmanPhone());
                    if (GlobalStatus.equalPhone(linkmans.getLinkmanPhone())) {
                        intent.putExtra("callType", 0);
                    } else if(callState != null && callState.getState() == GlobalStatus.STATE_CALL){
                        intent.putExtra("callType", 1);
                    } else {
                        intent.putExtra("callType", 2);
                    }
                    intent.putExtra("linkmanName", linkmans.getLinkmanName());
                    intent.putExtra("linkmanPhone", linkmans.getLinkmanPhone());
                    VideoOrVoiceDialog dialog = new VideoOrVoiceDialog(context,intent);
                    dialog.show();*/
//                    context.startActivity(intent);
                }
            });


            if (list.get(position).getLinkmanName().equals(list.get(position).getRealName())) {
                viewHolder.linkmanName.setText(list.get(position).getRealName());
                viewHolder.linkmanPhone.setText(list.get(position).getLinkmanPhone());
            } else {
                viewHolder.linkmanName.setText(list.get(position).getLinkmanName());
                if (list.get(position).getRealName().equals(list.get(position).getLinkmanPhone())) {
                    viewHolder.linkmanPhone.setText(list.get(position).getLinkmanPhone());
                } else {
                    viewHolder.linkmanPhone.setText("(" + list.get(position).getRealName() + ")" + list.get(position).getLinkmanPhone());
                }
            }

            CallState callState = GlobalStatus.getCallCallStatus().get(String.valueOf(0) + linkmans.getLinkmanPhone());
            if (GlobalStatus.equalPhone(linkmans.getLinkmanPhone())) {
                viewHolder.callLink.setImageResource(R.drawable.calling);
            } else if(callState != null && callState.getState() == GlobalStatus.STATE_CALL){
                viewHolder.callLink.setImageResource(R.drawable.img_other_talk);
            } else {
                viewHolder.callLink.setImageResource(R.drawable.btn_call);
            }
            viewHolder.callLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, FirstActivity.class);
                    intent.putExtra("data", 1);

                    CallState callState = GlobalStatus.getCallCallStatus().get(String.valueOf(0) + linkmans.getLinkmanPhone());
                    if (GlobalStatus.equalPhone(linkmans.getLinkmanPhone())) {
                        intent.putExtra("callType", 0);
                    } else if (callState != null && callState.getState() == GlobalStatus.STATE_CALL) {
                        intent.putExtra("callType", 1);
                    } else {
                        intent.putExtra("callType", 2);
                    }
                    intent.putExtra("linkmanName", linkmans.getLinkmanName());
                    intent.putExtra("linkmanPhone", linkmans.getLinkmanPhone());
                    VideoOrVoiceDialog dialog = new VideoOrVoiceDialog(context, intent);
                    dialog.show();
                }
            });
        }
        return convertView;
    }

    public Bitmap getUserFace(Linkmans param) {
        Bitmap bmp =  GlobalImg.getImage(context, param.getLinkmanPhone());
        try {
            if (bmp == null) {
                return null;
            }

            Bitmap bitmap1 = Bitmap.createScaledBitmap(bmp, 150, 150,
                    false); // 将图片缩小
            ImageTool ll = new ImageTool(); // 图片头像变成圆型
            bmp = ll.toRoundBitmap(bitmap1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;

    }



    private class ViewHolder {
        ImageView linkmanImages;
        TextView linkmanName;
        TextView linkmanPhone;
        CircleImageView callLink;
        LinearLayout callClick;
        private TextView indexTv;
    }
}
