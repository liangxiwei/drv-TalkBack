package com.example.jrd48.chat.VersionUpdate;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Administrator on 2017/1/13.
 */

public class MyDownloader {

    MyAppUpgradeListener func;
    Object locker = new Object();
    Object locker2 = new Object();

    private boolean bDownloading = false;
    private boolean bStop = false;


    public boolean isbDownloading() {
        synchronized (locker2) {
            return bDownloading;
        }

    }

    public void setbDownloading(boolean bDownloading) {
        synchronized (locker2) {
            this.bDownloading = bDownloading;
        }

    }


    public boolean isbStop() {
        synchronized (locker) {
            return bStop;
        }

    }

    public synchronized void setbStop(boolean bStop) {
        synchronized (locker) {
            this.bStop = bStop;
        }

    }

    public MyAppUpgradeListener getDownloadListener() {
        return func;
    }

    public void setDownloadListener(MyAppUpgradeListener func) {
        this.func = func;
    }

    private int downLoadFileSize;

    /**
     * 文件下载
     *
     * @param url：文件的下载地址
     * @param path：文件保存到本地的地址
     */
    public void down_file(String url, String path) {
        try {
            downLoadFileSize = 0;

            setbStop(false);
            setbDownloading(true);

            // 下载函数
            String filename = url.substring(url.lastIndexOf("/") + 1);
            // 获取文件名
            URL myURL = new URL(url);
            URLConnection conn = myURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            int fileSize = conn.getContentLength();// 根据响应获取文件大小
            if (fileSize <= 0) {
                throw new RuntimeException("无法获知文件大小 ");
            }
            if (func != null) {
                func.onGetSize(fileSize);
            }

            if (is == null) {
                throw new RuntimeException("stream is null");
            }

            File f = new File(path + "/" + filename);
            if (f.exists()) {
//                Log.i("INSTALL", "删除旧的安装包： " + f.getName());
                f.delete();
            }

            File file1 = new File(path);
            File file2 = new File(path + filename);
            if (!file1.exists()) {
                file1.mkdirs();
            }
            if (!file2.exists()) {
                file2.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(path + "/" + filename);
            // 把数据存入路径+文件名
            byte buf[] = new byte[10240];
            downLoadFileSize = 0;
            if (func != null) {
                func.onBeginDownload();
            }
            do {
                // 循环读取
                int numread = is.read(buf);
                if (numread == -1) {
                    break;
                }
                fos.write(buf, 0, numread);
                downLoadFileSize += numread;
                if (func != null) {
                    func.onProgress(downLoadFileSize);
                }
                Thread.sleep(50);
            } while (!isbStop());

            if (isbStop()) {

            } else {
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    is.close();
                } catch (Exception ex) {
                    Log.e("tag", "error: " + ex.getMessage(), ex);
                }

                if (func != null) {
                    func.onComplete();
                }
            }

            setbDownloading(false);
        } catch (Exception e) {
            setbDownloading(false);
            e.printStackTrace();
            if (func != null) {
                func.onFailed(e);
            }
        }
    }

}
