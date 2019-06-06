package com.luobin.search.friends;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.MyViewPager;
import com.example.jrd48.chat.PicTool.LetterTileDrawable;
import com.example.jrd48.chat.SettingRW;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.friend.AddFriendPrompt;
import com.example.jrd48.chat.friend.AddFriendPromptListener;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.friend.FriendFaceUtill;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.chat.location.ServiceCheckUserEvent;
import com.example.jrd48.chat.permission.PermissionUtil;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyBroadcastReceiver;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.RestartLocationBroadcast;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.ApplyFriendProcesser;
import com.example.jrd48.service.protocol.root.GetCarLocationProcesser;
import com.example.jrd48.service.protocol.root.LiveVideoCallProcesser;
import com.example.jrd48.service.protocol.root.SearchStrangerProcesser;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.luobin.dvr.R;
import com.luobin.model.SearchStrangers;
import com.luobin.model.StrangerLocationStatus;
import com.luobin.tool.RadiusTool;
import com.luobin.utils.ButtonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/8/7.
 */

public class AmapSearchActivity extends BaseActivity implements PermissionUtil.PermissionCallBack ,View.OnClickListener, AMap.OnMyLocationChangeListener, AMap.OnMarkerClickListener {
    private MapView mMapView;
    private ImageView mImageLocation;
    private ImageView mImageViewShowList;
    private ImageView mImageViewBack;
    private TextView mTextViewSetLocation;
    private TextView mTextViewShow;
    private RelativeLayout mRelativeLayoutMap;
    private Button btnMap;
    private Button btnList;
    private TextView mTextViewMap;
    private TextView mTextViewList;
    private GeocodeSearch geocoderSearch;
    private AMap mAMap;
    private String myPhone;
    private String userNameMe;
    private float mMapZoomLevel;
    private Context context;
    List<AppliedFriends> mFriend;
    private boolean isFriend ;

    private AddressMsg mAddressMsg;

    private double latitude;
    private double longitude;
    int MY_PERMISSIONS_REQUEST_LOCATION = 10021;
    private RestartLocationBroadcast mRelocationBroadcast;
    private String mTrackPhoneNum = "";
    private String key;
    public static final int SET_LOCATION = 1;
    public static final String SET_KEY = "set_key";

    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;

    private ArrayList<MarkerOptions> mMarkerOptionsList = new ArrayList<MarkerOptions>();
    private List<Marker> mMarkersList;
    private List<String> phoneList = new ArrayList<String>();

    private HashMap<String, Marker> mMarkers = new HashMap<>();
    private Marker marker;
    //    private List<AddressMsg> addressMsgList = new ArrayList<>();
    private boolean isTrackPhone = false;
    private List<StrangerLocationStatus> strangerLocationStatusesList = new ArrayList<>();
    Handler handler = new Handler();
    private String addressName = "";

    private PullToRefreshListView mPullRefreshListView;
    private ListView mListView;
    private SearchStrangersAdapter adapterU;
    StrangerLocationStatus strangerLocationStatus;
    protected PermissionUtil mPermissionUtil;

    private boolean mShowMap = true;

    //ViewPager
    private View view1, view2;
    private List<View> viewList;//view数组
    private MyViewPager viewPager;  //对应的viewPager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearby_vehicles_map);
        context = this;
        mPermissionUtil = PermissionUtil.getInstance();
        mPermissionUtil.requestPermission1(this, PermissionUtil.MY_READ_LOCATION, this,
                PermissionUtil.PERMISSIONS_LOCATION, PermissionUtil.PERMISSIONS_LOCATION);
        initViewPager();
        initData();
        loadUserFriendsFromCache();
       /* if (mPermissionUtil.isOverMarshmallow()) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }*/
        initView();
        mMapView.onCreate(savedInstanceState);

    }

    private void initViewPager() {
        viewPager = (MyViewPager) findViewById(R.id.viewpager);
        LayoutInflater inflater = getLayoutInflater();
        view1 = inflater.inflate(R.layout.stranger_map, null);
        view2 = inflater.inflate(R.layout.stranger_list, null);


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

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                if (position == 0){
                    refreshTextView(true);
                } else {
                    refreshTextView(false);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


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

    private void initView() {
        mTextViewMap = (TextView) findViewById(R.id.tv_map);
        mTextViewList = (TextView) findViewById(R.id.tv_list);
        mImageViewBack = (ImageView) findViewById(R.id.iv_back);
        mImageViewBack.setOnClickListener(this);
        mMapView = (MapView) view1.findViewById(R.id.texture_mapView);
        mImageLocation = (ImageView) view1.findViewById(R.id.img_location);
        mImageLocation.setOnClickListener(this);
        mImageViewShowList = (ImageView) view1.findViewById(R.id.img_other_batch);
        mImageViewShowList.setOnClickListener(this);
        mAMap = mMapView.getMap();
        mRelativeLayoutMap = (RelativeLayout) view1.findViewById(R.id.rl_map);
        mTextViewSetLocation = (TextView) findViewById(R.id.tv_set_location);
        mTextViewSetLocation.setOnClickListener(this);
        btnMap = (Button) findViewById(R.id.btn_map);
        btnMap.setOnClickListener(this);
        btnList = (Button) findViewById(R.id.btn_list);
        btnList.setOnClickListener(this);
        mTextViewShow = (TextView) view2.findViewById(R.id.tv_list_show);
        //TODO: 缩放以后把缩放级别保存到 SettingRW 中
        mAMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
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
        mAMap.setOnMyLocationChangeListener(this);
        mAMap.setOnMarkerClickListener(this);
        mAMap.setOnMapLongClickListener(new AMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                //TODO  地图长按事件
                LatLonPoint latLon = new LatLonPoint(latLng.latitude, latLng.longitude);
                RegeocodeQuery query = new RegeocodeQuery(latLon, 200, GeocodeSearch.AMAP);
                geocoderSearch.getFromLocationAsyn(query);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        longClickDialog(latLng);
                    }
                }, 300);

            }
        });

        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int arg1) {
                if (arg1 == 1000) {
                    if (regeocodeResult != null && regeocodeResult.getRegeocodeAddress() != null
                            && regeocodeResult.getRegeocodeAddress().getFormatAddress() != null) {
                        addressName = regeocodeResult.getRegeocodeAddress().getFormatAddress();
                    } else {
                    }
                } else {
                }
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

            }
        });


        mPullRefreshListView = (PullToRefreshListView) view2.findViewById(R.id.strangers_picture);
        mListView = mPullRefreshListView.getRefreshableView();
        adapterU = new SearchStrangersAdapter(this, strangerLocationStatusesList);
        mListView.setAdapter(adapterU);
        mListView.setSelector(R.drawable.dvr_listview_background);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
//                Intent intent = new Intent(AmapSearchActivity.this, ShowStrangerMsgActivity.class);
//                intent.putExtra("user_info", strangerLocationStatusesList.get(position - 1));
//                startActivity(intent);
                //TODO
                checkIsFriend(strangerLocationStatusesList.get(position - 1).getPhoneNum());
                resetMarker(strangerLocationStatusesList.get(position - 1).getPhoneNum(),isFriend);
            }
        });

        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                CheckIsLocation();
            }
        });
        registerForContextMenu(mListView);

    }

    /**
     * 获取本地朋友列表缓存信息
     */
    private void loadUserFriendsFromCache() {
        try {
            DBManagerFriendsList db = new DBManagerFriendsList(this, DBTableName.getTableName(this, DBHelperFriendsList.NAME));
            mFriend = db.getFriends(false);
            db.closeDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void longClickDialog(final LatLng latLng) {
        final AlertDialog dlg = new AlertDialog.Builder(this).create();
        dlg.show();
        Window window = dlg.getWindow();
        window.setContentView(R.layout.set_location);
        window.setGravity(Gravity.BOTTOM);
     //   window.setWindowAnimations(R.style.j_timepopwindow_anim_style);
        TextView content = (TextView) window.findViewById(R.id.tv_content);
        String str = getResources().getString(R.string.nearby);
        if (addressName.length() > 0) {
            str = getResources().getString(R.string.long_click_get_address, addressName+str);
        } else {
            str = getResources().getString(R.string.long_click_get_address, str);
        }
        content.setText(Html.fromHtml(str));
        Button sure = (Button) window.findViewById(R.id.birthday_sure_btn);
        sure.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO 确定要做的事
                toSetLocation(latLng);
                dlg.cancel();
            }
        });
        Button cancel = (Button) window.findViewById(R.id.birthday_cancel_btn);
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dlg.cancel();
                return;
            }
        });

    }

    private void toSetLocation(LatLng latLng) {
        longitude = latLng.longitude;
        latitude = latLng.latitude;
        toGetOtherLocation(0);
        addMarkersToMap();
    }

    @Override
    public void onPermissionSuccess(String type) {
    }

    @Override
    public void onPermissionReject(String strMessage) {
        ToastR.setToastLong(context, "[ 定位 ] 权限已经拒绝，请到手机管家或者系统设置里授权");
    }

    @Override
    public void onPermissionFail(String failType) {
        ToastR.setToastLong(context, "[ 定位 ] 权限设置失败，请到手机管家或者系统设置里授权");
    }



    public class SearchStrangersAdapter extends BaseAdapter {

        int friendsIndex = 0;
        private List<StrangerLocationStatus> mArrayList;
        private Context context;
        private LayoutInflater layoutInflater;

        public SearchStrangersAdapter(Context context, List<StrangerLocationStatus> mFriend) {
            layoutInflater = LayoutInflater.from(context);
            this.context = context;
            this.mArrayList = mFriend;
        }

        public List<StrangerLocationStatus> getList() {
            return mArrayList;
        }

        // 刷新适配器
        public void refresh(List<StrangerLocationStatus> mArryFriend) {
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
        public StrangerLocationStatus getItem(int position) {
            StrangerLocationStatus item = null;
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
            final StrangerLocationStatus user = mArrayList.get(position);
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.search_amap_strangers_item, null);
                holder.name = (TextView) convertView.findViewById(R.id.search_listitem_name);
                holder.phone = (TextView) convertView.findViewById(R.id.search_listitem_count);
                holder.image = (ImageView) convertView.findViewById(R.id.search_circle_image);
                holder.mButtonAddFriend = (Button) convertView.findViewById(R.id.btn_add_friend);
                holder.mButtonRequest = (Button) convertView.findViewById(R.id.btn_request);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (user == null) {
                return null;
            }
//                holder.phone.setText(user.getPhone());

            holder.image.setAlpha(1f);
//                holder.name.setTextColor(0xff888888);

            Bitmap bitmap = GlobalImg.getImage(context, user.getPhoneNum());
            if (bitmap == null) {
                LetterTileDrawable drawable = new LetterTileDrawable(getResources());
                if (user.getUserName() != null && user.getUserName().length() > 0) {
                    drawable.setContactDetails(user.getUserName(), user.getUserName());
                } else {
                    drawable.setContactDetails("1", "1");
                }
                Bitmap bmp = FriendFaceUtill.drawableToBitmap(drawable);
                holder.image.setImageBitmap(bmp);
            } else {
                holder.image.setImageBitmap(bitmap);
            }

            holder.mButtonAddFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO 添加好友
                }
            });

            holder.mButtonRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO 请求查看路况
                    toRoadConditionQuery(user.getPhoneNum());
                }
            });

            String number = user.getPhoneNum();
            String str = number.substring(0, 3);
            String str1 = number.substring(8, number.length());
            int radius = user.getRadius() <= 1 ? 1 : user.getRadius();
            String radiu = RadiusTool.getRadius(radius);
            holder.phone.setText(str + "*****" + str1 + "\n"+radiu);
            if (user.getUserName().equals(number)) {
                holder.name.setText(str + "*****" + str1);
            }else {
                holder.name.setText(user.getUserName() + "");
            }
            return convertView;
        }

        public class ViewHolder {
            // public TextView tvNumber;
            TextView name;
            TextView discourseCompetence;
            TextView phone;
            ImageView image;
            TextView nickName;
            RelativeLayout mRelativeLayout;
            Button mButtonAddFriend;
            Button mButtonRequest;
        }
    }

    private void refreshTextView(boolean isShowMap){
        mShowMap = isShowMap;
        if (isShowMap){
            mTextViewList.setVisibility(View.INVISIBLE);
            mTextViewMap.setVisibility(View.VISIBLE);
            btnMap.setTextColor(getResources().getColor(R.color.textColor));
            btnList.setTextColor(getResources().getColor(R.color.black));
        }else {
            mTextViewList.setVisibility(View.VISIBLE);
            mTextViewMap.setVisibility(View.INVISIBLE);
            btnMap.setTextColor(getResources().getColor(R.color.black));
            btnList.setTextColor(getResources().getColor(R.color.textColor));
        }
    }

    private void initData() {
        SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        myPhone = preferences.getString("phone", "");
        userNameMe = preferences.getString("name", myPhone);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_location:
                isFistLcation = false;
                start();
                break;
            case R.id.img_other_batch:
                CheckIsLocation();
                break;
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_set_location:
//                startActivityForResult(new Intent(this, SetLocationActivityGaode.class), SET_LOCATION);
                break;
            case R.id.btn_map:
//                mPullRefreshListView.setVisibility(View.GONE);
//                mRelativeLayoutMap.setVisibility(View.VISIBLE);
                viewPager.setCurrentItem(0);
                break;
            case R.id.btn_list:
                viewPager.setCurrentItem(1);
//                mRelativeLayoutMap.setVisibility(View.GONE);
//                mPullRefreshListView.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private void CheckIsLocation() {
        if (isFistLcation) {
            toGetOtherLocation(1);
        } else {
            start();
        }
    }

    SettingRW config;

    // 缩放到指定级别
    private void setMapZoomLevel() {
        if (mAMap == null) {
            return;
        }
        config = new SettingRW(this);
        config.loadMapZoomLevel();
        mAMap.moveCamera(CameraUpdateFactory.zoomTo(config.getMapZoomLevel()));
        mMapZoomLevel = config.getMapZoomLevel();
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
        Bitmap bm = GlobalImg.getImage(this, image);
        if (bm != null) {
            img_user.setImageBitmap(bm);
        } else {
            String name = "1";
            if (strangerLocationStatusesList != null) {
                for (int i = 0; i < strangerLocationStatusesList.size(); i++) {
                    if (strangerLocationStatusesList.get(i).getPhoneNum().equals(userPhone)) {
                        if (strangerLocationStatusesList.get(i).getUserName() != null &&
                                strangerLocationStatusesList.get(i).getUserName().length() > 0) {
                            name = strangerLocationStatusesList.get(i).getUserName();
                        }
                        break;
                    }
                }
            }
            LetterTileDrawable drawable = new LetterTileDrawable(getResources());
            drawable.setContactDetails(name, name);
            Bitmap bmp = FriendFaceUtill.drawableToBitmap(drawable);
            img_user.setImageBitmap(bmp);
        }
        return view;
    }

    private void moveCameraToPhone(Marker marker, String phoneNum, boolean isTrackPhone) {

        View view = getViewBorderColor(phoneNum, isTrackPhone);
        marker.setIcon(BitmapDescriptorFactory.fromView(view));
        mAMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
    }

    @Override
    protected void onResume() {
        setMapZoomLevel();
        Log.i("pocdemo", "默认的地图缩放等级: " + mMapZoomLevel);
        // 改为在此请求好友位置
        start();
//        startGetLocation();
        // 网络恢复时自动观察群组成员位置
        mRelocationBroadcast = new RestartLocationBroadcast(context);
        mRelocationBroadcast.setReceiver(new MyBroadcastReceiver() {
            @Override
            protected void onReceiveParam(String str) {
                // TODO:
//                startGetLocation();
            }
        });
        mRelocationBroadcast.start();
//        registerBoradcastReceiver();
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        stop();
        SharedPreferencesUtils.put(context, key, mTrackPhoneNum);
        try {
            Log.i("pocdemo", "地图：save zoom level: " + mMapZoomLevel);
            SettingRW rw = new SettingRW(context);
            rw.setMapZoomLevel(mMapZoomLevel);
            rw.saveMapZoomLevel();

//            if (mBroadcastReceiver != null) {
//                context.unregisterReceiver(mBroadcastReceiver);
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        setTrackPhoneNum("");
        mMapView.onPause();
        if (mRelocationBroadcast != null) {
            mRelocationBroadcast.stop();
            mRelocationBroadcast = null;
        }

        super.onPause();
    }

    private void setTrackPhoneNum(String phoneNum) {
        mTrackPhoneNum = phoneNum;

//        setTrackImageViewIcon(phoneNum);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //权限申请结果
        if (requestCode == PermissionUtil.MY_READ_LOCATION) {
            mPermissionUtil.requestResult(this, permissions, grantResults, this, PermissionUtil.PERMISSIONS_LOCATION);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String phoneNum = (String) marker.getObject();
        if (phoneNum == null) {
            // TODO: 点击我的图标
            return true;
        }
        checkIsFriend(phoneNum);
        Log.d("jim","phoneNum:"+phoneNum + " ---"+isFriend);
        resetMarker(phoneNum,isFriend);
        //   searchFriend(marker, phoneNum);
        return false;
    }

    private void checkIsFriend(String phoneNum) {
        isFriend = false;
        for (AppliedFriends friends : mFriend) {
            if (friends.getPhoneNum().equals(phoneNum)) {
                isFriend = true;
                Log.d("jim","phoneNum:"+phoneNum + " ---"+friends.getPhoneNum());
                break;
            }
        }
    }


    private void searchFriend(final Marker marker, String phoneNum) {
        if (phoneNum.equals(myPhone)) {
            ToastR.setToast(context, "点击了用户自己");
            return;
        }
        ProtoMessage.SearchUser.Builder builder = ProtoMessage.SearchUser.newBuilder();
        builder.setPhoneNum(phoneNum);
        builder.setOnlyPhoneNum(true);
        MyService.start(context, ProtoMessage.Cmd.cmdSearchUser2.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(SearchStrangerProcesser.ACTION);
        new TimeoutBroadcast(context, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                ToastR.setToast(context, "连接超时,网络质量差");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    SearchStrangers searchFriends = (SearchStrangers) i.getSerializableExtra("user_info");
                    if (searchFriends.getPhoneNum() == null || searchFriends.getPhoneNum().length() <= 0) {
                        ToastR.setToast(context, "未找到相关用户");
                    } else {
//                        resetMarker(marker,searchFriends);
                    }
                } else {
                    new ResponseErrorProcesser(context, i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    private void resetMarker(final String phone ,boolean isFriend) {
        strangerLocationStatus = null;
        for (StrangerLocationStatus staranger : strangerLocationStatusesList) {
            if (phone.equals(staranger.getPhoneNum())) {
                strangerLocationStatus = staranger;
                break;
            }
        }
        if (strangerLocationStatus == null) {
            return;
        }
        final AlertDialog dlg = new AlertDialog.Builder(context).create();
        dlg.setCancelable(true);
        dlg.show();
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
        Window window = dlg.getWindow();
        window.setContentView(R.layout.stranger_information);
        ImageView imageView = (ImageView) window.findViewById(R.id.iv_head);
        TextView title = (TextView) window.findViewById(R.id.tv_title);
        TextView lineTwo = (TextView) window.findViewById(R.id.tv_line_two);
        LinearLayout llDetailed = (LinearLayout) window.findViewById(R.id.layout_detailed);
        llDetailed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dlg.cancel();
                //TODO 添加好友
                Intent intent = new Intent(AmapSearchActivity.this, ShowStrangerMsgActivity.class);
                intent.putExtra("user_info", strangerLocationStatus);
                startActivity(intent);
//                detaliInfor(phoneNum);
            }
        });
        LinearLayout llTrack = (LinearLayout) window.findViewById(R.id.layout_look);
        lineTwo.setVisibility(View.GONE);
        llTrack.setVisibility(View.GONE);
        llTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dlg.cancel();
                toRoadConditionQuery(phone);
                //TODO 路况查询
            }
        });
        LinearLayout llAddFriend = (LinearLayout) window.findViewById(R.id.layout_add_friend);
        llAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dlg.cancel();
                dialogFriendsRequest(strangerLocationStatus);
            }
        });
        if (strangerLocationStatus.getUserName() != null && strangerLocationStatus.getUserName().length() > 0) {
            title.setText(strangerLocationStatus.getUserName());
        } else {
            title.setText("未设置");
        }
        Log.d("jim","isFriend:"+isFriend);
        if (isFriend){
            llAddFriend.setVisibility(View.GONE);
        }else {
            llAddFriend.setVisibility(View.VISIBLE);
        }
        Bitmap bitmap = FriendFaceUtill.getUserFace(context, strangerLocationStatus.getPhoneNum());
        if (bitmap == null) {
            String name = "1";
            if (strangerLocationStatus.getUserName() != null && strangerLocationStatus.getUserName().length() > 0) {
                if (strangerLocationStatus.getUserName() != null && strangerLocationStatus.getUserName().length() > 0) {
                    name = strangerLocationStatus.getUserName();
                }
            }
            LetterTileDrawable drawable = new LetterTileDrawable(getResources());
            drawable.setContactDetails(name, name);
            Bitmap bmp = FriendFaceUtill.drawableToBitmap(drawable);
            imageView.setImageBitmap(bmp);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    /**
     * 添加好友提示框
     */
    private void dialogFriendsRequest(final StrangerLocationStatus stranger) {
        try {
            String msg = "，请求加您为好友，谢谢。";
            AddFriendPrompt.dialogFriendsRequest(this, msg, stranger.getUserName(), userNameMe, new AddFriendPromptListener() {
                @Override
                public void onOk(String remark, String msg) {
                    addFriendsRequest(remark, msg,stranger.getPhoneNum());
                }

                @Override
                public void onFail(String typ) {
                    if (typ.equals(AddFriendPrompt.TYP)){
                        ToastR.setToast(AmapSearchActivity.this, "信息输入不能为空");
                    }else {
                        ToastR.setToast(AmapSearchActivity.this, "备注输入过长（最大只能设置16个字符）");
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
    private void addFriendsRequest(String remark, String msg,String phoneNum) {
        ProtoMessage.ApplyFriend.Builder builder = ProtoMessage.ApplyFriend.newBuilder();
        builder.setFriendPhoneNum(phoneNum);
        builder.setApplyInfo(msg);
        builder.setApplyRemark(remark);
        MyService.start(AmapSearchActivity.this, ProtoMessage.Cmd.cmdApplyFriend.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(ApplyFriendProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(AmapSearchActivity.this, filter, getBroadcastManager());

        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                ToastR.setToast(AmapSearchActivity.this, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(AmapSearchActivity.this, "请求成功，等待对方回应");
                } else {
                    new ResponseErrorProcesser(AmapSearchActivity.this,i.getIntExtra("error_code", -1));
                }
            }
        });
    }


    @Override
    public void onMyLocationChange(Location location) {

    }




    /**
     * 绘制系统默认marker背景图片
     */
    public void addMarkersToMap() {
        if (mAMap != null) {
            mAMap.clear();
        }

        mMarkers.clear();

        MarkerOptions markerOptions = new MarkerOptions();
        LatLng latlng = null;
        //获取自定义View
        View view = null;
        mMarkerOptionsList.clear();
        phoneList.clear();
        if (strangerLocationStatusesList != null) {
            Log.i("GetFriendLocation", "-------strangerLocationStatusesList------" + strangerLocationStatusesList.size());
            for (int i = 0; i < strangerLocationStatusesList.size(); i++) {
                if (strangerLocationStatusesList.get(i).getPhoneNum().equals(myPhone)) {
                    continue;
                }
                view = getViewBorderColor(strangerLocationStatusesList.get(i).getPhoneNum(), isTrackPhone);
                latlng = new LatLng(strangerLocationStatusesList.get(i).getLat(), strangerLocationStatusesList.get(i).getLng());
                Log.i("jim", "好友经纬度：" + strangerLocationStatusesList.get(i).getLat() + "     " + strangerLocationStatusesList.get(i).getLng());
                markerOptions = new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.fromView(view)).zIndex(2);
                //创建Marker对象
                Log.i("GetFriendLocation", "-------friendLocationStatus.getPhoneNum------" + strangerLocationStatusesList.get(i).getPhoneNum());
                mMarkerOptionsList.add(markerOptions);
                phoneList.add(strangerLocationStatusesList.get(i).getPhoneNum());

            }
        }
        if (myPhone != null) {
            //mTrackPhoneNum = myPhone;
            view = getViewBorderColor(myPhone, isTrackPhone);
//            latlng = new LatLng(latitude, longitude);
            latlng = new LatLng(latitude, longitude);
            MarkerOptions markerOptions1 = new MarkerOptions().position(latlng)
                    .icon(BitmapDescriptorFactory.fromView(view)).zIndex(2).anchor(0.5f, 0.5f);
            mMarkerOptionsList.add(markerOptions1);
            phoneList.add(myPhone);
            mAMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
//            marker = aMap.addMarker(markerOptions);
        }
        //批量加载Marker
        mMarkersList = mAMap.addMarkers(mMarkerOptionsList, false);

        for (int i = 0; i < mMarkersList.size(); i++) {
            mMarkersList.get(i).setObject(phoneList.get(i));
            mMarkers.put(phoneList.get(i), mMarkersList.get(i));
        }

//        recoveryTrack();
    }


    int i = 0;
    int pos = 0;
    boolean isFistLcation = false;
    int isToast = 0;

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

            locationOption.setInterval(3 * 1000);
            //设置定位参数
            locationClient.setLocationOption(locationOption);
            // 设置定位监听
            locationClient.setLocationListener(new AMapLocationListener() {

                @Override
                public void onLocationChanged(AMapLocation arg0) {

//                    Log.i(ServiceCheckUserEvent.TAG, "正在启动高德(gaode)定位服务...");
                    if (arg0.getLatitude() != 0 && arg0.getLongitude() != 0 && !isFistLcation) {
                        latitude = arg0.getLatitude();
                        longitude = arg0.getLongitude();
                        isFistLcation = true;
                        pos = 0;
                        toGetOtherLocation(pos);
                    }
                    if (arg0.getLatitude() == 0 && arg0.getLongitude() == 0 && !isFistLcation){
                        isToast ++;
                        if (isToast == 2) {
                            ToastR.setToastLong(context, "定位失败,请检查是否有定位权限，是否开启移动网络或开启了wifi模块");
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

    private void toGetOtherLocation(int pos) {

        Log.i("hjm", "开始获某个点周围位置。。。。");
        ProtoMessage.MsgSearchAround.Builder builder = ProtoMessage.MsgSearchAround.newBuilder();
        builder.setLat(latitude);
        builder.setLng(longitude);
        builder.setRadius(100000);  //半径(米)
        builder.setPos(pos);// 批次：0,第一批，1第二批，...， 每次100个
        //TODO 获取定位位置
        MyService.start(context, ProtoMessage.Cmd.cmdSearchAround.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(GetCarLocationProcesser.ACTION);
        new TimeoutBroadcast(context, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                if (mPullRefreshListView != null) {
                    mPullRefreshListView.onRefreshComplete();
                }
                ToastR.setToast(context, "连接超时");
            }

            @Override
            public void onGot(Intent intent) {
                if (mPullRefreshListView != null) {
                    mPullRefreshListView.onRefreshComplete();
                }
                int errorCode = intent.getIntExtra("error_code", -1);
                if (errorCode == ProtoMessage.ErrorCode.OK.getNumber()) {
                    List<StrangerLocationStatus> strangerList = (ArrayList<StrangerLocationStatus>) intent.getSerializableExtra("stranger_location");
                    stop();
                    if (strangerList == null || strangerList.size() <= 0){
                        addMarkersToMap();
                        if (strangerLocationStatusesList.size() > 0) {
                            mTextViewShow.setVisibility(View.GONE);
                        } else {
                            mTextViewShow.setVisibility(View.VISIBLE);
                        }
                        ToastR.setToastLong(context,"目前未找到更多附近车辆");
                        return;
                    }
                    strangerLocationStatusesList.clear();
                    strangerLocationStatusesList = strangerList;
                    StrangerComparator comparator = new StrangerComparator();
                    Collections.sort(strangerLocationStatusesList, comparator);
                    addMarkersToMap();
                    adapterU.refresh(strangerLocationStatusesList);
                    if (strangerLocationStatusesList.size() > 0) {
                        mTextViewShow.setVisibility(View.GONE);
                    } else {
                        mTextViewShow.setVisibility(View.VISIBLE);
                        if (mShowMap){
                            ToastR.setToastLong(context,"目前未找到更多附近车辆");
                        }
                    }
//                    setMapZoomLevel();

                } else {
                    ToastR.setToast(context, "查看位置失败: 错误码 " + errorCode);
                }
            }
        });


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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      /*  if (data == null) {
            return;
        }*/
        switch (requestCode) {
            case SET_LOCATION:
                if (data != null && data.hasExtra(SET_KEY)) {
                    mAddressMsg = (AddressMsg) data.getSerializableExtra(SET_KEY);
                    ToastR.setToast(context, "获取到的经度：" + mAddressMsg.getLon() + " 纬度：" + mAddressMsg.getLat());
                    Log.i("jim", "获取到的经度：" + mAddressMsg.getLon() + " 纬度：" + mAddressMsg.getLat());

                    longitude = mAddressMsg.getLon();
                    latitude = mAddressMsg.getLat();
                    toGetOtherLocation(0);
                    addMarkersToMap();
//                    setMapZoomLevel();


                } else {
                    ToastR.setToast(context, "没有获取到当前经纬度");
                }
                break;
            default:
                break;
        }
    }

    //查询路况
    private void toRoadConditionQuery(String phone) {

        ProtoMessage.StartVoiceMsg.Builder builder = ProtoMessage.StartVoiceMsg.newBuilder();
        builder.setToUserPhone(phone);
        MyService.start(context, ProtoMessage.Cmd.cmdLiveVideoCall.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(LiveVideoCallProcesser.ACTION);
        new TimeoutBroadcast(AmapSearchActivity.this, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(context, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    ToastR.setToast(context, "路况查询请求成功");
                } else {
                    new ResponseErrorProcesser(context, i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    class StrangerComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            StrangerLocationStatus contact1 = (StrangerLocationStatus) o1;
            StrangerLocationStatus contact2 = (StrangerLocationStatus) o2;
            int flag = 0;
            if (contact1.getRadius() > contact2.getRadius()) {
                flag = 1;
            } else if (contact1.getRadius() == contact2.getRadius()) {
                flag = 0;
            } else {
                flag = -1;
            }
            return flag;
        }
    }
}
