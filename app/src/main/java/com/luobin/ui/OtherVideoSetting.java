package com.luobin.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jrd48.GlobalStatus;
import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.SettingRW;
import com.example.jrd48.chat.crash.MyApplication;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.service.notify.NotifyManager;
import com.luobin.dvr.DvrService;
import com.luobin.dvr.R;
import com.luobin.utils.VideoRoadUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import me.lake.librestreaming.client.RESClient;

/**
 * Created by Administrator on 2017/8/17.
 */

public class OtherVideoSetting extends BaseActivity {
    private Context context;
    private LinearLayout replyLayout;
    private TextView reply;
    private SettingRW mSettings;
    private ListView listView;
    private RequstAdapter adapter;
    private String[] road_reply = new String[]{"拒绝", "询问", "同意"};
    private AlertDialog simplelistdialog;
    private String dialogPhone;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

            checkDialog();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.video_setting);
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
//        toolbar.setNavigationIcon(R.drawable.btn_back);//设置Navigatiicon 图标
        toolbar.setTitle(null);
        ((TextView) toolbar.findViewById(R.id.custom_title)).setText("路况分享");
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });

        mSettings = new SettingRW(this);
        mSettings.load();
        registerReceiver(receiver, new IntentFilter(GlobalStatus.REQUEST_CALL_ACTION));
        replyLayout = (LinearLayout) findViewById(R.id.video_reply_layout);
        reply = (TextView) findViewById(R.id.video_reply);
        listView = (ListView) findViewById(R.id.requestList);
        replyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("路况分享");
                builder.setItems(road_reply, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSettings.setRoadRequestReply(which);
                        mSettings.save();
                        reply.setText(road_reply[which]);
                    }
                });
                AlertDialog simplelistdialog = builder.create();
                simplelistdialog.show();
            }
        });
        replyLayout.setVisibility(View.GONE);
        reply.setText(road_reply[mSettings.getRoadRequestReply()]);
        adapter = new RequstAdapter();
        listView.setAdapter(adapter);
        listView.setSelector(R.drawable.submenu_default);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = (String) adapter.getItem(position);
                if (GlobalStatus.getCurViewPhone() != null && GlobalStatus.getCurViewPhone().equals(name)) {
                    videoDialog(true, name);
                } else {
                    videoDialog(false, name);
                }
            }
        });
        listView.setEmptyView(findViewById(R.id.empty_list_view));
    }

    public void videoDialog(final boolean isCurView, final String phone) {
        String title;
        String message;
        String showPhone = DBManagerFriendsList.getAFriendNickName(MyApplication.getContext(),phone);
        if(Pattern.matches("\\d{11}",showPhone)){
            showPhone = showPhone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
        }
        if (isCurView) {
            title = "关闭路况分享";
            message = "请确定要关闭对用户 " + showPhone + " 的路况分享";
        } else if(GlobalStatus.getChatRoomMsg() != null){
            title = "当前正在对讲";
            message = "请确定要结束对讲并开启对用户 " + showPhone + " 的路况分享";
        } else {
            title = "开启路况分享";
            message = "请确定要开启对用户 " + showPhone + " 的路况分享\n(注意:一次只能给一名用户分享路况)";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isCurView) {
                    DvrService.start(MyApplication.getContext(), RESClient.ACTION_STOP_RTMP, null);
                    GlobalStatus.setCurViewPhone(null);
                } else if(GlobalStatus.getChatRoomMsg() != null){
                    NotifyManager.getInstance().endCall(context);
                    VideoRoadUtils.AcceptLiveCall(context, phone);
                } else {
                    VideoRoadUtils.AcceptLiveCall(context, phone);
                }

            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        if (simplelistdialog != null && simplelistdialog.isShowing()) {
            simplelistdialog.dismiss();
        }

        simplelistdialog = builder.create();
        simplelistdialog.show();
        dialogPhone = phone;
    }

    public void checkDialog() {

        if (dialogPhone != null && simplelistdialog != null && simplelistdialog.isShowing()
                && !dialogPhone.equals(GlobalStatus.getCurViewPhone()) && !adapter.getViewList().contains(dialogPhone)) {
            simplelistdialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    public class RequstAdapter extends BaseAdapter {
        private List<String> viewList;

        public RequstAdapter() {
            viewList = new ArrayList<String>(GlobalStatus.getViewRoadPhones().keySet());
        }

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public Object getItem(int position) {
            return viewList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder viewHolder;
            if (convertView == null) {
                view = LayoutInflater.from(context).inflate(R.layout.view_request_item, null);
                viewHolder = new ViewHolder();
                viewHolder.requestName = (TextView) view.findViewById(R.id.name);
                viewHolder.requestStatus = (TextView) view.findViewById(R.id.status);
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();
            }

            String name = viewList.get(position);
            if (name.equals(GlobalStatus.getCurViewPhone())) {
                Log.v("wsDvr", "viewHolder.requestStatus.setText(\"分享中\");");
                viewHolder.requestStatus.setText("分享中");
            } else {
                viewHolder.requestStatus.setText("");
            }
            String showPhone = DBManagerFriendsList.getAFriendNickName(MyApplication.getContext(),name);
            if(Pattern.matches("\\d{11}",showPhone)){
                showPhone = showPhone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
            }
            viewHolder.requestName.setText(showPhone + " 请求查看路况");
            return view;
        }

        @Override
        public void notifyDataSetChanged() {
            viewList = new ArrayList<String>(GlobalStatus.getViewRoadPhones().keySet());
            super.notifyDataSetChanged();
        }


        public List<String> getViewList() {
            return viewList;
        }
    }


    private class ViewHolder {
        TextView requestName;
        TextView requestStatus;
    }


}
