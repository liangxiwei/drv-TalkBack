package com.luobin.search.friends.city.picverview;

import android.view.View;


import com.luobin.dvr.R;

import java.util.ArrayList;

/**
 * @version 0.1 king 2015-11
 */
final class MWheelOptions {
    private CharacterPickerView view;
    private LoopView wv_option1;
    private LoopView wv_option2;
    private LoopView wv_option3;

    private ArrayList<String> mOptions1Items;
    private ArrayList<ArrayList<String>> mOptions2Items;
    private ArrayList<ArrayList<ArrayList<String>>> mOptions3Items;
    private CharacterPickerView.OnOptionChangedListener mOnOptionChangedListener;

    public View getView() {
        return view;
    }

    public MWheelOptions(CharacterPickerView view) {
        super();
        this.view = view;
    }

    public void setOnOptionChangedListener(
            CharacterPickerView.OnOptionChangedListener listener) {
        this.mOnOptionChangedListener = listener;
    }

    public void setPicker(ArrayList<String> optionsItems) {
        setPicker(optionsItems, null, null);
    }

    public void setPicker(ArrayList<String> options1Items,
                          ArrayList<ArrayList<String>> options2Items) {
        setPicker(options1Items, options2Items, null);
    }

    public void setPicker(ArrayList<String> options1Items,
                          ArrayList<ArrayList<String>> options2Items,
                          ArrayList<ArrayList<ArrayList<String>>> options3Items) {
        this.mOptions1Items = (options1Items == null ? new ArrayList<String>() : options1Items);
        this.mOptions2Items = (options2Items == null ? new ArrayList<ArrayList<String>>() : options2Items);
        this.mOptions3Items = (options3Items == null ? new ArrayList<ArrayList<ArrayList<String>>>() : options3Items);
        // 选项1
        wv_option1 = (LoopView) view.findViewById(R.id.j_options1);
        wv_option1.setArrayList(mOptions1Items);// 设置显示数据
        //设置初始位置
        wv_option1.setPosition(0);// 初始化时显示的数据
        //设置是否循环播放
        wv_option1.setNotLoop();

        //滚动监听
        wv_option1.setListener(new LoopListener() {
            @Override
            public void onItemSelect(int item) {
                if (!mOptions2Items.isEmpty()) {
                    wv_option2.setArrayList(mOptions2Items.get(item));
                    wv_option2.setPosition(0);
                }
                if (!mOptions3Items.isEmpty()) {
                    wv_option3.setArrayList(mOptions3Items.get(item).get(0));
                    wv_option3.setPosition(0);
                } else {
                    doItemChange();
                }
            }
        });

        // 选项2
        wv_option2 = (LoopView) view.findViewById(R.id.j_options2);
        if (!mOptions2Items.isEmpty()) {
            wv_option2.setArrayList(mOptions2Items.get(0));// 设置显示数据
            wv_option2.setPosition(0);// 初始化时显示的数据
            //设置是否循环播放
            wv_option2.setNotLoop();
            //滚动监听
            wv_option2.setListener(new LoopListener() {
                @Override
                public void onItemSelect(int item) {
                    if (!mOptions3Items.isEmpty()) {
                        wv_option3.setArrayList(mOptions3Items.get(wv_option1.getCurrentItem()).get(item));
                        wv_option3.setPosition(0);
                    } else {
                        doItemChange();
                    }
                }
            });
        }

        // 选项3
        wv_option3 = (LoopView) view.findViewById(R.id.j_options3);
        if (!mOptions3Items.isEmpty()) {
            wv_option3.setArrayList(mOptions3Items.get(0).get(0));// 设置显示数据
            wv_option3.setPosition(0);// 初始化时显示的数据
            //设置是否循环播放
            wv_option3.setNotLoop();
            //滚动监听
            wv_option3.setListener(new LoopListener() {
                @Override
                public void onItemSelect(int item) {
                    doItemChange();
                }
            });
        }

        if (mOptions2Items.isEmpty())
            view.findViewById(R.id.j_layout2).setVisibility(View.GONE);
        if (mOptions3Items.isEmpty())
            view.findViewById(R.id.j_layout3).setVisibility(View.GONE);

        //   setCurrentItems(0, 0, 0);
    }

    /**
     * 选中项改变
     */
    private void doItemChange() {
        if (mOnOptionChangedListener != null) {
            int option1 = wv_option1.getCurrentItem();
            int option2 = wv_option2.getCurrentItem();
            int option3 = wv_option3.getCurrentItem();
            mOnOptionChangedListener.onOptionChanged(view, option1, option2, option3);
        }
    }

    /**
     * 设置是否循环滚动
     *
     * @param cyclic
     */
    public void setCyclic(boolean cyclic) {
        wv_option1.setNotLoop();
        wv_option2.setNotLoop();
        wv_option3.setNotLoop();
    }

    /**
     * 返回当前选中的结果对应的位置数组 因为支持三级联动效果，分三个级别索引，0，1，2
     *
     * @return
     */
    public int[] getCurrentItems() {
        int[] currentItems = new int[3];
        currentItems[0] = wv_option1.getCurrentItem();
        currentItems[1] = wv_option2.getCurrentItem();
        currentItems[2] = wv_option3.getCurrentItem();
        return currentItems;
    }

    public void setCurrentItems(int option1, int option2, int option3) {
        wv_option1.setPosition(option1);
        wv_option2.setPosition(option2);
        wv_option3.setPosition(option3);
    }

    public void setDefineCurrentItems(int option1, int option2, int option3) {
        wv_option1.setPosition(option1);
        if (!mOptions2Items.isEmpty()) {
//            boolean isenpt = mOptions2Items.get(option1).equals("");
//            if (isenpt){
//                Log.i("jim"," isenpt:"+isenpt);
//                wv_option2.setVisibility(View.GONE);
//            }else {
            wv_option2.setArrayList(mOptions2Items.get(option1));
            wv_option2.setPosition(option2);
//            }
        }
        if (!mOptions3Items.isEmpty()) {
//            boolean isenpt = mOptions3Items.get(option1).get(0).equals("");
//            if (isenpt){
//                Log.i("jim"," isenpt:"+isenpt);
//                wv_option2.setVisibility(View.GONE);
//            }else {
            wv_option3.setArrayList(mOptions3Items.get(option1).get(option2));
            wv_option3.setPosition(option3);
//            }

        } else {
            doItemChange();
        }
    }
}
