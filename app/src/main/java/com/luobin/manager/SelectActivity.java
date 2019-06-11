package com.luobin.manager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;

import com.example.jrd48.chat.BaseActivity;

public abstract class SelectActivity extends BaseActivity {

    SelectViewManager selectViewManager;
   public Context context;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
    }

    protected  void setSelectViewManager( SelectViewManager selectViewManager){
       this.selectViewManager = selectViewManager;
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                   if (selectViewManager != null){
                       selectViewManager.moveLeft();
                   }
                    return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (selectViewManager != null){
                        selectViewManager.moveRight();
                    }
                    return true;
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (selectViewManager != null){
                        selectViewManager.moveUp();
                    }
                    return true;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (selectViewManager != null){
                        selectViewManager.moveDown();
                    }
                    return true;
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    if (selectViewManager != null){
                        selectViewManager.center();
                    }

                    return true;
                case KeyEvent.KEYCODE_BACK:
                    finish();
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }


}
