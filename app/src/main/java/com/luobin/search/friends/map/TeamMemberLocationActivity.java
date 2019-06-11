package com.luobin.search.friends.map;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.FriendLocationStatus;
import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.MainActivity;
import com.example.jrd48.chat.MapMenu;
import com.example.jrd48.chat.MarkerMove;
import com.example.jrd48.chat.MyViewPager;
import com.example.jrd48.chat.PicTool.LetterTileDrawable;
import com.example.jrd48.chat.SQLite.SQLiteTool;
import com.example.jrd48.chat.SettingRW;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.TeamMemberInfo;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.User;
import com.example.jrd48.chat.amap.MapMemberListDialog;
import com.example.jrd48.chat.amap.OnItemClickListener;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.example.jrd48.chat.friend.FriendsDetailsActivity;
import com.example.jrd48.chat.group.DBHelperTeamList;
import com.example.jrd48.chat.group.DBManagerTeamList;
import com.example.jrd48.chat.group.GroupMemberDetailsActivity;
import com.example.jrd48.chat.group.ShowAllTeamMemberActivity;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.chat.location.ServiceCheckUserEvent;
import com.example.jrd48.chat.permission.PermissionUtil;
import com.example.jrd48.chat.search.Cn2Spell;
import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyBroadcastReceiver;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.RestartLocationBroadcast;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.FriendLocationChangedProcesser;
import com.example.jrd48.service.protocol.root.GetFriendLocationProcesser;
import com.example.jrd48.service.protocol.root.SearchFriendProcesser;
import com.example.jrd48.service.protocol.root.StopGetLocationProcesser;
import com.example.jrd48.service.protocol.root.TeamMemberProcesser;
import com.luobin.dvr.R;
import com.luobin.utils.ButtonUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/8/16.
 */

public class TeamMemberLocationActivity extends BaseActivity implements PermissionUtil.PermissionCallBack, View.OnClickListener
        , AMap.OnMyLocationChangeListener, AMap.OnMarkerClickListener ,AMap.OnMapTouchListener{
    protected PermissionUtil mPermissionUtil;
    int MY_PERMISSIONS_REQUEST_LOCATION = 10021;
    List<AppliedFriends> listMembersCache;
    private Context mContext;
    private String myPhone;
    private String linkman;
    private String linkNickName;
    private long teamId;
    private String teamName;
    private String teamDesc;
    private int teamPriority;
    private int memberRole;
    TeamInfo mTeamInfo;

    private TextView mTextViewName;
    private TextView mTextViewSume;
    private ImageView mImageViewBack;
    private ArrayList<TeamMemberInfo> mTeamMemberInfoList;
    private List<User> userList = new ArrayList<>();


    private AMap aMap;
    private MapView mapView;
    private MyLocationStyle myLocationStyle;
    private MarkerMove markerMove = new MarkerMove();
    private HashMap<String, Marker> mMarkers = new HashMap<>();
    private Marker marker;
    private boolean isTrackPhone = false;

    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private ImageView mImageLocation;
    private ImageView mClearTrack;
    private ImageView mSetTrack;
    private TextView textViewDiloag;

    private String mTrackPhoneNum = "";

    private double latitude;
    private double longitude;
    private List<FriendLocationStatus> friendLocationStatusesList = new ArrayList<>();

    private RestartLocationBroadcast mRelocationBroadcast;
    private float mMapZoomLevel;
    MapMenu mMapMenu;
    private boolean single;
    private String linkmanPhone = "";
    MapMemberListDialog mMapMemberListDialog;
    private String userNameMe;
    private boolean bConnected;
    private ArrayList<MarkerOptions> mMarkerOptionsList = new ArrayList<MarkerOptions>();
    private List<Marker> mMarkersList;
    private List<String> phoneList = new ArrayList<String>();
    //    private final String trackPhone = "trackPhone";
    private String key;

    private Button btnToChat;
    private MyViewPager viewPager;
    private boolean isCallHungon;
    SettingRW config;
    boolean showNotification = true;
    private String changeType = "Group";
    private static final int DISMISS_OR_QUIT = 5;
    public static final int REFRESH_REMARK = 6;
    public static final int REFRESH_TEAM_REMARK = 7;
    AppliedFriends mViewFriendsMsg;
    private boolean isCancelTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_location_map);
        mContext = this;
        mPermissionUtil = PermissionUtil.getInstance();
        mPermissionUtil.requestPermission(this, MY_PERMISSIONS_REQUEST_LOCATION, this, PermissionUtil.PERMISSIONS_LOCATION, PermissionUtil.PERMISSIONS_LOCATION);
        getlistMembersCache();
        getIntentMsg();
        initView();
        initBroadcast();
        initMapListener();
        mapView.onCreate(savedInstanceState);
    }

    private void initBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.FRIEND_ACTION);
        filter.setPriority(Integer.MAX_VALUE);
        registerReceiver(myReceiver, filter);
    }

    @Override
    protected void onResume() {
        setMapZoomLevel();
        Log.i("pocdemo", "默认的地图缩放等级: " + mMapZoomLevel);
        // 改为在此请求好友位置
        startGetLocation();
        start();
        // 网络恢复时自动观察群组成员位置
        mRelocationBroadcast = new RestartLocationBroadcast(mContext);
        mRelocationBroadcast.setReceiver(new MyBroadcastReceiver() {
            @Override
            protected void onReceiveParam(String str) {
                // TODO:
                startGetLocation();
            }
        });
        mRelocationBroadcast.start();
        registerBoradcastReceiver();
        mapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        stopGetLocation();
        stop();
        if (TextUtils.isEmpty(mTrackPhoneNum)){
            mTrackPhoneNum = myPhone;
        }
        SharedPreferencesUtils.put(mContext, key, mTrackPhoneNum);
        try {
            Log.i("pocdemo", "地图：save zoom level: " + mMapZoomLevel);
            SettingRW rw = new SettingRW(mContext);
            rw.setMapZoomLevel(mMapZoomLevel);
            rw.saveMapZoomLevel();

            if (mBroadcastReceiver != null) {
                mContext.unregisterReceiver(mBroadcastReceiver);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        setTrackPhoneNum("");
        mapView.onPause();
        if (mRelocationBroadcast != null) {
            mRelocationBroadcast.stop();
            mRelocationBroadcast = null;
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        try {
            if (myReceiver != null) {
                unregisterReceiver(myReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapView.onDestroy();
        super.onDestroy();
    }

    private void initView() {
        mImageViewBack = (ImageView) findViewById(R.id.iv_back);
        mImageViewBack.setOnClickListener(this);
        mapView = (MapView) findViewById(R.id.mapView1);
        mImageLocation = (ImageView) findViewById(R.id.img_location);
        mImageLocation.setOnClickListener(this);

        mSetTrack = (ImageView) findViewById(R.id.img_set_track);
        mSetTrack.setOnClickListener(this);

        textViewDiloag = (TextView) findViewById(R.id.tv_show);
    }

    private void initMapListener() {
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.drawable.my_location));// 设置小蓝点的图标
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        if (aMap == null) {
            return;
        }
        aMap.setMyLocationStyle(myLocationStyle);
      //  aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false

        aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
        aMap.getUiSettings().setCompassEnabled(true);

        //TODO: 缩放以后把缩放级别保存到 SettingRW 中
        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                mMapZoomLevel = cameraPosition.zoom;
                Log.i("myAmap", "地图缩放级别: " + cameraPosition.zoom);
            }
        });

        //设置SDK 自带定位消息监听
//        aMap.setOnMyLocationChangeListener(this);
        aMap.setOnMarkerClickListener(this);
        aMap.setOnMapTouchListener(this);
    }

    @Override
    public void onTouch(MotionEvent motionEvent) {
//        getMapCenterPoint();
    }


    private void progressDialogShow(){
        if (textViewDiloag != null) {
            textViewDiloag.setVisibility(View.VISIBLE);
        }
    }

    private void progressDialogDismiss(){
        if (textViewDiloag != null) {
            textViewDiloag.setVisibility(View.GONE);
        }
    }

    /**
     *  2017/10/11
     * 根据触摸滑动判断是否取消跟踪
     */
    public void getMapCenterPoint() {
        int left = mapView.getLeft();
        int top = mapView.getTop();
        int right = mapView.getRight();
        int bottom = mapView.getBottom();
        // 获得屏幕中心点位置的经纬度
        int x = (int)(mapView.getX() + (right - left) / 2);
        int y = (int)(mapView.getY() + (bottom - top) / 2)-15;
        Log.d("myAmap","left："+left + " top:"+top+" right："+right + " bottom:"+bottom);
        Log.d("myAmap","x："+x + " y:"+y+ "  mapView.getX():"+mapView.getX()+" mapView.getY():"+mapView.getY());
        Projection projection = aMap.getProjection();
        LatLng pt = projection.fromScreenLocation(new Point(x, y));
        Log.d("myAmap","获取屏幕经纬度："+pt.latitude + " longitude:"+pt.longitude);
        Log.d("myAmap","获取自己经纬度："+latitude + " longitude:"+longitude);
        Log.d("myAmap","-----差值："+Math.abs(pt.latitude-latitude) + " longitude:"+Math.abs(pt.longitude-longitude));
        if (TextUtils.isEmpty(mTrackPhoneNum)){
            if (!isCancelTrack){
                int zoom = Integer.parseInt(new DecimalFormat("0").format(mMapZoomLevel));
                double interval = 0.0002;
                Log.e("myAmap","------------------"+interval);
                if (Math.abs(pt.latitude-latitude)> interval || Math.abs(pt.longitude-longitude)>interval){
                    isCancelTrack = true;
                    Log.d("myAmap","取消实时定位");
                }
            }
        }else {
            Marker marker = mMarkers.get(mTrackPhoneNum);
            if (marker == null){
                Log.e("myAmap","没有找到需要取消的跟踪目标");
                return;
            }
            LatLng mLatLng = marker.getPosition();
            int zoom = Integer.parseInt(new DecimalFormat("0").format(mMapZoomLevel));
            double interval = 0.0002;
            Log.e("myAmap","------------------"+interval+" zoom:"+zoom);
            if (Math.abs(pt.latitude-mLatLng.latitude)> interval || Math.abs(pt.longitude-mLatLng.longitude)>interval){
                Log.d("myAmap","取消跟踪");
                cancelTrack();
                SharedPreferencesUtils.put(mContext, key, mTrackPhoneNum);
            }
        }
    }

    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(FriendLocationChangedProcesser.ACTION);
        //注册广播
        registerReceiver(mBroadcastReceiver, myIntentFilter);

    }

    private void getIntentMsg() {
        SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        myPhone = preferences.getString("phone", "");
        userNameMe = preferences.getString("name", myPhone);
        Intent intent = getIntent();
        linkman = intent.getStringExtra("linkmanName");
        linkNickName = intent.getStringExtra("linkNickName");
        linkmanPhone = intent.getStringExtra("linkmanPhone");
        teamId = intent.getLongExtra("team_id", 0);
        teamName = intent.getStringExtra("team_name");
        teamDesc = intent.getStringExtra("desc");
        memberRole = intent.getExtras().getInt("type");
        teamPriority = intent.getIntExtra("priority", 0);
        if (teamId != 0) {
            single = false;
            convertViewTeamMember(SQLiteTool.getAllTeamMembers(mContext, teamId));
            getGroupMan(teamId);
        } else {
            single = true;
            int sex = intent.getIntExtra("linkmanSex", 0);
            userT(linkman, ProtoMessage.ChatStatus.csOk_VALUE, linkmanPhone, sex,linkNickName);
        }

    }

    public void getGroupMan(final long id) {
        mTeamMemberInfoList = SQLiteTool.getAllTeamMembers(mContext, id);
        if (mTeamMemberInfoList != null && mTeamMemberInfoList.size() > 0) {
            convertViewTeamMember(mTeamMemberInfoList);
            return;
        }
//        mansNum = userList.size();

        ProtoMessage.AcceptTeam.Builder builder = ProtoMessage.AcceptTeam.newBuilder();
        builder.setTeamID(id);
        MyService.start(mContext, ProtoMessage.Cmd.cmdGetTeamMember.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(TeamMemberProcesser.ACTION);
        TimeoutBroadcast x = new TimeoutBroadcast(mContext, filter, getBroadcastManager());
        x.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
//                    TeamMemberInfoList list = i.getParcelableExtra("get_teamMember_list");
                    convertViewTeamMember(SQLiteTool.getAllTeamMembers(mContext, id));
                    //	ToastR.setToast(FirstActivity.this, "获取群成员成功");
                } else {
                    new ResponseErrorProcesser(mContext, i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    public TeamInfo getTeamInfo() {
        TeamInfo mTeamInfo = null;
        try {
            DBManagerTeamList db = new DBManagerTeamList(mContext, true, DBTableName.getTableName(mContext, DBHelperTeamList.NAME));
            mTeamInfo = db.getTeamInfo(teamId);
            db.closeDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mTeamInfo;
    }

    private void convertViewTeamMember(ArrayList<TeamMemberInfo> teamMemberInfos) {
        userList.clear();
        mTeamMemberInfoList = teamMemberInfos;//保存群成员列表
        String memberName;
        int sex;

        for (TeamMemberInfo in : teamMemberInfos) {
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

            if (myPhone.equals(in.getUserPhone())) {
//                mTeamInfo.setMemberRole(in.getRole());
                memberRole = in.getRole();
            }

            if (memberName == null || memberName.equals("")) {
                memberName = in.getNickName();
            }
            if (memberName == null || memberName.equals("")) {
                memberName = in.getUserName();
            }
            if (memberName == null || memberName.equals("")) {
                memberName = in.getUserPhone();
            }
            Bitmap bitmap = GlobalImg.getImage(mContext, in.getUserPhone());
            if (bitmap == null) {
                getUserFace(in.getUserPhone());
            }
            int status = ProtoMessage.ChatStatus.csOk_VALUE;
            userT(in.getUserName(), status, in.getUserPhone(), sex,memberName);
        }
        // 排序(实现了中英文混排)
        TeamMemberPinyinComparator comparator = new TeamMemberPinyinComparator();
        Collections.sort(userList, comparator);

//        mTMInfo.clear();
//        for (TeamMemberInfo te : allTeamMemberInfos) {
//            for (AppliedFriends af : listMembersCache) {
//                if (af.getPhoneNum().equals(te.getUserPhone())) {
//                    mTMInfo.add(te);
//                    break;
//                }
//            }
//        }
//        adapterU.refresh(userList);
    }

    public void userT(String name, int state, String phone, int sex,String nickName) {
        User user = new User(state, name, phone, sex,nickName);
        userList.add(user);
    }

    public void getUserFace(String memberName) {
        ProtoMessage.SearchUser.Builder builder = ProtoMessage.SearchUser.newBuilder();
        builder.setPhoneNum(memberName);
        builder.setOnlyPhoneNum(true);
        MyService.start(mContext, ProtoMessage.Cmd.cmdSearchUser.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(SearchFriendProcesser.ACTION);
        new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {

            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    AppliedFriends aply = i.getParcelableExtra("search_user");
                    if (aply.getPhoneNum() == null || aply.getPhoneNum().length() <= 0) {
                        ToastR.setToast(mContext, "未找到该用户");
                    } else {
                    }
                } else {
                    Log.e("jim","team member userFace  code:"+i.getIntExtra("error_code", -1));
                  //  new ResponseErrorProcesser(mContext, i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //权限申请结果
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            mPermissionUtil.requestResult(this, permissions, grantResults, this, PermissionUtil.PERMISSIONS_LOCATION);
        }
    }

    @Override
    public void onPermissionSuccess(String type) {

    }

    @Override
    public void onPermissionReject(String strMessage) {
        if (strMessage.equals(PermissionUtil.PERMISSIONS_LOCATION)) {
            ToastR.setToastLong(mContext, "[ 定位 ] 权限已经拒绝，请到手机管家或者系统设置里授权");
        }
    }

    @Override
    public void onPermissionFail(String failType) {
        ToastR.setToastLong(mContext, "[ 定位 ] 权限设置失败，请到手机管家或者系统设置里授权");
    }

    private void getlistMembersCache() {
        //获取好友列表
        try {
            DBManagerFriendsList db = new DBManagerFriendsList(mContext, DBTableName.getTableName(mContext, DBHelperFriendsList.NAME));
            listMembersCache = db.getFriends(false);
            db.closeDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                resultData("");
                break;
            case R.id.img_location:

                Marker marker = mMarkers.get(myPhone);
                if (marker != null){
                    marker.setToTop();
                }
                if (mTrackPhoneNum != null && mTrackPhoneNum.length() > 0) {
                    View bitmap = getViewBorderColor(mTrackPhoneNum, false);
                    if (!mTrackPhoneNum.equals(myPhone)) {
                        setMarkerIcon(bitmap);
                    }
                }
                mTrackPhoneNum = myPhone;
                resetMarker(marker,myPhone,0);
//                if (marker == null) {
//                    ToastR.setToast(mContext, "没有找到需要定位目标");
//                    return;
//                }
//                moveCameraToPhone(marker, myPhone, false);

                break;
            case R.id.img_set_track:
                selecteTrack();
                break;
            default:
                break;
        }
    }

    private void setMarkerIcon(View bitmap) {
        Marker markerTrack = mMarkers.get(mTrackPhoneNum);
        if (markerTrack != null) {
            markerTrack.setIcon(BitmapDescriptorFactory.fromView(bitmap));
        } else {
            Log.w("map","marker is null");
        }
    }


    //选择跟踪目标
    private void selecteTrack() {

        if (aMap == null) {
            ToastR.setToast(mContext, "当前不能选择跟踪目标");
            return;
        }
        if (userList.size() <= 0) {
            ToastR.setToastLong(mContext, "没有获取到群成员列表信息");
            return;
        }
        if (mMapMemberListDialog == null) {
            mMapMemberListDialog = new MapMemberListDialog();
        }
        mMapMemberListDialog.showDialog(mTrackPhoneNum, mContext, myPhone, userNameMe, single,
                userList, isCallHungon, new OnItemClickListener() {
                    @Override
                    public void onResult(User user) {

                        if (!isNetworkConnected(mContext)) {
                            ToastR.setToastLong(mContext, "请检测网络是否连接！");
                            return;
                        }

                        Marker marker = mMarkers.get(user.getPhone());
                        if (marker == null) {
                            ToastR.setToast(mContext, "没有找到跟踪目标");
                            return;
                        }
                        //设置marker显示在最上层
                        marker.setToTop();
                        if (myPhone.equals(user.getPhone())) {
                            if (mTrackPhoneNum != null && mTrackPhoneNum.equals(myPhone)) {
                                cancelTrack();
                            }else {
                                resetMarker(marker, user.getPhone(), 0);
                            }
                        } else if (mTrackPhoneNum == null || (!user.getPhone().equals(myPhone))) {
                            if (mTrackPhoneNum != null && mTrackPhoneNum.length() > 0) {
                                View view = getViewBorderColor(mTrackPhoneNum, false);
                                setMarkerIcon(view);
                            }
                            String name = user.getNickName();
                            if (TextUtils.isEmpty(name)){
                                name = user.getName();
                            }
                            if (mTrackPhoneNum.equals(user.getPhone())) {
//                                ToastR.setToast(mContext, "取消跟踪 " + name + " " + user.getPhone());
                                cancelTrack();
                            } else {
                                ToastR.setToast(mContext, "跟踪 " + name + " " + user.getPhone());
                                isCancelTrack = false;
                                setTrackPhoneNum(user.getPhone());
                                moveCameraToPhone(marker, mTrackPhoneNum, true);
                            }
                        }
                        setTrackImageViewIcon(mTrackPhoneNum);
                    }

                    @Override
                    public void onClickButton() {
                        cancelTrack();
                    }
                });
    }

    /*
   取消跟踪
    */
    public void cancelTrack() {
        isCancelTrack = true;
        if (mTrackPhoneNum != null && mTrackPhoneNum.length() > 0) {
            View view = getViewBorderColor(mTrackPhoneNum, false);
            setMarkerIcon(view);
            ToastR.setToast(mContext, "取消跟踪");
        } else {
            ToastR.setToast(mContext, "没有跟踪目标");
        }
        setTrackPhoneNum("");
    }

    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String phoneNum = (String) marker.getObject();
        if (phoneNum == null) {
            // TODO: 点击我的图标
            return true;
        }
        if (!single) {
            resetMarker(marker, phoneNum, 0);
        }
        return false;
    }

    @Override
    public void onMyLocationChange(Location location) {
        // 定位回调监听
        if (location != null) {
            Log.i("myAmap", "onMyLocationChange 定位成功， lat: " + location.getLatitude() + " lon: " + location.getLongitude());
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Bundle bundle = location.getExtras();
            if (bundle != null) {
                int errorCode = bundle.getInt(MyLocationStyle.ERROR_CODE);
                String errorInfo = bundle.getString(MyLocationStyle.ERROR_INFO);
                // 定位类型，可能为GPS WIFI等，具体可以参考官网的定位SDK介绍
                int locationType = bundle.getInt(MyLocationStyle.LOCATION_TYPE);

                /*
                errorCode
                errorInfo
                locationType
                */
//                addMarkersToMap();
                Log.i("myAmap", "定位信息， code: " + errorCode + " errorInfo: " + errorInfo + " locationType: " + locationType);
            } else {
                Log.e("myAmap", "定位信息， bundle is null ");
            }
        } else {
            Log.e("myAmap", "定位失败");
        }
    }

    class TeamMemberPinyinComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            User contact1 = (User) o1;
            User contact2 = (User) o2;

            String str = contact1.getName();
            String str3 = contact2.getName();

            String str1 = Cn2Spell.getPinYin(str);
            String str2 = Cn2Spell.getPinYin(str3);
//        Log.i("log", "str1:  " + str1 + "-----------------str2:  " + str2);
            int flag = str1.compareTo(str2);

            return flag;
        }
    }

    //地图相关
    private void moveCameraToPhone(Marker marker, String phoneNum, boolean isTrackPhone) {

        View view = getViewBorderColor(phoneNum, isTrackPhone);
        if (BitmapDescriptorFactory.fromView(view) != null) {
            marker.setIcon(BitmapDescriptorFactory.fromView(view));
        }
        aMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
    }

    //获取头像边框颜色
    public View getViewBorderColor(String userPhone, boolean isTrackPhone) {

//        isOrNoShowCrearTrack();

        View view = getLayoutInflater().inflate(R.layout.marker_icon_layout, null);
        ImageView img_user = (ImageView) view.findViewById(R.id.badge);
        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.layout);
        ImageView img_mylocation = (ImageView) view.findViewById(R.id.img_mylocation);

        if (isTrackPhone) {
            ViewGroup.LayoutParams params = layout.getLayoutParams();
            layout.setBackground(getResources().getDrawable(R.drawable.location_face_frame));
//            params.height = dip2px(mContext, 55);
//            params.width = dip2px(mContext, 55);
            layout.setLayoutParams(params);
            if (userPhone.equals(myPhone)) {
                img_mylocation.setBackground(getResources().getDrawable(R.drawable.my_location_light));
            }
        }

        if (userPhone.equals(myPhone)) {
            layout.setVisibility(View.GONE);
            img_mylocation.setVisibility(View.VISIBLE);
        }

        String image = userPhone;
        Bitmap bm = GlobalImg.getImage(mContext, image);
        if (bm != null) {
            img_user.setImageBitmap(bm);
        } else {
            String str = "1";
            for (int i = 0; i < userList.size(); i++) {
                if (userList.get(i).getPhone().equals(userPhone)) {
                    if (userList.get(i).getNickName() != null && userList.get(i).getNickName().length() > 0) {
                        str = userList.get(i).getNickName();
                    } else {
                        str = userList.get(i).getName();
                    }
                    break;
                }
            }
            LetterTileDrawable drawable = new LetterTileDrawable(getResources());
            drawable.setContactDetails(str, str);
            Bitmap bmp = FriendFaceUtill.drawableToBitmap(drawable);
            img_user.setImageBitmap(bmp);
        }
        return view;
    }

    private void resetMarker(Marker marker, String phoneNum, int index) {
        String name = "";
        if (userList != null) {
            for (int i = 0; i < userList.size(); i++) {
                if (userList.get(i).getPhone().equals(phoneNum)) {
                    if (TextUtils.isEmpty(userList.get(i).getNickName())){
                        name = userList.get(i).getName();
                    } else {
                        name = userList.get(i).getNickName();
                    }
                    break;
                }
            }
        }
        if (phoneNum.equals(myPhone)) {
            isCancelTrack = false;
            if (mTrackPhoneNum != null && mTrackPhoneNum.length() > 0) {
                View view = getViewBorderColor(mTrackPhoneNum, false);
                if (!mTrackPhoneNum.equals(myPhone)) {
                    setMarkerIcon(view);
                }
            }
            if (mTrackPhoneNum == null || mTrackPhoneNum.equals("") || !mTrackPhoneNum.equals(myPhone)) {
                ToastR.setToast(mContext, "跟踪自己");
            } else if (mTrackPhoneNum.equals(myPhone) && index <= 1) {
                ToastR.setToast(mContext, "正在跟踪自己");
            }
            setTrackPhoneNum(phoneNum);
            moveCameraToPhone(marker, mTrackPhoneNum, true);
        } else {
            onClickMarkerDialog(marker, phoneNum, name);
        }
    }

    private void setTrackPhoneNum(String phoneNum) {
        mTrackPhoneNum = phoneNum;

//        setTrackImageViewIcon(phoneNum);
    }

    private void onClickMarkerDialog(final Marker marker, final String phoneNum, final String names) {
        final AlertDialog dlg = new AlertDialog.Builder(mContext).create();
        dlg.setCancelable(true);
        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    ButtonUtils.changeLeftOrRight(true);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    ButtonUtils.changeLeftOrRight(false);
                    return true;
                }
                return false;
            }
        });
        dlg.show();
        Window window = dlg.getWindow();
        window.setContentView(R.layout.map_click);
        ImageView imageView = (ImageView) window.findViewById(R.id.iv_head);
        TextView title = (TextView) window.findViewById(R.id.tv_title);
        TextView lineTwo = (TextView) window.findViewById(R.id.tv_line_two);
        LinearLayout llDetailed = (LinearLayout) window.findViewById(R.id.layout_detailed);
        llDetailed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dlg.cancel();
                detaliInfor(phoneNum);
            }
        });
        LinearLayout llTrack = (LinearLayout) window.findViewById(R.id.layout_track);
        llTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dlg.cancel();
                if (mTrackPhoneNum != null && mTrackPhoneNum.length() > 0) {
                    View viewTrack = getViewBorderColor(mTrackPhoneNum, false);
                    setMarkerIcon(viewTrack);
                }
                ToastR.setToast(mContext, "跟踪 " + names + " " + phoneNum);
                setTrackPhoneNum(phoneNum);
                moveCameraToPhone(marker, mTrackPhoneNum, true);
            }
        });
        if (names != null && names.length() > 0) {
            title.setText(names);
        } else {
            title.setText("未设置");
        }
        if (mTrackPhoneNum.equals(phoneNum)) {
            lineTwo.setVisibility(View.GONE);
            llTrack.setVisibility(View.GONE);
        }
        Bitmap bitmap = FriendFaceUtill.getUserFace(mContext, phoneNum);
        if (bitmap == null) {
            LetterTileDrawable drawable = new LetterTileDrawable(getResources());
            if (names != null && names.length() > 0) {
                drawable.setContactDetails(names, names);
            } else {
                drawable.setContactDetails("1", "1");
            }
            Bitmap bmp = FriendFaceUtill.drawableToBitmap(drawable);
            imageView.setImageBitmap(bmp);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    private void detaliInfor(String phoneNum) {
        if (single) {
            showLinkManPhone();
        } else {
            showMapMemberMsg(phoneNum);
        }
    }

    //查看联系人详细信息
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

    /*
       地图上显示成员信息
       */
    public void showMapMemberMsg(String phone) {
        TeamMemberInfo infos = null;
        for (TeamMemberInfo tm : mTeamMemberInfoList) {
            if (tm.getUserPhone().equals(phone)) {
                infos = tm;
                break;
            }
        }
        if (infos != null && infos.getUserPhone().length() > 0) {
            showInformationDilog(infos);
        }
    }

    /*
       显示成员详情信息弹框
    */
    private void showInformationDilog(final TeamMemberInfo tm) {
        //TODO DDDD
        Intent intent = new Intent(mContext, GroupMemberDetailsActivity.class);
        Bundle bundle = new Bundle();
        for (User user : userList) {
            if (tm.getUserPhone().equals(user.getPhone())) {
                tm.setNickName(user.getNickName());
                break;
            }
        }
        bundle.putParcelable("team_detail", tm);
        intent.putExtra("type", REFRESH_TEAM_REMARK);
        intent.putExtra("my_phone", myPhone);
        intent.putExtra("teamID", teamId);
        intent.putExtra("role", memberRole);
        intent.putExtras(bundle);
        startActivityForResult(intent, REFRESH_TEAM_REMARK);

    }

    // 缩放到指定级别
    private void setMapZoomLevel() {
        if (aMap == null) {
            return;
        }
        config = new SettingRW(mContext);
        config.loadMapZoomLevel();
        aMap.moveCamera(CameraUpdateFactory.zoomTo(config.getMapZoomLevel()));
        mMapZoomLevel = config.getMapZoomLevel();
    }

    /**
     * 开始获取好友或团队位置
     */
    public void startGetLocation() {
        Log.i("pocdemo", "开始获取群组位置。。。。");
        progressDialogShow();
        ProtoMessage.LocationMsgList.Builder builder = ProtoMessage.LocationMsgList.newBuilder();
        if (single) {
            Log.d("pangtao","个人");
            builder.setPhoneNum(linkmanPhone);
        } else {
            Log.d("pangtao","地图查看群 teamId = " + teamId);
            builder.setTeamID(teamId);
        }
        MyService.start(mContext, ProtoMessage.Cmd.cmdStartGetLocation.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(GetFriendLocationProcesser.ACTION);
        new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                progressDialogDismiss();
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent intent) {
                int errorCode = intent.getIntExtra("error_code", -1);
                Log.d("pangtao","errorCode =" +errorCode);
                if (errorCode == ProtoMessage.ErrorCode.OK.getNumber()) {
                    friendLocationStatusesList = intent.getParcelableArrayListExtra("location");
                    addMarkersToMap();
                    setMapZoomLevel();
                    progressDialogDismiss();
                } else {
                    progressDialogDismiss();
                    Log.d("hhh","获取位置失败 errorCode:"+errorCode);
                    ToastR.setToast(mContext, "获取位置失败（请检查网络是否连接或者网络质量差）");
                }
            }
        });
    }

    /**
     * 绘制系统默认marker背景图片
     */
    public void addMarkersToMap() {
        if (aMap != null) {
            aMap.clear();
        }

        mMarkers.clear();

        MarkerOptions markerOptions = new MarkerOptions();
        LatLng latlng = null;
        //获取自定义View
        View view = null;
//        if(single){
//            if(linkmanPhone != null && !linkmanPhone.equals(myPhone)){
//                view = getViewBorderColor(linkmanPhone);
//                latlng = new LatLng(29.613918, 106.50361);
//                markerOptions = new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.fromView(view)).zIndex(2);
//                aMap.addMarker(markerOptions);
//            }
//            if(myPhone != null){
//                view = getViewBorderColor(myPhone);
//                latlng = new LatLng(latitude, longitude);
//                markerOptions = new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.fromView(view)).zIndex(2);
//                aMap.addMarker(markerOptions);
//            }
//        } else {
        mMarkerOptionsList.clear();
        phoneList.clear();
        if (friendLocationStatusesList != null) {
            Log.i("GetFriendLocation", "-------friendLocationStatusesList------" + friendLocationStatusesList.size());
            for (int i = 0; i < friendLocationStatusesList.size(); i++) {
                view = getViewBorderColor(friendLocationStatusesList.get(i).getPhoneNum(), isTrackPhone);
                latlng = new LatLng(friendLocationStatusesList.get(i).getLat(), friendLocationStatusesList.get(i).getLng());
                markerOptions = new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.fromView(view)).zIndex(2);
                //创建Marker对象
                Log.i("GetFriendLocation", "-------friendLocationStatus.getPhoneNum------" + friendLocationStatusesList.get(i).getPhoneNum());
                mMarkerOptionsList.add(markerOptions);
                phoneList.add(friendLocationStatusesList.get(i).getPhoneNum());
//                marker = aMap.addMarker(markerOptions);

            }
        }
        if (myPhone != null) {
            //mTrackPhoneNum = myPhone;
            view = getViewBorderColor(myPhone, isTrackPhone);
            latlng = new LatLng(latitude, longitude);
            MarkerOptions markerOptions1 = new MarkerOptions().position(latlng)
                    .icon(BitmapDescriptorFactory.fromView(view)).zIndex(2).anchor(0.5f, 0.5f);
            mMarkerOptionsList.add(markerOptions1);
            phoneList.add(myPhone);
//            marker = aMap.addMarker(markerOptions);
        }
        //批量加载Marker
        mMarkersList = aMap.addMarkers(mMarkerOptionsList, false);

        for (int i = 0; i < mMarkersList.size(); i++) {
            mMarkersList.get(i).setObject(phoneList.get(i));
            mMarkers.put(phoneList.get(i), mMarkersList.get(i));
        }

        recoveryTrack();
    }

    //恢复跟踪
    private void recoveryTrack() {
        key = "";
        if (linkmanPhone != null && linkmanPhone.length() > 0) {
            key = myPhone + " " + linkmanPhone;
        } else {
            key = myPhone + " " + teamId;
        }
        setTrackPhoneNum((String) SharedPreferencesUtils.get(mContext, key, ""));
        if (mTrackPhoneNum.length() > 0 && mMarkers.size() > 0) {
            Marker marker = mMarkers.get(mTrackPhoneNum);
            if (marker == null) {
                ToastR.setToast(mContext, "没有找到对应的目标");
                return;
            }
            marker.setToTop();
            moveCameraToPhone(marker, mTrackPhoneNum, true);
        }
        setTrackImageViewIcon(mTrackPhoneNum);
    }

    private void setTrackImageViewIcon(String phone) {

        if (mClearTrack == null) {
            return;
        }
        //TODO 目前取消了地图上的清除图标
/*
        if (phone != null && phone.length() > 0) {
            mClearTrack.setImageResource(R.drawable.location_tail);
        } else {
            mClearTrack.setImageResource(R.drawable.location_tail_cancel);
        }*/

    }

    int i = 0;
    int isToast = 0;
    boolean isFistLcation = false;
    public void start() {
        try {
            locationClient = new AMapLocationClient(mContext);
            locationOption = new AMapLocationClientOption();

            //设置定位模式 Hight_Accuracy高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
            locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            locationOption.setGpsFirst(true);
            locationOption.setNeedAddress(false);
            locationOption.setOnceLocation(false);
            //locationOption.setOnceLocationLatest(true);
            //locationOption.setInterval(intervar * 60 * 1000);

            locationOption.setInterval(5 * 1000);
            //设置定位参数
            locationClient.setLocationOption(locationOption);
            // 设置定位监听
            locationClient.setLocationListener(new AMapLocationListener() {

                @Override
                public void onLocationChanged(AMapLocation arg0) {
                    latitude = arg0.getLatitude();
                    longitude = arg0.getLongitude();
                    if (arg0.getLatitude() != 0 && arg0.getLongitude() != 0 ) {
                        isFistLcation = true;
//                    Log.i(ServiceCheckUserEvent.TAG, "正在启动高德(gaode)定位服务...");
                        View view = getViewBorderColor(myPhone, isTrackPhone);
                        LatLng latlng = new LatLng(latitude, longitude);
                        MarkerOptions markerOptions = new MarkerOptions().position(latlng)
                                .icon(BitmapDescriptorFactory.fromView(view)).zIndex(2);
                        Marker marker = mMarkers.get(myPhone);
                        if (marker == null) {
                            MarkerOptions markerOptions1 = new MarkerOptions().position(latlng)
                                    .icon(BitmapDescriptorFactory.fromView(view)).zIndex(2).anchor(0.5f, 0.5f);
                            marker = aMap.addMarker(markerOptions1);
                            marker.setObject(myPhone);
                            mMarkers.put(myPhone, marker);
                        }
                        i++;
                        if (i > 1) {
                            i = 2;
                        }
                        marker.setPosition(latlng);
                        if (!isCancelTrack) {
                            if (mTrackPhoneNum.equals(myPhone)) {
                                resetMarker(marker,mTrackPhoneNum,i);
                            }
                            if (mTrackPhoneNum == null || mTrackPhoneNum.equals("")) {
                                aMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                            }
                        }
                        //TODO  上传位置信息
                        if (ConnUtil.isConnected(mContext)) {
                            Log.d("myAmap","上传位置（5秒上传一次）");
                            toSendLocation(arg0);
                        }
                    }
                    if (arg0.getLatitude() == 0 && arg0.getLongitude() == 0 && !isFistLcation){
                        isToast ++;
                        if (isToast == 2) {
                            ToastR.setToastLong(mContext, "定位失败,请检查是否有定位权限，是否开启移动网络或开启了wifi模块");
                            stop();
                        }
                    }
                }
            });
            locationClient.startLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toSendLocation(AMapLocation aMapLocation) {
        try {
            ProtoMessage.LocationMsg.Builder builder = ProtoMessage.LocationMsg.newBuilder();
            builder.setLat(aMapLocation.getLatitude());
            builder.setLng(aMapLocation.getLongitude());
            builder.setRadius((int) aMapLocation.getAccuracy());
            builder.setTime((new Date().getTime())/1000);
            builder.setIsAccurate(aMapLocation.getLocationType() == AMapLocation.LOCATION_TYPE_GPS ? 1 : 0);
            MyService.start(mContext, ProtoMessage.Cmd.cmdReportLocation.getNumber(), builder.build());
        } catch (Exception e) {
            Log.d("map", "send error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            if (null != locationClient) {
                Log.i(ServiceCheckUserEvent.TAG, "高德 （gaode） location stop");
                locationClient.stopLocation();
                /**
                 * 如果AMapLocationClient是在当前Activity实例化的，
                 * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
                 */
                locationClient.onDestroy();
                locationClient = null;
                locationOption = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(FriendLocationChangedProcesser.ACTION)) {
                FriendLocationStatus friendLocationStatus = intent.getParcelableExtra("friendLocation");
                String phoneNum = friendLocationStatus.getPhoneNum();
                Marker marker = mMarkers.get(phoneNum);
                Log.i("GetFriendLocation", "-------BroadcastReceiver getPhoneNum------" + friendLocationStatus.getPhoneNum());
                if (marker != null) {
                    LatLng startPoint = marker.getPosition();
                    if (friendLocationStatus.getLat() == 0 && friendLocationStatus.getLng() == 0) {
                        Log.i("pocdemo", "没有位置");
                    } else {
                        //markerMove.moveLooper(startPoint, endPoint, marker);
                        LatLng endPoint = new LatLng(friendLocationStatus.getLat(), friendLocationStatus.getLng());
                        String test = friendLocationStatus.getPhoneNum() + ", 定位新座标：" + endPoint;
                        Log.i("pocdemo", test);
                        //ToastR.setToast(FirstActivity.this, test);
                        marker.setPosition(endPoint);
                        if (phoneNum.equals(mTrackPhoneNum)) {
                            aMap.moveCamera(CameraUpdateFactory.newLatLng(endPoint));
                        }
                    }
                } else {
                    //TODO:
                    //marker.setPosition(endPoint);
                    Log.i("pocdemo", "没有找到需要定位的Marker");
                }

            }
        }
    };

    public void stopGetLocation() {
        ProtoMessage.LocationMsgList.Builder builder = ProtoMessage.LocationMsgList.newBuilder();
        MyService.start(mContext, ProtoMessage.Cmd.cmdStopGetLocation.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(StopGetLocationProcesser.ACTION);
        new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {

            }

            @Override
            public void onGot(Intent intent) {
                int errorCode = intent.getIntExtra("error_code", -1);
                Log.i("StopGetFriend", "-----------friendLocationStatusesList----------" + errorCode);
            }
        });
    }

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
                    convertViewTeamMember(mTeamMemberInfoList);
                }
            } else if (deletePhone != null && str != null && str.length() > 0 && deletePhone.equals(linkmanPhone)) {
                showNotification = false;
                finish();
            }
        }
    };

    //处理返回信息
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        int resultData = 0;
        if (data != null) {
            resultData = data.getIntExtra("data", 0);
        }
        switch (requestCode) {
            case DISMISS_OR_QUIT:
                if (resultCode == RESULT_OK) {
                    if (data.getIntExtra("refresh", 0) == ShowAllTeamMemberActivity.REFRESH) {
                        getlistMembersCache();
                        refreshTeamInfo();
                        convertViewTeamMember(SQLiteTool.getAllTeamMembers(mContext, teamId));
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
//                            viewPager.setCurrentItem(0);
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
//        groupName = teamInfo.getTeamName();
//        name.setText(teamInfo.getTeamName());
    }

    private void updateLocalData(final AppliedFriends af, String str) {
        if (!(str.equals(changeType))) {
            linkman = af.getNickName();
//            name.setText(linkman);
//            sta_num.setVisibility(GONE);
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

    private void rankList(int ii) {
//        for (int i = 0; i < userList.size() - ii; i++) {
//            userList.get(i).setPinYin(StringHelper.getPinYinHeadChar(userList.get(i).getName()));
//        }

//        Collections.sort(userList, new Comparator<User>() {
//            @Override
//            public int compare(User o1, User o2) {
//
//                // 加号图标永远排在最后
//                if (o2.isAddIcon())
//                    return -1;
//                if (o1.isAddIcon()) {
//                    return 1;
//                }
//                return o1.getPinYin().compareTo(o2.getPinYin());
//            }
//        });
    }

    private void updateMemberLocalData(final TeamMemberInfo teamMemberInfo) {
        for (User user : userList) {
            if (teamMemberInfo.getUserPhone().equals(user.getPhone())) {
                user.setNickName(teamMemberInfo.getNickName());
                break;
            }
        }
        for (TeamMemberInfo tm : mTeamMemberInfoList) {
            if (tm.getUserPhone().equals(teamMemberInfo.getUserPhone())) {
                tm.setMemberPriority(teamMemberInfo.getMemberPriority());
                tm.setRole(teamMemberInfo.getRole());
                tm.setNickName(teamMemberInfo.getNickName());
                break;
            }
        }
        rankList(1);
    }

    private void refreshLocalData(TeamMemberInfo tm) {
        int k = -1;
        int i = -1;
        for (TeamMemberInfo mte : mTeamMemberInfoList) {
            ++i;
            if (tm.getUserPhone().equals(mte.getUserPhone())) {
                k = i;
                break;
            }
        }
        if (k >= 0) {
            mTeamMemberInfoList.remove(k);
            convertViewTeamMember(mTeamMemberInfoList);
        }
    }
    /**
     * 重写返回键功能
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            resultData("");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void resultData(String str) {
        Intent intent = new Intent();
        //刷新群成员列表信息
        intent.putParcelableArrayListExtra("team_detail_map", mTeamMemberInfoList);
        intent.putExtra("data", 1);
        setResult(RESULT_OK, intent);
        finish();
    }

}
