package com.example.jrd48.chat.amap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.luobin.dvr.R;
import com.example.jrd48.chat.User;
import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.luobin.utils.ButtonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/22.
 */

public class MapMemberListDialog {
    MapMemberListAdapter adapter;
    List<User> mList;

    public void showDialog(final String mTrackPhoneNum, final Context context, final String myPhone,
                           String userNameMe, final boolean single, final List<User> userList, final boolean isCallHungon,
                           final OnItemClickListener func) {
        Log.i("jim", "是否挂断：" + isCallHungon);
        if (single) {
            boolean isADD = true;
            for (User us : userList) {
                if (us.getPhone().equals(myPhone)) {
                    isADD = false;
                    break;
                }
            }
            if (isADD) {
                User user = new User(0, userNameMe, myPhone, 0);
                userList.add(user);
            }
        } else {
            deleteAddItem(userList);
        }

        if (isCallHungon) {
            mList = getOnLineData(isCallHungon, userList);
        } else {
            mList = userList;
        }

        final AlertDialog dlg = new AlertDialog.Builder(context).create();
        dlg.setCancelable(true);
        dlg.setInverseBackgroundForced(true);
        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                if(keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        ButtonUtils.changeLeftOrRight(true);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        ButtonUtils.changeLeftOrRight(false);
                        return true;
                    }
                }
                return false;
            }
        });
        dlg.show();
        Window window = dlg.getWindow();
        window.setContentView(R.layout.member_list);
        final TextView tvTitle = (TextView) window.findViewById(R.id.tv_title);
        if (isCallHungon) {
            tvTitle.setText("选择跟踪 " + "(在线:" + onLinelist.size() + " 人数:" + userList.size() + ")");
        } else {
            tvTitle.setText("选择跟踪 " + "(人数:" + userList.size() + ")");
        }
        final ListView listView = (ListView) window.findViewById(R.id.ll_member_list);
        final TextView tvCancel = (TextView) window.findViewById(R.id.tv_cancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dlg.cancel();
            }
        });
        adapter = new MapMemberListAdapter(context, mList, mTrackPhoneNum, isCallHungon, dlg, func);
        listView.setAdapter(adapter);
        listView.setSelector(R.drawable.dvr_listview_background);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dlg.cancel();
                if (mList.get(i).getPhone() != null && mList.get(i).getPhone().length() > 0) {
                    try {
                        func.onResult(mList.get(i));
                    }catch (Exception e){
                        Log.e("jim",e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    Log.e("chat", "当前列表出错");
                }
            }
        });
        int s = getSelection(mTrackPhoneNum, mList);
        listView.setSelection(s);
//        adapter.notifyDataSetChanged();
    }

    private List<User> onLinelist = new ArrayList<>();
    private List<User> offLinelist = new ArrayList<>();
    private List<User> list = new ArrayList<>();

    //区分在线的和不在线的
    private List<User> getOnLineData(boolean isCallHungon, List<User> userList) {
        onLinelist.clear();
        offLinelist.clear();
        list.clear();
        for (User user : userList) {
            if (user.getState() == ProtoMessage.ChatStatus.csSpeaking_VALUE ||
                    user.getState() == ProtoMessage.ChatStatus.csOk_VALUE) {
                onLinelist.add(user);
            } else {
                offLinelist.add(user);
            }
        }
        for (User user : onLinelist) {
            list.add(user);
        }
        for (User user : offLinelist) {
            list.add(user);
        }

        return list;
    }

    //获取上次设置跟踪的选项位置
    private int getSelection(final String mTrackPhoneNum, final List<User> userList) {
        int item = -1;
        if (mTrackPhoneNum != null && mTrackPhoneNum.length() > 0) {
            int t = -1;
            for (User user : userList) {
                t++;
                if (user.getPhone().equals(mTrackPhoneNum)) {
                    item = t;
                    break;
                }
            }
        }
        return item == -1 ? 0 : item;
    }

    private void deleteAddItem(List<User> userList) {
        int k = -1;
        int i = -1;
        for (User mte : userList) {
            ++i;
            if (User.ADD.equals(mte.getName())) {
                k = i;
                break;
            }
        }
        if (k >= 0) {
            userList.remove(k);
        }
    }

    /**
     * 自定义DbAdapter
     */

    public class MapMemberListAdapter extends BaseAdapter {

        private List<User> mArrayList;
        private Context context;
        private LayoutInflater layoutInflater;
        private final String mTrackPhoneNum;
        private AlertDialog mAlertDialog;
        private OnItemClickListener func;
        private boolean isCallHungon;

        public List<User> getList() {
            return mArrayList;
        }

        // 刷新适配器
//        public void refresh(List<User> mArryFriend) {
//            this.mArrayList = mArryFriend;
//            notifyDataSetChanged();
//        }

        public MapMemberListAdapter(Context context, List<User> mFriend, final String mTrackPhoneNum, boolean isCallHungon, AlertDialog dlg, final OnItemClickListener func) {
            layoutInflater = LayoutInflater.from(context);
            this.context = context;
            this.mArrayList = mFriend;
            this.mTrackPhoneNum = mTrackPhoneNum;
            this.isCallHungon = isCallHungon;
            this.mAlertDialog = dlg;
            this.func = func;
        }

        @Override
        public int getCount() {
            int count = 0;
            if (null != mArrayList) {
                count = mArrayList.size();
            }
            return count;
        }

        @Override
        public User getItem(int position) {
            User item = null;
            if (null != mArrayList) {
                item = mArrayList.get(position);
            }
            return item;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        int friendsIndex = 0;

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final User friendsMsg = mArrayList.get(position);
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.member_list_item, null);
                holder.name = (TextView) convertView.findViewById(R.id.member_name);
                holder.images = (ImageView) convertView.findViewById(R.id.member_image_s);
                holder.imgeOnLine = (ImageView) convertView.findViewById(R.id.iv_on_line);
                holder.cancelTrack = (Button) convertView.findViewById(R.id.cancel_track);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (friendsMsg == null) {
                return null;
            }


            if (friendsMsg.getPhone() != null && friendsMsg.getPhone().length() > 0) {
                holder.name.setText(friendsMsg.getName());
                if (mTrackPhoneNum != null && mTrackPhoneNum.equals(friendsMsg.getPhone())) {
                    holder.cancelTrack.setVisibility(View.VISIBLE);
                } else {
                    holder.cancelTrack.setVisibility(View.GONE);
                }

                holder.cancelTrack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mAlertDialog.cancel();
                        func.onClickButton();
                    }
                });

                Bitmap bitmap = FriendFaceUtill.getUserFace(context, friendsMsg.getPhone());
//                    GlobalImg.getImage(context, friendsMsg.friends.getPhoneNum());
                if (bitmap == null) {
                    if (friendsMsg.getUserSex() == ProtoMessage.Sex.female_VALUE) {
                        holder.images.setImageResource(R.drawable.woman);
                    } else {
                        holder.images.setImageResource(R.drawable.man);
                    }
                } else {
                    holder.images.setImageBitmap(bitmap);
                }
            }

            if (isCallHungon) {
                if (friendsMsg.getState() == ProtoMessage.ChatStatus.csNotIn_VALUE) {
                    holder.imgeOnLine.setVisibility(View.VISIBLE);
                    holder.images.setAlpha(0.5f);
                    holder.imgeOnLine.setBackgroundResource(R.drawable.off_line);
                } else if (friendsMsg.getState() == ProtoMessage.ChatStatus.csOffline_VALUE) {
                    holder.imgeOnLine.setBackgroundResource(R.drawable.unknow);
                    holder.images.setAlpha(0.5f);
                    holder.imgeOnLine.setVisibility(View.VISIBLE);
                } else {
                    holder.imgeOnLine.setVisibility(View.GONE);
                    holder.images.setAlpha(1f);
                }
            } else {
                holder.imgeOnLine.setVisibility(View.GONE);
            }

            return convertView;
        }

        public class ViewHolder {
            public Button cancelTrack;
            public TextView name;
            public TextView phone;
            public ImageView images;
            public ImageView imgeOnLine;
        }

    }

}
