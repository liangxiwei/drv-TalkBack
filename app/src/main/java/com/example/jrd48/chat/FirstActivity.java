package com.example.jrd48.chat;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.FileTransfer.TransferService;
import com.example.jrd48.chat.SQLite.LinkmanRecordHelper;
import com.example.jrd48.chat.SQLite.MsgRecordHelper;
import com.example.jrd48.chat.SQLite.SQLiteTool;
import com.example.jrd48.chat.SQLite.TeamMemberHelper;
import com.example.jrd48.chat.SQLite.TeamRecordHelper;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.AppliedFriendsList;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.friend.FriendsDetailsActivity;
import com.example.jrd48.chat.group.DBHelperTeamList;
import com.example.jrd48.chat.group.DBManagerTeamList;
import com.example.jrd48.chat.group.GroupMemberDetailsActivity;
import com.example.jrd48.chat.group.InviteJoinGroupActivity;
import com.example.jrd48.chat.group.ModifyPriorityPrompt;
import com.example.jrd48.chat.group.ModifyPrioritytListener;
import com.example.jrd48.chat.group.ModifyTeamMsgActivity;
import com.example.jrd48.chat.group.MsgTool;
import com.example.jrd48.chat.group.ShowAllTeamMemberActivity;
import com.example.jrd48.chat.group.ShowTeamInfoPrompt;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.chat.permission.PermissionUtil;
import com.example.jrd48.chat.wiget.VideoLayout;
import com.example.jrd48.service.BluetoothMonitor;
import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyBroadcastReceiver;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.ApplyFriendProcesser;
import com.example.jrd48.service.protocol.root.AssignTeamAdminProcesser;
import com.example.jrd48.service.protocol.root.AutoCloseProcesser;
import com.example.jrd48.service.protocol.root.ChangeTeamMemberNickNameProcesser;
import com.example.jrd48.service.protocol.root.CloseRoomProcesser;
import com.example.jrd48.service.protocol.root.DeleteFriendProcesser;
import com.example.jrd48.service.protocol.root.DeleteTeamMemberProcesser;
import com.example.jrd48.service.protocol.root.DismissTeamProcesser;
import com.example.jrd48.service.protocol.root.LiveVideoCallProcesser;
import com.example.jrd48.service.protocol.root.ModifyTeamMemberPriorityProcesser;
import com.example.jrd48.service.protocol.root.NotifyProcesser;
import com.example.jrd48.service.protocol.root.ReceiverProcesser;
import com.example.jrd48.service.protocol.root.SearchFriendProcesser;
import com.example.jrd48.service.protocol.root.SendMsgProcesser;
import com.example.jrd48.service.protocol.root.SetFriendInfoProcesser;
import com.example.jrd48.service.protocol.root.SpeakerBeginProcesser;
import com.example.jrd48.service.protocol.root.SpeakerEndProcesser;
import com.example.jrd48.service.protocol.root.TeamMemberProcesser;
import com.example.jrd48.service.protocol.root.VoiceAcceptProcesser;
import com.example.jrd48.service.protocol.root.VoiceStartProcesser;
import com.google.protobuf.ByteString;
import com.luobin.dvr.DvrConfig;
import com.luobin.dvr.DvrService;
import com.luobin.dvr.R;
import com.luobin.manager.SelectActivity;
import com.luobin.manager.SelectViewManager;
import com.luobin.model.CallState;
import com.luobin.search.friends.map.TeamMemberLocationActivity;
import com.luobin.timer.ChatManager;
import com.luobin.ui.FriendDetailsDialogActivity;
import com.luobin.ui.SelectMemberActivity;
import com.luobin.voice.AudioInitailFailedBroadcast;
import com.luobin.voice.AudioRecordStatusBroadcast;
import com.luobin.voice.VoiceHandler;
import com.luobin.widget.PromptDialog;
import com.video.VideoCallActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.lake.librestreaming.client.RESClient;

import static android.provider.Settings.System.NAVIGATION_BAR_CTRL;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.jrd48.service.protocol.root.ReceiverProcesser.getMyDataRoot;


public class FirstActivity extends SelectActivity implements OnClickListener, OnTouchListener, PermissionUtil.PermissionCallBack
        , Animation.AnimationListener {

    public static final int SWITCH_BTN_ALPHA = 200;
    @BindView(R.id.btn_return)
    Button btnReturn;
    @BindView(R.id.prefix_camera)
    Button prefixCamera;
    @BindView(R.id.rear_camera)
    Button rearCamera;
    @BindView(R.id.picture_in_picture)
    Button pictureInPicture;
    @BindView(R.id.voice)
    Button voice;
    @BindView(R.id.goto_map)
    Button gotoMap;
    @BindView(R.id.do_not_disturb)
    Button doNotDisturb;
    private volatile boolean showScreenOnce = true;
    private Cursor tempCursorUser;
    private boolean once = true;
    /*private static String[] indexStr = { "A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z"};*/
    /*private static String[] indexStr = { "a", "b", "c", "d", "e", "f", "g", "h",
            "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u",
            "v", "w", "x", "y", "z"};*/
    private boolean pauseIs = false;
    //notify user
    private RelativeLayout callNotify;
    private RelativeLayout speakNotify;
    private RelativeLayout mapNotify;
    private RelativeLayout backNotify;
    private RelativeLayout doNotTouch;
    private ImageView move;
    private int record = 0;
    private Animation alphaAnimation1;
    private Animation alphaAnimation2;
    private int width;
    private int height;
    private SettingRW mSettings;

    private float numRow;
    private float listH;
    private float gridviewH;
    private float singleH;
    private float minListH;
    private RelativeLayout list_layout;
    private boolean isGetHW = false;//是否获取过高度
    //调用相机
    private boolean run = false;
    public static final int TAKE_PHOTO = 1;
    public static final int TAKE_VIDEO = 2;
    //调用图库
    private static final int PICTURE = 0;
    private static final int VIDEO = 4;
    private static final int DISMISS_OR_QUIT = 5;
    public static final int REFRESH_REMARK = 6;
    public static final int REFRESH_TEAM_REMARK = 7;
    private int mansNum = 0;
    private int nowMansNum = 0;
    public static String add = "添加好友";
    public static String change = "修改";
    public boolean photo_or_no = false;
    private VideoLayout videoLayout;
    AdditionLayout additionLayout;
    LinearLayout linearLayout1;
    int type;
    Context mContext;
    List<AppliedFriends> listMembersCache;
    String phone;
    AppliedFriends mViewFriendsMsg;
    private BottomLayoutManager bottomLayoutManager;
    int teamRole;
    TeamMemberInfo mTeamMemberInfo = null;
    //******************title显示说话人信息********************
    int MY_PERMISSIONS_REQUEST_LOCATION = 10011;
    int MY_PERMISSIONS_REQUEST_AUDIO = 10022;
    int MY_PERMISSIONS_REQUEST_VIDEO = 10044;
    List<TeamMemberInfo> mTMInfo = new ArrayList<TeamMemberInfo>();
    List<TeamMemberInfo> mTMInfoNew = new ArrayList<TeamMemberInfo>();
    //****************************拍照发送******************************
    String mCurrentPhotoPath;
    String mCurrentVideoPath;
    private String changeType = "Group";
    private boolean up_down = false;
    private ProtoMessage.ChatRoomMsg r;
    private ListView memberListView;
    private FloatingActionButton button;
    private Button btn_sent_msg;
    private Button addition;
    private int SET_FRIEND_STAR = 1;
    private EditText edit;
    private ListView list;
    private MsgAdapter adapter;
    private UserAdapter adapterU;
    String menuType = "";
    private final String closeRoom = "closeRoom";
    private final String quitTeam = "quitTeam";
    private final String dismissTeam = "dismissTeam";
    private final String deleteLinkMan = "deleteLinkMan";
    //    private ImageView speakingimage;
    private ImageView showSpeakMan;
    private TextView groupNullHint;
    private TextView speakingname;
    private ImageView speakingstate;
    private ImageView speakingMenu;
    private ImageView videoCall;
    private LinearLayout pull_switch;
    private LinkmanRecordHelper linkmanRecordHelper;
    private TeamRecordHelper teamRecordHelper;
    private SQLiteDatabase dbMsg;
    private List<TeamMemberInfo> allTeamMemberInfos;
    boolean isBBS = false;

    private Handler refreshHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    adapterU.notifyDataSetChanged();
                    break;
                case 1:
                    adapter.notifyDataSetChanged();
                    list.setSelection(msgList.size());
                    break;
                case 2:
                    if (r != null) {
                        name.setText(groupName);
                        sta_num.setText(nowMansNum + "/" + mansNum);
                        r = null;
                    } else {
                        name.setText(groupName);
                        sta_num.setText("人数:" + mansNum);
                    }
                    break;
                case 3:
                    changespeaking(changeState, changePhone);
                    break;
                case 4:
                    /*new TimeCount(300,300).start();*/

//                    mMap.setaMap(savedInstanceState, myPhone, view2, single, linkmanPhone, group, userList, viewPager, call_hungon == 1);
//                    mMap.onResume();
                    mapInitIs = true;
                    if (bottomLayoutManager != null) {
                        // bottomLayoutManager.show(false);
                    }
                    _OnResume();
                    initNotifyUser();
                    if (uri != null) {
                        sendImg(uri);
                    }
                    if (mText != null && !mText.equals("")) {
                        sendText(mText);
                    }
                    break;
                case 5:
                    name.setText(groupName);
                    sta_num.setText("人数:" + mansNum);
                    break;
                case 6:
                    name.setText(groupName);
                    sta_num.setVisibility(VISIBLE);
                    speakingMenu.setVisibility(VISIBLE);
                    pull_switch.setVisibility(VISIBLE);
                    break;
                case 7:
                    name.setText(linkman);
                    sta_num.setVisibility(GONE);
                    speakingMenu.setVisibility(GONE);
                    pull_switch.setVisibility(GONE);
                    break;
                case 8:
                    msgDB_Single();
                    break;
                case 9:
                    msgDB_Team();
                    break;
                case 10:
                    //hanjiming
                    getGroupMan(group);
                    break;
                default:
                    break;
            }
        }
    };
    protected PermissionUtil mPermissionUtil;
    private Handler mHandlerTemp = new Handler();
    private int sentType = 0;
    private String[] online = {"不在线", "在线"};

    private int additionType = 0;
    private String tempStr;
    private Bitmap tempBit;
    private long group;
    private long groupID;
    private String linkman;
    private boolean single;
    private IntentFilter filter;
    private IntentFilter filterDelete;
    private GetMsgReceiiver getMsgReceiiver;
    private IntentFilter filterstatus;
    private ChatStatusReceiver chatStatusReceiver;
    private ChatCallReceiver chatCallReceiver;
    private IntentFilter filterRoom;
    private CloseRoomReceiiver closeRoomReceiiver;
    private RefreshReceiiver refreshReceiiver;
    private IntentFilter filterUp;
    private UploadReceiiver uploadReceiiver;
    private String myPhone;
    private AnimationDrawable AniDraw;
    private int call_hungon;
    private boolean speaking_now = false;
    private boolean temp_exist = false;
    private AudioRecordStatusBroadcast mAudioRecordStatusBroadcast;
    //通话，挂断
    private FloatingActionButton call;
    private FloatingActionButton hangup;
    //	AppliedFriendsList afList;
    //双指坐标
    private float x1, y1;
    private boolean finger = false;
    private boolean location = false;
    private Uri imageUri;
    private String addressTemp;
    //正在说话的用户
    private User speakingUser;
    private List<Msg> msgList = new ArrayList<>();
    private List<User> userList = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> listImageItem;
    private String linkmanPhone;
    private String groupName;
    private String groupDesc;
    private int groupPriority;
    private TextView name;
    private TextView sta_num;
    private long roomId;
    private AudioInitailFailedBroadcast mAudioBroadcast;
    // add by qhb
    private String noSet = "未设置";
    private boolean cancel = false;
    private boolean bCanCopy = false;
    private Bitmap tempHead = null;

    private boolean hostOk = false;
    public static final String SING_OUT = "singout";
    //private OfflineTTS TTs;

    //ViewPager
    private View view1, view2;
    private List<View> viewList;//view数组
    private MyViewPager viewPager;  //对应的viewPager

    Bundle savedInstanceState;

    private boolean mapInitIs = false;
    //    private MapHandler mMap;
    private int changeState;
    private String changePhone;
    private Button btnToMap;
    boolean showNotification = true;
    private int callType;// 0 代表不需要，1代表接收呼叫，2代表发起呼叫
    private String uri;
    public static String mText;
    public String maction;

    @Override
    public void onAnimationStart(Animation animation) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        //动画结束时结束欢迎界面并转到软件的主界面
        if (animation == alphaAnimation1) {
            mapNotify.setVisibility(View.GONE);
            doNotTouch.setVisibility(View.VISIBLE);
            rightMoveToLeft();
            doNotTouch.setVisibility(View.GONE);
            backNotify.setVisibility(View.VISIBLE);
            move = (ImageView) findViewById(R.id.left_to_right);
            alphaAnimation2 = AnimationUtils.loadAnimation(this, R.anim.left_to_right);
            alphaAnimation2.setFillAfter(true);  //设置动画的最后一帧是保持在View上面
            move.setAnimation(alphaAnimation2);
            alphaAnimation2.setAnimationListener(this);
            record = 2;
        }
        if (animation == alphaAnimation2) {
            backNotify.setVisibility(View.GONE);
            doNotTouch.setVisibility(View.VISIBLE);
            leftMoveToRight();
            doNotTouch.setVisibility(View.GONE);
            callNotify.setVisibility(View.VISIBLE);

            call.setVisibility(VISIBLE);
            hangup.setVisibility(GONE);
            button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.FloatingActionButton_false)));
            button.setImageResource(R.drawable.speak);
            record = 3;

        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        Log.i("pocdemo", "onWindowFocusChanged");
        if (!isFinishing() && !pauseIs) {
            ChatManager.getInstance().showChatInfo(videoLayout);
        }
        if (showScreenOnce) {
            if (hasFocus) {
                showScreenOnce = false;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message message = new Message();
                        message.what = 4;
                        refreshHandler.sendMessage(message);
                    }
                }).start();
            }
        }
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        group = 0;
        linkmanPhone = "";
        callType = 0;
        finish();
        startActivity(intent);
        super.onNewIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("pocdemo", "first activity created");
        mContext = this;
        hideSystemNaviBar();
        GlobalStatus.setFirstCreating(true);
        GlobalStatus.setIsFirstPause(false);
        super.onCreate(savedInstanceState);
        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        this.savedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
//        mMap = new MapHandler(this);
        //
        checkRandomChat();
        bottomLayoutManager = new BottomLayoutManager(this);
        initViewPager();
        initView1();
        voice.setTextColor(getResources().getColor(R.color.match_btn_bg_press));
        if (!ConnUtil.isConnected(this)) {
            ToastR.setToast(this, "连接服务器失败，请检查网络连接");
            finish();
            return;
        }
        if (mPermissionUtil == null) {
            PermissionUtil.getInstance();
        }
        if (mPermissionUtil.isOverMarshmallow()) {
            mPermissionUtil.requestPermission(this, MY_PERMISSIONS_REQUEST_LOCATION, this, PermissionUtil.PERMISSIONS_LOCATION, PermissionUtil.PERMISSIONS_LOCATION);
        }
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        dataInit();
        List<View>  selectViews = new ArrayList<>();
        selectViews.add(prefixCamera);
        selectViews.add(rearCamera);
        selectViews.add(pictureInPicture);
        selectViews.add(voice);
        selectViews.add(gotoMap);
        selectViews.add(doNotDisturb);
        setSelectViewManager(new SelectViewManager(selectViews));

    }

    private void checkRandomChat() {
        group = getIntent().getLongExtra("group", 0);
        Log.d("chatJrd", "group = " + group);
        if (group == 0) {
            //Log.d("chatJrd","setIsRandomChat 520");
            //GlobalStatus.setIsRandomChat(false);
        } else {
            TeamInfo teamInfo = getTeamInfo();
            /*if (teamInfo != null && teamInfo.getTeamType() == ProtoMessage.TeamType.teamRandom.getNumber()) {
                //Log.d("chatJrd","setIsRandomChat 525");
                GlobalStatus.setIsRandomChat(true);
                SharedPreferences preference = this.getSharedPreferences("token", Context.MODE_PRIVATE);
                int teamRandom = preference.getInt("team_random", 0);
                int teamRandomVideo = preference.getInt("team_random_video", 0);
                Log.d("chatJrd","teamRandom="+teamRandom+",teamRandomVideo="+teamRandomVideo);
                if (teamRandom == 1 || teamRandomVideo == 1) {
                    GlobalStatus.setIsRandomChatVideo(false);
                }else{
                    GlobalStatus.setIsRandomChatVideo(true);
                }
            }else{
                //Log.d("chatJrd","setIsRandomChat 535");
                GlobalStatus.setIsRandomChat(false);
            }*/
        }
    }

    private void initNotifyUser() {
        mSettings = new SettingRW(this);
        mSettings.load();
        if (mSettings.isNotifyUser()) {
            callNotify = (RelativeLayout) findViewById(R.id.call_notify);
            speakNotify = (RelativeLayout) findViewById(R.id.speak_notify);
            mapNotify = (RelativeLayout) findViewById(R.id.map_notify);
            backNotify = (RelativeLayout) findViewById(R.id.back_notify);
            doNotTouch = (RelativeLayout) findViewById(R.id.do_not_touch);
            mapNotify.setVisibility(View.VISIBLE);
            move = (ImageView) findViewById(R.id.right_to_left);
            alphaAnimation1 = AnimationUtils.loadAnimation(this, R.anim.right_to_left);
            alphaAnimation1.setFillAfter(true);  //设置动画的最后一帧是保持在View上面
            move.setAnimation(alphaAnimation1);
            alphaAnimation1.setAnimationListener(this);
            record = 1;
        }
    }

    private void initViewPager() {
        viewPager = (MyViewPager) findViewById(R.id.viewpager);
        LayoutInflater inflater = getLayoutInflater();
        view1 = inflater.inflate(R.layout.chat, null);
        view2 = inflater.inflate(R.layout.map, null);


        viewList = new ArrayList<View>();// 将要分页显示的View装入数组中
        viewList.add(view1);
        viewList.add(view2);

        PagerAdapter pagerAdapter = new PagerAdapter() {
            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                return viewList.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                container.removeView(viewList.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(viewList.get(position));
                return viewList.get(position);
            }
        };
        viewPager.setAdapter(pagerAdapter);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        if (mMap.getMapView() != null) {
//            mMap.getMapView().onSaveInstanceState(outState);
//        }
    }

    private void initView1() {
        Log.v("pocdemo", "initView1");

        mPermissionUtil = PermissionUtil.getInstance();
        try {
            // 取所有好友信息
            getlistMembersCache();

            //**************************悬浮按钮*****************************************
            call = (FloatingActionButton) findViewById(R.id.call);
            hangup = (FloatingActionButton) findViewById(R.id.hangup);
            call.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (record == 3) {
                        hangup.setVisibility(VISIBLE);
                        call.setVisibility(GONE);
                        button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.FloatingActionButton_up)));
                        button.setImageResource(R.drawable.speak_call);
                        callNotify.setVisibility(View.GONE);
                        speakNotify.setVisibility(View.VISIBLE);
                        record = 4;
                    } else if (record == 0) {
                        CallClick();
                    }
                }
            });
            hangup.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (record == 0) {
                        HungupClick();
                    }
                }
            });
            //**************************悬浮按钮*****************************************

            setGridView();

            //***********************其他初始设置**********************************
            videoLayout = (VideoLayout) findViewById(R.id.video_layout);
            videoLayout.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.e("wsDvr", "videoLayout onTouch");
                    if (bottomLayoutManager != null) {
                        //   bottomLayoutManager.show(true);
                    }
                    return false;
                }
            });
            additionLayout = (AdditionLayout) view1.findViewById(R.id.addition_1);
//            speakingimage = (ImageView) view1.findViewById(R.id.speakingimage);
            showSpeakMan = (ImageView) findViewById(R.id.show_speakman);
            groupNullHint = (TextView) findViewById(R.id.null_Text);
            //  singleMan = (ImageView) findViewById(R.id.single_other_man);
            speakingname = (TextView) findViewById(R.id.speakingname);
            speakingstate = (ImageView) findViewById(R.id.speakingstate);
            speakingMenu = (ImageView) findViewById(R.id.speaking_menu);
            videoCall = (ImageView) findViewById(R.id.video_call);
            pull_switch = (LinearLayout) view1.findViewById(R.id.pull_switch);
            btnToMap = (Button) view1.findViewById(R.id.btn_to_map);
            btnToMap.setOnClickListener(this);
            btnToMap.getBackground().setAlpha(SWITCH_BTN_ALPHA);
            speakingMenu.setOnClickListener(this);
            videoCall.setOnClickListener(this);
            button = (FloatingActionButton) findViewById(R.id.button_1);
            btn_sent_msg = (Button) view1.findViewById(R.id.send_msg);
            edit = (EditText) view1.findViewById(R.id.edit_1);
            addition = (Button) view1.findViewById(R.id.addition);
            linearLayout1 = (LinearLayout) view1.findViewById(R.id.linearLayout1);
            //TTs = OfflineTTS.getInstance(this);
            list_layout = (RelativeLayout) view1.findViewById(R.id.list_layout);

            SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
            myPhone = preferences.getString("phone", "");

            //title动画初始化
            speakingstate.setBackgroundResource(R.drawable.speakingt);
            AniDraw = (AnimationDrawable) speakingstate.getBackground();

            speakingUser = new User(1, "", "", 0);
            changeTitle();
            button.setOnTouchListener(this);
            btn_sent_msg.setOnTouchListener(this);
            addition.setOnClickListener(this);
            edit.addTextChangedListener(textChange);
            singleH = 80 * getResources().getDisplayMetrics().density;
            minListH = 110 * getResources().getDisplayMetrics().density;
            //button.setBackgroundResource(R.drawable.speaker_3);
            call_hungon = 0;
            //***********************其他初始设置**********************************

            //***********************设置ListView**********************************
            adapter = new MsgAdapter(FirstActivity.this, R.layout.msg_item, msgList, myPhone);
            list = (ListView) view1.findViewById(R.id.list_view);
            //initMsgs();//初始化消息
            list.setOnTouchListener(this);
            list.setAdapter(adapter);

            //***********************设置ListView**********************************

            pull_switch.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        //当手指按下的时候
                        listH = list_layout.getHeight();
                        gridviewH = memberListView.getHeight();
                        x1 = event.getRawX();
                        y1 = event.getRawY();
                    }

                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        listH = list_layout.getHeight();
                        gridviewH = memberListView.getHeight();
                        if (Math.abs(event.getRawX() - x1) < 3 &&
                                Math.abs(event.getRawY() - y1) < 3) {
                            if (memberListView.getHeight() > singleH) {
                                list_layout.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
                                memberListView.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, (int) (singleH + 0.5f), 0));
                            } else {
                                list_layout.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, (int) (minListH + 0.5f), 0));
                                memberListView.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
                            }
                        }
                    }

                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        float x2, y2;
                        x2 = event.getRawX();
                        y2 = event.getRawY();
                        if (Math.abs(x1 - x2) < Math.abs(y1 - y2)) {
                            numRow = y2 - y1;
                            float tempLH = listH - numRow;
                            float tempGH = gridviewH + numRow;
                            if (tempGH < singleH) {
                                list_layout.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
                                memberListView.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, (int) (singleH + 0.5f), 0));
                                //Log.i("chatjrd","在第一个if里面  "+tempLH+"  "+tempGH);
                            } else if (tempLH < minListH) {
                                list_layout.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, (int) (minListH + 0.5f), 0));
                                memberListView.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
                                //Log.i("chatjrd","在第二个if里面  "+tempLH+"  "+tempGH);
                            } else {
                                list_layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                        (int) (singleH + 0.5f), (int) (tempLH - singleH + 0.5f)));
                                memberListView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                        (int) (singleH + 0.5f), (int) (tempGH - singleH + 0.5f)));
                                //Log.i("chatjrd","在第三个if里面  "+tempLH+"  "+tempGH);
                            }
                        }
                    }
                    return true;
                }
            });

            showSpeakMan.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
//                    showSpeakMan.setVisibility(GONE);
                }
            });
            list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                    int i = msgList.get(position).getType();
                    if (i == Msg.TYPE_SENT || i == Msg.TYPE_SENT_IMAGE || i == Msg.TYPE_SENT_VIDEO) {
                        cancel = true;
                    } else {
                        cancel = false;
                    }

                    if (i == Msg.TYPE_SENT || i == Msg.TYPE_RECEIVED) {
                        bCanCopy = true;
                    } else {
                        bCanCopy = false;
                    }
                    return false;
                }
            });
            list.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                public void onCreateContextMenu(ContextMenu menu, View v,
                                                ContextMenu.ContextMenuInfo menuInfo) {
                    //info.id得到listview中选择的条目绑定的id
//				menu.add(0, 4, 0, "信息查询");
                    MenuItem mi = menu.add(0, 6, 0, "复制");

                    mi.setEnabled(bCanCopy);

                    menu.add(0, 7, 0, "删除");
                    if (cancel) {
                        menu.add(0, 8, 0, "撤回");
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void dataInit() {
        group = 0;
        groupName = "";
        linkman = "";
        linkmanPhone = "";
        type = 0;

        Intent intent = getIntent();
        callType = intent.getIntExtra("callType", 0);
        name = (TextView) findViewById(R.id.name);
        sta_num = (TextView) findViewById(R.id.num_man);

        linkman = intent.getStringExtra("linkmanName");
        linkmanPhone = intent.getStringExtra("linkmanPhone");
        group = intent.getLongExtra("group", 0);
        groupName = intent.getStringExtra("group_name");
        groupDesc = intent.getStringExtra("desc");
        groupPriority = intent.getIntExtra("priority", 0);
        groupID = intent.getLongExtra("group_id", 0);
        isBBS = intent.getBooleanExtra("isBBS",false);
        r = (ProtoMessage.ChatRoomMsg) intent.getSerializableExtra("member");
        call_hungon = intent.getExtras().getInt("data");
        uri = intent.getExtras().getString("uri");
        mText = intent.getExtras().getString("text");
        maction = intent.getExtras().getString("maction");
        if (group == 0) {
            Bitmap bitmap = GlobalImg.getImage(FirstActivity.this, linkmanPhone);
            int sex = intent.getIntExtra("linkmanSex", 0);
            userT(linkman, ProtoMessage.ChatStatus.csOk_VALUE, linkmanPhone, sex);
            Message message = new Message();
            message.what = 0;
            refreshHandler.sendMessage(message);
            Message message2 = new Message();
            message2.what = 7;
            refreshHandler.sendMessage(message2);
            single = true;
            if (GlobalStatus.equalPhone(linkmanPhone)) {
                call_hungon = 1;
                temp_exist = true;
                videoLayout.showVideo(FirstActivity.this);
//                videoLayout.showSingle(FirstActivity.this, linkmanPhone);

            }

            SharedPreferencesUtils.put(this, ReceiverProcesser.UPDATE_KEY, linkmanPhone);
            GlobalStatus.setTempChat(linkmanPhone);
            //list_layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        } else {
            GlobalStatus.setTempChat("");
            single = false;
            SharedPreferencesUtils.put(this, ReceiverProcesser.UPDATE_KEY, groupName);
            type = intent.getExtras().getInt("type");
            if (type == ProtoMessage.TeamRole.Owner_VALUE) {
                Log.i("chatJrd", "我是群主");
                //ToastR.setToast(FirstActivity.this, "我是群主");
            } else if (type == ProtoMessage.TeamRole.Manager_VALUE) {
                Log.i("chatJrd", "我是管理员");
                //ToastR.setToast(FirstActivity.this, "我是管理员");
            } else {
                Log.i("chatJrd", "我是群成员");
                //ToastR.setToast(FirstActivity.this, "我是群成员");
            }
            Message message = new Message();
            message.what = 6;
            refreshHandler.sendMessage(message);
            getGroupMan(group);
            single = false;

            if (GlobalStatus.equalTeamID(group)) {
                call_hungon = 1;
                temp_exist = true;
                videoLayout.showVideo(FirstActivity.this);
            }
        }

        if (single) {
            if (!GlobalStatus.equalPhone(linkmanPhone)) {
                Intent service = new Intent(this, MyService.class);
                service.putExtra("ptt_key_action", false);
                this.startService(service);
            }
        } else {
            if (!GlobalStatus.equalTeamID(group)) {
                Intent service = new Intent(this, MyService.class);
                service.putExtra("ptt_key_action", false);
                this.startService(service);
            }
        }

        initHungonData();
        //*****************************接收数据***********************************

        //*******************************数据库初始化*********************************
        Message message = new Message();
        if (single) {
            message.what = 8;
        } else {
            message.what = 9;
        }
        refreshHandler.sendMessage(message);
        //*******************************数据库初始化*********************************

        //设置长时间监听
        filter = new IntentFilter();
        getMsgReceiiver = new GetMsgReceiiver();
        filter.addAction(ReceiverProcesser.ACTION);
        registerReceiver(getMsgReceiiver, filter);

        filterDelete = new IntentFilter();
        filterDelete.addAction("ACTION.refreshTeamList");
        registerReceiver(refreshTeamReceiver, filterDelete);

        //初始化监听蓝牙按键广播
        IntentFilter filterBluet = new IntentFilter();
        filterBluet.addAction(BluetoothMonitor.UPDATE_CHAT_VIEW);
        filterBluet.addAction(BottomLayoutManager.ACTION_VIDEO_CONTROL_SHOW);
        filterBluet.addAction(RESClient.ACTION_VIDEO_VOIDE_SWITCH);
        filterBluet.addAction(RESClient.ACTION_VIDEO_VOIDE_UPDATE);
        registerReceiver(updateViewReceiver, filterBluet);

        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.FRIEND_ACTION);
        filter.setPriority(Integer.MAX_VALUE);
        registerReceiver(myReceiver, filter);

        filterRoom = new IntentFilter();
        closeRoomReceiiver = new CloseRoomReceiiver();
        filterRoom.addAction(AutoCloseProcesser.ACTION);
        registerReceiver(closeRoomReceiiver, filterRoom);

        filterRoom = new IntentFilter();
        refreshReceiiver = new RefreshReceiiver();
        filterRoom.addAction("call.refreshReceiiver");
        registerReceiver(refreshReceiiver, filterRoom);

        filterUp = new IntentFilter();
        uploadReceiiver = new UploadReceiiver();
        filterUp.addAction("upload.percent");
        registerReceiver(uploadReceiiver, filterUp);

        setIntent(null);
    }

    private void initHungonData() {
        if (call_hungon == 0) {
            hangup.setVisibility(GONE);
            call.setVisibility(VISIBLE);
            //button.setBackgroundResource(R.drawable.speaker_3);
        } else if (call_hungon == 1) {
            Log.i("chatjrd", roomId + "");
            /*if (roomId == -1) {*/
            hangup.setVisibility(GONE);
            call.setVisibility(VISIBLE);
            call_hungon = 0;
            refreshHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    CallClick();
                }
            }, 300);
        }
    }

    public void HungupClick() {
        ProtoMessage.AcceptVoice.Builder builder = ProtoMessage.AcceptVoice.newBuilder();
        if (GlobalStatus.isStartRooming() && GlobalStatus.getChatRoomtempId() != 0) {
            builder.setRoomID(GlobalStatus.getChatRoomtempId());
        } else {
            builder.setRoomID(GlobalStatus.getRoomID());
        }
        if (builder.getRoomID() == -1) {
            GlobalStatus.setChatRoomtempId(-1);
        } else {
            GlobalStatus.setChatRoomtempId(0);
        }
        Log.v("wsDvr", "roomId:" + builder.getRoomID());
        builder.setAcceptType(ProtoMessage.AcceptType.atDeny_VALUE);
        MyService.start(FirstActivity.this, ProtoMessage.Cmd.cmdAcceptVoice.getNumber(), builder.build());
//        IntentFilter filter = new IntentFilter();
        VoiceHandler.doVoiceAction(mContext, false);
        GlobalStatus.setOldChat(0, "", 0);
        GlobalStatus.clearChatRoomMsg();
        finish();
//        filter.addAction(VoiceAcceptProcesser.ACTION);
//        new TimeoutBroadcast(FirstActivity.this, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
//            @Override
//            public void onTimeout() {
//                ToastR.setToast(FirstActivity.this, "超时");
//            }
//
//            @Override
//            public void onGot(Intent i) {
//                if (i.getIntExtra("error_code", -1) ==
//                        ProtoMessage.ErrorCode.OK.getNumber()) {
//                    saveMsgRemind("房间被成功挂断");
//                } else {
//                    if (i.getIntExtra("error_code", -1) != ProtoMessage.ErrorCode.NOT_FOUND_THIS_ROOM_VALUE) {
//                        fail(i.getIntExtra("error_code", -1));
//                    }
////                    ToastR.setToast(FirstActivity.this, "挂断出错");
//                    saveMsgRemind("房间被强行挂断");
//                }
//                if (single) {
//                    name.setText(linkman);
//                    sta_num.setVisibility(GONE);
//                    videoLayout.hideVideo();
//                    //TTs.play("退出和"+linkman+"的对讲房间");
//                } else {
//                    name.setText(groupName);
//                    sta_num.setText("人数:" + mansNum);
//                    //TTs.play("退出对讲群组，"+groupName);
//                }
//                call.setVisibility(VISIBLE);
//                hangup.setVisibility(GONE);
//                GlobalStatus.clearChatRoomMsg();
//                finish();
//                call_hungon = 0;
//                button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.FloatingActionButton_false)));
//                button.setImageResource(R.drawable.speak);
//                if (edit.length() == 0) {
//                    /*button.setBackgroundResource(R.drawable.speaker_3);
//                    buttonChange(button, 200);*/
//                    sentType = 0;
//                } else {
//                    /*button.setBackgroundResource(R.drawable.btn_sent);
//                    buttonChange(button, 200);*/
//                    sentType = 1;
//                }
//                if (single) {
//                    msgDB_Single();
//                } else {
//                    msgDB_Team();
//                }
//                reGridView();
//                speakingUser.setName("");
//                changeTitle();
////                mMap.refreshMapLocalData(userList, call_hungon == 1);
//            }
//        });
    }

    private void CallClick() {
        Log.v("wsDvr", "CallClick:" + callType);
        ChatManager.getInstance().setShow(false);
        if (bottomLayoutManager != null) {
            bottomLayoutManager.setPttEnable(false);
        }
        ProtoMessage.ChatRoomMsg chatRoomMsg = GlobalStatus.getChatRoomMsg();
        if (chatRoomMsg != null && callType == 0) {
            if (chatRoomMsg.getTeamID() != 0 && group == chatRoomMsg.getTeamID()) {
                call_hungon = 1;
                Log.v("pocdemo", "call_hungon:" + call_hungon);
                roomId = chatRoomMsg.getRoomID();
                r = chatRoomMsg;
                notifyCallSuccess();
                return;
            } else if (chatRoomMsg.getTeamID() == 0 && GlobalStatus.equalPhone(linkmanPhone)) {
                call_hungon = 1;
                Log.v("pocdemo", "call_hungon:" + call_hungon);
                roomId = chatRoomMsg.getRoomID();
                r = chatRoomMsg;
                notifyCallSuccess();
                return;
            } else {
                closeRoom(true);
            }
        } else if (callType == 1) {
            CallState callState = null;
            if (GlobalStatus.getCallCallStatus().containsKey("0" + linkmanPhone)) {
                callState = GlobalStatus.getCallCallStatus().get("0" + linkmanPhone);
            } else if (GlobalStatus.getCallCallStatus().containsKey("1" + String.valueOf(group))) {
                callState = GlobalStatus.getCallCallStatus().get("1" + String.valueOf(group));
            }

            if (callState != null && callState.getRoomId() != 0 && callState.getState() == GlobalStatus.STATE_CALL) {
                ProtoMessage.AcceptVoice.Builder builder = ProtoMessage.AcceptVoice.newBuilder();//发送接受
                builder.setAcceptType(ProtoMessage.AcceptType.atAccept_VALUE);
                builder.setRoomID(callState.getRoomId());
                MyService.start(mContext, ProtoMessage.Cmd.cmdAcceptVoice.getNumber(), builder.build());
                IntentFilter filter = new IntentFilter();
                filter.addAction(VoiceAcceptProcesser.ACTION);
                Log.i("pocdemo", "accept voice action, want room id...");
                roomId = callState.getRoomId();
                new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
                    @Override
                    public void onTimeout() {
                        ToastR.setToast(FirstActivity.this, "接收呼叫超时");
                        closeRoom(true);
                        Log.v(FirstActivity.class.getSimpleName(), "accept timeout");
                    }

                    @Override
                    public void onGot(Intent i) {
                        if (i.getIntExtra("error_code", -1) ==
                                ProtoMessage.ErrorCode.OK.getNumber()) {
                            call_hungon = 1;
                            roomId = GlobalStatus.getRoomID();
                            r = GlobalStatus.getChatRoomMsg();
//                            GlobalStatus.clearChatRoomMsg();
                            notifyCallSuccess();
                            //context.overridePendingTransition(R.anim.fade, R.anim.hold);
                        } else if (i.getIntExtra("error_code", -1) == ProtoMessage.ErrorCode.NOT_FOUND_THIS_ROOM.getNumber()) {
                            GlobalStatus.closeRoom(roomId);
                            CallClick();
                        } else {
                            ToastR.setToast(FirstActivity.this, "接收呼叫失败");
                            closeRoom(true);
                            fail(i.getIntExtra("error_code", -1));
                        }
                    }
                });
                return;
            } else if (GlobalStatus.getChatRoomMsg() != null && (GlobalStatus.equalPhone(linkmanPhone) || GlobalStatus.equalTeamID(group))) {
                call_hungon = 1;
                roomId = GlobalStatus.getRoomID();
                r = GlobalStatus.getChatRoomMsg();
                notifyCallSuccess();
            } else {
                closeRoom(true);
            }
        } else if (callType == 2) {
            ProtoMessage.StartVoiceMsg.Builder builder = ProtoMessage.StartVoiceMsg.newBuilder();
            if (single) {
                builder.setToUserPhone(linkmanPhone);
            } else {
                builder.setTeamID(group);
            }
            GlobalStatus.setChatRoomtempId(0);
            GlobalStatus.setIsStartRooming(true);
            MyService.start(FirstActivity.this, ProtoMessage.Cmd.cmdStartVoice.getNumber(), builder.build());
            IntentFilter filter = new IntentFilter();
            filter.addAction(VoiceStartProcesser.ACTION);
            Log.i("pocdemo", "start voice action, want room id...");
            final TimeoutBroadcast b = new TimeoutBroadcast(FirstActivity.this, filter, getBroadcastManager());

            b.startReceiver(10, new ITimeoutBroadcast() {
                @Override
                public void onTimeout() {
                    // stop&start thread after startVoice failed
                    sendBroadcastAsUser(new Intent("com.erobbing.ACTION_STOP_THREAD"), UserHandle.ALL);
                    ToastR.setToast(FirstActivity.this, "发起呼叫失败");
                    closeRoom(true);
                    Log.v(FirstActivity.class.getSimpleName(), "startVoice timeout");
                    sendBroadcastAsUser(new Intent("com.erobbing.ACTION_START_THREAD"), UserHandle.ALL);
                }

                @Override
                public void onGot(Intent i) {
                    Log.v("pocdemo", "VoiceStartProcesser onGot");
                    if (i.getIntExtra("error_code", -1) ==
                            ProtoMessage.ErrorCode.OK.getNumber()) {
                        //ProtoMessage.ChatRoomMsg resp = (ProtoMessage.ChatRoomMsg) i.getSerializableExtra("member");
                        //List<ProtoMessage.ChatRoomMemberMsg> list = resp.getMembersList();
                        call_hungon = 1;
                        Log.v("pocdemo", "call_hungon:" + call_hungon);
                        roomId = i.getLongExtra("room_id", -1);
                        r = GlobalStatus.getChatRoomMsg();
//                        GlobalStatus.clearChatRoomMsg();
                        notifyCallSuccess();
                    } else {
                        ToastR.setToast(FirstActivity.this, "呼叫失败");
                        closeRoom(true);
                        fail(i.getIntExtra("error_code", -1));
                    }
                }
            });
        } else {
            closeRoom(true);
        }
    }

    public void notifyCallSuccess() {
        if (r.getPhoneNum().equals(myPhone)) {
            hostOk = true;
        } else {
            hostOk = false;
        }
        Log.v("wsDvr", "GlobalStatus.isPttBroadCast():" + GlobalStatus.isPttBroadCast());
        Log.v("wsDvr", "GlobalStatus.isPttKeyDown():" + GlobalStatus.isPttKeyDown());
        if (GlobalStatus.isPttBroadCast() && !GlobalStatus.isPttKeyDown()) {
            Intent service = new Intent(this, MyService.class);
            service.putExtra("ptt_key_action", true);
            startService(service);
        }

//        if(NotifyManager.getInstance().)

        if (single) {
            if (!GlobalStatus.equalPhone(linkmanPhone)) {
                saveMsgRemind("房间被成功呼起");
                //TTs.play("当前对讲对象，"+linkman);
                msgDB_Single();
            }
//            GlobalStatus.setChatRoomMsg(r, linkmanPhone);
//                        videoLayout.showSingle(FirstActivity.this, linkmanPhone);
            videoLayout.showVideo(FirstActivity.this);

            int status;
            if (r.getMembersList().get(0).getPhoneNum().equals(myPhone)) {
                status = r.getMembersList().get(1).getStatus();
                changespeaking(r.getMembersList().get(1).getStatus(), linkmanPhone);
            } else {
                status = r.getMembersList().get(0).getStatus();
                changespeaking(r.getMembersList().get(0).getStatus(), linkmanPhone);
            }
            if (status == ProtoMessage.ChatStatus.csOk_VALUE ||
                    status == ProtoMessage.ChatStatus.csSpeaking_VALUE) {
                nowMansNum = 1;
            } else {
                nowMansNum = 0;
            }
            name.setText(linkman);
            sta_num.setVisibility(VISIBLE);
            sta_num.setText(online[nowMansNum]);
        } else {
            if (!GlobalStatus.equalTeamID(group)) {
                saveMsgRemind("房间被成功呼起");
                //TTs.play("当前对讲群组，"+groupName);
                msgDB_Team();
            }
//            GlobalStatus.setChatRoomMsg(r);
            videoLayout.showVideo(FirstActivity.this);
            //TODO: allTeamMemberInfos 可能为空
            if (allTeamMemberInfos != null) {
                convertViewTeamMember(allTeamMemberInfos);
            }
        }
//        if (single) {
//            ToastR.setToast(mContext,"当前对讲联系人：" + linkman);
//        } else {
//            ToastR.setToast(mContext,"当前对讲群组：" + groupName);
//        }
        GlobalStatus.setIsStartRooming(false);
        GlobalStatus.setIsAcceptRooming(false);
        GlobalStatus.setChatRoomtempId(0);
        ChatManager.getInstance().setShow(true);
        bottomLayoutManager.setPttEnable(true);
        if (!isFinishing() && !pauseIs) {
            ChatManager.getInstance().showChatInfo(videoLayout);
        }
        if (bottomLayoutManager != null) {
            //   bottomLayoutManager.show(false);
        }
        hangup.setVisibility(VISIBLE);
        call.setVisibility(GONE);
        button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.FloatingActionButton_up)));
        button.setImageResource(R.drawable.speak_call);
        if (edit.length() == 0) {
                        /*button.setBackgroundResource(R.drawable.btn_speaker);
                        buttonChange(button, 200);*/
            sentType = 0;
        } else {
                        /*button.setBackgroundResource(R.drawable.btn_sent);
                        buttonChange(button, 200);*/
            sentType = 1;
        }
//        mMap.refreshMapLocalData(userList, call_hungon == 1);
        NotificationManager nm = (NotificationManager) (getSystemService(NOTIFICATION_SERVICE));
        nm.cancel(-1);//消除对应ID的通知
        GlobalStatus.setFirstCreating(false);
    }

    //*********************************文本框监听**********************************
    TextWatcher textChange = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
            if (edit.length() == 0) {
                /*if (call_hungon == 0) {
                    button.setBackgroundResource(R.drawable.speaker_3);
                } else {
                    button.setBackgroundResource(R.drawable.btn_speaker);
                }
                buttonChange(button, 200);*/
                sentType = 0;
            } else {
                if (sentType != 1) {
                    /*button.setBackgroundResource(R.drawable.btn_sent);
                    buttonChange(button, 200);*/
                    sentType = 1;
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }
    };

    //******************得到剪贴板管理器（复制）**************************************
    @SuppressWarnings("deprecation")
    public static void copy(String content, Context context) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
    }

    public static void deletePic(Context context, Cursor cursor) {
        if (cursor.moveToFirst()) {
            String picAddress = getMyDataRoot(context);

            String filePath = cursor.getString(cursor.getColumnIndex("pic_address"));
            if (TextUtils.isEmpty(filePath)) {
                return;
            }
            try {
                File file = new File(picAddress + filePath);
                if (file != null && file.exists()) {
                    file.delete();
                    Log.i("chatjrd", "删除成功：" + file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getLinkmanPhone() {
        return linkmanPhone;
    }

    public ListView getList() {
        return list;
    }

    private void getlistMembersCache() {
        //获取好友列表
        try {
            DBManagerFriendsList db = new DBManagerFriendsList(FirstActivity.this, DBTableName.getTableName(mContext, DBHelperFriendsList.NAME));
            listMembersCache = db.getFriends(false);
            db.closeDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver refreshTeamReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Long teamid = intent.getLongExtra("teamid", 0);
            if (intent.hasExtra("singout") && (teamid == group)) {
                showNotification = false;
                finish();
            }
        }
    };

    private BroadcastReceiver updateViewReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO 蓝牙广播
//            pttKeyDownSent();
            if (intent.getAction().equalsIgnoreCase(BluetoothMonitor.UPDATE_CHAT_VIEW)) {
                boolean keyDown = intent.getBooleanExtra("keyDown", false);
                Log.i(BluetoothMonitor.TAG, "bluetooth: " + keyDown);
                button.setEnabled(keyDown ? false : true);
            } else if (intent.getAction().equalsIgnoreCase(BottomLayoutManager.ACTION_VIDEO_CONTROL_SHOW) && bottomLayoutManager != null && !pauseIs) {
                // bottomLayoutManager.show(true);
            } else if (intent.getAction().equalsIgnoreCase(RESClient.ACTION_VIDEO_VOIDE_SWITCH) && bottomLayoutManager != null) {
                boolean isVideo = intent.getBooleanExtra("isVideo", false);
                if (!GlobalStatus.IsRandomChat()) {
                    bottomLayoutManager.switchVoiceOrVideo(isVideo);
                }
                bottomLayoutManager.updateChatModeSelection();
            } else if (intent.getAction().equalsIgnoreCase(RESClient.ACTION_VIDEO_VOIDE_UPDATE) && bottomLayoutManager != null) {
                //bottomLayoutManager.updateSwitchText();
                bottomLayoutManager.updateChatModeSelection();
            }
//            //按下操作
//            if (keyDown) {
//                if (record == 0) {
//                    pttKeyDownSent();
//                }
//            } else {
//                //抬起操作
//                btnUp();
//            }


        }
    };


    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String deletePhone = intent.getStringExtra("phone");
            String str = intent.getStringExtra("delete");
            if (!single) {
                if (deletePhone != null && deletePhone.length() > 0) {
                    //                    ToastR.setToast(mContext,"你");
                    //                finish();
                    getlistMembersCache();
                    if (allTeamMemberInfos != null) {
                        convertViewTeamMember(allTeamMemberInfos);
                    }
                }
            } else if (deletePhone != null && str != null && str.length() > 0 && deletePhone.equals(linkmanPhone)) {
                showNotification = false;
                finish();
            }
        }
    };

    public void DeleteMsg(long sn) {

        Log.i("jrdchat", "delete msg sn: " + sn);
        if (single) {
            linkmanRecordHelper = new LinkmanRecordHelper(FirstActivity.this, myPhone + linkmanPhone + "LinkmanMsgShow.dp", null);
            dbMsg = linkmanRecordHelper.getWritableDatabase();
            dbMsg.delete("LinkmanRecord", "id = ?", new String[]{sn + ""});
            dbMsg.close();
        } else {
            teamRecordHelper = new TeamRecordHelper(FirstActivity.this, myPhone + group + "TeamMsgShow.dp", null);
            dbMsg = teamRecordHelper.getWritableDatabase();
            dbMsg.delete("TeamRecord", "id = ?", new String[]{sn + ""});
            dbMsg.close();
        }
    }

    //添加 @ + name
    public void editAddText(String phone) {
        int index = edit.getSelectionStart();
        Editable editable = edit.getText();
        String name = getName(phone);
        editable.insert(index, name);
    }

    private String getName(String phone) {
        String name = "";
        if (single) {
            name = "@" + linkman;
        } else {
            for (TeamMemberInfo user : allTeamMemberInfos) {
                if (user.getUserPhone().equals(phone)) {
                    if (user.getNickName() == null || user.getNickName().equals("")) {
                        name = "@" + user.getUserName();
                    } else {
                        name = "@" + user.getNickName();
                    }
                    break;
                }
            }
        }
        return name;
    }

    private void msgDB_Single() {
        Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
        int yearNow = c.get(Calendar.YEAR);
        int monthNow = c.get(Calendar.MONTH);
        int dateNow = c.get(Calendar.DATE);
        String time;
        linkmanRecordHelper = new LinkmanRecordHelper(FirstActivity.this, myPhone + linkmanPhone + "LinkmanMsgShow.dp", null);
        dbMsg = linkmanRecordHelper.getWritableDatabase();
        Cursor cursor = dbMsg.query("LinkmanRecord", null, null, null, null, null, null);
        msgList.clear();
        int i = 0;
        if (cursor.moveToLast()) {
            do {
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
                int msgType = cursor.getInt(cursor.getColumnIndex("msg_type"));
                int msgSend = cursor.getInt(cursor.getColumnIndex("msg_send"));
                int sentState = cursor.getInt(cursor.getColumnIndex("send_state"));
                long msgID = cursor.getLong(cursor.getColumnIndex("service_id"));
                //Log.i("chatjrd","返回的——msgId:"+msgID+"        id:"+cursor.getInt(cursor.getColumnIndex("id")));
                Msg msg;
                if (msgType == 1) {
                    String picAddress = getMyDataRoot(FirstActivity.this) + cursor.getString(cursor.getColumnIndex("pic_address"));
                    Bitmap bmp = BitmapFactory.decodeFile(picAddress);
                    if (msgSend == 0) {
                        msg = new Msg(bmp, Msg.TYPE_SENT_IMAGE, time, "", sentState, cursor.getInt(cursor.getColumnIndex("id")), myPhone, msgID, 0);
                        msg.setPercent(cursor.getInt(cursor.getColumnIndex("percent")));
                    } else {
                        msg = new Msg(bmp, Msg.TYPE_RECEIVED_IMAGE, time, "", sentState, cursor.getInt(cursor.getColumnIndex("id")), linkmanPhone, msgID, 0);
                    }
                    msg.setAddress(cursor.getString(cursor.getColumnIndex("pic_address_true")));
                } else if (msgType == 2) {
                    String text = cursor.getString(cursor.getColumnIndex("msg"));
                    msg = new Msg(text, Msg.TYPE_MSG_RECORD, time, 0, 0, linkmanPhone, msgID, 0);
                } else if (msgType == 3) {
                    if (msgSend == 0) {
                        msg = new Msg("", Msg.TYPE_MSG_MY_CANCEL, time, 0, 0, myPhone, msgID, 0);
                    } else {
                        msg = new Msg("", Msg.TYPE_MSG_CANCEL, time, 0, 0, linkmanPhone, msgID, 0);
                    }
                } else if (msgType == ProtoMessage.MsgType.mtVideoFile_VALUE) {
                    String picAddress = getMyDataRoot(FirstActivity.this) + cursor.getString(cursor.getColumnIndex("pic_address"));
                    Bitmap bmp = BitmapFactory.decodeFile(picAddress);
                    if (msgSend == 0) {
                        msg = new Msg(bmp, Msg.TYPE_SENT_VIDEO, time, "", sentState, cursor.getInt(cursor.getColumnIndex("id")), myPhone, msgID, 0);
                        msg.setPercent(cursor.getInt(cursor.getColumnIndex("percent")));
                    } else {
                        msg = new Msg(bmp, Msg.TYPE_RECEIVED_VIDEO, time, "", sentState, cursor.getInt(cursor.getColumnIndex("id")), linkmanPhone, msgID, 0);
                    }
                    msg.setAddress(cursor.getString(cursor.getColumnIndex("pic_address_true")));
                } else {
                    String text = cursor.getString(cursor.getColumnIndex("msg"));
                    if (msgSend == 0) {
                        msg = new Msg(text, Msg.TYPE_SENT, time, sentState, cursor.getInt(cursor.getColumnIndex("id")), myPhone, msgID, 0);
                    } else {
                        msg = new Msg(text, Msg.TYPE_RECEIVED, time, sentState, cursor.getInt(cursor.getColumnIndex("id")), linkmanPhone, msgID, 0);
                    }
                }
                msgList.add(msg);
                i++;
                if (i > 50) {
                    break;
                }
            } while (cursor.moveToPrevious());
            Collections.reverse(msgList);//倒序

        }

        adapter.notifyDataSetChanged();
        list.setSelection(msgList.size());
        cursor.close();
        dbMsg.close();
    }

    private void msgDB_Team() {
        Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
        int yearNow = c.get(Calendar.YEAR);
        int monthNow = c.get(Calendar.MONTH);
        int dateNow = c.get(Calendar.DATE);
        String time;
        teamRecordHelper = new TeamRecordHelper(FirstActivity.this, myPhone + group + "TeamMsgShow.dp", null);
        dbMsg = teamRecordHelper.getWritableDatabase();
        Cursor cursor = dbMsg.query("TeamRecord", null, null, null, null, null, null);
        msgList.clear();
        int i = 0;
        if (cursor.moveToLast()) {
            do {
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
                int msgType = cursor.getInt(cursor.getColumnIndex("msg_type"));
                int msgSend = cursor.getInt(cursor.getColumnIndex("msg_send"));
                int sentState = cursor.getInt(cursor.getColumnIndex("send_state"));
                long msgID = cursor.getLong(cursor.getColumnIndex("service_id"));
                //Log.i("chatjrd","返回的——msgId:"+msgID+"        id:"+cursor.getInt(cursor.getColumnIndex("id")));
                String fromPhone = cursor.getString(cursor.getColumnIndex("phone"));
                Msg msg;
                if (msgType == 1) {
                    String picAddress = getMyDataRoot(FirstActivity.this) + cursor.getString(cursor.getColumnIndex("pic_address"));
                    Bitmap bmp = BitmapFactory.decodeFile(picAddress);
                    if (msgSend == 0) {
                        msg = new Msg(bmp, Msg.TYPE_SENT_IMAGE, time, "", sentState, cursor.getInt(cursor.getColumnIndex("id")), fromPhone, msgID, group);
                        msg.setPercent(cursor.getInt(cursor.getColumnIndex("percent")));
                    } else {
                        msg = new Msg(bmp, Msg.TYPE_RECEIVED_IMAGE, time, "", sentState, cursor.getInt(cursor.getColumnIndex("id")), fromPhone, msgID, group);
                    }
                    msg.setAddress(cursor.getString(cursor.getColumnIndex("pic_address_true")));
                } else if (msgType == 2) {
                    String text = cursor.getString(cursor.getColumnIndex("msg"));
                    msg = new Msg(text, Msg.TYPE_MSG_RECORD, time, 0, 0, fromPhone, msgID, group);
                } else if (msgType == 3) {
                    if (msgSend == 0) {
                        msg = new Msg("", Msg.TYPE_MSG_MY_CANCEL, time, 0, 0, fromPhone, msgID, group);
                    } else {
                        msg = new Msg("", Msg.TYPE_MSG_CANCEL, time, 0, 0, fromPhone, msgID, group);
                    }
                } else if (msgType == ProtoMessage.MsgType.mtVideoFile_VALUE) {
                    String picAddress = getMyDataRoot(FirstActivity.this) + cursor.getString(cursor.getColumnIndex("pic_address"));
                    Bitmap bmp = BitmapFactory.decodeFile(picAddress);
                    if (msgSend == 0) {
                        msg = new Msg(bmp, Msg.TYPE_SENT_VIDEO, time, "", sentState, cursor.getInt(cursor.getColumnIndex("id")), fromPhone, msgID, group);
                        msg.setPercent(cursor.getInt(cursor.getColumnIndex("percent")));
                    } else {
                        msg = new Msg(bmp, Msg.TYPE_RECEIVED_VIDEO, time, "", sentState, cursor.getInt(cursor.getColumnIndex("id")), fromPhone, msgID, group);
                    }
                    msg.setAddress(cursor.getString(cursor.getColumnIndex("pic_address_true")));
                } else {
                    String text = cursor.getString(cursor.getColumnIndex("msg"));
                    if (msgSend == 0) {
                        msg = new Msg(text, Msg.TYPE_SENT, time, sentState, cursor.getInt(cursor.getColumnIndex("id")), fromPhone, msgID, group);
                    } else {
                        msg = new Msg(text, Msg.TYPE_RECEIVED, time, sentState, cursor.getInt(cursor.getColumnIndex("id")), fromPhone, msgID, group);
                    }
                }
                msgList.add(msg);
                i++;
                if (i > 50) {
                    break;
                }
            } while (cursor.moveToPrevious());
            Collections.reverse(msgList);//倒序

        }
        adapter.notifyDataSetChanged();
        list.setSelection(msgList.size());
        cursor.close();
        dbMsg.close();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("pocdemo", " first activity stop");
        int[] rect = new int[4];
        DvrConfig.getThumbnailViewRect(rect);
        DvrService.updatePlayView(mContext, rect[0], rect[1], rect[2], rect[3]);
        DvrService.updateRtmpView(mContext, rect[0], rect[1], rect[2], rect[3]);
        if (bottomLayoutManager != null) {
            bottomLayoutManager.removeView();
        }
    }

    @Override
    public void finish() {
        Log.i("pocdemo", " first activity finish");
        ChatManager.getInstance().setIsFinishing(true);
        ChatManager.getInstance().hideView();
        if (bottomLayoutManager != null) {
            bottomLayoutManager.removeView();
        }
        super.finish();
    }

    @Override
    protected void onDestroy() {
        Log.i("pocdemo", "first activity destroyed");
        GlobalStatus.setTempChat("");
        SharedPreferencesUtils.put(this, NotifyProcesser.Call_KEY, "");
        if (getMsgReceiiver != null) {
            unregisterReceiver(getMsgReceiiver);
        }
        if (closeRoomReceiiver != null) {
            unregisterReceiver(closeRoomReceiiver);
        }

        if (refreshReceiiver != null) {
            unregisterReceiver(refreshReceiiver);
        }
        try {
            if (refreshTeamReceiver != null) {
                unregisterReceiver(refreshTeamReceiver);
            }

            if (updateViewReceiver != null) {
                unregisterReceiver(updateViewReceiver);
            }

            if (myReceiver != null) {
                unregisterReceiver(myReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (uploadReceiiver != null) {
            unregisterReceiver(uploadReceiiver);
        }
        SharedPreferencesUtils.put(this, ReceiverProcesser.UPDATE_KEY, "");
        clearUnfinishedUploadImage();
        chatCallReceiver = null;
        if (mapInitIs) {
//            mMap.onDestroy();
        }
        videoLayout.hideVideo();
        if (bottomLayoutManager != null) {
            bottomLayoutManager.removeView();
        }

        if (ChatManager.getInstance().isIsFinishing()) {
            ChatManager.getInstance().hideView();
        }
        GlobalStatus.setFirstCreating(false);

        if (isBBS){
            // 如果是海聊群，退出群
            groupQuit();
        }
        super.onDestroy();

    }

    //***********************设置GridView显示*******************************
    public void setGridView() {
        memberListView = (ListView) findViewById(R.id.member_list);
        adapterU = new UserAdapter(FirstActivity.this, R.layout.singleuser, userList);
        //initGridView();//初始化GridView
        memberListView.setAdapter(adapterU);
        setGridViewClick(false);
    }

    private void setGridViewClick(final boolean check) {
        memberListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                if (!single) {
                    if (check) {
                        String str = userList.get(position).getPhone();
                        if (str == null || str.length() <= 0) {
                            copy(str, FirstActivity.this);
                            onItemType(userList.get(position).getName());
                        } else {
                            onItemUserPicture(position);
                        }
                    }
                } else {
//					linkmanPhone
                    showLinkManPhone();
                }
            }
        });
        if (check) {
            memberListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                    if (position == (userList.size() - 1)) {
                        return true;
                    }
                    phone = userList.get(position).getPhone();
                    return false;
                }
            });
            memberListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

                public void onCreateContextMenu(ContextMenu menu, View v,
                                                ContextMenu.ContextMenuInfo menuInfo) {
                    menu.setHeaderIcon(R.color.white);
                    menu.add(0, 10, 0, getName(phone));
                    //info.id得到listview中选择的条目绑定的id
//				menu.add(0, 4, 0, "信息查询");
                    if (mTMInfo.size() > 0) {
                        for (TeamMemberInfo te : mTMInfo) {
                            if (te.getUserPhone().equals(phone)) {
                                menu.add(0, 3, 0, "设置备注");
                                break;
                            }
                        }
                    }
                    if (myPhone.equals(phone)) {
                        menu.add(0, 3, 0, "设置备注");
                    }

                    if (type != ProtoMessage.TeamRole.memberOnly_VALUE) {
                        menu.add(0, 2, 0, "设置话权");
                    }
                    if (type != ProtoMessage.TeamRole.memberOnly_VALUE && !(myPhone.equals(phone))) {
                        menu.add(0, 0, 0, "设置管理员");
                        menu.add(0, 1, 0, "删除");
                    }
                    if (myPhone.equals(phone) && type != ProtoMessage.TeamRole.Owner_VALUE) {
//                        menu.add(0, 5, 0, "退出该群");
                    }

                    for (TeamMemberInfo te : mTMInfoNew) {
                        if (te.getUserPhone().equals(phone) && !myPhone.equals(phone)) {
                            menu.add(0, 4, 0, "添加好友");
                            break;
                        }
                    }
                }

            });
        }
    }

    public void showLinkManPhone() {
        if (listMembersCache == null || listMembersCache.size() <= 0) {
            return;
        }
        for (AppliedFriends vm : listMembersCache) {
            if (linkmanPhone.equals(vm.getPhoneNum())) {
                mViewFriendsMsg = vm;
                break;
            }
        }
        Intent intent = new Intent(mContext, FriendsDetailsActivity.class);
        Bundle bundle = new Bundle();
        mViewFriendsMsg.setNickName(linkman);
        bundle.putParcelable("friend_detail", mViewFriendsMsg);
        intent.putExtra("type", REFRESH_REMARK);
        intent.putExtras(bundle);
        startActivityForResult(intent, REFRESH_REMARK);
    }

    private void showSetFriendInfoDialog(final AppliedFriends af, final String type, final User user) {
        String str = "";
        String name = "";
        if (type.equals(changeType)) {
            str = "好友备注修改";
            name = user.getName();
        } else {
            str = "用户名修改";
            name = af.getNickName();
        }
        LayoutInflater factory = LayoutInflater.from(mContext);// 提示框
        final View view = factory.inflate(R.layout.friend_request_editbox_layout, null);// 这里必须是final的
        final TextView remark = (TextView) view.findViewById(R.id.tv_remark_name);
        final EditText editRemark = (EditText) view.findViewById(R.id.remark_editText);// 获得输入框对象
        final RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.rl_msg);
        rl.setVisibility(GONE);
        remark.setText("用户名");
        editRemark.setText(name);
        editRemark.setSelection(editRemark.length());// 将光标追踪到内容的最后

        PromptDialog dialog = new PromptDialog(mContext);
        dialog.show();
        dialog.setTitle(str);
        dialog.setView(view);
        dialog.setOkListener("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String remark = editRemark.getText().toString().trim();
                setFriendInfo(af, type, remark);
                dialog.dismiss();
            }
        });
        dialog.setCancelListener("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

      /*  new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT).setTitle(str)// 提示框标题
                .setView(view).setPositiveButton("确定", // 提示框的两个按钮
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String remark = editRemark.getText().toString().trim();
                        setFriendInfo(af, type, remark);
                        dialog.dismiss();
                    }

                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                }).create().show();*/
    }

    private void onChangeNickName() {
        SharedPreferencesUtils.put(this, "friend_list_changed", true);
    }

    /*
    设置好友信息
     */
    public void setFriendInfo(final AppliedFriends af, final String str, final String remark) {

        if (remark.length() <= 1) {
            ToastR.setToast(mContext, "输入数字必须是两位或者两位以上");
            return;
        }
        ProtoMessage.UserInfo.Builder builder = ProtoMessage.UserInfo.newBuilder();
        builder.setNickName(af.getNickName());
        builder.setPhoneNum(af.getPhoneNum());
        MyService.start(mContext, ProtoMessage.Cmd.cmdSetFriendInfo.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(SetFriendInfoProcesser.ACTION);
        new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    try {
                        af.setNickName(remark);
                        DBManagerFriendsList db = new DBManagerFriendsList(mContext, DBTableName.getTableName(mContext, DBHelperFriendsList.NAME));
                        db.updateFriendNickName(af);
                        db.closeDB();
                        updateLocalData(af, str);

                        onChangeNickName();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void updateLocalData(final AppliedFriends af, String str) {
        if (!(str.equals(changeType))) {
            linkman = af.getNickName();
            name.setText(linkman);
            sta_num.setVisibility(GONE);
            userList.get(0).setName(af.getNickName());
        } else {
            for (User user : userList) {
                if (af.getPhoneNum().equals(user.getPhone())) {
                    user.setName(af.getNickName());
                    break;
                }
            }
        }
        rankList(1);
    }

    /*
     地图上显示成员信息
     */
    public void showMapMemberMsg(String phone) {
        TeamMemberInfo infos = null;
        for (TeamMemberInfo tm : allTeamMemberInfos) {
            if (tm.getUserPhone().equals(phone)) {
                infos = tm;
                break;
            }
        }
        if (infos != null && infos.getUserPhone().length() > 0) {
            showInformationDilog(infos);
        }
    }

    private void onItemUserPicture(int position) {
        TeamMemberInfo infos = null;
        String phone = userList.get(position).getPhone();
        String nickName = userList.get(position).getName();
        for (TeamMemberInfo tm : allTeamMemberInfos) {
            if (tm.getUserPhone().equals(phone)) {
                infos = tm;
                infos.setNickName(nickName);
                break;
            }
        }
        if (infos != null && infos.getUserPhone().length() > 0) {
            showInformationDilog(infos);
        }
    }

    //判断点击类型
    private void onItemType(String str) {
        if (str.equals(User.ADD)) {
//			if (type == ProtoMessage.TeamRole.memberOnly_VALUE){
//				ToastR.setToast(FirstActivity.this,"你不是管理员，没有权限邀请好友");
//				return;
//			}

            //获取好友列表
//			DBManagerFriendsList db = new DBManagerFriendsList(FirstActivity.this);
//			List<AppliedFriends> list = db.getFriends();
//			db.closeDB();

            AppliedFriendsList afList = new AppliedFriendsList();
            List<AppliedFriends> appliedList = new ArrayList<AppliedFriends>();
            for (AppliedFriends info : listMembersCache) {
                int t = 0;
                for (int i = 0; i < allTeamMemberInfos.size(); i++) {
                    if (!allTeamMemberInfos.get(i).getUserPhone().equals(info.getPhoneNum())) {
                        ++t;
                        if (t == allTeamMemberInfos.size()) {
                            appliedList.add(info);
                        }
                    } else {
                        break;
                    }
                }
            }
            if (appliedList.size() <= 0) {
                ToastR.setToast(getApplicationContext(), "请添加新的好友再来邀请好友");
            } else {
                afList.setAppliedFriends(appliedList);
                sendIntentMsg(afList);
            }
        }
    }

    private void sendIntentMsg(AppliedFriendsList afList) {

        Intent i = new Intent(FirstActivity.this, InviteJoinGroupActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("apply_member", afList);
        i.putExtras(bundle);
        i.putExtra("teamID", group);
        FirstActivity.this.startActivity(i);
        //finish();
    }

    public void showsinfo(String phoneTemp) {
        if (single) {
            showFriendMsg(phoneTemp);
        } else {
            for (TeamMemberInfo tm : allTeamMemberInfos) {
                if (tm.getUserPhone().equals(phoneTemp)) {
                    showInformationDilog(tm);
                    break;
                }
            }
        }
    }

    private void showFriendMsg(String phone) {

    }

    //GridView弹出菜单
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        //info.id得到listview中选择的条目绑定的id
        //String id = String.valueOf(info.id);
        if (item.getItemId() < 6) {
            String phone = userList.get((int) info.id).getPhone();
            for (TeamMemberInfo tm : allTeamMemberInfos) {
                if (tm.getUserPhone().equals(phone)) {
                    mTeamMemberInfo = tm;
                    break;
                }
            }
        }
        switch (item.getItemId()) {
            case 0:
                if (mTeamMemberInfo.getRole() == ProtoMessage.TeamRole.Owner_VALUE) {
                    ToastR.setToast(FirstActivity.this, "不能修改群主的权限");
                } else if (type == ProtoMessage.TeamRole.Manager_VALUE &&
                        mTeamMemberInfo.getRole() == ProtoMessage.TeamRole.Manager_VALUE) {
                    ToastR.setToast(FirstActivity.this, "管理员不能对管理员进行权限设置");
                } else {
                    setPermissions();
                }
                //	ToastR.setToast(FirstActivity.this, "设置 " + userList.get((int)info.id).getName() + " 的权限");
                return true;
            case 1:
                if (mTeamMemberInfo.getRole() == ProtoMessage.TeamRole.Owner_VALUE) {
                    ToastR.setToast(FirstActivity.this, "不能删除群主");
                } else if (type == ProtoMessage.TeamRole.Manager_VALUE &&
                        mTeamMemberInfo.getRole() == ProtoMessage.TeamRole.Manager_VALUE) {
                    ToastR.setToast(FirstActivity.this, "管理员不能删除管理员");
                } else {
                    showDeleteDialog(mTeamMemberInfo);
                    //	ToastR.setToast(FirstActivity.this, "把 " + userList.get((int) info.id).getName() + " 踢出该讨论组");
                }
                return true;
            case 2:
                showChangePriorityDilog(mTeamMemberInfo);
//					}
                //	ToastR.setToast(FirstActivity.this, "修改成员优先级");
                return true;
            case 3:
                showChangeMemberRemarkDilog(mTeamMemberInfo);
                return true;
            case 4:
                toAddLinkMan(mTeamMemberInfo);
                return true;
            case 5:
                groupQuit();
                return true;
            case 7:
                DeleteMsgSQL((int) info.id);
                return true;
            case 6:
                String str = msgList.get((int) info.id).getContent();
                if (str == null || str.equals("")) {
                    ToastR.setToast(getApplicationContext(), "不能复制图片");
                } else {
                    copy(str, FirstActivity.this);
                    ToastR.setToast(getApplicationContext(), "已将“" + str + "”复制到剪切板");
                }
                return true;
            case 8:
                CancelMsg(msgList.get((int) info.id).getSn(), (int) info.id);
                return true;
            case 10:
                editAddText(userList.get((int) info.id).getPhone());
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void CancelMsg(long sn, int position) {
        /*      */
        ProtoMessage.CommonMsg.Builder builder = ProtoMessage.CommonMsg.newBuilder();
        long id = -1;
        if (single) {
            linkmanRecordHelper = new LinkmanRecordHelper(FirstActivity.this, myPhone + linkmanPhone + "LinkmanMsgShow.dp", null);
            dbMsg = linkmanRecordHelper.getWritableDatabase();
            Cursor cursor = dbMsg.query("LinkmanRecord", null, "id = ?", new String[]{sn + ""}, null, null, null);
            if (cursor.moveToFirst()) {
                id = cursor.getLong(cursor.getColumnIndex("service_id"));
            }
            builder.setToUserPhone(linkmanPhone);
        } else {
            teamRecordHelper = new TeamRecordHelper(FirstActivity.this, myPhone + group + "TeamMsgShow.dp", null);
            dbMsg = teamRecordHelper.getWritableDatabase();
            Cursor cursor = dbMsg.query("TeamRecord", null, "id = ?", new String[]{sn + ""}, null, null, null);
            if (cursor.moveToFirst()) {
                id = cursor.getLong(cursor.getColumnIndex("service_id"));
            }
            builder.setToTeamID(group);
        }
        builder.setMsgSN(sn);
        builder.setMsgContent(ByteString.copyFromUtf8(id + ""));
        builder.setMsgType(ProtoMessage.MsgType.mtCancel_VALUE);
        MyService.start(FirstActivity.this, ProtoMessage.Cmd.cmdCommonMsg.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(SendMsgProcesser.ACTION);
        final long SN = sn;
        new TimeoutBroadcast(FirstActivity.this, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(FirstActivity.this, "撤回失败");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    Log.i("chatjrd", "撤回成功");
                    ContentValues values = new ContentValues();
                    values.put("msg_type", ProtoMessage.MsgType.mtCancel_VALUE);
                    values.put("msg", "");
                    MsgRecordHelper msgRecordHelper = new MsgRecordHelper(FirstActivity.this, myPhone + "MsgShow.dp", null);
                    SQLiteDatabase db = msgRecordHelper.getWritableDatabase();
                    ContentValues values_msg = new ContentValues();
                    values_msg.put("msg", "撤回了一条消息");
                    Cursor cursor;
                    if (single) {
                        linkmanRecordHelper = new LinkmanRecordHelper(FirstActivity.this, myPhone + linkmanPhone + "LinkmanMsgShow.dp", null);
                        dbMsg = linkmanRecordHelper.getWritableDatabase();
                        dbMsg.update("LinkmanRecord", values, "id = ?", new String[]{SN + ""});
                        cursor = dbMsg.query("LinkmanRecord", null, "id = ?", new String[]{SN + ""}, null, null, null);
                        deletePic(FirstActivity.this, cursor);
                        cursor.close();
                        dbMsg.close();
                        db.update("Msg", values_msg, "phone = ? and msg_from = ?", new String[]{phone, 0 + ""});
                        db.close();
                        msgDB_Single();
                    } else {
                        teamRecordHelper = new TeamRecordHelper(FirstActivity.this, myPhone + group + "TeamMsgShow.dp", null);
                        dbMsg = teamRecordHelper.getWritableDatabase();
                        dbMsg.update("TeamRecord", values, "id = ?", new String[]{SN + ""});
                        cursor = dbMsg.query("TeamRecord", null, "id = ?", new String[]{SN + ""}, null, null, null);
                        deletePic(FirstActivity.this, cursor);
                        cursor.close();
                        dbMsg.close();
                        db.update("Msg", values_msg, "group_id = ?", new String[]{group + ""});
                        db.close();
                        msgDB_Team();
                    }


                } else {
                    ToastR.setToast(FirstActivity.this, "撤回失败");
                }
            }
        });
    }

    /*
  删除好友
   */
    public void deleteFriend() {
        ProtoMessage.DeleteFriend.Builder builder = ProtoMessage.DeleteFriend.newBuilder();
        builder.setFriendPhoneNum(linkmanPhone);
        MyService.start(mContext, ProtoMessage.Cmd.cmdDeleteFriend.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(DeleteFriendProcesser.ACTION);
        new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    MsgTool.deleteFriendsMsg(mContext, linkmanPhone);
                    ToastR.setToast(mContext, "删除好友成功");
                    finish();
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void groupQuit() {
        ProtoMessage.ApplyTeam.Builder builder = ProtoMessage.ApplyTeam.newBuilder();
        builder.setTeamID(group);
        MyService.start(mContext, ProtoMessage.Cmd.cmdQuitTeam.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(DismissTeamProcesser.ACTION);
        new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
//                    deleteDBitem(group);
                    MsgTool.deleteTeamMsg(mContext, group);
                    if (!isBBS){
                        ToastR.setToast(mContext, "退出群组成功");
                        finish();
                    }
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    /**
     * 解散群组
     */
    private void groupDismiss() {
        ProtoMessage.AcceptTeam.Builder builder = ProtoMessage.AcceptTeam.newBuilder();
        builder.setTeamID(group);
        MyService.start(mContext, ProtoMessage.Cmd.cmdDismissTeam.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(DismissTeamProcesser.ACTION);
        new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    MsgTool.deleteTeamMsg(mContext, group);
                    ToastR.setToast(mContext, "删除群组成功");
                    finish();
                } else {
                    new ResponseErrorProcesser(mContext, i.getIntExtra("error_code", -1));
                }
            }
        });
    }


    private void toAddLinkMan(final TeamMemberInfo mTeamMemberInfo) {
        User user = null;
        for (User us : userList) {
            if (mTeamMemberInfo.getUserPhone().equals(us.getPhone())) {
                user = us;
                break;
            }
        }
        if (user == null) return;

        LayoutInflater factory = LayoutInflater.from(mContext);// 提示框
        final View view = factory.inflate(R.layout.applied_add_linkman, null);// 这里必须是final的
        final TextView userName = (TextView) view.findViewById(R.id.user_name);
        final EditText nickName = (EditText) view.findViewById(R.id.nick_name);
        final EditText etMsg = (EditText) view.findViewById(R.id.et_msg);
        final TextView ok = (TextView) view.findViewById(R.id.ok);
        final TextView cancel = (TextView) view.findViewById(R.id.cancel);
        if (user.getName() != null && !user.getName().equals("")) {
            userName.setText(user.getName());
            nickName.setText(user.getName());
            nickName.setSelection(nickName.length());
        } else {
            userName.setText(noSet);
        }
        SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        String myPhone = preferences.getString("phone", "");
        String userNameMe = preferences.getString("name", myPhone);
        etMsg.setText("我是" + userNameMe + ",请求加您为好友，谢谢。");
        final AlertDialog dialog = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT)// 提示框标题
                .setView(view)
                /* .setPositiveButton("确定", // 提示框的两个按钮
                 new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {

                         String msg = etMsg.getText().toString().trim();
                         String remark = nickName.getText().toString().trim();
                         if (remark.length() <= 0) {
                             ToastR.setToast(mContext, "备注输入不能为空");
                             return;
                         }
                         if (msg.length() <= 0) {
                             ToastR.setToast(mContext, "验证信息输入不能为空");
                             return;
                         }
                         if (remark.length() > GlobalStatus.MAX_TEXT_COUNT) {
                             ToastR.setToast(mContext, "备注输入过长（最大只能设置16个字符）");
                             return;
                         }
                         addFriendsRequest(remark, msg, mTeamMemberInfo.getUserPhone());
                         dialog.dismiss();
                     }

                 })
                 .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.dismiss();

                     }
                 })*/

                .create();


        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = etMsg.getText().toString().trim();
                String remark = nickName.getText().toString().trim();
                if (remark.length() <= 0) {
                    ToastR.setToast(mContext, "备注输入不能为空");
                    return;
                }
                if (msg.length() <= 0) {
                    ToastR.setToast(mContext, "验证信息输入不能为空");
                    return;
                }
                if (remark.length() > GlobalStatus.MAX_TEXT_COUNT) {
                    ToastR.setToast(mContext, "备注输入过长（最大只能设置16个字符）");
                    return;
                }
                addFriendsRequest(remark, msg, mTeamMemberInfo.getUserPhone());
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 加好友网络请求
     *
     * @param remark
     * @param msg    phoneNum
     */
    private void addFriendsRequest(String remark, String msg, String phone) {
        ProtoMessage.ApplyFriend.Builder builder = ProtoMessage.ApplyFriend.newBuilder();
        builder.setFriendPhoneNum(phone);
        builder.setApplyInfo(msg);
        builder.setApplyRemark(remark);
        MyService.start(mContext, ProtoMessage.Cmd.cmdApplyFriend.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(ApplyFriendProcesser.ACTION);
        new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(mContext, "请求成功，等待对方回应");
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void DeletMsgFile(int position) {
        long sn = msgList.get(position).getSn();
        if (single) {
            String rootAddress = getMyDataRoot(FirstActivity.this) + "/" + myPhone + "/" + 0 + "/" + linkmanPhone + "/" + sn;
            String addressTemp = null;
            String address = null;
            if (msgList.get(position).getType() == Msg.TYPE_RECEIVED_IMAGE ||
                    msgList.get(position).getType() == Msg.TYPE_SENT_IMAGE) {
                addressTemp = rootAddress + ".jpg";
                address = rootAddress + "_true" + ".jpg";

            } else if (msgList.get(position).getType() == Msg.TYPE_SENT_VIDEO ||
                    msgList.get(position).getType() == Msg.TYPE_RECEIVED_VIDEO) {
                addressTemp = rootAddress + ".mp4";
                address = rootAddress + "_true" + ".mp4";
            }
            if (address != null) {
                File file = new File(addressTemp);
                if (file.exists()) {
                    file.delete();
                }
                file = new File(address);
                if (file.exists()) {
                    file.delete();
                }
            }
        } else {
            String rootAddress = getMyDataRoot(FirstActivity.this) + "/" + myPhone + "/" +
                    msgList.get(position).getTeamID() + "/" + msgList.get(position).getPhone() + "/" + sn;
            String addressTemp = null;
            String address = null;
            if (msgList.get(position).getType() == Msg.TYPE_RECEIVED_IMAGE ||
                    msgList.get(position).getType() == Msg.TYPE_SENT_IMAGE) {
                addressTemp = rootAddress + ".jpg";
                address = rootAddress + "_true" + ".jpg";
            } else if (msgList.get(position).getType() == Msg.TYPE_SENT_VIDEO ||
                    msgList.get(position).getType() == Msg.TYPE_RECEIVED_VIDEO) {
                addressTemp = rootAddress + ".mp4";
                address = rootAddress + "_true" + ".mp4";
            }
            if (address != null) {
                File file = new File(addressTemp);
                if (file.exists()) {
                    file.delete();
                }
                file = new File(address);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    private void DeleteMsgSQL(int position) {
        long sn = msgList.get(position).getSn();
        DeletMsgFile(position);
        if (single) {
            linkmanRecordHelper = new LinkmanRecordHelper(FirstActivity.this, myPhone + linkmanPhone + "LinkmanMsgShow.dp", null);
            dbMsg = linkmanRecordHelper.getWritableDatabase();
            dbMsg.delete("LinkmanRecord", "id = ?", new String[]{sn + ""});
            dbMsg.close();
            msgDB_Single();
            if (position == msgList.size()) {
                MsgRecordHelper msgRecordHelper = new MsgRecordHelper(FirstActivity.this, myPhone + "MsgShow.dp", null);
                SQLiteDatabase db = msgRecordHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                String msg;
                if (msgList.size() == 0) {
                    db.delete("Msg", "phone = ? and msg_from = ?", new String[]{linkmanPhone, 0 + ""});
                } else {
                    msg = msgList.get(msgList.size() - 1).getContent();
                    if (msg == null || msg.equals("")) {
                        msg = "[图片]";
                    }
                    values.put("msg", msg);
                    db.update("Msg", values, "phone = ? and msg_from = ?", new String[]{linkmanPhone, 0 + ""});
                }
                db.close();
            }
        } else {
            teamRecordHelper = new TeamRecordHelper(FirstActivity.this, myPhone + group + "TeamMsgShow.dp", null);
            dbMsg = teamRecordHelper.getWritableDatabase();
            dbMsg.delete("TeamRecord", "id = ?", new String[]{sn + ""});
            dbMsg.close();
            msgDB_Team();
            if (position == msgList.size()) {
                MsgRecordHelper msgRecordHelper = new MsgRecordHelper(FirstActivity.this, myPhone + "MsgShow.dp", null);
                SQLiteDatabase db = msgRecordHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                String msg;
                if (msgList.size() == 0) {
                    db.delete("Msg", "group_id = ?", new String[]{group + ""});
                } else {
                    msg = msgList.get(msgList.size() - 1).getContent();
                    if (msg == null || msg.equals("")) {
                        msg = "[图片]";
                    }
                    values.put("msg", msg);
                    db.update("Msg", values, "group_id = ?", new String[]{group + ""});
                }
                db.close();
            }
        }
    }

    /*
        显示成员详情信息弹框
     */
    private void showInformationDilog(final TeamMemberInfo teamMemberInfo) {
        //TODO DDDD
        Intent intent = new Intent(mContext, FriendDetailsDialogActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userPhone", teamMemberInfo.getUserPhone());
        bundle.putString("userName", teamMemberInfo.getUserName());
        intent.putExtra("teamID", group);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    //邀请加为好友
    private void inviteFriends(TeamMemberInfo tm) {
        toAddLinkMan(tm);
    }

    /*
    显示修改群名称备注弹框
     */
    private void showChangeMemberRemarkDilog(final TeamMemberInfo tm) {
        if (myPhone.equals(tm.getUserPhone())) {
            //修改自己在群里的备注
            LayoutInflater factory = LayoutInflater.from(FirstActivity.this);// 提示框
            final View view = factory.inflate(R.layout.change_member_nick_name, null);
            final EditText mEditText = (EditText) view.findViewById(R.id.et_change_nick_name);
            String name = tm.getNickName();
            if (name == null || name.equals("")) {
                name = tm.getUserName();
            } else if (tm.getUserName() == null || tm.getUserName().equals("")) {
                name = tm.getUserPhone();
            }
            mEditText.setText(name);
            mEditText.setSelection(mEditText.length());

            new AlertDialog.Builder(FirstActivity.this).setTitle("设置我在群中的名称")// 提示框标题
                    .setView(view).setPositiveButton("确定", // 提示框的两个按钮
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String priority = mEditText.getText().toString().trim();
                            String remark = mEditText.getText().toString().trim();
                            toChangeMemberRemark(remark, tm);
                            dialog.dismiss();
                        }

                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        } else {
            //修改好友在群里的备注
            for (AppliedFriends vm : listMembersCache) {
                if (tm.getUserPhone().equals(vm.getPhoneNum())) {
                    mViewFriendsMsg = vm;
                    break;
                }
            }
            User user = null;
            for (User us : userList) {
                if (tm.getUserPhone().equals(us.getPhone())) {
                    user = us;
                    break;
                }
            }

            showSetFriendInfoDialog(mViewFriendsMsg, changeType, user);
        }
    }

    /*
       修改群名称备注
     */
    private void toChangeMemberRemark(String remark, TeamMemberInfo tm) {
        ProtoMessage.TeamInfo.Builder builder = ProtoMessage.TeamInfo.newBuilder();
        builder.setMyTeamName(remark);
        builder.setTeamID(group);
        MyService.start(FirstActivity.this, ProtoMessage.Cmd.cmdChangeMyTeamName.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(ChangeTeamMemberNickNameProcesser.ACTION);
        new TimeoutBroadcast(FirstActivity.this, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(FirstActivity.this, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(FirstActivity.this, "修改成员名称成功");
                    getGroupMan(group);
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    public void fail(int i) {
        new ResponseErrorProcesser(FirstActivity.this, i);
    }
    //***********************设置GridView显示*******************************

    /*
    显示修改成员优先级的弹框
     */
    private void showChangePriorityDilog(final TeamMemberInfo tm) {

        new ModifyPriorityPrompt().dialogModifyPriorityRequest(mContext, "话权", tm.getMemberPriority(), new ModifyPrioritytListener() {
            @Override
            public void onOk(int data) {
                toChangePriority(data, tm);
            }
        });
    }

    /*
    改变成员权限
     */
    private void toChangePriority(int priority, TeamMemberInfo tm) {
        ProtoMessage.TeamMember.Builder builder = ProtoMessage.TeamMember.newBuilder();
        builder.setUserPhone(tm.getUserPhone());
        builder.setMemberPriority(priority);
        builder.setTeamID(group);
        MyService.start(FirstActivity.this, ProtoMessage.Cmd.cmdModifyTeamMemberPriority.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(ModifyTeamMemberPriorityProcesser.ACTION);
        new TimeoutBroadcast(FirstActivity.this, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(FirstActivity.this, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(FirstActivity.this, "话权修改成功");
                    getGroupMan(group);
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });

    }
    //*****************************************设置群成员显示********************************************

    private void showDeleteDialog(final TeamMemberInfo tm) {
//		cmdDeleteTeamMember
        String userName = tm.getNickName();
        if (TextUtils.isEmpty(userName)) {
            userName = tm.getUserName();
        }
        String msg = tm.getUserName();
        if (msg == null || msg.equals("")) {
            msg = tm.getUserPhone();
        }
        PromptDialog dialog = new PromptDialog(mContext);
        dialog.show();
        dialog.setTitle("提示：");
        dialog.setMessage("确定要删除 " + userName + " ？");
        dialog.setOkListener("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                deleteTeamMember(tm);
            }
        });
        dialog.setCancelListener("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
       /* new AlertDialog.Builder(FirstActivity.this, AlertDialog.THEME_HOLO_LIGHT).setTitle("提示：")// 提示框标题
                .setMessage("确定要删除" + msg + "?").setPositiveButton("确定", // 提示框的两个按钮
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deleteTeamMember(tm);
                        dialog.dismiss();
                    }

                }).setNegativeButton("取消", new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();*/

    }

    /**
     * 删除群成员
     *
     * @param tm
     */
    public void deleteTeamMember(final TeamMemberInfo tm) {
        ProtoMessage.AcceptTeam.Builder builder = ProtoMessage.AcceptTeam.newBuilder();
        builder.setTeamID(group);
        builder.setPhoneNum(tm.getUserPhone());
        MyService.start(FirstActivity.this, ProtoMessage.Cmd.cmdDeleteTeamMember.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(DeleteTeamMemberProcesser.ACTION);
        new TimeoutBroadcast(FirstActivity.this, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(FirstActivity.this, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {

                    refreshLocalData(tm);
                    ToastR.setToast(FirstActivity.this, "删除成员成功");
                    //	ToastR.setToast(FirstActivity.this, "获取群成员成功");
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void refreshLocalData(TeamMemberInfo tm) {
        int k = -1;
        int i = -1;
        for (TeamMemberInfo mte : allTeamMemberInfos) {
            ++i;
            if (tm.getUserPhone().equals(mte.getUserPhone())) {
                k = i;
                break;
            }
        }
        if (k >= 0) {
            if (allTeamMemberInfos != null) {
                allTeamMemberInfos.remove(k);
                convertViewTeamMember(allTeamMemberInfos);
            }
        }
    }
    //**********************退出键判断***********************************

    //***********************设置权限*******************************
    private void setPermissions() {
        LayoutInflater factory = LayoutInflater.from(FirstActivity.this);// 提示框
        final View view = factory.inflate(R.layout.set_member_permissions, null);
        final RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
        final RadioButton radioMember = (RadioButton) view.findViewById(R.id.radio_member);
        final RadioButton radioManager = (RadioButton) view.findViewById(R.id.radio_manager);
        if (mTeamMemberInfo == null) {
            return;
        }
        if (mTeamMemberInfo.getRole() == ProtoMessage.TeamRole.memberOnly_VALUE) {
            radioMember.setChecked(true);
        } else {
            radioManager.setChecked(true);
        }


        PromptDialog dialog = new PromptDialog(mContext);
        dialog.show();
        dialog.setTitle("设置管理员");
        dialog.setView(view);
        dialog.setOkListener("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (radioMember.isChecked()) {
                    teamRole = ProtoMessage.TeamRole.memberOnly_VALUE;
                } else if (radioManager.isChecked()) {
                    teamRole = ProtoMessage.TeamRole.Manager_VALUE;
                }
                toSendChangePermission(teamRole, mTeamMemberInfo);
                dialog.dismiss();
            }
        });
        dialog.setCancelListener("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


      /*  new AlertDialog.Builder(FirstActivity.this, AlertDialog.THEME_HOLO_LIGHT).setTitle("设置管理员")// 提示框标题
                .setView(view).setPositiveButton("确定", // 提示框的两个按钮
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (radioMember.isChecked()) {
                            teamRole = ProtoMessage.TeamRole.memberOnly_VALUE;
                        } else if (radioManager.isChecked()) {
                            teamRole = ProtoMessage.TeamRole.Manager_VALUE;
                        }
                        toSendChangePermission(teamRole, mTeamMemberInfo);
                        dialog.dismiss();
                    }

                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();*/
    }
    //**********************改变GridView大小***********************************

    //上传权限修改设置
    private void toSendChangePermission(final int role, TeamMemberInfo info) {
        ProtoMessage.AssignTeamAdmin.Builder builder = ProtoMessage.AssignTeamAdmin.newBuilder();
        builder.setTeamID(group);
        builder.setPhoneNum(info.getUserPhone());
        builder.setAdmin(role);
        MyService.start(FirstActivity.this, ProtoMessage.Cmd.cmdAssignTeamAdmin.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(AssignTeamAdminProcesser.ACTION);
        new TimeoutBroadcast(FirstActivity.this, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(FirstActivity.this, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    TeamMemberInfo teamMemberInfo = i.getParcelableExtra("get_team_member_info");
                    mTeamMemberInfo.setRole(role);
                    getGroupMan(group);
                    ToastR.setToast(FirstActivity.this, "修改权限成功");
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    public void getGroupMan(final long id) {
        readSql();
        mansNum = userList.size();

        ProtoMessage.AcceptTeam.Builder builder = ProtoMessage.AcceptTeam.newBuilder();
        builder.setTeamID(id);
        MyService.start(FirstActivity.this, ProtoMessage.Cmd.cmdGetTeamMember.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(TeamMemberProcesser.ACTION);
        TimeoutBroadcast x = new TimeoutBroadcast(FirstActivity.this, filter, getBroadcastManager());
        x.startReceiver(15, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                ToastR.setToast(FirstActivity.this, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
//                    TeamMemberInfoList list = i.getParcelableExtra("get_teamMember_list");
                    convertViewTeamMember(SQLiteTool.getTeamMembers(mContext, id));
                    //	ToastR.setToast(FirstActivity.this, "获取群成员成功");
                } else if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.TEAM_NOT_EXIST_VALUE ||
                        i.getIntExtra("error_code", -1) ==
                                ProtoMessage.ErrorCode.NOT_TEAM_MEMBER_VALUE) {
                    MsgTool.deleteTeamMsg(FirstActivity.this, group);
                    ToastR.setToast(FirstActivity.this, "你已经不是这个群成员（被踢出），或者群已经被解散");
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }
    //*****************通过系统图库选择图片发送********************

    private void readSql() {
        TeamMemberHelper teamMemberHelper = new TeamMemberHelper(FirstActivity.this, group + "TeamMember.dp", null);
        SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
        try {
            tempCursorUser = db.query("LinkmanMember", null, null, null, null, null, null);
            try {
                userList.clear();
                if (tempCursorUser.moveToFirst()) {
                    do {
                        int sex = -1;
                        String memberName = null;
                        String phone = tempCursorUser.getString(tempCursorUser.getColumnIndex("user_phone"));
                        if (listMembersCache.size() > 0) {
                            for (AppliedFriends appliedFriends : listMembersCache) {
                                if (appliedFriends.getPhoneNum().equals(phone)) {
                                    memberName = appliedFriends.getNickName();
                                    sex = appliedFriends.getUserSex();
                                    break;
                                }
                            }
                        }
                        if (memberName == null || memberName.equals("")) {
                            memberName = tempCursorUser.getString(tempCursorUser.getColumnIndex("nick_name"));
                        }
                        if (memberName == null || memberName.equals("")) {
                            memberName = tempCursorUser.getString(tempCursorUser.getColumnIndex("user_name"));
                        }
                        int status = ProtoMessage.ChatStatus.csOk_VALUE;
                        userT(memberName, status, phone, sex);
                    } while (tempCursorUser.moveToNext());
                    rankList(0);
                    Message message2 = new Message();
                    message2.what = 5;
                    refreshHandler.sendMessage(message2);
                }
//                mMap.refreshMapLocalData(userList, call_hungon == 1);
            } finally {
                tempCursorUser.close();
            }
        } finally {
            db.close();
        }

    }
    //*****************改变说话人信息*********************

    //*****************************************设置群成员显示********************************************
    private void convertViewTeamMember(List<TeamMemberInfo> teamMemberInfos) {
        nowMansNum = 0;
        userList.clear();
        allTeamMemberInfos = teamMemberInfos;//保存群成员列表
        String memberName;
        int sex;
//		DBManagerFriendsList db = new DBManagerFriendsList(FirstActivity.this);
//		List<AppliedFriends> listMembers = db.getFriends();
//		db.closeDB();

        for (TeamMemberInfo in : allTeamMemberInfos) {
            memberName = null;
            sex = 0;
            if (listMembersCache.size() > 0) {
                for (AppliedFriends appliedFriends : listMembersCache) {
                    if (appliedFriends.getPhoneNum().equals(in.getUserPhone())) {
                        memberName = appliedFriends.getNickName();
                        sex = appliedFriends.getUserSex();
                        break;
                    }
                }
            }
            if (memberName == null || memberName.equals("")) {
                memberName = in.getNickName();
            }
            if (memberName == null || memberName.equals("")) {
                memberName = in.getUserName();
            }
            int status = ProtoMessage.ChatStatus.csOk_VALUE;
            if (r != null) {
                List<ProtoMessage.ChatRoomMemberMsg> members = r.getMembersList();
                for (ProtoMessage.ChatRoomMemberMsg member : members) {
                    if (member.getPhoneNum().equals(in.getUserPhone())) {
                        status = member.getStatus();
                        if (status == ProtoMessage.ChatStatus.csOk_VALUE) {
                            nowMansNum++;
                        } else if (status == ProtoMessage.ChatStatus.csSpeaking_VALUE) {
                            nowMansNum++;
                        }
                        break;
                    }
                }
            }
            userT(memberName, status, in.getUserPhone(), sex);
//            Message message = new Message();
//            message.what = 3;
//            refreshHandler.sendMessage(message);
//            changeState = status;
//            changePhone = in.getUserPhone();
        }
//        mMap.refreshMapLocalData(userList, call_hungon == 1);
        mansNum = userList.size();
        Message message = new Message();
        message.what = 2;
        refreshHandler.sendMessage(message);

        //  userT(User.ADD, 0, "", 0);

        mTMInfo.clear();
        for (TeamMemberInfo te : allTeamMemberInfos) {
            for (AppliedFriends af : listMembersCache) {
                if (af.getPhoneNum().equals(te.getUserPhone())) {
                    mTMInfo.add(te);
                    break;
                }
            }
        }
        mTMInfoNew.clear();
        for (TeamMemberInfo te : allTeamMemberInfos) {
            int i = 0;
            for (TeamMemberInfo af : mTMInfo) {
                if (af.getUserPhone().equals(te.getUserPhone())) {
                    //Log.i("info", "no:[" + te.getUserPhone() + "]" + "mTMInfo[" + af.getUserPhone() + "]");
                    break;
                } else {
                    i++;
                    if (i == mTMInfo.size()) {
                        mTMInfoNew.add(te);
                    }
                    //Log.i("info", "eq:[" + te.getUserPhone() + "]" + "mTMInfo[" + af.getUserPhone() + "]");
                }
            }
            if (mTMInfo.size() <= 0) {
                mTMInfoNew.add(te);
            }
        }
        setGridViewClick(true);
        rankList(0);

        //adapterU.notifyDataSetChanged();

    }

    private void rankList(int ii) {
        for (int i = 0; i < userList.size() - ii; i++) {
            userList.get(i).setPinYin(StringHelper.getPinYinHeadChar(userList.get(i).getName()));
        }

        Collections.sort(userList, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {

                // 加号图标永远排在最后
                if (o2.isAddIcon())
                    return -1;
                if (o1.isAddIcon()) {
                    return 1;
                }

                return o1.getPinYin().compareTo(o2.getPinYin());
            }
        });

        /*
        for(String s : indexStr){
            for(int i = 0; i < userList.size()-ii;){
                if(s.equals(userList.get(i).getPinYin())){
                    templist.add(userList.get(i));
                    userList.remove(i);
                }else{
                    i++;
                }
            }
        }
        if(ii == 0){
            for(User user : templist){
                userList.add(user);
            }
        }else{
            //int i = userList.size() - 1;
            User userTemp = (User)((LinkedList)userList).pollLast();
            //userList.remove(i);
            for(User user : templist){
                userList.add(user);
            }
            userList.add(userTemp);
        }
        */

        Message message = new Message();
        message.what = 0;
        refreshHandler.sendMessage(message);
    }

    public void getUserFace(String memberName) {
        ProtoMessage.SearchUser.Builder builder = ProtoMessage.SearchUser.newBuilder();
        builder.setPhoneNum(memberName);
        builder.setOnlyPhoneNum(true);
        MyService.start(FirstActivity.this, ProtoMessage.Cmd.cmdSearchUser.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(SearchFriendProcesser.ACTION);
        new TimeoutBroadcast(FirstActivity.this, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {

            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    AppliedFriends aply = i.getParcelableExtra("search_user");
                    if (aply.getPhoneNum() == null || aply.getPhoneNum().length() <= 0) {
                        ToastR.setToast(FirstActivity.this, "未找到该用户");
                    } else {
                        msgDB_Team();
                        adapterU.notifyDataSetChanged();
                    }
                } else {
                    Log.e("jim", "firstactivity userFace  code:" + i.getIntExtra("error_code", -1));
                    // fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    //**********************退出键判断***********************************
    @Override
    public void onBackPressed() {
        Log.i("pocdemo", "first activit onBackPress:" + additionType);

        if (GlobalStatus.isVideo()) {
            HungupClick();
        } else if (additionType == 1) {
            additionLayout.setVisibility(GONE);
            additionType = 0;
        } else {
            if (bottomLayoutManager != null) {
                bottomLayoutManager.removeView();
            }
            if (!isFinishing() && !pauseIs) {
                super.onBackPressed();
            }
        }
    }
    //****************************拍照发送******************************

    //正在对讲
    private void notification() {
        if (GlobalStatus.getChatRoomMsg() != null) {
            Intent notificationIntent = new Intent(FirstActivity.this, FirstActivity.class);
            notificationIntent.putExtra("callType", 0);
            String titleMsg;
            if (single) {
                titleMsg = linkman;
                notificationIntent.putExtra("linkmanName", linkman);
                notificationIntent.putExtra("linkmanPhone", linkmanPhone);
                notificationIntent.putExtra("data", 1);
            } else {
                titleMsg = groupName;
                notificationIntent.putExtra("group", group);
                notificationIntent.putExtra("data", 1);
                notificationIntent.putExtra("group_name", groupName);
                notificationIntent.putExtra("type", type);
            }
            notificationIntent.putExtra("room_id", roomId);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(FirstActivity.this, 0, notificationIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification;
            notification = new Notification.Builder(FirstActivity.this)
                    .setSmallIcon(R.mipmap.ic_launcher)//必须要先setSmallIcon，否则会显示默认的通知，不显示自定义通知
                    .setTicker(titleMsg)
                    .setContentTitle(titleMsg)
                    .setContentText("正在对讲")
                    .setContentIntent(pendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
//                .setDefaults(Notification.DEFAULT_VIBRATE)
                    .build();
            notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT;
            //notification.flags += Notification.FLAG_AUTO_CANCEL|Notification.FLAG_ONGOING_EVENT;
            manager.notify(-1, notification);
        }
    }

    //*****************通过系统图库选择图片发送********************
    public void openPicture() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        /*打开文件intent.setAction(Intent.ACTION_OPEN_DOCUMENT);*/
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICTURE);
    }

    public void openVideo() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        /*打开文件intent.setAction(Intent.ACTION_OPEN_DOCUMENT);*/
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, VIDEO);
    }
    //****************************压缩图片******************************

    //*****************改变说话人信息*********************
    public synchronized void changespeaking(int judge, String changephone) {
        if (call_hungon == 0) {
            Log.v("pocdemo", "call_hungon == 0");
            return;
        }
        Log.v("pocdemo", "changespeaking:" + judge + ",phone:" + changephone);
        if (judge == ProtoMessage.ChatStatus.csSpeaking_VALUE) {
            for (int i = 0; i < userList.size(); i++) {
                if (userList.get(i).getState() == ProtoMessage.ChatStatus.csSpeaking_VALUE) {
                    userList.get(i).setState(ProtoMessage.ChatStatus.csOk_VALUE);
                }
                if (userList.get(i).getPhone().equals(changephone)) {
                    userList.get(i).setState(judge);
                    speakingUser.setName(userList.get(i).getName());
                    speakingUser.setPhone(changephone);
                    tempHead = GlobalImg.getImage(FirstActivity.this, changephone);
                    //speakingUser.setImage(userList.get(i).getImage());
                    String time = saveMsgRemind(userList.get(i).getName() + "在话权中");
                    msgList.add(new Msg(userList.get(i).getName() + "在话权中", Msg.TYPE_MSG_RECORD, time, 0, 0, changephone, 0, 0));
                    userList.add(0, userList.get(i));
                    userList.remove(i + 1);
                }
            }
            if (changephone.equals(this.myPhone)) {
                if (single) {
                    speakingUser.setName("您正在说话");
                    speakingname.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                    speakingname.setText(speakingUser.getName());
                    tempHead = GlobalImg.getImage(FirstActivity.this, changephone);
                    AniDraw.start();
                    speakingstate.setVisibility(VISIBLE);
                    String time = saveMsgRemind("您在话权中");
                    msgList.add(new Msg("您在话权中", Msg.TYPE_MSG_RECORD, time, 0, 0, changephone, 0, 0));

                }

            }
        } else {
            for (int i = 0; i < userList.size(); i++) {
                if (userList.get(i).getPhone().equals(changephone)) {
                    userList.get(i).setState(judge);
                    if (userList.get(i).getName().equals(speakingUser.getName())) {
                        speakingUser.setName("");
                        speakingUser.setPhone("");
                    }
                    break;
                }
            }
            if (!GlobalStatus.getStatusList().contains(ProtoMessage.ChatStatus.csSpeaking_VALUE)) {
                speakingUser.setName("");
                speakingUser.setPhone("");
            }
            if (changephone.equals(myPhone)) {
                speakingUser.setName("");
            }
        }
        changeTitle();
        adapter.notifyDataSetChanged();
        adapterU.notifyDataSetChanged();
        nowMansNum = 0;
        for (User user : userList) {
            if (user.getState() == ProtoMessage.ChatStatus.csSpeaking_VALUE ||
                    user.getState() == ProtoMessage.ChatStatus.csOk_VALUE) {
                nowMansNum++;
            }
        }
        if (call_hungon == 1) {
            if (single) {
                name.setText(linkman);
                sta_num.setVisibility(VISIBLE);
                sta_num.setText(online[nowMansNum]);
            } else {
                name.setText(groupName);
                sta_num.setText(nowMansNum + "/" + mansNum);
            }
            //  mMap.refreshMapLocalData(userList);
        }
    }
    //******************得到剪贴板管理器（复制）**************************************


    //******************title显示说话人信息********************
    public void changeTitle() {
        groupNullHint.setVisibility(GONE);
        Log.v("pocdemo", "changeTitle speakingUser.getName():" + speakingUser.getName() + ",group:" + group);
        if (speakingUser.getName().equals("")) {
//            speakingimage.setVisibility(View.GONE);
            showSpeakMan.setVisibility(GONE);
            AniDraw.stop();
            speakingstate.setVisibility(GONE);
            speakingname.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            speakingname.setText("");
            videoLayout.hideVideo();
            groupNullHint.setVisibility(VISIBLE);
            Log.v("pocdemo", "changeTitle no single");
        } else {
            speakingname.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            speakingname.setText(speakingUser.getName());

            if (tempHead == null) {
                tempHead = GlobalImg.getImage(this, speakingUser.getPhone());
            }
            if (tempHead != null) {
//                speakingimage.setImageBitmap(tempHead);
                showSpeakMan.setImageBitmap(tempHead);
            } else {
//                speakingimage.setImageResource(R.drawable.man);
                showSpeakMan.setImageResource(R.drawable.man);
            }
            AniDraw.start();
            speakingstate.setVisibility(VISIBLE);
//            speakingimage.setVisibility(View.VISIBLE);
            if (GlobalStatus.isVideo()) {
                videoLayout.showVideo(FirstActivity.this);
                showSpeakMan.setVisibility(GONE);
            } else {
                showSpeakMan.setVisibility(VISIBLE);
            }
        }
    }

    public void showSpeakMan() {
        if (!speakingUser.getName().equals("")) {
            showSpeakMan.setVisibility(VISIBLE);
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM + "/luobin");
        if (!path.exists()) {
            path.mkdirs();
        }
        SimpleDateFormat x = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timeStamp = x.format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        Log.i("jrdchat", "path: " + path.getAbsolutePath());
        //.getExternalFilesDir()方法可以获取到 SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
        //创建临时文件,文件前缀不能少于三个字符,后缀如果为空默认未".tmp"
        File image = File.createTempFile(
                imageFileName,  /* 前缀 */
                ".jpg",         /* 后缀 */
                path      /* 文件夹 */
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private File createVideoFile() throws IOException {
        // Create an image file name
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM + "/luobin");
        if (!path.exists()) {
            path.mkdirs();
        }
        SimpleDateFormat x = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timeStamp = x.format(new Date());
        String imageFileName = "VIDEO_" + timeStamp;
        Log.i("jrdchat", "time file name: " + imageFileName);
        //.getExternalFilesDir()方法可以获取到 SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
        //创建临时文件,文件前缀不能少于三个字符,后缀如果为空默认未".tmp"
        File image = File.createTempFile(
                imageFileName,  /* 前缀 */
                ".mp4",         /* 后缀 */
                path      /* 文件夹 */
        );
        mCurrentVideoPath = image.getAbsolutePath();
        return image;
    }

    //调用相机
    public void openCamera() {
        isOpenCamera();
    }

    public void isOpenCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {//判断是否有相机应用
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();//创建临时图片文件
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                //FileProvider 是一个特殊的 ContentProvider 的子类，
                //它使用 content:// Uri 代替了 file:/// Uri. ，更便利而且安全的为另一个app分享文件
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.luobin.dvr.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, TAKE_PHOTO);
            }
        }
    }

    //调用相机录像
    public void openCameraVideo() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {//判断是否有相机应用
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createVideoFile();//创建临时图片文件
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                //FileProvider 是一个特殊的 ContentProvider 的子类，
                //它使用 content:// Uri 代替了 file:/// Uri. ，更便利而且安全的为另一个app分享文件
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.luobin.dvr.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                //takePictureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                startActivityForResult(takePictureIntent, TAKE_VIDEO);
            }
        }
    }
    //*********************按钮点击******************************

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //权限申请结果
        if (requestCode == MY_PERMISSIONS_REQUEST_AUDIO) {
            mPermissionUtil.requestResult(this, permissions, grantResults, this, PermissionUtil.PERMISSIONS_RECORD_AUDIO);

        } else if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            mPermissionUtil.requestResult(this, permissions, grantResults, this, PermissionUtil.PERMISSIONS_LOCATION);
        } else if (requestCode == MY_PERMISSIONS_REQUEST_VIDEO) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    String permission = permissions[i];
                    if (permission.equals(PermissionUtil.PERMISSIONS_RECORD_AUDIO)) {
                        ToastR.setToastLong(mContext, "[ 录音 ] 权限已经拒绝，请到手机管家或者系统设置里授权");
                    } else if (permission.equals(PermissionUtil.PERMISSIONS_LOCATION)) {
                        ToastR.setToastLong(mContext, "[ 定位 ] 权限已经拒绝，请到手机管家或者系统设置里授权");
                    } else if (permission.equals(PermissionUtil.PERMISSIONS_CAMERA)) {
                        ToastR.setToastLong(mContext, "[ 摄像 ] 权限已经拒绝，请到手机管家或者系统设置里授权");
                    } else if (permission.equals(PermissionUtil.PERMISSIONS_STORAGE)) {
                        ToastR.setToastLong(mContext, "[ 存储 ] 权限已经拒绝，请到手机管家或者系统设置里授权");
                    }
                } else {
                    checkVideoPermission();
                }
            }
        } else if (requestCode == VideoLayout.MY_PERMISSIONS_REQUEST_VIDEO_LAYOUT) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    String permission = permissions[i];
                    if (permission.equals(PermissionUtil.PERMISSIONS_CAMERA)) {
                        ToastR.setToastLong(mContext, "[ 摄像 ] 权限已经拒绝，请到手机管家或者系统设置里授权");
                    } else if (permission.equals(PermissionUtil.PERMISSIONS_STORAGE)) {
                        ToastR.setToastLong(mContext, "[ 存储 ] 权限已经拒绝，请到手机管家或者系统设置里授权");
                    }
                    videoLayout.hideVideo();
                } else if (videoLayout != null) {
                    videoLayout.checkVideoPermission();
                }
            }
        }
    }


    @Override
    public void onPermissionSuccess(String type) {
        if (type.equals(PermissionUtil.PERMISSIONS_RECORD_AUDIO)) {

        } else if (type.equals(PermissionUtil.PERMISSIONS_LOCATION)) {
//            Intent intent = new Intent(ServiceCheckUserEvent.ACTION);
//            intent.putExtra("type", ServiceCheckUserEvent.START_AMAP);
//            mContext.sendBroadcast(intent);
//            isOpenCamera();
        }

    }

    @Override
    public void onPermissionReject(String strMessage) {
        if (strMessage.equals(PermissionUtil.PERMISSIONS_RECORD_AUDIO)) {
            ToastR.setToastLong(mContext, "[ 录音 ] 权限已经拒绝，请到手机管家或者系统设置里授权");
        } else if (strMessage.equals(PermissionUtil.PERMISSIONS_LOCATION)) {
            ToastR.setToastLong(mContext, "[ 定位 ] 权限已经拒绝，请到手机管家或者系统设置里授权");
        } else if (strMessage.equals(PermissionUtil.PERMISSIONS_CAMERA)) {
            ToastR.setToastLong(mContext, "[ 摄像 ] 权限已经拒绝，请到手机管家或者系统设置里授权");
        } else if (strMessage.equals(PermissionUtil.PERMISSIONS_STORAGE)) {
            ToastR.setToastLong(mContext, "[ 存储 ] 权限已经拒绝，请到手机管家或者系统设置里授权");
        }
    }

    @Override
    public void onPermissionFail(String failType) {

    }

    //*********************************文本框监听**********************************

    //处理返回信息
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null && requestCode != TAKE_PHOTO && requestCode != TAKE_VIDEO) {
            return;
        }
        int resultData = 0;
        if (data != null) {
            resultData = data.getIntExtra("data", 0);
        }
        switch (requestCode) {
            case TAKE_PHOTO://调用剪切程序
                if (resultCode == RESULT_OK) {
                    Bitmap bmp = BitmapFactory.decodeFile(mCurrentPhotoPath);
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File f = new File(mCurrentPhotoPath);
                    Uri contentUri = Uri.fromFile(f);
                    mediaScanIntent.setData(contentUri);
                    sendBroadcast(mediaScanIntent);
//                    saveI(bmp, Msg.TYPE_SENT_IMAGE, saveBitmap(bmp));
                    /*Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(imageUri, "image*//*");
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, CROP_PHOTO);*/
                }
                break;
            /*case CROP_PHOTO://剪切后发送图片
                if (resultCode == RESULT_OK) {
                    try {
                        String image = FileUtils.getUriPath(this, imageUri);
                        saveI(BitmapFactory.decodeStream(getContentResolver().
                                openInputStream(imageUri)), Msg.TYPE_SENT_IMAGE, image);

                    } catch (FileNotFoundException e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    }
                }
                break;*/
            case TAKE_VIDEO:
                if (resultCode == RESULT_OK) {
                    Bitmap bmp = ThumbnailUtils.createVideoThumbnail(mCurrentVideoPath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File f = new File(mCurrentVideoPath);
                    Uri contentUri = Uri.fromFile(f);
                    mediaScanIntent.setData(contentUri);
                    sendBroadcast(mediaScanIntent);
                    saveI(bmp, Msg.TYPE_SENT_VIDEO, mCurrentVideoPath);
                    /*Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(imageUri, "image*//*");
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, CROP_PHOTO);*/
                }
                break;
            case PICTURE://打开图库图片后，发送
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    String image = FileUtils.getUriPath(this, uri); //（因为4.4以后图片Uri发生了变化）通过文件工具类 对uri进行解析得到图片路径
                    try {
                        Bitmap bmp = BitmapFactory.decodeFile(image);
                        if (bmp == null) {
                            ToastR.setToastLong(this, "请确认是否是图片或者图片异常！");
                            return;
                        }
                        saveI(bmp, Msg.TYPE_SENT_IMAGE, image);

                    } catch (OutOfMemoryError e) {
                        ToastR.setToast(this, "内存不足，无法打开图片");
                        clearUnfinishedUploadImage();
                        finish();
                    }
                }
                break;
            case VIDEO://打开图库选择视频后
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    String image = FileUtils.getUriPath(this, uri); //（因为4.4以后图片Uri发生了变化）通过文件工具类 对uri进行解析得到图片路径
                    if (image.endsWith(".mp4") || image.endsWith(".MP4")) {
                        try {
                            Bitmap bmp = ThumbnailUtils.createVideoThumbnail(image, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
                            saveI(bmp, Msg.TYPE_SENT_VIDEO, image);

                        } catch (OutOfMemoryError e) {
                            ToastR.setToast(this, "内存不足，无法打开图片");
                            clearUnfinishedUploadImage();
                            finish();
                        }
                    } else {
                        ToastR.setToast(this, "请选择MP4格式的视频");
                    }
                }
                break;
            case DISMISS_OR_QUIT:
                if (resultCode == RESULT_OK) {
                    if (data.getIntExtra("refresh", 0) == ShowAllTeamMemberActivity.REFRESH) {
                        getlistMembersCache();
                        refreshTeamInfo();
                        convertViewTeamMember(getTeamMember(group));
                    }
                    if (resultData == 2) {
                        finish();
                    }
                }
                break;
            case REFRESH_REMARK:
                if (resultCode == RESULT_OK) {
                    if (resultData == 1) {
                        AppliedFriends appliedFriend = data.getParcelableExtra("friend_detail");
                        updateLocalData(appliedFriend, "");
                        if (data.getIntExtra("map", 0) == 2) {
                            viewPager.setCurrentItem(0);
                        }
                    }
                    if (resultData == 2) {
                        finish();
                    }

                }
                break;
            case REFRESH_TEAM_REMARK:
                if (resultCode == RESULT_OK) {
                    TeamMemberInfo teamMemberInfo = data.getParcelableExtra("team_refresh");
                    switch (resultData) {
                        case 1:
                            updateMemberLocalData(teamMemberInfo);
                            break;
                        case 2:
                            finish();
                            break;
                        case 3:
                            String phone = data.getStringExtra("phone");
                            TeamMemberInfo tm = new TeamMemberInfo();
                            tm.setUserPhone(phone);
                            refreshLocalData(tm);
                            break;
                    }
                }
                break;
            default:
                break;
        }
    }

    private void refreshTeamInfo() {
        TeamInfo teamInfo = getTeamInfo();
        if (teamInfo == null) {
            return;
        }
        groupName = teamInfo.getTeamName();
        name.setText(teamInfo.getTeamName());
    }


    public List<TeamMemberInfo> getTeamMember(Long teamId) {
        TeamMemberHelper teamMemberHelper = new TeamMemberHelper(mContext, teamId + "TeamMember.dp", null);
        List<TeamMemberInfo> teamMemberInfo = new ArrayList<TeamMemberInfo>();
        SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
        try {
            Cursor c = db.query("LinkmanMember", null, null, null, null, null, null);
            try {
                TeamMemberInfo af = null;
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
        } finally {
            db.close();
        }
        return teamMemberInfo;
    }


    private void updateMemberLocalData(final TeamMemberInfo teamMemberInfo) {
        for (User user : userList) {
            if (teamMemberInfo.getUserPhone().equals(user.getPhone())) {
                user.setName(teamMemberInfo.getNickName());
                break;
            }
        }
        for (TeamMemberInfo tm : allTeamMemberInfos) {
            if (tm.getUserPhone().equals(teamMemberInfo.getUserPhone())) {
                tm.setMemberPriority(teamMemberInfo.getMemberPriority());
                tm.setRole(teamMemberInfo.getRole());
                tm.setNickName(teamMemberInfo.getNickName());
                break;
            }
        }
        rankList(1);
    }

    private void clearUnfinishedUploadImage() {
        try {
            for (Msg msg : msgList) {
                // 没有失败，并且没有完成。
                if (msg.getPercent() > -1 && msg.getPercent() < 101) {
                    if (single) {
                        linkmanRecordHelper = new LinkmanRecordHelper(FirstActivity.this, myPhone + linkmanPhone + "LinkmanMsgShow.dp", null);
                        dbMsg = linkmanRecordHelper.getWritableDatabase();
                        try {
                            ContentValues values = new ContentValues();
                            values.put("percent", -1);
                            dbMsg.update("LinkmanRecord", values, "id = ?", new String[]{msg.getSn() + ""});
                        } finally {
                            try {
                                dbMsg.close();
                            } catch (Exception e) {

                            }
                        }

                    } else {
                        teamRecordHelper = new TeamRecordHelper(FirstActivity.this, myPhone + group + "TeamMsgShow.dp", null);
                        dbMsg = teamRecordHelper.getWritableDatabase();
                        try {
                            ContentValues values = new ContentValues();
                            values.put("percent", -1);
                            dbMsg.update("TeamRecord", values, "id = ?", new String[]{msg.getSn() + ""});
                        } finally {
                            try {
                                dbMsg.close();
                            } catch (Exception e) {

                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            Log.e("jrdchat", "清除图片未上传完成任务失败！ " + e.getMessage());
            e.printStackTrace();
        }
    }

    //***************按钮缩放动画*********************

    //****************************压缩图片******************************
    private Bitmap comp(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        if (baos.toByteArray().length / 1024 > 1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;
        if (newOpts.outHeight > newOpts.outWidth) {
            be = (newOpts.outHeight / 300);//be=1表示不缩放
        } else {
            be = (newOpts.outWidth / 300);//be=1表示不缩放
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return bitmap;//压缩好比例大小后再进行质量压缩
    }

    //*********************按钮点击******************************
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addition:
                //list.setSelection(msgList.size());
                if (additionType == 0) {
                    refreshHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(FirstActivity.this.getCurrentFocus().getWindowToken()
                                    , InputMethodManager.HIDE_NOT_ALWAYS);
                            additionLayout.setVisibility(VISIBLE);
                            additionType = 1;
                        }
                    }, 10);//关于优先级问题，不能随意改
                } else {
                    refreshHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            additionLayout.setVisibility(GONE);
                            additionType = 0;
                        }
                    }, 0);//关于优先级问题，不能随意改
                }
                break;
            case R.id.speaking_menu:
                boolean hangup = false;
                int role = 0;
                if (hostOk && call_hungon == 1) {
                    hangup = true;
                }
                if (single) {
                    menuType = GroupPopWindow.linkMan;
                } else {
                    if (type == ProtoMessage.TeamRole.memberOnly_VALUE) {
                        menuType = GroupPopWindow.member;
                    } else if (type == ProtoMessage.TeamRole.Manager_VALUE) {
                        menuType = GroupPopWindow.manager;
                    } else {
                        menuType = GroupPopWindow.owner;
                    }
                }
                //TODO 下拉菜单
                groupPopWindow = new GroupPopWindow(this, itemsOnClick, menuType, hangup);
                groupPopWindow.showPopupWindow(speakingMenu, mContext);
                break;
            case R.id.video_call:
                //TODO 视频通话
                if (ConnUtil.checkNetworkWifi(mContext)) {
                    checkVideoPermission();
                } else {
                    Dialog dialog = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT).setTitle("提示：")
                            .setMessage(getString(R.string.video_not_wifi)).setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            checkVideoPermission();
                                        }
                                    })
                            .setNegativeButton("取消", new AlertDialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create();
                    dialog.show();
                }

                break;
            case R.id.btn_to_map:
                viewPager.setCurrentItem(1);
                break;
            default:
                break;
        }
    }

    GroupPopWindow groupPopWindow;

    private OnClickListener itemsOnClick = new OnClickListener() {
        public void onClick(View v) {
            groupPopWindow.dismiss();
            switch (v.getId()) {
                case R.id.layout_modify_team:
                    if (type == ProtoMessage.TeamRole.memberOnly_VALUE) {
                        ShowTeamMsgDialog();
                    } else {
                        modifyTeamMsg();
                    }
                    break;
                case R.id.layout_see_all_member:

                    Intent intent = new Intent(mContext, ShowAllTeamMemberActivity.class);
                    TeamInfo mTeamInfo = getTeamInfo();
                    if (mTeamInfo == null) {
                        return;
                    }
                    Bundle mBundle = new Bundle();
                    mBundle.putParcelable("team_desc", mTeamInfo);
                    intent.putExtras(mBundle);
                    intent.putExtra("team_type", ShowAllTeamMemberActivity.INTENT_TYPE);
                    startActivityForResult(intent, DISMISS_OR_QUIT);
                    break;
                case R.id.layout_dismiss_team:
                    if (menuType.equals(GroupPopWindow.linkMan)) {
                        showDialog(deleteLinkMan);
                    } else if (menuType.equals(GroupPopWindow.owner)) {
                        showDialog(dismissTeam);
                    } else {
                        showDialog(quitTeam);
                    }
                    break;
                case R.id.layout_hang_up:
                    showDialog(closeRoom);
                    break;
                default:
                    break;
            }

        }
    };

    private void modifyTeamMsg() {
        TeamInfo mTeamInfo = getTeamInfo();
        if (mTeamInfo == null) {
            return;
        }
        Intent i = new Intent(mContext, ModifyTeamMsgActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("modify_team_msg", mTeamInfo);
        i.putExtras(bundle);
        startActivity(i);
    }

    private void ShowTeamMsgDialog() {
        TeamInfo mTeamInfo = getTeamInfo();
        if (mTeamInfo == null) {
            return;
        }
        new ShowTeamInfoPrompt().dialogSTeamInfo(mContext, mTeamInfo);
    }
    //******************添加ListView内容***************

    //***************按钮缩放动画*********************
    public void buttonChange(FloatingActionButton btn, int time) {
        ScaleAnimation sa = new ScaleAnimation(0, 1, 0, 1,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        sa.setDuration(time);
        btn.startAnimation(sa);
    }
    //******************添加ListView内容***************

    //******************添加ListView内容***************
    public void saveT(String str, int i) {
        if (i == Msg.TYPE_SENT) {
            tempStr = str;
            refreshHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendText(tempStr);
                }
            }, 0);
        }
        /*Msg msg = new Msg(str, i);
        msgList.add(msg);
		adapter.notifyDataSetChanged();
		list.setSelection(msgList.size());*/
    }

    public void dialogShow(String type) {
    }


    public void saveI(Bitmap bitmap, int i, final String address) {
        tempBit = bitmap;
        sendPic(comp(tempBit), address, i);
        /*Msg msg = new Msg(comp(bitmap), i);
        msgList.add(msg);
		adapter.notifyDataSetChanged();
		list.setSelection(msgList.size());*/
    }

    //******************添加GridView内容***************
    public void userT(String name, int state, String phone, int sex) {
        User user = new User(state, name, phone, sex);
        userList.add(user);

        // adapterU.notifyDataSetChanged();
    }

    private void speaking() {
        //Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        //vibrator.vibrate(50);

        /*long[] pattern = {0, 20, 10, 20};
        //-1表示不重复, 如果不是-1, 比如改成1, 表示从前面这个long数组的下标为1的元素开始重复.
        vibrator.vibrate(pattern, -1);*/
        speaking_now = true;


        //changespeaking(ProtoMessage.ChatStatus.csSpeaking_VALUE, myPhone);
        VoiceHandler.doVoiceAction(FirstActivity.this, true);
        button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.FloatingActionButton_down)));
    }

    //*****************************onTouch事件**************************
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            //语音以及文本发送按钮
        /*case R.id.list_view:
            if(event.getAction()==MotionEvent.ACTION_DOWN){
				InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(FirstActivity.this.getCurrentFocus().getWindowToken()
						,InputMethodManager.HIDE_NOT_ALWAYS);
				additionLayout.setVisibility(View.GONE);
				additionType=0;
			}
			break;*/
            case R.id.send_msg:
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    //抬起操作
                    if (sentType == 1) { //判断文本框里是否有已经输入
                        saveT(edit.getText().toString(), Msg.TYPE_SENT);
                        edit.setText("");
                        if (single) {
                            SharedPreferencesUtils.put(mContext, linkmanPhone, "");
                        } else {
                            SharedPreferencesUtils.put(mContext, group + "", "");
                        }
                    } else if (sentType == 0) {
                        ToastR.setToast(this, "请输入信息");
                    }
                }
                break;
            case R.id.button_1:
                //按下操作
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (record == 0) {
                        pttKeyDownSent();
                    }
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    //抬起操作
                    btnUp();
                    return true;
                }
                //移动操作
            /*if(event.getAction()==MotionEvent.ACTION_MOVE){
            }*/
                break;
            default:
                break;
        }
        return false;
    }

    private void btnUp() {
        if (record == 0) {
            pttKeyUpSent();
        } else if (record == 4) {
            record = 0;
            speakNotify.setVisibility(View.GONE);
            if (call_hungon == 0) {
                call.setVisibility(VISIBLE);
                hangup.setVisibility(GONE);
                button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.FloatingActionButton_false)));
                button.setImageResource(R.drawable.speak);
            } else {
                hangup.setVisibility(VISIBLE);
                call.setVisibility(GONE);
                button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.FloatingActionButton_up)));
                button.setImageResource(R.drawable.speak_call);
            }
            mSettings.setNotifyUser(false);
            mSettings.save();
        }
    }
    //*****************************onTouch事件**************************

    private void pttKeyDownSent() {
        up_down = true;
        if (call_hungon != 0) {
            edit.setFocusable(false);
            //ToastR.setToast(getApplicationContext(),"请说话");
            ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
            MyService.start(FirstActivity.this, ProtoMessage.Cmd.cmdSpeakBegin.getNumber(), builder.build());

            IntentFilter filter = new IntentFilter();
            filter.addAction(SpeakerBeginProcesser.ACTION);
            new TimeoutBroadcast(FirstActivity.this, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
                @Override
                public void onTimeout() {
                }

                @Override
                public void onGot(Intent i) {
                    if (i.getIntExtra("error_code", -1) ==
                            ProtoMessage.ErrorCode.OK.getNumber()) {
                        if (up_down) {
                            speaking();
                        } else {
                            ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
                            MyService.start(FirstActivity.this, ProtoMessage.Cmd.cmdSpeakEnd.getNumber(), builder.build());
                        }
                    } else {
                        fail(i.getIntExtra("error_code", -1));
                    }
                }
            });
        }

        mPermissionUtil.requestPermission(this, MY_PERMISSIONS_REQUEST_AUDIO, this, PermissionUtil.PERMISSIONS_RECORD_AUDIO, PermissionUtil.PERMISSIONS_RECORD_AUDIO);

//        if (Build.VERSION.SDK_INT > 22) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{
//                                Manifest.permission.RECORD_AUDIO},
//                        MY_PERMISSIONS_REQUEST_AUDIO
//                );
//            }
//        }

    }
    //****************还原群信息*************

    private void pttKeyUpSent() {
        edit.setFocusable(true);
        edit.setFocusableInTouchMode(true);
        up_down = false;
        if (call_hungon != 0) {
            VoiceHandler.doVoiceAction(FirstActivity.this, false);
            ProtoMessage.CommonRequest.Builder builder = ProtoMessage.CommonRequest.newBuilder();
            MyService.start(FirstActivity.this, ProtoMessage.Cmd.cmdSpeakEnd.getNumber(), builder.build());
            button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.FloatingActionButton_up)));

            IntentFilter filter = new IntentFilter();
            filter.addAction(SpeakerEndProcesser.ACTION);
            new TimeoutBroadcast(FirstActivity.this, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
                @Override
                public void onTimeout() {

                }

                @Override
                public void onGot(Intent i) {
                    if (i.getIntExtra("error_code", -1) ==
                            ProtoMessage.ErrorCode.OK.getNumber()) {
                    } else {
                        fail(i.getIntExtra("error_code", -1));
                    }
                }
            });
            if (speaking_now) {
                speaking_now = false;
                ToastR.setToast(getApplicationContext(), "请停止讲话 :-(");
//                ToastR.setToast(getApplicationContext(), "结束说话");
//                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//                vibrator.vibrate(50);

            }
        }
        /*if (sentType == 1) { //判断文本框里是否有已经输入
            saveT(edit.getText().toString(), Msg.TYPE_SENT);
            edit.setText("");
        } else if (sentType == 0) {

        }*/
    }

    //****************还原群信息*************
    public void reGridView() {
        for (User user : userList) {
            user.setState(ProtoMessage.ChatStatus.csOk_VALUE);
        }
        adapterU.notifyDataSetChanged();
    }
    //*************************************发送图片*******************************************

    //*************************************发送文字*******************************************
    public void sendText(String str) {
        saveMsg(str);
        long l;
        if (single) {
            linkmanRecordHelper = new LinkmanRecordHelper(FirstActivity.this, myPhone + linkmanPhone + "LinkmanMsgShow.dp", null);
            dbMsg = linkmanRecordHelper.getWritableDatabase();
            Cursor cursor = dbMsg.query("LinkmanRecord", null, null, null, null, null, null);
            if (cursor.moveToLast()) {
                l = cursor.getLong(cursor.getColumnIndex("id"));
            } else {
                l = 0;
            }
            cursor.close();
        } else {
            teamRecordHelper = new TeamRecordHelper(FirstActivity.this, myPhone + group + "TeamMsgShow.dp", null);
            dbMsg = teamRecordHelper.getWritableDatabase();
            Cursor cursor = dbMsg.query("TeamRecord", null, null, null, null, null, null);
            if (cursor.moveToLast()) {
                l = cursor.getLong(cursor.getColumnIndex("id"));
            } else {
                l = 0;
            }
            cursor.close();
        }
        dbMsg.close();
        ProtoMessage.CommonMsg.Builder builder = ProtoMessage.CommonMsg.newBuilder();
        if (linkman != null) {
            builder.setToUserPhone(linkmanPhone);
        } else {
            builder.setToTeamID(group);
        }
        builder.setMsgSN(l);
        builder.setMsgType(ProtoMessage.MsgType.mtText.getNumber());
        builder.setMsgContent(ByteString.copyFromUtf8(str));
        MyService.start(FirstActivity.this, ProtoMessage.Cmd.cmdCommonMsg.getNumber(), builder.build());
        intentFilter(l, "");
    }

    //*************************************发送文字*******************************************
    //*************************************发送图片*******************************************
    public void sendPic(Bitmap bitmap, String picAddress, int type) {
        long l;
        String address = saveMyBitmap(bitmap);
        savePic(address, picAddress, type);
        if (single) {
            linkmanRecordHelper = new LinkmanRecordHelper(FirstActivity.this, myPhone + linkmanPhone + "LinkmanMsgShow.dp", null);
            dbMsg = linkmanRecordHelper.getWritableDatabase();
            Cursor cursor = dbMsg.query("LinkmanRecord", null, null, null, null, null, null);
            if (cursor.moveToLast()) {
                l = cursor.getLong(cursor.getColumnIndex("id"));
            } else {
                l = 0;
            }
            cursor.close();
        } else {
            teamRecordHelper = new TeamRecordHelper(FirstActivity.this, myPhone + group + "TeamMsgShow.dp", null);
            dbMsg = teamRecordHelper.getWritableDatabase();
            Cursor cursor = dbMsg.query("TeamRecord", null, null, null, null, null, null);
            if (cursor.moveToLast()) {
                l = cursor.getLong(cursor.getColumnIndex("id"));
            } else {
                l = 0;
            }
            cursor.close();
        }
        dbMsg.close();
        ProtoMessage.CommonMsg.Builder builder = ProtoMessage.CommonMsg.newBuilder();
        if (linkman != null) {
            builder.setToUserPhone(linkmanPhone);
        } else {
            builder.setToTeamID(group);
        }
        builder.setMsgType(ProtoMessage.MsgType.mtImage.getNumber());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bitmapByte = baos.toByteArray();
        if (type == Msg.TYPE_SENT_IMAGE) {
            builder.setMsgType(ProtoMessage.MsgType.mtImage.getNumber());
        } else if (type == Msg.TYPE_SENT_VIDEO) {
            builder.setMsgType(ProtoMessage.MsgType.mtVideoFile_VALUE);
        }
        //builder.setMsgType(ProtoMessage.MsgType.mtImage.getNumber());
        builder.setMsgSN(l);
        builder.setMsgContent(ByteString.copyFrom(bitmapByte));
        MyService.start(FirstActivity.this, ProtoMessage.Cmd.cmdCommonMsg.getNumber(), builder.build());
        intentFilter(l, picAddress);
    }

    public String saveMyBitmap(Bitmap mBitmap) {
        String picAddress1;
        String picAddress2;
        String picAddress3;
        String picAddress4;
        String picAddress5;
        long l;
        if (single) {
            linkmanRecordHelper = new LinkmanRecordHelper(FirstActivity.this, myPhone + linkmanPhone + "LinkmanMsgShow.dp", null);
            dbMsg = linkmanRecordHelper.getWritableDatabase();
            Cursor cursor = dbMsg.query("LinkmanRecord", null, null, null, null, null, null);
            if (cursor.moveToLast()) {
                l = cursor.getLong(cursor.getColumnIndex("id")) + 1;
            } else {
                l = 0;
            }
            cursor.close();
        } else {
            teamRecordHelper = new TeamRecordHelper(FirstActivity.this, myPhone + group + "TeamMsgShow.dp", null);
            dbMsg = teamRecordHelper.getWritableDatabase();
            Cursor cursor = dbMsg.query("TeamRecord", null, null, null, null, null, null);
            if (cursor.moveToLast()) {
                l = cursor.getLong(cursor.getColumnIndex("id")) + 1;
            } else {
                l = 0;
            }
            cursor.close();
        }
        dbMsg.close();
        picAddress1 = getMyDataRoot(this);
        picAddress2 = "/" + myPhone;
        picAddress3 = "/" + group;
        picAddress4 = "/" + linkmanPhone;
        picAddress5 = "/" + l + ".jpg";

        FileOutputStream fOut = null;
        try {
            File outputImage = new File(picAddress1 + picAddress2 + picAddress3 + picAddress4);
            if (!outputImage.exists()) {
                outputImage.mkdirs();
            }
            outputImage = new File(picAddress1 + picAddress2 + picAddress3 + picAddress4 + picAddress5);
            if (!outputImage.exists()) {
                outputImage.createNewFile();
            }
            fOut = new FileOutputStream(outputImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return picAddress2 + picAddress3 + picAddress4 + picAddress5;
    }

   /* public String saveBitmap(Bitmap mBitmap) {
        String picAddress1;
        String picAddress2;
        String picAddress3;
        String picAddress4;
        String picAddress5;
        long l;
        if (single) {
            linkmanRecordHelper = new LinkmanRecordHelper(FirstActivity.this, myPhone + linkmanPhone + "LinkmanMsgShow.dp", null);
            dbMsg = linkmanRecordHelper.getWritableDatabase();
            l = createNewSn(dbMsg, "LinkmanRecord");
        } else {
            teamRecordHelper = new TeamRecordHelper(FirstActivity.this, myPhone + group + "TeamMsgShow.dp", null);
            dbMsg = teamRecordHelper.getWritableDatabase();
            l = createNewSn(dbMsg, "TeamRecord");
        }
        dbMsg.close();
        picAddress1 = getMyDataRoot(this);
        picAddress2 = "/" + myPhone;
        picAddress3 = "/" + group;
        picAddress4 = "/" + linkmanPhone;
        picAddress5 = "/" + l + "_true" + ".jpg";

        FileOutputStream fOut = null;
        try {
            File outputImage = new File(picAddress1 + picAddress2 + picAddress3 + picAddress4);
            if (!outputImage.exists()) {
                outputImage.mkdirs();
            }
            outputImage = new File(picAddress1 + picAddress2 + picAddress3 + picAddress4 + picAddress5);
            if (!outputImage.exists()) {
                outputImage.createNewFile();
            }
            fOut = new FileOutputStream(outputImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return picAddress1 + picAddress2 + picAddress3 + picAddress4 + picAddress5;
    }*/

    /*private long createNewSn(SQLiteDatabase db, String tableName) {
        //"select seq from sqlite_sequence where name = ?"
        long seq = 0;
        Cursor cursor = db.rawQuery("select seq from sqlite_sequence where name = ?", new String[]{tableName});
        if (cursor.moveToNext()) {
            seq = cursor.getInt(0); //获取第一列的值,第一列的索引从0开始
        } else {
            Log.i("jrdchat", "查找DB SN失败，打开数据库失败");
            //throw new RuntimeException("createNewSn failed");
        }
        cursor.close();
        seq++;
        Log.i("jrdchat", "获取新的SN：" + seq);

        return seq;
    }
*/
    //储存消息
    private String saveMsgRemind(String str) {
        Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int date = c.get(Calendar.DATE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);

        ContentValues values = new ContentValues();
        values.put("year", year);
        values.put("month", month);
        values.put("date", date);
        values.put("hour", hour);
        values.put("minute", minute);
        values.put("second", second);
        values.put("msg_send", 0);
        values.put("msg_type", 2);
        values.put("send_state", Msg.SENTS);
        values.put("msg", str);
        values.put("pic_address", "");
        if (single) {
            linkmanRecordHelper = new LinkmanRecordHelper(FirstActivity.this, myPhone + linkmanPhone + "LinkmanMsgShow.dp", null);
            dbMsg = linkmanRecordHelper.getWritableDatabase();
            dbMsg.insert("LinkmanRecord", null, values);
        } else {
            teamRecordHelper = new TeamRecordHelper(FirstActivity.this, myPhone + group + "TeamMsgShow.dp", null);
            dbMsg = teamRecordHelper.getWritableDatabase();
            values.put("phone", myPhone);
            dbMsg.insert("TeamRecord", null, values);
        }
        dbMsg.close();

        upDataMsg(0, str);
        return String.format("%02d:%02d", hour, minute);
    }

    private void saveMsg(String str) {
        Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int date = c.get(Calendar.DATE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);

        ContentValues values = new ContentValues();
        values.put("year", year);
        values.put("month", month);
        values.put("date", date);
        values.put("hour", hour);
        values.put("minute", minute);
        values.put("second", second);
        values.put("msg_send", 0);
        values.put("msg_type", 0);
        values.put("send_state", Msg.SENDING);
        values.put("msg", str);
        values.put("pic_address", "");
        if (single) {
            linkmanRecordHelper = new LinkmanRecordHelper(FirstActivity.this, myPhone + linkmanPhone + "LinkmanMsgShow.dp", null);
            dbMsg = linkmanRecordHelper.getWritableDatabase();
            dbMsg.insert("LinkmanRecord", null, values);
        } else {
            teamRecordHelper = new TeamRecordHelper(FirstActivity.this, myPhone + group + "TeamMsgShow.dp", null);
            dbMsg = teamRecordHelper.getWritableDatabase();
            values.put("phone", myPhone);
            dbMsg.insert("TeamRecord", null, values);
        }
        dbMsg.close();

        upDataMsg(0, str);
    }

    private void savePic(String str, String picAddress, int type) {
        Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int date = c.get(Calendar.DATE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);

        String msgcont = "";
        ContentValues values = new ContentValues();
        values.put("year", year);
        values.put("month", month);
        values.put("date", date);
        values.put("hour", hour);
        values.put("minute", minute);
        values.put("second", second);
        values.put("msg_send", 0);
        if (type == Msg.TYPE_SENT_IMAGE) {
            values.put("msg_type", 1);
            msgcont = "[图片]";
        } else if (type == Msg.TYPE_SENT_VIDEO) {
            values.put("msg_type", ProtoMessage.MsgType.mtVideoFile_VALUE);
            msgcont = "[视频]";
        }
        values.put("send_state", Msg.SENDING);
        values.put("msg", "");
        values.put("pic_address", str);
        values.put("pic_address_true", picAddress);
        if (single) {
            linkmanRecordHelper = new LinkmanRecordHelper(FirstActivity.this, myPhone + linkmanPhone + "LinkmanMsgShow.dp", null);
            dbMsg = linkmanRecordHelper.getWritableDatabase();
            dbMsg.insert("LinkmanRecord", null, values);
        } else {
            teamRecordHelper = new TeamRecordHelper(FirstActivity.this, myPhone + group + "TeamMsgShow.dp", null);
            dbMsg = teamRecordHelper.getWritableDatabase();
            values.put("phone", myPhone);
            dbMsg.insert("TeamRecord", null, values);
        }
        dbMsg.close();
        upDataMsg(1, msgcont);
    }
    //*************************************设置监听*******************************************

    //更新消息列表
    private void upDataMsg(int msgType, String msg) {
        MsgRecordHelper msgRecordHelper = new MsgRecordHelper(FirstActivity.this, myPhone + "MsgShow.dp", null);
        SQLiteDatabase db = msgRecordHelper.getWritableDatabase();


        Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int date = c.get(Calendar.DATE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        int top = 0;

        ContentValues values = new ContentValues();
        values.put("new_msg", 0);
        values.put("year", year);
        values.put("month", month);
        values.put("date", date);
        values.put("hour", hour);
        values.put("minute", minute);
        values.put("second", second);

        if (single) {
            values.put("msg_from", 0);
            values.put("phone", linkmanPhone);
            Cursor cursor = db.query("Msg", null, "phone=? and group_id=?", new String[]{linkmanPhone, 0 + ""}, null, null, null);
            if (cursor.moveToFirst()) {
                top = cursor.getInt(cursor.getColumnIndex("top"));
            }
            cursor.close();
            db.delete("Msg", "phone = ? and msg_from = ?", new String[]{linkmanPhone, 0 + ""});
        } else {
            values.put("msg_from", 1);
            values.put("phone", myPhone);
            Cursor cursor = db.query("Msg", null, "group_id=?", new String[]{group + ""}, null, null, null);
            if (cursor.moveToFirst()) {
                top = cursor.getInt(cursor.getColumnIndex("top"));
            }
            cursor.close();
            db.delete("Msg", "group_id = ?", new String[]{group + ""});
        }
        values.put("top", top);
        values.put("msg_type", msgType);
        values.put("group_id", group);
        values.put("msg", msg);
        db.insert("Msg", null, values);
        db.close();
    }

    //*************************************设置监听*******************************************
    private void intentFilter(long sn, final String picAddress) {
        if (single) {
            msgDB_Single();
        } else {
            msgDB_Team();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(SendMsgProcesser.ACTION);
        final long SN = sn;
        final String PicAddress = picAddress;
        //Log.i("chatjrd", "打开图片地址" + picAddress);
        new TimeoutBroadcast(FirstActivity.this, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(FirstActivity.this, "发送失败");
                if (single) {
                    linkmanRecordHelper = new LinkmanRecordHelper(FirstActivity.this, myPhone + linkmanPhone + "LinkmanMsgShow.dp", null);
                    dbMsg = linkmanRecordHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("send_state", Msg.SENTF);
                    dbMsg.update("LinkmanRecord", values, "id = ?", new String[]{SN + ""});
                    dbMsg.close();
                    msgDB_Single();
                } else {
                    teamRecordHelper = new TeamRecordHelper(FirstActivity.this, myPhone + group + "TeamMsgShow.dp", null);
                    dbMsg = teamRecordHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("send_state", Msg.SENTF);
                    dbMsg.update("TeamRecord", values, "id = ?", new String[]{SN + ""});
                    dbMsg.close();
                    msgDB_Team();
                }
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    Log.i("chatjrd", "发送成功");
                    if (single) {
                        linkmanRecordHelper = new LinkmanRecordHelper(FirstActivity.this, myPhone + linkmanPhone + "LinkmanMsgShow.dp", null);
                        dbMsg = linkmanRecordHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("send_state", Msg.SENTS);
                        values.put("service_id", i.getLongExtra("service_id", -1));
                        //Log.i("jrdchat","返回的——msgid:"+i.getLongExtra("service_id", -1));
                        //Log.i("jrdchat","返回的——sn:"+i.getLongExtra("id", -1));
                        dbMsg.update("LinkmanRecord", values, "id = ?", new String[]{i.getLongExtra("id", -1) + ""});
                        dbMsg.close();
                        msgDB_Single();
                    } else {
                        teamRecordHelper = new TeamRecordHelper(FirstActivity.this, myPhone + group + "TeamMsgShow.dp", null);
                        dbMsg = teamRecordHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("send_state", Msg.SENTS);
                        values.put("service_id", i.getLongExtra("service_id", -1));
                        //Log.i("jrdchat","返回的——msgid:"+i.getLongExtra("service_id", -1));
                        //Log.i("jrdchat","返回的——sn:"+i.getLongExtra("id", -1));
                        dbMsg.update("TeamRecord", values, "id = ?", new String[]{i.getLongExtra("id", -1) + ""});
                        dbMsg.close();
                        msgDB_Team();
                    }

                    if (PicAddress != null && !PicAddress.equals("")) {
                        Intent intent = new Intent(FirstActivity.this, TransferService.class);
                        intent.putExtra("type", TransferService.UPLOAD_FILE);
                        intent.putExtra("teamid", group);
                        intent.putExtra("phone", linkmanPhone);
                        intent.putExtra("msgid", i.getLongExtra("service_id", -1));
                        intent.putExtra("address", PicAddress);
                        startService(intent);
                        Log.i("jrdchat", "图片缩略图发送成功");
                        /*mUploadFile.startUpload(new TansferFileUp(group, linkmanPhone,
                                i.getLongExtra("service_id", -1), PicAddress));*/
                    }
                    shareSuceess();
                } else {
                    ToastR.setToast(FirstActivity.this, "发送失败");
                    if (single) {
                        linkmanRecordHelper = new LinkmanRecordHelper(FirstActivity.this, myPhone + linkmanPhone + "LinkmanMsgShow.dp", null);
                        dbMsg = linkmanRecordHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("send_state", Msg.SENTF);
                        values.put("service_id", i.getLongExtra("service_id", -1));
                        dbMsg.update("LinkmanRecord", values, "id = ?", new String[]{i.getLongExtra("id", -1) + ""});
                        dbMsg.close();
                        msgDB_Single();
                    } else {
                        teamRecordHelper = new TeamRecordHelper(FirstActivity.this, myPhone + group + "TeamMsgShow.dp", null);
                        dbMsg = teamRecordHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("send_state", Msg.SENTF);
                        values.put("service_id", i.getLongExtra("service_id", -1));
                        dbMsg.update("TeamRecord", values, "id = ?", new String[]{i.getLongExtra("id", -1) + ""});
                        dbMsg.close();
                        msgDB_Team();
                    }
                    new ResponseErrorProcesser(FirstActivity.this, i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void shareSuceess() {
        if (maction != null) {
            if (maction.equals(Intent.ACTION_SEND)) {
                maction = null;
                finish();
            }
        }
    }

    /**
     * find phone from chat list
     * change his status,
     * change icon etc.
     *
     * @param resp
     */
    private void onChatStatusChanged(ProtoMessage.NotifyMsg resp) {
        changespeaking(resp.getChatStatus(), resp.getFriendPhoneNum());
    }

    //****************触摸操作（双指下滑，上滑）***************
    public boolean dispatchTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_UP) {
            //当手指离开的时候
            //edit.setFocusable(false);
            refreshHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    additionLayout.setVisibility(GONE);
                    additionType = 0;
                }
            }, 5);//关于优先级问题，不能随意改
        }
        return super.dispatchTouchEvent(event);
    }
    //*************************************一直开启的监听*********************************************

    @Override
    protected void onResume() {
        Log.v("pocdemo", "onResume");
        // 设置地图参数及事件处理
        if (run) {
            hideSystemNaviBar();

            _OnResume();
            if (call_hungon == 1) {
                CallClick();
            }

            if (mapInitIs) {
//                mMap.onResume();
            }

        }
        run = true;
        pauseIs = false;
        GlobalStatus.setIsFirstPause(false);
        ChatManager.getInstance().setIsFinishing(false);
        super.onResume();
    }

    private void sendImg(String image) {
        try {
            Bitmap bmp = BitmapFactory.decodeFile(image);
            if (bmp == null) {
                ToastR.setToastLong(this, "请确认是否是图片或者图片异常！");
                return;
            }
            saveI(bmp, Msg.TYPE_SENT_IMAGE, image);

        } catch (OutOfMemoryError e) {
            ToastR.setToast(this, "内存不足，无法打开图片");
            clearUnfinishedUploadImage();
            finish();
        }
    }

    private void _OnResume() {
        if (mapInitIs) {
            Log.i("pocdemo", "first activity resume");
            mAudioRecordStatusBroadcast = new AudioRecordStatusBroadcast(this);
            mAudioRecordStatusBroadcast.setReceiver(new MyBroadcastReceiver() {
                @Override
                protected void onReceiveParam(String str) {
                    if (str.equals("1")) {
                        // 指示正在说话
//                        ToastR.setToast(getApplicationContext(), "请说话");
                        changeSpeakSign(true);
                    } else {
                        // 指示不在说话状态，即不是在录音状态。
                        changeSpeakSign(false);
                    }
                }
            });
            if (single) {
                edit.setText((String) SharedPreferencesUtils.get(mContext, linkmanPhone, ""));
                SharedPreferencesUtils.put(this, NotifyProcesser.Call_KEY, linkmanPhone);
            } else {
                edit.setText((String) SharedPreferencesUtils.get(mContext, group + "", ""));
                SharedPreferencesUtils.put(this, NotifyProcesser.Call_KEY, groupName);
            }

//            if (!single && allTeamMemberInfos == null) {
//                getGroupMan(group);
//            }
            mAudioRecordStatusBroadcast.start();

            photo_or_no = false;
            if (single) {
                if (GlobalStatus.equalPhone(linkmanPhone)) {
                    NotificationManager nm = (NotificationManager) (getSystemService(NOTIFICATION_SERVICE));
                    nm.cancel(-1);//消除对应ID的通知
                }
            } else {
                if (GlobalStatus.equalTeamID(group)) {
                    NotificationManager nm = (NotificationManager) (getSystemService(NOTIFICATION_SERVICE));
                    nm.cancel(-1);//消除对应ID的通知
                }

            }
            try {
                filterstatus = new IntentFilter();
                chatStatusReceiver = new ChatStatusReceiver();
                filterstatus.addAction("NotifyProcesser.ChatStatus");
                registerReceiver(chatStatusReceiver, filterstatus);

                IntentFilter filt = new IntentFilter();
                chatCallReceiver = new ChatCallReceiver();
                filt.addAction(NotifyProcesser.Call_ACTION);
                registerReceiver(chatCallReceiver, filt);

                /** 启动录音失败的广播接收器 */
                mAudioBroadcast = new AudioInitailFailedBroadcast(this);
                mAudioBroadcast.setReceiver(new MyBroadcastReceiver() {
                    @Override
                    protected void onReceiveParam(String str) {
                        Log.e("FirstActivity", "收到没有录音权限的广播");
                        ToastR.setToast(FirstActivity.this, "录音失败！请检查录音权限");
                    }
                });
                mAudioBroadcast.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 切换显示是否在录音状态
     */
    private void changeSpeakSign(boolean bRecording) {
        if (bRecording) {
            Log.i("pocdemo", "正在录音++++");

        } else {
            Log.i("pocdemo", "停止录音----");

        }
    }

    //****************触摸操作（双指下滑，上滑）***************

    @Override
    protected void onPause() {
        Log.i("pocdemo", "first activity paused");
        showSystemNaviBar();
        ChatManager.getInstance().hideView();
        if (single) {
            SharedPreferencesUtils.put(mContext, linkmanPhone, edit.getText());
        } else {
            SharedPreferencesUtils.put(mContext, group + "", edit.getText());
        }
        try {
            if (mAudioBroadcast != null) {
                mAudioBroadcast.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAudioBroadcast = null;

        try {
            if (chatStatusReceiver != null) {
                unregisterReceiver(chatStatusReceiver);
            }

            if (chatCallReceiver != null) {
                unregisterReceiver(chatCallReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        chatStatusReceiver = null;

        GlobalStatus.setIsFirstPause(true);
        if (!photo_or_no) {
            if (call_hungon == 1 && showNotification) {
                notification();
            }
        }
        if (sentType != 1) {
//            pttKeyUpSent();
        }

        if (mAudioRecordStatusBroadcast != null) {
            mAudioRecordStatusBroadcast.stop();
        }

        if (mapInitIs) {
//            mMap.onPause();
        }
        pauseIs = true;

        shareSuceess();

        ChatManager.getInstance().hideView();
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (bottomLayoutManager != null && keyCode != KeyEvent.KEYCODE_BACK && keyCode != KeyEvent.KEYCODE_F6 && !pauseIs) {
            //  bottomLayoutManager.show(false);
        }

//        if (keyCode == KeyEvent.KEYCODE_F6) {
//            //Log.i("pocdemo", "key F6 按下");
//            /**
//             TODO:
//             if (当前窗口在会话界面) {
//             if (不在呼叫状态) {
//             发起呼叫;
//             }
//             }
//
//             if (当前在呼叫 状态）{
//             触发说话按钮->按下。
//             }
//             */
//            // pttKeyDownSent();
//        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_F6) {
//            //Log.i("pocdemo", "key F6 弹起");
//            /**
//             TODO:
//             if (当前在呼叫 状态）{
//             触发说话按钮->弹起。
//             }
//             */
//            //pttKeyUpSent();
//        }
        return super.onKeyUp(keyCode, event);
    }

    private void showDialog(final String type) {
        String msg = "";
        if (type.equals(deleteLinkMan)) {
            msg = "确认要删除 " + linkman + " ?";
        } else if (type.equals(dismissTeam)) {
            msg = "确认要解散 " + groupName + " 群组？";
        } else if (type.equals(quitTeam)) {
            msg = "确认要退出 " + groupName + " 群组？";
        } else {
            msg = "确认要关闭聊天室？";
        }
        new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT).setTitle("提示")// 提示框标题
                .setMessage(msg)
                .setPositiveButton("确定", // 提示框的两个按钮
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (type.equals(deleteLinkMan)) {
                                    deleteFriend();
                                } else if (type.equals(dismissTeam)) {
                                    groupDismiss();
                                } else if (type.equals(quitTeam)) {
                                    groupQuit();
                                } else {
                                    forceCloseRoom();
                                }
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

    private void forceCloseRoom() {
        if (hostOk) {
            if (call_hungon == 1) {
                ProtoMessage.AcceptVoice.Builder builder = ProtoMessage.AcceptVoice.newBuilder();
                builder.setRoomID(GlobalStatus.getRoomID());
                MyService.start(mContext, ProtoMessage.Cmd.cmdCloseChatRoom_VALUE, builder.build());
                IntentFilter filter = new IntentFilter();
                filter.addAction(CloseRoomProcesser.ACTION);
                new TimeoutBroadcast(FirstActivity.this, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
                    @Override
                    public void onTimeout() {
                        ToastR.setToast(FirstActivity.this, "连接超时");
                    }

                    @Override
                    public void onGot(Intent i) {
                        if (i.getIntExtra("error_code", -1) ==
                                ProtoMessage.ErrorCode.OK.getNumber()) {
                            hangup.setVisibility(GONE);
                            call.setVisibility(VISIBLE);
                            GlobalStatus.clearChatRoomMsg();
                            call_hungon = 0;
                            button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.FloatingActionButton_false)));
                            button.setImageResource(R.drawable.speak);
                            saveMsgRemind("房间被成功关闭");
                            if (single) {
                                name.setText(linkman);
                                sta_num.setVisibility(GONE);
                            } else {
                                name.setText(groupName);
                                sta_num.setText("人数:" + mansNum);
                            }
                            if (single) {
                                msgDB_Single();
                            } else {
                                msgDB_Team();
                            }
                            reGridView();
                            speakingUser.setName("");
                            changeTitle();
                        } else {
                            fail(i.getIntExtra("error_code", -1));
                        }
                    }
                });
            } else {
                ToastR.setToast(this, "您并不在此房间中！");
            }
        } else {
            ToastR.setToast(this, "不是房间呼起人！");
        }
    }

    @OnClick({R.id.voice, R.id.btn_return, R.id.prefix_camera, R.id.rear_camera, R.id.picture_in_picture, R.id.goto_map, R.id.do_not_disturb, R.id.btn_add})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_return:
                finish();
                break;
            case R.id.prefix_camera:
                //前置对讲
                prefixCamera.setTextColor(getResources().getColor(R.color.match_btn_bg_press));
                rearCamera.setTextColor(getResources().getColor(R.color.white));
                pictureInPicture.setTextColor(getResources().getColor(R.color.white));
                voice.setTextColor(getResources().getColor(R.color.white));
                doNotDisturb.setTextColor(getResources().getColor(R.color.white));

                GlobalStatus.setIsVideo(true);
                GlobalStatus.changeChatStatusInfo();
                changeTitle();
                if (GlobalStatus.checkSpeakPhone(getMyPhone(), getRoomId())) {
                    VoiceHandler.startRTMP(mContext, GlobalStatus.getCurRtmpAddr());
                }
                break;
            case R.id.rear_camera:
                //后置对讲
                prefixCamera.setTextColor(getResources().getColor(R.color.white));
                rearCamera.setTextColor(getResources().getColor(R.color.match_btn_bg_press));
                pictureInPicture.setTextColor(getResources().getColor(R.color.white));
                voice.setTextColor(getResources().getColor(R.color.white));
                doNotDisturb.setTextColor(getResources().getColor(R.color.white));
                //按下操作

                SharedPreferencesUtils.put(mContext, "pttKeyDown", true);
                pttKeyDownSent();


                break;
            case R.id.picture_in_picture:
                //画中画对讲
                prefixCamera.setTextColor(getResources().getColor(R.color.white));
                rearCamera.setTextColor(getResources().getColor(R.color.white));
                pictureInPicture.setTextColor(getResources().getColor(R.color.match_btn_bg_press));
                voice.setTextColor(getResources().getColor(R.color.white));
                doNotDisturb.setTextColor(getResources().getColor(R.color.white));

                //抬起操作
                SharedPreferencesUtils.put(mContext, "pttKeyDown", false);
                pttKeyUpSent();
                break;
            case R.id.voice:
                //语音对讲
                prefixCamera.setTextColor(getResources().getColor(R.color.white));
                rearCamera.setTextColor(getResources().getColor(R.color.white));
                pictureInPicture.setTextColor(getResources().getColor(R.color.white));
                voice.setTextColor(getResources().getColor(R.color.match_btn_bg_press));
                doNotDisturb.setTextColor(getResources().getColor(R.color.white));


                GlobalStatus.setIsVideo(false);
                changeTitle();
                DvrService.start(MyApplication.getContext(), RESClient.ACTION_STOP_RTMP, null);
                DvrService.start(MyApplication.getContext(), RESClient.ACTION_STOP_PLAY, null);
                break;
            case R.id.goto_map:
                // 地图查看
                Intent mapIntent = new Intent(mContext, TeamMemberLocationActivity.class);
                mapIntent.putExtra("team_id",group);
                startActivity(mapIntent);
                break;
            case R.id.do_not_disturb:
                //免打扰
                prefixCamera.setTextColor(getResources().getColor(R.color.white));
                rearCamera.setTextColor(getResources().getColor(R.color.white));
                pictureInPicture.setTextColor(getResources().getColor(R.color.white));
                voice.setTextColor(getResources().getColor(R.color.white));
                doNotDisturb.setTextColor(getResources().getColor(R.color.match_btn_bg_press));

                HungupClick();
                break;
            case R.id.btn_add:
                //添加好友
                Intent intent = new Intent(mContext, SelectMemberActivity.class);
                intent.putExtra("teamID", group);
                startActivity(intent);
                break;

        }
    }



    //*************************************一直开启的监听*********************************************
    class UploadReceiiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            long msgid = intent.getLongExtra("msgid", -1);
            for (Msg msg : msgList) {
                if (msg.getMsgID() == msgid) {
                    if (intent.getBooleanExtra("uploading", false)) {
                        msg.setPercent(intent.getIntExtra("percent", -1));
                        adapter.notifyDataSetChanged();
                    } else {
                        if (single) {
                            msgDB_Single();
                        } else {
                            msgDB_Team();
                        }
                    }
                    break;
                }
            }
        }
    }

    class CloseRoomReceiiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (roomId == intent.getLongExtra("roomID", -1)) {
                closeRoom(true);
            }
        }
    }

    class RefreshReceiiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("pocdemo", "refreshReceiver action:" + intent.getAction());
            if (call_hungon == 1) {
                closeRoom(false);
            }
        }
    }

    private void closeRoom(boolean isClose) {
        if (GlobalStatus.getChatRoomMsg() != null && (GlobalStatus.equalPhone(linkmanPhone) || GlobalStatus.equalTeamID(group))) {
            return;
        }
        Log.v("wsDvr", "closeRoom:" + isClose);
        Log.v("wsDvr", "finish");
        hangup.setVisibility(GONE);
        call.setVisibility(VISIBLE);
        if (isClose) {
            videoLayout.hideVideo();
            finish();
        }
        call_hungon = 0;
        button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.FloatingActionButton_false)));
        button.setImageResource(R.drawable.speak);
        saveMsgRemind(getString(R.string.close_room_success));
        if (single) {
            name.setText(linkman);
            sta_num.setVisibility(GONE);
        } else {
            name.setText(groupName);
            sta_num.setText(getString(R.string.group_count) + mansNum);
        }
        if (single) {
            msgDB_Single();
        } else {
            msgDB_Team();
        }
        reGridView();
        speakingUser.setName("");
        changeTitle();
//        mMap.refreshMapLocalData(userList, call_hungon == 1);
    }

    class ChatStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ProtoMessage.NotifyMsg resp = (ProtoMessage.NotifyMsg) intent.getSerializableExtra("chat_status");

            Log.i("chatchat", "receiver come");
            if (resp == null) {
                Log.i("chatjrd", "resp is null");
            } else {
                if (roomId == resp.getRoomID()) {
                    FirstActivity.this.onChatStatusChanged(resp);
                } else {
                    Log.i("chatjrd", "不是本房间的消息: 房间号：" + roomId + ", 获得的房间号： " + resp.getRoomID());
                }
            }
        }
    }

    class ChatCallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String phone = intent.getStringExtra("linkmanPhone");
            String name = intent.getStringExtra("group_name");
            if ((phone != null && phone.equals(linkmanPhone)) || (name != null && name.equals(groupName))) {
                Log.i("chatchat", "启动对讲");
                r = (ProtoMessage.ChatRoomMsg) intent.getSerializableExtra("member");
                call_hungon = intent.getExtras().getInt("data");
                initHungonData();
            } else {
                Log.i("chatchat", "不是当前对讲页面");
            }
        }
    }


    class GetMsgReceiiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String phone = intent.getStringExtra("phone");
            long gotGroup = intent.getLongExtra("group", 0);
            MsgRecordHelper msgRecordHelper = new MsgRecordHelper(FirstActivity.this, myPhone + "MsgShow.dp", null);
            SQLiteDatabase db = msgRecordHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("new_msg", 0);
            if (gotGroup == 0) {
                if (phone.equals(linkmanPhone)) {
                    if (!pauseIs) {
                        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        nm.cancel(0);//消除对应ID的通知
                    }
                    msgDB_Single();
                    db.update("Msg", values, "phone = ? and msg_from = ?", new String[]{linkmanPhone, 0 + ""});

                }
            } else if (gotGroup == group) {

                if (intent.getIntExtra("msg_type", -1) == 4) {
//                    getGroupMan(group);
                    refreshHandler.removeCallbacks(mRefreshTeamRunnable);
                    refreshHandler.postDelayed(mRefreshTeamRunnable, 2000);
                } else {
                    if (!pauseIs) {
                        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        nm.cancel(0);//消除对应ID的通知
                    }
                    msgDB_Team();
                    db.update("Msg", values, "group_id = ?", new String[]{group + ""});
                }

            }
            db.close();
        }
    }


    private Runnable mRefreshTeamRunnable = new Runnable() {
        @Override
        public void run() {
            Message message = new Message();
            message.what = 10;
            refreshHandler.sendMessage(message);
        }
    };

    public TeamInfo getTeamInfo() {
        TeamInfo mTeamInfo = null;
        try {
            DBManagerTeamList db = new DBManagerTeamList(mContext, true, DBTableName.getTableName(mContext, DBHelperTeamList.NAME));
            mTeamInfo = db.getTeamInfo(group);
            db.closeDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mTeamInfo;
    }

    public void checkVideoPermission() {
        if (Build.VERSION.SDK_INT >= 19) {
            List<String> mList = new ArrayList<String>();
            mList.add(PermissionUtil.PERMISSIONS_STORAGE);
            mList.add(PermissionUtil.PERMISSIONS_CAMERA);
            mList.add(PermissionUtil.PERMISSIONS_RECORD_AUDIO);
            List<String> mDeniedPermissionList = mPermissionUtil.findDeniedPermissions(FirstActivity.this, mList);
            if (mDeniedPermissionList != null && mDeniedPermissionList.size() > 0) {
                mPermissionUtil.requestPermission(FirstActivity.this, MY_PERMISSIONS_REQUEST_VIDEO, FirstActivity.this, mDeniedPermissionList.get(0), mDeniedPermissionList.get(0));
            } else {
                startVideoCall();
            }
        }
    }

    public void startVideoCall() {
        ProtoMessage.StartVoiceMsg.Builder builder = ProtoMessage.StartVoiceMsg.newBuilder();
        if (single) {
            builder.setToUserPhone(linkmanPhone);
        } else {
            builder.setTeamID(group);
        }
        MyService.start(FirstActivity.this, ProtoMessage.Cmd.cmdLiveVideoCall.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(LiveVideoCallProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(FirstActivity.this, filter, getBroadcastManager());

        b.startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(FirstActivity.this, "超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) == ProtoMessage.ErrorCode.OK.getNumber()) {
                    if (single) {
                        VideoCallActivity.startActivity(mContext, linkmanPhone);
                    } else {
                        VideoCallActivity.startActivity(mContext, group, (ArrayList<TeamMemberInfo>) allTeamMemberInfos);
                    }
                } else {
                    ToastR.setToast(FirstActivity.this, "呼叫失败");
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void rightMoveToLeft() {
        final long downTime = SystemClock.currentThreadTimeMillis();
        WindowManager wm = this.getWindowManager();
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();
        MotionEvent down = MotionEvent.obtain(downTime, SystemClock.currentThreadTimeMillis(), MotionEvent.ACTION_DOWN, width, height / 2, 0);
        viewPager.dispatchTouchEvent(down);
        MotionEvent move;
        for (int i = 1; i < width; i++) {
            move = MotionEvent.obtain(downTime, SystemClock.currentThreadTimeMillis(), MotionEvent.ACTION_MOVE, width - i, height / 2, 0);
            viewPager.dispatchTouchEvent(move);
        }
        MotionEvent up = MotionEvent.obtain(downTime, SystemClock.currentThreadTimeMillis(), MotionEvent.ACTION_UP, 0, height / 2, 0);
        viewPager.dispatchTouchEvent(up);
    }

    private void leftMoveToRight() {
        final long downTime = SystemClock.currentThreadTimeMillis();
        WindowManager wm = this.getWindowManager();
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();
        MotionEvent down = MotionEvent.obtain(downTime, SystemClock.currentThreadTimeMillis(), MotionEvent.ACTION_DOWN, 0, height / 2, 0);
        viewPager.dispatchTouchEvent(down);
        MotionEvent move;
        for (int i = 1; i < width; i++) {
            move = MotionEvent.obtain(downTime, SystemClock.currentThreadTimeMillis(), MotionEvent.ACTION_MOVE, i, height / 2, 0);
            viewPager.dispatchTouchEvent(move);
        }
        MotionEvent up = MotionEvent.obtain(downTime, SystemClock.currentThreadTimeMillis(), MotionEvent.ACTION_UP, width, height / 2, 0);
        viewPager.dispatchTouchEvent(up);
    }

    public String getMyPhone() {
        return myPhone;
    }

    public long getRoomId() {
        return roomId;
    }

    public String getChatName() {
        if (single) {
            return linkman;
        } else {
            return groupName;
        }
    }

    private void hideSystemNaviBar() {
        /*Intent intent = new Intent(RESClient.ACTION_HIDE_NAVI_BAR);
        sendBroadcast(intent);*/
        Settings.System.putInt(mContext.getContentResolver(), NAVIGATION_BAR_CTRL, 0);
    }

    private void showSystemNaviBar() {
        /*Intent intent = new Intent(RESClient.ACTION_SHOW_NAVI_BAR);
        sendBroadcast(intent);*/
        Settings.System.putInt(mContext.getContentResolver(), NAVIGATION_BAR_CTRL, 1);
    }


}
