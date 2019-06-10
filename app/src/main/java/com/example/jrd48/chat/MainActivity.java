package com.example.jrd48.chat;

import android.app.Activity;
import android.app.Dialog;
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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.NoScrollViewPager;
import com.example.jrd48.PolyphonePinYin;
import com.example.jrd48.chat.SQLite.MsgRecordHelper;
import com.example.jrd48.chat.VersionUpdate.GetActivityNullException;
import com.example.jrd48.chat.VersionUpdate.GetAppVersion;
import com.example.jrd48.chat.VersionUpdate.MyAppUpgradeListener;
import com.example.jrd48.chat.VersionUpdate.MyAsynHttpClient;
import com.example.jrd48.chat.VersionUpdate.MyDownloader;
import com.example.jrd48.chat.VersionUpdate.MyGetActivity;
import com.luobin.notice.NotificationActivity;
import com.example.jrd48.chat.group.CreateGroupActivity;
import com.example.jrd48.chat.location.ServiceCheckUserEvent;
import com.example.jrd48.chat.permission.PermissionUtil;
import com.luobin.dvr.R;
import com.example.jrd48.chat.receiver.InitDataBroadcast;
import com.example.jrd48.chat.receiver.NotifyFriendBroadcast;
import com.example.jrd48.chat.search.NotificationUtil;
import com.example.jrd48.chat.search.SearchActivity;
import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyBroadcastReceiver;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.ReceiverProcesser;
import com.example.jrd48.service.protocol.root.UserInfoProcesser;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.luobin.myinfor.MyInforActivity;
import com.luobin.ui.LoginActivity;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, PermissionUtil.PermissionCallBack {
    public static final boolean UPDATE_CHECK = false;
    public static final int MENU_ZERO = 0;
    public static final int MENU_ONE = 1;
    public static final int MENU_TWO = 2;
    public static final String type = "ragment";
    public static final String FRIEND_ACTION = "com.example.jrd48.chat.friend.notify";
    public static final String TEAM_ACTION = "com.example.jrd48.chat.team.notify";
    public static final String DOWNLOAD_APK = "com.example.jrd48.chat.download.new.version";
    public static final String MSG_NOTIFY = "com.example.jrd48.chat.msg.notifyReceiver";
    public static final int RESULT_HAS_CODE = 1;
    private static final int DOWN_GET_SIZE = 1;
    private static final int DOWN_BEGIN = 2;
    private static final int DOWN_PROGRESS = 3;
    private static final int DOWN_COMPLETE = 4;
    private static final int DOWN_FAILED = 5;
    private static final int ADDGROUP_CODE = 2;
    private static final int ADDFriend_CODE = 3;
    public static String deleteStr = "delete";
    public static String friendType = "friend";
    public static String msgType = "msg";
    public static String teamType = "team";
    static List<Integer> list = new ArrayList<Integer>();
    static int notifice = 0;
    static int team = 0;
    MyDownloader downloader;
    ProgressDialog dlgProgress;
    int nFileSize = 0;
    InitDataBroadcast mInitDataBroadcast;
    NotifyFriendBroadcast mNotifyFriendBroadcast;
    //    LocationBroadcast mLocationBroadcast;
    int MY_PERMISSIONS_REQUEST_WRITE = 10033;
    public static final String TYPE = "type";
    public static final String click = "notification_click_type";
    String URL;
    private TabLayout mTabLayout;
    private NoScrollViewPager mViewPager;
    private String[] sexItems = new String[]{"", "男", "女", "未设置"};
    private List<String> mTitleList = new ArrayList<>();//页卡标题集合
    private ImageView headimage;
    private TextView name;
    private List<Fragment> mFragments = new ArrayList<Fragment>();
    private BadgeView badgeView;
    private MainPagerAdapter mAdapter;
    private Context mContext;
    protected PermissionUtil mPermissionUtil;
    //通知
    NotificationUtil notificationUtil;
    boolean showNotification = true;
    boolean isComplete = false;
    boolean isConnected = true;
    double percent;

    public static Uri mUri;
    public static Intent mIntent;
    public static String mAction;
    public static String mText;

    class NotificationBroadcastReceiver extends BroadcastReceiver {

        public static final String TYPE = "type"; //这个type是为了Notification更新信息的，这个不明白的朋友可以去搜搜，很多

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int type = intent.getIntExtra(TYPE, -1);

            if (type != -1) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(NotificationUtil.NOTIFYCATIONID);
//            notificationManager.cancelAll();
            }


            if (action.equals("notification_clicked")) {
                //处理点击事件
                ToastR.setToast(context, "点击事件");
            }

            if (action.equals("notification_cancelled")) {
                //处理滑动清除和点击删除事件
                ToastR.setToast(context, "滑动取消");
            }
        }
    }

//    private RefreshFriendsBroadCast mRefreshFriendsBroadCast;
    /**
     * 获取下载信息
     */

    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_GET_SIZE:
                    isComplete = false;
                    int x = (int) msg.obj;
                    nFileSize = x;
                    // dlgProgress.setMessage(String.format("已下载: 0/%d", x));
//                    dlgProgress.setProgress(0);
                    if (showNotification) {
                        notificationUtil.showProgressNotify(0, NotificationUtil.SHOW_NOTIFICATION);
                    }
                    break;
                case DOWN_PROGRESS:
                    isComplete = false;
                    int temp = (int) msg.obj;
                    percent = temp * 100.0 / nFileSize;
//                    dlgProgress.setProgress((int) percent);
                    // dlgProgress.setMessage(String.format("已下载: 0/%d", temp));
                    if (showNotification) {
                        notificationUtil.showProgressNotify((int) percent, "");
                    }
                    break;
                case DOWN_COMPLETE:
                    isComplete = true;
//                    dlgProgress.setProgress(100);
                    if (showNotification) {
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(NotificationUtil.NOTIFYCATIONID);
                        notificationUtil.showProgressNotify(100, NotificationUtil.COMPLETE);
                    }
                    ToastR.setToast(mContext, "应用下载完成");
//                    dlgProgress.dismiss();
                    doInstall();
                    break;
                case DOWN_FAILED:
                    if (showNotification) {
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(NotificationUtil.NOTIFYCATIONID);
                        notificationUtil.showProgressNotify((int) percent, NotificationUtil.FAIL);
                    }
                    doDownFailed();
                    break;
            }
        }

        ;
    };
    //***********************************获取个人信息*************************************
    private boolean mIsChecking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_main);
        handleImage();
        notificationUtil = new NotificationUtil(this);
//        String str = "长沙市长重庆";
//        String pinyin = Pinyin4jutil.converterToSpell(str);
//        Log.i("pinyin",str+" : "+pinyin);
//        String pinyin1 = Pinyin4jutil.converterToFirstSpell(str);
//        Log.i("pinyin",str+" : "+pinyin1);

        mContext = this;
        addPopWindow = new AddPopWindow(MainActivity.this, itemsOnClick);
        getBroadcastMsg();
        initTabLayout();//TabLayout初始化
        initToolbar();//Toolbar和NavigationView初始化
        initDownload();
        checkVersion(false);
        Infor();
        ServiceCheckUserEvent.restart(mContext);
//        checkLocation();
//        checkStoragePermisson();
        //初始化多音字拼音
        PolyphonePinYin.initPinyin();
        new ChineseToHanYuPYTest();
    }

    private void checkStoragePermisson() {
        mPermissionUtil = PermissionUtil.getInstance();
        if (mPermissionUtil.isOverMarshmallow()) {
            mPermissionUtil.requestPermission(this, MY_PERMISSIONS_REQUEST_WRITE, this, PermissionUtil.PERMISSIONS_STORAGE, PermissionUtil.PERMISSIONS_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mPermissionUtil.requestResult(this, permissions, grantResults, this, PermissionUtil.PERMISSIONS_STORAGE);
    }

    @Override
    public void onPermissionSuccess(String type) {
    }

    @Override
    public void onPermissionReject(String strMessage) {
//        ToastR.setToastLong(mContext, "应用存储器读写已经拒绝，请到手机管家或者系统设置里授权");
    }

    @Override
    public void onPermissionFail(String failType) {
        ToastR.setToast(mContext, "应用没有外部存储器读写权限，请授权！");
    }


    private void getBroadcastMsg() {
        //更新数据广播
        mInitDataBroadcast = new InitDataBroadcast(mContext);
        mInitDataBroadcast.setReceiver(new MyBroadcastReceiver() {
            @Override
            protected void onReceiveParam(String str) {
                upDataMsg();
            }
        });
        mInitDataBroadcast.start();


//        mLocationBroadcast = new  LocationBroadcast(mContext);
//        mLocationBroadcast.setReceiver(new MyBroadcastReceiver() {
//            @Override
//            protected void onReceiveParam(String str) {
//                if (str.equals("1")){
//                    startSendLocation();
//                } else {
//                    stopSendLocation();
//                }
//            }
//        });
//        mLocationBroadcast.start();

        //注册获取申请加好友广播
        mNotifyFriendBroadcast = new NotifyFriendBroadcast(mContext);
        mNotifyFriendBroadcast.setReceiver(new MyBroadcastReceiver() {
            @Override
            protected void onReceiveParam(String str) {
                refreshData(msgType, MainActivity.MENU_ONE);
                supportInvalidateOptionsMenu();
            }
        });
        mNotifyFriendBroadcast.start();

//        IntentFilter checkConnet = new IntentFilter();
//        checkConnet.addAction(CHECK_CONNET);
//        checkConnet.setPriority(Integer.MAX_VALUE);
//        registerReceiver(checkConnetReceiver, checkConnet);

        IntentFilter filter = new IntentFilter();
        filter.addAction(DOWNLOAD_APK);
        filter.setPriority(Integer.MAX_VALUE);
        registerReceiver(downloadReceiver, filter);

        // hanjiming 2017/4/20 注册点击信息通知广播
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(MSG_NOTIFY);
        filter1.setPriority(Integer.MAX_VALUE);
        registerReceiver(notifyReceiver, filter1);


    }

    private BroadcastReceiver checkConnetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isConnected = intent.getBooleanExtra("connect", true);
//            Log.e("clickType", " ---  " + isConnected);
        }
    };
    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra(TYPE, -1);
            String clickType = intent.getStringExtra(click);
            if (type == NotificationUtil.NOTIFYCATIONID) {
                if (clickType.equals(NotificationUtil.CANCEL)) {
                    downloader.setbStop(true);
                    hideNotification();
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(NotificationUtil.NOTIFYCATIONID);
                } else if (clickType.equals(NotificationUtil.LOAD)) {
                    downloadNewVersion(URL);
                } else if (clickType.equals(NotificationUtil.INSTALL)) {
                    doInstall();
                }

                if (!isComplete) {
//                    initDialogProgress();
                }
            }
        }
    };

    // hanjiming 2017/4/20 接受点击信息通知广播
    private BroadcastReceiver notifyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MSG_NOTIFY)) {
                MsgRecordHelper msgRecordHelper = new MsgRecordHelper(context, intent.getStringExtra("myphone") + "MsgShow.dp", null);
                SQLiteDatabase db = msgRecordHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("new_msg", 0);

                long groupId = intent.getLongExtra("group", 0);
                Intent notificationIntent = new Intent(context, FirstActivity.class);
                notificationIntent.putExtra("data", 0);
                if (groupId == 0) {
                    String linkmanPhone = intent.getStringExtra("linkmanPhone");
                    notificationIntent.putExtra("linkmanName", intent.getStringExtra("linkmanName"));
                    notificationIntent.putExtra("linkmanPhone", linkmanPhone);

                    db.update("Msg", values, "phone = ? and msg_from = ?", new String[]{linkmanPhone, 0 + ""});
                } else {
                    db.update("Msg", values, "group_id = ?", new String[]{groupId + ""});

                    notificationIntent.putExtra("group", groupId);
                    notificationIntent.putExtra("group_name", intent.getStringExtra("group_name"));
                    notificationIntent.putExtra("type", intent.getStringExtra("type"));
                }
                db.close();
                upDataMsg();
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(notificationIntent);
            }
        }
    };

    private void initDialogProgress() {
        try {
            final Activity act = MyGetActivity.getParent(this);

            dlgProgress = new ProgressDialog(mContext);
            dlgProgress.setTitle(act.getString(R.string.download_progress_title));
            dlgProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dlgProgress.setCancelable(true);
            dlgProgress.setIndeterminate(false);
            dlgProgress.setCanceledOnTouchOutside(false);
            dlgProgress.setButton(DialogInterface.BUTTON_POSITIVE,
                    act.getString(R.string.download_progress_btn_background), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dlgProgress.dismiss();
                        }
                    });

            dlgProgress.setButton(DialogInterface.BUTTON_NEGATIVE, act.getString(R.string.download_progress_btn_cancel),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            downloader.setbStop(true);
                            dlgProgress.dismiss();
                        }
                    });

            dlgProgress.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideNotification() {
        showNotification = false;
    }

    //***********************************获取个人信息*************************************

    //TabLayout初始化
    private void initTabLayout() {
        mViewPager = (NoScrollViewPager) findViewById(R.id.vp_view);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);

        //添加页卡标题
        mTitleList.add("消息");
        mTitleList.add("联系人");
        mTitleList.add("群");

        mFragments.add(new TabFragmentMsg());
        mFragments.add(new TabFragmentLinkmans());
        mFragments.add(new TabFragmentLinkGroup());


        mTabLayout.setTabMode(TabLayout.MODE_FIXED);//设置tab模式，当前为系统默认模式


        mAdapter = new MainPagerAdapter(getSupportFragmentManager(), mFragments, mTitleList);
        mViewPager.setAdapter(mAdapter);//给ViewPager设置适配器
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        setUpTabBadge();
    }

    //***********************************读取个人信息*************************************
    private void Infor() {
        SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        String str = preferences.getString("name", "");
        String myPhone = preferences.getString("phone", "");

        String address = ReceiverProcesser.getMyDataRoot(this) + "/" + myPhone + "/";
        String img_path = address + "head2.jpg";
        if (str == null || str.equals("")) {
            getInfor();
        } else {
            Bitmap bmp = BitmapFactory.decodeFile(img_path);
            if (bmp == null) {
                headimage.setImageResource(R.drawable.default_useravatar);
            } else {
                headimage.setImageBitmap(bmp);
            }

            if (myPhone.equals(str)) {
                if ((Boolean) SharedPreferencesUtils.get(this, "show_name_tip", true)) {
                    Intent intent = new Intent(MainActivity.this, MyInforActivity.class);
                    startActivity(intent);
                    ToastR.setToast(this, "请点击姓名，设置您的名字");
                    SharedPreferencesUtils.put(this, "show_name_tip", false);
                }
            }

            name.setText(str);
        }
    }

    //***********************************获取个人信息*************************************
    private void getInfor() {
        ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
        MyService.start(MainActivity.this, ProtoMessage.Cmd.cmdGetMyInfo.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(UserInfoProcesser.ACTION);
        new TimeoutBroadcast(MainActivity.this, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(MainActivity.this, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK_VALUE) {
                    String myPhone;
                    SharedPreferences preferences1 = getSharedPreferences("token", Context.MODE_PRIVATE);
                    myPhone = preferences1.getString("phone", "");
                    String address = ReceiverProcesser.getMyDataRoot(MainActivity.this) + "/" + myPhone + "/";
                    String img_path = address + "head2.jpg";
                    Bitmap bmp = BitmapFactory.decodeFile(img_path);
                    String myName = i.getStringExtra("name");
                    if (bmp == null) {
                        headimage.setImageResource(R.drawable.default_useravatar);
                    } else {
                        headimage.setImageBitmap(bmp);
                    }
                    name.setText(myName);
                    SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("name", i.getStringExtra("name"));
                    int sexdata = i.getIntExtra("sex", 0);
                    editor.putString("sex", sexItems[sexdata]);
                    editor.commit();
                    if (myPhone.equals(myName)) {
                        Intent intent = new Intent(MainActivity.this, MyInforActivity.class);
                        startActivity(intent);
                        ToastR.setToast(MainActivity.this, "请点击姓名，设置你的名字");
                    }
                } else {
                    new ResponseErrorProcesser(MainActivity.this, i.getIntExtra("error_code", -1));
                }

            }
        });
    }

    protected void onResume() {
        SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        String str = preferences.getString("name", "");
        String myPhone = preferences.getString("phone", "");

        String address = ReceiverProcesser.getMyDataRoot(this) + "/" + myPhone + "/";
        String img_path = address + "head2.jpg";
        if (str == null || str.equals("")) {
            getInfor();
        } else {
            Bitmap bmp = BitmapFactory.decodeFile(img_path);
            if (bmp == null) {
                headimage.setImageResource(R.drawable.default_useravatar);
            } else {
                headimage.setImageBitmap(bmp);
            }
            name.setText(str);
        }
        super.onResume();
    }

    //Toolbar和NavigationView初始化
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);//激活滑动操作
        getSupportActionBar().setDisplayShowTitleEnabled(false);//除去标签
        toolbar.setTitle("Chat");//父标题
        toolbar.setSubtitle("珞宾对讲");//子标题

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //TODO 显示当前版本号
        try {
            Menu menu1 = navigationView.getMenu();
            menu1.getItem(4).setTitle("检查更新 (" + new GetAppVersion(MainActivity.this).getVersionName() + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }
        View headerView = navigationView.getHeaderView(0);
        headimage = (ImageView) headerView.findViewById(R.id.headimage);
        name = (TextView) headerView.findViewById(R.id.name);
        headimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MyInforActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(true);
        }
    }

    public void refreshData(String type, final int number) {
        if (type.equals(msgType)) {
            if (number >= MENU_ONE) {
                notifice = MENU_ONE;
            } else if (number == MENU_ZERO) {
                notifice = MENU_ZERO;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (notifice > MENU_ZERO) {
            menu.findItem(R.id.message_notification).setIcon(R.drawable.addgroup_new);
        } else if (notifice == MENU_ZERO) {
            menu.findItem(R.id.message_notification).setIcon(R.drawable.addgroup);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
//            ToastR.setToast(this, "功能待完善");
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, SearchActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.message_notification) {
            notifice = 0;
            item.setIcon(R.drawable.addgroup);
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, NotificationActivity.class);
            startActivityForResult(intent, ADDGROUP_CODE);
            return true;
        } else if (id == R.id.add_menu) {
            addPopWindow.showPopupWindow(mTabLayout, mContext);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     *
     */
    AddPopWindow addPopWindow;

    /**
     *
     */
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {

        public void onClick(View v) {
            addPopWindow.dismiss();
            switch (v.getId()) {
                case R.id.layout_add_friend:
                    ToastR.setToast(mContext, "加好友");

                    break;
                case R.id.layout_create_team:
                    Intent i = new Intent(mContext, CreateGroupActivity.class);
                    startActivity(i);
                    break;
                case R.id.layout_group_management:
                    ToastR.setToast(mContext, "组管理");
                case R.id.layout_exit:
                    ToastR.setToast(mContext, "退出");
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADDGROUP_CODE) {
            if (resultCode >= RESULT_HAS_CODE) {
                refreshData(msgType, MainActivity.MENU_ONE);
            } else {
                refreshData(msgType, MainActivity.MENU_ZERO);
            }
            supportInvalidateOptionsMenu();
        } else if (requestCode == ADDFriend_CODE) {
            if (resultCode >= RESULT_HAS_CODE) {
                refreshData(friendType, MainActivity.MENU_ONE);
            } else {
                refreshData(friendType, MainActivity.MENU_ZERO);
            }
            supportInvalidateOptionsMenu();
        } else {

        }
    }

    public void upDataMsg() {
        ((TabFragmentMsg) mFragments.get(0)).initMsgs(MainActivity.this);
    }


    /**
     * 设置Tablayout上的标题的角标
     */
    public void setUpTabBadge() {
        TabLayout.Tab tab = mTabLayout.getTabAt(0);
        if (tab != null) {
            View view = LayoutInflater.from(this).inflate(R.layout.tab_title_layout, null);
            ((TextView) view.findViewById(R.id.tv_title)).setText(mTitleList.get(0));
            tab.setCustomView(view);
            badgeView = new BadgeView(this, view);
            badgeView.setBadgePosition(BadgeView.POSITION_RIGHT);
            badgeView.hide();
        }
    }

    public void setUpTabBadge(int new_msg) {
        if (new_msg == 0) {
            badgeView.hide();
        } else if (new_msg == 99) {
            badgeView.setText("99+");
            badgeView.show();
        } else {
            badgeView.setText(new_msg + "");
            badgeView.show();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.   new GetAppVersion(act)
        int id = item.getItemId();

        if (id == R.id.my_documents) {
            startActivity(new Intent(this, DocumentAllActivity.class));
            // Handle the camera action
       /* } else if (id == R.id.add_friend) {
            startActivity(new Intent(MainActivity.this, NotificationActivity.class));
        } else if (id == R.id.scan) {*/

        } else if (id == R.id.nav_update) {
            checkVersion(true);
        } else if (id == R.id.setting) {
            startActivity(new Intent(MainActivity.this, SettingActivity.class));
       /* } else if (id == R.id.nav_share) {*/

        } else if (id == R.id.nav_loginout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("确认退出帐号吗？");

            builder.setTitle("提示");

            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
//                    //删除缓存的好友信息
//                    DBHelperFriendsList db = new DBHelperFriendsList(MainActivity.this , DBTableName.getTableName(mContext, DBHelperFriendsList.NAME));
//                    db.deleteDatabase(DBTableName.getTableName(mContext, DBHelperFriendsList.NAME));
//                    //删除缓存的群组信息
//                    DBHelperTeamList teamHelper = new DBHelperTeamList(MainActivity.this, DBTableName.getTableName(mContext, DBHelperTeamList.NAME));
//                    teamHelper.deleteDatabase(DBTableName.getTableName(mContext, DBHelperTeamList.NAME));

                    SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("token", "");
                    editor.putString("phone", "");
                    editor.putString("name", "");
                    editor.commit();
                    NotificationManager nm = (NotificationManager) (getSystemService(NOTIFICATION_SERVICE));
                    nm.cancel(-1);//消除对应ID的通知
                    //SettingRW.reDefault(MainActivity.this);
                    SharedPreferencesUtils.put(MainActivity.this, "data_init", true);
                    ActivityCollector.finishAct();
                    ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
                    MyService.start(MainActivity.this, ProtoMessage.Cmd.cmdLogout.getNumber(), builder.build());
                    MyService.restart(MainActivity.this);
                    GlobalStatus.clearChatRoomMsg();
                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            });

            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.create().show();
        } else if (id == R.id.nav_close) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("确认退出软件吗？");

            builder.setTitle("提示");

            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    GlobalStatus.clearChatRoomMsg();
                    ActivityCollector.finishAct();
                    Intent stopIntent = new Intent(MainActivity.this, MyService.class);
                    stopService(stopIntent);
                    NotificationManager nm = (NotificationManager) (getSystemService(NOTIFICATION_SERVICE));
                    nm.cancel(-1);//消除对应ID的通知

                }
            });

            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.create().show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        notifice = 0;
        if (mNotifyFriendBroadcast != null) {
            mNotifyFriendBroadcast.stop();
        }
        if (mInitDataBroadcast != null) {
            mInitDataBroadcast.stop();
        }
//        if(mLocationBroadcast != null){
//            mLocationBroadcast.stop();
//        }
        try {
            if (downloadReceiver != null) {
                unregisterReceiver(downloadReceiver);
            }
            if (notifyReceiver != null) {
                unregisterReceiver(notifyReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(0);//消除对应ID的通知
        nm.cancel(-1);//消除对应ID的通知

        downloader.setbStop(true);
        nm.cancel(NotificationUtil.NOTIFYCATIONID);
        super.onDestroy();
    }

    /**
     * 检查版本是否是最新版本
     */
    private void checkVersion(final boolean btnChecking) {
        try {
            if(!UPDATE_CHECK){
                return;
            }
            final Activity act = MyGetActivity.getParent(this);
            if (downloader.isbDownloading()) {
                ToastR.setToast(act, act.getString(R.string.download_doing));
                return;
            }
            if (mIsChecking) {
                Log.d("CHAT_CHECK_VERSION", "正在检查中，请稍候再试.");
                return;
            }
            mIsChecking = true;
            final GetAppVersion appVersion = new GetAppVersion(act);
            String url = getResources().getString(R.string.update_url);
            Log.d("CHAT_CHECK_VERSION", "正在检查是否有新版本");
            MyAsynHttpClient client = new MyAsynHttpClient(act);
            client.get(url, new JsonHttpResponseHandler("utf-8") {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        int serverVersion = response.getInt(act.getString(R.string.download_version_key));
                        if (appVersion.getVersionCode() < serverVersion) {
                            String updateDescription = "";
                            updateDescription = response.getString(act.getString(R.string.download_version_update_description));
                            promptNewVersion(appVersion.getVersionName(), response.getString(act.getString(R.string.download_version_name)),
                                    response.getString(act.getString(R.string.download_url_key)), updateDescription);
                        } else {
                            Log.i("chatjrd", "" + R.string.download_already_latest);
                            if (btnChecking) {
                                ToastR.setToastLong(mContext, act.getString(R.string.download_already_latest));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (btnChecking) {
                            ToastR.setToast(mContext, act.getString(R.string.download_check_version_fail));
                        }
                    }
                }

                @Override
                public void onFinish() {
                    mIsChecking = false;
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    if (btnChecking) {
                        ToastR.setToastLong(mContext, "检查更新失败，请检查是否连接网络");
                    }
                }
            });
        } catch (GetActivityNullException e) {

            e.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
            mIsChecking = false;
        }

    }

    /**
     * 新版本提示框
     *
     * @param oldVersionName
     * @param serverVersionName
     * @param url
     */
    protected void promptNewVersion(String oldVersionName, String serverVersionName, final String url,
                                    String updateDescription) {
        try {
            Activity act = MyGetActivity.getParent(this);
            String str = String.format(act.getString(R.string.download_find_new_version), oldVersionName, serverVersionName);

            if (updateDescription != null && updateDescription.length() > 0) {
                updateDescription = updateDescription.replace("\\n", "\n");
                str = str + "\n" + "--------------------------------------" + "\n" + updateDescription;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext); // 先得到构造器
            builder.setTitle(act.getString(R.string.download_prompt_title))
                    .setMessage(str).setPositiveButton(
                    act.getString(R.string.download_btn_start), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            checkNetWork(url);

                        }
                    })
                    .setNegativeButton(act.getString(R.string.download_btn_later),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
            builder.show();
        } catch (GetActivityNullException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //检查网络是否是移动流量
    private void checkNetWork(final String url) {
        if (ConnUtil.checkNetworkWifi(mContext)) {
            downloadNewVersion(url);
        } else {
            Dialog dialog = new android.app.AlertDialog.Builder(mContext).setTitle("提示：")
                    .setMessage(getString(R.string.download_not_wifi)).setPositiveButton("确定",
                            new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    downloadNewVersion(url);
                                }
                            })
                    .setNegativeButton("取消", new android.app.AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();
            dialog.show();
        }

    }

    /**
     * 下载最新版本
     *
     * @param url
     */
    protected void downloadNewVersion(final String url) {
        ToastR.setToast(mContext, mContext.getString(R.string.download_progress_btn_background));
        URL = url;
        showNotification = true;
//        if (!isConnected){
//            ToastR.setToast(mContext,"网络断开，请检查网络！");
//            return;
//        }
        try {
            final Activity act = MyGetActivity.getParent(this);
//            initDialogProgress();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        downloader.down_file(url, MyFileUtil.getSDCardPath(act));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Message msg = new Message();
                        msg.what = DOWN_FAILED;
                        msg.obj = e.getMessage();
                        mHandler.sendMessage(msg);
                        ToastR.setToast(mContext, act.getString(R.string.download_failed) + " " + e.getMessage());
                    }
                }
            }).start();
        } catch (GetActivityNullException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    /**
     * 初始化下载信息
     *
     * @return
     */
    public MyDownloader initDownload() {
        downloader = new MyDownloader();
        downloader.setDownloadListener(new MyAppUpgradeListener() {

            @Override
            public void onBeginDownload() {
                mHandler.sendEmptyMessage(DOWN_BEGIN);

            }

            @Override
            public void onGetSize(int filesize) {
                Message msg = new Message();
                msg.what = DOWN_GET_SIZE;
                msg.obj = filesize;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onProgress(int downloadedSize) {
                Message msg = new Message();
                msg.what = DOWN_PROGRESS;
                msg.obj = downloadedSize;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onComplete() {
                mHandler.sendEmptyMessage(DOWN_COMPLETE);

            }

            @Override
            public void onFailed(Throwable e) {
                Message msg = new Message();
                msg.what = DOWN_FAILED;
                msg.obj = e.getMessage();
                mHandler.sendMessage(msg);

            }

        });
        return downloader;
    }

    /**
     * 安装apk
     */
    protected void doInstall() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        File file = new File(MyFileUtil.getSDCardPath(mContext) + "/pocdemo.apk");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//如果不加，最后安装完成，点打开，无法打开新版本应用。
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivity(intent);
//		android.os.Process.killProcess(android.os.Process.myPid());//如果不加，最后不会提示完成、打开

    }

    /**
     * 下载失败提示
     */
    protected void doDownFailed() {
//        dlgProgress.dismiss();
        try {
            ToastR.setToast(mContext, mContext.getString(R.string.download_failed));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        if (MainActivity.mAction != null) {
            if (MainActivity.mAction.equals(Intent.ACTION_SEND)) {
                MainActivity.mAction = null;
                finish();
            }
        }
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (MainActivity.mAction != null) {
            if (MainActivity.mAction.equals(Intent.ACTION_SEND)) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    finish();
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setNoScrollViewPager(boolean NoScroll) {
        mViewPager.setScroll(NoScroll);
    }

    private void handleImage() {
        mIntent = getIntent();
        mAction = mIntent.getAction();
//        String type = intent.getType();
        if (mAction != null) {
            if (mAction.equals(Intent.ACTION_SEND)) {
                //&& getIntent().getType().equals("image/*") && getIntent().getType().equals("text/plain")) {
                mUri = mIntent.getParcelableExtra(Intent.EXTRA_STREAM);
                mText = mIntent.getStringExtra(Intent.EXTRA_TEXT);
            }
        }
    }

}
