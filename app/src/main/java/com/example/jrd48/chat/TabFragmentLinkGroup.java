package com.example.jrd48.chat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.SQLite.TeamMemberHelper;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.group.DBHelperTeamList;
import com.example.jrd48.chat.group.DBManagerTeamList;
import com.example.jrd48.chat.group.MsgTool;
import com.example.jrd48.chat.group.ShowTeamInfoPrompt;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.TeamInfoList;
import com.example.jrd48.chat.group.cache.DBHelperChatTimeList;
import com.example.jrd48.chat.group.cache.DBManagerChatTimeList;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.chat.location.ServiceCheckUserEvent;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.AutoCloseProcesser;
import com.example.jrd48.service.protocol.root.DismissTeamProcesser;
import com.example.jrd48.service.protocol.root.GroupsListProcesser;
import com.example.jrd48.service.protocol.root.ModifyTeamInfoProcesser;
import com.example.jrd48.service.protocol.root.ReceiverProcesser;
import com.example.jrd48.service.protocol.root.SearchFriendProcesser;
import com.example.jrd48.service.protocol.root.TeamMemberProcesser;
import com.luobin.dvr.R;
import com.luobin.model.CallState;
import com.luobin.search.friends.map.TeamMemberLocationActivity;
import com.luobin.ui.FriendDetailsDialogActivity;
import com.luobin.ui.SelectMemberActivity;
import com.luobin.ui.VideoOrVoiceDialog;
import com.luobin.ui.adapter.ContactsGroupAdapter;
import com.luobin.ui.adapter.ContactsMemberAdapter;
import com.luobin.utils.ButtonUtils;
import com.luobin.widget.LoadingDialog;
import com.luobin.widget.PromptDialog;
import com.luobin.widget.ScrollListView;
import com.qihoo.linker.logcollector.utils.LogCollectorUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static com.luobin.ui.SelectMemberActivity.APPLYTEAM;

import android.support.design.widget.FloatingActionButton;
import android.view.View.OnClickListener;

import com.luobin.ui.SelectMemberToDeleteActivity;

import android.widget.ImageView;
import android.app.Activity;


/**
 * Created by jrd48
 */

public class TabFragmentLinkGroup extends BaseLazyFragment {
    private static final String TAG = "TabFragmentLinkGroup";
    int i;
    private ScrollListView groupListView;
    private ListView memberListView;
    private ContactsGroupAdapter groupAdapter;
    private ContactsMemberAdapter memberAdapter;
    private TextView tvGroupName;
    private TextView btnAddGroupMember;
    private PullRefreshLayout pullRefreshLayout;
    private List<Team> groupList = new ArrayList<>();

    private IntentFilter filterRoom;
    private CloseRoomReceiiver closeRoomReceiiver;
    private boolean run = false;
    private boolean isPullRefresh = false;
    private long deletTeamID = -1;
    String myPhone;
    private long TIME_GET_MEMBERS_DELAY = 5 * 1000L;
    private static final int MSG_GET_MEMBERS = 2000;
    private static final int QUIT_TEAM = 0;
    private static final int DELETE_TEAM = 1;
    private static final int UPDATE_UI = 0;
    LoadingDialog loadingDialog;

    public static final int MOVE_GROUP_LIST = 0;
    public static final int MOVE_MEMBER_LIST = 1;

    private  int groupSelectPosition = 0;
    private int preGroupSelectPosition = -1; // 用来解决listview.getSelectedPosition 不准确
    private  int memberSelectPosition = 0;
    private static int moveList = MOVE_GROUP_LIST;

    public static boolean isVisiable = true;

	private ImageView addMember;
    private ImageView removeMember;
    private ProgressDialog mProgressDialog;

	private static final int ADD_TEAM_MEMBER = 1;
	private static final int DELETE_TEAM_MEMBER = 2;

	private long mDelMemberTeamID = -1;
	private String mDelMemberPhone = "";
	
    public boolean isPullRefresh() {
        return isPullRefresh;
    }


    HashMap<Long, List<TeamMemberInfo>> allMemberMap = new HashMap();

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_UI: //刷新UI
                    isPullRefresh = false;
                    if (pullRefreshLayout != null)
                        pullRefreshLayout.setRefreshing(false);
                    dissmissLoading();

                    if (groupAdapter == null) {
                        if (groupList.size() > 0) {
                            groupAdapter = new ContactsGroupAdapter(groupList, allMemberMap, mContext);
                            groupListView.setAdapter(groupAdapter);
                            groupListView.requestFocus();
                            memberAdapter = new ContactsMemberAdapter(allMemberMap.get(groupList.get(groupSelectPosition).getTeamID()), mContext);
                            List<AppliedFriends> listFriends = getlistMembersCache();
                            memberAdapter.setAppliedFriends(listFriends);
                            memberListView.setAdapter(memberAdapter);
                            groupListView.setSelection(groupSelectPosition);
                            groupListView.requestFocus();
                            groupListView.requestFocusFromTouch();
                            tvGroupName.setText(groupList.get(groupSelectPosition).getLinkmanName());
                        }
                    } else {
                        groupAdapter.seteData(groupList);
                        groupAdapter.notifyDataSetChanged();

                        if (groupSelectPosition >= groupList.size()) {
                            groupSelectPosition = groupList.size() - 1;
                            if (groupSelectPosition < 0) {
                                groupSelectPosition = 0;
                            }
                        }

                        if (groupList.size() > 0) {
                            memberAdapter.setData(allMemberMap.get(groupList.get(groupSelectPosition).getTeamID()));
                            List<AppliedFriends> listFriends = getlistMembersCache();
                            memberAdapter.setAppliedFriends(listFriends);
                            memberAdapter.notifyDataSetChanged();
                            groupListView.setSelection(groupSelectPosition);
                            groupListView.requestFocus();
                            groupListView.requestFocusFromTouch();
                            tvGroupName.setText(groupList.get(groupSelectPosition).getLinkmanName());
                        } else {//如果当前没有群组，清空群成员列表
                            List<AppliedFriends> listFriends = getlistMembersCache();
                            memberAdapter.setAppliedFriends(listFriends);
                            memberAdapter.setData(new ArrayList<TeamMemberInfo>());
                            memberAdapter.notifyDataSetChanged();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private ChatStatusReceiver chatStatusReceiver;

    //群组变化，创建群组等
    private BroadcastReceiver myTeamReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            deletTeamID = intent.getLongExtra("teamID", -1);
            if (deletTeamID == -1) {
                loadTeamListFromNet();
            } else {
                // deleteSQLite();
                getDBMsg();
            }
        }

    };

    //刷新群，加入退去群组等
    private BroadcastReceiver refreshTeamReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            if ("ACTION.refreshTeamList".equals(intent.getAction())) {
                loadTeamListFromNet();
            }
        }

    };

    //刷新头像
    private BroadcastReceiver changgeImaReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String phone = intent.getStringExtra(ReceiverProcesser.PHONE_NUMBER);
            if (TextUtils.isEmpty(phone)) {
                return;
            }

			getDBMsg();
        }

    };

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        try {
            if (closeRoomReceiiver != null) {
                getContext().unregisterReceiver(closeRoomReceiiver);
            }
            if (myTeamReceiver != null) {
                getContext().unregisterReceiver(myTeamReceiver);
            }
            if (refreshTeamReceiver != null) {
                getContext().unregisterReceiver(refreshTeamReceiver);
            }
            if (changgeImaReceiver != null) {
                getContext().unregisterReceiver(changgeImaReceiver);
            }
            if (chatStatusReceiver != null) {
                getContext().unregisterReceiver(chatStatusReceiver);
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        (new Instrumentation()).setInTouchMode(false);
        if (groupList == null || groupList.size() == 0) {
            (new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i("TabFragmentLinkGroup", "onResume set groupSelectPosition to 0");
                    getDBMsg();
                }
            })).start();
        } else {
            DBManagerChatTimeList chatDB = new DBManagerChatTimeList(getContext(), true, DBTableName.getTableName(getContext(), DBHelperChatTimeList.NAME));
            Map<Long, Long> timeList = chatDB.getTimeList();
            chatDB.closeDB();
            AllTeamPinyinComparator comparator = new AllTeamPinyinComparator(timeList);
            Collections.sort(groupList, comparator);
            groupSelectPosition = 0;
            mHandler.sendEmptyMessage(UPDATE_UI);
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
        filterRoom = new IntentFilter();
        closeRoomReceiiver = new CloseRoomReceiiver();
        filterRoom.addAction(AutoCloseProcesser.ACTION);
        filterRoom.addAction(TeamMemberProcesser.ACTION);
        filterRoom.addAction(SearchFriendProcesser.ACTION);
        getContext().registerReceiver(closeRoomReceiiver, filterRoom);

        IntentFilter filt = new IntentFilter();
        chatStatusReceiver = new ChatStatusReceiver();
        filt.addAction("NotifyProcesser.ChatStatus");
        filt.addAction(GlobalStatus.NOTIFY_CALL_ACTION);
        getContext().registerReceiver(chatStatusReceiver, filt);

        initBroadcast();
//        myPhone = getMyPhone();
//        if (!run) {
//            run = true;
//            getDBMsg();
//        }
    }

    private void showLoading() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(mContext, R.style.loadingDialog);
        }
        if (!loadingDialog.isShowing())
            loadingDialog.show();
    }

    private void dissmissLoading() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    @Override
    protected View initView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.demolayout, container,
                false);
        dissmissLoading();
        tvGroupName = (TextView) view.findViewById(R.id.group_name);
        if ((boolean) SharedPreferencesUtils.get(mContext, "group_booting", false)) {
            SharedPreferencesUtils.put(mContext, "group_booting", false);
        }
        pullRefreshLayout = (PullRefreshLayout) view.findViewById(R.id.refresh_layout);
        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadTeamListFromNet(false);
            }
        });

        groupListView = (ScrollListView) view.findViewById(R.id.lv_group);
        groupListView.setSelector(R.drawable.tab_list_item_selector);
        groupListView.setNoNearby(true);
        groupListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                groupSelectPosition = position;
                List<TeamMemberInfo> memberList = allMemberMap.get(groupList.get(groupSelectPosition).getTeamID());
                List<AppliedFriends> listFriends = getlistMembersCache();
                memberAdapter.setAppliedFriends(listFriends);
                memberAdapter.setData(memberList);
                memberAdapter.notifyDataSetChanged();
                tvGroupName.setText(groupList.get(groupSelectPosition).getLinkmanName());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        groupListView.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (!groupListView.isFocusable()){
				   Log.i("TabFragmentLinkGroup", "not focusable");
                   return false;
                }
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getRepeatCount() == 0) {
                    switch (keyEvent.getKeyCode()) {
                        case KeyEvent.KEYCODE_F6:
                            if (groupListView.getSelectedItemPosition() != -1) {
                                groupSelectPosition = groupListView.getSelectedItemPosition();
                            }
                            if (isVisible){
                                Log.i("TabFragmentLinkGroup", "KEYCODE_F6, groupSelectPosition = " + groupSelectPosition);
                                Team msg = groupList.get(groupSelectPosition);
                                Intent intent = new Intent(getContext(), FirstActivity.class);
                                intent.putExtra("data", 1);
                                CallState callState = GlobalStatus.getCallCallStatus().get(String.valueOf(1) + msg.getTeamID());
                                if (GlobalStatus.equalTeamID(msg.getTeamID())) {
                                    //intent.putExtra("callType", 0);
                                    intent.putExtra("callType", 2);
                                } else if (callState != null && callState.getState() == GlobalStatus.STATE_CALL) {
                                    intent.putExtra("callType", 1);
                                } else {
                                    intent.putExtra("callType", 2);
                                }
                                intent.putExtra("group", msg.getTeamID());
                                intent.putExtra("type", msg.getMemberRole());
                                intent.putExtra("group_name", msg.getLinkmanName());
                                Log.d("TabFragmentLinkGroup", "==========KEYCODE_F6-=group=" + msg.getTeamID()
                                        + "--type=" + msg.getMemberRole() + "--group_name=" + msg.getLinkmanName());
                                intent.putParcelableArrayListExtra("memberList", (ArrayList<? extends Parcelable>) allMemberMap.get(groupList.get(groupSelectPosition).getTeamID()));
                                VideoOrVoiceDialog dialog = new VideoOrVoiceDialog(getContext(), intent);
                                dialog.show();
                                return true;
                            }
                            break;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        memberListView = (ListView) view.findViewById(R.id.lv_member);
        memberListView.setSelector(R.drawable.tab_list_item_selector);
        memberListView.setFocusable(true);
        memberListView.setFocusableInTouchMode(true);
        memberListView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyEvent.getKeyCode()) {
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        groupListView.setNextFocusRightId(R.id.lv_member);
        memberListView.setNextFocusLeftId(R.id.lv_group);

        groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                moveList = MOVE_GROUP_LIST;
				//rs added if for crash LBCJW-71
				try{
					if(groupList.size() > 0){
		                groupList.get(groupSelectPosition).setSelect(false);
		                groupSelectPosition = position;
		                groupList.get(groupSelectPosition).setSelect(true);
						
		                groupAdapter.seteData(groupList);
		                groupAdapter.notifyDataSetChanged();

						//rs added for LBCJW-170
						if (groupSelectPosition >= groupList.size()) {
	                            groupSelectPosition = groupList.size() - 1;
	                            if (groupSelectPosition < 0) {
	                                groupSelectPosition = 0;
	                            }
	                    }
						//end
						
						//rs added for LBCJW-41,
						tvGroupName.setText(groupList.get(groupSelectPosition).getLinkmanName());
		                if (allMemberMap.size() > 0) {
		                    List<TeamMemberInfo> memberList = allMemberMap.get(groupList.get(groupSelectPosition).getTeamID());
		                    //memberList.get(memberSelectPosition).setSelect(false);//rs del for crash:LBCJW-71
		                    memberSelectPosition = 0;
	                    	List<AppliedFriends> listFriends = getlistMembersCache();
	                    	memberAdapter.setAppliedFriends(listFriends);
		                    memberAdapter.setData(memberList);
		                    memberAdapter.notifyDataSetChanged();
		                }
					}
				}catch(Exception ex){
					ex.printStackTrace();
					Log.d("rs", "found Exception:"+ex.toString());
				}
				//end
            }
        });

        groupListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                final Team team = groupList.get(position);
                PopupMenu pop = new PopupMenu(mContext, view);
                pop.getMenuInflater().inflate(R.menu.menu_group, pop.getMenu());
                if (team.getMemberRole() == ProtoMessage.TeamRole.Owner_VALUE) {
                    pop.getMenu().removeItem(R.id.quit_group);
                } else {
                    pop.getMenu().removeItem(R.id.dismiss_group);
                }
                pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.modify_group_name:
                                showChangeGroupNameDialog(team);
                                break;
                            case R.id.show_location:
                                goToMap(team.getTeamID());
                                break;
                            case R.id.quit_group:
                                deleteTeamDialog(team.getTeamID(), QUIT_TEAM, team.getLinkmanName());
                                break;
                            case R.id.dismiss_group:
                                deleteTeamDialog(team.getTeamID(), DELETE_TEAM, team.getLinkmanName());
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                pop.show();
                return true;
            }
        });

        memberListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                moveList = MOVE_MEMBER_LIST;
                groupList.get(groupSelectPosition).setSelect(false);
                groupAdapter.notifyDataSetChanged();

				if(groupList.get(groupSelectPosition) == null){
					//rs added for null exception
					Log.d("rs", "memberListView.setOnItemClickListener groupList found null");
					return;

				}
				
                List<TeamMemberInfo> memberList = allMemberMap.get(groupList.get(groupSelectPosition).getTeamID());

				if(memberList.get(memberSelectPosition) == null){
                    //rs added for null exception
                    Log.d("rs", "memberListView.setOnItemClickListener memberList found null");
                    return;
                }
                memberList.get(memberSelectPosition).setSelect(false);
                memberSelectPosition = position;
                memberList.get(memberSelectPosition).setSelect(true);

                List<AppliedFriends> listFriends = getlistMembersCache();
                memberAdapter.setAppliedFriends(listFriends);
                memberAdapter.setData(memberList);
                memberAdapter.notifyDataSetChanged();

                Intent intent = new Intent(mContext, FriendDetailsDialogActivity.class);
                Bundle bundle = new Bundle();
                List<TeamMemberInfo> teamMemberInfos = memberAdapter.getData();
                TeamMemberInfo teamMemberInfo = teamMemberInfos.get(position);
                bundle.putString("userPhone", teamMemberInfo.getUserPhone());
                bundle.putString("userName", teamMemberInfo.getUserName());
                intent.putExtra("teamID", groupList.get(groupSelectPosition).getTeamID());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        btnAddGroupMember = (TextView) view.findViewById(R.id.btn_add_group_member);
        btnAddGroupMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (groupList.size() == 0) {
                    ToastR.setToast(getContext(), "暂无群组可以添加成员");
                } else {
                    try {
                        Intent intent = new Intent(mContext, SelectMemberActivity.class);
                        intent.putExtra("teamID", groupList.get(groupSelectPosition).getTeamID());
					    intent.putParcelableArrayListExtra("curMemberList", (ArrayList<? extends Parcelable>) allMemberMap.get(groupList.get(groupSelectPosition).getTeamID()));//rs added for LBCJW-68
                        startActivityForResult(intent, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

		//rs added for add/delete member
		addMember = (ImageView) view.findViewById(R.id.add_member);
		removeMember = (ImageView) view.findViewById(R.id.remove_member);
		addMember.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				ToastR.setToast(getContext(), "添加群组成员");			
				
                if (groupList.size() == 0) {
                    ToastR.setToast(getContext(), "暂无群组可以添加成员");
                } else {
                    try {
                        Intent intent = new Intent(mContext, SelectMemberActivity.class);
                        intent.putExtra("teamID", groupList.get(groupSelectPosition).getTeamID());
					    intent.putParcelableArrayListExtra("curMemberList", (ArrayList<? extends Parcelable>) allMemberMap.get(groupList.get(groupSelectPosition).getTeamID()));//rs added for LBCJW-68
                        startActivityForResult(intent, ADD_TEAM_MEMBER);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
			}
		});

		
		removeMember.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				//ToastR.setToast(getContext(), "删除群组成员");			
				Log.d("rs", "removeMember clicked:");
				if (groupList == null || groupList.size() == 0) {
                    ToastR.setToast(getContext(), "暂无群组可以删除成员");
                } else {
					try {
						Team curTeam = groupList.get(groupSelectPosition);
						//mDelMemberTeamID = curTeam.getTeamID();
		                Intent intent = new Intent(mContext, SelectMemberToDeleteActivity.class);
		                intent.putExtra("teamID", curTeam.getTeamID());
		                intent.putExtra("type", curTeam.getMemberRole());
		                intent.putParcelableArrayListExtra("curMemberList", (ArrayList<? extends Parcelable>) allMemberMap.get(groupList.get(groupSelectPosition).getTeamID()));
						startActivityForResult(intent, DELETE_TEAM_MEMBER);
						} catch (Exception e) {
	                        e.printStackTrace();
							Log.d("rs", "found exception:"+e.toString());
	                    }
                }
			}
		});
		//end	
		
        return view;
    }

    /**
     * 获取群组
     */
    private void loadTeamListFromNet(boolean isLoad) {
        isPullRefresh = true;
        if (isLoad) {
            //showLoading();
        }

        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
        MyService.start(getContext(), ProtoMessage.Cmd.cmdGetTeamList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(GroupsListProcesser.ACTION);
        new TimeoutBroadcast(getContext(), filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                isPullRefresh = false;
                if (pullRefreshLayout != null)
                    pullRefreshLayout.setRefreshing(false);
                ToastR.setToast(getContext(), "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    TeamInfoList list = i.getParcelableExtra("get_group_list");
                    SharedPreferencesUtils.put(getContext(), "data_init", true);
                    convertViewGroupList(list.getmTeamInfo());
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }


    /**
     * 获取群组
     */
    private void loadTeamListFromNet() {
        isPullRefresh = true;
        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
        MyService.start(getContext(), ProtoMessage.Cmd.cmdGetTeamList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(GroupsListProcesser.ACTION);
        new TimeoutBroadcast(getContext(), filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                isPullRefresh = false;
                if (pullRefreshLayout != null)
                    pullRefreshLayout.setRefreshing(false);
                ToastR.setToast(getContext(), "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    //ToastR.setToast(getContext(), "获取群组成功");
                    TeamInfoList list = i.getParcelableExtra("get_group_list");
                    SharedPreferencesUtils.put(getContext(), "data_init", true);
                    convertViewGroupList(list.getmTeamInfo());
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });

    }

    //视频通话状态更新
    class ChatStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(GlobalStatus.NOTIFY_CALL_ACTION)) {

            } else if (intent.hasExtra("chat_status")) {

            }
        }
    }

    public void fail(int i) {
        isPullRefresh = true;
        if (pullRefreshLayout != null)
            pullRefreshLayout.setRefreshing(false);
        new ResponseErrorProcesser(getContext(), i);
    }

    /**
     * 退群或者解散群提示框
     */
    public void deleteTeamDialog(final long teamId, final int type, final String teamName) {
        String str = "退出";
        if (type == DELETE_TEAM) {
            str = "解散";
        }

        PromptDialog promptDialog = new PromptDialog(getContext());
        promptDialog.show();
        promptDialog.setTitle("提示：");
        promptDialog.setMessage("确定要" + str + " " + teamName + " 群组？");
        promptDialog.setOkListener(str, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (type == DELETE_TEAM) {
                    groupDelete(teamId);
                } else {
                    groupQuit(teamId);
                }
                dialog.dismiss();
            }
        });
        promptDialog.setCancelListener("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

      /*  AlertDialog.Builder builder = new AlertDialog.Builder(getContext()); // 先得到构造器
        builder.setMessage("确定要" + str + " " + teamName + " 群组？").setTitle("提示：").setPositiveButton("确定", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (type == DELETE_TEAM) {
                    groupDelete(teamId);
                } else {
                    groupQuit(teamId);
                }

            }
        }).setNegativeButton("取消", new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();*/

    }

    /**
     * 退出群组
     */
    private void groupQuit(final long teamId) {
        deletTeamID = teamId;
        ProtoMessage.ApplyTeam.Builder builder = ProtoMessage.ApplyTeam.newBuilder();
        builder.setTeamID(teamId);
        MyService.start(getContext(), ProtoMessage.Cmd.cmdQuitTeam.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(DismissTeamProcesser.ACTION);
        new TimeoutBroadcast(getContext(), filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(getContext(), "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    deleteSQLite();
                    refreshLocalData(teamId);
                    ToastR.setToast(getContext(), "退出群组成功");
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    /**
     * 删除/解散群组
     */
    private void groupDelete(final long l) {
        deletTeamID = l;
        ProtoMessage.AcceptTeam.Builder builder = ProtoMessage.AcceptTeam.newBuilder();
        builder.setTeamID(l);
        MyService.start(getContext(), ProtoMessage.Cmd.cmdDismissTeam.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(DismissTeamProcesser.ACTION);
        new TimeoutBroadcast(getContext(), filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(getContext(), "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    deleteSQLite();
                    refreshLocalData(l);
                    ToastR.setToast(getContext(), "删除群组成功");
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void refreshLocalData(long l) {
        Team team;
        for (int i = 0; i < groupList.size(); i++) {
            team = groupList.get(i);
            if (team.getTeamID() == l) {
                groupList.remove(team);
                if (groupSelectPosition > groupList.size() - 1) {
                    groupSelectPosition = groupList.size() - 1;
                    if (groupSelectPosition < 0) {
                        groupSelectPosition = 0;
                    }
                }

                if (groupAdapter != null) {
                    groupAdapter.seteData(groupList);
                    groupAdapter.notifyDataSetChanged();
                }

                if (allMemberMap.containsKey(l)) {
                    allMemberMap.remove(l);
                    List<AppliedFriends> listFriends = getlistMembersCache();
                    memberAdapter.setAppliedFriends(listFriends);
                    memberAdapter.setData(allMemberMap.get(groupList.get(groupSelectPosition)));
					memberAdapter.notifyDataSetChanged();//rs added for crash (LBCJW-258)
                }
                break;
            }
        }


    }

    /**
     * 数据库有数据，送数据库取，否则从网络取
     */
    private void getDBMsg() {
        isPullRefresh = true;
        try {
            DBManagerTeamList db = new DBManagerTeamList(getContext(), true, DBTableName.getTableName(getContext(), DBHelperTeamList.NAME));
            List<TeamInfo> mTeamInfo = db.getTeams();
            db.closeDB();
            //showLoading();
            if (true || mTeamInfo.size() <= 0) {
                Log.i(ServiceCheckUserEvent.TAG, "get team list = 0");
                loadTeamListFromNet();
            } else {
				GlobalStatus.setTeamsInfoList(mTeamInfo);
                convertViewGroupList(mTeamInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.TEAM_ACTION);
        filter.setPriority(Integer.MAX_VALUE);
        getContext().registerReceiver(myTeamReceiver, filter);
        filter = new IntentFilter();
        filter.addAction("ACTION.refreshTeamList");
        getContext().registerReceiver(refreshTeamReceiver, filter);
        filter = new IntentFilter();
        filter.addAction("ACTION.changeImage");
        getContext().registerReceiver(changgeImaReceiver, filter);

    }


    private void deleteSQLite() {
        MsgTool.deleteTeamMsg(getContext(), deletTeamID);
        deletTeamID = -1;
    }

    private void convertViewGroupList(List<TeamInfo> teamInfos) {
        DBManagerChatTimeList chatDB = new DBManagerChatTimeList(getContext(), true, DBTableName.getTableName(getContext(), DBHelperChatTimeList.NAME));
        Map<Long, Long> timeList = chatDB.getTimeList();
        chatDB.closeDB();
        String typedesc;
        String name;
        List<Team> oldGroupList = new ArrayList<>();
        oldGroupList.addAll(groupList);
        groupList.clear();
        for (TeamInfo in : teamInfos) {
            int i = in.getMemberRole();
            typedesc = in.getTeamDesc();
            name = in.getTeamName();
            //过滤掉海聊群
            if (in.getTeamType() == ProtoMessage.TeamType.teamBBS.getNumber()) {
                continue;
            }
            Team msg = new Team(name, typedesc, in.getTeamID(), in.getMemberRole(), false, in.getTeamType());
            groupList.add(msg);
        }

        // 排序(实现了中英文混排)
        AllTeamPinyinComparator comparator = new AllTeamPinyinComparator(timeList);
        Collections.sort(groupList, comparator);
        if (oldGroupList.size() == groupList.size() && groupList.size() > 0) {
            Team oldHead = oldGroupList.get(0);
            Team newHead = groupList.get(0);
            if (!TextUtils.equals(oldHead.getLinkmanName(), newHead.getLinkmanName())) {
                groupSelectPosition = 0;
                Log.d(TAG, "convertViewGroupList reset groupSelectPosition");
            }
        } else {
            groupSelectPosition = 0;
            Log.d(TAG, "convertViewGroupList reset groupSelectPosition");
        }
        getMembersData(groupList);
    }

    private boolean isTop(TeamInfo in) {
        return (boolean) SharedPreferencesUtils.get(getContext(), myPhone + in.getTeamID(), false);
    }

    private void setTop(long l, boolean top) {
        SharedPreferencesUtils.put(getContext(), myPhone + l, top);
    }

    @NonNull
    private String getMyPhone() {
        SharedPreferences preferences = getContext().getSharedPreferences("token", Context.MODE_PRIVATE);
        return preferences.getString("phone", "");
    }


    private void showTeamInfoDilog(List<TeamInfo> allTeamInfos, int id) {
        new ShowTeamInfoPrompt().dialogSTeamInfo(getMyActivity(), allTeamInfos.get(id));
    }

    /**
     * 获取所有群成员
     *
     * @param mTeamInfo
     */
    private void getMembersData(final List<Team> mTeamInfo) {
        allMemberMap.clear();
		if (mTeamInfo.size() == 0) {
			isPullRefresh = false;
			Log.i("TabFragmentLinkGroup", "no team info");
			return;
		}
        for (final Team teamInfo : mTeamInfo) {
            // 如果数据库中没有，从网络获取群成员
            ProtoMessage.AcceptTeam.Builder builder = ProtoMessage.AcceptTeam.newBuilder();
            builder.setTeamID(teamInfo.getTeamID());
            MyService.start(mContext, ProtoMessage.Cmd.cmdGetTeamMember.getNumber(), builder.build());
            IntentFilter filter = new IntentFilter();
            filter.addAction(TeamMemberProcesser.ACTION);
            final TimeoutBroadcast b = new TimeoutBroadcast(mContext, filter, getBroadcastManager());
            b.startReceiver(5/*TimeoutBroadcast.TIME_OUT_IIME*/, new ITimeoutBroadcast() { //rs change timeout from 60s->5s
                @Override
                public void onTimeout() {
                    //ToastR.setToast(mContext, "连接超时");
                    Log.d(TAG, "onTimeout Cmd.cmdGetTeamMember teamId = " + teamInfo.getTeamID());
                    //网络超时，从数据库中获取
                    TeamMemberHelper teamMemberHelper = new TeamMemberHelper(getContext(), teamInfo.getTeamID() + "TeamMember.dp", null);
                    SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
                    final Cursor cursor = db.query("LinkmanMember", null, null, null, null, null, null);
                    Log.d(TAG, "cursor=" + cursor.getCount());
                    if (cursor != null && cursor.getCount() > 0) { // 如果数据库里有，从数据库里取出群成员
                        List<TeamMemberInfo> memberInfos = new ArrayList<>();
                        if (cursor.moveToFirst()) {
                            do {
                                String user_phone = cursor.getString(cursor.getColumnIndex("user_phone"));
                                String user_name = cursor.getString(cursor.getColumnIndex("user_name"));
                                String nick_name = cursor.getString(cursor.getColumnIndex("nick_name"));
                                int role = cursor.getInt(cursor.getColumnIndex("role"));
                                int member_priority = cursor.getInt(cursor.getColumnIndex("member_priority"));

                                TeamMemberInfo memberInfo = new TeamMemberInfo();
                                memberInfo.setUserPhone(user_phone);
                                memberInfo.setUserName(user_name);
                                memberInfo.setNickName(nick_name);
                                memberInfo.setRole(role);
                                memberInfo.setMemberPriority(member_priority);
                                memberInfos.add(memberInfo);
                            } while (cursor.moveToNext());
                            allMemberMap.put(teamInfo.getTeamID(), memberInfos);
                            Log.d(TAG, "allMemberMap.size() = " + allMemberMap.size());
                            Log.d(TAG, "mTeamInfo.size() = " + mTeamInfo.size());
                            if (allMemberMap.size() == mTeamInfo.size()) {
                                if (mHandler != null) {
                                    mHandler.sendEmptyMessage(UPDATE_UI);
                                }
                            }
                        } else {
                            Log.d(TAG, "cursor.moveToFirst() = false");
                        }
                    } else {
                        //数据库中没有，放入空联系人
                        allMemberMap.put(teamInfo.getTeamID(), new ArrayList<TeamMemberInfo>());
                        Log.d(TAG, "allMemberMap.size() = " + allMemberMap.size());
                        Log.d(TAG, "mTeamInfo.size() = " + mTeamInfo.size());
                        if (allMemberMap.size() == mTeamInfo.size()) {
                            if (mHandler != null) {
                                mHandler.sendEmptyMessage(UPDATE_UI);
                            }
                        }
                    }

					db.close();//rs added
                }

                @Override
                public void onGot(Intent i) {
                    Log.d(TAG, "getGroupMan onGot ");
                    int code = i.getIntExtra("error_code", -1);
                    Log.d(TAG, "code = " + code);
                    if (code ==
                            ProtoMessage.ErrorCode.OK.getNumber()) {
                        TeamMemberHelper teamMemberHelper = new TeamMemberHelper(getContext(), teamInfo.getTeamID() + "TeamMember.dp", null);
                        SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
                        final Cursor mCursor = db.query("LinkmanMember", null, null, null, null, null, null);
                        if (mCursor != null) {
                            List<TeamMemberInfo> memberInfos = new ArrayList<>();
                            if (mCursor.moveToFirst()) {
                                do {
                                    String user_phone = mCursor.getString(mCursor.getColumnIndex("user_phone"));
                                    String user_name = mCursor.getString(mCursor.getColumnIndex("user_name"));
                                    String nick_name = mCursor.getString(mCursor.getColumnIndex("nick_name"));
                                    int role = mCursor.getInt(mCursor.getColumnIndex("role"));
                                    int member_priority = mCursor.getInt(mCursor.getColumnIndex("member_priority"));

									
									//rs added for check the del phoneNum show in the current member list LBCJW-203
									if(((mDelMemberTeamID != -1) && teamInfo.getTeamID() == mDelMemberTeamID) 
										&&(user_phone != null && mDelMemberPhone != null && user_phone.equals(mDelMemberPhone))){
										Log.d("rs", "found delete phoneNum:"+user_phone+"  with teamId:"+mDelMemberTeamID);
										mDelMemberTeamID = -1;
										continue;
									}
									//end

                                    TeamMemberInfo memberInfo = new TeamMemberInfo();
                                    memberInfo.setUserPhone(user_phone);
                                    memberInfo.setUserName(user_name);
                                    memberInfo.setNickName(nick_name);
                                    memberInfo.setRole(role);
                                    memberInfo.setMemberPriority(member_priority);
                                    memberInfos.add(memberInfo);
                                } while (mCursor.moveToNext());
                            }
                            allMemberMap.put(teamInfo.getTeamID(), memberInfos);
                            Log.d(TAG, "allMemberMap.size() = " + allMemberMap.size());
                            Log.d(TAG, "mTeamInfo.size() = " + mTeamInfo.size());
                            if (allMemberMap.size() == mTeamInfo.size()) {
                                if (mHandler != null) {
                                    mHandler.sendEmptyMessage(UPDATE_UI);
                                }
                            }
                        } else {
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    getMembersData(mTeamInfo);
                                }
                            }, 1000);
                        }

						db.close();//rs added
                    } else {
                        Log.e(TAG, "getGroupMan groupMan code:" + code);
                        new ResponseErrorProcesser(mContext, code);

						//rs added for get member from database if response is error
						Log.d(TAG, "Error response: Cmd.cmdGetTeamMember teamId = " + teamInfo.getTeamID());
	                    //网络返回错误，从数据库中获取
	                    TeamMemberHelper teamMemberHelper = new TeamMemberHelper(getContext(), teamInfo.getTeamID() + "TeamMember.dp", null);
	                    SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
	                    final Cursor cursor = db.query("LinkmanMember", null, null, null, null, null, null);
	                    Log.d(TAG, "cursor=" + cursor.getCount());
	                    if (cursor != null && cursor.getCount() > 0) { // 如果数据库里有，从数据库里取出群成员
	                        List<TeamMemberInfo> memberInfos = new ArrayList<>();
	                        if (cursor.moveToFirst()) {
	                            do {
	                                String user_phone = cursor.getString(cursor.getColumnIndex("user_phone"));
	                                String user_name = cursor.getString(cursor.getColumnIndex("user_name"));
	                                String nick_name = cursor.getString(cursor.getColumnIndex("nick_name"));
	                                int role = cursor.getInt(cursor.getColumnIndex("role"));
	                                int member_priority = cursor.getInt(cursor.getColumnIndex("member_priority"));

	                                TeamMemberInfo memberInfo = new TeamMemberInfo();
	                                memberInfo.setUserPhone(user_phone);
	                                memberInfo.setUserName(user_name);
	                                memberInfo.setNickName(nick_name);
	                                memberInfo.setRole(role);
	                                memberInfo.setMemberPriority(member_priority);
	                                memberInfos.add(memberInfo);
	                            } while (cursor.moveToNext());
								
	                            allMemberMap.put(teamInfo.getTeamID(), memberInfos);
	                            Log.d(TAG, "allMemberMap.size() = " + allMemberMap.size());
	                            Log.d(TAG, "mTeamInfo.size() = " + mTeamInfo.size());
	                            if (allMemberMap.size() == mTeamInfo.size()) {
	                                if (mHandler != null) {
	                                    mHandler.sendEmptyMessage(UPDATE_UI);
	                                }
	                            }
	                        } else {
	                            Log.d(TAG, "cursor.moveToFirst() = false");
	                        }
	                    } else {
	                        //数据库中没有，放入空联系人
	                        allMemberMap.put(teamInfo.getTeamID(), new ArrayList<TeamMemberInfo>());
	                        Log.d(TAG, "allMemberMap.size() = " + allMemberMap.size());
	                        Log.d(TAG, "mTeamInfo.size() = " + mTeamInfo.size());
	                        if (allMemberMap.size() == mTeamInfo.size()) {
	                            if (mHandler != null) {
	                                mHandler.sendEmptyMessage(UPDATE_UI);
	                            }
	                        }
	                    }

						db.close();//rs added
						//end

                    }
                }
            });
            // }
        }

    }

    class CloseRoomReceiiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    private class AllTeamPinyinComparator implements Comparator {
        private Map<Long, Long> chatTime;

        public AllTeamPinyinComparator(Map<Long, Long> time) {
            chatTime = time;
        }

        @Override
        public int compare(Object o1, Object o2) {

            Team contact1 = (Team) o1;
            Team contact2 = (Team) o2;
            long time1 = 0;
            long time2 = 0;
            // if (!Build.PRODUCT.contains("LB1728") && !"LB1822".equals(Build.PRODUCT)) {
                if (chatTime != null) {
                    if (chatTime.get(contact1.getTeamID()) != null) {
                        time1 = chatTime.get(contact1.getTeamID());
                    }

                    if (chatTime.get(contact2.getTeamID()) != null) {
                        time2 = chatTime.get(contact2.getTeamID());
                    }

                    if (time1 != 0 || time2 != 0) {
                        if (time1 < time2) {
                            return 1;
                        } else if (time1 == time2) {
                            return 0;
                        } else {
                            return -1;
                        }
                    }
                    // Log.e("wsTest","id:" + contact1.getTeamID());
                    // Log.e("wsTest","time1:" + time1);
                    // Log.e("wsTest","id:" + contact2.getTeamID());
                    // Log.e("wsTest","time2:" + time2);
                } else {
                    // Log.e("wsTest","chatTime == null");
                }
            // }
            String str = contact1.getLinkmanName();
            String str3 = contact2.getLinkmanName();

            // String str1 = Cn2Spell.getPinYin(str);
            // String str2 = Cn2Spell.getPinYin(str3);
            String str1 = ChineseToHanYuPYTest.convertChineseToPinyin(str, false);
            String str2 = ChineseToHanYuPYTest.convertChineseToPinyin(str3, false);
            // Log.e("wsTest", "str1:  " + str1 + "-----------------str2:  " + str2 + "   str 和 str3:   " + str + "---" + str3);
            int flag = str1.compareTo(str2);
            // Log.e("wsTest", "flag:" + flag);
            return flag;
        }
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    private void toModifyTeamInfo(final TeamInfo tm) {
        showModifyProgressDialog();
        ProtoMessage.TeamInfo.Builder builder = ProtoMessage.TeamInfo.newBuilder();
        builder.setTeamName(tm.getTeamName());
        // builder.setTeamType(tm.getTeamType());
        // builder.setTeamDesc(tm.getTeamDesc());
        // builder.setTeamPriority(tm.getTeamPriority());
		//rs modified for LBCJW-177:modify team name
        builder.setTeamID(tm.getTeamID());
        MyService.start(mContext, ProtoMessage.Cmd.cmdModifyTeamInfo.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(ModifyTeamInfoProcesser.ACTION);
        new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                dismissProgressDialog();
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                dismissProgressDialog();
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    //modifyDBitem(tm);
                	//groupAdapter.notifyDataSetChanged();//rs
                	loadTeamListFromNet();
                    ToastR.setToast(mContext, "修改群组名称成功");
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void showModifyProgressDialog () {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        mProgressDialog = new ProgressDialog(mContext, R.style.CustomDialog);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("请稍等...");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                getBroadcastManager().stopAll();
                ToastR.setToast(mContext, "取消修改群组名称");
            }
        });
    }

    private void modifyDBitem(TeamInfo tm) {
        try {
            DBManagerTeamList db = new DBManagerTeamList(mContext, DBTableName.getTableName(mContext, DBHelperTeamList.NAME));
            db.updateData(tm);
            db.closeDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goToMap(long groupId) {
        Intent mapIntent = new Intent(mContext, TeamMemberLocationActivity.class);
        mapIntent.putExtra("team_id", groupId);
        startActivity(mapIntent);
    }

    private void showChangeGroupNameDialog(final Team team) {
        LayoutInflater factory = LayoutInflater.from(mContext);
        final View view = factory.inflate(R.layout.dialog_change_group_name, null);
        final EditText nameEdit = view.findViewById(R.id.name_edit);
        final Button btnOk = view.findViewById(R.id.ok);
        final Button btnCancel = view.findViewById(R.id.cancel);
        final AlertDialog dialog = new AlertDialog.Builder(mContext).setView(view).create();
        dialog.show();
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            String name = nameEdit.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                ToastR.setToast(mContext, "请输入群组名称");
                return;
            }
            if (name.length() > 16) {
                ToastR.setToast(mContext, "群组名称长度不能超过16");
                return;
            }
            if (TextUtils.equals(name, team.getLinkmanName())) {
                ToastR.setToast(mContext, "群组名称未改变");
                return;
            }
            TeamInfo tmInfo = new TeamInfo();
            tmInfo.setTeamName(name);
			tmInfo.setTeamID(team.getTeamID());
			Log.d("rs", "toModifyTeamInfo->setTeamID:"+team.getTeamID());
            toModifyTeamInfo(tmInfo);
            dialog.dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

	    //rs added for LBCJW-181:refresh members after delete
		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			switch (requestCode) {
				case DELETE_TEAM_MEMBER:
					Log.d("rs", "onActivityResult->DELETE_TEAM_MEMBER");

					if (resultCode == Activity.RESULT_OK) {
                        mDelMemberPhone = data.getStringExtra("del_member_phone");
						mDelMemberTeamID = data.getLongExtra("group_id",-1);
						Log.d("rs", "onActivityResult->mDelMemberPhone:"+mDelMemberPhone+", mDelMemberTeamID:"+mDelMemberTeamID);
						//refreshMemberListLocalData(delMemberPhone);
						//loadTeamListFromNet();
					}
					break;
                case ADD_TEAM_MEMBER:
					//rs added for LBCJW-158, after added, sync the team members
					Log.d("rs", "onActivityResult->ADD_TEAM_MEMBER");

					if (resultCode == Activity.RESULT_OK) {
						Log.d("rs", "onActivityResult->ADD_TEAM_MEMBER->RESULT_OK");
						//loadTeamListFromNet();
					}
					break;
				default:
					break;
			}
		}

	/*
    private void refreshMemberListLocalData(String phoneNum) {
        int k = -1;
        int i = -1;

        List<TeamMemberInfo> memberList = allMemberMap.get(groupList.get(groupSelectPosition).getTeamID());
        for (TeamMemberInfo mte : memberList) {
            ++i;
            if (phoneNum.equals(mte.getUserPhone())) {
                k = i;
                Log.d("rs", "found phoneNum:"+phoneNum+", k:"+k);
                break;
            }
        }

        if (k >= 0) {
            if (memberList != null) {
                memberList.remove(k);
                List<AppliedFriends> listFriends = getlistMembersCache();
                memberAdapter.setAppliedFriends(listFriends);
                memberAdapter.setData(memberList);
                memberAdapter.notifyDataSetChanged();
            }
        }
    }
	*/
    //end

    private List<AppliedFriends> getlistMembersCache() {
        List<AppliedFriends> list = null;
        try {
            DBManagerFriendsList db = new DBManagerFriendsList(getContext(), DBTableName.getTableName(mContext, DBHelperFriendsList.NAME));
            list = db.getFriends(false);
            db.closeDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}