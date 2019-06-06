package com.luobin.ui;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.TabFragmentLinkGroup;
import com.example.jrd48.chat.TabFragmentLinkmans;
import com.luobin.dvr.R;

/**
 * Created by Administrator on 2017/8/8.
 */

public class ContactsActivity extends BaseActivity {
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_fragment_dvr);
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
//        toolbar.setNavigationIcon(R.drawable.btn_back);//设置Navigatiicon 图标
        toolbar.setTitle(null);
        ((TextView)toolbar.findViewById(R.id.custom_title)).setText(R.string.contacts_title);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment, new TabFragmentLinkmans());
        fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        GlobalStatus.setIsFirstPause(false);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GlobalStatus.setIsFirstPause(true);
        sendBroadcast(new Intent(VideoOrVoiceDialog.DISMISS_ACTION));
    }
}
