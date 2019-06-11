package me.lake.librestreaming.client;

import android.content.Context;
import android.os.Build;

/**
 * Created by zhouyuhuan on 2018/4/2.
 */

public class VideoFactory {

    public VideoBase createVideo(Context context) {
        String product = Build.PRODUCT;
        if (product != null && (product.equals("LB1728V4") ||product.equals("LB1822") )) {
            USBVideo usb = new USBVideo(context);
            return usb;
        }else/* if (product.equals("DVR_CAM")) */{
            CameraVideo cam = new CameraVideo(context);
            return cam;
        }
    }


}
