package com.luobin.dvr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.example.jrd48.chat.SharedPreferencesUtils;

import java.io.File;

public class DvrReceiver extends BroadcastReceiver {
    private static final String TAG = "DvrReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
////            if(GlobalStatus.getUsbVideo1() != null){
////                GlobalStatus.getUsbVideo1().close();
////                GlobalStatus.setUsbVideo1(null);
////            }
//
//            if(GlobalStatus.getUsbVideo2() != null){
//                GlobalStatus.getUsbVideo2().close();
//                GlobalStatus.setUsbVideo2(null);
//            }
            Log.d(TAG, "DvrReceiver:boot completed");
            SharedPreferencesUtils.put(context, "group_booting", true);
            SharedPreferencesUtils.put(context, "member_booting", true);
            context.startService(new Intent(context, DvrService.class));
            clearAudioRecord();//avoid too many acc files
        }

    }

    private void clearAudioRecord() {
        String audioRecordFilesPath = Environment.getExternalStorageDirectory().getPath() + "/AudioRecord/";
        File audioRecordFiles = new File(audioRecordFilesPath);
        if (audioRecordFiles.exists()) {
            for (File file : audioRecordFiles.listFiles()) {
                if (file.exists())
                    file.delete();
            }
        }
    }

}
