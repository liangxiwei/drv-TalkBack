package com.example.jrd48.chat;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jrd48.chat.SQLite.MsgRecordHelper;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.group.DBHelperTeamList;
import com.example.jrd48.chat.group.DBManagerTeamList;
import com.example.jrd48.chat.group.MsgTool;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.TeamInfoList;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.root.AutoCloseProcesser;
import com.example.jrd48.service.protocol.root.FriendsListProcesser;
import com.example.jrd48.service.protocol.root.GroupsListProcesser;
import com.example.jrd48.service.protocol.root.ReceiverProcesser;
import com.luobin.dvr.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;


public class TabFragmentMsg extends BaseLazyFragment {
    private List<MsgRecord> msgList1 = new ArrayList<>();
    private MsgRecordAdapter adapter1;
    private ListView list1;
    private MsgRecordHelper msgRecordHelper;
    private GetMsgReceiiver getMsgReceiiver;
    private IntentFilter filter;
    private int group;
    private int memberRole = 0;
    private boolean run = false;
    private IntentFilter filterRoom;
    private CloseRoomReceiiver closeRoomReceiiver;
    private Map<String, String> friendNames;
    private boolean this_top = false;
    private ProgressDialog m_pDialog;
    private ChatStatusReceiver chatStatusReceiver;

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisible) {
            try {
                if ((Boolean) SharedPreferencesUtils.get(getContext(), "friend_list_changed", false) == true) {
                    DBManagerFriendsList db = new DBManagerFriendsList(this.getMyActivity(), true, DBTableName.getTableName(getContext(), DBHelperFriendsList.NAME));
                    friendNames = db.getFriendsNickName();
                    db.closeDB();
                    SharedPreferencesUtils.put(getContext(), "friend_list_changed", false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (run) {
                initMsgs(getContext());
            }
        }
    }

    @Override
    protected void initPrepare() {

    }

    @Override
    protected void onInvisible() {

    }

    @Override
    protected void initData() {
        checkData();
        if ((Boolean) SharedPreferencesUtils.get(this.getContext(), "data_init", false) == false) {
            // TODO：开启转圈
            m_pDialog.show();
            initFriendList();
        } else {
            onInitDataSucc();
        }
        DBManagerFriendsList db = new DBManagerFriendsList(this.getMyActivity(), true, DBTableName.getTableName(getContext(), DBHelperFriendsList.NAME));
        friendNames = db.getFriendsNickName();
        db.closeDB();

    }

    private void checkData() {
        m_pDialog = new ProgressDialog(getContext(), R.style.CustomDialog);
        m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        m_pDialog.setMessage("请稍等...正在刷新数据");
        m_pDialog.setIndeterminate(false);
        m_pDialog.setCancelable(false);
        //查看本地是否有好友列表
//        DBManagerFriendsList friendDb = new DBManagerFriendsList(getMyActivity());
//        List<AppliedFriends> list = friendDb.getFriends();
//        friendDb.closeDB();
//        if (list.size() <= 0) {
//            downFriendsThread th = new downFriendsThread();
//            th.start();
//        }
        //查看本地是否有群组列表
//        DBManagerTeamList teamDb = new DBManagerTeamList(getMyActivity());
//        List<TeamInfo> teamList = teamDb.getTeams();
//        teamDb.closeDB();
//        if (teamList.size() <= 0) {
//            downGroupThread gt = new downGroupThread();
//            gt.start();
//        }
    }

    @Override
    public void onDestroy() {
        if (getMsgReceiiver != null) {
            getContext().unregisterReceiver(getMsgReceiiver);
        }
        if (closeRoomReceiiver != null) {
            getContext().unregisterReceiver(closeRoomReceiiver);
        }
        super.onDestroy();
    }

    @Override
    protected View initView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.demolayout1, container,
                false);
        list1 = (ListView) view.findViewById(R.id.list_view);

        return view;
    }

    private void showSharePicDialog(final int position) {
        LayoutInflater factory = LayoutInflater.from(getContext());// 提示框
        final View view = factory.inflate(R.layout.dialog_share_pic, null);
        TextView tvFriendName = (TextView) view.findViewById(R.id.tv_name);
        ImageView imgFriendPic = (ImageView) view.findViewById(R.id.img_friend);
        ImageView imgDialog = (ImageView) view.findViewById(R.id.dialog_img);
        final EditText edFriendMsg = (EditText) view.findViewById(R.id.et_friend_msg);

        Button btnCancel = (Button) view.findViewById(R.id.btn_dialog_cancel);
        Button btnSend = (Button) view.findViewById(R.id.btn_dialog_send);

        if (msgList1.get(position).getSg()) {
            tvFriendName.setText(msgList1.get(position).getGroupName());
            imgFriendPic.setImageBitmap(GlobalImg.getImage(getContext(), msgList1.get(position).getPhone()));
        } else {
            tvFriendName.setText(msgList1.get(position).getGroupName());
            imgFriendPic.setImageBitmap(GlobalImg.getImage(getContext(), "team" + msgList1.get(position).getTeamId()));
        }
        final String image = FileUtils.getUriPath(getContext(), MainActivity.mUri);

        if (MainActivity.mUri != null) {
            imgDialog.setVisibility(View.VISIBLE);
            Bitmap bitmap = BitmapFactory.decodeFile(image);
            imgDialog.setImageBitmap(bitmap);
        }

        if (MainActivity.mText != null && !MainActivity.mText.equals("")) {
            edFriendMsg.setText(MainActivity.mText);
            edFriendMsg.setVisibility(View.VISIBLE);
        }

        final AlertDialog mDialog = new AlertDialog.Builder(getContext())// 提示框标题
                .setView(view).create();
        mDialog.show();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String edText = edFriendMsg.getText().toString();
                onItemClicks(position, image, edText);
                mDialog.dismiss();
            }
        });

    }

    private void initListViewClick() {
        //ListView1点击事件
        list1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Intent intents = MainActivity.mIntent;
                if (MainActivity.mAction != null) {
                    if (MainActivity.mAction.equals(Intent.ACTION_SEND)) {
                        showSharePicDialog(position);
                    }
                } else {
                    onItemClicks(position, null, null);
                }
            }
        });
        list1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                int i = msgList1.get(position).getTop();
                if (i == 0) {
                    this_top = true;
                } else {
                    this_top = false;
                }
                return false;
            }
        });
        list1.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenu.ContextMenuInfo menuInfo) {
                if(this_top){
                    menu.add(0, 0, 0, "置顶消息");
                }else{
                    menu.add(0, 2, 0, "取消置顶消息");
                }

                menu.add(0, 1, 0, "删除消息记录");
                //menu.add(0, 2, 0, "删除所有消息记录");
            }
        });
    }

    private void onItemClicks(final int position, String iamge, String text) {
        SharedPreferences preferences = getContext().getSharedPreferences("token", Context.MODE_PRIVATE);
        String phone = preferences.getString("phone", "");
        msgRecordHelper = new MsgRecordHelper(getContext(), phone + "MsgShow.dp", null);
        SQLiteDatabase db = msgRecordHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("new_msg", 0);
        if (msgList1.get(position).getSg()) {
            db.update("Msg", values, "phone = ? and msg_from = ?", new String[]{msgList1.get(position).getPhone(), 0 + ""});
        } else {
            db.update("Msg", values, "group_id = ?", new String[]{msgList1.get(position).getTeamId() + ""});
        }
        db.close();
        initMsgs(getContext());

        Intent intent = new Intent(getContext(), FirstActivity.class);
        intent.putExtra("data", 0);
        intent.putExtra("uri", iamge);
        intent.putExtra("text", text);
        intent.putExtra("maction", MainActivity.mAction);

        if (msgList1.get(position).getSg()) {
            intent.putExtra("linkmanName", msgList1.get(position).getGroupName());
            intent.putExtra("linkmanPhone", msgList1.get(position).getPhone());
        } else {
            intent.putExtra("group", msgList1.get(position).getTeamId());
            intent.putExtra("group_name", msgList1.get(position).getGroupName());
            intent.putExtra("type", msgList1.get(position).getMemberRole());
        }
        startActivity(intent);
        NotificationManager nm = (NotificationManager) (getContext().getSystemService(getContext().NOTIFICATION_SERVICE));
        nm.cancel(0);//消除对应ID的通知
    }

    //ListView弹出菜单
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        //info.id得到listview中选择的条目绑定的id
        String id = String.valueOf(info.id);
        switch (item.getItemId()) {
            case 0:
                topMsg(msgList1.get((int) info.id).getTeamId(), msgList1.get((int) info.id).getPhone(), msgList1.get((int) info.id).getSg(),1);
                initMsgs(getContext());
                return true;
            case 1:
                if (msgList1.get((int) info.id).getSg()) {
                    MsgTool.deleteFriends_OnlyMsg(getContext(), msgList1.get((int) info.id).getPhone());
                } else {
                    MsgTool.deleteTeam_OnlyMsg(getContext(), msgList1.get((int) info.id).getTeamId());
                }
                initMsgs(getContext());
                return true;
            case 2:
                topMsg(msgList1.get((int) info.id).getTeamId(), msgList1.get((int) info.id).getPhone(), msgList1.get((int) info.id).getSg(),0);
                initMsgs(getContext());
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void topMsg(long teamId, String phone, boolean sg,int top) {
        SharedPreferences preferences = getContext().getSharedPreferences("token", Context.MODE_PRIVATE);
        String myphone = preferences.getString("phone", "");
        msgRecordHelper = new MsgRecordHelper(getContext(), myphone + "MsgShow.dp", null);
        SQLiteDatabase db = msgRecordHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("top", top);
        if (sg) {
            db.update("Msg", values, "phone = ? and msg_from = ?", new String[]{phone, 0 + ""});
        } else {
            db.update("Msg", values, "group_id = ?", new String[]{teamId + ""});
        }
        db.close();
    }

    //读取名字
    private String getName(long l) {
        DBManagerTeamList db = new DBManagerTeamList(getContext(), true, DBTableName.getTableName(getContext(), DBHelperTeamList.NAME));
        List<TeamInfo> list = db.getTeams();
        db.closeDB();
        if (list.size() <= 0) {
            return l + "";
        } else {
            for (TeamInfo teamInfo : list) {
                if (teamInfo.getTeamID() == l) {
                    memberRole = teamInfo.getMemberRole();
                    return teamInfo.getTeamName();
                }
            }
        }
        return l + "";
    }

    private String getName(String phoneNum) {
        if (friendNames == null) {
            return phoneNum;
        }

        try {
            return friendNames.get(phoneNum);
        } catch (Exception e) {
            return phoneNum;
        }

//        DBManagerFriendsList db = new DBManagerFriendsList(getContext());
//        List<AppliedFriends> list = db.getFriends();
//        db.closeDB();
//        if (list.size() <= 0) {
//            return str;
//        } else {
//            for (AppliedFriends appliedFriends : list) {
//                if (appliedFriends.getPhoneNum().equals(str)) {
//                    String friendName = appliedFriends.getNickName();
//                    if (friendName == null || friendName.equals("")) {
//                        friendName = appliedFriends.getUserName();
//                    }
//                    if (friendName == null || friendName.equals("")) {
//                        friendName = appliedFriends.getPhoneNum();
//                    }
//                    return friendName;
//                }
//            }
//        }
//        return str;
    }

    //初始信息写入，读取数据库
    public void initMsgs(Context context) {

        Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
        int yearNow = c.get(Calendar.YEAR);
        int monthNow = c.get(Calendar.MONTH);
        int dateNow = c.get(Calendar.DATE);
        String time = null;

        SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
        String myphone = preferences.getString("phone", "");
        msgRecordHelper = new MsgRecordHelper(context, myphone + "MsgShow.dp", null);
        SQLiteDatabase db = msgRecordHelper.getWritableDatabase();
        Cursor cursor = db.query("Msg", null, null, null, null, null, null);
        try {
            msgList1.clear();
            if (cursor.moveToLast()) {
                do {
                    readSQLite(cursor, yearNow, monthNow, dateNow, 1);
                } while (cursor.moveToPrevious());
                //Collections.reverse(msgList1);//倒序
                cursor.moveToLast();
                do {
                    readSQLite(cursor, yearNow, monthNow, dateNow, 0);
                } while (cursor.moveToPrevious());
                int i = 0;
                for (MsgRecord msg : msgList1) {
                    i += msg.getNew_msg();
                    if (i > 99) {
                        i = 99;
                        break;
                    }
                }
                ((MainActivity)context).setUpTabBadge(i);
                //db.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            db.close();
        }
        // 更新显示
        if(adapter1!=null) {
            adapter1.notifyDataSetChanged();
        }
    }

    private void readSQLite(Cursor cursor, int yearNow, int monthNow, int dateNow, int TOP) {
        String time;
        int top = cursor.getInt(cursor.getColumnIndex("top"));
        if(top == TOP) {

            int year = cursor.getInt(cursor.getColumnIndex("year"));
            int month = cursor.getInt(cursor.getColumnIndex("month"));
            int date = cursor.getInt(cursor.getColumnIndex("date"));

            if (yearNow == year) {
                if (monthNow == month && dateNow == date) {
                    int hour = cursor.getInt(cursor.getColumnIndex("hour"));
                    int minute = cursor.getInt(cursor.getColumnIndex("minute"));
                    time = String.format("%02d:%02d", hour, minute);
                } else {
                    time = (month + 1) + "月" + date + "日";
                }
            } else {
                time = year + "年" + (month + 1) + "月" + date + "日";
            }

            int new_msg = cursor.getInt(cursor.getColumnIndex("new_msg"));
            if (new_msg > 99 || new_msg <= 0) {
                new_msg = 0;
            }

            String msg = cursor.getString(cursor.getColumnIndex("msg"));
            group = cursor.getInt(cursor.getColumnIndex("msg_from"));
            String phone = cursor.getString(cursor.getColumnIndex("phone"));


            if (group == 1) {
                // 组的会话项
                long groupId = cursor.getLong(cursor.getColumnIndex("group_id"));
                String name = getName(groupId);
                //Bitmap bitmap = GlobalImg.getImage(getContext(), groupId + "");
                MsgRecord msgR = new MsgRecord(null, name,/*phone+"："+ */msg, time, false, new_msg);
                msgR.setMemberRole(memberRole);
                msgR.setTeamId(groupId);
                msgR.setPhone(phone);
                if(TOP == 1){
                    msgR.setTop(1);
                }else{
                    msgR.setTop(0);
                }
                msgList1.add(msgR);
            } else {
                String name = getName(phone);
                Bitmap bitmap = GlobalImg.getImage(getContext(), phone);
                MsgRecord msgR = new MsgRecord(bitmap, name, msg, time, true, new_msg);
                msgR.setPhone(phone);
                if(TOP == 1){
                    msgR.setTop(1);
                }else{
                    msgR.setTop(0);
                }
                msgList1.add(msgR);
            }
        }
    }

    private void initFriendList() {
        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
        MyService.start(getMyActivity(), ProtoMessage.Cmd.cmdGetFriendList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(FriendsListProcesser.ACTION);
        new TimeoutBroadcast(getMyActivity(), filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                Log.i("downFriendsThread", "连接超时");
                onInitDataFail();
            }

            @Override
            public void onGot(Intent i) {

                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    Log.i("downFriendsThread", "获取好友成功");
                    initTeamList();

                } else {
                    Log.i("downFriendsThread", "获取好友失败");
                    onInitDataFail();
                }
            }
        });
    }

    private void initTeamList() {
        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
        MyService.start(getMyActivity(), ProtoMessage.Cmd.cmdGetTeamList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(GroupsListProcesser.ACTION);
        new TimeoutBroadcast(getMyActivity(), filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                Log.i("downGroupThread", "连接超时");
                onInitDataFail();
            }

            @Override
            public void onGot(Intent i) {

                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    TeamInfoList list = i.getParcelableExtra("get_group_list");
                    try {

                        Log.i("downFriendsThread", "获取群组成功");
                        onInitDataSucc();
                        MyService.restart(getContext());
                        return;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    onInitDataFail();

                } else {
                    Log.i("downFriendsThread", "获取群组失败");
                    onInitDataFail();
                }
            }
        });
    }


    private void onInitDataSucc() {
        DBManagerFriendsList db = new DBManagerFriendsList(this.getMyActivity(), true, DBTableName.getTableName(getContext(), DBHelperFriendsList.NAME));
        friendNames = db.getFriendsNickName();
        db.closeDB();


        adapter1 = new MsgRecordAdapter(getContext(), R.layout.msg_record_layout, msgList1);
        initMsgs(getContext());//初始化消息
        run = true;
        list1.setAdapter(adapter1);
        initListViewClick();
        //设置长时间监听
        filter = new IntentFilter();
        getMsgReceiiver = new GetMsgReceiiver();
        filter.addAction(ReceiverProcesser.ACTION);
        getContext().registerReceiver(getMsgReceiiver, filter);

        filterRoom = new IntentFilter();
        closeRoomReceiiver = new CloseRoomReceiiver();
        filterRoom.addAction(AutoCloseProcesser.ACTION);
        getContext().registerReceiver(closeRoomReceiiver, filterRoom);

        IntentFilter filt = new IntentFilter();
        chatStatusReceiver = new ChatStatusReceiver();
        filt.addAction("NotifyProcesser.ChatStatus");
        getContext().registerReceiver(chatStatusReceiver, filt);
        SharedPreferencesUtils.put(this.getContext(), "data_init", true);

        // TODO： 停止转圈
        m_pDialog.dismiss();
        Log.w("pocdemo", "初始化数据成功！");


    }

    private void onInitDataFail() {
        // TODO： 停止转圈
        m_pDialog.dismiss();
        Log.w("pocdemo", "初始化数据失败！");
    }

    class GetMsgReceiiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            initMsgs(getContext());
        }
    }

    class CloseRoomReceiiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter1.notifyDataSetChanged();
        }
    }

    class ChatStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("chat_status")) {
                adapter1.notifyDataSetChanged();
            }
        }
    }
}
