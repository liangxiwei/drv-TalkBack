package com.luobin.search.friends.car.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.luobin.dvr.R;
import com.luobin.model.CarBrands;
import com.luobin.model.CarFirstType;
import com.luobin.model.CarLastTypes;
import com.luobin.search.friends.car.DBManagerCarList;
import com.luobin.search.friends.car.listForSelectUsed.CharacterParser;
import com.luobin.search.friends.car.listForSelectUsed.ClearEditText;
import com.luobin.search.friends.car.listForSelectUsed.PinyinComparator;
import com.luobin.search.friends.car.listForSelectUsed.SideBar;
import com.luobin.search.friends.car.listForSelectUsed.SortAdapter;
import com.luobin.search.friends.car.listForSelectUsed.SortModel;
import com.luobin.search.friends.car.listForSelectUsed.view.PullToZoomListViewEx;
import com.luobin.tool.MyCarBrands;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;


public class SelectCarBrandsAndTypesFragment extends Fragment {
    private View view;
    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser characterParser;
    /**
     * 数据源
     */
    private List<SortModel> sourceDateList;
    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;
    /**
     * 数据显示用
     */
    private PullToZoomListViewEx sortListView;
    /**
     * List右侧索引选择
     */
    private SideBar sideBar;
    /**
     * 右侧索引选择显示
     */
    private TextView dialog;
    /**
     * list的数据填充器
     */
    private SortAdapter adapter;
    /**
     * 搜索框
     */
    private ClearEditText mClearEditText;
    /**
     * 汽车品牌选择的头部数据
     */
    private int[] listHeaderViewIdArray = new int[]{R.id.listViewHeaderView1, R.id.listViewHeaderView2, R.id.listViewHeaderView3,
            R.id.listViewHeaderView4, R.id.listViewHeaderView5, R.id.listViewHeaderView6, R.id.listViewHeaderView7, R.id.listViewHeaderView8};
    /**
     * 汽车品牌选择的头部数据
     */
    private String[] listHeaderViewTextArray = new String[]{"大众", "奥迪", "奔驰", "宝马", "比亚迪", "别克", "福特", "现代"};

    /**
     * 右侧索引字母集合
     */
    private TreeSet<String> sideBarString = new TreeSet<String>();

    private int requestCode;

    private Integer fatherCode;

    private ProgressDialog m_pDialog;

    ArrayList<CarBrands> data;


    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
//                    mCurrentPhotoPath = msg.getData().getString("path", "");
//                    Log.i(SendPic.TAG, mCurrentPhotoPath);
                    data = (ArrayList<CarBrands>)msg.obj;
                    Log.i("ces","2:"+getTimeString());
//                    sourceDateList = filledBrandsData(data);
                    if (m_pDialog != null) {
                        m_pDialog.dismiss();
                    }
//                    setAdapter();
//                    Log.i("ces","3:"+getTimeString());
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 用户处理list的OnItemSelected事件或者是OnItemClick时间的回调 交给Activity处理
     */
    private SelectFragmentCallbackI selectFragmentCallbackI;

    public SelectCarBrandsAndTypesFragment() {
        super();
    }

    public SelectCarBrandsAndTypesFragment(int requestCode, Integer fatherCode) {
        super();
        this.requestCode = requestCode;
        this.fatherCode = fatherCode;
    }


    public void setFatherCode(Integer fatherCode) {
        this.fatherCode = fatherCode;
        //initData(requestCode, fatherCode);
        //setAdapter();
    }

    public void setSelectFragmentCallbackI(SelectFragmentCallbackI selectFragmentCallbackI) {
        this.selectFragmentCallbackI = selectFragmentCallbackI;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /*@Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (requestCode == 1){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DBManagerCarList carListDBM = new DBManagerCarList(getActivity());
                    ArrayList<CarBrands> data = carListDBM.getCarBrandsList();
                    Message message = new Message();
                    message.obj = data;
                    message.what = 1;//标志是哪个线程传数据
                    mHandler.handleMessage(message);
                }
            }).start();
        }

    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.select_fragment, container, false);
        initView();
        initData(requestCode, fatherCode);
        setAdapter();
        return view;
    }

    private void initView() {
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
        sideBar = (SideBar) view.findViewById(R.id.sidrbar);
        dialog = (TextView) view.findViewById(R.id.dialog);
        sideBar.setTextView(dialog);
        //设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    sortListView.getPullRootView().setSelection(position);
                }

            }
        });
        View header = View.inflate(getContext(), R.layout.list_head_view, null);

        sortListView = (PullToZoomListViewEx) view.findViewById(R.id.content_listview);
        sortListView.setHeaderView(header);
        //设置不能放缩
        sortListView.setZoomEnabled(false);
        if (requestCode == 1)
            initListHeaderView();
        sortListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //这里要利用adapter.getItem(position)来获取当前position所对应的对象
                if (selectFragmentCallbackI != null) {
                    if (!sortListView.isHideHeader())//如果没有隐藏头部则position比正常的+1，故判断头部是否隐藏，如果没有隐藏头部则position-1；
                        position -= 1;
                    if (position < 0) {
                        return;
                    }
                    selectFragmentCallbackI.ItemSelectedCallback(((SortModel) adapter.getItem(position)).getName(),
                            ((SortModel) adapter.getItem(position)).getCode());
                }
            }
        });
        mClearEditText = (ClearEditText) view.findViewById(R.id.filter_edit);
        //根据输入框输入值的改变来过滤搜索
        mClearEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString())) {
                    sideBar.setVisibility(View.VISIBLE);
                } else {
                    sideBar.setVisibility(View.GONE);
                }
                //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                filterData(s.toString());
            }
        });

    }

    private void initProgressDilog(){
        //创建ProgressDialog对象
        m_pDialog = new ProgressDialog(getContext(), R.style.CustomDialog);
        // 设置进度条风格，风格为圆形，旋转的
        m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // 设置ProgressDialog 提示信息
        m_pDialog.setMessage("正在获取数据...");
        // 设置ProgressDialog 的进度条是否不明确
        m_pDialog.setIndeterminate(false);
        // 设置ProgressDialog 是否可以按退回按键取消
        m_pDialog.setCancelable(true);
    }

    /**
     * @param
     * @return
     * @Description: 初始化list头部View
     * @createAuthor: XiongChangHui
     * @createDate:2016-6-17 下午1:48:55
     * @lastUpdateAuthor:xiongchanghui
     * @lasetUpdateDate:2016-6-17 下午1:48:55
     */
    private void initListHeaderView() {
        View headerView = sortListView.getHeaderView();
        TextView tv = null;
        for (int i = 0; i < listHeaderViewIdArray.length; i++) {
            tv = (TextView) headerView.findViewById(listHeaderViewIdArray[i]);
            tv.setText(listHeaderViewTextArray[i]);
            tv.setOnClickListener(listHeaderViewListener);
        }
        tv = null;
    }

    /**
     * 用于监听listView头部的事件
     */
    private OnClickListener listHeaderViewListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            for (int i = 0; i < listHeaderViewIdArray.length; i++) {
                if (v.getId() == listHeaderViewIdArray[i]) {
                    if (selectFragmentCallbackI != null) {
                        DBManagerCarList carListDBM = new DBManagerCarList(getActivity());
                        String text = listHeaderViewTextArray[i];
                        selectFragmentCallbackI.ItemSelectedCallback(text,
                                carListDBM.getCarBandId(text));
                        carListDBM.closeDB();
                        carListDBM = null;
                        text = null;
                    }
                    break;
                }
            }
        }
    };

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     *
     * @param filterStr
     */
    private void filterData(String filterStr) {
        List<SortModel> filterDateList = new ArrayList<SortModel>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = sourceDateList;
        } else {
            filterDateList.clear();
            for (SortModel sortModel : sourceDateList) {
                String name = sortModel.getName();
                if (name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())) {
                    filterDateList.add(sortModel);
                }
            }
        }

        // 根据a-z进行排序
        if (filterDateList != null) {
            Collections.sort(filterDateList, pinyinComparator);
        }
        adapter.updateListView(filterDateList);
    }

    /**
     * @param requestCode 请求码 ；fatherCode 根据请求码来使用父类code
     * @return
     * @Description: 根据请求code来初始化数据
     * @createAuthor: XiongChangHui
     * @createDate:2016-6-21 上午10:08:59
     * @lastUpdateAuthor:xiongchanghui
     * @lasetUpdateDate:2016-6-21 上午10:08:59
     */
    private void initData(int requestCode, Integer fatherCode) {
        DBManagerCarList carListDBM = new DBManagerCarList(getActivity());
        switch (requestCode) {
            case 1:
                data = MyCarBrands.getData();
                if (data == null || data.size()<= 0){
                    data = carListDBM.getCarBrandsList(false);
                    sourceDateList = filledBrandsData(data);
                } else {
                    sourceDateList = MyCarBrands.getSourceDateList();
                    sideBarString = MyCarBrands.getSideBarString();
                }
                break;
            case 2:
                ArrayList<CarFirstType> date1 = carListDBM.getCarFristTypesList(fatherCode);
                sourceDateList = filledFristCarTypesData(date1);
                sortListView.setHideHeader(true);
                break;
            case 3:
                ArrayList<CarLastTypes> date2 = carListDBM.getCarLastTypesList(fatherCode);
                sourceDateList = filledLastCarTypesData(date2);
                sortListView.setHideHeader(true);
                break;
            default:
                break;
        }
        carListDBM.closeDB();
    }
    SimpleDateFormat x;
    private  String getTimeString(){
        if (x == null) {
            x = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        }
        Date date = new Date();
        return x.format(date);
    }

    /**
     * 为ListView填充数据
     *
     * @param date
     * @return
     */
    private List<SortModel> filledData(String[] date) {
        List<SortModel> mSortList = new ArrayList<SortModel>();

        for (int i = 0; i < date.length; i++) {
            SortModel sortModel = new SortModel();
            sortModel.setName(date[i]);
            //汉字转换成拼音
            String pinyin = characterParser.getSelling(date[i]);
            String sortString = pinyin.substring(0, 1).toUpperCase();

            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                sortModel.setSortLetters(sortString.toUpperCase());
            } else {
                sortModel.setSortLetters("#");
            }

            mSortList.add(sortModel);
        }
        return mSortList;

    }

    /**
     * 为ListView填充数据
     *
     * @param date
     * @return
     */
    private List<SortModel> filledBrandsData(ArrayList<CarBrands> date) {
        List<SortModel> mSortList = new ArrayList<SortModel>();
        CarBrands carBrand = null;
        sideBarString.clear();
        for (int i = 0, p = date.size(); i < p; i++) {
            SortModel sortModel = new SortModel();
            carBrand = date.get(i);
            sortModel.setName(carBrand.getCarBrandName());
            sortModel.setCode(carBrand.getCarBrandID());
            //汉字转换成拼音
            String pinyin = characterParser.getSelling(carBrand.getCarBrandName());
            String sortString = pinyin.substring(0, 1).toUpperCase();

            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                sortModel.setSortLetters(sortString.toUpperCase());
                sideBarString.add(sortString.toUpperCase());
            } else {
                sortModel.setSortLetters("#");
                sideBarString.add("#");
            }

            mSortList.add(sortModel);
        }
        return mSortList;

    }

    /**
     * 为ListView填充数据
     *
     * @param date
     * @return
     */
    private List<SortModel> filledFristCarTypesData(ArrayList<CarFirstType> date) {
        List<SortModel> mSortList = new ArrayList<SortModel>();
        CarFirstType carType = null;
        sideBarString.clear();
        for (int i = 0, p = date.size(); i < p; i++) {
            SortModel sortModel = new SortModel();
            carType = date.get(i);
            sortModel.setName(carType.getCarFirstTypeName());
            sortModel.setCode(carType.getCarFirstTypeID());
            //汉字转换成拼音
            String pinyin = characterParser.getSelling(carType.getCarFirstTypeName());
            String sortString = pinyin.substring(0, 1).toUpperCase();

            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                sortModel.setSortLetters(sortString.toUpperCase());
                sideBarString.add(sortString.toUpperCase());
            } else {
                sortModel.setSortLetters("#");
                sideBarString.add("#");
            }

            mSortList.add(sortModel);
        }
        return mSortList;

    }

    /**
     * 为ListView填充数据
     *
     * @param date
     * @return
     */
    private List<SortModel> filledLastCarTypesData(ArrayList<CarLastTypes> date) {
        List<SortModel> mSortList = new ArrayList<SortModel>();
        CarLastTypes carType = null;
        sideBarString.clear();
        for (int i = 0, p = date.size(); i < p; i++) {
            SortModel sortModel = new SortModel();
            carType = date.get(i);
            sortModel.setName(carType.getCarLastTypeName());
            sortModel.setCode(carType.getCarLastTypeID());
            //汉字转换成拼音
            String pinyin = characterParser.getSelling(carType.getCarLastTypeName());
            String sortString = pinyin.substring(0, 1).toUpperCase();

            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                sortModel.setSortLetters(sortString.toUpperCase());
                sideBarString.add(sortString.toUpperCase());
            } else {
                sortModel.setSortLetters("#");
                sideBarString.add("#");
            }

            mSortList.add(sortModel);
        }
        return mSortList;

    }

    /**
     * @param
     * @return
     * @Description: 给list设置adapter，用于每次数据修改或者数据修改
     * @createAuthor: XiongChangHui
     * @createDate:2016-6-21 上午10:35:10
     * @lastUpdateAuthor:xiongchanghui
     * @lasetUpdateDate:2016-6-21 上午10:35:10
     */
    private void setAdapter() {
        // 根据a-z进行排序源数据
        if (sourceDateList != null) {
            Collections.sort(sourceDateList, pinyinComparator);
        }
        sideBar.setSideBarString(sideBarString);
        adapter = new SortAdapter(getActivity(), sourceDateList);
        sortListView.setAdapter(adapter);
    }

    /**
     * @param
     * @return
     * @Description: Activity finished前调用回收数据，提高性能
     * @createAuthor: XiongChangHui
     * @createDate:2016-6-21 上午11:06:27
     * @lastUpdateAuthor:xiongchanghui
     * @lasetUpdateDate:2016-6-21 上午11:06:27
     */
    public void clearDate() {
        characterParser = null;
        sourceDateList = null;
        pinyinComparator = null;
        sortListView = null;
        sideBar = null;
        dialog = null;
        adapter = null;
        mClearEditText = null;
        sideBarString = null;
        selectFragmentCallbackI = null;
    }
}
