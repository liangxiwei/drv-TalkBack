package com.luobin.search.friends;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.SharedPreferencesUtils;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.ViewUtil;
import com.example.jrd48.chat.friend.MatchPhoneNumberActivity;
import com.example.jrd48.chat.group.TeamInfo;
import com.example.jrd48.chat.permission.PermissionUtil;
import com.example.jrd48.chat.search.SearchFriends;
import com.example.jrd48.chat.search.SearchListItemAdapter;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.GetCarLocationProcesser;
import com.example.jrd48.service.protocol.root.TypeSearchFriendsProcesser;
import com.luobin.dvr.R;
import com.luobin.model.SearchFriendsCondition;
import com.luobin.model.SearchStrangers;
import com.luobin.model.StrangerLocationStatus;
import com.luobin.model.ViewAllCarParam;
import com.luobin.myinfor.MyInforActivity;
import com.luobin.search.friends.car.DBManagerCarList;
import com.luobin.search.friends.car.SelectTypeActivity;
import com.luobin.search.friends.city.OptionsWindowHelper;
import com.luobin.search.friends.city.picverview.CharacterPickerWindow;
import com.luobin.tool.MyCarBrands;
import com.luobin.utils.ButtonUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/2/6.
 */

public class SearchFriendsByConditionActivity extends BaseActivity implements View.OnClickListener,View.OnLongClickListener,PermissionUtil.PermissionCallBack{
    private EditText mEditText;
    private ListView mListView;
    private ImageView ivBack;
    private TextView mTvCancel;
    private Context mContext;
    private Button btnMatchPhone;
    private RelativeLayout mRelativeLayoutCarType;
    private RelativeLayout mRelativeLayoutMyLocation;
    private RelativeLayout mRelativeLayoutMatchPhone;
    private RelativeLayout mRelativeLayoutCarBrand;
    private RelativeLayout mRelativeLayoutTitle;
    private RelativeLayout mRelativeLayoutBtn;
    private RelativeLayout mRelativeLayoutShow;
    private RelativeLayout mRelativeLayoutSearch;
    private RelativeLayout mRelativeLayoutDistance;
    private RelativeLayout mRelativeLayoutSex;
    private RelativeLayout mRelativeLayoutCarNumber;
    public static final String NOT_SET = "未设置";
    private TextView tvSex;
    private TextView mTextCarNumber;
    private FrameLayout mFrameLayoutInput;
    private Button mBtnSearch;
    private TextView tvMyLoation;
    private TextView mTextCarBrand;
    private TextView mTextCarType;
    public String mProvince = "";
    public String mCity = "";
    public String mTown = "";
    private String carType = "";
    private String carType2 = "";
    private String carBrand = "";
    private ProgressDialog m_pDialog;
    private boolean checkDialog = true;
    private SearchListItemAdapter mSearchListItemAdapter;
    List<TeamInfo> mTeamInfo = new ArrayList<TeamInfo>();
    List<SearchFriends> mNewAppliedFriends = new ArrayList<SearchFriends>();
    CharacterPickerWindow window;
    protected PermissionUtil mPermissionUtil;
    private ViewAllCarParam param;
    private final int selectBands = 1, selectTypes = 2,selectDistance = 3;
    private SearchFriendsCondition searchFriendsCondition;
    private String[] sexShow = new String[]{"男", "女", "未设置"};

    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private List<StrangerLocationStatus> strangerLocationStatusesList = new ArrayList<>();
    private double latitude;
    private double longitude;
    private ProgressDialog  myDialog;
    public static int MY_PERMISSIONS_REQUEST_LOCATION = 10043;

    private static final int CAR_NUMBER = 1;
    private static final int CAR_BRAND = 2;
    private static final int CAR_TYPE = 3;
    private static final int MY_LOCATION = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_friends);
        mContext = this;
        mPermissionUtil  = PermissionUtil.getInstance();
        initView();
        setMyLocalMsg("", "", "");
        registerReceiver(CheckAndUpdateCarTypeCompleteReceiver, new IntentFilter("CheckAndUpdateCarTypeComplete"));
        showCheckAndUpdateCarTypePrompt();
        refreshLocalData();
    }



    private BroadcastReceiver CheckAndUpdateCarTypeCompleteReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if ((int) SharedPreferencesUtils.get(mContext, "type_code", 1) != 1) {
                mRelativeLayoutCarBrand.setOnClickListener(carClickListener);
                mRelativeLayoutCarBrand.setBackgroundResource(R.drawable.dvr_layout_background);
                mRelativeLayoutCarType.setOnClickListener(carClickListener);
                mRelativeLayoutCarType.setBackgroundResource(R.drawable.dvr_layout_background);
                initCarBrandsData();
//                textView.setText("");
                if (myDialog != null) {
                    myDialog.cancel();
                }
            }
        }

    };

    private void initCarBrandsData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MyCarBrands.getInstance(mContext);
            }
        }).start();
    }

    private View.OnClickListener carClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.re_car_brand:
//                    CheckMsg.editviewFocusable(mEditText,mInputMethodManager);
                    Intent bandIntent = new Intent(mContext, SelectTypeActivity.class);
                    bandIntent.putExtra("requestCode", selectBands);
                    bandIntent.putExtra("requestBand", selectBands);
                    startActivityForResult(bandIntent, selectBands);
                    break;
                case R.id.re_car_type:
//                    CheckMsg.editviewFocusable(mEditText,mInputMethodManager);
                    Intent nextIntent = new Intent(mContext, SelectTypeActivity.class);
                    if (mTextCarBrand.getText().toString() == null || TextUtils.isEmpty(mTextCarBrand.getText().toString())) {
                        nextIntent.putExtra("requestCode", selectBands);
                        startActivityForResult(nextIntent, selectBands);
                    } else {
                        int fatherCode = new DBManagerCarList(mContext)
                                .getCarBandId(mTextCarBrand.getText().toString());
                        if (fatherCode == -1) {
                            nextIntent.putExtra("requestCode", selectBands);
                            startActivityForResult(nextIntent, selectBands);
                        } else {
                            nextIntent.putExtra("requestCode", selectTypes);
                            nextIntent.putExtra("fatherCode", fatherCode);
                            startActivityForResult(nextIntent, selectTypes);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void showCheckAndUpdateCarTypePrompt() {
        if ((int) SharedPreferencesUtils.get(mContext, "type_code", 1) == 1) {
            mRelativeLayoutCarBrand.setOnClickListener(null);
            mRelativeLayoutCarBrand.setBackgroundResource(R.drawable.btn_cannotclick_color);
            mRelativeLayoutCarType.setOnClickListener(null);
            mRelativeLayoutCarType.setBackgroundResource(R.drawable.btn_cannotclick_color);
//			textView.setText("正在同步汽车数据，请稍候选择汽车品牌和型号");
//            textView.setText("");
            myDialog = new ProgressDialog(this, R.style.CustomDialog);
            myDialog.setMessage("正在同步数据请稍候...");
            myDialog.setCancelable(true);
            myDialog.setIndeterminate(false);
            myDialog.show();
        } else {
            mRelativeLayoutCarBrand.setOnClickListener(carClickListener);
            mRelativeLayoutCarBrand.setBackgroundResource(R.drawable.dvr_layout_background);
            mRelativeLayoutCarType.setOnClickListener(carClickListener);
            mRelativeLayoutCarType.setBackgroundResource(R.drawable.dvr_layout_background);
            initCarBrandsData();
//            textView.setText("");
        }
    }

    private void refreshLocalData() {
        mTextCarType.setText(NOT_SET);
        mTextCarBrand.setText(NOT_SET);
        searchFriendsCondition = new SearchFriendsCondition();
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(CheckAndUpdateCarTypeCompleteReceiver);
        super.onDestroy();
    }



    private void initView() {
        mRelativeLayoutDistance = (RelativeLayout) findViewById(R.id.re_by_distance);
        mRelativeLayoutDistance.setOnClickListener(this);
        mRelativeLayoutCarType = (RelativeLayout) findViewById(R.id.re_car_type);
        mRelativeLayoutCarType.setOnLongClickListener(this);
        mRelativeLayoutSex = (RelativeLayout) findViewById(R.id.re_my_sex);
        mRelativeLayoutSex.setOnClickListener(this);
        mRelativeLayoutCarNumber = (RelativeLayout) findViewById(R.id.re_my_car_number);
        mRelativeLayoutCarNumber.setOnClickListener(this);
        mRelativeLayoutCarNumber.setOnLongClickListener(this);
        mRelativeLayoutMatchPhone = (RelativeLayout) findViewById(R.id.re_match_phone);
        mRelativeLayoutMatchPhone.setOnClickListener(this);
        mRelativeLayoutCarBrand = (RelativeLayout) findViewById(R.id.re_car_brand);
        mRelativeLayoutCarBrand.setOnLongClickListener(this);
        mRelativeLayoutTitle = (RelativeLayout) findViewById(R.id.title);
        mRelativeLayoutBtn = (RelativeLayout) findViewById(R.id.rl_btn_search);
        mRelativeLayoutShow = (RelativeLayout) findViewById(R.id.rl_show_title);
        mFrameLayoutInput = (FrameLayout) findViewById(R.id.fl_input);
        mRelativeLayoutSearch = (RelativeLayout) findViewById(R.id.ll_search);
        mRelativeLayoutSearch.setOnClickListener(this);
        mRelativeLayoutMyLocation = (RelativeLayout) findViewById(R.id.re_my_loation);
        mRelativeLayoutMyLocation.setOnClickListener(this);
        mRelativeLayoutMyLocation.setOnLongClickListener(this);
        mBtnSearch = (Button) findViewById(R.id.btn_search);
        mBtnSearch.setOnClickListener(this);
        tvMyLoation = (TextView) findViewById(R.id.tv_my_loation);
        mTextCarBrand = (TextView) findViewById(R.id.tv_car_brand);
        tvSex = (TextView) findViewById(R.id.tv_my_sex);
        mTextCarNumber = (TextView) findViewById(R.id.tv_my_car_number);
        mTextCarType = (TextView) findViewById(R.id.tv_car_type);
        btnMatchPhone = (Button) findViewById(R.id.btn_match_phone);
        btnMatchPhone.setOnClickListener(this);
        mListView = (ListView) findViewById(R.id.lv_search);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);
        mEditText = (EditText) findViewById(R.id.et_search);
        mTvCancel = (TextView) findViewById(R.id.tv_cancel);
        mTvCancel.setOnClickListener(this);
        Drawable drawable1 = getResources().getDrawable(R.drawable.search);
        int s = ViewUtil.dip2px(SearchFriendsByConditionActivity.this, 32);
        drawable1.setBounds(0, 0, s, s);//第一0是距左边距离，第二0是距上边距离，32分别是长宽
        mEditText.setCompoundDrawables(drawable1, null, null, null);//只放左边

        mSearchListItemAdapter = new SearchListItemAdapter(this, mNewAppliedFriends);
        mListView.setAdapter(mSearchListItemAdapter);
        mListView.setSelector(R.drawable.submenu_default);

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

    private InputMethodManager imm;
    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.re_my_car_number:
                if (!mTextCarNumber.getText().equals(NOT_SET)) {
                    ClearData(CAR_NUMBER, "车牌号");
                }
                break;
            case R.id.re_my_loation:
                if (!tvMyLoation.getText().equals(NOT_SET)) {
                    ClearData(MY_LOCATION, "所在地");
                }
                break;
            case R.id.re_car_brand:
                if (!mTextCarBrand.getText().equals(NOT_SET)) {
                    ClearData(CAR_BRAND, "车品牌");
                }
                break;
            case R.id.re_car_type:
                if (!mTextCarType.getText().equals(NOT_SET)) {
                    ClearData(CAR_TYPE, "车型");
                }
                break;
            default:
                break;
        }
        return false;
    }


    public void ClearData(final int type,final String str) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext); // 先得到构造器
        builder.setMessage("是否要清除之前设置的"+str+"数据？").setTitle("提示：").setPositiveButton("确定", new android.app.AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                toClearData(type);
//                toUnbind();
            }
        }).setNegativeButton("取消", new android.app.AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();

    }

    private void toClearData(int type) {

      if (type == CAR_NUMBER){
          mTextCarNumber.setText(NOT_SET);
      } else if (CAR_BRAND == type){
          carBrand = "";
          carType = "";
          carType2 = "";
          mTextCarBrand.setText(NOT_SET);
          mTextCarType.setText(NOT_SET);
      } else if (CAR_TYPE == type){
          carType = "";
          carType2 = "";
          mTextCarType.setText(NOT_SET);
      } else if (MY_LOCATION == type){
          mProvince ="";
          mCity ="";
          mTown ="";
          tvMyLoation.setText(NOT_SET);
      }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                m_pDialog.dismiss();
                finish();
                break;
            case R.id.re_match_phone:
                m_pDialog.dismiss();
                Intent intent = new Intent(mContext, MatchPhoneNumberActivity.class); // 跳转界面到
                startActivity(intent);
                break;
            case R.id.tv_cancel:
                //TODO 输入框设置
                toChangeView(true);
                mEditText.setText("");
                imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);//隐藏软键盘
//                mEditText.setText("");
                break;
            case R.id.ll_search:

                toChangeView(false);
                imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);//显示软键盘
                mEditText.requestFocus();//设置输入框焦点，etSearch为输入框控件
//                String phoneOrName = mEditText.getText().toString().trim();
//                searchFriend(phoneOrName);
                break;
            case R.id.re_my_car_number:
            //车牌号
                plateNumberDialog();
                break;
            case R.id.re_my_sex:
            //性别
                sexDialog();
                break;
            case R.id.re_my_loation:
                window.showAtLocation(view, Gravity.BOTTOM, 0, 0);
                //我的位置
                break;
            case R.id.btn_search:
                toSearchFriends();
                break;
            case R.id.re_by_distance:
                //TODO 距离查询
                startActivityForResult(new Intent(SearchFriendsByConditionActivity.this,Tankuangactivity.class),selectDistance);
                break;
            default:
                break;
        }
    }



    public void plateNumberDialog() {
        //获取xml布局文件对象
        LayoutInflater factory = LayoutInflater.from(mContext);// 提示框
        final View view = factory.inflate(R.layout.setname_layout, null);// 这里必须是final的
        final EditText name = (EditText) view.findViewById(R.id.set_name);// 获得输入框对象

        boolean isSetText = true;
        if (mTextCarNumber.getText().toString().equals(NOT_SET)){
            isSetText = false;
        }
        name.setSelection(name.length());
        name.setHint("至少输入后4位");
        if (mTextCarNumber.getText().toString().length() > 0 && isSetText) {
            name.setText(mTextCarNumber.getText().toString());
            name.setSelection(name.length());
        }
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("设置车牌号");
        //设置对话框显示的View组件
        builder.setView(view);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

                if (name.getText().toString().length() <= 3) {
                    // 条件不成立不能关闭 AlertDialog 窗口
                    ToastR.setToastLong(mContext, "车牌号输入太短，不得少于4位");
                } else {
                    String plateNumber = name.getText().toString();
                    mTextCarNumber.setText(plateNumber);
                    searchFriendsCondition.setCarPlateNumber(plateNumber);
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        //builder创建对话框对象AlertDialog
        android.app.AlertDialog viewdialog = builder.create();
        viewdialog.show();
    }

    //******************************设置性别弹窗******************************
    private int sexdatashow = 2;
    private int sexdefualt;
    private int sexdata = 0;
    public void sexDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置性别");
        //设置单选列表项，默认选中第二项
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                if(keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        ButtonUtils.changeLeftOrRight(true);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        ButtonUtils.changeLeftOrRight(false);
                        return true;
                    }
                }
                return false;
            }
        })
        .setSingleChoiceItems(sexShow, sexdatashow, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                sexdefualt = which;

            }
        })
        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                ToastR.setToast(mContext, "你选择了：" + sexShow[sexdefualt]);
                sexdata = sexdefualt + 1;
                sexdatashow = sexdefualt;
                tvSex.setText(sexShow[sexdefualt]);
                sexdata = (sexdata == 3 ? 0 : sexdata);
                searchFriendsCondition.setmSex(sexdata);
            }
        })
//        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                // TODO Auto-generated method stub
////                ToastR.setToast(MyInforActivity.this, "你取消了性别设置");
//            }
//        })
          .create().show();
    }

    private void toSearchFriends() {

        ProtoMessage.MsgSearchCar.Builder builder = ProtoMessage.MsgSearchCar.newBuilder();
        builder.setProv(mProvince);
        builder.setCity(mCity);
        builder.setTown(mTown);
        builder.setCarType1(carBrand);
        builder.setCarType2(carType);
//        builder.setCarType3(carType2);
        builder.setSex(sexdata);
        if (mTextCarNumber.getText().equals(NOT_SET)) {
            builder.setCarNum("");
        } else {
            builder.setCarNum(mTextCarNumber.getText().toString());
        }

        MyService.start(mContext, ProtoMessage.Cmd.cmdSearchCar.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(TypeSearchFriendsProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(mContext, filter, getBroadcastManager());
        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK_VALUE) {
                    ArrayList<SearchStrangers> userInfoList = (ArrayList<SearchStrangers>) i.getSerializableExtra("user_info");
                    if (userInfoList == null || userInfoList.size() <= 0) {
                        ToastR.setToastLong(mContext, "未找到相关的陌生人");
                    } else {
                        Intent intent = new Intent(SearchFriendsByConditionActivity.this, SearchReturnActivity.class);
                        intent.putExtra("user_info", (Serializable) userInfoList);
                        intent.putExtra("conditon", searchFriendsCondition);
                        startActivity(intent);
                    }
                } else {
                    new ResponseErrorProcesser(mContext, i.getIntExtra("error_code", -1));
                }

            }
        });


    }

    private void toChangeView(boolean isShow) {
        if (isShow) {
            mTvCancel.setVisibility(View.GONE);
            mFrameLayoutInput.setVisibility(View.GONE);
            mRelativeLayoutCarType.setVisibility(View.VISIBLE);
            mRelativeLayoutMyLocation.setVisibility(View.VISIBLE);
            mRelativeLayoutMatchPhone.setVisibility(View.VISIBLE);
            mRelativeLayoutCarBrand.setVisibility(View.VISIBLE);
            mRelativeLayoutShow.setVisibility(View.VISIBLE);
            mRelativeLayoutTitle.setVisibility(View.VISIBLE);
            mRelativeLayoutBtn.setVisibility(View.VISIBLE);
        } else {
            mTvCancel.setVisibility(View.VISIBLE);
            mFrameLayoutInput.setVisibility(View.VISIBLE);
            mRelativeLayoutCarType.setVisibility(View.GONE);
            mRelativeLayoutMyLocation.setVisibility(View.GONE);
            mRelativeLayoutMatchPhone.setVisibility(View.GONE);
            mRelativeLayoutCarBrand.setVisibility(View.GONE);
            mRelativeLayoutShow.setVisibility(View.GONE);
            mRelativeLayoutTitle.setVisibility(View.GONE);
            mRelativeLayoutBtn.setVisibility(View.GONE);
        }
    }
    private int requestBand;
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
            case selectBands:
                String brand = data.getStringExtra("carBandText");
                requestBand = data.getIntExtra("requestBand", 0);
                param.setCarBrandText(brand);
                if (requestBand == 0) {
                    param.setCarTypeText(data.getStringExtra("carTypeText"));
                    param.setCarTypeId(data.getIntExtra("carTypeId", 0));
                } else {
                    param.setCarTypeText("");
                    param.setCarTypeId(0);
                }
                toEditBrandAndCarType(param, true);
                break;
            case selectTypes:
                param.setCarTypeText(data.getStringExtra("carTypeText"));
                param.setCarTypeId(data.getIntExtra("carTypeId", 0));
                requestBand = data.getIntExtra("requestBand", 0);
                toEditBrandAndCarType(param, false);
                break;

            case selectDistance:
                int radius = data.getIntExtra("data",0);
                if (radius > 0){
                    isFistLcation = false;
                    mRadius = radius;
                    mPermissionUtil.requestPermission1(SearchFriendsByConditionActivity.this, PermissionUtil.MY_READ_LOCATION, SearchFriendsByConditionActivity.this,
                                    PermissionUtil.PERMISSIONS_LOCATION, PermissionUtil.PERMISSIONS_LOCATION);
                        }
                break;
            default:
                break;
        }

    }

    @Override
    public void onPermissionSuccess(String type) {
        start(mRadius);
    }

    @Override
    public void onPermissionReject(String strMessage) {
        ToastR.setToastLong(mContext, "[ 定位 ] 权限已经拒绝，请到手机管家或者系统设置里授权");
    }

    @Override
    public void onPermissionFail(String failType) {
        ToastR.setToastLong(mContext, "[ 定位 ] 权限设置失败，请到手机管家或者系统设置里授权");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //权限申请结果
        if (requestCode == PermissionUtil.MY_READ_LOCATION) {
            mPermissionUtil.requestResult(this, permissions, grantResults, this, PermissionUtil.PERMISSIONS_LOCATION);
        }
    }

    boolean isFistLcation = false;
    int mRadius;
    int isToast = 0;
    public void start(final int radius) {
        try {
            isToast = 0;
            locationClient = new AMapLocationClient(mContext);
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
                        toGetOtherLocation(radius);
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

    private void toGetOtherLocation(final int radius) {
        Log.i("jim","按距离查询陌生人设置条件:"+ " radius:"+radius+"公里"+ " latitude:"+latitude+" longitude:"+longitude);
        ProtoMessage.MsgSearchAround.Builder builder = ProtoMessage.MsgSearchAround.newBuilder();
        builder.setLat(latitude);
        builder.setLng(longitude);
        builder.setRadius(radius*1000);  //半径(米)
        builder.setPos(0);// 批次：0,第一批，1第二批，...， 每次100个
        //TODO 获取定位位置
        MyService.start(mContext, ProtoMessage.Cmd.cmdSearchAround.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(GetCarLocationProcesser.ACTION);
        new TimeoutBroadcast(mContext, filter, getBroadcastManager()).startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
//                ToastR.setToast(mContext, "连接超时");
            }

            @Override
            public void onGot(Intent intent) {
                int errorCode = intent.getIntExtra("error_code", -1);
                if (errorCode == ProtoMessage.ErrorCode.OK.getNumber()) {
                    strangerLocationStatusesList.clear();
                    strangerLocationStatusesList = (ArrayList<StrangerLocationStatus>) intent.getSerializableExtra("stranger_location");
                    stop();
                    if (strangerLocationStatusesList.size() > 0) {
                        Intent intent1 = new Intent(SearchFriendsByConditionActivity.this,DistanceSearchReturnActivity.class);
                        intent1.putExtra("stranger_location", (Serializable) strangerLocationStatusesList);
                        intent1.putExtra("latitude",latitude);
                        intent1.putExtra("longitude",longitude);
                        intent1.putExtra("radius",radius);
                        startActivity(intent1);
                    } else {
                        ToastR.setToastLong(mContext, "未找到相关的陌生人");
                    }
//                    setMapZoomLevel();

                } else {
//                    ToastR.setToast(mContext, "查看位置失败: 错误码 " + errorCode);
                }
            }
        });


    }

    public void stop() {
        try {
            if (null != locationClient) {
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

    private void toEditBrandAndCarType(final ViewAllCarParam param, final boolean isEditCarBrand) {
        if (requestBand == 0) {
            carType = param.getCarTypeText();
//            int endIndex = carType.indexOf(" ");
//            carType1 = carType.substring(0, endIndex);
//            carType2 = carType.substring(endIndex, carType.length());
            searchFriendsCondition.setCarType1(carType);
            searchFriendsCondition.setCarType2("");
            if (isEditCarBrand) {
                carBrand = param.getCarBrandText();
                searchFriendsCondition.setCarBrand(carBrand);
                mTextCarBrand.setText(param.getCarBrandText());
            }
            mTextCarType.setText(param.getCarTypeText());
        } else {
            carBrand = param.getCarBrandText();
            carType = "";
            carType2 = "";
            searchFriendsCondition.setCarBrand(carBrand);
            mTextCarBrand.setText(param.getCarBrandText());
            searchFriendsCondition.setCarType1("");
            searchFriendsCondition.setCarType2("");
            mTextCarType.setText(NOT_SET);
        }
    }


    private void setMyLocalMsg(String province, String city, String town) {
        String st = "0";
        String str1 = "不限";
        String str2 = " ";
        String str3 = " ";
        String str = "不限";
        if (province == null || province.equals("")) {
            str = NOT_SET;
        } else if (city.equals(st)) {
            str = province;
            if (!province.equals(MyInforActivity.NO_SET)) {
                str1 = province;
                str2 = MyInforActivity.NO_SET;
            }
        } else if (province.equals(st) && town.equals(st) && !city.equals(st)) {
            str = city;
            str1 = MyInforActivity.OTHER;
            str2 = city;
        } else if (!province.equals(st) && town.equals(st) && !city.equals(st)) {
            str = province + "-" + city;
            str1 = province;
            str2 = city;
            str3 = MyInforActivity.NO_SET;
        } else {
            str = province + "-" + city + "-" + town;
            str1 = province;
            str2 = city;
            str3 = town;
        }

        tvMyLoation.setText(str);

        window = OptionsWindowHelper.builder(str1, str2, str3, "", SearchFriendsByConditionActivity.this, new OptionsWindowHelper.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(String province, String city, String area) {
//                Toast.makeText(mContext, province + "," + city + "," + area, Toast.LENGTH_SHORT).show();
                setMyLocationText(province, city, area);
            }
        });

    }

    private void setMyLocationText(String province, String city, String town) {
        mTown = "";
        mProvince = "";
        mTown = "";
        String str = "";
        if (province.equals(MyInforActivity.NO_SET) || city.equals(MyInforActivity.NO_SET)) {
            mProvince = province;
            str = province;
        } else if (town.equals(MyInforActivity.NO_SET)) {
            mProvince = province;
            mCity = city;
            str = province + "-" + city;
        } else if (province.equals(MyInforActivity.OTHER)) {
            mProvince = city;
            str = city;
        } else {
            if (town == null || town.length() <= 0) {
                mProvince = province;
                mCity = city;
                str = province + "-" + city;
            } else {
                mProvince = province;
                mCity = city;
                mTown = town;
                str = province + "-" + city + "-" + town;
            }
        }
        searchFriendsCondition.setmCity(mCity);
        searchFriendsCondition.setmProvince(mProvince);
        searchFriendsCondition.setmTown(mTown);
        tvMyLoation.setText(str);

    }

}
