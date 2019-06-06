package com.example.jrd48.service.notify;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.example.jrd48.chat.group.DBHelperTeamList;
import com.example.jrd48.chat.group.DBManagerTeamList;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.chat.wiget.PuzzleView;
import com.luobin.dvr.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/3.
 */

public class NotifyAdapter extends BaseAdapter {
    private List<String> names;
    private Map<String, String> showNameList;
    private List<Integer> typeList;

    public NotifyAdapter() {
        names = new ArrayList<String>();
        showNameList = new HashMap<String, String>();
        typeList = new ArrayList<Integer>();
    }

    public List<String> getNames() {
        return names;
    }

    public List<Integer> getTypes() {
        return typeList;
    }

    public Map<String, String> getShowNames() {
        return showNameList;
    }

    public void addName(String name, int type) {
        for (int i = 0; i < names.size(); i++) {
            if(names.get(i).equals(name) && type == typeList.get(i)){
                return;
            }
        }
        names.add(name);
        typeList.add(type);
        Context context = MyApplication.getContext();
        if (!showNameList.containsKey(name) || TextUtils.isEmpty(showNameList.get(name))) {
            if (name.startsWith("0")) {
                DBManagerFriendsList db = new DBManagerFriendsList(context, true, DBTableName.getTableName(context, DBHelperFriendsList.NAME));
                AppliedFriends linkman = db.getFriend(name.substring(1));
                db.closeDB();
                String linkmanName = "";
                if(linkman != null){
                    linkmanName = linkman.getNickName();
                    if(TextUtils.isEmpty(linkmanName)){
                        linkmanName = linkman.getUserName();
                    }
                }
                showNameList.put(name, linkmanName);
            } else {

                DBManagerTeamList db = new DBManagerTeamList(context, true, DBTableName.getTableName(context, DBHelperTeamList.NAME));
                TeamInfo t = db.getTeamInfo(Long.valueOf(name.substring(1)));
                db.closeDB();
//                if (t == null) {
//                    throw new RuntimeException("取组信息时出错");
//                }
//                showNameList.put(name, t.getTeamName());

//
                if (t != null) {
                    showNameList.put(name, t.getTeamName());
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return names.size();
    }

    @Override
    public Object getItem(int position) {
        return names.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHold vh = null;
        if (convertView == null) {
            convertView = View.inflate(MyApplication.getContext(), R.layout.notify_list_item, null);
            vh = new ViewHold();
            convertView.setTag(vh);
            vh.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            vh.imageView = (ImageView) convertView.findViewById(R.id.linkman_image_g);
            vh.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
        } else {
            vh = (ViewHold) convertView.getTag();
        }
        String name = names.get(position);
        String action = null;
        if (typeList.get(position) == 0) {
            action = " 发起呼叫";
        } else if (typeList.get(position) == 1) {
            action = " 请求查看路况";
        } else if (typeList.get(position) == 2) {
            action = " 正在说话";
        }

        if (name.startsWith("0") && showNameList.get(name) != null) {
            Bitmap bitmap = GlobalImg.getImage(MyApplication.getContext(),name.substring(1));
            if(bitmap != null){
                vh.imageView.setImageBitmap(bitmap);
            } else {
                vh.imageView.setImageResource(R.drawable.default_useravatar);
            }


            if (!TextUtils.isEmpty(showNameList.get(name))) {
//                    Log.e("wsDvr","showNameList.get(name):" + showNameList.get(name));
                vh.tv_name.setText(showNameList.get(name) );
                vh.tv_content.setText(action);
            } else {
                String phone = name.substring(1);
                Log.d("wsDvr",phone);
                phone = phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
                vh.tv_name.setText(phone);
                vh.tv_content.setText(action);
            }
        } else if (name.startsWith("1")) {
            Bitmap bitmap = GlobalImg.getImage(MyApplication.getContext(), "team" + name.substring(1));
            if(bitmap != null){
                vh.imageView.setImageBitmap(bitmap);
            } else {
                vh.imageView.setImageResource(R.drawable.group);
            }

            if(TextUtils.isEmpty(showNameList.get(name))){
                showNameList.remove(name);
                vh.tv_name.setText("群组" + name.substring(1));
            } else {
                vh.tv_name.setText("群组" + showNameList.get(name));
            }
            vh.tv_content.setText(action);
        }

        return convertView;
    }


    public class ViewHold {
        TextView tv_name;
        ImageView imageView;
        TextView tv_content;
    }
}
