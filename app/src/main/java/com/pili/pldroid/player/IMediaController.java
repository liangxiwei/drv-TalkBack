//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pili.pldroid.player;

import android.view.View;

public interface IMediaController {
    void setMediaPlayer(IMediaController.MediaPlayerControl var1);

    void show();

    void show(int var1);

    void hide();

    boolean isShowing();

    void setEnabled(boolean var1);

    void setAnchorView(View var1);

    public interface MediaPlayerControl {
        void start();

        void pause();

        long getDuration();

        long getCurrentPosition();

        void seekTo(long var1);

        boolean isPlaying();

        int getBufferPercentage();

        boolean canPause();

        boolean canSeekBackward();

        boolean canSeekForward();
    }
}
