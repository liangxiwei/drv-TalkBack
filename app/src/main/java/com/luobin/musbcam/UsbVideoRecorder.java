package com.luobin.musbcam;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.luobin.dvr.DvrService;

import java.io.File;

import static com.autonavi.ae.search.log.GLog.filename;


public class UsbVideoRecorder {
    private final static String TAG = "UsbVideoRecorder";
    private native boolean nativeStart(String dev,int width, int height, String filename);
    private native boolean nativeStop();
    private native boolean nativeSwitchNewFile(String filename);
    private boolean lockNow = false;
    private String lastName = "";
    private String nowName = "";
    public boolean start(String dev,int width, int height, String filename) {
        Log.d(TAG, "start dev ="+dev+",filename="+filename);
        lastName = nowName;
        nowName = filename;
        checkRenameFile();
        if(nativeStart(dev,width, height, filename)) {
            return true;
        } else {
            Log.e(TAG, "nativeStart fail");
            return false;
        }
    }

    public void stop() {
        Log.d(TAG,  "stop");
        nativeStop();
    }

    public void switchNewFile(String filename) {
        Log.d(TAG, "switchNewFile filename="+filename);
        lastName = nowName;
        nowName = filename;
        checkRenameFile();
        nativeSwitchNewFile(filename);
    }
    public void setLock(boolean isLock) {
        Log.d(TAG, "setLock ="+isLock);
        lockNow = isLock;
    }
    public void checkRenameFile(){
        Log.d(TAG, "lockNow ="+lockNow);
        if(lockNow) {
            lockNow = false;
            File file = new File(lastName);
            if (file.exists()) {
                boolean isSuccess = file.renameTo(new File(lastName.replace(".mp4", DvrService.LOCK + ".mp4")));
                Log.d(TAG, "==== rename ==== " + isSuccess);
            }
        }
    }


}
