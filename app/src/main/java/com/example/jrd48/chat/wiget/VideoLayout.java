package com.example.jrd48.chat.wiget;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.FirstActivity;
import com.example.jrd48.chat.GlobalImg;
import com.luobin.dvr.DvrService;
import com.luobin.dvr.R;
import com.example.jrd48.chat.SettingRW;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.permission.PermissionUtil;
import com.example.jrd48.service.ConnUtil;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.widget.PLVideoView;
import com.video.AspectTextureView;
import com.video.GlobalVideo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.lake.librestreaming.client.RESClient;
import me.lake.librestreaming.core.listener.RESConnectionListener;
import me.lake.librestreaming.model.RESConfig;
import me.lake.librestreaming.model.Size;

/**
 * Created by Administrator on 2017/7/27.
 */

public class VideoLayout extends RelativeLayout {
    private static final String TAG = "VideoLayout";
    public static final int MY_PERMISSIONS_REQUEST_VIDEO_LAYOUT = 10055;
    private Activity activity;
    public VideoLayout(Context context) {
        super(context);
    }

    public VideoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public void showVideo(Activity activity){
        this.activity = activity;
        if(GlobalStatus.isVideo()) {
            Rect location = new Rect();
            getLocalVisibleRect(location);
            Log.i(TAG, "showVideo");

            int x = location.left;
            int y = location.top;
            //full screen
            DisplayMetrics dm = new DisplayMetrics();
            WindowManager windowMgr = (WindowManager)activity.getSystemService(Context.WINDOW_SERVICE);
            windowMgr.getDefaultDisplay().getRealMetrics(dm);
            int ScreenH = dm.heightPixels;
            int ScreenW = dm.widthPixels;
            int width = ScreenW;//getWidth();
            int height = ScreenH;//getHeight();
            Log.i(TAG, "showVideo x="+x+",y="+y+",w="+width+",h="+height);

            float newScale = (float) RESConfig.VIDEO_WIDTH/RESConfig.VIDEO_HEIGHT;
            Log.i(TAG, "showVideo h*="+(height * newScale));
            if(height * newScale < width){
                width = (int) (height * newScale);
                x = x + (/*getWidth()*/ScreenW - width)/2;
            } else if(height * newScale > width){
                height = (int) (width/newScale);
                y = y + (/*getHeight()*/ScreenH - height)/2;
            }
            Log.i(TAG, "showVideo x="+x+",y="+y+",width="+width+",height="+height);
            DvrService.updateRtmpView(activity, x,
                    y,
                    width,
                    height);
            DvrService.updatePlayView(activity, x,
                    y,
                    width,
                    height);
        }
    }

    public void hideVideo(){
        Log.i(TAG,"hideVideo");
    }

    @Override
    public void removeAllViews() {
        synchronized (VideoLayout.class) {
            Log.e(TAG, "removeAllViews");
            super.removeAllViews();
            if(activity != null) {
                ((FirstActivity) activity).showSpeakMan();
            }
        }
    }

    public void checkVideoPermission() {
        if(activity == null){
            return;
        }
        if (Build.VERSION.SDK_INT >= 19) {
            List<String> mList = new ArrayList<String>();
            mList.add(PermissionUtil.PERMISSIONS_STORAGE);
            mList.add(PermissionUtil.PERMISSIONS_CAMERA);
            List<String> mDeniedPermissionList = PermissionUtil.getInstance().findDeniedPermissions(activity, mList);
            if (mDeniedPermissionList != null && mDeniedPermissionList.size() > 0) {
                PermissionUtil.getInstance().requestPermission(activity, MY_PERMISSIONS_REQUEST_VIDEO_LAYOUT, (FirstActivity)activity, mDeniedPermissionList.get(0), mDeniedPermissionList.get(0));
            } else {
//                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                params.addRule(RelativeLayout.CENTER_VERTICAL);//水平居中
//                contentView.setLayoutParams(params);
//                addView(contentView);
            }
        }
    }
}
