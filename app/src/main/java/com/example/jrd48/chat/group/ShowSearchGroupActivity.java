package com.example.jrd48.chat.group;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.ImageTool;
import com.luobin.dvr.R;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.ApplyGroupProcesser;
import com.luobin.ui.BaseDialogActivity;

import java.util.ArrayList;
import java.util.List;

public class ShowSearchGroupActivity extends BaseDialogActivity {

    private GroupRequestAdapter adapter;
    private ListView lvSearchGroup;
    TeamInfoList teamInfoList;
    List<TeamInfo> teamInfo = new ArrayList<TeamInfo>();

    ImageView imageView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_group_show);
        getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        initControl();
        getMsg();
        adapter = new GroupRequestAdapter(this, teamInfo);
        lvSearchGroup.setAdapter(adapter);


    }


    /**
     * 控件初始化
     */
    private void initControl() {
        lvSearchGroup = (ListView) findViewById(R.id.lv_search_group_show);
        imageView = (ImageView) findViewById(R.id.imgClose);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        lvSearchGroup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialogGroupRequest(teamInfo.get(i));
            }
        });

    }

    /**
     * 按钮onClick事件重写
     *
     * @param view
     */
    public void back(View view) {
//        startActivity(new Intent(ShowSearchGroupActivity.this, AddGroupActivity.class));
        Intent intent = new Intent();
        intent.putExtra("data", 0);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 重写返回键功能
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // 这里重写返回键
//            startActivity(new Intent(ShowSearchGroupActivity.this, AddGroupActivity.class));
            Intent intent = new Intent();
            intent.putExtra("data", 0);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }


    /**
     * 获取Activity传来的信息
     */
    private void getMsg() {
        Intent intent = getIntent();
        teamInfoList = intent.getParcelableExtra("intent_seach_group_list");
        if (teamInfoList == null && teamInfoList.getmTeamInfo().size() <= 0) {
            return;
        }
        teamInfo = teamInfoList.getmTeamInfo();
        setIntent(null);
    }

    /**
     * 自定义DbAdapter
     */

    public class GroupRequestAdapter extends BaseAdapter {

        private List<TeamInfo> mArrayList;
        private Context context;
        private LayoutInflater layoutInflater;

        public List<TeamInfo> getList() {
            return mArrayList;
        }

        // 刷新适配器
        public void refresh(List<TeamInfo> mArrayList) {
            this.mArrayList = mArrayList;
            notifyDataSetChanged();
        }

        public GroupRequestAdapter(Context context, List<TeamInfo> mArrayList) {
            layoutInflater = LayoutInflater.from(context);
            this.context = context;
            this.mArrayList = mArrayList;
        }

        @Override
        public int getCount() {
            int count = 0;
            if (null != mArrayList) {
                count = mArrayList.size();
            }
            return count;
        }

        @Override
        public TeamInfo getItem(int position) {
            TeamInfo item = null;
            if (null != mArrayList) {
                item = mArrayList.get(position);
            }
            return item;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        int friendsIndex = 0;

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final TeamInfo groupMsg = mArrayList.get(position);

            GroupRequestAdapter.ViewHolder holder = null;

            if (convertView == null) {
                holder = new GroupRequestAdapter.ViewHolder();
                convertView = layoutInflater.inflate(R.layout.search_group_list_item, null);
                holder.tvGroupName = (TextView) convertView.findViewById(R.id.tv_search_group_name);
                holder.tvGroupDes = (TextView) convertView.findViewById(R.id.tv_search_group_describle);
                holder.mShowImage = (ImageView) convertView.findViewById(R.id.request_in_image);
                convertView.setTag(holder);

            } else {
                holder = (GroupRequestAdapter.ViewHolder) convertView.getTag();
            }
            if (groupMsg == null) {
                return null;
            }
            holder.tvGroupName.setText(groupMsg.getTeamName());
//			Bitmap bitmap = getUserFace(friendsMsg);
//			if (bitmap == null) {
//				holder.mShowImage.setImageResource(R.drawable.default_useravatar);
//			} else {
//				holder.mShowImage.setImageBitmap(bitmap);
//			}
            holder.tvGroupDes.setText(groupMsg.getTeamDesc());
            return convertView;
        }

        public class ViewHolder {
            // public TextView tvNumber;
            public TextView tvGroupName;
            public TextView tvGroupDes;
            public ImageView mShowImage;
        }

    }

    /**
     * 获取好友头像
     *
     * @param param
     * @return
     */
    public Bitmap getUserFace(AppliedFriends param) {
        Bitmap bmp = null;
        try {
            if (param == null || param.getUserPic() == null || param.getUserPic().length <= 0) {
                Log.e("UserFace", "UserFace is null");
                bmp = BitmapFactory.decodeResource(ShowSearchGroupActivity.this.getResources(), R.drawable.default_useravatar);
            } else {
                bmp = BitmapFactory.decodeByteArray(param.getUserPic(), 0, param.getUserPic().length);
            }
            Bitmap bitmap1 = Bitmap.createScaledBitmap(bmp, 96, 96,
                    false); // 将图片缩小
            ImageTool ll = new ImageTool(); // 图片头像变成圆型
            bmp = ll.toRoundBitmap(bitmap1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;
    }


    /**
     * 添加组提示框
     */
    private void dialogGroupRequest(final TeamInfo teamInfo) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(ShowSearchGroupActivity.this); // 先得到构造器

            builder.setMessage("请求加入" + teamInfo.getTeamName() + "群组").setTitle("提示：").setPositiveButton("确定", new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    addGroupRequest(teamInfo.getTeamID());
                }
            }).setNegativeButton("取消", new AlertDialog.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();

//			String msg = "请求加入该群，谢谢。";
//		    String	userNameMe = "12";
//			String	userName = "3";
//			AddFriendPrompt.dialogFriendsRequest(this,msg, userName, userNameMe, new AddFriendPromptListener() {
//
//				@Override
//				public void onOk(String remark, String msg) {
//					addGroupRequest(remark, msg);
//
//				}
//			});
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 加组网络请求
     *
     * @param id
     */
    private void addGroupRequest(long id) {
        ProtoMessage.ApplyTeam.Builder builder = ProtoMessage.ApplyTeam.newBuilder();
        builder.setTeamID(id);
//		builder.sett
//		builder.setApplyRemark(remark);
        MyService.start(ShowSearchGroupActivity.this, ProtoMessage.Cmd.cmdApplyTeam.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(ApplyGroupProcesser.ACTION);
        final TimeoutBroadcast b = new TimeoutBroadcast(ShowSearchGroupActivity.this, filter, getBroadcastManager());

        b.startReceiver(TimeoutBroadcast.TIME_OUT_IIME, new ITimeoutBroadcast() {

            @Override
            public void onTimeout() {
                ToastR.setToast(ShowSearchGroupActivity.this, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {
                    successBack();
                } else {
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }

    public void successBack() {
        ToastR.setToast(ShowSearchGroupActivity.this, "加群请求成功，等待对方回应");
        searchSuccess();
    }

    public void fail(int code) {
        new ResponseErrorProcesser(ShowSearchGroupActivity.this, code);
    }

    /**
     * 添加成功关闭该Activity
     */
    protected void searchSuccess() {
        Intent intent = new Intent();
        intent.putExtra("data", 1);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}