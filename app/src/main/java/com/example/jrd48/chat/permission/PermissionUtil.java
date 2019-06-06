package com.example.jrd48.chat.permission;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.AppOpsManagerCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.luobin.dvr.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/2/21.
 */
public class PermissionUtil {
    private static PermissionUtil permissionUtil = null;
    private static List<String> mListPermissions;
    public static final int MY_READ_LOCATION = 20001;
    public static final int MY_WRITE_EXTERNAL_STORAGE = 20002;
    public static final int MY_CAMERA = 20003;
    public static final int MY_READ_CONTACTS = 20004;
    public static final int MY_RECORD_AUDIO = 20005;
    public static final int MY_PERMISSIONS_CHECK_ALL = 20006;
    //    public static final String PERMISSIONS_READ_SMS = Manifest.permission.READ_SMS;//读取短信权限 ACCESS_COARSE_LOCATION
    public static final String PERMISSIONS_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;//定位手机权限
    public static final String PERMISSIONS_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;//写外部存储器
    public static final String PERMISSIONS_CAMERA = Manifest.permission.CAMERA;//相机权限
    public static final String PERMISSIONS_READ_CONTACTS = Manifest.permission.READ_CONTACTS; //读取通讯录;
    public static final String PERMISSIONS_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;//录音权限
    public static String TYPE = "";

    /**
     * 添加权限
     * author LH
     * data 2016/7/27 11:27
     */
    private void addAllPermissions(List<String> outListPermissions) {
        outListPermissions.add(PERMISSIONS_LOCATION);
        outListPermissions.add(PERMISSIONS_STORAGE);
        outListPermissions.add(PERMISSIONS_CAMERA);
        outListPermissions.add(PERMISSIONS_READ_CONTACTS);
        outListPermissions.add(PERMISSIONS_RECORD_AUDIO);
    }

    /**
     * 单例模式初始化
     * author LH
     * data 2016/7/27 11:27
     */
    public static synchronized PermissionUtil getInstance() {
        if (permissionUtil == null) {
            permissionUtil = new PermissionUtil();
        }
        return permissionUtil;
    }

    /**
     * 判断当前为M以上版本
     * author LH
     * data 2016/7/27 11:29
     */
    public static boolean isOverMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * 获得没有授权的权限
     * author LH
     * data 2016/7/27 11:46
     */
    @TargetApi(value = Build.VERSION_CODES.M)
    public List<String> findDeniedPermissions(Activity activity, List<String> permissions) {
        List<String> denyPermissions = new ArrayList<>();
        for (String value : permissions) {
            if (Build.VERSION.SDK_INT >= 23 && activity.checkSelfPermission(value) != PackageManager.PERMISSION_GRANTED) {
                denyPermissions.add(value);
            } else if(Build.VERSION.SDK_INT < 23 && ActivityCompat.checkSelfPermission(activity,value) != PackageManager.PERMISSION_GRANTED){
                denyPermissions.add(value);
            }
        }
        return denyPermissions;
    }

    /**
     * 判断所有权限
     * author LH
     * data 2016/7/27 13:37
     */
    @TargetApi(value = Build.VERSION_CODES.M)
    public void requestPermissions(Activity activity, int requestCode, PermissionCallBack permissionCallBack) {
        if (mListPermissions == null) {
            mListPermissions = new ArrayList<String>();
            addAllPermissions(mListPermissions);
        }
        if (!isOverMarshmallow()) {
            return;
        }
        mListPermissions = findDeniedPermissions(activity, mListPermissions);
        if (mListPermissions != null && mListPermissions.size() > 0) {
            activity.requestPermissions(mListPermissions.toArray(new String[mListPermissions.size()]),
                    requestCode);
        } else {
            permissionCallBack.onPermissionSuccess(TYPE);
        }
    }


    /**
     * 判断单个权限
     * author LH
     * data 2016/7/27 13:37
     */
    @TargetApi(value = Build.VERSION_CODES.M)
    public void requestPermission(Activity activity, int requestCode, PermissionCallBack permissionCallBack, String permission, String type) {
        if (!isOverMarshmallow()){
            //permissionCallBack.onPermissionFail(type);
            return ;
        }

        if (mListPermissions == null) {
            mListPermissions = new ArrayList<String>();
            mListPermissions.add(permission);
        } else {
            mListPermissions.clear();
            mListPermissions.add(permission);
        }

        mListPermissions = findDeniedPermissions(activity, mListPermissions);
        if (mListPermissions != null && mListPermissions.size() > 0) {
            activity.requestPermissions(mListPermissions.toArray(new String[mListPermissions.size()]),
                    requestCode);
        } else {
            permissionCallBack.onPermissionSuccess(type);
        }
    }

    /**
     * 判断单个权限
     * author LH
     * data 2016/7/27 13:37
     */
    @TargetApi(value = Build.VERSION_CODES.M)
    public void requestPermission1(Activity activity, int requestCode, PermissionCallBack permissionCallBack, String permission, String type) {
        if (!isOverMarshmallow()) {
            permissionCallBack.onPermissionSuccess(type);
            return;
        }

        if (mListPermissions == null) {
            mListPermissions = new ArrayList<String>();
            mListPermissions.add(permission);
        } else {
            mListPermissions.clear();
            mListPermissions.add(permission);
        }

        boolean isHasPermission = true;
        List<String> denyPermissions = new ArrayList<>();
        for (String permission1 : mListPermissions) {
            String op = AppOpsManagerCompat.permissionToOp(permission1);
            if (TextUtils.isEmpty(op)) continue;
            int result = AppOpsManagerCompat.noteProxyOp(activity, op, activity.getPackageName());
            if (result == AppOpsManagerCompat.MODE_IGNORED) {
                isHasPermission = false;
                denyPermissions.add(permission1);
                break;
//                return false;
            }
            result = ContextCompat.checkSelfPermission(activity, permission1);
            if (result != PackageManager.PERMISSION_GRANTED) {
                isHasPermission = false;
                denyPermissions.add(permission1);
                break;
//                return false;
            }
        }

//        mListPermissions = findDeniedPermissions(activity, mListPermissions);
        if (denyPermissions != null && denyPermissions.size() > 0) {
            activity.requestPermissions(denyPermissions.toArray(new String[denyPermissions.size()]),
                    requestCode);
        } else {
            permissionCallBack.onPermissionSuccess(type);
        }
    }

    /**
     * 权限请求返回值
     */
    public void requestResult(Activity activity, String[] permissions, int[] grantResults, PermissionCallBack permissionCallBack, String type) {
        mListPermissions = new ArrayList<String>();
        ArrayList<String> noPermissions = new ArrayList<String>();
        ArrayList<String> rejectPermissons = new ArrayList<String>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                if (isOverMarshmallow()) {
                    boolean isShould = activity.shouldShowRequestPermissionRationale(permissions[i]);
                    mListPermissions.add(permissions[i]);
                    if (isShould) {
                        noPermissions.add(permissions[i]);
                    } else {
                        rejectPermissons.add(permissions[i]);
                    }
                }
            }
        }

        if (noPermissions.size() > 0) {
            permissionCallBack.onPermissionFail(type);
        } else if (rejectPermissons.size() > 0) {
            ArrayList<String> permissonsList = new ArrayList<String>();
            for (int i = 0; i < rejectPermissons.size(); i++) {
                String strPermissons = rejectPermissons.get(i);

                if (PERMISSIONS_LOCATION.equals(strPermissons)) {
                    permissonsList.add("定位手机权限");
                } else if (PERMISSIONS_STORAGE.equals(strPermissons)) {
                    permissonsList.add("写外部存储器");
                } else if (PERMISSIONS_CAMERA.equals(strPermissons)) {
                    permissonsList.add("相机权限");
                } else if (PERMISSIONS_READ_CONTACTS.equals(strPermissons)) {
                    permissonsList.add("读取通讯录");
                } else if (PERMISSIONS_RECORD_AUDIO.equals(strPermissons)) {
                    permissonsList.add("录音权限");
                }


//                if (PERMISSIONS_STORAGE.equals(strPermissons)) {
//                    permissonsList.add(activity.getString(R.string.permission_storage));
//                } else if (PERMISSIONS_ACCOUNTS.equals(strPermissons)) {
//                    permissonsList.add(activity.getString(R.string.permission_accounts));
//                } else if (PERMISSIONS_PHONE.equals(strPermissons)) {
//                    permissonsList.add(activity.getString(R.string.permission_phone));
//                } else if (PERMISSIONS_LOCATION.equals(strPermissons)) {
//                    permissonsList.add(activity.getString(R.string.permission_location));
//                } else if (PERMISSIONS_AUDIO.equals(strPermissons)) {
//                    permissonsList.add(activity.getString(R.string.permission_audio));
//                }
            }
            String strPermissons = permissonsList.toString();
            strPermissons = strPermissons.replace("[", "");
            strPermissons = strPermissons.replace("]", "");
            strPermissons = strPermissons.replace(",", "、");
            String strAppName = activity.getString(R.string.app_name);
            String strMessage = activity.getString(R.string.permission_message);
            strMessage = String.format(strMessage + strAppName, strPermissons, "\"");
            permissionCallBack.onPermissionReject(type);

        } else {
            permissionCallBack.onPermissionSuccess(type);
        }
    }

    public interface PermissionCallBack {
        void onPermissionSuccess(String type);

        void onPermissionReject(String strMessage);

        void onPermissionFail(String failType);
    }

    /**
     * 获取设置所有权限
     * author LH
     * data 2016/7/27 11:27
     */
    public List<String> getPermissionsList() {
        List<String> mList = new ArrayList<String>();
        mList.add(PERMISSIONS_READ_CONTACTS);
        mList.add(PERMISSIONS_LOCATION);
        mList.add(PERMISSIONS_STORAGE);
        mList.add(PERMISSIONS_CAMERA);
        mList.add(PERMISSIONS_RECORD_AUDIO);
        return mList;
    }

    /**
     * 判断是否有权限没有允许
     * author LH
     * data 2016/7/27 11:27
     */
    public boolean checkPermissions(Activity activity) {
        List<String> mList = new ArrayList<String>();
        addAllPermissions(mList);
        if (!isOverMarshmallow()) {
            return false;
        }
        mList = findDeniedPermissions(activity, mList);
        if (mList != null && mList.size() > 0) {
            return true;
        } else {
            return false;
        }
    }
}
