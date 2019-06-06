package com.example.jrd48.chat.amap;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
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
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.example.jrd48.chat.FirstActivity;
import com.example.jrd48.chat.FriendLocationStatus;
import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.MapMenu;
import com.example.jrd48.chat.MarkerMove;
import com.example.jrd48.chat.MyViewPager;
import com.luobin.dvr.R;
import com.example.jrd48.chat.SettingRW;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.User;
import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.example.jrd48.chat.location.ServiceCheckUserEvent;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyBroadcastReceiver;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.RestartLocationBroadcast;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.root.FriendLocationChangedProcesser;
import com.example.jrd48.service.protocol.root.GetFriendLocationProcesser;
import com.example.jrd48.service.protocol.root.StopGetLocationProcesser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by qhb on 17-3-22.
 */

public class MapHandler implements AMap.OnMyLocationChangeListener, AMap.OnMarkerClickListener {
    private Context context;
    private AMap aMap;
    private MapView mapView;
    private View view2;
    private MyLocationStyle myLocationStyle;
    private MarkerMove markerMove = new MarkerMove();
    private HashMap<String, Marker> mMarkers = new HashMap<>();
    private Marker marker;
    private List<User> userList = new ArrayList<>();
    private boolean isTrackPhone = false;

    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private ImageView mImageLocation;
    private ImageView mClearTrack;
    private ImageView mSetTrack;

    private String mTrackPhoneNum = "";

    private double latitude;
    private double longitude;
    private List<FriendLocationStatus> friendLocationStatusesList = new ArrayList<>();

    private RestartLocationBroadcast mRelocationBroadcast;
    private float mMapZoomLevel;
    MapMenu mMapMenu;
    private String myPhone;
    private boolean single;
    private String linkmanPhone = "";
    private long teamID;
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

    public MapHandler(Context context) {
        this.context = context;
    }

    public MapView getMapView() {
        return mapView;
    }


    public void setaMap(Bundle savedInstanceState, String myPhone, View view2,
                        boolean single, String linkmanPhone, long teamID, List<User> userList, MyViewPager viewPager, boolean isCallHungon) {
        this.myPhone = myPhone;
        this.view2 = view2;
        this.single = single;
        this.linkmanPhone = linkmanPhone;
        this.teamID = teamID;
        this.userList = userList;
        this.viewPager = viewPager;
        this.isCallHungon = isCallHungon;
        initMapView(savedInstanceState);
        // 设置地图事件处理等参数
        initMapListener();
        getUserNameMe();
    }

    private void getUserNameMe() {
        if (single) {
            SharedPreferences preferences = context.getSharedPreferences("token", Context.MODE_PRIVATE);
            userNameMe = preferences.getString("name", myPhone);
        }
    }

    private void initMapListener() {
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        if (aMap == null) {
            return;
        }

        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false

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
                Log.i("pocdemo", "地图缩放级别: " + cameraPosition.zoom);
            }
        });

        //设置SDK 自带定位消息监听
        aMap.setOnMyLocationChangeListener(this);
        aMap.setOnMarkerClickListener(this);

    }

    private void initMapView(Bundle savedInstanceState) {
        // TODO 添加地图界面的初始化

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                Log.i("jim","onPageScrolled--"+position);
            }

            @Override
            public void onPageSelected(int position) {
                if (aMap == null && position == 1) {
                    new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT).setTitle("提示")// 提示框标题
                            .setMessage("地图初始化失败！当前不能使用地图相关功能！")
                            .setPositiveButton("确定", // 提示框的两个按钮
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            viewPager.setCurrentItem(0);
                                            dialog.dismiss();
                                        }

                                    }).create().show();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
//                Log.i("jim","onPageScrollStateChanged--"+state);
            }
        });


        mapView = (MapView) view2.findViewById(R.id.mapView);
        btnToChat = (Button) view2.findViewById(R.id.btn_to_chat);
        btnToChat.getBackground().setAlpha(FirstActivity.SWITCH_BTN_ALPHA);
        btnToChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(0);
            }
        });
        mImageLocation = (ImageView) view2.findViewById(R.id.img_location);
        mImageLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Marker marker = mMarkers.get(myPhone);
                if (marker == null) {
                    ToastR.setToast(context, "没有找到需要定位目标");
                    return;
                }
//                marker.setToTop();
                moveCameraToPhone(marker, myPhone, false);
            }
        });
        mClearTrack = (ImageView) view2.findViewById(R.id.img_clear_track);
        mClearTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO 取消跟踪
                if (mTrackPhoneNum != null && mTrackPhoneNum.length() > 0) {
                    View view1 = getViewBorderColor(mTrackPhoneNum, false);
                    mMarkers.get(mTrackPhoneNum).setIcon(BitmapDescriptorFactory.fromView(view1));
                    ToastR.setToast(context, "取消跟踪");
                } else {
                    ToastR.setToast(context, "没有跟踪目标");
                }
                setTrackPhoneNum("");
                setTrackImageViewIcon(mTrackPhoneNum);
            }
        });
        mSetTrack = (ImageView) view2.findViewById(R.id.img_set_track);
        mSetTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selecteTrack();
               /* boolean isTrack;
                if (mTrackPhoneNum != null && mTrackPhoneNum.length() > 0) {
                    isTrack = true;
                } else {
                    isTrack = false;
                }
                //TODO 当设置了跟踪 才显示下拉菜单
                mMapMenu = new MapMenu((FirstActivity) context, mapItemsOnClick, isTrack);
                mMapMenu.showPopupWindow(mSetTrack, context);*/
            }
        });

        mapView.onCreate(savedInstanceState);
    }

    private void checkMarker() {

    }

    private View.OnClickListener mapItemsOnClick = new View.OnClickListener() {
        public void onClick(View v) {
            mMapMenu.dismiss();
            switch (v.getId()) {
                case R.id.layout_clear_track:
//                    if (!bConnected){
//                        ToastR.setToast(context,"请检测网络是否连接！");
//                        return;
//                    }
                    cancelTrack();
//                    isOrNoShowCrearTrack();
//
                    break;
                case R.id.layout_member_list:
                    selecteTrack();
                    break;
                default:
                    break;
            }

        }
    };

    //选择跟踪目标
    private void selecteTrack() {

        if (aMap == null) {
            ToastR.setToast(context, "当前不能选择跟踪目标");
            return;
        }
        if (userList.size() <= 0) {
            ToastR.setToastLong(context, "没有获取到群成员列表信息");
            return;
        }
        if (mMapMemberListDialog == null) {
            mMapMemberListDialog = new MapMemberListDialog();
        }
        mMapMemberListDialog.showDialog(mTrackPhoneNum, context, myPhone, userNameMe, single,
                userList, isCallHungon, new OnItemClickListener() {
            @Override
            public void onResult(User user) {

                if (!isNetworkConnected(context)) {
                    ToastR.setToastLong(context, "请检测网络是否连接！");
                    return;
                }

                Marker marker = mMarkers.get(user.getPhone());
                if (marker == null) {
                    ToastR.setToast(context, "没有找到跟踪目标");
                    return;
                }
                //设置marker显示在最上层
                marker.setToTop();
                if (myPhone.equals(user.getPhone())) {
                    resetMarker(marker, user.getPhone());
                } else if (mTrackPhoneNum == null || (!user.getPhone().equals(myPhone))) {
                    if (mTrackPhoneNum != null && mTrackPhoneNum.length() > 0) {
                        View view = getViewBorderColor(mTrackPhoneNum, false);
                        mMarkers.get(mTrackPhoneNum).setIcon(BitmapDescriptorFactory.fromView(view));
                    }
                    if (mTrackPhoneNum.equals(user.getPhone())) {
                        ToastR.setToast(context, "正在跟踪 " + user.getName() + " " + user.getPhone());
                    } else {
                        ToastR.setToast(context, "跟踪 " + user.getName() + " " + user.getPhone());
                    }
                    setTrackPhoneNum(user.getPhone());
                    moveCameraToPhone(marker, mTrackPhoneNum, true);
                }
                setTrackImageViewIcon(mTrackPhoneNum);
            }

            @Override
            public void onClickButton() {
                cancelTrack();
            }
        });
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

    /*
    取消跟踪
     */
    public void cancelTrack() {

        if (mTrackPhoneNum != null && mTrackPhoneNum.length() > 0) {
            View view = getViewBorderColor(mTrackPhoneNum, false);
            mMarkers.get(mTrackPhoneNum).setIcon(BitmapDescriptorFactory.fromView(view));
            ToastR.setToast(context, "取消跟踪");
        } else {
            ToastR.setToast(context, "没有跟踪目标");
        }
        setTrackPhoneNum("");
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


    //获取头像边框颜色
    public View getViewBorderColor(String userPhone, boolean isTrackPhone) {

//        isOrNoShowCrearTrack();

        View view = ((FirstActivity) context).getLayoutInflater().inflate(R.layout.marker_icon_layout, null);
        ImageView img_user = (ImageView) view.findViewById(R.id.badge);
        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.layout);
        ImageView img_mylocation = (ImageView) view.findViewById(R.id.img_mylocation);

        if (isTrackPhone) {
            ViewGroup.LayoutParams params = layout.getLayoutParams();
            layout.setBackground(context.getResources().getDrawable(R.drawable.location_face_frame));
//            params.height = dip2px(mContext, 55);
//            params.width = dip2px(mContext, 55);
            layout.setLayoutParams(params);
            if (userPhone.equals(myPhone)) {
                img_mylocation.setBackground(context.getResources().getDrawable(R.drawable.my_location_light));
            }
        }

        if (userPhone.equals(myPhone)) {
            layout.setVisibility(View.GONE);
            img_mylocation.setVisibility(View.VISIBLE);
        }

        String image = userPhone;
        Bitmap bm = GlobalImg.getImage(context, image);
        if (bm != null) {
            img_user.setImageBitmap(bm);
        }
        return view;
    }

    private void moveCameraToPhone(Marker marker, String phoneNum, boolean isTrackPhone) {

        View view = getViewBorderColor(phoneNum, isTrackPhone);
        marker.setIcon(BitmapDescriptorFactory.fromView(view));
        aMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
    }

    int i = 0;
    public void start() {
        try {
            locationClient = new AMapLocationClient(context);
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
                    if (mTrackPhoneNum.equals(myPhone)) {
                        moveCameraToPhone(marker, mTrackPhoneNum, true);
                    }
                    if (mTrackPhoneNum == null || mTrackPhoneNum.equals("")) {
                        if (i == 1) {
                            aMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                        }
                    }
                }
            });
            locationClient.startLocation();
        } catch (Exception e) {
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

    /**
     * 开始获取好友或团队位置
     */
    public void startGetLocation() {
        Log.i("pocdemo", "开始获取群组位置。。。。");
        ProtoMessage.LocationMsgList.Builder builder = ProtoMessage.LocationMsgList.newBuilder();
        if (single) {
            builder.setPhoneNum(linkmanPhone);
        } else {
            builder.setTeamID(teamID);
        }
        MyService.start(context, ProtoMessage.Cmd.cmdStartGetLocation.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(GetFriendLocationProcesser.ACTION);
        new TimeoutBroadcast(context, filter, ((FirstActivity) context).getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(context, "连接超时");
            }

            @Override
            public void onGot(Intent intent) {
                int errorCode = intent.getIntExtra("error_code", -1);
                if (errorCode == ProtoMessage.ErrorCode.OK.getNumber()) {
                    friendLocationStatusesList = intent.getParcelableArrayListExtra("location");
                    addMarkersToMap();
                    setMapZoomLevel();

                } else {
                    ToastR.setToast(context, "查看位置失败: 错误码 " + errorCode);
                }
            }
        });
    }

    public void stopGetLocation() {
        ProtoMessage.LocationMsgList.Builder builder = ProtoMessage.LocationMsgList.newBuilder();
        MyService.start(context, ProtoMessage.Cmd.cmdStopGetLocation.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(StopGetLocationProcesser.ACTION);
        new TimeoutBroadcast(context, filter, ((FirstActivity) context).getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
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

    public void onResume() {


        setMapZoomLevel();
        Log.i("pocdemo", "默认的地图缩放等级: " + mMapZoomLevel);
        // 改为在此请求好友位置
        startGetLocation();
        start();
        // 网络恢复时自动观察群组成员位置
        mRelocationBroadcast = new RestartLocationBroadcast(context);
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
//        bConnected = (boolean) SharedPreferencesUtils.get(context,"connect",false);
    }

    SettingRW config;
    // 缩放到指定级别
    private void setMapZoomLevel() {
        if (config == null) {
            config = new SettingRW(context, true);
        }
        if (aMap == null) {
            return;
        }
        aMap.moveCamera(CameraUpdateFactory.zoomTo(config.getMapZoomLevel()));
        mMapZoomLevel = config.getMapZoomLevel();
    }

    //恢复跟踪
    private void recoveryTrack() {
        key = "";
        if (linkmanPhone != null && linkmanPhone.length() > 0) {
            key = myPhone + " " + linkmanPhone;
        } else {
            key = myPhone + " " + teamID;
        }
        setTrackPhoneNum((String) SharedPreferencesUtils.get(context, key, ""));
        if (mTrackPhoneNum.length() > 0 && mMarkers.size() > 0) {
            Marker marker = mMarkers.get(mTrackPhoneNum);
            if (marker == null) {
                ToastR.setToast(context, "没有找到对应的目标");
                return;
            }
            marker.setToTop();
            moveCameraToPhone(marker, mTrackPhoneNum, true);
        }
        setTrackImageViewIcon(mTrackPhoneNum);
    }

    public void onPause() {
        stopGetLocation();
        stop();
        SharedPreferencesUtils.put(context, key, mTrackPhoneNum);
        try {
            Log.i("pocdemo", "地图：save zoom level: " + mMapZoomLevel);
            SettingRW rw = new SettingRW(context, true);
            rw.setMapZoomLevel(mMapZoomLevel);
            rw.save();

            if (mBroadcastReceiver != null) {
                context.unregisterReceiver(mBroadcastReceiver);
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
    }

    public void onDestroy() {
        mapView.onDestroy();
    }

    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(FriendLocationChangedProcesser.ACTION);
        //注册广播
        context.registerReceiver(mBroadcastReceiver, myIntentFilter);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String phoneNum = (String) marker.getObject();
        if (phoneNum == null) {
            // TODO: 点击我的图标
            return true;
        }
        resetMarker(marker, phoneNum);
        return false;
    }

    private void resetMarker(Marker marker, String phoneNum) {
        String name = "";
        if (userList != null) {
            for (int i = 0; i < userList.size(); i++) {
                if (userList.get(i).getPhone().equals(phoneNum)) {
                    name = userList.get(i).getName();
                    break;
                }
            }
        }
        if (phoneNum.equals(myPhone)) {
            if (mTrackPhoneNum != null && mTrackPhoneNum.length() > 0) {
                View view = getViewBorderColor(mTrackPhoneNum, false);
                if (!mTrackPhoneNum.equals(myPhone)) {
                    mMarkers.get(mTrackPhoneNum).setIcon(BitmapDescriptorFactory.fromView(view));
                }
            }
            if (mTrackPhoneNum == null || mTrackPhoneNum.equals("") || !mTrackPhoneNum.equals(myPhone)) {
                ToastR.setToast(context, "跟踪自己");
            } else if (mTrackPhoneNum.equals(myPhone)) {
                ToastR.setToast(context, "正在跟踪自己");
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


    private void onClickMarkerDialog(final Marker marker, final String phoneNum, final String names) {
        final AlertDialog dlg = new AlertDialog.Builder(context).create();
        dlg.setCancelable(true);
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
                    View view1 = getViewBorderColor(mTrackPhoneNum, false);
                    mMarkers.get(mTrackPhoneNum).setIcon(BitmapDescriptorFactory.fromView(view1));
                }
                ToastR.setToast(context, "跟踪 " + names + " " + phoneNum);
                setTrackPhoneNum(phoneNum);
                moveCameraToPhone(marker, mTrackPhoneNum, true);
            }
        });
        title.setText((names));
        if (mTrackPhoneNum.equals(phoneNum)) {
            lineTwo.setVisibility(View.GONE);
            llTrack.setVisibility(View.GONE);
        }
        Bitmap bitmap = FriendFaceUtill.getUserFace(context, phoneNum);
        if (bitmap == null) {
            imageView.setImageResource(R.drawable.woman);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    private void detaliInfor(String phoneNum) {
        if (single) {
            ((FirstActivity) context).showLinkManPhone();
        } else {
            ((FirstActivity) context).showMapMemberMsg(phoneNum);
        }
    }

    /**
     * 刷新本地数据
     */
    public void refreshMapLocalData(List<User> userList, boolean isCallHungon) {
        this.userList = userList;
        this.isCallHungon = isCallHungon;
    }

    @Override
    public void onMyLocationChange(Location location) {
        // 定位回调监听
        if (location != null) {
            Log.i("amap", "onMyLocationChange 定位成功， lat: " + location.getLatitude() + " lon: " + location.getLongitude());
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
                Log.i("amap", "定位信息， code: " + errorCode + " errorInfo: " + errorInfo + " locationType: " + locationType);
            } else {
                Log.e("amap", "定位信息， bundle is null ");
            }
        } else {
            Log.e("amap", "定位失败");
        }
    }

    public void setbConnected(boolean bConnected) {
        this.bConnected = bConnected;
    }

    //TODO 是否显示 取消跟踪 按钮
    public void isOrNoShowCrearTrack() {
        if (mTrackPhoneNum != null && mTrackPhoneNum.length() > 0) {
            mClearTrack.setVisibility(View.VISIBLE);
        } else {
            mClearTrack.setVisibility(View.GONE);
        }
    }
}
