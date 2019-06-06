package com.example.jrd48.chat.search;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.ChineseToHanYuPYTest;
import com.example.jrd48.chat.SQLite.TeamMemberHelper;
import com.example.jrd48.chat.TeamMemberInfo;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.ViewUtil;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.friend.FriendsDetailsActivity;
import com.example.jrd48.chat.friend.MatchPhoneNumberActivity;
import com.example.jrd48.chat.friend.ShowFriendMsgActivity;
import com.example.jrd48.chat.group.ContainsEmojiEditText;
import com.example.jrd48.chat.group.DBHelperTeamList;
import com.example.jrd48.chat.group.DBManagerTeamList;
import com.example.jrd48.chat.group.ShowAllTeamMemberActivity;
import com.example.jrd48.chat.group.ShowSearchGroupActivity;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.TeamInfoList;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.SearchGroupProcesser;
import com.example.jrd48.service.protocol.root.SearchStrangerProcesser;
import com.luobin.dvr.R;
import com.luobin.model.SearchStrangers;
import com.luobin.model.ViewAllCarParam;
import com.luobin.search.friends.AmapSearchActivity;
import com.luobin.search.friends.SearchFriendsByConditionActivity;
import com.luobin.utils.ButtonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/2/6.
 */

public class SearchActivity extends BaseActivity implements View.OnClickListener {
    private EditText mEditText;
    private ListView mListView;
    private TextView mTvCancel;
    private Context mContext;
    private RelativeLayout mRelativeLayoutTitle;
    private RelativeLayout mRelativeLayoutShow;
    private RelativeLayout mRelativeLayoutSearch;
    private RelativeLayout mRelativeLayoutSearchFriends;
    private RelativeLayout mRelativeLayoutNearbyCar;
    private FrameLayout mFrameLayoutInput;
    private RelativeLayout mRelativeLayoutMatchPhone;
    private static String TAG = "SearchActivity";
    public static String strName = "name";
    public static String strPhone = "phone";
    public static String linkTitle = "标题";
    public static String linkSearch = "搜索";
    public static String linkMan = "联系人";
    public static String linkTeam = "群组";
    private ProgressDialog m_pDialog;
    private boolean checkDialog = true;
    private SearchListItemAdapter mSearchListItemAdapter;
    List<TeamInfo> mTeamInfo = new ArrayList<TeamInfo>();
    List<SearchFriends> mAppliedFriends = new ArrayList<SearchFriends>();
    List<SearchFriends> mNewAppliedFriends = new ArrayList<SearchFriends>();
    List<SearchFriends> mNewAppliedFriends1 = new ArrayList<SearchFriends>();
    List<SearchFriends> mNewAppliedFriends2 = new ArrayList<SearchFriends>();
    List<SearchFriends> searTeams = new ArrayList<SearchFriends>();
    List<SearchFriends> mNewList = new ArrayList<SearchFriends>();
    private ViewAllCarParam param;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);
        mContext = this;
        getDBMsg();
        getTeamDBMsg();
        initView();
        editTextChangeListener();
        ListViewListener();
    }


    @Override
    protected void onResume() {
        if(mEditText.getText().length() > 0) {
            toChangeView(false);
        }else {
//            toChangeView(true);
        }
        super.onResume();
    }

    private void getTeamDBMsg() {
        try {
            DBManagerTeamList db = new DBManagerTeamList(mContext, DBTableName.getTableName(mContext, DBHelperTeamList.NAME));
            mTeamInfo = db.getTeams();
            db.closeDB();
//            mTeamInfo.clear();
//            for (TeamInfo a : af) {
//                mTeamInfo.add(a);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (TeamInfo ti : mTeamInfo) {
//            ti.setPinYinHeadChar(Cn2Spell.getPinYinHeadChar(ti.getTeamName()));
//            ti.setPinyin(Cn2Spell.getPinYin(ti.getTeamName()));
            ti.setPinYinHeadChar(ChineseToHanYuPYTest.convertChineseToPinyin(ti.getTeamName(), true));
            ti.setPinyin(ChineseToHanYuPYTest.convertChineseToPinyin(ti.getTeamName(), false));
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                ButtonUtils.changeLeftOrRight(true);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                ButtonUtils.changeLeftOrRight(false);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideSoftInputFromWindow();
    }

    private void ListViewListener() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                if (!mNewList.get(position).getSearchType().equals(linkTitle)) {
                    if (mNewList.get(position).getSearchType().equals(linkMan)) {
                        AppliedFriends appliedFriends = null;
                        try {
                            DBManagerFriendsList db = new DBManagerFriendsList(mContext, DBTableName.getTableName(mContext, DBHelperFriendsList.NAME));
                            appliedFriends = db.getFriend(mNewList.get(position).getPhoneNum());
                            db.closeDB();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (appliedFriends == null){
                            Log.e(TAG,"appliedFriends is null");
                            return;
                        }
                        Intent intent = new Intent(mContext, FriendsDetailsActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("friend_detail", appliedFriends);
                        intent.putExtras(bundle);
                        startActivity(intent);

                    } else if (mNewList.get(position).getSearchType().equals(linkTeam)) {


                        Intent intent = new Intent(mContext, ShowAllTeamMemberActivity.class);
                        TeamInfo teamInfo = null;
                        for (TeamInfo info : mTeamInfo) {
                            if (mNewList.get(position).getTeamID().equals(info.getTeamID())) {
                                teamInfo = info;
                                break;
                            }
                        }

                        Bundle mBundle = new Bundle();
                        mBundle.putParcelable("team_desc", teamInfo);
                        intent.putExtras(mBundle);
                        startActivity(intent);


//                        SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
//                        String myPhone = preferences.getString("phone", "");
//                        MsgRecordHelper msgRecordHelper = new MsgRecordHelper(mContext, myPhone + "MsgShow.dp", null);
//                        SQLiteDatabase db = msgRecordHelper.getWritableDatabase();
//                        ContentValues values = new ContentValues();
//                        values.put("new_msg", 0);
//                        db.update("Msg", values, "group_id = ?", new String[]{mNewList.get(position).getTeamID() + ""});
//                        db.close();
//
//                        NotificationManager nm = (NotificationManager) getSystemService(mContext.NOTIFICATION_SERVICE);
//                        nm.cancel(0);//消除对应ID的通知
//
//                        new InitDataBroadcast(mContext).sendBroadcast("");
//                        Intent intent = new Intent(mContext, FirstActivity.class);
//                        intent.putExtra("data", 0);
//                        intent.putExtra("group", mNewList.get(position).getTeamID());
//                        intent.putExtra("type", mNewList.get(position).getMemberRole());
//                        String name = mNewList.get(position).getTeamName();
//                        intent.putExtra("group_name", name);
//                        startActivity(intent);
                    } else if (mNewList.get(position).getSearchType().equals(SearchActivity.linkSearch)) {
                        if (mNewList.get(position).getType() != null && mNewList.get(position).getType().equals(SearchActivity.linkSearch)) {
                            searchGroup();
                        } else {
                            String phoneOrName = mEditText.getText().toString().trim();
                            searchFriend(phoneOrName);
                        }
                    }
                }else {

                }
            }
        });


    }

    private void getDBMsg() {
        try {
            DBManagerFriendsList db = new DBManagerFriendsList(mContext, DBTableName.getTableName(mContext, DBHelperFriendsList.NAME));
            List<AppliedFriends> af = db.getFriends(false);
            db.closeDB();
            mAppliedFriends.clear();
            for (AppliedFriends a : af) {
                String friendName = a.getNickName();
                if (friendName == null || friendName.equals("")) {
                    friendName = a.getUserName();
                }
                if (friendName == null || friendName.equals("")) {
                    friendName = a.getPhoneNum();
                }
                SearchFriends apply = new SearchFriends();
                apply.setUserName(friendName);
                apply.setPhoneNum(a.getPhoneNum());
                apply.setFriendStar(a.getFriendStar());
                apply.setNickName(a.getNickName());
                apply.setUserSex(a.getUserSex());
//                apply.setPinYinHeadChar(Cn2Spell.getPinYinHeadChar(friendName));
//                apply.setPinyin(Cn2Spell.getPinYin(friendName));
                apply.setPinYinHeadChar(ChineseToHanYuPYTest.convertChineseToPinyin(friendName, true));
                apply.setPinyin(ChineseToHanYuPYTest.convertChineseToPinyin(friendName, false));
                mAppliedFriends.add(apply);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        mRelativeLayoutTitle = (RelativeLayout) findViewById(R.id.title);
        mRelativeLayoutShow = (RelativeLayout) findViewById(R.id.rl_show_title);
        mRelativeLayoutShow.setOnClickListener(this);
        mFrameLayoutInput = (FrameLayout) findViewById(R.id.fl_input);
        mRelativeLayoutSearch = (RelativeLayout) findViewById(R.id.ll_search);
        mRelativeLayoutSearchFriends = (RelativeLayout) findViewById(R.id.re_search_friends);
        mRelativeLayoutSearchFriends.setOnClickListener(this);
        mRelativeLayoutNearbyCar = (RelativeLayout) findViewById(R.id.re_nearby_car);
        mRelativeLayoutNearbyCar.setOnClickListener(this);
        mRelativeLayoutMatchPhone = (RelativeLayout) findViewById(R.id.re_match_phone);
        mRelativeLayoutMatchPhone.setOnClickListener(this);
        mListView = (ListView) findViewById(R.id.lv_search);
        mEditText = (EditText) findViewById(R.id.et_search);
        mTvCancel = (TextView) findViewById(R.id.tv_cancel);
        mEditText.setFocusable(true);
        mTvCancel.setFocusable(true);
        mTvCancel.setOnClickListener(this);
        Drawable drawable1 = getResources().getDrawable(R.drawable.search);
        int s = ViewUtil.dip2px(SearchActivity.this, 32);
        drawable1.setBounds(0, 0, s, s);//第一0是距左边距离，第二0是距上边距离，32分别是长宽
        mEditText.setCompoundDrawables(drawable1, null, null, null);//只放左边
        mEditText.setOnFocusChangeListener(new android.view.View.
                OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // 此处为得到焦点时的处理内容
                    showSoftInputFromWindow();
                    Log.d("jim","获取焦点");
                    mEditText.requestFocus();
//                    mTvCancel.setFocusable(false);
//                    mListView.setFocusable(false);
                } else {
                    // 此处为失去焦点时的处理内容
                    Log.d("jim","没有焦点");
                    hideSoftInputFromWindow();
//                    mTvCancel.setFocusable(true);
//                    mTvCancel.requestFocus();
                }
            }
        });

        mSearchListItemAdapter = new SearchListItemAdapter(this, mNewAppliedFriends);
        mListView.setAdapter(mSearchListItemAdapter);
        mListView.setSelector(R.drawable.dvr_listview_background);

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
                    ToastR.setToast(mContext, "取消在线搜索");
                }
            }
        });
        //********************************************弹窗设置****************************************************

    }

    private void editTextChangeListener() {
        mEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
//                if (s.toString().length() > 0) {
//                    mRelativeLayoutFriend.setVisibility(View.VISIBLE);
//                    mRelativeLayoutTeam.setVisibility(View.VISIBLE);
//                    mTextViewFriend.setText(s.toString());
//                    mTextViewTeam.setText(s.toString());
//                } else {
//                    mRelativeLayoutFriend.setVisibility(View.GONE);
//                    mRelativeLayoutTeam.setVisibility(View.GONE);
//                }
                Editable edit = mEditText.getText();
                int index = mEditText.getSelectionStart() - 1;
//                if (index > 0) {
//                    if (!ContainsEmojiEditText.isEmojiCharacter(s.charAt(index))) {
//                        edit.delete(index, index + 1);
//                    }
//                }
                checkUserByChinese(edit.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }
        });
    }

    StringBuffer sb = new StringBuffer();

    private void checkUserByChinese(CharSequence s) {

        mNewAppliedFriends.clear();
        mNewAppliedFriends1.clear();
//        list.clear();
        for (SearchFriends af : mAppliedFriends) {
            if (s.length() > 0) {
                String pinYinHeadChar = af.getPinYinHeadChar();
                String pinYin = af.getPinyin();

                if (af.getUserName().contains(s)) {
                    af.setType(strName);
                    af.setSearchType(linkMan);
                    mNewAppliedFriends.add(af);
                } else if (pinYinHeadChar.contains(s) && !s.toString().matches("^[0-9]*$")) {
                    //简拼搜索
                    af.setType(strName);
                    af.setSearchType(linkMan);
                    String name = af.getUserName();
                    int index = pinYinHeadChar.indexOf(s.toString());
                    String str = name.substring(index, s.toString().length() + index);
                    if (index >= 0 && s.toString().length() > 0) {
                        af.setKeyword(name.substring(index, s.toString().length() + index));
                    }
                    mNewAppliedFriends.add(af);
                } else if (pinYin.contains(s) && !s.toString().matches("^[0-9]*$") && s.length() >= 2) {
                    //全拼拼搜索
                    sb.setLength(0);
                    af.setType(strName);
                    af.setSearchType(linkMan);
                    String name = af.getUserName();
                    String str = s.toString();
                    int t = str.length();
//                    NameMatchPinYing nameMatchPinYing = Cn2Spell.getNameMatchPinYing(name);
                    NameMatchPinYing nameMatchPinYing = ChineseToHanYuPYTest.getNameMatchPinYing(name);
                    List<String> nameS = nameMatchPinYing.getName();
                    List<String> pinyinlistS = nameMatchPinYing.getPinyinlist();
                    for (int i = 0; i < pinyinlistS.size(); i++) {
                        if (i > 0 && str.length() >= pinyinlistS.get(i - 1).length() && str.contains(pinyinlistS.get(i - 1))) {
                            str = str.substring(pinyinlistS.get(i - 1).length(), str.length());
                        }
                        if (str == null || str.length() <= 0) {
                            break;
                        }
                        if (pinyinlistS.get(i).contains(str) || str.contains(pinyinlistS.get(i))) {
                            if (str.length() >= pinyinlistS.get(i).length()) {
                                sb.append(nameS.get(i));
                                t = str.length();
                            } else if (str.length() > 0) {
                                sb.append(nameS.get(i));
                                break;
                            }
                        }
                    }
                    af.setKeyword(sb.toString());
                    mNewAppliedFriends.add(af);
                } else if (s.toString().matches("^[0-9]*$") && af.getPhoneNum().contains(s)) {
                    af.setType(strPhone);
                    af.setSearchType(linkMan);
                    mNewAppliedFriends1.add(af);
                }


            } else {
                break;
            }
        }
        setRefreshData(mNewAppliedFriends, mNewAppliedFriends1, s);
//        mNewAppliedFriends1 = removeDuplicate(mNewAppliedFriends);
    }

    private void setRefreshData(List<SearchFriends> mNewAppliedFriends, List<SearchFriends> mNewAppliedFriends1, CharSequence s) {
        mNewAppliedFriends2.clear();
        mNewList.clear();
        if (s.length() > 0) {
            SearchFriends sf = new SearchFriends();
            sf.setSearchType(linkSearch);
            sf.setUserName(s.toString());
            mNewList.add(sf);
            SearchFriends friend = new SearchFriends();
            friend.setSearchType(linkSearch);
            friend.setUserName(s.toString());
            friend.setType(linkSearch);
            mNewList.add(friend);
        }
        for (SearchFriends af : mNewAppliedFriends) {
            mNewAppliedFriends2.add(af);
        }
        for (SearchFriends af : mNewAppliedFriends1) {
            mNewAppliedFriends2.add(af);
        }
        if (mNewAppliedFriends2.size() > 0) {
            SearchFriends a = new SearchFriends();
            a.setUserName(linkMan);
            a.setSearchType(linkTitle);
            mNewList.add(a);
            for (SearchFriends af : mNewAppliedFriends2) {
                mNewList.add(af);
            }
        }
        setTeamData(s);
//        mSearchListItemAdapter.refresh(mNewList, s.toString());
    }


    private void setTeamData(CharSequence s) {
        searTeams.clear();
        for (TeamInfo te : mTeamInfo) {

            if (s.length() > 0) {
                if (te.getTeamName().contains(s)) {
                    SearchFriends sf = new SearchFriends();
                    sf.setSearchType(linkTeam);
                    sf.setTeamID(te.getTeamID());
                    sf.setMemberRole(te.getMemberRole());
                    sf.setTeamName(te.getTeamName());
                    searTeams.add(sf);
                } else if (te.getPinYinHeadChar().contains(s.toString()) && !s.toString().matches("^[0-9]*$")) {
                    //简拼搜索
                    SearchFriends sf = new SearchFriends();
                    String name = te.getTeamName();
                    int index = te.getPinYinHeadChar().indexOf(s.toString());
                    if (index >= 0 && s.toString().length() > 0) {
                        sf.setKeyword(name.substring(index, s.toString().length() + index));
                        sf.setSearchType(linkTeam);
                        sf.setTeamID(te.getTeamID());
                        sf.setMemberRole(te.getMemberRole());
                        sf.setTeamName(te.getTeamName());
                        searTeams.add(sf);
                    }

                } else if (te.getPinyin().contains(s) && !s.toString().matches("^[0-9]*$") && s.length() >= 2) {
                    //全拼拼搜索
                    SearchFriends sf = new SearchFriends();
                    sb.setLength(0);
                    String name = te.getTeamName();
                    String str = s.toString();
//                    NameMatchPinYing nameMatchPinYing = Cn2Spell.getNameMatchPinYing(name);
                    NameMatchPinYing nameMatchPinYing = ChineseToHanYuPYTest.getNameMatchPinYing(name);
                    List<String> nameS = nameMatchPinYing.getName();
                    List<String> pinyinlistS = nameMatchPinYing.getPinyinlist();
                    for (int i = 0; i < pinyinlistS.size(); i++) {
                        if (i > 0 && str.length() >= pinyinlistS.get(i - 1).length() && str.contains(pinyinlistS.get(i - 1))) {
                            str = str.substring(pinyinlistS.get(i - 1).length(), str.length());
                        }
                        if (str == null || str.length() <= 0) {
                            break;
                        }
                        if (pinyinlistS.get(i).contains(str) || str.contains(pinyinlistS.get(i))) {
                            if (str.length() >= pinyinlistS.get(i).length()) {
                                sb.append(nameS.get(i));
                            } else if (str.length() > 0) {
                                sb.append(nameS.get(i));
                                break;
                            }
                        }
                    }
                    if (sb.toString().length() > 0) {
                        sf.setKeyword(sb.toString());
                        sf.setSearchType(linkTeam);
                        sf.setTeamID(te.getTeamID());
                        sf.setMemberRole(te.getMemberRole());
                        sf.setTeamName(te.getTeamName());
                        searTeams.add(sf);
                    }
                } else {
//                     List<AllTeamMember>  mAllTeamMember =  getAllTeamMember(te.getTeamID());
//                    if (mAllTeamMember == null || mAllTeamMember.size() <= 0){
//                        continue;
//                    }
                    for (TeamMemberInfo tm : getTeamMember(te.getTeamID())) {
                        String name = null;
                        for (SearchFriends sf : mAppliedFriends) {
                            if (sf.getPhoneNum().equals(tm.getUserPhone())) {
                                name = sf.getNickName();
                                break;
                            }
                        }
                        if (name == null || name.length() <= 0) {
                            name = tm.getNickName();
                        }
                        if (name == null || name.length() <= 0) {
                            name = tm.getUserName();
                        }

//                        String pinYinHeadChar = Cn2Spell.getPinYinHeadChar(name);
//                        String pinYinHead = Cn2Spell.getPinYin(name);
                        String pinYinHeadChar = ChineseToHanYuPYTest.convertChineseToPinyin(name, true);
                        String pinYinHead = ChineseToHanYuPYTest.convertChineseToPinyin(name, false);

                        if (name.contains(s)) {
                            SearchFriends sf = new SearchFriends();
                            sf.setSearchType(linkTeam);
                            sf.setTeamID(te.getTeamID());
                            sf.setMemberRole(te.getMemberRole());
                            sf.setTeamName(te.getTeamName());
                            sf.setUserName(name);
                            sf.setType(strName);
                            sf.setPhoneNum(tm.getUserPhone());
                            searTeams.add(sf);
                            break;
                        } else if (pinYinHeadChar.contains(s.toString()) && !s.toString().matches("^[0-9]*$")) {
                            //简拼搜索
                            SearchFriends sf = new SearchFriends();
                            int index = pinYinHeadChar.indexOf(s.toString());

                            if (index >= 0 && s.toString().length() > 0) {
                                sf.setKeyword(name.substring(index, s.toString().length() + index));
                                sf.setSearchType(linkTeam);
                                sf.setTeamID(te.getTeamID());
                                sf.setMemberRole(te.getMemberRole());
                                sf.setTeamName(te.getTeamName());
                                sf.setUserName(name);
                                sf.setType(strName);
                                sf.setPhoneNum(tm.getUserPhone());
                                searTeams.add(sf);
                            }

                        } else if (pinYinHead.contains(s) && !s.toString().matches("^[0-9]*$") && s.length() >= 2) {
                            //全拼拼搜索
                            SearchFriends sf = new SearchFriends();
                            sb.setLength(0);
                            String str = s.toString();
//                            NameMatchPinYing nameMatchPinYing = Cn2Spell.getNameMatchPinYing(name);
                            NameMatchPinYing nameMatchPinYing = ChineseToHanYuPYTest.getNameMatchPinYing(name);
                            List<String> nameS = nameMatchPinYing.getName();
                            List<String> pinyinlistS = nameMatchPinYing.getPinyinlist();
                            for (int i = 0; i < pinyinlistS.size(); i++) {
                                if (i > 0 && str.length() >= pinyinlistS.get(i - 1).length() && str.contains(pinyinlistS.get(i - 1))) {
                                    str = str.substring(pinyinlistS.get(i - 1).length(), str.length());
                                }
                                if (str == null || str.length() <= 0) {
                                    break;
                                }
                                if (pinyinlistS.get(i).contains(str) || str.contains(pinyinlistS.get(i))) {
                                    if (str.length() >= pinyinlistS.get(i).length()) {
                                        sb.append(nameS.get(i));
                                    } else if (str.length() > 0) {
                                        sb.append(nameS.get(i));
                                        break;
                                    }
                                }
                            }
                            if (sb.toString().length() > 0) {
                                sf.setKeyword(sb.toString());
                                sf.setSearchType(linkTeam);
                                sf.setType(strName);
                                sf.setTeamID(te.getTeamID());
                                sf.setPhoneNum(tm.getUserPhone());
                                sf.setMemberRole(te.getMemberRole());
                                sf.setTeamName(te.getTeamName());
                                sf.setUserName(name);
                                searTeams.add(sf);
                            }
                        } else if (tm.getUserPhone().contains(s)) {
                            SearchFriends sf = new SearchFriends();
                            sf.setSearchType(linkTeam);
                            sf.setTeamID(te.getTeamID());
                            sf.setMemberRole(te.getMemberRole());
                            sf.setTeamName(te.getTeamName());
                            sf.setUserName(name);
                            sf.setPhoneNum(tm.getUserPhone());
                            sf.setType(strPhone);
                            searTeams.add(sf);
                            break;
                        }
                    }
                }
            } else {
                break;
            }
        }
        if (searTeams.size() > 0) {
            SearchFriends a = new SearchFriends();
            a.setUserName(linkTeam);
            a.setSearchType(linkTitle);
            mNewList.add(a);
            for (SearchFriends af : searTeams) {
                String str = "";
                List<TeamMemberInfo> team = getTeamMember(af.getTeamID());
                if (team.size() > 0) {
                    str = "  (" + team.size() + ")";
                }
                af.setTeamName(af.getTeamName() + str);
                mNewList.add(af);
            }
        }
        mSearchListItemAdapter.refresh(mNewList, s.toString());
    }

    public List<SearchFriends> removeDuplicate(List<SearchFriends> list) {
        List<SearchFriends> listTemp = new ArrayList();
        for (SearchFriends sf : list) {
            if (listTemp.size() <= 0) {
                listTemp.add(sf);
            } else {
                for (SearchFriends f : list) {
                    if (!f.getPhoneNum().contains(sf.getPhoneNum())) {
                        listTemp.add(sf);
                    }
                }
            }
        }
        return listTemp;
    }

    public List<TeamMemberInfo> getTeamMember(Long teamId) {
        TeamMemberHelper teamMemberHelper = new TeamMemberHelper(mContext, teamId + "TeamMember.dp", null);
        SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
        Cursor c = db.query("LinkmanMember", null, null, null, null, null, null);
        List<TeamMemberInfo> teamMemberInfo = new ArrayList<TeamMemberInfo>();
        TeamMemberInfo af = null;
        try {
            while (c.moveToNext()) {
                af = new TeamMemberInfo();
                af.setUserName(c.getString(c.getColumnIndex("user_name")));
                af.setUserPhone(c.getString(c.getColumnIndex("user_phone")));
                af.setNickName(c.getString(c.getColumnIndex("nick_name")));
                af.setRole(c.getInt(c.getColumnIndex("role")));
                af.setMemberPriority(c.getInt(c.getColumnIndex("member_priority")));
                teamMemberInfo.add(af);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
        db.close();
        return teamMemberInfo;
    }

    private InputMethodManager imm;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.re_match_phone:
                Intent intent = new Intent(mContext, MatchPhoneNumberActivity.class); // 跳转界面到
                startActivity(intent);
                break;
            case R.id.tv_cancel:
                //TODO 输入框设置
                toChangeView(true);
                mEditText.setText("");
                hideSoftInputFromWindow();
                break;
            case R.id.rl_show_title:

                toChangeView(false);
//                showSoftInputFromWindow();
                mEditText.requestFocus();//设置输入框焦点，etSearch为输入框控件
//                String phoneOrName = mEditText.getText().toString().trim();
//                searchFriend(phoneOrName);
                break;
            case R.id.re_search_friends:
                startActivity(new Intent(SearchActivity.this, SearchFriendsByConditionActivity.class));
                break;
            case R.id.re_nearby_car:
                startActivity(new Intent(SearchActivity.this, AmapSearchActivity.class));
                break;
            default:
                break;
        }
    }

    private void showSoftInputFromWindow() {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();
        if (isOpen) {
            imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);//显示软键盘
        }
    }

    //隐藏输入法
    private void hideSoftInputFromWindow() {
        if (imm != null) {
            boolean isOpen = imm.isActive();
            if (isOpen) {
                imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);//隐藏软键盘
            }
        }
    }

    private void toChangeView(boolean isShow) {

        if (isShow) {
            mTvCancel.setVisibility(View.GONE);
            mFrameLayoutInput.setVisibility(View.GONE);
            mRelativeLayoutSearch.setVisibility(View.GONE);
            mRelativeLayoutShow.setVisibility(View.VISIBLE);
            mRelativeLayoutSearchFriends.setVisibility(View.VISIBLE);
            mRelativeLayoutNearbyCar.setVisibility(View.VISIBLE);
            mRelativeLayoutMatchPhone.setVisibility(View.GONE);
            mRelativeLayoutTitle.setVisibility(View.VISIBLE);
        } else {
            mTvCancel.setVisibility(View.VISIBLE);
            mFrameLayoutInput.setVisibility(View.VISIBLE);
            mRelativeLayoutSearch.setVisibility(View.VISIBLE);
            mRelativeLayoutShow.setVisibility(View.GONE);
            mRelativeLayoutSearchFriends.setVisibility(View.GONE);
            mRelativeLayoutNearbyCar.setVisibility(View.GONE);
            mRelativeLayoutMatchPhone.setVisibility(View.GONE);
            mRelativeLayoutTitle.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (param == null) {
            param = new ViewAllCarParam();
        }
        switch (requestCode) {
            case 2:
                if (resultCode == RESULT_OK) {
                    if (data.getIntExtra("data", 0) == 1) {
                        finish();
                    }
                    if (data.getIntExtra("data", 0) == 0) {
                    }
                }
                break;
            case 3:
                if (resultCode == RESULT_OK) {
                    if (data.getIntExtra("data", 0) == 1) {
                        finish();
                    }
                    if (data.getIntExtra("data", 0) == 0) {
                    }
                }
                break;

            default:
                break;
        }

    }


    /**
     * 搜索好友
     *昵称/号码/车牌号
     * @param phoneNum
     */
    private void searchFriend(String phoneNum) {
        checkDialog = true;
//		final ProgressDialog dialog = new ProgressDialog(this);
//		dialog.setMessage("正在查找联系人...");
//		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//		dialog.show();
        m_pDialog.show();
        ProtoMessage.SearchUser.Builder builder = ProtoMessage.SearchUser.newBuilder();
        builder.setPhoneNum(phoneNum);
        builder.setOnlyPhoneNum(false);
        MyService.start(mContext, ProtoMessage.Cmd.cmdSearchUser2.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(SearchStrangerProcesser.ACTION);
        new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                checkDialog = false;
                m_pDialog.dismiss();
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                checkDialog = false;
                m_pDialog.dismiss();
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    SearchStrangers searchFriends = (SearchStrangers) i.getSerializableExtra("user_info");
                    if (searchFriends.getPhoneNum() == null || searchFriends.getPhoneNum().length() <= 0) {
                        ToastR.setToast(mContext, "未找到相关用户");
                    } else {
                        successFreindBack(searchFriends);
                    }
                } else {
                    new ResponseErrorProcesser(mContext, i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    public void successFreindBack(SearchStrangers aply) {
        Intent i = new Intent(mContext, ShowFriendMsgActivity.class);
        i.putExtra("user_msg", aply);
        i.putExtra("activity", "new");
        startActivityForResult(i, 2);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        // TODO Auto-generated method stub
//        switch (requestCode) {
//            case 2:
//                if (resultCode == RESULT_OK) {
//                    if (data.getIntExtra("data", 0) == 1) {
//                        finish();
//                    }
//                    if (data.getIntExtra("data", 0) == 0) {
//                    }
//                }
//                break;
//            case 3:
//                if (resultCode == RESULT_OK) {
//                    if (data.getIntExtra("data", 0) == 1) {
//                        finish();
//                    }
//                    if (data.getIntExtra("data", 0) == 0) {
//                    }
//                }
//                break;
//            default:
//        }
//    }

    private void searchGroup() {
        String msg = mEditText.getText().toString().trim();
        if (msg.length() <= 0) {
            ToastR.setToast(mContext, "请输入数据");
            return;
        }
        checkDialog = true;
        m_pDialog.show();
        ProtoMessage.TeamInfo.Builder builder = ProtoMessage.TeamInfo.newBuilder();
        builder.setTeamName(msg);
        MyService.start(mContext, ProtoMessage.Cmd.cmdSearchTeam.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(SearchGroupProcesser.ACTION);
        new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                checkDialog = false;
                m_pDialog.dismiss();
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                checkDialog = false;
                m_pDialog.dismiss();
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    TeamInfoList teList = i.getParcelableExtra("get_seach_group_list");
                    if (teList != null) {
                        searchTeamSuccess(teList);
                        ToastR.setToast(mContext, "搜索群组成功");
                    } else {
                        ToastR.setToast(mContext, "未找到相关群组");
                    }
                } else {
                    new ResponseErrorProcesser(mContext, i.getIntExtra("error_code", -1));
                }
            }
        });

    }

    private void searchTeamSuccess(TeamInfoList teList) {
        Intent i = new Intent(mContext, ShowSearchGroupActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("intent_seach_group_list", teList);
        i.putExtras(bundle);
        startActivityForResult(i, 3);
    }

}
