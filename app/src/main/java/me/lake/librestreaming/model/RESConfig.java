package me.lake.librestreaming.model;


import android.hardware.Camera;

/**
 * Created by lake on 16-3-16.
 */
public class RESConfig {
    public static final int VIDEO_WIDTH = 640;
    public static final int VIDEO_HEIGHT = 360;
    public static final int VIDEO_HEIGHT2 = 480;
    public static final int VIDEO_WIDTH_BIG = 1280;
    public static final int VIDEO_HEIGHT_BIG = 720;
    public static final int BITRATE = 1024*1024;
    public static final int FPS = 30;
    public static class FilterMode {
        public static final int HARD = RESCoreParameters.FILTER_MODE_HARD;
        public static final int SOFT = RESCoreParameters.FILTER_MODE_SOFT;
    }

    public static class RenderingMode {
        public static final int NativeWindow = RESCoreParameters.RENDERING_MODE_NATIVE_WINDOW;
        public static final int OpenGLES = RESCoreParameters.RENDERING_MODE_OPENGLES;
    }

    public static class DirectionMode {
        public static final int FLAG_DIRECTION_FLIP_HORIZONTAL = RESCoreParameters.FLAG_DIRECTION_FLIP_HORIZONTAL;
        public static final int FLAG_DIRECTION_FLIP_VERTICAL = RESCoreParameters.FLAG_DIRECTION_FLIP_VERTICAL;
        public static final int FLAG_DIRECTION_ROATATION_0 = RESCoreParameters.FLAG_DIRECTION_ROATATION_0;
        public static final int FLAG_DIRECTION_ROATATION_90 = RESCoreParameters.FLAG_DIRECTION_ROATATION_90;
        public static final int FLAG_DIRECTION_ROATATION_180 = RESCoreParameters.FLAG_DIRECTION_ROATATION_180;
        public static final int FLAG_DIRECTION_ROATATION_270 = RESCoreParameters.FLAG_DIRECTION_ROATATION_270;
    }

    private int filterMode;
    private Size targetVideoSize;
    private int videoBufferQueueNum;
    private int bitRate;
    private String rtmpAddr;
    private int renderingMode;
    private int defaultCamera;
    private int frontCameraDirectionMode;
    private int backCameraDirectionMode;
    private int videoFPS;
    private boolean printDetailMsg;

    private RESConfig() {
    }

    public static RESConfig obtain() {
        RESConfig res = new RESConfig();
        res.setFilterMode(FilterMode.HARD);
        res.setRenderingMode(RenderingMode.OpenGLES);
        res.setTargetVideoSize(new Size(VIDEO_WIDTH, VIDEO_HEIGHT));
        res.setVideoFPS(FPS);
        res.setVideoBufferQueueNum(5);
        res.setBitRate(BITRATE);
        res.setPrintDetailMsg(false);
        res.setDefaultCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        res.setBackCameraDirectionMode(DirectionMode.FLAG_DIRECTION_ROATATION_0);
        res.setFrontCameraDirectionMode(DirectionMode.FLAG_DIRECTION_ROATATION_0);
        return res;
    }

    /**
     * set the filter mode.
     *
     * @param filterMode {@link FilterMode}
     */
    public void setFilterMode(int filterMode) {
        this.filterMode = filterMode;
    }

    /**
     * set the default camera to start stream
     */
    public void setDefaultCamera(int defaultCamera) {
        this.defaultCamera = defaultCamera;
    }

    /**
     * set front camera rotation & flip
     * @param frontCameraDirectionMode {@link DirectionMode}
     */
    public void setFrontCameraDirectionMode(int frontCameraDirectionMode) {
        this.frontCameraDirectionMode = frontCameraDirectionMode;
    }
    /**
     * set front camera rotation & flip
     * @param backCameraDirectionMode {@link DirectionMode}
     */
    public void setBackCameraDirectionMode(int backCameraDirectionMode) {
        this.backCameraDirectionMode = backCameraDirectionMode;
    }

    /**
     * set  renderingMode when using soft mode<br/>
     * no use for hard mode
     * @param renderingMode {@link RenderingMode}
     */
    public void setRenderingMode(int renderingMode) {
        this.renderingMode = renderingMode;
    }

    /**
     * no use for now
     * @param printDetailMsg
     */
    public void setPrintDetailMsg(boolean printDetailMsg) {
        this.printDetailMsg = printDetailMsg;
    }

    /**
     * set the target video size.<br/>
     * real video size may different from it.Depend on device.
     * @param videoSize
     */
    public void setTargetVideoSize(Size videoSize) {
        targetVideoSize = videoSize;
    }

    /**
     * set video buffer number for soft mode.<br/>
     * num larger:video Smoother,more memory.
     * @param num
     */
    public void setVideoBufferQueueNum(int num) {
        videoBufferQueueNum = num;
    }

    /**
     * set video bitrate
     * @param bitRate
     */
    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    public int getVideoFPS() {
        return videoFPS;
    }

    public void setVideoFPS(int videoFPS) {
        this.videoFPS = videoFPS;
    }

    public int getVideoBufferQueueNum() {
        return videoBufferQueueNum;
    }

    public int getBitRate() {
        return bitRate;
    }

    public Size getTargetVideoSize() {
        return targetVideoSize;
    }

    public int getFilterMode() {
        return filterMode;
    }

    public int getDefaultCamera() {
        return defaultCamera;
    }

    public int getBackCameraDirectionMode() {
        return backCameraDirectionMode;
    }

    public int getFrontCameraDirectionMode() {
        return frontCameraDirectionMode;
    }

    public int getRenderingMode() {
        return renderingMode;
    }

    public String getRtmpAddr() {
        return rtmpAddr;
    }

    public void setRtmpAddr(String rtmpAddr) {
        this.rtmpAddr = rtmpAddr;
    }

    public boolean isPrintDetailMsg() {
        return printDetailMsg;
    }
}
