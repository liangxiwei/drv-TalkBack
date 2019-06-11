package com.luobin.utils;

import android.os.Environment;

public class PathUtlis {

    /**
     * 检查SD卡是否被挂载
     */
    private static boolean existSDcard() {
        // 获取SD卡的状态
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取本地根目录，优先sd卡
     * @return
     */
    public static String getRootDirectory(){
        if (existSDcard()){
            return  Environment.getExternalStorageDirectory().getAbsolutePath();
        }else{
            return Environment.getDataDirectory().getPath();
        }

    }






}
