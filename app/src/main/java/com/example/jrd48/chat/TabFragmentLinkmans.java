package com.example.jrd48.chat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.SQLite.MsgRecordHelper;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.AppliedFriendsList;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.friend.FriendsDetailsActivity;
import com.example.jrd48.chat.group.MsgTool;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.chat.search.Cn2Spell;
import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.ConnectionChangeReceiver;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.AppliedListProcesser;
import com.example.jrd48.service.protocol.root.AutoCloseProcesser;
import com.example.jrd48.service.protocol.root.DeleteFriendProcesser;
import com.example.jrd48.service.protocol.root.FriendsListProcesser;
import com.example.jrd48.service.protocol.root.GetFriendsStatusProcesser;
import com.example.jrd48.service.protocol.root.NotifyProcesser;
import com.example.jrd48.service.protocol.root.ReceiverProcesser;
import com.example.jrd48.service.protocol.root.SetFriendInfoProcesser;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.luobin.dvr.R;
import com.luobin.model.CallState;
import com.luobin.tool.OnlineSetTool;
import com.luobin.ui.FriendDetailsDialogActivity;
import com.luobin.ui.VideoOrVoiceDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by jrd48
 */

public class TabFragmentLinkmans extends BaseLazyFragment {

    final private static String[] indexStr = {"☆", "#", "A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z"};

    List<AppliedFriends> personsS = new ArrayList<AppliedFriends>();
    List<AppliedFriends> personsStar = new ArrayList<AppliedFriends>();
    List<ViewFriendsMsg> mFriend = new ArrayList<ViewFriendsMsg>();
    LinkmansAdapter adapter;
    /**
     * 查看好友信息
     */
    ViewFriendsMsg mViewFriendsMsg;
    private PullRefreshLayout mPullRefreshView;
    private int MODIFY_FRIEND_STAR = 0;
    private int MODIFY_FRIEND_NICKNAME = 1;
    private int IS_FRIEND_STAR = 1;
    private int NOT_FRIEND_STAR = 0;
    private int SET_FRIEND_STAR = 1;
    private int CANCEL_FRIEND_STAR = 0;
    private int NO_FRIEND_STAR = 2;
    private ListView listView;
    private TextView tv_show;
    private TextView tv_wait;
    private ProgressDialog mWaitingProgressDialog;
    private FrameLayout all;
    private boolean host;
    private boolean checkPullRefresh = false;
    private String noSet = "未设置";
    /*private int[] personH = {R.drawable.u, R.drawable.u1, R.drawable.u2, R.drawable.u3, R.drawable.u4,
            R.drawable.u5, R.drawable.u6, R.drawable.u7, R.drawable.u, R.drawable.u1, R.drawable.u2, R.drawable.u3,
            R.drawable.u4, R.drawable.u5, R.drawable.u6, R.drawable.u7, R.drawable.u, R.drawable.u1, R.drawable.u2,
            R.drawable.u3, R.drawable.u4, R.drawable.u5, R.drawable.u6, R.drawable.u7};*/
    private List<Linkmans> persons = null;
    private List<Linkmans> newPersons = new ArrayList<>();
    private List<Linkmans> newPersonStar = new ArrayList<>();
    private int height;// 字体高度
    private boolean run = false;
    private IntentFilter filterRoom;
    private CloseRoomReceiiver closeRoomReceiiver;
    private MyConnectionChangeReceiver myConnectionChangeReceiver;
    private ChatStatusReceiver chatStatusReceiver;
    private String TAG = "linkman";
    String myPhone;
    private boolean isGetFriendStatus;
    private LinearLayout layoutIndex;
    //好友在线状态
    Set<String> onlineSet = new HashSet<String>();
    private HashMap<String, Integer> selector;// 存放含有索引字母的位置
    boolean isHasHeight = false;

    private BroadcastReceiver changgeImaReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String phone = intent.getStringExtra(ReceiverProcesser.PHONE_NUMBER);
            if (TextUtils.isEmpty(phone)) {
                return;
            }
            if (!phone.equals(myPhone)) {
                loadFriendsListFromCache();
            }
        }

    };
    private String deletePhone = null;
    private long TIME_LOAD_FRIEND_STATUS_DELAY = 60 * 1000L;
    private static final int MSG_LOAD_FRIEND_STATUS = 2000;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Log.d(TAG, "mHandler what = " + msg.what);
            switch (msg.what) {
                case MSG_LOAD_FRIEND_STATUS:
                    mHandler.removeMessages(MSG_LOAD_FRIEND_STATUS);
                    loadFriendStatus();
                    mHandler.sendEmptyMessageDelayed(MSG_LOAD_FRIEND_STATUS, TIME_LOAD_FRIEND_STATUS_DELAY);
                    break;
                default:
                    break;
            }
        }
    };
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TabFragmentLinkmans.class.getSimpleName(), "myReceiver:" + intent.getAction());
            deletePhone = intent.getStringExtra("phone");
            String str = intent.getStringExtra("delete");
            if (deletePhone != null && deletePhone.length() > 0) {
                if (str != null && str.equals(MainActivity.deleteStr)) {
                    refreshLocalData(deletePhone);
                    refreshStatus();
                } else {
                    updateLocalData();
                    loadFriendStatus();
                }
            } else {
                loadFriendsListFromNet();
            }
        }
    };

    private void setLinkManOnlineSetData() {
        onlineSet.clear();
        onlineSet = OnlineSetTool.getOnlineSet();
        adapter.clearOnlineStatus();
        refreshStatus();
    }

    private void refreshStatus() {
        if (onlineSet.size() > 0) {
            Iterator setList = onlineSet.iterator();
            while (setList.hasNext()) {
                String phone = (String) setList.next();
                Linkmans x = adapter.getItemByPhoneNum(phone);
                if (x != null) {
                    x.setOnline(true);
                    Log.d(TAG, "在线好友状态：" + phone);
                } else {
                    Log.w(TAG, "好友在线状态，未找到号码");
                }
            }
        }

        linkManSort();
    }

    @Override
    public void onDestroy() {
        try {
            if (mHandler != null) {
                mHandler.removeMessages(MSG_LOAD_FRIEND_STATUS);
            }
            if (closeRoomReceiiver != null) {
                getContext().unregisterReceiver(closeRoomReceiiver);
            }
            if (chatStatusReceiver != null) {
                getContext().unregisterReceiver(chatStatusReceiver);
            }
            if (changgeImaReceiver != null) {
                getContext().unregisterReceiver(changgeImaReceiver);
            }
            if (myReceiver != null) {
                getContext().unregisterReceiver(myReceiver);
            }
            if (myConnectionChangeReceiver != null) {
                getContext().unregisterReceiver(myConnectionChangeReceiver);
            }
            if (friendStatus != null) {
                getContext().unregisterReceiver(friendStatus);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        //TODO  获取好友状态
        if (run) {
            //  loadFriendsListFromCache();
        }
        try {
            DBManagerFriendsList db = new DBManagerFriendsList(getContext(), DBTableName.getTableName(getContext(), DBHelperFriendsList.NAME));
            List<AppliedFriends> list = db.getFriends(false);
            db.closeDB();
            convertViewFriendList(list);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            Log.w("pocdemo", "TabFragmentLinkmans.onResume adapter is null?????");
        }
        if (mHandler != null) {
            mHandler.sendEmptyMessage(MSG_LOAD_FRIEND_STATUS);
        }
//        if (run) {
//            if (adapter != null) {
//                adapter.notifyDataSetChanged();
//            } else {
//                Log.w("pocdemo", "TabFragmentLinkmans.onResume adapter is null?????");
//            }
//        }
    }

    //获取好友状态
    private void loadFriendStatus() {
        if (getActivity() == null) {
            Log.e(TAG, "getActivity is null");
            System.exit(0);
        }

        if (ConnUtil.isConnected(getActivity())) {
            Log.d(TAG, "获取全部好友在线状态");
            ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
            MyService.start(getContext(), ProtoMessage.Cmd.cmdGetFriendsStatus.getNumber(), builder.build());
            IntentFilter filter = new IntentFilter();
            filter.addAction(GetFriendsStatusProcesser.ACTION);
            new TimeoutBroadcast(getContext(), filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {

                @Override
                public void onTimeout() {
                    //  ToastR.setToast(getContext(), "连接超时");
                    if (!isGetFriendStatus) {
                        isGetFriendStatus = true;
                        loadFriendStatus();
                    }
                }

                @Override
                public void onGot(Intent i) {
                    int code = i.getIntExtra("error_code", -1);
                    if (code == ProtoMessage.ErrorCode.OK.getNumber()) {
//                        ToastR.setToast(getContext(), "获取在线好友成功");
                        ProtoMessage.MsgFriendsStatus re =
                                (ProtoMessage.MsgFriendsStatus) i.getSerializableExtra(GetFriendsStatusProcesser.STATUS_KEY);
                        OnlineSetTool.addList(re.getFriendsList());
                        refreshLocalFriendStatus(re);
                    } else {
                        fail(code);
                        if (code == ProtoMessage.ErrorCode.NOT_LOGIN_VALUE && !isGetFriendStatus) {
                            isGetFriendStatus = true;
                            loadFriendStatus();
                        }
                    }
                    mWaitingProgressDialog.hide();
                }
            });
        }
    }

    private BroadcastReceiver friendStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "收到好友在线广播 。。。。");
            String phone = intent.getStringExtra(NotifyProcesser.NUMBER);
            boolean online = intent.getBooleanExtra(NotifyProcesser.ONLINE_KEY, false);
            if (phone != null) {
                Log.d(TAG, "收到好友在线广播: " + phone + ", online: " + online);
                if (online) {
                    onlineSet.add(phone);
                } else {
                    onlineSet.remove(phone);
                }

                Linkmans x = adapter.getItemByPhoneNum(phone);
                if (x != null) {
                    x.setOnline(online);
                    linkManSort();
                } else {
                    Log.w(TAG, "not find in list: " + phone);
                }
            } else {
                Log.w(TAG, "...");
            }
        }
    };

    private void refreshLocalFriendStatus(ProtoMessage.MsgFriendsStatus re) {
        onlineSet.clear();
        adapter.clearOnlineStatus();

        if (re.getFriendsList().size() > 0) {
            for (int i = 0; i < re.getFriendsList().size(); i++) {
                String phone = re.getFriendsList().get(i).getPhoneNum();
                onlineSet.add(phone);
                Linkmans x = adapter.getItemByPhoneNum(phone);
                if (x != null) {
                    x.setOnline(true);
                    Log.d(TAG, "在线好友状态：" + phone);
                } else {
                    Log.w(TAG, "好友在线状态，未找到号码");
                }

            }
        }

        linkManSort();
    }

    private void linkManSort() {
        if (adapter.getList() != null && adapter.getList().size() > 0) {
            final LinkManPinyinComparator comparator = new LinkManPinyinComparator();
            Collections.sort(adapter.getList(), new Comparator<Linkmans>() {
                @Override
                public int compare(Linkmans x, Linkmans y) {
                    if (x.isOnline() != y.isOnline()) {
                        if (x.isOnline()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                    return comparator.compare(x, y);
                }
            });
        }
        adapter.notifyDataSetChanged();
    }

    class LinkManPinyinComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            Linkmans contact1 = (Linkmans) o1;
            Linkmans contact2 = (Linkmans) o2;
            String str = contact1.getLinkmanName();
            String str3 = contact2.getLinkmanName();
            String str1 = Cn2Spell.getPinYin(str);
            String str2 = Cn2Spell.getPinYin(str3);
            int flag = str1.compareTo(str2);

            return flag;
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
        layoutIndex.setBackgroundColor(Color.parseColor("#00ffffff"));
//        loadFriendsListFromNet();
        onlineSet.clear();
        onlineSet = OnlineSetTool.getOnlineSet();
        initAdapterData();

        ViewTreeObserver vto = layoutIndex.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                int height = layoutIndex.getMeasuredHeight();
                if (height > 0 && !isHasHeight){
                    isHasHeight = true;
                    getIndexView(height);
                }
                return true;
            }
        });

        all.setVisibility(View.VISIBLE);
        initListViewClick();

        initBroadcast();
        SharedPreferences preferences = getContext().getSharedPreferences("token", Context.MODE_PRIVATE);
        myPhone = preferences.getString("phone", "");

    }

    private void initBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.FRIEND_ACTION);
        filter.setPriority(Integer.MAX_VALUE);
        getMyActivity().registerReceiver(myReceiver, filter);

        filterRoom = new IntentFilter();
        closeRoomReceiiver = new CloseRoomReceiiver();
        filterRoom.addAction(AutoCloseProcesser.ACTION);
        getContext().registerReceiver(closeRoomReceiiver, filterRoom);
        filter = new IntentFilter();
        filter.addAction("ACTION.changeImage");
        getMyActivity().registerReceiver(changgeImaReceiver, filter);

        IntentFilter filt = new IntentFilter();
        chatStatusReceiver = new ChatStatusReceiver();
        filt.addAction("NotifyProcesser.ChatStatus");
        filt.addAction(GlobalStatus.NOTIFY_CALL_ACTION);
        getContext().registerReceiver(chatStatusReceiver, filt);

        //注册好友在线状态变化广播
        IntentFilter filterStatus = new IntentFilter();
        filterStatus.addAction(NotifyProcesser.FRIEND_STATUS_ACTION);
        getMyActivity().registerReceiver(friendStatus, filterStatus);

        //网络变化广播
        IntentFilter filterNetWork = new IntentFilter();
        myConnectionChangeReceiver = new MyConnectionChangeReceiver();
        filterNetWork.addAction(ConnectionChangeReceiver.NETWORK_CHANGE_ACTION);
        getMyActivity().registerReceiver(myConnectionChangeReceiver, filterNetWork);
    }

    private void loadFriendsListFromCache() {
        try {
            DBManagerFriendsList db = new DBManagerFriendsList(getContext(), DBTableName.getTableName(getContext(), DBHelperFriendsList.NAME));
            List<AppliedFriends> list = db.getFriends(false);
            db.closeDB();

//            if (list.size() <= 0) {
            loadFriendsListFromNet();
//            } else {
            convertViewFriendList(list);
//            setLinkManOnlineSetData();
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initAdapterData() {
        newPersonStar.clear();
        setData();
        String[] allNames = sortIndex(persons);
        sortList(allNames);
        selector = new HashMap<>();
        if (personsStar.size() > 0) {
/*            Linkmans link = new Linkmans("星标朋友", null, NOT_FRIEND_STAR, null, 0, null);
            newPersonStar.add(link);*/
            for (AppliedFriends af : personsStar) {
                //int linkmanImage, String linkmanName, String linkmanPhone, int friendStar, byte[] userPic
                Linkmans linkman = new Linkmans(af.getUserName(), af.getPhoneNum(), af.getFriendStar(), af.getUserPic(), af.getUserSex(), af.getNickName());
                newPersonStar.add(linkman);
            }
            //selector.put(indexStr[0], 0);
        }

        for (String anIndexStr : indexStr) {// 循环字母表，找出newPersons中对应字母的位置
            for (int t = 0; t < newPersons.size(); t++) {
                if (newPersons.get(t).getLinkmanName().equalsIgnoreCase(anIndexStr)) {
                    selector.put(anIndexStr, t);
                    break;
                }
            }
        }

        for (Linkmans ls : newPersons) {
            if (TextUtils.isEmpty(ls.getLinkmanPhone())) {
                continue;
            }
            newPersonStar.add(ls);
        }
        if (newPersonStar != null) {
            for (Linkmans ls : newPersonStar) {
                if (onlineSet.contains(ls.getLinkmanPhone())) {
                    ls.setOnline(true);
                } else {
                    ls.setOnline(false);
                }
            }
        }
        if (newPersonStar != null && newPersonStar.size() > 0) {
            final LinkManPinyinComparator comparator = new LinkManPinyinComparator();
            Collections.sort(newPersonStar, new Comparator<Linkmans>() {
                @Override
                public int compare(Linkmans x, Linkmans y) {
                    if (x.isOnline() != y.isOnline()) {
                        if (x.isOnline()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                    return comparator.compare(x, y);
                }
            });
        }
        if (!run) {
            run = true;
            loadFriendsListFromCache();
        }
    }

    @Override
    protected View initView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.linkman_layout, container,
                false);
        layoutIndex = (LinearLayout) view.findViewById(R.id.layout);
        mWaitingProgressDialog = new ProgressDialog(mContext);
        mWaitingProgressDialog.setMessage(getResources().getString(R.string.waiting_progress_dialog_friends_msg));
        mWaitingProgressDialog.setCanceledOnTouchOutside(false);
        mWaitingProgressDialog.setCancelable(false);
        if ((boolean) SharedPreferencesUtils.get(mContext, "member_booting", false)) {
            mWaitingProgressDialog.show();
            SharedPreferencesUtils.put(mContext, "member_booting", false);
        }
//        listView = (ListView) view.findViewById(R.id.listView);
        tv_show = (TextView) view.findViewById(R.id.tv);
        tv_wait = (TextView) view.findViewById(R.id.wait);
        all = (FrameLayout) view.findViewById(R.id.all);
        listView = (ListView) view.findViewById(R.id.pull_request_refresh_list);

        adapter = new LinkmansAdapter(getContext(), newPersonStar);
        listView.setAdapter(adapter);
        listView.setSelector(R.drawable.tab_list_item_selector);
        listView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int position = listView.getSelectedItemPosition();
                Log.v("wsDvr", "position:" + position);
                if (keyCode == KeyEvent.KEYCODE_F6 && event.getAction() == KeyEvent.ACTION_DOWN && position > 0) {

                    final Linkmans linkmans = (Linkmans) adapter.getItem(position);
                    if (linkmans != null) {
                        Intent intent = new Intent(mContext, FirstActivity.class);
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
                        VideoOrVoiceDialog dialog = new VideoOrVoiceDialog(mContext, intent);
                        dialog.show();
                    }
                }
                return false;
            }
        });
        mPullRefreshView = (PullRefreshLayout) view.findViewById(R.id.pullRefreshLayout);
        mPullRefreshView.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String label = DateUtils.formatDateTime(getContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        checkPullRefresh = true;
                        loadFriendsListFromNet();
                        refreshFriendsMenu();
                    }
                });
            }
        });

        registerForContextMenu(listView);
        refreshFriendsMenu();
        return view;
    }

    /**
     * 重新排序获得一个新的List集合
     */
    private void sortList(String[] allNames) {
        newPersons.clear();
        String tempName = null;
        for (String allName : allNames) {
            if (tempName == null || !tempName.equals(allName)) {
                if (!allName.matches("[A-Z]") && !allName.equals("#")) {
                    for (int j = 0; j < persons.size(); j++) {
                        if (allName.equals(persons.get(j).getLinkmanNamePinYin())) {
                            Linkmans x = persons.get(j);
                            Linkmans p = new Linkmans(x.getLinkmanName(), x.getLinkmanNamePinYin(),
                                    x.getLinkmanPhone(), x.getFriendStar(), x.getUserPic(),
                                    x.getLinkmanSex(), x.getRealName());
                            newPersons.add(p);
                        }
                    }
                } else {
                    newPersons.add(new Linkmans(allName));
                }
            }
            tempName = allName;
        }
    }


    /* @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // 在oncreate里面执行下面的代码没反应，因为oncreate里面得到的getHeight=0
        if (!flag) {// 这里为什么要设置个flag进行标记，我这里不先告诉你们，请读者研究，因为这对你们以后的开发有好处

            flag = true;
        }
    }*/


    /**
     * 获取排序后的新数据
     */
    public String[] sortIndex(List<Linkmans> persons) {
        TreeSet<String> set = new TreeSet<>();
        String[] pinYinNames = new String[persons.size()];
        for (int j = 0; j < persons.size(); j++) {
//            pinYinNames[j] = StringHelper.getPingYin(persons.get(j).getLinkmanName());
            pinYinNames[j] = ChineseToHanYuPYTest.convertChineseToPinyin(persons.get(j).getLinkmanName(), false);
            persons.get(j).setLinkmanNamePinYin(pinYinNames[j]);
            // 获取初始化数据源中的首字母，添加到set中
//            String s = StringHelper.getHeadChar(persons.get(j).getLinkmanName());
            String t = pinYinNames[j];
            String s = ChineseToHanYuPYTest.convertToUpperCase(t);
            if (s != null && s.length() > 1) {
                s = s.substring(0, 1);
            }
            if (s.matches("[a-zA-Z]")) {
                set.add(s);
            } else {
                set.add("#");
            }
        }
        String[] names = new String[persons.size() + set.size()];
        int i = 0;
        for (String string : set) {
            names[i] = string;
            i++;
        }
        // 将原数据拷贝到新数据中
        System.arraycopy
                (pinYinNames, 0, names, set.size(), pinYinNames.length);
        // 自动按照首字母排序
        Arrays.sort(names, String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    class ChatStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(GlobalStatus.NOTIFY_CALL_ACTION)) {
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            } else if (intent.hasExtra("chat_status")) {
                adapter.notifyDataSetChanged();
            }
        }
    }


    /**
     * 设置模拟数据
     */
    private void setData() {
        persons = new ArrayList<>();
        int i;
        //   int k = personsS.size()/2 ;
        for (int j = 0; j < personsS.size(); j++) {
            //  i=j*2;
//            Linkmans p1 = new Linkmans(personH[j], personsS.get(i), personsS.get(i+1));
            int linkmanImage;
            Linkmans p1 = new Linkmans(personsS.get(j).getUserName(), personsS.get(j).getPhoneNum(), personsS.get(j).getFriendStar(), personsS.get(j).getUserPic(), personsS.get(j).getUserSex(), personsS.get(j).getNickName());
            persons.add(p1);
        }
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

        tvFriendName.setText(newPersonStar.get(position).getLinkmanName());
        imgFriendPic.setImageBitmap(GlobalImg.getImage(getContext(), newPersonStar.get(position).getLinkmanPhone()));

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
        //ListView3点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

//
//        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
//                friendStar = newPersonStar.get(position - 1).getFriendStar();
////                if (i== ProtoMessage.TeamRole.Owner_VALUE) {
////                    host = true;
////                }else{
////                    host = false;
////                }
//                return false;
//            }
//        });
//        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
//            public void onCreateContextMenu(ContextMenu menu, View v,
//                                            ContextMenu.ContextMenuInfo menuInfo) {
//                if (friendStar == SET_FRIEND_STAR) {
//                    menu.add(0, 6, 0, "取消置顶");
//                } else {
//                    menu.add(0, 11, 0, "置顶");
//                }
//                menu.add(0, 5, 0, "好友信息");
//
////                menu.add(0, 3, 0, "修改该联系人名称");
////                menu.add(0, 4, 0, "删除该联系人");
//
//            }
//        });
    }

    private void onItemClicks(final int position, String iamge, String text) {
        SharedPreferences preferences = getContext().getSharedPreferences("token", Context.MODE_PRIVATE);
        String myPhone = preferences.getString("phone", "");
        MsgRecordHelper msgRecordHelper = new MsgRecordHelper(getContext(), myPhone + "MsgShow.dp", null);
        SQLiteDatabase db = msgRecordHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("new_msg", 0);
        db.update("Msg", values, "phone = ? and msg_from = ?", new String[]{newPersonStar.get(position).getLinkmanPhone(), 0 + ""});
        db.close();
        NotificationManager nm = (NotificationManager) getContext().getSystemService(getContext().NOTIFICATION_SERVICE);
        nm.cancel(0);//消除对应ID的通知

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getContext()).upDataMsg();
        }

        showFriendMsg(newPersonStar.get(position).getLinkmanPhone());

    }

    //AppliedFriends mAppliedFriends;
    //ListView弹出菜单
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        //info.id得到listview中选择的条目绑定的id
        String id = String.valueOf(info.id);
        switch (item.getItemId()) {
            case 4:
                String phone = newPersonStar.get((int) info.id).getLinkmanPhone();
                String name = newPersonStar.get((int) info.id).getLinkmanName();
                deleteFriendDialog(phone, name);
                //  ToastR.setToast(getContext(), "删除第" + id + "个联系人");
                return true;
            case 3:
                AppliedFriends af = new AppliedFriends();
                af.setFriendStar(newPersonStar.get((int) info.id).getFriendStar());
                af.setPhoneNum(newPersonStar.get((int) info.id).getLinkmanPhone());
                af.setNickName(newPersonStar.get((int) info.id).getLinkmanName());
                showSetFriendInfoDialog(af, MODIFY_FRIEND_NICKNAME);
                // ToastR.setToast(getContext(), "查看第" + id + "个联系人的消息");
                return true;
            case 5:
                showFriendMsg(newPersonStar.get((int) info.id).getLinkmanPhone());
                return true;
            case 6:
                AppliedFriends af1 = new AppliedFriends();
                af1.setFriendStar(newPersonStar.get((int) info.id).getFriendStar());
                af1.setPhoneNum(newPersonStar.get((int) info.id).getLinkmanPhone());
                setFriendInfo(af1, MODIFY_FRIEND_STAR, CANCEL_FRIEND_STAR);
                return true;
            case 11:
                AppliedFriends af2 = new AppliedFriends();
                af2.setFriendStar(newPersonStar.get((int) info.id).getFriendStar());
                af2.setPhoneNum(newPersonStar.get((int) info.id).getLinkmanPhone());
                setFriendInfo(af2, MODIFY_FRIEND_STAR, SET_FRIEND_STAR);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void showFriendMsg(String phone) {
        for (ViewFriendsMsg vm : mFriend) {
            if (phone.equals(vm.friends.getPhoneNum())) {
                mViewFriendsMsg = vm;
                break;
            }
        }
        Intent intent = new Intent(getContext(), FriendDetailsDialogActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("appliedFriends", mViewFriendsMsg.friends);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void showSetFriendInfoDialog(final AppliedFriends af, final int type) {
        LayoutInflater factory = LayoutInflater.from(getContext());// 提示框
        final View view = factory.inflate(R.layout.friend_request_editbox_layout, null);// 这里必须是final的
        final TextView remark = (TextView) view.findViewById(R.id.tv_remark_name);
//        final TextView msg = (TextView)view.findViewById(R.id.tv_msg);
//        final EditText edit = (EditText) view.findViewById(R.id.msg_editText);// 获得输入框对象
        final EditText editRemark = (EditText) view.findViewById(R.id.remark_editText);// 获得输入框对象
        final RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.rl_msg);
        rl.setVisibility(View.GONE);
        remark.setText("用户名");

        editRemark.setText(af.getNickName());
        editRemark.setSelection(editRemark.length());// 将光标追踪到内容的最后

        final String oldName = af.getNickName();
        new AlertDialog.Builder(getContext()).setTitle("用户名修改")// 提示框标题
                .setView(view).setPositiveButton("确定", // 提示框的两个按钮
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String remark = editRemark.getText().toString().trim();
                        if (oldName.equals(remark) == false) {
                            af.setNickName(remark);
                            SharedPreferencesUtils.put(getContext(), "friend_list_changed", true);
                        }
//
                        setFriendInfo(af, type, NO_FRIEND_STAR);
                        dialog.dismiss();
                    }

                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                }).create().show();
    }

    /*
    设置好友信息
     */
    public void setFriendInfo(final AppliedFriends af, final int type, final int star) {

        ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
        if (type == MODIFY_FRIEND_NICKNAME) {
            if (af.getNickName().length() == 1) {
                ToastR.setToast(getContext(), "输入数字必须是两位或者两位以上");
                return;
            }
            builder.setNickName(af.getNickName());
        }
        builder.setPhoneNum(af.getPhoneNum());
        if (type == MODIFY_FRIEND_STAR) {
            if (star == SET_FRIEND_STAR) {
                builder.setFriendStar(true);
            } else {
                builder.setFriendStar(false);
            }
        }
        MyService.start(getContext(), ProtoMessage.Cmd.cmdSetFriendInfo.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(SetFriendInfoProcesser.ACTION);
        new TimeoutBroadcast(getContext(), filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(getContext(), "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    try {
                        if (type == MODIFY_FRIEND_NICKNAME) {
                            DBManagerFriendsList db = new DBManagerFriendsList(getContext(), DBTableName.getTableName(getContext(), DBHelperFriendsList.NAME));
                            db.updateFriendNickName(af);
                            db.closeDB();
                            updateLocalData();
//                        loadFriendsListFromNet();
//                        ToastR.setToast(getContext(), "修改好友名字成功");
                        } else {
                            af.setFriendStar(star);
                            DBManagerFriendsList db = new DBManagerFriendsList(getContext(), DBTableName.getTableName(getContext(), DBHelperFriendsList.NAME));
                            db.updateFriendStar(af);
                            db.closeDB();
                            updateLocalData();
//                        ToastR.setToast(getContext(), "星标修改成功");
                        /*
                        DBManagerFriendsList db = new DBManagerFriendsList(getContext());
                        mList = db.getFriends();
                        db.closeDB();
                        */
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void updateLocalData() {
        try {
            List<AppliedFriends> list;
            DBManagerFriendsList db = new DBManagerFriendsList(getContext(), DBTableName.getTableName(getContext(), DBHelperFriendsList.NAME));
            list = db.getFriends(false);
            db.closeDB();
            convertViewFriendList(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除好友提示框
     */
    public void deleteFriendDialog(final String str, final String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()); // 先得到构造器

        builder.setMessage("确定要删除 " + name + " ？").setTitle("提示：").setPositiveButton("确定", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                deleteFriend(str);
            }
        }).setNegativeButton("取消", new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();

    }

    /*
    删除好友
     */
    public void deleteFriend(final String str) {
        deletePhone = str;
        ProtoMessage.DeleteFriend.Builder builder = ProtoMessage.DeleteFriend.newBuilder();
        builder.setFriendPhoneNum(str);
        MyService.start(getContext(), ProtoMessage.Cmd.cmdDeleteFriend.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(DeleteFriendProcesser.ACTION);
        new TimeoutBroadcast(getContext(), filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(getContext(), "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    deleteSQLite();
                    refreshLocalData(str);
                    //loadFriendsListFromNet();
                    ToastR.setToast(getContext(), "删除好友成功");
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void deleteSQLite() {
//        SharedPreferences preferences = getContext().getSharedPreferences("token", Context.MODE_PRIVATE);
//        String myphone = preferences.getString("phone", "");
//        LinkmanRecordHelper linkmanRecordHelper = new LinkmanRecordHelper(getContext(), myphone + deletePhone + "LinkmanMsgShow.dp", null);
//        SQLiteDatabase dbMan = linkmanRecordHelper.getWritableDatabase();
//        MsgRecordHelper msgRecordHelper = new MsgRecordHelper(getContext(), myphone + "MsgShow.dp", null);
//        SQLiteDatabase db = msgRecordHelper.getWritableDatabase();
//        db.delete("Msg", "phone = ? and msg_from = ?", new String[]{deletePhone, 0 + ""});
//        dbMan.delete("LinkmanRecord", null, null);
//        db.close();
//        dbMan.close();
        MsgTool.deleteFriendsMsg(getContext(), deletePhone);
        deletePhone = null;
    }

    /*
    刷新本地数据
     */
    private void refreshLocalData(String str) {
        try {
            final List<String> phoneNum = new ArrayList<String>();
            phoneNum.add(str);
            if (onlineSet.size() > 0) {
                onlineSet.remove(str);
            }
            /*DBManagerFriendsList db = new DBManagerFriendsList(getContext(), DBTableName.getTableName(getContext(), DBHelperFriendsList.NAME));
            db.deleteSomeFriends(phoneNum);
            db.closeDB();*/
            // 遍历删除内存数据
            Log.v("wsDvr", "phoneNum:" + phoneNum.toString());
            Log.v("wsDvr", "personsS:" + personsS.size());
            for (String phone : phoneNum) {
                Iterator<AppliedFriends> i = personsS.iterator();
                while (i.hasNext()) {
                    AppliedFriends k = i.next();
                    if (k.getPhoneNum().equals(phone)) {
                        i.remove();
                        break;
                    }
                }
            }
            Log.v("wsDvr", "personsS:" + personsS.size());
            setList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    获取好友
     */
    public void loadFriendsListFromNet() {

        if (!ConnUtil.isConnected(getMyActivity())) {
            Log.w("drv", "没有网络");
            OnlineSetTool.removeAll();
            return;
        }
        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
        MyService.start(getContext(), ProtoMessage.Cmd.cmdGetFriendList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(FriendsListProcesser.ACTION);
        new TimeoutBroadcast(getContext(), filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                if (mPullRefreshView != null)
                    mPullRefreshView.setRefreshing(false);
                ToastR.setToast(getContext(), "连接超时");
            }

            @Override
            public void onGot(Intent i) {

                if (mPullRefreshView != null)
                    mPullRefreshView.setRefreshing(false);
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    List<AppliedFriends> mList;
//                    AppliedFriendsList list = i.getParcelableExtra("get_friends_list");
                    try {
                        GlobalImg.clear();
                        DBManagerFriendsList db = new DBManagerFriendsList(getContext(), DBTableName.getTableName(getContext(), DBHelperFriendsList.NAME));
                        mList = db.getFriends(false);
                        db.closeDB();
                        convertViewFriendList(mList);

                        // 通知消息界面更新
                        SharedPreferencesUtils.put(getContext(), "friend_list_changed", true);
                        if (newPersonStar != null && newPersonStar.size() > 0) {
                            loadFriendStatus();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        if (mPullRefreshView != null)
                            mPullRefreshView.setRefreshing(false);
                    }
                    if (checkPullRefresh == true) {
                        ToastR.setToast(getContext(), "获取好友成功");
                        checkPullRefresh = false;
                    }
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });

    }

    public void refreshFriendsMenu() {
        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
        MyService.start(getContext(), ProtoMessage.Cmd.cmdAppliedList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppliedListProcesser.ACTION);
        new TimeoutBroadcast(getContext(), filter, getBroadcastManager()).startReceiver(30, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                Log.i("downFriendsApplied", "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    AppliedFriendsList list = i.getParcelableExtra("get_applied_msg");
                    if (list.getAppliedFriends().size() <= 0) {
                        Log.i("downFriendsApplied", "目前没有好友邀请的列表信息");
                    } else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity mainActivity = new MainActivity();
                                mainActivity.refreshData(MainActivity.msgType, MainActivity.MENU_ONE);
                                getMyActivity().supportInvalidateOptionsMenu();
                            }
                        });
                    }
                } else {
                    Log.i("downFriendsApplied", "获取失败" + i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    public void fail(int i) {
        new ResponseErrorProcesser(getContext(), i);
    }

    public void convertViewFriendList(List<AppliedFriends> list) {
        personsS.clear();
        personsStar.clear();
        mFriend.clear();
        tv_wait.setVisibility(View.GONE);
        for (int i = 0; i < list.size(); i++) {
            ViewFriendsMsg temp = new ViewFriendsMsg();

            String friendName = list.get(i).getNickName();
            if (friendName == null || friendName.equals("")) {
                friendName = list.get(i).getUserName();
            }
            if (friendName == null || friendName.equals("")) {
                friendName = list.get(i).getPhoneNum();
            }
            AppliedFriends af = new AppliedFriends();
            af.setUserName(friendName);
            af.setPhoneNum(list.get(i).getPhoneNum());
            af.setFriendStar(list.get(i).getFriendStar());
            af.setNickName(list.get(i).getUserName());
            af.setUserSex(list.get(i).getUserSex());
            //af.setUserPic(FriendFaceUtill.getFriendsFace(this.getMyActivity(), af.getPhoneNum()));
            if (list.get(i).getFriendStar() == NOT_FRIEND_STAR) {
                personsS.add(af);
            } else {
                personsStar.add(af);
            }
            temp.friends = list.get(i);
            mFriend.add(temp);
            // personsS.add(friendName);
            //personsS.add(list.get(i).getPhoneNum());
        }
        setList();
    }

    protected void setList() {
        initAdapterData();
        refreshListData(newPersonStar);
    }

    private void refreshListData(List<Linkmans> list) {
        try {
            if (adapter == null) {
                Log.d("CHAT", "Excepton: refreshListData: Adapter is null!");
            } else {
                adapter.refresh(list);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    class CloseRoomReceiiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter.notifyDataSetChanged();
        }
    }

    //网络监听广播
    class MyConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean connect = intent.getBooleanExtra(ConnectionChangeReceiver.NETWORK_CHANGE_KEY, false);
            isGetFriendStatus = false;
            Log.d(TAG, "ConnectionChange :" + connect);
            if (connect) {
                loadFriendStatus();
            } else {
                if (adapter != null) {
                    adapter.clearOnlineStatus();
                    linkManSort();
                }
            }
        }
    }

    /**
     * 绘制索引列表
     */
    @SuppressLint("ClickableViewAccessibility")
    public void getIndexView(int layoutHeight) {
        height = layoutHeight / indexStr.length;
        int size = 13;
        if (height >= 40 ){
            size = 13;
        } else if (height >= 25 && height < 40){
            size = 11;
        } else if (height >= 10 && height < 25){
            size = 9;
        } else {
            size = 7;
        }
//        ToastR.setToastLong(getActivity(),height+"  "+size);
//        Log.i("jmh","height:"+height + " size:"+size);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, height);
        for (String anIndexStr : indexStr) {
            final TextView tv = new TextView(getContext());
            tv.setLayoutParams(params);
            tv.setText(anIndexStr);
            tv.setTextSize(size);
            tv.setPadding(5, 0, 5, 0);
            layoutIndex.addView(tv);
        }
        layoutIndex.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        layoutIndex.setBackgroundColor(Color
                                .parseColor("#77000000"));
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float y = event.getY();
                        int index = (int) (y / height);
                        if (index > -1 && index < indexStr.length) {// 防止越界
                            String key = indexStr[index];
                            if (selector.containsKey(key)) {
                                int pos = selector.get(key);
                                listView.setSelectionFromTop(// 防止ListView有标题栏
                                        pos + listView.getHeaderViewsCount(), 0);
                                tv_show.setVisibility(View.VISIBLE);
                                tv_show.setText(indexStr[index]);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        layoutIndex.setBackgroundColor(Color
                                .parseColor("#00ffffff"));
                        tv_show.setVisibility(View.GONE);
                        break;
                }
                return true;
            }
        });
    }

    class ViewFriendsMsg {
        public AppliedFriends friends;

        public ViewFriendsMsg() {
        }
    }
}
