package com.example.jrd48.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.luobin.dvr.R;
import com.luobin.ui.DvrMainActivity;
import com.luobin.ui.LoginActivity;

/**
 * Created by jrd48 on 2016/11/18.
 */

public class WelcomeActivity extends BaseActivity implements Animation.AnimationListener {
    private ImageView imageView = null;
    private Animation alphaAnimation = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        SharedPreferences preferences=getSharedPreferences("token", Context.MODE_PRIVATE);
        String token=preferences.getString("token","");
        if(token.equals("")){
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
//            overridePendingTransition(R.anim.fade, R.anim.hold);
            WelcomeActivity.this.finish();
        }else {
            Intent intent = new Intent(WelcomeActivity.this, DvrMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
//            overridePendingTransition(R.anim.fade, R.anim.hold);
            WelcomeActivity.this.finish();
        }
//        imageView = (ImageView)findViewById(R.id.imageView_welcome);
//        alphaAnimation = AnimationUtils.loadAnimation(this, R.anim.welcome);
//        alphaAnimation.setFillEnabled(true); //启动Fill保持
//        alphaAnimation.setFillAfter(true);  //设置动画的最后一帧是保持在View上面
//        imageView.setAnimation(alphaAnimation);
//        alphaAnimation.setAnimationListener(this);  //为动画设置监听
    }

    @Override
    public void onAnimationStart(Animation animation) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        //动画结束时结束欢迎界面并转到软件的主界面

        SharedPreferences preferences=getSharedPreferences("token", Context.MODE_PRIVATE);
        String token=preferences.getString("token","");
        if(token.equals("")){
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.fade, R.anim.hold);
            WelcomeActivity.this.finish();
        }else {
            Intent intent = new Intent(WelcomeActivity.this, DvrMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.fade, R.anim.hold);
            WelcomeActivity.this.finish();
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //在欢迎界面屏蔽BACK键,在退出的时候就不会返回这个欢迎页
        if(keyCode==KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return false;
    }
}
