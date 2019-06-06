package com.example.jrd48.chat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.jrd48.chat.permission.PermissionUtil;
import com.luobin.dvr.R;

public class AdditionLayout extends LinearLayout {
    public static int MY_PERMISSIONS_REQUEST_WRITE = 10033;

    public AdditionLayout(final Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.addition, this);
        Button camera = (Button) findViewById(R.id.camera);
        Button picture = (Button) findViewById(R.id.picture);
        Button video = (Button) findViewById(R.id.video);
        camera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (PermissionUtil.isOverMarshmallow()) {


                    /*if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        ToastR.setToastLong(getContext(), "请打开存储权限！");
                        return;
                    } else */if (getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((FirstActivity) getContext(), new String[]{
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_WRITE
                        );
                        return;
                    }
                }
                if (PermissionUtil.isOverMarshmallow()) {
                    /*if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                        ToastR.setToastLong(getContext(), "请打开拍照录像权限！");
                        return;
                    } else */if (getContext().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((FirstActivity) getContext(), new String[]{
                                        Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_WRITE
                        );
                        return;
                    }
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("拍摄");
                String[] headItems = new String[]{"拍照", "录像"};
                builder.setItems(headItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                ((FirstActivity) getContext()).openCamera();
                                break;
                            case 1:
                                ((FirstActivity) getContext()).openCameraVideo();
                                break;
                            default:
                                break;
                        }
                    }
                });
                AlertDialog simplelistdialog = builder.create();
                simplelistdialog.show();
            }
        });
        picture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FirstActivity) getContext()).openPicture();
            }
        });
        video.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FirstActivity) getContext()).openVideo();
            }
        });
    }
}