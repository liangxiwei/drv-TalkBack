package com.example.jrd48.chat;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.chat.wiget.AppToast;

public class ToastR {
    public static int x = 0, y = 12;
    public static AppToast TOAST = new AppToast(MyApplication.getContext());

    //Toast屏幕宽度的上中下位置47，15，79；文本宽度上中下位置48(Gravity.TOP)，17(Gravity.CENTER)，80(Gravity.BOTTOM)
    public static void setToast(Context context, String str, int gravity) {
//        if (TOAST != null)
//            TOAST.cancel();
//        TOAST = Toast.makeText(context, str, Toast.LENGTH_SHORT);
        TOAST.setGravity(gravity, x, y);//(坐标原点，横坐标，纵坐标)
        TOAST.show(str);
    }

    public static void setToast(Context context, String str) {
//        if (TOAST != null)
//            TOAST.cancel();
//        TOAST = Toast.makeText(context, str, Toast.LENGTH_SHORT);
        TOAST.show(str);
    }

    public static void setToastLong(Context context, String str) {
//        if (TOAST != null)
//            TOAST.cancel();
//        TOAST = Toast.makeText(context, str, Toast.LENGTH_LONG);
        TOAST.show(str,4000);
    }
}
