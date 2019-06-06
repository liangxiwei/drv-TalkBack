package com.example.jrd48.chat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.jrd48.ShowPhotoActivity;
import com.example.jrd48.chat.FileTransfer.VideoActivity;
import com.example.jrd48.chat.SQLite.LinkmanRecordHelper;
import com.example.jrd48.chat.SQLite.TeamRecordHelper;
import com.example.jrd48.chat.filemanagement.MenuType;
import com.example.jrd48.chat.filemanagement.MyGridView;
import com.example.jrd48.chat.filemanagement.TimeComparator;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.group.DBHelperTeamList;
import com.example.jrd48.chat.group.DBManagerTeamList;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.luobin.dvr.R;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static com.example.jrd48.service.protocol.root.ReceiverProcesser.getMyDataRoot;

/**
 * Created by Administrator on 2017/4/24 0024.
 */

public class DocumentAllActivity extends BaseActivity implements View.OnClickListener {

    private ListView mListView;
    //    private PullToRefreshListView mPullRefreshListView;
    private TextView mTvGroups;
    private TextView mTvTypes;
    private TextView mTvTimes;
    private LinearLayout mGroups;
    private LinearLayout mTypes;
    private LinearLayout mTimes;
    private MyGridView mMyGridView;
    private List<TeamInfo> mTeamInfo = new ArrayList<>();
    private List<AppliedFriends> mLinkmanList = new ArrayList<>();
    private DialogGroupAdapter mDialogGroupAdapter;
    private ShowAdapter showAdapter;
    byte[] byteMap;
    String myPhone;
    private List<Msg> mMsgList = new ArrayList<>();
    List<MsgPicInfo> msgPicInfos = new ArrayList<>();
    private List<ViewMsg> viewMsg = new ArrayList<>();

    private String mGroupType = MenuType.ALL; //默认为全部
    private String mShowType = MenuType.ALL; //默认为全部
    private String mType = MenuType.ALL; //默认为全部
    private String mTimeType = MenuType.REVERSE; //默认为倒序
    Handler handler = new Handler();

    class ViewMsg {
        public Msg msg;
        public List<Msg> msgList;
        private String time;
        private String type;   //  all :显示全部   pic:显示图片  video:显示视频

        public ViewMsg() {
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);
        initData();
        initToolbar();
        initView();
        getDBGroup();
        getDBLinkman();
        handler.post(new Runnable() {
            @Override
            public void run() {
                getAllData();
            }
        });

    }

    private void initData() {
        SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        myPhone = preferences.getString("phone", "");
    }

    private long group;
    private List<Long> groupList = new ArrayList<>();
    private List<String> linkManPhoneList = new ArrayList<>();
    private String linkManPhone;
    private SQLiteDatabase dbMsg;
    private TeamRecordHelper teamRecordHelper;
    private LinkmanRecordHelper linkmanRecordHelper;

    private void initGroupData(String groupType, String type, String timeType) {

        if (groupType == MenuType.ALL) {
            for (Long phone : groupList) {
                refreshGroupData(type, timeType, phone);
            }
        } else {
            refreshGroupData(type, timeType, group);
        }
        if (timeType.equals(MenuType.ORDER)) {
            Collections.reverse(mMsgList);//顺序排序
        } else {
            TimeComparator comparator = new TimeComparator("");
            Collections.sort(mMsgList, comparator);
        }
        showAdapter.notifyDataSetChanged();
    }

    private void refreshGroupData(String type, String timeType, long group) {
        Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
        int yearNow = c.get(Calendar.YEAR);
        int monthNow = c.get(Calendar.MONTH);
        int dateNow = c.get(Calendar.DATE);
        String time;
        String allTime;
        teamRecordHelper = new TeamRecordHelper(DocumentAllActivity.this, myPhone + group + "TeamMsgShow.dp", null);
        dbMsg = teamRecordHelper.getWritableDatabase();
        Cursor cursor = dbMsg.query("TeamRecord", null, null, null, null, null, null);

        viewMsg.clear();
        int i = 0;
        if (cursor.moveToLast()) {
            do {
                int year = cursor.getInt(cursor.getColumnIndex("year"));
                int month = cursor.getInt(cursor.getColumnIndex("month"));
                int date = cursor.getInt(cursor.getColumnIndex("date"));
                int hour = cursor.getInt(cursor.getColumnIndex("hour"));
                int minute = cursor.getInt(cursor.getColumnIndex("minute"));
                allTime = year + "-" + (month + 1) + "-" + date + "-" + hour + "-" + minute;
                if (yearNow == year) {
                    if (monthNow == month && dateNow == date) {
                        time = String.format("%02d:%02d", hour, minute);
                    } else {
                        time = (month + 1) + "月" + date + "日";
                    }
                } else {
                    time = year + "年" + (month + 1) + "月" + date + "日";
                }
                int msgType = cursor.getInt(cursor.getColumnIndex("msg_type"));
                int msgSend = cursor.getInt(cursor.getColumnIndex("msg_send"));
                int sentState = cursor.getInt(cursor.getColumnIndex("send_state"));
                long msgID = cursor.getLong(cursor.getColumnIndex("service_id"));
                //Log.i("chatjrd","返回的——msgId:"+msgID+"        id:"+cursor.getInt(cursor.getColumnIndex("id")));
                String fromPhone = cursor.getString(cursor.getColumnIndex("phone"));
                Msg msg;
                if (msgType == 1) {
                    String picAddress = getMyDataRoot(DocumentAllActivity.this) + cursor.getString(cursor.getColumnIndex("pic_address"));
                    Bitmap bmp = BitmapFactory.decodeFile(picAddress);
                    if (msgSend == 0) {
                        msg = new Msg(bmp, Msg.TYPE_SENT_IMAGE, time, allTime, sentState, cursor.getInt(cursor.getColumnIndex("id")), fromPhone, msgID, group);
                        msg.setPercent(cursor.getInt(cursor.getColumnIndex("percent")));
                    } else {
                        msg = new Msg(bmp, Msg.TYPE_RECEIVED_IMAGE, time, allTime, sentState, cursor.getInt(cursor.getColumnIndex("id")), fromPhone, msgID, group);
                    }
                    msg.setAddress(cursor.getString(cursor.getColumnIndex("pic_address_true")));
                    if (type.equals(MenuType.ALL) || type.equals(MenuType.PIC)) {
                        mMsgList.add(msg);
                    }
//                    ViewMsg v = new ViewMsg();
//                    v.msg = msg;
//                    viewMsg.add(v);
               /* } else if (msgType == 2) {
                    String text = cursor.getString(cursor.getColumnIndex("msg"));
                    msg = new Msg(text, Msg.TYPE_MSG_RECORD, time, 0, 0, fromPhone, msgID, group);
                } else if (msgType == 3) {
                    if (msgSend == 0) {
                        msg = new Msg("", Msg.TYPE_MSG_MY_CANCEL, time, 0, 0, fromPhone, msgID, group);
                    } else {
                        msg = new Msg("", Msg.TYPE_MSG_CANCEL, time, 0, 0, fromPhone, msgID, group);
                    }*/
                } else if (msgType == ProtoMessage.MsgType.mtVideoFile_VALUE) {
                    String picAddress = getMyDataRoot(DocumentAllActivity.this) + cursor.getString(cursor.getColumnIndex("pic_address"));
                    Bitmap bmp = BitmapFactory.decodeFile(picAddress);
                    if (msgSend == 0) {
                        msg = new Msg(bmp, Msg.TYPE_SENT_VIDEO, time, allTime, sentState, cursor.getInt(cursor.getColumnIndex("id")), fromPhone, msgID, group);
                        msg.setPercent(cursor.getInt(cursor.getColumnIndex("percent")));
                    } else {
                        msg = new Msg(bmp, Msg.TYPE_RECEIVED_VIDEO, time, allTime, sentState, cursor.getInt(cursor.getColumnIndex("id")), fromPhone, msgID, group);
                    }
                    msg.setAddress(cursor.getString(cursor.getColumnIndex("pic_address_true")));
                    if (type.equals(MenuType.ALL) || type.equals(MenuType.VIDEO)) {
                        mMsgList.add(msg);
                    }
//                    ViewMsg v = new ViewMsg();
//                    v.msg = msg;
//                    viewMsg.add(v);
                }
               /* else {
                    String text = cursor.getString(cursor.getColumnIndex("msg"));
                    if (msgSend == 0) {
                        msg = new Msg(text, Msg.TYPE_SENT, time, sentState, cursor.getInt(cursor.getColumnIndex("id")), fromPhone, msgID, group);
                    } else {
                        msg = new Msg(text, Msg.TYPE_RECEIVED, time, sentState, cursor.getInt(cursor.getColumnIndex("id")), fromPhone, msgID, group);
                    }
                }*/

//                ViewMsg viewMsg = new ViewMsg();
//                viewMsg.msgList = msgList;
//                viewMsg.time =
//                viewMsg.add();
                i++;
                if (i > 100) {
                    break;
                }
            } while (cursor.moveToPrevious());


        }

//        list.setSelection(msgList.size());
        cursor.close();
        dbMsg.close();
    }


    private void initLinkmanData(String linkmanType, String type, String timeType) {
        if (linkmanType == MenuType.ALL) {
            for (String phone : linkManPhoneList) {
                refreshLinkmanData(type, timeType, phone);
            }
        } else {
            refreshLinkmanData(type, timeType, linkManPhone);
        }
        if (timeType.equals(MenuType.ORDER)) {
            Collections.reverse(mMsgList);//顺序排序
        } else {
            TimeComparator comparator = new TimeComparator("");
            Collections.sort(mMsgList, comparator);
        }
        showAdapter.notifyDataSetChanged();
    }

    private void refreshLinkmanData(String type, String timeType, String linkManPhone) {
        Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
        int yearNow = c.get(Calendar.YEAR);
        int monthNow = c.get(Calendar.MONTH);
        int dateNow = c.get(Calendar.DATE);
        String time;
        String allTime;
        linkmanRecordHelper = new LinkmanRecordHelper(DocumentAllActivity.this, myPhone + linkManPhone + "LinkmanMsgShow.dp", null);
        dbMsg = linkmanRecordHelper.getWritableDatabase();
        Cursor cursor = dbMsg.query("LinkmanRecord", null, null, null, null, null, null);

        int i = 0;
        if (cursor.moveToLast()) {
            do {
                int year = cursor.getInt(cursor.getColumnIndex("year"));
                int month = cursor.getInt(cursor.getColumnIndex("month"));
                int date = cursor.getInt(cursor.getColumnIndex("date"));
                int hour = cursor.getInt(cursor.getColumnIndex("hour"));
                int minute = cursor.getInt(cursor.getColumnIndex("minute"));
                allTime = year + "-" + (month + 1) + "-" + date + "-" + hour + "-" + minute;
                if (yearNow == year) {
                    if (monthNow == month && dateNow == date) {
                        time = String.format("%02d:%02d", hour, minute);
                    } else {
                        time = (month + 1) + "月" + date + "日";
                    }
                } else {
                    time = year + "年" + (month + 1) + "月" + date + "日";
                }
                int msgType = cursor.getInt(cursor.getColumnIndex("msg_type"));
                int msgSend = cursor.getInt(cursor.getColumnIndex("msg_send"));
                int sentState = cursor.getInt(cursor.getColumnIndex("send_state"));
                long msgID = cursor.getLong(cursor.getColumnIndex("service_id"));
                //Log.i("chatjrd","返回的——msgId:"+msgID+"        id:"+cursor.getInt(cursor.getColumnIndex("id")));
                Msg msg;
                if (msgType == 1) {
                    String picAddress = getMyDataRoot(DocumentAllActivity.this) + cursor.getString(cursor.getColumnIndex("pic_address"));
                    Bitmap bmp = BitmapFactory.decodeFile(picAddress);
                    if (msgSend == 0) {
                        msg = new Msg(bmp, Msg.TYPE_SENT_IMAGE, time, allTime, sentState, cursor.getInt(cursor.getColumnIndex("id")), myPhone, msgID, 0);
                        msg.setPercent(cursor.getInt(cursor.getColumnIndex("percent")));
                    } else {
                        msg = new Msg(bmp, Msg.TYPE_RECEIVED_IMAGE, time, allTime, sentState, cursor.getInt(cursor.getColumnIndex("id")), linkManPhone, msgID, 0);
                    }
                    msg.setAddress(cursor.getString(cursor.getColumnIndex("pic_address_true")));
                    if (type.equals(MenuType.ALL) || type.equals(MenuType.PIC)) {
                        mMsgList.add(msg);
                    }
                } else if (msgType == ProtoMessage.MsgType.mtVideoFile_VALUE) {
                    String picAddress = getMyDataRoot(DocumentAllActivity.this) + cursor.getString(cursor.getColumnIndex("pic_address"));
                    Bitmap bmp = BitmapFactory.decodeFile(picAddress);
                    if (msgSend == 0) {
                        msg = new Msg(bmp, Msg.TYPE_SENT_VIDEO, time, allTime, sentState, cursor.getInt(cursor.getColumnIndex("id")), myPhone, msgID, 0);
                        msg.setPercent(cursor.getInt(cursor.getColumnIndex("percent")));
                    } else {
                        msg = new Msg(bmp, Msg.TYPE_RECEIVED_VIDEO, time, allTime, sentState, cursor.getInt(cursor.getColumnIndex("id")), linkManPhone, msgID, 0);
                    }
                    msg.setAddress(cursor.getString(cursor.getColumnIndex("pic_address_true")));
                    if (type.equals(MenuType.ALL) || type.equals(MenuType.VIDEO)) {
                        mMsgList.add(msg);
                    }
                }
                i++;
                if (i > 100) {
                    break;
                }
            } while (cursor.moveToPrevious());


        }

//        list.setSelection(msgList.size());
        cursor.close();
        dbMsg.close();
    }

    private void initView() {
        mTvGroups = (TextView) findViewById(R.id.tv_groups);
        mTvTypes = (TextView) findViewById(R.id.tv_types);
        mTvTimes = (TextView) findViewById(R.id.tv_times);

        mGroups = (LinearLayout) findViewById(R.id.ll_groups);
        mTypes = (LinearLayout) findViewById(R.id.ll_types);
        mTimes = (LinearLayout) findViewById(R.id.ll_time);

        mGroups.setOnClickListener(this);
        mTypes.setOnClickListener(this);
        mTimes.setOnClickListener(this);

        mMyGridView = (MyGridView) findViewById(R.id.gridview_show);
        showAdapter = new ShowAdapter(this, mMsgList);
        mMyGridView.setAdapter(showAdapter);

//        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_document_refresh_list);
//        mListView = mPullRefreshListView.getRefreshableView();
//        showAdapter = new ShowAdapter(this,mMsgList);
//        mListView.setAdapter(showAdapter);
//        mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
//
//            @Override
//            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
//                String label = DateUtils.formatDateTime(DocumentAllActivity.this, System.currentTimeMillis(),
//                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
//
//                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
//                Log.i("DocumentAllActivity", "------PullToRefreshBase-------" + "刷新了");
//
//                freshCompelte();
//            }
//        });

//        registerForContextMenu(mListView);

        mMyGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Msg msg = mMsgList.get(position);
                //TODO 点击视频
                if (msg.getType() == Msg.TYPE_SENT_VIDEO || msg.getType() == Msg.TYPE_RECEIVED_VIDEO) {
                    if (msg.getBitmap() != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        msg.getBitmap().compress(Bitmap.CompressFormat.JPEG, 50, baos);
                        byteMap = baos.toByteArray();
                    }
                    Intent i = new Intent(view.getContext(), VideoActivity.class);
                    i.putExtra("msgid", msg.getMsgID());
                    i.putExtra("teamid", msg.getTeamID());
                    i.putExtra("sn", msg.getSn());
                    i.putExtra("otherphone", msg.getPhone());
                    i.putExtra("address", msg.getAddress());
                    i.putExtra("bitmap", byteMap);
                    view.getContext().startActivity(i);
//                    ToastR.setToast(DocumentAllActivity.this,"点击了:"+mMsgList.get(position).getPhone() + " 视频");
                }
                //TODO 点击图片
                if (msg.getType() == Msg.TYPE_SENT_IMAGE || msg.getType() == Msg.TYPE_RECEIVED_IMAGE) {
                    msgPicInfos.clear();
                    int currentNum = 0;
                    for (Msg msg1 : mMsgList) {
                        if (msg1.getType() == Msg.TYPE_SENT_IMAGE ||
                                msg1.getType() == Msg.TYPE_RECEIVED_IMAGE) {
                            MsgPicInfo mp = new MsgPicInfo(msg1);
                            if (msg1.getBitmap() != null) {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                msg1.getBitmap().compress(Bitmap.CompressFormat.JPEG, 50, baos);
                                mp.setPictures(baos.toByteArray());
                            }
                            msgPicInfos.add(mp);
                            if (msg1.getMsgID() == msg.getMsgID()) {
                                currentNum = msgPicInfos.size() - 1;
                            }
                        }
                    }
                    Intent i = new Intent(view.getContext(), ShowPhotoActivity.class);
                    i.putExtra("curr_index", currentNum);
                    i.putExtra("all_pic_info", (Serializable) msgPicInfos);
                    view.getContext().startActivity(i);
//                    ToastR.setToast(DocumentAllActivity.this,"点击了:"+mMsgList.get(position).getPhone() + " 图片");
                }
            }
        });
    }

//    private void freshCompelte() {
//        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
//        MyService.start(this, ProtoMessage.Cmd.cmdAppliedTeamList.getNumber(), builder.build());
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(AppliedGroupListProcesser.ACTION);
//        new TimeoutBroadcast(this, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
//            @Override
//            public void onTimeout() {
//                if (mPullRefreshListView != null)
//                    mPullRefreshListView.onRefreshComplete();
//            }
//
//            @Override
//            public void onGot(Intent i) {
//                if (mPullRefreshListView != null) {
//                    mPullRefreshListView.onRefreshComplete();
//                }
//            }
//        });
//    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setNavigationIcon(R.drawable.btn_back);//设置Navigatiicon 图标
        toolbar.setTitle(null);
        ((TextView)toolbar.findViewById(R.id.custom_title)).setText("我的文件");
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
    }

    private void getDBGroup() {
        try {
            DBManagerTeamList db = new DBManagerTeamList(this, true, DBTableName.getTableName(this, DBHelperTeamList.NAME));
            mTeamInfo = db.getTeams();
            db.closeDB();

            groupList.clear();
            for (TeamInfo tm : mTeamInfo) {
                groupList.add(tm.getTeamID());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getDBLinkman() {
        try {
            DBManagerFriendsList db = new DBManagerFriendsList(DocumentAllActivity.this, DBTableName.getTableName(DocumentAllActivity.this, DBHelperFriendsList.NAME));
            mLinkmanList = db.getFriends(false);
            db.closeDB();

            linkManPhoneList.clear();
            for (AppliedFriends af : mLinkmanList) {
                linkManPhoneList.add(af.getPhoneNum());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_groups:
                dialogSelectType();
                break;
            case R.id.ll_types:
                dialogTypeMenu(mShowType);
                break;
            case R.id.ll_time:
                dialogTimeMenu(mShowType);
                break;
            default:
                break;
        }
    }

    private void dialogSelectType() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.document_type));
        List<String> list = new ArrayList<String>();
        final String documentAll = getResources().getString(R.string.document_all_type);
        final String documentGroup = getResources().getString(R.string.document_group_type);
        final String documentLinkman = getResources().getString(R.string.document_linkman_type);

        list.add(documentAll);
        list.add(documentGroup);
        list.add(documentLinkman);

        final String[] array = (String[]) list.toArray(new String[list.size()]);

        builder.setItems(array,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (documentAll.equals(array[which])) {
                            mShowType = MenuType.ALL;
                            mTvGroups.setText(array[which]);
                            getAllData();

//                            mMsgList.clear();
//                            showAdapter.notifyDataSetChanged();
                        } else if (documentGroup.equals(array[which])) {
                            mShowType = MenuType.GROUP;
                            dialogGroupMenu(mShowType);
                        } else {
                            mShowType = MenuType.LINKMAN;
                            dialogGroupMenu(mShowType);
                        }
                    }
                });
        builder.create().show();
    }

    private void getAllData() {
        mMsgList.clear();
        initGroupData(mShowType, mType, mTimeType);
        initLinkmanData(mShowType, mType, mTimeType);
    }

    private void dialogTimeMenu(final String showType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.document_type));
        List<String> list = new ArrayList<String>();
        final String documentReverse = getResources().getString(R.string.document_reverse);
        final String documentOrder = getResources().getString(R.string.document_order);

        list.add(documentReverse);
        list.add(documentOrder);

        final String[] array = (String[]) list.toArray(new String[list.size()]);

        builder.setItems(array,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (documentReverse.equals(array[which])) {
                            mTimeType = MenuType.REVERSE;
                        } else if (documentOrder.equals(array[which])) {
                            mTimeType = MenuType.ORDER;
                        }
                        mTvTimes.setText(array[which]);
//                        if (showType.equals(MenuType.GROUP)) {
//                            mMsgList.clear();
//                            initGroupData(mGroupType, mType, mTimeType);
//                        } else if(showType.equals(MenuType.LINKMAN)){
//                            mMsgList.clear();
//                            initLinkmanData(mGroupType, mType, mTimeType);
//                        }
                        if (mTimeType.equals(MenuType.ORDER)) {
//                            Collections.reverse(mMsgList);//顺序排序
                            TimeComparator comparator = new TimeComparator("order");
                            Collections.sort(mMsgList, comparator);
                        } else {
                            TimeComparator comparator = new TimeComparator("");
                            Collections.sort(mMsgList, comparator);
                        }
                        showAdapter.notifyDataSetChanged();
                    }
                });
        builder.create().show();
    }

    private void dialogTypeMenu(final String showType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.document_type));
        List<String> list = new ArrayList<String>();
        final String documentAllStr = getResources().getString(R.string.document_all);
        final String documentPicStr = getResources().getString(R.string.document_pic);
        final String documentVideoStr = getResources().getString(R.string.document_video);

        list.add(documentAllStr);
        list.add(documentPicStr);
        list.add(documentVideoStr);

        final String[] array = (String[]) list.toArray(new String[list.size()]);

        builder.setItems(array,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (documentAllStr.equals(array[which])) {
                            mType = MenuType.ALL;
                        } else if (documentPicStr.equals(array[which])) {
                            mType = MenuType.PIC;
                        } else if (documentVideoStr.equals(array[which])) {
                            mType = MenuType.VIDEO;
                        }
                        Log.i("jim", mType);
                        if (showType.equals(MenuType.GROUP)) {
                            mMsgList.clear();
                            initGroupData(mGroupType, mType, mTimeType);
                        } else if (showType.equals(MenuType.LINKMAN)) {
                            mMsgList.clear();
                            initLinkmanData(mGroupType, mType, mTimeType);
                        } else {
                            getAllData();
                        }
                        mTvTypes.setText(array[which]);
                    }
                });
        builder.create().show();
    }

    private void dialogGroupMenu(final String showType) {
        final android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this, R.style.BottomNoTitleDialogStyle).create();
        dialog.setCancelable(true);
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.dialog_group_menu);
        final TextView tvTitle = (TextView) window.findViewById(R.id.dialog_title);
        final TextView tvAllGroup = (TextView) window.findViewById(R.id.tv_dialog_all);
        final TextView tvCloseDialog = (TextView) window.findViewById(R.id.tv_dialog_close);
        final ListView listView = (ListView) window.findViewById(R.id.dialog_list);
        if (showType.equals(MenuType.GROUP)) {
            tvTitle.setText(getResources().getString(R.string.document_group_type));
        } else if (showType.equals(MenuType.LINKMAN)) {
            tvTitle.setText(getResources().getString(R.string.document_linkman_type));
        } else {
            tvTitle.setText(getResources().getString(R.string.document_all));
        }
        mDialogGroupAdapter = new DialogGroupAdapter(this, mTeamInfo, mLinkmanList, showType);
        listView.setAdapter(mDialogGroupAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mGroupType = MenuType.SINGLE;
                if (showType.equals(MenuType.GROUP)) {
                    mTvGroups.setText(mTeamInfo.get(i).getTeamName());
                    group = mTeamInfo.get(i).getTeamID();
                    mMsgList.clear();
                    initGroupData(mGroupType, mType, mTimeType);
                } else if (showType.equals(MenuType.LINKMAN)) {
                    String name = mLinkmanList.get(i).getNickName();
                    if (name == null || name.equals("")) {
                        name = mLinkmanList.get(i).getUserName();
                    }
                    mTvGroups.setText(name);
                    linkManPhone = mLinkmanList.get(i).getPhoneNum();
                    mMsgList.clear();
                    initLinkmanData(mGroupType, mType, mTimeType);
                }
                dialog.dismiss();
            }
        });

        tvAllGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGroupType = MenuType.ALL;
                if (showType.equals(MenuType.GROUP)) {
                    mTvGroups.setText(getResources().getString(R.string.document_all_group_type));
                    mMsgList.clear();
                    initGroupData(mGroupType, mType, mTimeType);
                } else if (showType.equals(MenuType.LINKMAN)) {
                    mTvGroups.setText(getResources().getString(R.string.document_all_linkman_type));
                    mMsgList.clear();
                    initLinkmanData(mGroupType, mType, mTimeType);
                }
                dialog.dismiss();
            }
        });

        tvCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private class DialogGroupAdapter extends BaseAdapter {

        private LayoutInflater layoutInflater;
        private List<TeamInfo> mInfo;
        private List<AppliedFriends> linkmanList;
        private String showType;
        private Context mContext;

        public DialogGroupAdapter(Context context, List<TeamInfo> mTeamInfo, List<AppliedFriends> mLinkmanList, String showType) {
            layoutInflater = LayoutInflater.from(context);
            this.mContext = context;
            this.mInfo = mTeamInfo;
            this.linkmanList = mLinkmanList;
            this.showType = showType;
        }

        @Override
        public int getCount() {
            if (showType.equals(MenuType.GROUP)) {
                return mInfo.size();
            } else if (showType.equals(MenuType.LINKMAN)) {
                return linkmanList.size();
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.dialog_listitem_group, null);
                holder.orgPhoto = (ImageView) convertView.findViewById(R.id.call_invite_photo);
                holder.teamName = (TextView) convertView.findViewById(R.id.tv_title);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (showType.equals(MenuType.GROUP)) {
                TeamInfo teamInfo = mInfo.get(position);
                Bitmap bitmap = GlobalImg.getImage(DocumentAllActivity.this, "team" + teamInfo.getTeamID());
                if (bitmap == null) {
                    holder.orgPhoto.setBackgroundResource(R.drawable.group);
                } else {
                    holder.orgPhoto.setImageBitmap(bitmap);
                }
                holder.teamName.setText(teamInfo.getTeamName());
                holder.teamName.setTextColor(getResources().getColor(R.color.textColor));

            } else if (showType.equals(MenuType.LINKMAN)) {
                AppliedFriends appliedFriends = linkmanList.get(position);
                if (GlobalImg.getImage(DocumentAllActivity.this, appliedFriends.getPhoneNum()) == null) {
                    holder.orgPhoto.setImageBitmap(BitmapFactory.decodeResource(DocumentAllActivity.this.getResources(), R.drawable.default_useravatar));
                } else {
                    holder.orgPhoto.setImageBitmap(GlobalImg.getImage(DocumentAllActivity.this, appliedFriends.getPhoneNum()));
                }
                String name = appliedFriends.getNickName();
                if (name == null || name.equals("")) {
                    name = appliedFriends.getUserName();
                }
                holder.teamName.setText(name);
                holder.teamName.setTextColor(getResources().getColor(R.color.textColor));
            } else {

            }

            return convertView;
        }

        public class ViewHolder {
            public ImageView orgPhoto;
            public TextView teamName;
        }
    }

    private class ShowAdapter extends BaseAdapter {

        private LayoutInflater layoutInflater;
        private List<Msg> msgList;
        private Context mContext;

        public ShowAdapter(Context context, List<Msg> mMsgList) {
            layoutInflater = LayoutInflater.from(context);
            this.mContext = context;
            this.msgList = mMsgList;
        }

        @Override
        public int getCount() {
            return msgList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ViewHolder holder = null;
            Msg msg = msgList.get(position);
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.photo_gridview, null);
//                holder.orgPhoto = (ImageView) convertView.findViewById(R.id.call_invite_photo);
                holder.teamName = (TextView) convertView.findViewById(R.id.textViewPhotoDate);
//                holder.mGridView = (MyGridView) convertView.findViewById(R.id.gridview_show);
                holder.imageView = (ImageView) convertView.findViewById(R.id.image);
                holder.player = (ImageView) convertView.findViewById(R.id.player);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

//            Bitmap bitmap = GlobalImg.getImage(DocumentAllActivity.this, "team" + mInfo.get(position).getTeamID());
//            if(bitmap == null){
//                holder.orgPhoto.setBackgroundResource(R.drawable.group);
//            } else {
//                holder.orgPhoto.setImageBitmap(bitmap);
//            }
            holder.teamName.setText(msg.getTime());
//            holder.teamName.setTextColor(getResources().getColor(R.color.textColor));
            if (msg.getType() == Msg.TYPE_SENT_VIDEO || msg.getType() == Msg.TYPE_RECEIVED_VIDEO) {
                holder.imageView.setImageBitmap(msg.getBitmap());
                holder.player.setVisibility(View.VISIBLE);
            }
            if (msg.getType() == Msg.TYPE_SENT_IMAGE || msg.getType() == Msg.TYPE_RECEIVED_IMAGE) {
                holder.imageView.setImageBitmap(msg.getBitmap());
                holder.player.setVisibility(View.GONE);
            }
//            showHolder(holder, position);
            return convertView;
        }

        public class ViewHolder {
            //            public ImageView orgPhoto;
            public TextView teamName;
            public MyGridView mGridView;
            public ImageView imageView;
            public ImageView player;
        }
    }

    private void showHolder(final ShowAdapter.ViewHolder holder, int position) {
//        final ArrayList<CarPhotoBmp> p = mDat.get(position);
        final List<MsgPicInfo> m = new ArrayList<>();
//
//        Calendar c1 = Calendar.getInstance();
//        c1.setTimeInMillis(p.get(0).getPhoto().getPhotoTime().getTime());
//
//        boolean bNewDate = true;
//        if (position > 0) {
//            CarPhoto lastPhoto = mDat.get(position - 1).get(0).getPhoto();
//            Calendar c2 = Calendar.getInstance();
//            c2.setTimeInMillis(lastPhoto.getPhotoTime().getTime());
//
//            if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
//                    && c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH)) {
//                bNewDate = false;
//            }
//
//        }
//        if (bNewDate) {
//            holder.textDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(p.get(0).getPhoto().getPhotoTime()));
////				holder.textDate.setHeight(75);
//        } else {
////				holder.textDate.setText("");
////				holder.textDate.setHeight(0);
//            holder.textDate.setVisibility(View.GONE);
//        }
//        if (mDat.size() > 0) {
//            holder.mRelativeLayout.setVisibility(View.VISIBLE);

        initInfoImages(holder.mGridView, m);
//        } else {
//            holder.mRelativeLayout.setVisibility(View.GONE);
//            holder.mGridView.setAdapter(null);
//        }

    }

    public void initInfoImages(MyGridView mGridView, final List<MsgPicInfo> p) {
        if (mGridView == null) {
            return;
        }

        GridViewAdapter nearByInfoImgsAdapter = new GridViewAdapter(DocumentAllActivity.this, p);
        mGridView.setAdapter(nearByInfoImgsAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int currentNum = 0;
                //TODO 点击图片
//                for (Msg msg : list) {
//                    if (msg.getType() == Msg.TYPE_SENT_IMAGE ||
//                            msg.getType() == Msg.TYPE_RECEIVED_IMAGE) {
//                        MsgPicInfo mp = new MsgPicInfo(msg);
//                        if (msg.getBitmap() != null) {
//                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                            msg.getBitmap().compress(Bitmap.CompressFormat.JPEG, 50, baos);
//                            mp.setPictures(baos.toByteArray());
//                        }
//                        msgPicInfos.add(mp);
//                        if (msg.getMsgID() == msgId) {
//                            currentNum = msgPicInfos.size() - 1;
//                        }
//                    }
//                }
//                if ()
                Intent i = new Intent(view.getContext(), ShowPhotoActivity.class);
                i.putExtra("curr_index", currentNum);
                i.putExtra("all_pic_info", (Serializable) msgPicInfos);
//				i.putExtra("msgid", msgId);
//				i.putExtra("teamid", teamId);
//				i.putExtra("sn", sn);
//				i.putExtra("otherphone", otherphone);
//				i.putExtra("address", address);
                view.getContext().startActivity(i);

//                ((FirstActivity) getContext()).photo_or_no = true;

                //TODO 点击视频
//                if (bitmap != null) {
//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
//                    byteMap = baos.toByteArray();
//                }
//                Intent i = new Intent(view.getContext(), VideoActivity.class);
//                i.putExtra("msgid", msgId);
//                i.putExtra("teamid", teamId);
//                i.putExtra("sn", sn);
//                i.putExtra("otherphone", otherphone);
//                i.putExtra("address", address);
//                i.putExtra("bitmap", byteMap);
//                view.getContext().startActivity(i);


//                if (!isbEditMode()) {
//                    List<ShowPhotoParam> urls = getUrlList(p, position);
//                    CarPhotoActivityParam param = new CarPhotoActivityParam();
//                    param.setUrls(urls);
//
//                    if (mData != null) {
//                        for (int i = 0; i < mData.size(); i++) {
//                            if (mData.get(i).getPhoto().getPhotoID() == p.get(position).getPhoto().getPhotoID()) {
//                                param.setCurrIndex(i);
//                                Intent intent = new Intent(getActivity(), CarPhotoShow.class);
//
//                                intent.putExtra("photoParam", param);
//                                startActivity(intent);
//                                break;
//                            }
//                        }
//                    } else {
//                        return;
//                    }
//                    // mData.get
//
//                } else {
//                    CarPhotoBmp cp = p.get(position);
//                    cp.setbChecked(!cp.isbChecked());
//                    sumChckedImage();
//                    mAdapter.notifyDataSetChanged();
//                }

            }

        });

    }

    public class GridViewAdapter extends BaseAdapter {
        private Context mContext;
        List<MsgPicInfo> mMsgPicInfo;

        public GridViewAdapter(Context mContext, List<MsgPicInfo> m) {
            super();
            this.mContext = mContext;
            this.mMsgPicInfo = m;
        }

        @Override
        public int getCount() {
            if (mMsgPicInfo == null) {
                return 0;
            } else {
                return mMsgPicInfo.size();
            }
        }

        @Override
        public Object getItem(int position) {
            if (mMsgPicInfo == null) {
                return null;
            } else {
                return mMsgPicInfo.get(position);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(this.mContext).inflate(R.layout.photo_gridview, null, false);
                holder.textPhotoID = (TextView) convertView.findViewById(R.id.textViewPhotoID);
                holder.textCarName = (TextView) convertView.findViewById(R.id.textViewCarName);
                holder.imageView = (ImageView) convertView.findViewById(R.id.image);
                holder.textView = (TextView) convertView.findViewById(R.id.textViewPhotoDate);
                holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar1);
                holder.imageCover = (ImageView) convertView.findViewById(R.id.imageViewCover);
                holder.imageSelete = (ImageView) convertView.findViewById(R.id.imageView2);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            showGridViewHolder(holder, position);

            return convertView;

        }

        private class ViewHolder {
            public ImageView imageView;
            public ImageView imageCover;
            public ImageView imageSelete;
            public CheckBox checkView;

            public TextView textView;
            public long photoID = -1;
            protected ProgressBar progressBar;
            public TextView textDate;
            public TextView textPhotoID;
            public TextView textCarName;
        }

        private void showGridViewHolder(ViewHolder holder, int position) {


//            final CarPhotoBmp p1 = mCar.get(position);
//
//            String carNumAll = null;
//            for (ViewAllCar viewAllCar : mUserCarList) {
//                if (viewAllCar.getUserCarID() == p1.getPhoto().getUserCarID()) {
//                    carNumAll = viewAllCar.getCarNumAll();
//                    holder.textCarName.setText(carNumAll);
//                    break;
//                }
//
//            }
//            try {
//                String strTime = new SimpleDateFormat("HH:mm").format(p1.getPhoto().getPhotoTime());
//                holder.textView.setText(strTime);
//            } catch (Exception e) {
//                holder.textView.setText("时间错误");
//            }
//            holder.textPhotoID.setText(String.valueOf(p1.getPhoto().getPhotoID()));
//
//            boolean bHasBmp = (p1.getBmp() != null);
//            if (bHasBmp) {
//                holder.imageView.setImageBitmap(p1.getBmp());
//            } else {
//                holder.imageView.setImageResource(R.drawable.ic_empty);
//            }
//
//            holder.progressBar.setVisibility(bHasBmp ? View.INVISIBLE : View.VISIBLE);
//            if (isbEditMode()) {
//                holder.imageSelete.setVisibility(p1.isbChecked() ? View.VISIBLE : View.GONE);
//                holder.imageCover.setVisibility(p1.isbChecked() ? View.VISIBLE : View.GONE);
//            } else {
//                holder.imageSelete.setVisibility(View.GONE);
//                holder.imageCover.setVisibility(View.GONE);
//            }
//
        }

    }

}
