package com.qihoo.linker.logcollector.upload;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.qihoo.linker.logcollector.capture.LogFileStorage;
import com.qihoo.linker.logcollector.utils.SHA256Tool;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * 
 * @author jiabin
 *
 */
public class UploadLogManager {
	
	private static final String TAG = UploadLogManager.class.getName();
	
	private static UploadLogManager sInstance;
	
	private Context mContext;
	
	private HandlerThread mHandlerThread;
	
    private static volatile MyHandler mHandler;
	
    private volatile Looper mLooper;
    
    private volatile boolean isRunning = false;
    
    private String url;
    
    private HttpParameters params;
	
	private UploadLogManager(Context c){
		mContext = c.getApplicationContext();
		mHandlerThread = new HandlerThread(TAG + ":HandlerThread");
		mHandlerThread.start();
		
		
	}

	public static synchronized UploadLogManager getInstance(Context c){
		if(sInstance == null){
			sInstance = new UploadLogManager(c);
		}
		return sInstance;
	}
	
	public void uploadLogFile(String url , HttpParameters params){
		this.url = url;
		this.params = params;
		
		mLooper = mHandlerThread.getLooper();
		mHandler = new MyHandler(mLooper);
		if(mHandlerThread == null){
			return;
		}
		if(isRunning){
			return;
		}
		mHandler.sendMessage(mHandler.obtainMessage());
		isRunning = true;
	}
	
	private final class MyHandler extends Handler{

		public MyHandler(Looper looper) {
			super(looper);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void handleMessage(Message msg) {
			File traces = new File("/data/anr/traces.txt");
			if(traces.exists()){
				String msgSum = Arrays.toString(SHA256Tool.getFileSHA256(traces));
				SharedPreferences sharedPreferences = mContext.getSharedPreferences("user", Context.MODE_PRIVATE);
				String oldSum = sharedPreferences.getString("oldTraceSum","");
				if(!msgSum.equals("null") && msgSum.equals(oldSum)){
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString("oldTraceSum", msgSum);
					editor.apply();
					try {
						HttpManager.uploadFile(url, params, traces);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					Log.i("upload","traces is sample");
				}
			} else {
				Log.i("upload","traces == null");
			}

			File logFile = LogFileStorage.getInstance(mContext).getUploadLogFile();
			if(logFile == null){
				isRunning = false;
				Log.i("upload","logFile == null");
				return;
			}
			try {
				String result = HttpManager.uploadFile(url, params, logFile);
				if(result != null){
					LogFileStorage.getInstance(mContext).deleteUploadLogFile();
				}
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally{
				isRunning = false;
			}
		}
		
	}
	
}
