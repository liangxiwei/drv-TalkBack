package com.example.jrd48.chat;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jrd48.service.TimeoutBroadcastManager;

/**
 * Created by jrd48
 */

public abstract class BaseLazyFragment extends Fragment {
    protected View mRootView;
    protected Context mContext;
    protected boolean isVisible;
    private boolean isPrepared;
    private boolean isFirst = true;
    Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private TimeoutBroadcastManager mTimeoutBroadcastManager = new TimeoutBroadcastManager();

    public TimeoutBroadcastManager getBroadcastManager() {
        return mTimeoutBroadcastManager;
    }
    //--------------------system method callback------------------------//

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isPrepared = true;
        initPrepare();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()){
            isVisible = true;
            lazyLoad();
        }else{
            isVisible = false;
            onInvisible();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getUserVisibleHint()){
            setUserVisibleHint(true);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    public FragmentActivity getMyActivity() {
        FragmentActivity temp = getActivity();
        if (temp == null) {
            Log.e("jrdchat", "fragment getactivity is null????");
            int pid = android.os.Process.myPid();    //获取当前应用程序的PID
            android.os.Process.killProcess(pid);    //杀死当前进程

            return null;
        }
        return temp;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(mRootView == null){
            mRootView = initView(inflater,container,savedInstanceState);
        }
        return mRootView;
    }

    //--------------------------------method---------------------------//

    /**
     * 懒加载
     */
    protected void lazyLoad(){
        if(!isPrepared || !isVisible || !isFirst){
            return;
        }
        initData();
        isFirst = false;
    }

    //--------------------------abstract method------------------------//

    /**
     * 在onActivityCreated中调用的方法，可以用来进行初始化操作。
     */
    protected abstract void initPrepare();

    /**
     * fragment被设置为不可见时调用
     */
    protected abstract void onInvisible();

    /**
     * 这里获取数据，刷新界面
     */
    protected abstract void initData();

    /**
     * 初始化布局，请不要把耗时操作放在这个方法里，这个方法用来提供一个
     * 基本的布局而非一个完整的布局，以免ViewPager预加载消耗大量的资源。
     */
    protected abstract View initView(LayoutInflater inflater,
                                     @Nullable ViewGroup container,
                                     @Nullable Bundle savedInstanceState);

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mTimeoutBroadcastManager.stopAll();
        super.onDestroy();
    }
}
