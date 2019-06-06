//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pili.pldroid.player.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;

import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.IMediaController;
import com.pili.pldroid.player.IMediaController.MediaPlayerControl;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.PLMediaPlayer.OnBufferingUpdateListener;
import com.pili.pldroid.player.PLMediaPlayer.OnCompletionListener;
import com.pili.pldroid.player.PLMediaPlayer.OnErrorListener;
import com.pili.pldroid.player.PLMediaPlayer.OnInfoListener;
import com.pili.pldroid.player.PLMediaPlayer.OnPreparedListener;
import com.pili.pldroid.player.PLMediaPlayer.OnSeekCompleteListener;
import com.pili.pldroid.player.PLMediaPlayer.OnVideoSizeChangedListener;
import com.pili.pldroid.player.PlayerState;

import java.io.IOException;
import java.util.HashMap;

abstract class Widget1 extends FrameLayout implements MediaPlayerControl {
    public static final int ASPECT_RATIO_ORIGIN = 0;
    public static final int ASPECT_RATIO_FIT_PARENT = 1;
    public static final int ASPECT_RATIO_PAVED_PARENT = 2;
    public static final int ASPECT_RATIO_16_9 = 3;
    public static final int ASPECT_RATIO_4_3 = 4;
    private int c = 0;
    private int d = 0;
    private long e = 0L;
    private int f = 0;
    protected Surface a;
    private Uri g;
    private AVOptions h;
    private int i = 0;
    private int j = 0;
    private View k;
    private InterfaceWidget1 l;
    private PLMediaPlayer m;
    private IMediaController n;
    private View o = null;
    private View topRightImage = null;
    private int p = 1;
    private boolean q = false;
    private boolean r = true;
    private int s = 1;
    private float t = -1.0F;
    private float u = -1.0F;
    private boolean v = false;
    protected boolean b = true;
    private OnCompletionListener w;
    private OnPreparedListener x;
    private OnErrorListener y;
    private OnInfoListener z;
    private OnBufferingUpdateListener A;
    private OnSeekCompleteListener B;
    private OnVideoSizeChangedListener C;
    private OnPreparedListener D = new OnPreparedListener() {
        public void onPrepared(PLMediaPlayer var1) {
            Widget1.this.i = 2;
            Widget1.this.c = var1.getVideoWidth();
            Widget1.this.d = var1.getVideoHeight();
            if(Widget1.this.x != null) {
                Widget1.this.x.onPrepared(var1);
            }

            if(Widget1.this.n != null) {
                Widget1.this.n.setEnabled(true);
            }

            if(Widget1.this.e != 0L) {
                Widget1.this.seekTo(Widget1.this.e);
            }

            if(Widget1.this.j == 3) {
                Widget1.this.start();
                if(Widget1.this.n != null) {
                    Widget1.this.n.show();
                }
            }

        }
    };
    private OnVideoSizeChangedListener E = new OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(PLMediaPlayer var1, int var2, int var3, int var4, int var5) {
            if(Widget1.this.C != null) {
                Widget1.this.C.onVideoSizeChanged(var1, var2, var3, var4, var5);
            }

            Widget1.this.c = var1.getVideoWidth();
            Widget1.this.d = var1.getVideoHeight();
            if(Widget1.this.c != 0 && Widget1.this.d != 0) {
                Widget1.this.l.a(Widget1.this.c, Widget1.this.d);
                Widget1.this.requestLayout();
            }

        }
    };
    private OnSeekCompleteListener F = new OnSeekCompleteListener() {
        public void onSeekComplete(PLMediaPlayer var1) {
            if(Widget1.this.B != null) {
                Widget1.this.B.onSeekComplete(var1);
            }

        }
    };
    private OnInfoListener G = new OnInfoListener() {
        public boolean onInfo(PLMediaPlayer var1, int var2, int var3) {
            if(Widget1.this.z != null) {
                Widget1.this.z.onInfo(var1, var2, var3);
            }

            if(Widget1.this.k != null) {
                if(var2 == 701 && !var1.a()) {
                    Widget1.this.k.setVisibility(VISIBLE);
                } else if(var2 == 702 || var2 == 10002 || var2 == 3) {
                    Widget1.this.k.setVisibility(GONE);
                }
            }

            if(var2 == 3 && Widget1.this.o != null) {
                Widget1.this.o.setVisibility(GONE);
                if(Widget1.this.topRightImage != null) {
                    Widget1.this.topRightImage.setVisibility(VISIBLE);
                }
            }

            return true;
        }
    };
    private OnBufferingUpdateListener H = new OnBufferingUpdateListener() {
        public void onBufferingUpdate(PLMediaPlayer var1, int var2) {
            Widget1.this.f = var2;
            if(Widget1.this.A != null) {
                Widget1.this.A.onBufferingUpdate(var1, var2);
            }

        }
    };
    private OnCompletionListener I = new OnCompletionListener() {
        public void onCompletion(PLMediaPlayer var1) {
            if(Widget1.this.n != null) {
                Widget1.this.n.hide();
            }

            if(Widget1.this.k != null) {
                Widget1.this.k.setVisibility(GONE);
            }

            if(Widget1.this.w != null) {
                Widget1.this.w.onCompletion(var1);
            }

            Widget1.this.i = 5;
            Widget1.this.j = 5;
        }
    };
    private OnErrorListener J = new OnErrorListener() {
        public boolean onError(PLMediaPlayer var1, int var2) {
            Widget1.this.i = -1;
            Widget1.this.j = -1;
            if(Widget1.this.n != null) {
                Widget1.this.n.hide();
            }

            if(Widget1.this.k != null) {
                Widget1.this.k.setVisibility(GONE);
            }

            return Widget1.this.y != null?Widget1.this.y.onError(var1, var2):true;
        }
    };
    private InterfaceWidget1.InterfaceWidget2 K = new InterfaceWidget1.InterfaceWidget2() {
        public void a(Surface var1, int var2, int var3) {
            if(Widget1.this.a == null) {
                Widget1.this.a = var1;
            }

            if(Widget1.this.m != null) {
                Widget1.this.a(Widget1.this.m, var1);
            } else {
                Widget1.this.c();
            }

        }

        public void b(Surface var1, int var2, int var3) {
            boolean var4 = Widget1.this.j == 3;
            boolean var5 = Widget1.this.c == var2 && Widget1.this.d == var3;
            if(Widget1.this.m != null && var4 && var5) {
                if(Widget1.this.e != 0L) {
                    Widget1.this.seekTo(Widget1.this.e);
                }

                Widget1.this.start();
            }

        }

        public void a(Surface var1) {
            if(Widget1.this.n != null) {
                Widget1.this.n.hide();
            }

            Widget1.this.a();
        }
    };

    protected abstract InterfaceWidget1 getRenderView();

    public Widget1(Context var1) {
        super(var1);
        this.a(var1);
    }

    public Widget1(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.a(var1);
    }

    public Widget1(Context var1, AttributeSet var2, int var3) {
        super(var1, var2, var3);
        this.a(var1);
    }

    @TargetApi(21)
    public Widget1(Context var1, AttributeSet var2, int var3, int var4) {
        super(var1, var2, var3, var4);
        this.a(var1);
    }

    public void setDebugLoggingEnabled(boolean var1) {
        this.v = var1;
        if(this.m != null) {
            this.m.setDebugLoggingEnabled(var1);
        }

    }

    protected void a(Context var1) {
        this.l = this.getRenderView();
        this.l.setRenderCallback(this.K);
        LayoutParams var2 = new LayoutParams(-1, -1, 17);
        this.l.getView().setLayoutParams(var2);
        this.addView(this.l.getView());
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        this.requestFocus();
        this.i = 0;
        this.j = 0;
    }

    public void setDisplayAspectRatio(int var1) {
        this.p = var1;
        View var2 = this.getChildAt(0);
        if(var2 != null) {
            var2.requestLayout();
        }

    }

    public int getDisplayAspectRatio() {
        return this.p;
    }

    public void stopPlayback() {
        this.a(true);
    }

    protected void a() {
        if(this.m != null) {
            this.m.setDisplay((SurfaceHolder)null);
        }

    }

    public void setAVOptions(AVOptions var1) {
        this.h = var1;
    }

    public void setVideoPath(String var1) {
        if(var1 != null) {
            this.g = Uri.parse(var1);
            this.setVideoURI(this.g);
        } else {
            this.g = null;
        }

    }

    public void setVideoURI(Uri var1) {
        this.g = var1;
        if(var1 != null) {
            this.e = 0L;
            this.b = true;
            this.c();
            this.requestLayout();
            this.invalidate();
        }

    }

    public void setBufferingIndicator(View var1) {
        if(this.k != null) {
            this.k.setVisibility(GONE);
        }

        this.k = var1;
    }

    public void setCoverView(View var1) {
        this.o = var1;
    }

    public void setMediaController(IMediaController var1) {
        if(this.n != null) {
            this.n.hide();
        }

        this.n = var1;
        this.b();
    }

    public void setVolume(float var1, float var2) {
        this.t = var1;
        this.u = var2;
        if(this.m != null) {
            this.m.setVolume(var1, var2);
        }

    }

    public void setWakeMode(Context var1, int var2) {
        this.s = var2;
        if(this.m != null) {
            this.m.setWakeMode(var1.getApplicationContext(), var2);
        }

    }

    public void setScreenOnWhilePlaying(boolean var1) {
        this.r = var1;
        if(this.m != null) {
            this.m.setScreenOnWhilePlaying(var1);
        }

    }

    public void setLooping(boolean var1) {
        this.q = var1;
        if(this.m != null) {
            this.m.setLooping(var1);
        }

    }

    public boolean isLooping() {
        return this.q;
    }

    protected void b() {
        if(this.m != null && this.n != null) {
            this.n.setMediaPlayer(this);
            Object var1 = this.getParent() instanceof View?(View)this.getParent():this;
            this.n.setAnchorView((View)var1);
            this.n.setEnabled(this.e());
        }

    }

    protected void c() {
        if(this.g != null && this.a != null) {
            this.f = 0;
            this.a(false);
            AudioManager var1 = (AudioManager)this.getContext().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            var1.requestAudioFocus((OnAudioFocusChangeListener)null, 3, 1);

            try {
                this.m = new PLMediaPlayer(this.getContext(), this.h);
            } catch (UnsatisfiedLinkError var6) {
                var6.printStackTrace();
                return;
            }

            this.m.setDebugLoggingEnabled(this.v);
            this.m.setLooping(this.q);
            this.m.setScreenOnWhilePlaying(this.r);
            if(this.s != -1) {
                this.m.setWakeMode(this.getContext().getApplicationContext(), this.s);
            }

            if(this.t != -1.0F && this.u != -1.0F) {
                this.m.setVolume(this.t, this.u);
            }

            this.m.setOnPreparedListener(this.D);
            this.m.setOnVideoSizeChangedListener(this.E);
            this.m.setOnCompletionListener(this.I);
            this.m.setOnErrorListener(this.J);
            this.m.setOnInfoListener(this.G);
            this.m.setOnBufferingUpdateListener(this.H);
            this.m.setOnSeekCompleteListener(this.F);

            try {
                this.m.setDataSource(this.g.toString());
                this.a(this.m, this.a);
                this.m.prepareAsync();
                this.b();
                this.i = 1;
                return;
            } catch (IllegalArgumentException var3) {
                var3.printStackTrace();
            } catch (IllegalStateException var4) {
                var4.printStackTrace();
            } catch (IOException var5) {
                var5.printStackTrace();
            }

            if(this.y != null) {
                this.y.onError(this.m, -1);
            }

            this.i = -1;
            this.j = -1;
        }
    }

    protected void a(boolean var1) {
        if(this.m != null) {
            if(var1) {
                this.j = 0;
                this.g = null;
            }

            this.m.stop();
            this.m.release();
            this.m = null;
            this.i = 0;
            AudioManager var2 = (AudioManager)this.getContext().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            var2.abandonAudioFocus((OnAudioFocusChangeListener)null);
        }

    }

    public void start() {
        Log.v("wsDvr","Widget1 start this.i:" + this.i);
        if(this.i == 5) {
            this.setVideoURI(this.g);
            this.j = 3;
        } else {
            if(this.e()) {
                this.m.start();
                this.i = 3;
            }

            this.j = 3;
        }
    }

    public void pause() {
        if(this.e() && this.m.isPlaying()) {
            this.m.pause();
            this.i = 4;
        }

        this.j = 4;
    }

    public long getDuration() {
        return this.e()?this.m.getDuration():-1L;
    }

    public long getCurrentPosition() {
        return this.e()?this.m.getCurrentPosition():0L;
    }

    public void seekTo(long var1) {
        if(this.e()) {
            this.m.seekTo(var1);
            this.e = 0L;
        } else {
            this.e = var1;
        }

    }

    public boolean isPlaying() {
        return this.e() && this.m.isPlaying();
    }

    public int getBufferPercentage() {
        return this.f;
    }

    public boolean canPause() {
        return true;
    }

    public boolean canSeekBackward() {
        return true;
    }

    public boolean canSeekForward() {
        return true;
    }

    public PlayerState getPlayerState() {
        return this.m != null?this.m.getPlayerState():PlayerState.IDLE;
    }

    public HashMap<String, String> getMetadata() {
        return this.m != null?this.m.getMetadata():null;
    }

    public long getVideoBitrate() {
        return this.m != null?this.m.getVideoBitrate():0L;
    }

    public int getVideoFps() {
        return this.m != null?this.m.getVideoFps():0;
    }

    public String getResolutionInline() {
        return this.m != null?this.m.getResolutionInline():null;
    }

    private boolean e() {
        return this.m != null && this.i != -1 && this.i != 0 && this.i != 1;
    }

    public void setOnInfoListener(OnInfoListener var1) {
        this.z = var1;
    }

    public void setOnErrorListener(OnErrorListener var1) {
        this.y = var1;
    }

    public void setOnPreparedListener(OnPreparedListener var1) {
        this.x = var1;
    }

    public void setOnCompletionListener(OnCompletionListener var1) {
        this.w = var1;
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener var1) {
        this.A = var1;
    }

    public void setOnSeekCompleteListener(OnSeekCompleteListener var1) {
        this.B = var1;
    }

    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener var1) {
        this.C = var1;
    }

    public boolean onTouchEvent(MotionEvent var1) {
        if(this.e() && this.n != null) {
            this.d();
        }

        return false;
    }

    public boolean onTrackballEvent(MotionEvent var1) {
        if(this.e() && this.n != null) {
            this.d();
        }

        return false;
    }

    public boolean onKeyDown(int var1, KeyEvent var2) {
        boolean var3 = var1 != 4 && var1 != 24 && var1 != 25 && var1 != 164 && var1 != 82 && var1 != 5 && var1 != 6;
        if(this.e() && var3 && this.n != null) {
            if(var1 == 79 || var1 == 85) {
                if(this.m.isPlaying()) {
                    this.pause();
                    this.n.show();
                } else {
                    this.start();
                    this.n.hide();
                }

                return true;
            }

            if(var1 == 126) {
                if(!this.m.isPlaying()) {
                    this.start();
                    this.n.hide();
                }

                return true;
            }

            if(var1 == 86 || var1 == 127) {
                if(this.m.isPlaying()) {
                    this.pause();
                    this.n.show();
                }

                return true;
            }

            this.d();
        }

        return super.onKeyDown(var1, var2);
    }

    protected void d() {
        if(this.n.isShowing()) {
            this.n.hide();
        } else {
            this.n.show();
        }

    }

    protected void a(PLMediaPlayer var1, Surface var2) {
        if(var1 != null && var2 != null) {
            var1.setSurface(var2);
        }
    }

    public View getTopRightImage() {
        return topRightImage;
    }

    public void setTopRightImage(View topRightImage) {
        this.topRightImage = topRightImage;
    }

    protected interface InterfaceWidget1 {
        View getView();

        void a(int var1, int var2);

        void setRenderCallback(InterfaceWidget2 var1);

        public interface InterfaceWidget2 {
            void a(Surface var1, int var2, int var3);

            void b(Surface var1, int var2, int var3);

            void a(Surface var1);
        }
    }
}
