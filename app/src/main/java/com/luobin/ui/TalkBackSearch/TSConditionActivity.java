package com.luobin.ui.TalkBackSearch;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.CustomListener;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.TypeSearchFriendsProcesser;
import com.luobin.dvr.R;
import com.luobin.model.CarBrands;
import com.luobin.model.CarFirstType;
import com.luobin.model.City;
import com.luobin.model.County;
import com.luobin.model.Province;
import com.luobin.model.SearchFriendsCondition;
import com.luobin.model.SearchStrangers;
import com.luobin.search.friends.car.DBManagerCarList;
import com.luobin.tool.MyCarBrands;
import com.luobin.ui.BaseDialogActivity;
import com.luobin.ui.CarInfoBean_New;
import com.luobin.ui.InputTextDialog;
import com.luobin.ui.InterestBean;
import com.luobin.ui.SelectInterestAdapter;
import com.luobin.ui.SelectInterestDialog;
import com.luobin.ui.TalkBackSearch.adapter.TSConditionPersionAdapter;
import com.luobin.utils.ButtonUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author wangjunjie
 */
public class TSConditionActivity extends BaseDialogActivity implements
        SelectInterestAdapter.OnRecyclerViewItemClickListener {

    @BindView(R.id.imgClose)
    ImageView imgClose;

    @BindView(R.id.btnSex)
    Button btnSex;

    @BindView(R.id.btnCar)
    Button btnCar;

    @BindView(R.id.btnAge)
    Button btnAge;

    @BindView(R.id.btnAddress)
    Button btnAddress;

    @BindView(R.id.btnIndustry)
    Button btnIndustry;

    @BindView(R.id.btnLikeGood)
    Button btnLikeGood;

    @BindView(R.id.btnSearch)
    Button btnSearch;

    @BindView(R.id.tvSex)
    TextView tvSex;

    @BindView(R.id.tvCar)
    TextView tvCar;

    @BindView(R.id.tvAge)
    TextView tvAge;

    @BindView(R.id.tvAddress)
    TextView tvAddress;

    @BindView(R.id.tvIndustry)
    TextView tvIndustry;

    @BindView(R.id.tvLikeGood)
    TextView tvLikeGood;

    public static final String NOT_SET = "未设置";

    private Context context = null;
    //实体类
    private SearchFriendsCondition searchFriendsCondition;
    private String[] sexShow = new String[]{"男", "女", "未设置"};
    private ProgressDialog m_pDialog;
    private boolean checkDialog = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tscondition);
        ButterKnife.bind(this);
        context = this;
        getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);

        initData();
        initDialog();
    }

    private void initData() {
        searchFriendsCondition = new SearchFriendsCondition();

    }

    private void initDialog() {
        //********************************************弹窗设置**************************
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
                    ToastR.setToast(context, "取消在线搜索");
                }
            }
        });
        //********************************************弹窗设置**************************

    }


    @OnClick({R.id.imgClose, R.id.btnSex, R.id.btnCar, R.id.btnAge,
            R.id.btnAddress, R.id.btnIndustry, R.id.btnLikeGood, R.id.btnSearch})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgClose:
                finish();
                break;
            case R.id.btnSex:
                sexDialog();
                break;
            case R.id.btnCar:
                selectTypeDialog(2, DIALOG_TYPE.CARTYPE);
                break;
            case R.id.btnAge:

                InputTextDialog inputTextDialog = new InputTextDialog(context, "年纪",
                        InputTextDialog.Type.NUMBER, new InputTextDialog.GetPasswordListener() {
                    @Override
                    public void getPassword(String password) {
                        tvAge.setText(password);
                    }
                });
                inputTextDialog.show();
                break;
            case R.id.btnAddress:
                setPickerData();
                selectTypeDialog(2, DIALOG_TYPE.ADDRESS);

                break;
            case R.id.btnIndustry:
                selectTypeDialog(1, DIALOG_TYPE.INDUSTRY);
                break;
            case R.id.btnLikeGood:
                selectInterestDialog();
                break;
            case R.id.btnSearch:
                //TODO 搜索
                searchFriends();
                break;
            default:
                break;
        }
    }

    private int sexdata = 0;
    public String mProvince = "";
    public String mCity = "";
    private String carType = "";
    private String carBrand = "";

    private void searchFriends() {

        ProtoMessage.MsgSearchCar.Builder builder = ProtoMessage.MsgSearchCar.newBuilder();
        builder.setProv(mProvince);
        builder.setCity(mCity);
        builder.setTown("");
        builder.setCarType1(carBrand);
        builder.setCarType2(carType);
        builder.setSex(sexdata);
        //TODO 还有 爱好 行业 年纪


        MyService.start(context, ProtoMessage.Cmd.cmdSearchCar.getNumber(), builder.build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(TypeSearchFriendsProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(context, filter, getBroadcastManager());
        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(context, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK_VALUE) {
                    ArrayList<SearchStrangers> userInfoList =
                            (ArrayList<SearchStrangers>) i.getSerializableExtra("user_info");
                    if (userInfoList == null || userInfoList.size() <= 0) {
                        ToastR.setToastLong(context, "未找到相关的陌生人");
                    } else {
                        Intent intent = new Intent(context, TSConditionPersonActivity.class);
                        intent.putExtra("user_info", (Serializable) userInfoList);
                        intent.putExtra("conditon", searchFriendsCondition);
                        startActivity(intent);
                        //TODO 跳转好友列表
                    }
                } else {
                    new ResponseErrorProcesser(context, i.getIntExtra("error_code", -1));
                }

            }
        });
    }

    private int sexdatashow = 2;
    private int sexdefualt;

    public void sexDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置性别");
        //设置单选列表项，默认选中第二项
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
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
                        sexdefualt = which;

                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ToastR.setToast(context, "你选择了：" + sexShow[sexdefualt]);
                        sexdata = sexdefualt + 1;
                        sexdatashow = sexdefualt;
                        tvSex.setText(sexShow[sexdefualt]);
                        sexdata = (sexdata == 3 ? 0 : sexdata);
                        searchFriendsCondition.setmSex(sexdata);
                    }
                })
                .create().show();
    }

    @Override
    public void onItemClick(List<InterestBean> interestBeans) {
        Log.i("aihao","shujju");
        //TODO 设置兴趣爱好
        String interestName = "";
        for (int a = 0; a < interestBeans.size(); a++) {
            if (interestBeans.get(a).isChecked()) {
                interestName += interestBeans.get(a).getName() + ",";
            }

        }
        tvLikeGood.setText(interestName);

    }


    public enum DIALOG_TYPE {
        //车型
        CARTYPE,
        //ADDRESS
        ADDRESS,
        //行业
        INDUSTRY,
    }

    private DIALOG_TYPE type;

    /**
     * 别的选择控件
     */
    OptionsPickerView pvOptions = null;
    private static ArrayList<String> options1Items = null;
    private static ArrayList<ArrayList<String>> options2Items = null;
    protected static ArrayList<ArrayList<ArrayList<String>>> options3Items = null;

    void selectTypeDialog(final int num, final DIALOG_TYPE type) {
        pvOptions = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                //返回的分别是三个级别的选中位置
                String one = "";
                String tx = "";
                String two = "";
                if (type == DIALOG_TYPE.CARTYPE) {
                    one = car1items.get(options1);
                    tx = "";

                    if (num == 1) {
                        tx = one;
                    } else if (num == 2) {
                        two = car2items.get(options1).get(options2);
                        tx = one + two;
                    }
                } else {
                    one = options1Items.get(options1);
                    tx = "";

                    if (num == 1) {
                        tx = one;
                    } else if (num == 2) {
                        two = options2Items.get(options1).get(options2);
                        tx = one + two;
                    }
                }

                if (type == DIALOG_TYPE.CARTYPE) {
                    carBrand = one;
                    carType = two;
                    tvCar.setText(tx);
                } else if (type == DIALOG_TYPE.ADDRESS) {
                    mProvince = one;
                    mCity = two;
                    tvAddress.setText(tx);
                } else if (type == DIALOG_TYPE.INDUSTRY) {
                    //TODO 行业 String X =one;

                    tvIndustry.setText(tx);
                }


            }
        }).setLayoutRes(R.layout.dialog_select, new CustomListener() {
            @Override
            public void customLayout(View v) {
                TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);

                if (type == DIALOG_TYPE.CARTYPE) {
                    tvTitle.setText("车型");
                } else if (type == DIALOG_TYPE.ADDRESS) {
                    tvTitle.setText("所在地");
                } else if (type == DIALOG_TYPE.INDUSTRY) {
                    tvTitle.setText("行业");
                }

                Button btnSure = (Button) v.findViewById(R.id.btnSure);
                btnSure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pvOptions.returnData();
                        pvOptions.dismiss();


                    }
                });

            }
        })
                .setDividerColor(getResources().getColor(R.color.dialog_color_line))
                //设置选中项文字颜色
                .setTextColorCenter(getResources().getColor(R.color.text_color_90white))
                .setContentTextSize(20)
                .build();

        if (num == 1) {
            //TODO 行业假数据 测试数据坑死爹
            options1Items = new ArrayList<>();
            String[] testData = {"java开发-假数据", "C开发-假数据",
                    "产品经理-假数据", "android开发-假数据"
                    , "ios开发-假数据", "RN开发-假数据"
                    , "前端开发-假数据"};
            for (String a : Arrays.asList(testData)) {
                options1Items.add(a);
            }

            //二级选择器*/
            pvOptions.setPicker(options1Items);
        } else if (num == 2) {
            //二级选择器*/
            if (type == DIALOG_TYPE.CARTYPE) {

                if (car1items == null || car1items.size() < 1) {
                    ToastR.setToast(context, "数据为空正在获取汽车品牌");
                    getCarList();
                    return;
                }

                pvOptions.setPicker(car1items, car2items);
            } else {
                pvOptions.setPicker(options1Items, options2Items);
            }
        }


        pvOptions.show();
    }

    /**
     * CarBrands 车数据
     */
    ArrayList<CarInfoBean_New> carDataNew = new ArrayList<>();
    ArrayList<CarFirstType> carFirstData = new ArrayList<>();
    ArrayList<String> data = new ArrayList<>();
    private static ArrayList<String> car1items = new ArrayList<>();
    private static ArrayList<ArrayList<String>> car2items = new ArrayList<>();

    /**
     * 获取车辆型号
     */
    private void getCarList() {
        car1items = new ArrayList<>();
        car2items = new ArrayList<ArrayList<String>>();

        DBManagerCarList carListDBM = new DBManagerCarList(this);

        ArrayList<CarBrands> carData = MyCarBrands.getData();
        if (carData == null || carData.size() <= 0) {
            carData = carListDBM.getCarBrandsList(false);
        }

        carListDBM.closeDB();

        for (CarBrands carBrands : carData) {
            CarInfoBean_New carInfoBean_new = new CarInfoBean_New();
            carInfoBean_new.setCarBrandID(carBrands.getCarBrandID());
            carInfoBean_new.setCarBrandName(carBrands.getCarBrandName());
            carInfoBean_new.setVersion(carBrands.getVersion());
            carDataNew.add(carInfoBean_new);
            car1items.add(carBrands.getCarBrandName());
        }


        for (int i = 0; i < carDataNew.size(); i++) {
            carFirstData = new DBManagerCarList(this).
                    getCarFristTypesList(carDataNew.get(i).getCarBrandID());
            CarInfoBean_New carInfoBean = new CarInfoBean_New();
            carInfoBean.setCarFirstTypes(carFirstData);
            carInfoBean.setVersion(carDataNew.get(i).getVersion());
            carInfoBean.setCarBrandName(carDataNew.get(i).getCarBrandName());
            carInfoBean.setCarBrandID(carDataNew.get(i).getCarBrandID());
            carDataNew.set(i, carInfoBean);

        }

        for (int i = 0; i < carDataNew.size(); i++) {
            data = new ArrayList<>();

            for (int i1 = 0; i1 < carDataNew.get(i).getCarFirstTypes().size(); i1++) {
                data.add(carDataNew.get(i).getCarFirstTypes().get(i1).getCarFirstTypeName());
                Log.i("data is Show >", carDataNew.get(i).getCarFirstTypes().get(i1)
                        .getCarFirstTypeName());
            }
            car2items.add(data);

        }
    }

    String[] list = {"汽车", "旅游", "动漫", "影视",
            "时尚", "音乐", "体育", "美食",
            "摄影", "宠物", "钓鱼", "工艺",
            "手工", "游戏"};
    List<InterestBean> interestList = new ArrayList<>();

    /**
     * 兴趣爱好
     */
    private void selectInterestDialog() {
        interestList = new ArrayList<>();
        for (int a = 0; a < Arrays.asList(list).size(); a++) {
            InterestBean interestBean = new InterestBean();
            interestBean.setName(Arrays.asList(list).get(a));
            interestBean.setChecked(false);
            interestList.add(interestBean);
        }

        SelectInterestDialog selectInterestDialog = new SelectInterestDialog(this
                , interestList);
        selectInterestDialog.getAdapter().setOnItemClickListener(this);
        selectInterestDialog.show();

    }

    /**
     * 初始化选项数据 3级列表的城市
     */
    public void setPickerData() {
        options1Items = new ArrayList();
        options2Items = new ArrayList<>();
        options3Items = new ArrayList();

        ArrayList<Province> data = new ArrayList<Province>();
        try {
            String json;
            json = convertString(this.getAssets().open("cityand.json"), "utf-8");
            data.addAll(JSON.parseArray(json, Province.class));
        } catch (Exception e) {
            Log.e("helper", " get data error:" + e.getMessage());
            e.printStackTrace();
        }
        //添加省
        for (int x = 0; x < data.size(); x++) {
            Province pro = data.get(x);
            options1Items.add(pro.getAreaName());
            ArrayList<City> cities = pro.getCities();
            ArrayList<String> xCities = new ArrayList<String>();
            ArrayList<ArrayList<String>> xCounties = new ArrayList<>();
            int citySize = cities.size();
            //添加地市
            for (int y = 0; y < citySize; y++) {
                City cit = cities.get(y);
                xCities.add(cit.getAreaName());
                ArrayList<County> counties = cit.getCounties();
                ArrayList<String> yCounties = new ArrayList<String>();
                int countySize = counties.size();
                //添加区县
                if (countySize == 0) {

                    yCounties.add(cit.getAreaName());
                } else {
                    for (int z = 0; z < countySize; z++) {
                        yCounties.add(counties.get(z).getAreaName());
                    }
                }
                xCounties.add(yCounties);
            }

            options2Items.add(xCities);
            options3Items.add(xCounties);
        }


    }

    public String convertString(InputStream is, String charset) {
        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            is.close();
        } catch (IOException e) {
            Log.e("convertString", e.toString());
        }
        return sb.toString();
    }


}
