package com.example.jrd48;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.FileTransfer.DownloadFile;
import com.example.jrd48.chat.FileTransfer.TransferService;
import com.example.jrd48.chat.MsgPicInfo;
import com.luobin.dvr.R;
import com.example.jrd48.chat.ToastR;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.sample.HackyViewPager;

import static com.example.jrd48.service.protocol.root.ReceiverProcesser.getMyDataRoot;

public class ShowPhotoActivity extends BaseActivity {


    DownloadFile mDownloadFile;
    //private PhotoView viewPager;
    private HackyViewPager hViewPager;
    private Button btnDownload;
    private int currentNum;
    private IntentFilter filterDown;
    private DownloadReceiiver downloadReceiver;
    private String myPhone;

    private Toolbar toolbar;
    private List<MsgPicInfo> msgPicInfos;
    private Map<Integer, PhotoView> mPhotoViews = new HashMap<Integer, PhotoView>();
    private void showFullscreen(boolean enable) {
        if (enable) {
            toolbar.setVisibility(View.GONE);

            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            toolbar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_photo);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        hViewPager = (HackyViewPager) findViewById(R.id.view_pager);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle("查看图片");
        //viewPager = (PhotoView) findViewById(R.id.view_pager);
        btnDownload = (Button) findViewById(R.id.buttonDownload);
        btnDownload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowPhotoActivity.this.doDowndloadImage();
            }
        });
        mDownloadFile = new DownloadFile(this, getBroadcastManager());


        showFullscreen(true);
        doWithIntent();

    }

    private void doWithIntent() {
        SharedPreferences preferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        myPhone = preferences.getString("phone", "");
        Intent i = getIntent();
        if (i != null) {
//            msgID = i.getLongExtra("msgid", -1);
//            teamID = i.getLongExtra("teamid", 0);
//            phone = i.getStringExtra("otherphone");
//            sn = i.getLongExtra("sn", -1);
            msgPicInfos = (List<MsgPicInfo>) i.getSerializableExtra("all_pic_info");
            if (msgPicInfos == null || msgPicInfos.isEmpty()) {
                ToastR.setToast(this, "参数传递错误");
                this.finish();
                return;
            }
            currentNum = i.getIntExtra("curr_index", msgPicInfos.size() - 1);
            hViewPager.setAdapter(new SamplePagerAdapter());
            hViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageSelected(final int position) {
                    currentNum = position;
                    showBtn(position);
                }

                @Override
                public void onPageScrolled(int arg0, float arg1, int arg2) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onPageScrollStateChanged(int arg0) {
                    // TODO Auto-generated method stub
                }
            });
            if (currentNum == 0) {
                showBtn(0);
            } else {
                hViewPager.setCurrentItem(currentNum);
            }
        }
        setIntent(null);

        filterDown = new IntentFilter();
        downloadReceiver = new DownloadReceiiver();
        filterDown.addAction("download.percent");
    }

    private void showBtn(int position) {
        MsgPicInfo pic = msgPicInfos.get(position);
        File fileTrue = new File(pic.getAddress());
        File fileThumb = null;
        try {
            if (!fileTrue.exists()) {
                btnDownload.setVisibility(View.VISIBLE);
                if (pic.getDownload()) {
                    btnDownload.setText("正在下载");
                    btnDownload.setClickable(false);
                } else {
                    if (myPhone.equals(pic.getPhone())) {
                        btnDownload.setVisibility(View.GONE);
                    } else {
                        btnDownload.setText("获取图片");
                        btnDownload.setClickable(true);
                    }
                }
            } else {
                btnDownload.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            ToastR.setToast(ShowPhotoActivity.this, "无法查看相应文件");
            ShowPhotoActivity.this.finish();
            e.printStackTrace();
        }
    }

    private void savePic(){
        MsgPicInfo pic = msgPicInfos.get(currentNum);
        if(pic.getPhone().equals(myPhone)){
            return;
        }
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM+"/luobin");
        if(!path.exists()){
            path.mkdirs();
        }
        File image = new File(path.getAbsolutePath(), pic.getMsgID()+".jpg");
        if(image.exists()){
            ToastR.setToast(this,"图片已经保存过~");
        }else{
            try {
                int bytesum = 0;
                int byteread = 0;
                File oldfile = new File(pic.getAddress());
                if (oldfile.exists()) { //文件不存在时
                    InputStream inStream = new FileInputStream(pic.getAddress()); //读入原文件
                    FileOutputStream fs = new FileOutputStream(image.getAbsolutePath());
                    byte[] buffer = new byte[1024];
                    int length;
                    while ( (byteread = inStream.read(buffer)) != -1) {
                        bytesum += byteread; //字节数 文件大小
                        System.out.println(bytesum);
                        fs.write(buffer, 0, byteread);
                    }
                    inStream.close();
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File f = new File(image.getAbsolutePath());
                    Uri contentUri = Uri.fromFile(f);
                    mediaScanIntent.setData(contentUri);
                    sendBroadcast(mediaScanIntent);
                    ToastR.setToastLong(this,"图片已经保存在："+image.getAbsolutePath());
                }else{
                    ToastR.setToast(this,"大图还未下载");
                }
            }
            catch (Exception e) {
                System.out.println("保存图片出错");
                e.printStackTrace();

            }
        }
    }

    private void doDowndloadImage() {
        MsgPicInfo pic = msgPicInfos.get(currentNum);
        pic.setDownload(true);
        Intent i = new Intent(this, TransferService.class);
        i.putExtra("type",TransferService.DOWNLOAD_FILE);
        i.putExtra("teamid",pic.getTeamID());
        i.putExtra("phone",pic.getPhone());
        i.putExtra("msgid",pic.getMsgID());
        i.putExtra("address",pic.getAddress());
        startService(i);
        //mDownloadFile.startDownload(new TansferFileDown(pic.getTeamID(), pic.getPhone(), pic.getMsgID(), pic.getAddress()));
        btnDownload.setClickable(false);
        btnDownload.setText("正在下载");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(downloadReceiver, filterDown);
    }

    @Override
    protected void onPause() {
        if (downloadReceiver != null) {
            unregisterReceiver(downloadReceiver);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mDownloadFile.stopAll();
        super.onDestroy();
    }

    class DownloadReceiiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            long msgid = intent.getLongExtra("msgid", -1);
            MsgPicInfo pic = msgPicInfos.get(currentNum);
            if (pic.getMsgID() == msgid) {
                if (intent.getBooleanExtra("success", false)) {
                    Log.i("jrdchat", "下载图片成功了~");
                    File file = new File(pic.getAddress());
                    if (file.exists()) {
                        try {
                            //Log.i("jrdchat", "文件长度：" + file.length());
                            PhotoView view = mPhotoViews.get(currentNum);
                            //Log.i("jrdchat", "view: " + view.toString());
                            view.setImageURI(Uri.fromFile(file));
                        } catch (OutOfMemoryError e) {
                            ToastR.setToast(ShowPhotoActivity.this, "内存不足，无法显示！");
                            ShowPhotoActivity.this.finish();
                            return;
                        }
                        btnDownload.setVisibility(View.GONE);
                    } else {
                        pic.setDownload(false);
                        btnDownload.setClickable(true);
                        btnDownload.setText("获取图片");
                    }
                } else {
                    //还未成功
                    int percent = intent.getIntExtra("percent", -1);
                    if (percent != -1) {
                        //btnDownload.setClickable(false);
                        pic.setDownload(true);
                        btnDownload.setText("正在下载 " + percent + "%");
                    } else {
                        Log.i("jrdchat", "下载图片失败了~");
                        pic.setDownload(false);
                        ToastR.setToast(ShowPhotoActivity.this, "图片下载失败！");
                        btnDownload.setClickable(true);
                        btnDownload.setText("获取图片");
                    }
                }
            }
        }
    }

    public class SamplePagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return msgPicInfos.size();
        }
        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
        @Override
        public View instantiateItem(ViewGroup container, final int position) {
            PhotoView photoView = new PhotoView(container.getContext());
            photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float x, float y) {
                    if (toolbar.getVisibility() == View.VISIBLE) {
                        showFullscreen(true);
                    } else {
                        showFullscreen(false);
                    }
                }
            });
            photoView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ShowPhotoActivity.this);
                    String[] headItems = new String[]{"保存图片"};
                    builder.setItems(headItems, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    savePic();
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                    AlertDialog simplelistdialog = builder.create();
                    simplelistdialog.show();
                    return false;
                }
            });
            // Now just add PhotoView to ViewPager and return it
            container.addView(photoView, ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT);
            MsgPicInfo pic = msgPicInfos.get(position);
            File fileTrue = new File(pic.getAddress());
            File fileThumb = null;
            try {
                if (!fileTrue.exists()) {
                    fileThumb = new File(getMyDataRoot(ShowPhotoActivity.this) + "/" + myPhone + "/" + pic.getTeamID()
                            + "/" + pic.getPhone() + "/" + pic.getSn() + ".jpg");
                    if (fileThumb.exists()) {
                        photoView.setImageURI(Uri.fromFile(fileThumb));
                    } else {
                        if (pic.getPictures() != null && pic.getPictures().length > 0) {
                            photoView.setImageBitmap(BitmapFactory.decodeByteArray(pic.getPictures(), 0, pic.getPictures().length));
                        } else {
                            throw new RuntimeException("文件没找到: " + fileThumb.getAbsolutePath());
                        }
                    }
                } else {
                    photoView.setImageURI(Uri.fromFile(fileTrue));
                }
            } catch (Exception e) {
                ToastR.setToast(ShowPhotoActivity.this, "无法查看相应文件");
                ShowPhotoActivity.this.finish();
                e.printStackTrace();
            }

            mPhotoViews.put(position, photoView);
            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // super.destroyItem(container, position, object);
            container.removeView((View) (object));
            mPhotoViews.remove(position);
        }

    }
}

