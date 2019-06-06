package com.example.jrd48.chat.VersionUpdate;

/**
 * Created by Administrator on 2017/1/13.
 */

public interface MyAppUpgradeListener {
    void onGetSize(int filesize);

    void onBeginDownload();

    void onProgress(int downloadedSize);

    void onComplete();

    void onFailed(Throwable e);
}
