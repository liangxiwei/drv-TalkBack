package com.luobin.ui.TalkBackSearch;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.AllTeamMember;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.friend.AddFriendPrompt;
import com.example.jrd48.chat.friend.AddFriendPromptListener;
import com.example.jrd48.chat.friend.ShowFriendMsgActivity;
import com.example.jrd48.chat.search.Cn2Spell;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.ApplyFriendProcesser;
import com.example.jrd48.service.protocol.root.TypeSearchFriendsProcesser;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.luobin.dvr.R;
import com.luobin.model.SearchFriendsCondition;
import com.luobin.model.SearchStrangers;
import com.luobin.search.friends.DistanceSearchReturnActivity;
import com.luobin.search.friends.SearchFriendsByConditionActivity;
import com.luobin.search.friends.SearchReturnActivity;
import com.luobin.ui.BaseDialogActivity;
import com.luobin.ui.TalkBackSearch.adapter.TSConditionPersionAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

/**
 * @author wangjunjie
 */
public class TSConditionPersonActivity extends BaseDialogActivity implements
        ClickInterFace {

    boolean showDialog = true;

    @BindView(R.id.imgClose)
    ImageView imgClose;
    @BindView(R.id.list)
    PullToRefreshListView list;

    ListView listView = null;

    private Context context = null;
    private SearchFriendsCondition searchFriendsCondition;
    ArrayList<SearchStrangers> userInfoList;

    TSConditionPersionAdapter adapter = null;

    private ProgressDialog m_pDialog;
    private int REQUST_TYPE = 0;
    boolean checkDialog = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tscondition_person);
        ButterKnife.bind(this);
        context = this;
        getIntentMsg();

        SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        String myPhone = preferences.getString("phone", "");
        userNameMe = preferences.getString("name", myPhone);


        listView = list.getRefreshableView();
        adapter = new TSConditionPersionAdapter(this, userInfoList);
        listView.setAdapter(adapter);
        adapter.setClickInterFace(this);
        getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);

        list.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                showDialog = false;
//                getGroupMan(teamID);
                toSearchFriends();
            }
        });

        initDialog();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (list != null) {
            list.onRefreshComplete();
        }
    }

    private void initDialog() {
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
        m_pDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (checkDialog) {
                    getBroadcastManager().stopAll();
                    switch (REQUST_TYPE) {
                        case 1:
                            ToastR.setToast(context, "取消删除好友");
                            break;
                        case 2:
                            ToastR.setToast(context, "取消设置话权");
                            break;
                        case 3:
                            ToastR.setToast(context, "取消设置管理员");
                            break;
                        case 4:
                            ToastR.setToast(context, "取消修改昵称");
                            break;
                        case 5:
                            ToastR.setToast(context, "取消解散群组");
                            break;
                        case 6:
                            ToastR.setToast(context, "取消删除并退出群组");
                            break;
                        case 7:
                            if (showDialog) {
                                ToastR.setToast(context, "取消获取群成员");
                            }
                            break;
                        case 8:
                            ToastR.setToast(context, "取消添加好友");
                            break;
                        default:
                            break;
                    }

                }
            }
        });

    }

    private void getIntentMsg() {
        Intent intent = getIntent();
        searchFriendsCondition = (SearchFriendsCondition) intent.getSerializableExtra("conditon");
        userInfoList = (ArrayList<SearchStrangers>) intent.getSerializableExtra("user_info");
        // 排序(实现了中英文混排)
        AllTeamPinyinComparator comparator = new AllTeamPinyinComparator();
        Collections.sort(userInfoList, comparator);
        setIntent(null);
    }


    @OnClick({R.id.imgClose})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgClose:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void itemClick(View view, int postion) {
        SearchStrangers searchStrangers = userInfoList.get(postion);
        //TODO 添加好友
        dialogFriendsRequest(searchStrangers);

    }

    class AllTeamPinyinComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            SearchStrangers contact1 = (SearchStrangers) o1;
            SearchStrangers contact2 = (SearchStrangers) o2;

            String str = contact1.getUserName();
            String str3 = contact2.getUserName();

            String str1 = Cn2Spell.getPinYin(str);
            String str2 = Cn2Spell.getPinYin(str3);
//        Log.i("log", "str1:  " + str1 + "-----------------str2:  " + str2);
            int flag = str1.compareTo(str2);

            return flag;
        }
    }

    private void toSearchFriends() {

        ProtoMessage.MsgSearchCar.Builder builder = ProtoMessage.MsgSearchCar.newBuilder();
        builder.setProv(searchFriendsCondition.getmProvince());
        builder.setCity(searchFriendsCondition.getmCity());
        builder.setTown(searchFriendsCondition.getmTown());
        builder.setCarType1(searchFriendsCondition.getCarBrand());
        builder.setCarType2(searchFriendsCondition.getCarType1());
        builder.setCarType3(searchFriendsCondition.getCarType2());
        builder.setSex(searchFriendsCondition.getmSex());

        if (searchFriendsCondition.getCarPlateNumber().equals(SearchFriendsByConditionActivity.NOT_SET)) {
            builder.setCarNum("");
        } else {
            builder.setCarNum(searchFriendsCondition.getCarPlateNumber());
        }
        builder.setPos(1);

        MyService.start(context, ProtoMessage.Cmd.cmdSearchCar.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(TypeSearchFriendsProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(context, filter, getBroadcastManager());
        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(context, "连接超时");
                if (list != null) {
                    list.onRefreshComplete();
                }
            }

            @Override
            public void onGot(Intent i) {
                if (list != null) {
                    list.onRefreshComplete();
                }
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK_VALUE) {
                    userInfoList = (ArrayList<SearchStrangers>) i.getSerializableExtra("user_info");
                    AllTeamPinyinComparator comparator = new AllTeamPinyinComparator();
                    Collections.sort(userInfoList, comparator);
                    adapter.refresh(userInfoList);

                } else {
                    new ResponseErrorProcesser(context, i.getIntExtra("error_code", -1));
                }

            }
        });
    }

    public void dismissDialogShow() {
        checkDialog = false;
        m_pDialog.dismiss();
    }

    public void showMyDialog(int type) {
        REQUST_TYPE = type;
        checkDialog = true;
        m_pDialog.show();
    }

    private String userNameMe;

    /**
     * 添加好友提示框
     */
    private void dialogFriendsRequest(final SearchStrangers searchStrangers) {
        try {
            String msg = "，请求加您为好友，谢谢。";
            AddFriendPrompt.dialogFriendsRequest(this, msg,
                    searchStrangers.getUserName(), userNameMe, new AddFriendPromptListener() {
                @Override
                public void onOk(String remark, String msg) {
                    addFriendsRequest(remark, msg, searchStrangers.getPhoneNum());
                }

                @Override
                public void onFail(String typ) {
                    if (typ.equals(AddFriendPrompt.TYP)) {
                        ToastR.setToast(context, "信息输入不能为空");
                    } else {
                        ToastR.setToast(context, "备注输入过长（最大只能设置16个字符）");
                    }
                }
            });
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 加好友网络请求
     *
     * @param remark
     * @param msg    phoneNum
     */
    private void addFriendsRequest(String remark, String msg, String phoneNum) {
        ProtoMessage.ApplyFriend.Builder builder = ProtoMessage.ApplyFriend.newBuilder();
        builder.setFriendPhoneNum(phoneNum);
        builder.setApplyInfo(msg);
        builder.setApplyRemark(remark);
        MyService.start(this, ProtoMessage.Cmd.cmdApplyFriend.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(ApplyFriendProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(this, filter, getBroadcastManager());

        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                ToastR.setToast(context, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    successBack();
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    public void successBack() {
        ToastR.setToast(this, "请求成功，等待对方回应");

    }

    public void fail(int i) {
        new ResponseErrorProcesser(this, i);
    }


}
