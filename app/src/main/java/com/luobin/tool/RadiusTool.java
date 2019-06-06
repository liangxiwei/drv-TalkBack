package com.luobin.tool;

import android.util.Log;

/**
 * Created by jrd48 on 2017/3/3.
 */

public class RadiusTool {

    public static synchronized String getRadius(int radiu){
        String radius = "";
        if (radiu > 1000 ){
            radius = "(" +(radiu/1000+1)+"公里以内)";
        }else {
            radius = "(约" + radiu + "米" + ")";
        }
        Log.i("jim"," 获取到的半径："+radiu + " 显示数据："+radius);
        return radius;
    }

}
