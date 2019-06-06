//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pili.pldroid.player;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.pili.pldroid.player.qos.Qos2;
import com.pili.pldroid.player.qos.Qos3;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkLibLoader;
import tv.danmaku.ijk.media.player.IjkMediaMeta;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.MediaInfo;

public class PLMediaPlayer {
    private Context a;
    private IjkMediaPlayer b;
    private long c;
    private static volatile boolean d = false;
    private com.pili.pldroid.player.qos.Qos1 e;
    private PLMediaPlayer.a f;
    private long g;
    private long h;
    private long i;
    private int j;
    private boolean k;
    private boolean l;
    private boolean m;
    private boolean n;
    private PlayerState o;
    private String p;
    private Map<String, String> q;
    private SurfaceHolder r;
    private Surface s;
    private AVOptions t;
    private boolean u;
    private tv.danmaku.ijk.media.player.IMediaPlayer.OnVideoSizeChangedListener v;
    private tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener w;
    private tv.danmaku.ijk.media.player.IMediaPlayer.OnSeekCompleteListener x;
    private tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener y;
    private tv.danmaku.ijk.media.player.IMediaPlayer.OnBufferingUpdateListener z;
    private tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener A;
    private tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener B;
    public static final int MEDIA_INFO_UNKNOWN = 1;
    public static final int MEDIA_INFO_VIDEO_RENDERING_START = 3;
    public static final int MEDIA_INFO_BUFFERING_BYTES_UPDATE = 503;
    public static final int MEDIA_INFO_BUFFERING_START = 701;
    public static final int MEDIA_INFO_BUFFERING_END = 702;
    public static final int MEDIA_INFO_NOT_SEEKABLE = 801;
    public static final int MEDIA_INFO_SWITCHING_SW_DECODE = 802;
    public static final int MEDIA_INFO_VIDEO_ROTATION_CHANGED = 10001;
    public static final int MEDIA_INFO_AUDIO_RENDERING_START = 10002;
    public static final int MEDIA_INFO_VIDEO_GOP_TIME = 10003;
    private PLMediaPlayer.OnInfoListener C;
    private PLMediaPlayer.OnPreparedListener D;
    private PLMediaPlayer.OnCompletionListener E;
    private PLMediaPlayer.OnBufferingUpdateListener F;
    private PLMediaPlayer.OnSeekCompleteListener G;
    private PLMediaPlayer.OnVideoSizeChangedListener H;
    public static final int MEDIA_ERROR_UNKNOWN = -1;
    public static final int ERROR_CODE_INVALID_URI = -2;
    public static final int ERROR_CODE_IO_ERROR = -5;
    public static final int ERROR_CODE_STREAM_DISCONNECTED = -11;
    public static final int ERROR_CODE_EMPTY_PLAYLIST = -541478725;
    public static final int ERROR_CODE_404_NOT_FOUND = -875574520;
    public static final int ERROR_CODE_CONNECTION_REFUSED = -111;
    public static final int ERROR_CODE_CONNECTION_TIMEOUT = -110;
    public static final int ERROR_CODE_UNAUTHORIZED = -825242872;
    public static final int ERROR_CODE_PREPARE_TIMEOUT = -2001;
    public static final int ERROR_CODE_READ_FRAME_TIMEOUT = -2002;
    public static final int ERROR_CODE_HW_DECODE_FAILURE = -2003;
    private PLMediaPlayer.OnErrorListener I;

    public PLMediaPlayer(Context var1) {
        this(var1, (AVOptions)null);
    }

    public PLMediaPlayer(Context var1, AVOptions var2) {
        this.c = 0L;
        this.e = new com.pili.pldroid.player.qos.Qos1();
        this.j = 0;
        this.k = false;
        this.l = false;
        this.m = false;
        this.n = true;
        this.o = PlayerState.IDLE;
        this.p = null;
        this.q = null;
        this.r = null;
        this.s = null;
        this.t = null;
        this.u = false;
        this.v = new tv.danmaku.ijk.media.player.IMediaPlayer.OnVideoSizeChangedListener() {
            public void onVideoSizeChanged(IMediaPlayer var1, int var2, int var3, int var4, int var5) {
                if(PLMediaPlayer.this.H != null) {
                    PLMediaPlayer.this.H.onVideoSizeChanged(PLMediaPlayer.this, var2, var3, var4, var5);
                }

            }
        };
        this.w = new tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener() {
            public void onPrepared(IMediaPlayer var1) {
                if(PLMediaPlayer.this.D != null) {
                    PLMediaPlayer.this.D.onPrepared(PLMediaPlayer.this);
                }

                long var2 = System.currentTimeMillis() - PLMediaPlayer.this.c;
                Log.d("PLMediaPlayer", "on prepared: " + var2 + " ms");
                PLMediaPlayer.this.o = PlayerState.PREPARED;
            }
        };
        this.x = new tv.danmaku.ijk.media.player.IMediaPlayer.OnSeekCompleteListener() {
            public void onSeekComplete(IMediaPlayer var1) {
                if(PLMediaPlayer.this.G != null) {
                    PLMediaPlayer.this.G.onSeekComplete(PLMediaPlayer.this);
                }

            }
        };
        this.y = new tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener() {
            public boolean onInfo(IMediaPlayer var1, int var2, int var3) {
                switch(var2) {
                    case 3:
                        long var4 = System.currentTimeMillis() - PLMediaPlayer.this.c;
                        PLMediaPlayer.this.e.n = var4;
                        Log.d("PLMediaPlayer", "first video rendered: " + var4 + " ms");
                        PLMediaPlayer.this.o = PlayerState.PLAYING;
                        if(PLMediaPlayer.this.f != null) {
                            PLMediaPlayer.this.f.sendMessage(PLMediaPlayer.this.f.obtainMessage(0));
                        }
                        break;
                    case 701:
                        Log.d("PLMediaPlayer", "MEDIA_INFO_BUFFERING_START");
                        PLMediaPlayer.this.o = PlayerState.BUFFERING;
                        PLMediaPlayer.this.i = System.currentTimeMillis();
                        break;
                    case 702:
                        Log.d("PLMediaPlayer", "MEDIA_INFO_BUFFERING_END");
                        PLMediaPlayer.this.o = PlayerState.PLAYING;
                        ++PLMediaPlayer.this.e.c;
                        PLMediaPlayer.this.g++;
                        PLMediaPlayer.this.h = PLMediaPlayer.this.h + (System.currentTimeMillis() - PLMediaPlayer.this.i);
                        PLMediaPlayer.this.i = 0L;
                        break;
                    case 10002:
                        long var6 = System.currentTimeMillis() - PLMediaPlayer.this.c;
                        PLMediaPlayer.this.e.o = var6;
                        Log.d("PLMediaPlayer", "first audio rendered: " + var6 + " ms");
                        PLMediaPlayer.this.o = PlayerState.PLAYING;
                        break;
                    case 10003:
                        PLMediaPlayer.this.m = true;
                        PLMediaPlayer.this.e.p = (long)var3;
                }

                if(!PLMediaPlayer.this.k && PLMediaPlayer.this.m && PLMediaPlayer.this.e.n > 0L && PLMediaPlayer.this.e.o > 0L) {
                    PLMediaPlayer.this.c();
                }

                if(PLMediaPlayer.this.C != null) {
                    PLMediaPlayer.this.C.onInfo(PLMediaPlayer.this, var2, var3);
                }

                return true;
            }
        };
        this.z = new tv.danmaku.ijk.media.player.IMediaPlayer.OnBufferingUpdateListener() {
            public void onBufferingUpdate(IMediaPlayer var1, int var2) {
                if(PLMediaPlayer.this.F != null) {
                    PLMediaPlayer.this.F.onBufferingUpdate(PLMediaPlayer.this, var2);
                }

            }
        };
        this.A = new tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener() {
            public void onCompletion(IMediaPlayer var1) {
                if(PLMediaPlayer.this.E != null) {
                    PLMediaPlayer.this.E.onCompletion(PLMediaPlayer.this);
                }

                PLMediaPlayer.this.o = PlayerState.COMPLETED;
                if(!PLMediaPlayer.this.l) {
                    PLMediaPlayer.this.a(0, 0);
                }

            }
        };
        this.B = new tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener() {
            public boolean onError(IMediaPlayer var1, int var2, int var3) {
                Log.e("PLMediaPlayer", "Error happened, what = " + var2 + ", errorCode = " + var3);
                if(var3 == 0) {
                    var3 = -1;
                } else if(var3 == -2003 && PLMediaPlayer.this.j == 2) {
                    PLMediaPlayer.this.j = 0;
                    PLMediaPlayer.this.t.setInteger("mediacodec", PLMediaPlayer.this.j);
                    PLMediaPlayer.this.t.setInteger("start-on-prepared", 1);
                    if(PLMediaPlayer.this.C != null) {
                        PLMediaPlayer.this.C.onInfo(PLMediaPlayer.this, 802, 0);
                    }

                    PLMediaPlayer.this.d();
                    return true;
                }

                PLMediaPlayer.this.o = PlayerState.ERROR;
                if(!PLMediaPlayer.this.l) {
                    PLMediaPlayer.this.a(var3, var3);
                }

                return PLMediaPlayer.this.I != null?PLMediaPlayer.this.I.onError(PLMediaPlayer.this, var3):false;
            }
        };
        this.a = var1.getApplicationContext();
        this.t = var2;
        Qos2.a(this.a);
        this.a(var2);
        this.e.a();
    }

    private void a(AVOptions var1) {
        this.o = PlayerState.IDLE;
        this.l = false;
        this.b = new IjkMediaPlayer(new IjkLibLoader() {
            public void loadLibrary(String var1) throws UnsatisfiedLinkError, SecurityException {
                if(!PLMediaPlayer.d) {
//                    Log.i("PLMediaPlayer", "load shared lib:" + SharedLibraryNameHelper.getInstance().getSharedLibraryName());
                    PLMediaPlayer.d = true;
                    SharedLibraryNameHelper.getInstance().a();
                    IjkMediaPlayer.native_setLogLevel(6);
                }

            }
        });
        this.b.setOnPreparedListener(this.w);
        this.b.setOnInfoListener(this.y);
        this.b.setOnErrorListener(this.B);
        this.b.setOnCompletionListener(this.A);
        this.b.setOnBufferingUpdateListener(this.z);
        this.b.setOnSeekCompleteListener(this.x);
        this.b.setOnVideoSizeChangedListener(this.v);
        this.setAVOptions(var1);
    }

    public void release() {
        if(!this.n) {
            this.stop();
        }

        this.b.release();
        this.o = PlayerState.IDLE;
    }

    public void setDebugLoggingEnabled(boolean var1) {
        if(var1) {
            IjkMediaPlayer.native_setLogLevel(3);
        } else {
            IjkMediaPlayer.native_setLogLevel(6);
        }

    }

    public PlayerState getPlayerState() {
        return this.o;
    }

    public HashMap<String, String> getMetadata() {
        HashMap var1 = new HashMap();
        MediaInfo var2 = this.b.getMediaInfo();
        Set var3 = var2.mMeta.mMediaMeta.keySet();
        Iterator var4 = var3.iterator();

        while(var4.hasNext()) {
            String var5 = (String)var4.next();
            IjkMediaMeta var10001 = var2.mMeta;
            if(var5.compareTo("streams") != 0) {
                try {
                    String var6 = var2.mMeta.mMediaMeta.getString(var5);
                    if(var6 != null) {
                        var1.put(var5, var6);
                    }
                } catch (ClassCastException var7) {
                    ;
                }
            }
        }

        return var1;
    }

    private void a(String var1, String var2) {
        if(this.u) {
            try {
                this.k = false;
                this.m = false;
                this.l = false;
                URI var3 = new URI(var1);
                URI var4 = new URI(var2);
                Intent var5 = new Intent("pldroid-player-qos-filter");
                var5.putExtra("pldroid-qos-msg-type", 4);
                var5.putExtra("scheme", var3.getScheme());
                var5.putExtra("domain", var3.getHost());
                var5.putExtra("remoteIp", var4.getHost());
                var5.putExtra("path", var3.getPath());
                Qos3.a().a(var5);
            } catch (URISyntaxException var6) {
                var6.printStackTrace();
            } catch (Exception var7) {
                var7.printStackTrace();
            }

        }
    }

    private void c() {
        if(this.u) {
            this.k = true;
            Intent var1 = new Intent("pldroid-player-qos-filter");
            var1.putExtra("pldroid-qos-msg-type", 195);
            var1.putExtra("firstVideoTime", this.e.n);
            var1.putExtra("firstAudioTime", this.e.o);
            var1.putExtra("gopTime", this.e.p);
            if(this.j == 0) {
                this.e.r = "ffmpeg";
                this.e.s = "ffmpeg";
            } else {
                this.e.r = "droid264";
                this.e.s = "droidaac";
            }

            var1.putExtra("videoDecoderType", this.e.r);
            var1.putExtra("audioDecoderType", this.e.s);
            Qos3.a().a(var1);
        }
    }

    private void a(int var1, int var2) {
        if(this.u) {
            this.l = true;
            Intent var3 = new Intent("pldroid-player-qos-filter");
            var3.putExtra("pldroid-qos-msg-type", 196);
            var3.putExtra("beginAt", this.c);
            var3.putExtra("endAt", System.currentTimeMillis());
            var3.putExtra("bufferingTotalCount", this.g);
            var3.putExtra("bufferingTotalTimes", this.h);
            var3.putExtra("totalRecvBytes", this.e.q);
            int var4 = (int)(this.i > 0L?System.currentTimeMillis() - this.i:this.i);
            var3.putExtra("endBufferingTime", var4);
            var3.putExtra("gopTime", this.e.p);
            var3.putExtra("errorCode", var1);
            var3.putExtra("errorOSCode", var2);
            Qos3.a().a(var3);
        }
    }

    private void setAVOptions(AVOptions var1) {
        if(var1 != null) {

//            this.b.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
//            this.b.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
//            this.b.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", var1.getInteger("start-on-prepared", 1));
//            this.b.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
//            this.b.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
            this.b.setOption(4, "overlay-format", 842225234L);
            this.b.setOption(4, "framedrop", 1);
            this.b.setOption(4, "start-on-prepared", 1L);
            this.b.setOption(1, "http-detect-range-support", 0L);
            this.b.setOption(2, "skip_loop_filter", 0L);

            this.u = false;
            if(var1.containsKey("live-streaming") && var1.getInteger("live-streaming") != 0) {
                this.u = true;
                this.b.setOption(1, "rtmp_live", 1L);
                this.b.setOption(1, "rtmp_buffer", var1.containsKey("rtmp_buffer")?(long)var1.getInteger("rtmp_buffer"):100L);
                if(var1.containsKey("timeout")) {
                    this.b.setOption(1, "timeout", (long)(var1.getInteger("timeout") * 1000));
                }

                this.b.setOption(2, "threads", "1");
            }

            this.b.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "async,cache,crypto,file,http,https,ijkhttphook,ijkinject,ijklivehook,ijklongurl,ijksegment,ijktcphook,pipe,rtp,tcp,tls,udp,ijkurlhook,data,rtmp");

            String var3 = "analyzeduration";
            this.b.setOption(1, var3, var1.containsKey(var3)?(long)var1.getInteger(var3):0L);
            this.b.setOption(1, "analyzemaxduration", var1.containsKey("analyzemaxduration")?(long)var1.getInteger("analyzemaxduration"):100L);
            this.b.setOption(1, "probesize", var1.containsKey("probesize")?(long)var1.getInteger("probesize"):131072L);
            this.b.setOption(4, "live-streaming", (long)(this.u?1:0));
            this.b.setOption(4, "get-av-frame-timeout", var1.containsKey("get-av-frame-timeout")?(long)(var1.getInteger("get-av-frame-timeout") * 1000):10000000L);
            this.j = var1.containsKey("mediacodec")?var1.getInteger("mediacodec"):0;

            this.b.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", this.j == 0?0:1);

            this.b.setOption(4, "delay-optimization", var1.containsKey("delay-optimization")?(long)var1.getInteger("delay-optimization"):0L);
            this.b.setOption(4, "cache-buffer-duration", var1.containsKey("cache-buffer-duration")?(long)var1.getInteger("cache-buffer-duration"):20L);
            this.b.setOption(4, "max-cache-buffer-duration", var1.containsKey("max-cache-buffer-duration")?(long)var1.getInteger("max-cache-buffer-duration"):40L);
            this.b.setOption(1, "reconnect", var1.containsKey("reconnect")?(long)var1.getInteger("reconnect"):1L);

            this.b.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", var1.containsKey("max_cached_duration")?(long)var1.getInteger("max_cached_duration"):700);
            this.b.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1);
            this.b.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
            this.b.setOption(1, "flush_packets", 1L);
        }
    }

    public boolean a() {
        return this.u;
    }

    public void setDisplay(SurfaceHolder var1) {
        this.b.setDisplay(var1);
        this.r = var1;
    }

    public void setSurface(Surface var1) {
        this.b.setSurface(var1);
        this.s = var1;
    }

    public void setWakeMode(Context var1, int var2) {
        this.b.setWakeMode(var1, var2);
    }

    public void setScreenOnWhilePlaying(boolean var1) {
        this.b.setScreenOnWhilePlaying(var1);
    }

    public void setDataSource(Context var1, Uri var2) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        this.setDataSource(var1, var2, (Map)null);
    }

    public void setDataSource(Context var1, Uri var2, Map<String, String> var3) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        Uri var4 = PLNetworkManager.getInstance().a(var2);
        this.a(var2.toString(), var4.toString());
        if(VERSION.SDK_INT > 14) {
            this.b.setDataSource(var1, var4, var3);
            this.q = var3;
        } else {
            this.b.setDataSource(var4.toString());
        }

        this.p = var2.toString();
    }

    public void setDataSource(String var1) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        String var2 = PLNetworkManager.getInstance().a(var1);
        this.a(var1, var2);
        this.b.setDataSource(var2);
        this.p = var1;
    }

    public void setDataSource(FileDescriptor var1) throws IOException, IllegalArgumentException, IllegalStateException {
        this.b.setDataSource(var1);
    }

    public String getDataSource() {
        return this.b.getDataSource();
    }

    public void prepareAsync() throws IllegalStateException {
        String var1 = this.getDataSource();
        Log.i("wsDvr","PLMediaPlayer prepareAsync var1:" + var1);
        if(var1 != null && var1.contains(".m3u8")) {
            this.b.setOption(4, "delay-optimization", 0L);
        }

        this.o = PlayerState.PREPARING;
        this.b.prepareAsync();
        this.c = System.currentTimeMillis();
        this.h = 0L;
        this.g = 0L;
        this.n = true;
        this.k = false;
        this.l = false;
        this.m = false;
        HandlerThread var2 = new HandlerThread("PlayerHt");
        var2.start();
        this.f = new PLMediaPlayer.a(var2.getLooper(), this);
    }

    public void setVolume(float var1, float var2) {
        this.b.setVolume(var1, var2);
    }

    public void start() throws IllegalStateException {
        this.b.start();
    }

    public void pause() throws IllegalStateException {
        this.b.pause();
        this.o = PlayerState.PAUSED;
    }

    public void stop() throws IllegalStateException {
        if(!this.l) {
            this.e.q = this.b.getPktTotalSize();
            this.a(0, 0);
        }

        this.b.stop();
        Qos2.b(this.a);
        if(this.f != null) {
            this.f.removeCallbacksAndMessages((Object)null);
            this.f.a();
        }

        this.n = true;
    }

    private void d() {
        (new Handler(Looper.getMainLooper())).post(new Runnable() {
            public void run() {
                try {
                    PLMediaPlayer.this.b.stop();
                    PLMediaPlayer.this.b.release();
                    PLMediaPlayer.this.a(PLMediaPlayer.this.t);
                    if(PLMediaPlayer.this.q == null) {
                        PLMediaPlayer.this.b.setDataSource(PLMediaPlayer.this.p);
                    } else {
                        PLMediaPlayer.this.b.setDataSource(PLMediaPlayer.this.p, PLMediaPlayer.this.q);
                    }

                    if(PLMediaPlayer.this.r != null) {
                        PLMediaPlayer.this.b.setDisplay(PLMediaPlayer.this.r);
                    } else if(PLMediaPlayer.this.s != null) {
                        PLMediaPlayer.this.b.setSurface(PLMediaPlayer.this.s);
                    }

                    PLMediaPlayer.this.b.prepareAsync();
                } catch (IOException var2) {
                    var2.printStackTrace();
                    PLMediaPlayer.this.e();
                } catch (Exception var3) {
                    var3.printStackTrace();
                    PLMediaPlayer.this.e();
                }

            }
        });
    }

    private void e() {
        if(this.I != null) {
            this.I.onError(this, -1);
        }

    }

    public void seekTo(long var1) throws IllegalStateException {
        this.b.seekTo(var1);
    }

    public void reset() {
        this.b.reset();
    }

    public int getVideoWidth() {
        return this.b.getVideoWidth();
    }

    public String getResolutionInline() {
        String var1 = null;
        if(this.b != null) {
            try {
                MediaInfo var2 = this.b.getMediaInfo();
                var1 = var2.mMeta.mVideoStream.getResolutionInline();
            } catch (Exception var3) {
                ;
            }
        }

        return var1;
    }

    public int getVideoHeight() {
        return this.b.getVideoHeight();
    }

    public long getVideoBitrate() {
        return this.b.getBitrateVideo();
    }

    public int getVideoFps() {
        return (int)this.b.getVideoOutputFramesPerSecond();
    }

    public boolean isPlaying() {
        return this.b.isPlaying();
    }

    public long getCurrentPosition() {
        return this.b.getCurrentPosition();
    }

    public long getDuration() {
        return this.b.getDuration();
    }

    public void setLooping(boolean var1) {
        this.b.setLooping(var1);
    }

    public boolean isLooping() {
        return this.b.isLooping();
    }

    public void setOnInfoListener(PLMediaPlayer.OnInfoListener var1) {
        this.C = var1;
    }

    public void setOnPreparedListener(PLMediaPlayer.OnPreparedListener var1) {
        this.D = var1;
    }

    public void setOnCompletionListener(PLMediaPlayer.OnCompletionListener var1) {
        this.E = var1;
    }

    public void setOnBufferingUpdateListener(PLMediaPlayer.OnBufferingUpdateListener var1) {
        this.F = var1;
    }

    public void setOnSeekCompleteListener(PLMediaPlayer.OnSeekCompleteListener var1) {
        this.G = var1;
    }

    public void setOnVideoSizeChangedListener(PLMediaPlayer.OnVideoSizeChangedListener var1) {
        this.H = var1;
    }

    public void setOnErrorListener(PLMediaPlayer.OnErrorListener var1) {
        this.I = var1;
    }

    private void f() {
        if(this.e != null && this.b != null) {
            this.e.d = (int)this.b.getSourcFpsVideo();
            this.e.e = (int)this.b.getFramesDroppedVideo();
            this.e.f = (int)this.b.getSourcFpsAudio();
            this.e.g = (int)this.b.getFramesDroppedAudio();
            this.e.h = (int)this.b.getVideoOutputFramesPerSecond();
            this.e.i = (int)this.b.getRenderFpsAudio();
            this.e.j = (int)this.b.getBufferTimeVideo();
            this.e.k = (int)this.b.getBufferTimeAudio();
            this.e.l = this.b.getBitrateVideo();
            this.e.m = this.b.getBitrateAudio();
        }
    }

    public static class a extends Handler {
        private WeakReference<PLMediaPlayer> a;

        public a(Looper var1, PLMediaPlayer var2) {
            super(var1);
            this.a = new WeakReference(var2);
        }

        public void a() {
            this.getLooper().quit();
            this.a.clear();
        }

        public void handleMessage(Message var1) {
            PLMediaPlayer var2 = (PLMediaPlayer)this.a.get();
            if(var2 != null && var2.e != null) {
                switch(var1.what) {
                    case 0:
                        var2.f();
                        com.pili.pldroid.player.qos.Qos1 var3 = var2.e;
                        var3.b = System.currentTimeMillis();
                        Intent var4 = new Intent("pldroid-player-qos-filter");
                        var4.putExtra("pldroid-qos-msg-type", 193);
                        var4.putExtra("beginAt", var3.a);
                        var4.putExtra("endAt", var3.b);
                        var4.putExtra("bufferingTimes", var3.c);
                        var4.putExtra("videoSourceFps", var3.d);
                        var4.putExtra("dropVideoFrames", var3.e);
                        var4.putExtra("audioSourceFps", var3.f);
                        var4.putExtra("audioDropFrames", var3.g);
                        var4.putExtra("videoRenderFps", var3.h);
                        var4.putExtra("audioRenderFps", var3.i);
                        var4.putExtra("videoBufferTime", var3.j);
                        var4.putExtra("audioBufferTime", var3.k);
                        var4.putExtra("videoBitrate", var3.l);
                        var4.putExtra("audioBitrate", var3.m);
                        if(var3.p > 0L && var3.a > 0L) {
                            Qos3.a().a(var4);
                        }

                        var3.a = System.currentTimeMillis();
                        var2.e.a();
                        this.sendMessageDelayed(this.obtainMessage(0), (long)Qos3.a().b());
                    default:
                }
            } else {
                Log.w("PLMediaPlayer", "MuxerHandler.handleMessage: muxer is null");
            }
        }
    }

    public interface OnErrorListener {
        boolean onError(PLMediaPlayer var1, int var2);
    }

    public interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(PLMediaPlayer var1, int var2, int var3, int var4, int var5);
    }

    public interface OnSeekCompleteListener {
        void onSeekComplete(PLMediaPlayer var1);
    }

    public interface OnBufferingUpdateListener {
        void onBufferingUpdate(PLMediaPlayer var1, int var2);
    }

    public interface OnCompletionListener {
        void onCompletion(PLMediaPlayer var1);
    }

    public interface OnPreparedListener {
        void onPrepared(PLMediaPlayer var1);
    }

    public interface OnInfoListener {
        boolean onInfo(PLMediaPlayer var1, int var2, int var3);
    }
}
