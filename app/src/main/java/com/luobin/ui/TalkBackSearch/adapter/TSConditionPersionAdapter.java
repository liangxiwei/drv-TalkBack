package com.luobin.ui.TalkBackSearch.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.PicTool.LetterTileDrawable;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.root.SearchFriendProcesser;
import com.luobin.dvr.R;
import com.luobin.model.SearchStrangers;
import com.luobin.search.friends.SearchReturnActivity;
import com.luobin.ui.InterestBean;
import com.luobin.ui.SelectInterestAdapter;
import com.luobin.ui.TalkBackSearch.ClickInterFace;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TSConditionPersionAdapter extends BaseAdapter {
    private Context context = null;
    private List<SearchStrangers> mArrayList = new ArrayList<>();

    public TSConditionPersionAdapter(Context context, List<SearchStrangers> mFriend) {
        this.context = context;
        this.mArrayList = mFriend;
    }

    // 刷新适配器
    public void refresh(List<SearchStrangers> mArryFriend) {
        this.mArrayList = mArryFriend;
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return mArrayList.size();
    }

    @Override
    public SearchStrangers getItem(int i) {
        return mArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final SearchStrangers user = mArrayList.get(i);

        ViewHolder viewHolder = null;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.adapter_persion_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Bitmap bitmap = GlobalImg.getImage(context, user.getPhoneNum());
        if (bitmap == null) {
//                holder.image.setImageResource(R.drawable.default_useravatar);
            LetterTileDrawable drawable = new LetterTileDrawable(context.getResources());
            drawable.setContactDetails(user.getUserName(), user.getUserName());
            Bitmap bmp = FriendFaceUtill.drawableToBitmap(drawable);
            viewHolder.imgHead.setImageBitmap(bmp);
            //    getUserFace(user.getPhoneNum());

        } else {
            viewHolder.imgHead.setImageBitmap(bitmap);
        }


        String number = user.getPhoneNum();
        String str = number.substring(0, 3);
        String str1 = number.substring(8, number.length());
        if (user.getUserName().equals(number)) {
            viewHolder.tvName.setText(str + "*****" + str1);
        } else {
            viewHolder.tvName.setText(user.getUserName() + "");
        }

        viewHolder.tvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickInterFace != null) {
                    clickInterFace.itemClick(v, i);
                }
            }
        });

        return view;
    }


    static class ViewHolder {
        @BindView(R.id.imgHead)
        ImageView imgHead;
        @BindView(R.id.tvName)
        TextView tvName;
        @BindView(R.id.tvAdd)
        TextView tvAdd;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public void setClickInterFace(ClickInterFace clickInterFace) {
        this.clickInterFace = clickInterFace;
    }

    ClickInterFace clickInterFace = null;



}
