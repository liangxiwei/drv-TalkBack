package com.luobin.dvr;

////////////////////////////////////////////////////////
/*
 * DVR Service Interface
 * Call me as below code:
 * Intent i = new Intent("com.luobin.dvr.DvrService");
 * i.setComponent(new ComponentName("com.luobin.dvr", "com.luobin.dvr.DvrService"));
 * bindService(i,mServiceConn, BIND_AUTO_CREATE);
 */
////////////////////////////////////////////////////////

interface IDvrService {
	/*
	 * start camera preview
	 */
	boolean startPreview();
	
	boolean isPreviewing(); 
	/* 
	 * show camera preview view
	 * param :x,y, width, height
	 */
	boolean show(int x, int y, int w, int h);
	
	/*
	 * hide camera preview view
	 */
	boolean hide();
	/*
	 * start one video recording
	 * if there is already one recording, it will be stoped.
	 * this func can be used to switch recording file without frame dropping.
	 */
	boolean startRecord(String file);
	
	/*
	 * start one video recording with pre n seconds video.
	 * if there is already one recording, it will be stoped.
	 * this func can be used to switch recording file without frame dropping.
	 */
	boolean startRecordWithPreVideo(String file);
	
	boolean isRecording();
	
	/*
	 * stop recording, DVR service should be recording state
	 */
	boolean stopRecord();
	
	/*
	 * stop preview, DVR service should be previewing or recording
	 */
	boolean stopPreview();
	
	/*
	 * set water mark image, it will be recorded in video
	 * param gravity could be  Gravity.RIGHT/LEFT/TOP/BOTTOM/CENTER_VERTICAL/CENTER_HORIZONTAL
	 */
	boolean setWaterMark(in byte[] png, int gravity);

	/*
	 * take one photo, DVR service should be previewing or recording
	 */
	boolean takePhoto(String file);
	
	void getVideoSize(out int[] size);
	void setVideoSize(in int[] size);
	boolean getAudioEnabled();
	void setAudioEnabled(boolean enabled);
	/*
	* seconds of video we want to have in our buffer at any time.
	*/
	int getPreVideoTime();
	void setPreVideoTime(int seconds);
    /*
	* set video bitrate 
	*/
	int getVideoBitrate();
	void setVideoBitrate(int bitrate);
    



	//own //////////////////////////////////////////////////////////////////////////////
	
	/*
	 * start circle record.
	 */
	boolean startCircleRecord();
    /*
	 * auto generate time stamp water mark
	 */
	boolean setTimeStampWaterMark(boolean enabled);
	
	//////////////////////////////////////////////////////////////////////////////
	/*
	 * config interface
	 */
	 
	void setAutoRunWhenBoot(boolean enabled);
	 
	boolean getAutoRunWhenBoot();
	 
	int getVideoDuration();
	
	void setVideoDuration(int duration_ms);
	
	String getStoragePath();
	
	void setStoragePath(String path);

	int getCollisionSensitivity();
	
	void setCollisionSensitivity(int val);
	
	/*
	 * thumbnail preview rect, used to show camera preview when no client
	 * connected to dvrservice
	 * size should be 4 elements array.
	 */
	void getThumbnailViewRect(out int[] size);
	
	void setThumbnailViewRect(in int[] size);

	void startRtmp();

	void stopRtmp();

	void switchCamera();

	int rtmpStatus();
}