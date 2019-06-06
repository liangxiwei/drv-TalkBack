package com.luobin.search.friends.city;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.luobin.model.City;
import com.luobin.model.County;
import com.luobin.model.Province;
import com.luobin.search.friends.city.picverview.CharacterPickerView;
import com.luobin.search.friends.city.picverview.CharacterPickerWindow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * 地址选择器
 *
 * @version 0.1 king 2015-10
 */
public class OptionsWindowHelper {

    public static final String NO_SET = "不限";
    public static final String NOT_SET = "未设置";
    public static final String OTHER = "其他";
    public static final String SET_CITY = "set_city";

    private static ArrayList<String> options1Items = null;
    private static ArrayList<ArrayList<String>> options2Items = null;
    protected static ArrayList<ArrayList<ArrayList<String>>> options3Items = null;
    private static Context context;
    private static CharacterPickerWindow mOptions;
    private static String mType;

    public static interface OnOptionsSelectListener {
        public void onOptionsSelect(String province, String city, String area);
    }

    private OptionsWindowHelper() {
    }

    public static CharacterPickerWindow builder(String str, String str1, String str2, String type, Activity activity, final OnOptionsSelectListener listener) {
        context = activity;
        //选项选择器
        mOptions = new CharacterPickerWindow(activity);
        //初始化选项数据
        mType = type;
        setPickerData(mOptions.getPickerView());
        //设置默认选中的三级项目
        /**
         *第一个参数：表示哪个城市
         * 第二个参数：表示该城市的哪个区
         * 第三个参数：表示该区的那个县
         */

        if (mType.equals(SET_CITY)) {
            if (!str.equals(NO_SET)) {
                setDefualt(str, str1, str2);
            } else {
                mOptions.setDefineSelectOptions(0, 0, 0);
            }
        } else {
            if (str.equals("不限")){
                str = "北京市";
            }
            setDefualt(str, str1, str2);
        }

        //监听确定选择按钮
        mOptions.setOnoptionsSelectListener(new CharacterPickerWindow.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                if (listener != null) {
                    String province = options1Items.get(options1);
                    String city = options2Items.get(options1).get(option2);
                    String area = options3Items.get(options1).get(option2).get(options3);
                    listener.onOptionsSelect(province, city, area);
                }
            }
        });
        return mOptions;
    }

    private static void setDefualt(String str, String str1, String str2) {
        int index = 0;
        int index1 = 0;
        int index2 = 0;
        index = options1Items.indexOf(str);
        for (int i = 0; i < options2Items.get(index).size(); i++) {
            if (options2Items.get(index).get(i).equals(str1)) {
                index1 = i;
                break;
            }
        }
        for (int i = 0; i < options3Items.get(index).get(index1).size(); i++) {
            if (options3Items.get(index).get(index1).get(i).equals(str2)) {
                index2 = i;
                break;
            }
        }

        mOptions.setDefineSelectOptions(index, index1, index2);
    }

    /**
     * 初始化选项数据
     */
    public static void setPickerData(CharacterPickerView view) {
//        if (options1Items == null) {
            options1Items = new ArrayList();
            options2Items = new ArrayList<>();
            options3Items = new ArrayList();

            ArrayList<Province> data = new ArrayList<Province>();
            try {
                String json;
                if (mType.equals(SET_CITY)) {
                    json = convertString(context.getAssets().open("cityand.json"), "utf-8");
                } else {
                    json = convertString(context.getAssets().open("city.json"), "utf-8");
                }

                data.addAll(JSON.parseArray(json, Province.class));
            } catch (Exception e) {
                Log.e("helper"," get data error:"+e.getMessage());
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
                        if (mType.equals(SET_CITY)) {
                            yCounties.add(cit.getAreaName());
                        } else {
                            yCounties.add("");
                        }

                    } else {
                        for (int z = 0; z < countySize; z++) {
                            yCounties.add(counties.get(z).getAreaName());
                        }
                    }
                    xCounties.add(yCounties);
                }
                if (mType.equals(SET_CITY)) {
                } else {
                    if (xCities.size() <= 0) {
                        xCities.add("");
                        xCounties.add(xCities);
                    }
                }

                options2Items.add(xCities);
                options3Items.add(xCounties);
            }
            //   Collections.sort(options1Items);

//        }

        //三级联动效果
        view.setPicker(options1Items, options2Items, options3Items);

    }


    public static String convertString(InputStream is, String charset) {
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
