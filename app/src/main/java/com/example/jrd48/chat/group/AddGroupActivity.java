package com.example.jrd48.chat.group;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.MainActivity;
import com.luobin.dvr.R;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.AcceptGroupProcesser;
import com.example.jrd48.service.protocol.root.AppliedGroupListProcesser;
import com.example.jrd48.service.protocol.root.SearchGroupProcesser;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/7.
 */

public class AddGroupActivity extends BaseActivity implements View.OnClickListener {
    private PullToRefreshListView mPullRefreshListView;
    ListView listView;
    private GroupRequestAdapter adapter;
    private RelativeLayout mRelativeLayout;
    private View mView;
    private EditText etSearch;
    private TextView tvSearch;
    private ImageView mImageView;
    private Handler mHandler = new Handler();
    Button btnAddFriend;
    String getEditText;
    private static int listSize = 0;
    List<ViewFriendsMsg> teamList = new ArrayList<ViewFriendsMsg>();
    private boolean checkPullRefresh = false;

    class ViewFriendsMsg {
        public AppliedTeams teams;

        public ViewFriendsMsg() {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_new);
        initView();
    }

    private void initView() {
        etSearch = (EditText) findViewById(R.id.search_new_group);
        mRelativeLayout = (RelativeLayout) this.findViewById(R.id.re_search_group);
        mView = (View) this.findViewById(R.id.view_show);
        mRelativeLayout.setOnClickListener(this);
        tvSearch = (TextView) this.findViewById(R.id.tv_search);
        mImageView = (ImageView) findViewById(R.id.iv_back);
        btnAddFriend = (Button) findViewById(R.id.btn_add);
        mImageView.setOnClickListener(this);
        btnAddFriend.setOnClickListener(this);

        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_group_request_in_refresh_list);
        listView = mPullRefreshListView.getRefreshableView();
        /**
         * EditText监听
         */
        etSearch.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    mRelativeLayout.setVisibility(View.VISIBLE);
                    mView.setVisibility(View.VISIBLE);
                    tvSearch.setText(etSearch.getText().toString().trim());
                } else {

                    mRelativeLayout.setVisibility(View.GONE);
                    mView.setVisibility(View.GONE);
                    tvSearch.setText("");

                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {

            }
        });
        adapter = new GroupRequestAdapter(this, teamList);
        listView.setAdapter(adapter);
        loadGroupListFromNet(AddGroupActivity.this);
        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(AddGroupActivity.this, System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                checkPullRefresh = true;
                loadGroupListFromNet(AddGroupActivity.this);

            }


        });
        registerForContextMenu(listView);

    }

    private void loadGroupListFromNet(AddGroupActivity addGroup) {

        ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
        MyService.start(AddGroupActivity.this, ProtoMessage.Cmd.cmdAppliedTeamList.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppliedGroupListProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(AddGroupActivity.this, filter, getBroadcastManager());
        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                if (mPullRefreshListView != null)
                    mPullRefreshListView.onRefreshComplete();
                ToastR.setToast(AddGroupActivity.this, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (mPullRefreshListView != null) {
                    mPullRefreshListView.onRefreshComplete();
                }
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    AppliedTeamsList list = i.getParcelableExtra("get_applied_group_list");
                    if (list != null && list.getAppliedTeams() != null && list.getAppliedTeams().size() > 0) {
                        convertViewFriendList(teamList, list.getAppliedTeams());
                        refreshListData(teamList);
                        if (checkPullRefresh == true) {
                            ToastR.setToast(AddGroupActivity.this, "获取群组成功");
                            checkPullRefresh = false;
                        }
                    } else {
                        listSize = MainActivity.MENU_ZERO;
                        if (checkPullRefresh == true) {
                            ToastR.setToast(AddGroupActivity.this, "没有邀请或申请加群的列表信息");
                            checkPullRefresh = false;
                        }
                    }
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    public void fail(int i) {
        new ResponseErrorProcesser(AddGroupActivity.this, i);
    }

    /**
     * 刷新列表
     */
    private void refreshListData(List<ViewFriendsMsg> data) {
        try {
            listSize = data.size();
            if (adapter == null) {
                Log.d("MOBASSIST", "Excepton: refreshListData: Adapter is null!");
            } else {
                adapter.refresh(data);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 自定义DbAdapter
     */

    public class GroupRequestAdapter extends BaseAdapter {

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

        public GroupRequestAdapter(Context context, List<ViewFriendsMsg> mFriend) {
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

            GroupRequestAdapter.ViewHolder holder = null;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.applie_group_list_item, null);
                holder.tvName = (TextView) convertView.findViewById(R.id.request_in_text_friend_name);
                holder.tvMsg = (TextView) convertView.findViewById(R.id.request_in_text_friend_msg);
                holder.btnRefuse = (Button) convertView.findViewById(R.id.request_in_refuse);
                holder.btnAccept = (Button) convertView.findViewById(R.id.request_in_accept);
                holder.mShowImage = (ImageView) convertView.findViewById(R.id.request_in_image);
                convertView.setTag(holder);

            } else {
                holder = (GroupRequestAdapter.ViewHolder) convertView.getTag();
            }
            if (friendsMsg == null) {
                return null;
            }
            final int teamType = friendsMsg.teams.getApplyType();
            if (teamType == ProtoMessage.ApplyTeamType.attApply_VALUE) {
                holder.tvMsg.setText("申请加入" + friendsMsg.teams.getTeamName() + "群组");
            } else {
                holder.tvMsg.setText("邀请您加入" + friendsMsg.teams.getTeamName() + "群组");
            }
            holder.tvName.setText(friendsMsg.teams.getUserName());

            holder.btnRefuse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refuseAdd(position, friendsMsg);
                }
            });
            holder.btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    accepGroupsRequest(position, friendsMsg, teamType);
                }
            });

            return convertView;
        }

        public class ViewHolder {
            // public TextView tvNumber;
            public TextView tvName;
            public TextView tvMsg;
            public Button btnRefuse;
            public Button btnAccept;
            public ImageView mShowImage;
        }

    }

    public void refuseOrAccept(final int position, final ViewFriendsMsg friendsMsg, final int type, String remark) {
        ProtoMessage.AcceptTeam.Builder builder = ProtoMessage.AcceptTeam.newBuilder();
        builder.setTeamID(friendsMsg.teams.getTeamID());
        builder.setAcceptType(type);
        builder.setApplyType(friendsMsg.teams.getApplyType());
        if (friendsMsg.teams.getApplyType() == ProtoMessage.ApplyTeamType.attApply_VALUE) {
            builder.setPhoneNum(friendsMsg.teams.getUserPhone());
        } else {
            SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
            String myPhone = preferences.getString("phone", "");
            builder.setPhoneNum(myPhone);
        }
        MyService.start(AddGroupActivity.this, ProtoMessage.Cmd.cmdAcceptTeam.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(AcceptGroupProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(AddGroupActivity.this, filter, getBroadcastManager());
        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                ToastR.setToast(AddGroupActivity.this, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    backSuccess(type);
                    clearListorCache(position, friendsMsg);
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    //拒绝和接受失败提示
    public void backFail(int type) {
        if (type == 1) {
            ToastR.setToast(AddGroupActivity.this, "拒绝失败");
        } else {
            ToastR.setToast(AddGroupActivity.this, "接受失败");
        }
    }

    //拒绝和接受成功提示
    public void backSuccess(int type) {
        if (type == 1) {
            ToastR.setToast(AddGroupActivity.this, "拒绝成功");
        } else {
            ToastR.setToast(AddGroupActivity.this, "接受成功");
            AddGroupActivity.this.sendBroadcast(new Intent(MainActivity.TEAM_ACTION));
        }
    }

    /**
     * 清除缓存数据
     *
     * @param position
     * @param groupMsg
     */
    protected void clearListorCache(int position, ViewFriendsMsg groupMsg) {
        List<ViewFriendsMsg> data = adapter.getList();
        int k = -1;
        int i = -1;
        for (ViewFriendsMsg c : data) {
            ++i;
            if (c.teams.getTeamID() == groupMsg.teams.getTeamID()) {
                k = i;
//                doDeleteCarByCarID(groupMsg);
                break;
            }
        }
        if (k >= 0) {
            data.remove(k);
            if (data.size() == 0) {
                finish();
            }
            refreshListData(data);
        }

    }

    /*
    接受添加组
     */
    private void accepGroupsRequest(final int position, final ViewFriendsMsg friendsMsg, int teamType) {
//        LayoutInflater factory = LayoutInflater.from(AddGroupActivity.this);// 提示框
//        final View view = factory.inflate(R.layout.accept_friend_editbox_layout, null);// 这里必须是final的
//        final EditText editRemark = (EditText) view.findViewById(R.id.remark_editText);// 获得输入框对象
        String msg;
        if (teamType == ProtoMessage.ApplyTeamType.attApply_VALUE) {
            msg = "允许" + friendsMsg.teams.getUserName() + "加入" + friendsMsg.teams.getTeamName() + "群组";
        } else {
            msg = "同意加入" + friendsMsg.teams.getTeamName() + "群组";
        }

        new AlertDialog.Builder(AddGroupActivity.this).setTitle("提示：")// 提示框标题
                .setMessage(msg).setPositiveButton("确定",
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String remark = "";
                        //= editRemark.getText().toString().trim();
                        refuseOrAccept(position, friendsMsg, ProtoMessage.AcceptType.atAccept_VALUE, remark);
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
    拒绝添加组
     */
    private void refuseAdd(int position, ViewFriendsMsg friendsMsg) {
        String remark = "";
        refuseOrAccept(position, friendsMsg, ProtoMessage.AcceptType.atDeny_VALUE, remark);

    }

    /**
     * 缓存数据到本地
     *
     * @param te
     * @param list
     */
    public void convertViewFriendList(List<ViewFriendsMsg> te, List<AppliedTeams> list) {
        te.clear();
        for (AppliedTeams c : list) {
            ViewFriendsMsg temp = new ViewFriendsMsg();
            temp.teams = c;
            te.add(temp);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.re_search_group:
                searchGroup();
                break;
            case R.id.iv_back:
                Intent intent = new Intent();
                setResult(listSize, intent);
                finish();
                break;
            case R.id.btn_add:
                Intent i = new Intent(AddGroupActivity.this, CreateGroupActivity.class);
                AddGroupActivity.this.startActivityForResult(i, 3);
//                finish();
                break;
        }

    }

    /**
     * 重写返回键功能
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Intent intent = new Intent();
            setResult(listSize, intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    private void searchGroup() {
        String msg = etSearch.getText().toString().trim();
        if (msg.length() <= 0) {
            ToastR.setToast(AddGroupActivity.this, "请输入数据");
            return;
        }
        ProtoMessage.TeamInfo.Builder builder = ProtoMessage.TeamInfo.newBuilder();
        builder.setTeamName(msg);
        MyService.start(AddGroupActivity.this, ProtoMessage.Cmd.cmdSearchTeam.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(SearchGroupProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(AddGroupActivity.this, filter, getBroadcastManager());

        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(AddGroupActivity.this, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    TeamInfoList teList = i.getParcelableExtra("get_seach_group_list");
                    if (teList != null) {
                        searchSuccess(teList);
                        ToastR.setToast(AddGroupActivity.this, "搜索群组成功");
                    } else {
                        ToastR.setToast(AddGroupActivity.this, "未找到该群组");
                    }
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });

    }

    public void searchSuccess(TeamInfoList teList) {
        Intent i = new Intent(AddGroupActivity.this, ShowSearchGroupActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("intent_seach_group_list", teList);
        i.putExtras(bundle);
        AddGroupActivity.this.startActivityForResult(i, 2);
//        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        switch (requestCode) {
            case 2:
                if (resultCode == RESULT_OK) {
                    if (data.getIntExtra("data", 0) == 1) {
                        Intent intent = new Intent();
                        setResult(listSize, intent);
                        finish();
                    }
                    if (data.getIntExtra("data", 0) == 0) {
                        etSearch.setText("");
                    }
                }
                break;
            case 3:
                if (resultCode == RESULT_OK) {
                    if (data.getIntExtra("data", 0) == 1) {
                        Intent intent = new Intent();
                        setResult(listSize, intent);
                        finish();
                    }
                    if (data.getIntExtra("data", 0) == 0) {
                        etSearch.setText("");
                    }
                }
                break;
            default:
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPullRefreshListView != null) {
            mPullRefreshListView.onRefreshComplete();
        }
    }

}
