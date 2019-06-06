//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pili.pldroid.player.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.common.Common1;

public class PLVideoView extends Widget1 {
    private PLVideoView.a c;

    public PLVideoView(Context var1) {
        super(var1);
    }

    public PLVideoView(Context var1, AttributeSet var2) {
        super(var1, var2);
    }

    public PLVideoView(Context var1, AttributeSet var2, int var3) {
        super(var1, var2, var3);
    }

    @TargetApi(21)
    public PLVideoView(Context var1, AttributeSet var2, int var3, int var4) {
        super(var1, var2, var3, var4);
    }

    public SurfaceView getSurfaceView() {
        return this.c;
    }

    protected void a(Context var1) {
        this.c = new PLVideoView.a(var1);
        super.a(var1);
    }

    protected void a(PLMediaPlayer var1, Surface var2) {
        super.a(var1, var2);
        if(this.isPlaying() && !var1.a()) {
            var1.seekTo(var1.getCurrentPosition());
        }

    }

    protected InterfaceWidget1 getRenderView() {
        return this.c;
    }

    private class a extends SurfaceView implements InterfaceWidget1 {
        private InterfaceWidget2 b;
        private int c = 0;
        private int d = 0;
        private Callback e = new Callback() {
            public void surfaceCreated(SurfaceHolder var1) {
                if(a.this.b != null) {
                    a.this.b.a(var1.getSurface(), 0, 0);
                }
            }

            public void surfaceChanged(SurfaceHolder var1, int var2, int var3, int var4) {
                if(a.this.b != null) {
                    a.this.b.b(var1.getSurface(), var3, var4);
                }

            }

            public void surfaceDestroyed(SurfaceHolder var1) {
                if(a.this.b != null) {
                    a.this.b.a(var1.getSurface());
                    PLVideoView.this.a = null;
                }

            }
        };

        public a(Context var2) {
            super(var2);
            this.getHolder().addCallback(this.e);
        }

        protected void onMeasure(int var1, int var2) {
            Common1.a var3 = Common1.a(PLVideoView.this.getDisplayAspectRatio(), var1, var2, this.c, this.d);
            this.setMeasuredDimension(var3.a, var3.b);
        }

        public View getView() {
            return this;
        }

        public void a(int var1, int var2) {
            this.c = var1;
            this.d = var2;
            this.getHolder().setFixedSize(var1, var2);
            this.requestLayout();
        }

        public void setRenderCallback(InterfaceWidget2 var1) {
            this.b = var1;
        }
    }
}
