package com.luobin.tool;

import android.content.Context;


import com.luobin.model.CarBrands;
import com.luobin.search.friends.car.DBManagerCarList;
import com.luobin.search.friends.car.listForSelectUsed.CharacterParser;
import com.luobin.search.friends.car.listForSelectUsed.SortModel;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by Administrator on 2017/9/16.
 */

public class MyCarBrands {

    private static volatile MyCarBrands instance = null;
    private static Context mContext;
    private static volatile List<SortModel> sourceDateList;
    private volatile CharacterParser characterParser;
    private static volatile TreeSet<String> sideBarString = new TreeSet<String>();
    private static volatile ArrayList<CarBrands> data;

    public static ArrayList<CarBrands> getData() {
        return data;
    }

    public static List<SortModel> getSourceDateList() {
        return sourceDateList;
    }

    public CharacterParser getCharacterParser() {
        return characterParser;
    }

    public static TreeSet<String> getSideBarString() {
        return sideBarString;
    }

    private MyCarBrands() {

        DBManagerCarList carListDBM = null;
        try {
            carListDBM = new DBManagerCarList(mContext);
            data = carListDBM.getCarBrandsList(false);
        }finally {
            if (carListDBM != null) {
                carListDBM.closeDB();
            }
        }

        characterParser = CharacterParser.getInstance();
        sourceDateList = filledBrandsData(data);
    }

    public static MyCarBrands getInstance(Context context) {
        // if already inited, no need to get lock everytime
        mContext = context;
        if (instance == null) {
            synchronized (MyCarBrands.class) {
                if (instance == null) {
                    instance = new MyCarBrands();
                }
            }
        }

        return instance;
    }

    private List<SortModel> filledBrandsData(ArrayList<CarBrands> data) {
        List<SortModel> mSortList = new ArrayList<SortModel>();
        CarBrands carBrand = null;
        sideBarString.clear();
        for (int i = 0, p = data.size(); i < p; i++) {
            SortModel sortModel = new SortModel();
            carBrand = data.get(i);
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
}
