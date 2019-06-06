package com.example.jrd48.chat.friend;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jrd48.chat.BaseActivity;
import com.luobin.dvr.R;
import com.example.jrd48.chat.SimpleUser;
import com.example.jrd48.chat.SimpleUserInfo;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.chat.permission.PermissionUtil;
import com.example.jrd48.chat.search.PinyinComparator;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.ApplyFriendProcesser;
import com.example.jrd48.service.protocol.root.MatchContactsProcesser;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MatchPhoneNumberActivity extends BaseActivity implements PermissionUtil.PermissionCallBack {

    /**
     * 获取库Phon表字段
     **/
    private static final String[] PHONES_PROJECTION = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Photo.PHOTO_ID,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID};
    /**
     * 联系人显示名称
     **/
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;
    /**
     * 电话号码
     **/
    private static final int PHONES_NUMBER_INDEX = 1;
    /**
     * 头像ID
     **/
    private static final int PHONES_PHOTO_ID_INDEX = 2;
    /**
     * 联系人的ID
     **/
    private static final int PHONES_CONTACT_ID_INDEX = 3;
    ListView listView;
    int MY_PERMISSIONS_REQUEST_CONTACT = 12123;
    int MY_PERMISSIONS_REQUEST_AUDIO = 13;
    Context context;
    List<AppliedFriends> mFriends = new ArrayList<AppliedFriends>();
    List<AppliedFriends> mGotContact = new ArrayList<AppliedFriends>();
    List<AppliedFriends> mLocalFriends = new ArrayList<AppliedFriends>();
    String userNameMe;   //自己用户名
    String myPhone;
    List<AppliedFriends> mSameFriends = new ArrayList<AppliedFriends>();
    List<AppliedFriends> mNoFriendsContacts = new ArrayList<AppliedFriends>();
    private TextView tvPrompt;
    private TextView tvNoPhone;
    private PullToRefreshListView mPullRefreshListView;
    private ProgressDialog m_pDialog;
    boolean checkDialog = true;
    boolean checkType = true;
    boolean showDialog = false;
    protected PermissionUtil mPermissionUtil;
    private MatchPhoneNumberAdapter adapter;
    private Handler mHandler = new Handler();

    public static boolean checkPhone(String phoneNumber) {
        String telRegex = "[1][34578]\\d{9}";
        if (TextUtils.isEmpty(phoneNumber))
            return false;
        else
            return phoneNumber.matches(telRegex);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match_phone_number);
        context = this;
        getDBFridsMsg();
        getMsg();
        initView();
    }

    private void getDBFridsMsg() {
        try {
            DBManagerFriendsList db = new DBManagerFriendsList(context, DBTableName.getTableName(context, DBHelperFriendsList.NAME));
            mLocalFriends = db.getFriends(false);
            db.closeDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        tvPrompt = (TextView) this.findViewById(R.id.tv_prompt);
        tvPrompt.setVisibility(View.GONE);
        tvNoPhone = (TextView) this.findViewById(R.id.tv_no_phone);
        tvNoPhone.setVisibility(View.GONE);
        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_match_phone_refresh_list);
        listView = mPullRefreshListView.getRefreshableView();
        adapter = new MatchPhoneNumberAdapter(this, mFriends);
        listView.setAdapter(adapter);


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
                    if (checkType == false) {
                        ToastR.setToast(MatchPhoneNumberActivity.this, "取消匹配通讯录");
                    } else {
                        if (cancelToast) {
                            ToastR.setToast(MatchPhoneNumberActivity.this, "取消添加好友");
                        }
                    }
                }
            }
        });
        //********************************************弹窗设置****************************************************

        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                tvPrompt.setVisibility(View.GONE);
                checkPhoneContactsPermission(false);

            }
        });
        registerForContextMenu(listView);
        checkPhoneContactsPermission(true);

//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                AppliedFriends vf = mFriends.get(i);
////                vf.setbChecked(!vf.isbChecked());
//                adapter.notifyDataSetChanged();
//            }
//        });
    }

    private void checkPhoneContactsPermission(boolean show) {
        showDialog = show;
        mPermissionUtil = PermissionUtil.getInstance();
        if (mPermissionUtil.isOverMarshmallow()) {
            mPermissionUtil.requestPermission(this, MY_PERMISSIONS_REQUEST_CONTACT, this, PermissionUtil.PERMISSIONS_READ_CONTACTS, PermissionUtil.PERMISSIONS_READ_CONTACTS);
        } else {
            if (showDialog) {
                m_pDialog.show();
            }
            toMatchPhoneNumber();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mPermissionUtil.requestResult(this, permissions, grantResults, this, PermissionUtil.PERMISSIONS_READ_CONTACTS);
    }

    @Override
    public void onPermissionSuccess(String type) {
        if (showDialog) {
            m_pDialog.show();
        }
        toMatchPhoneNumber();
    }

    @Override
    public void onPermissionReject(String strMessage) {
        ToastR.setToastLong(context, "应用读取通讯录权限已经拒绝，请到手机管家或者系统设置里允许权限");
    }

    @Override
    public void onPermissionFail(String failType) {
        ToastR.setToastLong(context, "应用没有读取通讯录权限，请授权！");
    }

    /**
     * 得到手机通讯录联系人信息
     */
    private boolean getPhoneContacts() {
        try {
            ContentResolver resolver = getContentResolver();
//        mSetContact = null;
            mGotContact.clear();
            // 获取手机联系人
            Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PHONES_PROJECTION, null, null, null);

            if (phoneCursor != null) {
                while (phoneCursor.moveToNext()) {

                    // 得到手机号码
                    String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX).toString().trim();

                    if (phoneNumber.startsWith("+86")) {
                        phoneNumber = phoneNumber.substring(3, phoneNumber.length());
                    }
                    // 当手机号码为空的或者为空字段 跳过当前循环
                    if (TextUtils.isEmpty(phoneNumber) || !checkPhone(phoneNumber)) {
                        continue;
                    }
                    // 得到联系人名称
                    String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
                    AppliedFriends con = new AppliedFriends();
                    con.setUserName(contactName);
                    con.setPhoneNum(phoneNumber);
                    mGotContact.add(con);
                }
                phoneCursor.close();

                if (mGotContact.size() <= 0) {
                    return false;
                } else {
                    return true;
                }

//            mSetContact = new HashSet<>(mContacts);
            } else {
                // backDilog(AddFriendsActivity.this);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void getNoFriendsContacts() {
        mSameFriends.clear();
        mNoFriendsContacts.clear();
        for (AppliedFriends af : mGotContact) {
            for (AppliedFriends local : mLocalFriends) {
                if (local.getPhoneNum().equals(af.getPhoneNum()) || af.getPhoneNum().equals(myPhone)) {
                    mSameFriends.add(af);
                }
            }
        }
//        for (AppliedFriends app:mSameFriends) {
//            if (app.getPhoneNum().equals(myPhone)) {
//
//            }
//        }
        for (AppliedFriends info : mGotContact) {
            int t = 0;
            if (mSameFriends.size() <= 0) {
                mNoFriendsContacts.add(info);
                continue;
            }
            for (int i = 0; i < mSameFriends.size(); i++) {
                if (!mSameFriends.get(i).getPhoneNum().equals(info.getPhoneNum())) {
                    ++t;
                    if (t == mSameFriends.size()) {
                        mNoFriendsContacts.add(info);
                    }
                } else {
                    break;
                }
            }
        }
    }

    private void getMsg() {
        SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        myPhone = preferences.getString("phone", "");
        userNameMe = preferences.getString("name", myPhone);
    }

    public void back(View view) {
        finish();
    }

    /**
     * 重写返回键功能
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

//    @Override
//    public void onClick(View v) {
//
//        switch (v.getId()) {
//            case R.id.re_search:
//                break;
//        }
//
//    }

    /**
     * 搜索好友
     *
     * @param editMsg
     */
    private void searchFriend(String editMsg) {

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("正在查找联系人...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

//		try {
//			MyAsynHttpClient client = new MyAsynHttpClient(getApplicationContext());
//			ApplyFriendRequestParam param = new ApplyFriendRequestParam();
//			param.setToken(GlobalData.getInstance().getToken());
//			param.setNameOrPhone(editMsg);
//			param.setMessage("搜索");
//			Gson gson = MyGson.createGson();
//			JSONObject json = new JSONObject(gson.toJson(param));
//
//			String url = GlobalData.getInstance().getUrlFoundUser();
//
//			client.postJson2(url, json, new MyAsynPostListener(this) {
//
//				@Override
//				public void onGotSuccess(JSONObject response) {
//					try {
//						Gson gson = MyGson.createGson();
//						FoundUsersResultStatus result = gson.fromJson(response.toString(),
//								FoundUsersResultStatus.class);
//						SimpleUserInfo user = result.getUsers().get(0);
//						searchSuccess(user);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//
//				@Override
//				public void onGotFailed(JSONObject response) {
//					super.onGotFailed(response);
//					try {
//						int statusCode = response.getInt("status");
//						switch (statusCode) {
//						case ErrorCode.FAILED:
//							Toast.makeText(AddFriendsActivity.this, "提示：查找好友失败！", Toast.LENGTH_LONG).show();
//							break;
//						default:
//							break;
//						}
//
//					} catch (JSONException e) {
//						e.printStackTrace();
//					}
//
//				}
//
//				@Override
//				public void onFinish() {
//					if (dialog != null)
//						dialog.cancel();
//					super.onFinish();
//				}
//
//			});
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
    }


    /**
     * 搜索成功后传递信息
     *
     * @param user
     */
    private void searchSuccess(SimpleUserInfo user) {

        SimpleUser ll = new SimpleUser();
        ll.setUserName(user.getUserName());
        ll.setUserID(user.getUserID());
        ll.setUserFace(user.getUserFace());
        ll.setSexCode(user.getSexCode());
        ll.setUserCity(user.getUserCity());
        ll.setUserProv(user.getUserProv());
        ll.setFromActivity("AddFriendsActivity");

//        Intent intent = new Intent(AddFriendsActivity.this, ShowFriendMsgActivity.class); // 跳转界面到

//        Bundle bundle = new Bundle();
//        bundle.putSerializable("searchFriends", ll);
//        intent.putExtras(bundle);
//
//        startActivity(intent);
        finish();
    }


    public void fail(int code) {
        new ResponseErrorProcesser(context, code);
    }

    /**
     * 清除缓存数据
     *
     * @param friend
     */
    protected void clearListorCache(AppliedFriends friend) {
        List<AppliedFriends> data = adapter.getList();
        // 遍历删除内存数据
        Iterator<AppliedFriends> iterator = data.iterator();
        while (iterator.hasNext()) {
            AppliedFriends k = iterator.next();
            if (k.getPhoneNum().equals(friend.getPhoneNum())) {
                iterator.remove();
                break;
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
    private void refreshListData(List<AppliedFriends> data) {
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

    /**
     * 添加好友提示框
     */
    private void dialogFriendsRequest(final AppliedFriends friendsMsg) {
        try {
            String msg = "，请求加您为好友，谢谢。";
            AddFriendPrompt.dialogFriendsRequest(this, msg, friendsMsg.getNickName(), userNameMe, new AddFriendPromptListener() {

                @Override
                public void onOk(String remark, String msg) {
                    addFriendsRequest(remark, msg, friendsMsg);

                }

                @Override
                public void onFail(String typ) {
                    if (typ.equals(AddFriendPrompt.TYP)) {
                        ToastR.setToast(context, "信息输入不能为空");
                    }else {
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
    private void addFriendsRequest(String remark, String msg, final AppliedFriends friendsMsg) {
        checkType = true;
        checkDialog = true;
        m_pDialog.show();
        ProtoMessage.ApplyFriend.Builder builder = ProtoMessage.ApplyFriend.newBuilder();
        builder.setFriendPhoneNum(friendsMsg.getPhoneNum());
        builder.setApplyInfo(msg);
        builder.setApplyRemark(remark);
        MyService.start(context, ProtoMessage.Cmd.cmdApplyFriend.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(ApplyFriendProcesser.ACTION);
        new TimeoutBroadcast(context, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                checkDialog = false;
                m_pDialog.dismiss();
                ToastR.setToast(context, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                checkDialog = false;
                m_pDialog.dismiss();
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    clearListorCache(friendsMsg);
                    ToastR.setToast(context, "请求成功，等待对方回应");
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    boolean cancelToast = true;
    public void toMatchPhoneNumber() {
        if (getPhoneContacts()) {
            getNoFriendsContacts();
            toCheckContacts(mNoFriendsContacts);
        } else {
            ToastR.setToastLong(this, "未能读取到手机联系人，可能需要在系统设置中打开读取通讯录权限。");
            cancelToast = false;
            if (showDialog) {
                m_pDialog.cancel();
            }
            finish();
        }
    }

    //mNoFriendsContacts
    private void toCheckContacts(List<AppliedFriends> mNoFriendsContacts) {
        checkDialog = true;
        checkType = false;
        ProtoMessage.FriendList.Builder builder = ProtoMessage.FriendList.newBuilder();
        for (AppliedFriends af : mNoFriendsContacts) {
            //ProtoMessage.UserInfo uf = new ProtoMessage.UserInfo();
            ProtoMessage.UserInfo.Builder b = ProtoMessage.UserInfo.newBuilder();
            b.setPhoneNum(af.getPhoneNum());
            b.setNickName(af.getUserName());
            builder.addFriends(b);
        }


        MyService.start(context, ProtoMessage.Cmd.cmdSearchPhoneContact.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(MatchContactsProcesser.ACTION);
        new TimeoutBroadcast(context, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                if (mPullRefreshListView != null) {
                    mPullRefreshListView.onRefreshComplete();
                }
                checkDialog = false;
                if (showDialog) {
                    m_pDialog.cancel();
                }
                ToastR.setToast(context, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (mPullRefreshListView != null) {
                    mPullRefreshListView.onRefreshComplete();
                }
                checkDialog = false;
                if (showDialog) {
                    m_pDialog.cancel();
                }
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    AppliedFriendsList list = i.getParcelableExtra("get_match_list");
                    if (list.getAppliedFriends() != null && list.getAppliedFriends().size() > 0) {
                        tvNoPhone.setVisibility(View.GONE);
                        convertViewTeamMember(list.getAppliedFriends());
                    } else {
                        tvNoPhone.setVisibility(View.VISIBLE);
//                        ToastR.setToast(context, "没有相匹配的号码");
                    }
                    //	ToastR.setToast(FirstActivity.this, "获取群成员成功");
                } else if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.TEAM_NOT_EXIST_VALUE ||
                        i.getIntExtra("error_code", -1) ==
                                ProtoMessage.ErrorCode.NOT_TEAM_MEMBER_VALUE) {
                    ToastR.setToast(context, "匹配成功");
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void convertViewTeamMember(List<AppliedFriends> appliedFriends) {
        mFriends.clear();
        m_pDialog.cancel();
        if (appliedFriends.size() > 0) {
            tvPrompt.setVisibility(View.GONE);
        } else {
            tvPrompt.setVisibility(View.VISIBLE);
        }
        for (AppliedFriends af : appliedFriends) {
            AppliedFriends vf = new AppliedFriends();
            vf.setUserName(af.getUserName());
            vf.setNickName(af.getNickName());
            vf.setPhoneNum(af.getPhoneNum());
            mFriends.add(vf);
        }
        // 排序(实现了中英文混排)
        PinyinComparator comparator = new PinyinComparator();
        Collections.sort(mFriends, comparator);
        refreshListData(mFriends);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPullRefreshListView != null) {
            mPullRefreshListView.onRefreshComplete();
        }
    }

    /**
     * 自定义DbAdapter
     */

    public class MatchPhoneNumberAdapter extends BaseAdapter {

        int friendsIndex = 0;
        private List<AppliedFriends> mArrayList;
        private Context context;
        private LayoutInflater layoutInflater;

        public MatchPhoneNumberAdapter(Context context, List<AppliedFriends> mFriend) {
            layoutInflater = LayoutInflater.from(context);
            this.context = context;
            this.mArrayList = mFriend;
        }

        public List<AppliedFriends> getList() {
            return mArrayList;
        }

        // 刷新适配器
        public void refresh(List<AppliedFriends> mArryFriend) {
            this.mArrayList = mArryFriend;
            notifyDataSetChanged();
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
        public AppliedFriends getItem(int position) {
            AppliedFriends item = null;
            if (null != mArrayList) {
                item = mArrayList.get(position);
            }
            return item;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final AppliedFriends friendsMsg = mArrayList.get(position);
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.match_number_item, null);
                holder.name = (TextView) convertView.findViewById(R.id.match_listitem_name);
                holder.memberNumber = (TextView) convertView.findViewById(R.id.match_listitem_count);
                holder.images = (ImageView) convertView.findViewById(R.id.match_circle_image);
                holder.selectedBtn = (Button) convertView.findViewById(R.id.match_listitem_selected);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (friendsMsg == null) {
                return null;
            }
            holder.memberNumber.setText(friendsMsg.getPhoneNum());
            String name = friendsMsg.getNickName();
//            if (TextUtils.isEmpty(name) || name.equals("null")) {
//                name = friendsMsg.getUserName();
//            } else if (TextUtils.isEmpty(friendsMsg.getUserName()) || friendsMsg.getUserName().equals("null")) {
//                name = friendsMsg.getPhoneNum();
//            }
            holder.name.setText(name + " (" + friendsMsg.getUserName() + ")");

            holder.selectedBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogFriendsRequest(friendsMsg);
                }
            });

            Bitmap bitmap = FriendFaceUtill.getUserFace(context, friendsMsg.getPhoneNum());
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
            return convertView;
        }

        public class ViewHolder {
            // public TextView tvNumber;
            private TextView name;
            private TextView memberNumber;
            private TextView groupNumber;
            private Button selectedBtn;
            private ImageView images;
        }


    }

}
