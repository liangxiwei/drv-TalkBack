package com.example.jrd48.chat.group;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.GlobalImg;
import com.luobin.dvr.R;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.AppliedFriendsList;
import com.example.jrd48.chat.search.PinyinComparator;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.ApplyGroupProcesser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2016/12/15.
 */

public class InviteJoinGroupActivity extends BaseActivity implements View.OnClickListener {
    Context mContext;
    private Button mBtnConfirm;
    private ImageView mImageViewBack;
    private ListView mListView;
    private ApplyMemberAdapter adapter;
    boolean checkDialog = true;
    List<ViewFriendsMsg> mFriends = new ArrayList<ViewFriendsMsg>();
    private ProgressDialog m_pDialog;
    long teamID;

    class ViewFriendsMsg {
        public AppliedFriends friends;
        public String pinyin;
        private boolean bChecked = false;

        public ViewFriendsMsg() {
        }

        public String getPinyin() {
            return pinyin;
        }

        public void setPinyin(String pinyin) {
            this.pinyin = pinyin;
        }

        public boolean isbChecked() {
            return bChecked;
        }

        public void setbChecked(boolean bChecked) {
            this.bChecked = bChecked;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invite_join);
        mContext = this;
        getIntentMsgAndSave();
        initView();
        adapter = new ApplyMemberAdapter(this, mFriends);
        mListView.setAdapter(adapter);
        mListView.setSelector(R.drawable.dvr_listview_background);
    }

    private void getIntentMsgAndSave() {
        Intent i = getIntent();
        teamID = i.getLongExtra("teamID", 0);
        AppliedFriendsList list = i.getParcelableExtra("apply_member");
        mFriends.clear();
        List<AppliedFriends> list1 = list.getAppliedFriends();
        for (AppliedFriends c : list1) {
            if (TextUtils.isEmpty(c.getNickName())) {
                c.setNickName(c.getUserName());
            }
        }
        // 排序(实现了中英文混排)
        PinyinComparator comparator = new PinyinComparator();
        Collections.sort(list1, comparator);

        for (AppliedFriends c : list1) {
            ViewFriendsMsg temp = new ViewFriendsMsg();
            temp.friends = c;
            mFriends.add(temp);
        }
        setIntent(null);
    }

    private void initView() {
        mBtnConfirm = (Button) findViewById(R.id.btn_confirm);
        mBtnConfirm.setOnClickListener(this);
        mImageViewBack = (ImageView) findViewById(R.id.iv_back);
        mImageViewBack.setOnClickListener(this);
        mListView = (ListView) findViewById(R.id.invie_list);


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ViewFriendsMsg vf = mFriends.get(i);
                vf.setbChecked(!vf.isbChecked());
                adapter.notifyDataSetChanged();
            }
        });

        //********************************************弹窗设置****************************************************
        //创建ProgressDialog对象
        m_pDialog = new ProgressDialog(this, R.style.CustomDialog);
        // 设置进度条风格，风格为圆形，旋转的
        m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // 设置ProgressDialog 提示信息
        m_pDialog.setMessage("请稍等...");
        // 设置ProgressDialog 的进度条是否不明确
        m_pDialog.setIndeterminate(false);
        // 设置ProgressDialog 是否可以按退回按键取消
        m_pDialog.setCancelable(true);
        //ProgressDialog取消监听事件
        m_pDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (checkDialog) {
                    getBroadcastManager().stopAll();
                    ToastR.setToast(mContext, "取消邀请好友");
                }
            }
        });
        //********************************************弹窗设置****************************************************
    }

    private void applyMember(final List<AppliedFriends> friend) {
        m_pDialog.show();
        checkDialog = true;
        ProtoMessage.ApplyTeam.Builder builder = ProtoMessage.ApplyTeam.newBuilder();
        builder.setTeamID(teamID);
        for (int i = 0; i < friend.size(); i++) {
            builder.addPhoneList(friend.get(i).getPhoneNum());
        }
        MyService.start(mContext, ProtoMessage.Cmd.cmdApplyTeam.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(ApplyGroupProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(InviteJoinGroupActivity.this, filter, getBroadcastManager());

        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                checkDialog = false;
                m_pDialog.cancel();
                ToastR.setToast(InviteJoinGroupActivity.this, "连接超时");
            }

            @Override
            public void onGot(Intent intent) {
                checkDialog = false;
                m_pDialog.cancel();
                int errorCode = intent.getIntExtra("error_code", -1);
                if (errorCode == ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(mContext, "邀请成功");
                    clearListorCache(friend);
                } else {
                    fail(intent.getIntExtra("error_code", -1));
                }
            }
        });
    }

    public void fail(int code) {
        new ResponseErrorProcesser(InviteJoinGroupActivity.this, code);
    }

    /**
     * 清除缓存数据
     *
     * @param friend
     */
    protected void clearListorCache(List<AppliedFriends> friend) {
        List<ViewFriendsMsg> data = adapter.getList();
        // 遍历删除内存数据
        for (AppliedFriends af : friend) {
            Iterator<ViewFriendsMsg> iterator = data.iterator();
            while (iterator.hasNext()) {
                ViewFriendsMsg k = iterator.next();
                if (k.friends.getPhoneNum().equals(af.getPhoneNum())) {
                    iterator.remove();
                    break;
                }
            }
        }
        Log.d("chat", " data.size(): " + data.size());
        if (data.size() == 0) {
            finish();
        }
        refreshListData(data);
    }

    /**
     * 刷新列表
     *
     * @param data
     */
    private void refreshListData(List<ViewFriendsMsg> data) {
        try {

            if (adapter == null) {
                Log.d("CHAT", "Excepton: refreshListData: Adapter is null!");
            } else {
                adapter.refresh(data);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm:
                checkList();
                break;
            case R.id.iv_back:
//                Intent intent = new Intent(InviteJoinGroupActivity.this, FirstActivity.class);
//                startActivity(intent);
                m_pDialog.cancel();
                finish();
                break;
        }

    }

    private void checkList() {
        List<AppliedFriends> friend = new ArrayList<AppliedFriends>();
        for (ViewFriendsMsg vf : mFriends) {
            if (vf.bChecked == true) {
                friend.add(vf.friends);
            }
        }
        if (friend.size() <= 0) {
            ToastR.setToast(mContext, "请选择你要邀请的好友");
            return;
        } else {
            applyMember(friend);
        }
    }

    /**
     * 重写返回键功能
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // 这里重写返回键
//                startActivity(new Intent(mContext, FirstActivity.class));
            m_pDialog.cancel();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    /**
     * 自定义DbAdapter
     */

    public class ApplyMemberAdapter extends BaseAdapter {

        private List<ViewFriendsMsg> mArrayList;
        private Context context;
        private LayoutInflater layoutInflater;

        public List<ViewFriendsMsg> getList() {
            return mArrayList;
        }

        // 刷新适配器
        public void refresh(List<ViewFriendsMsg> mArryFriend) {
            this.mArrayList = mArryFriend;
            notifyDataSetChanged();
        }

        public ApplyMemberAdapter(Context context, List<ViewFriendsMsg> mFriend) {
            layoutInflater = LayoutInflater.from(context);
            this.context = context;
            this.mArrayList = mFriend;
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
        public ViewFriendsMsg getItem(int position) {
            ViewFriendsMsg item = null;
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

            final ViewFriendsMsg friendsMsg = mArrayList.get(position);

            ViewHolder holder = null;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.invite_join_item, null);
                holder.name = (TextView) convertView.findViewById(R.id.listitem_name);
                holder.memberNumber = (TextView) convertView.findViewById(R.id.listitem_count);
                holder.images = (ImageView) convertView.findViewById(R.id.circle_image);
                holder.groupNumber = (TextView) convertView.findViewById(R.id.listitem_number);
                holder.selectedIcon = (ImageView) convertView.findViewById(R.id.listitem_selected);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (friendsMsg == null) {
                return null;
            }
            holder.memberNumber.setText(friendsMsg.friends.getPhoneNum());

            String name = friendsMsg.friends.getNickName();
            if (TextUtils.isEmpty(name) || name.equals("null")) {
                name = friendsMsg.friends.getUserName();
            }
            holder.name.setText(name);

            if (friendsMsg.bChecked == true) {
                holder.selectedIcon.setVisibility(View.VISIBLE);
            } else {
                holder.selectedIcon.setVisibility(View.INVISIBLE);
            }
            Bitmap bitmap = GlobalImg.getImage(InviteJoinGroupActivity.this, friendsMsg.friends.getPhoneNum());
            if (bitmap == null) {
                if (friendsMsg.friends.getUserSex() == ProtoMessage.Sex.female_VALUE) {
                    holder.images.setImageResource(R.drawable.woman);
                } else {
                    holder.images.setImageResource(R.drawable.man);
                }
//                holder.images.setImageResource(R.drawable.default_useravatar);
            } else {
                holder.images.setImageBitmap(bitmap);
            }
            return convertView;
        }

        public class ViewHolder {
            // public TextView tvNumber;
            private TextView name;
            private TextView memberNumber;
            private TextView groupNumber;
            private ImageView selectedIcon;
            private ImageView images;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
