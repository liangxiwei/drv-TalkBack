package com.luobin.ui;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.TabFragmentLinkmans;
import com.example.jrd48.chat.ToastR;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.friend.DBHelperFriendsList;
import com.example.jrd48.chat.friend.DBManagerFriendsList;
import com.example.jrd48.chat.group.InviteJoinGroupActivity;
import com.example.jrd48.chat.group.cache.DBTableName;
import com.example.jrd48.chat.search.PinyinComparator;
import com.example.jrd48.service.ConnUtil;
import com.example.jrd48.service.ITimeoutBroadcast;
import com.example.jrd48.service.MyService;
import com.example.jrd48.service.TimeoutBroadcast;
import com.example.jrd48.service.protocol.ResponseErrorProcesser;
import com.example.jrd48.service.protocol.root.ApplyGroupProcesser;
import com.example.jrd48.service.protocol.root.FriendsListProcesser;
import com.luobin.dvr.R;
import com.luobin.tool.OnlineSetTool;
import com.luobin.ui.adapter.SelectMemberToDeleteAdapter;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.example.jrd48.chat.TeamMemberInfo;
import java.util.Iterator;

import com.example.jrd48.service.protocol.root.DeleteTeamMemberProcesser;

import com.example.jrd48.chat.SQLite.TeamMemberHelper;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class SelectMemberToDeleteActivity extends BaseDialogActivity {

    ListView selectMemberListView;
    Button ok;
    Context context;
    //List<AppliedFriends> selectMemberList = new ArrayList<>();
    SelectMemberToDeleteAdapter adapter;
    ImageView imgClose;
    long teamID;
    public static final int APPLYTEAM = 100102;


	//TextView tvLoadingMember = null;
	Button buttonDelete = null;

	private List<TeamMemberInfo> curAllTeamMemberInfos;
	//List<String> selectMemberPhoneList = new ArrayList<String>();
	int myType = 0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		Log.d("rs", "SelectMemberToDeleteActivity->start");
        setContentView(R.layout.activity_select_member_to_delete);
        getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        context = this;
        teamID = getIntent().getLongExtra("teamID",-1);
		myType = getIntent().getExtras().getInt("type");
		curAllTeamMemberInfos = getIntent().getParcelableArrayListExtra("curMemberList");//rs added for LBCJW-68

        if (teamID == -1){
            ToastR.setToast(this,"群ID错误");
            finish();
        }

		Log.d("rs", "teamID->" + teamID+", myType:"+myType);

        initView();
    }


    private void initView(){
    	Log.d("rs","initView start");
        selectMemberListView = (ListView) findViewById(R.id.member_select_listview);
		//PinyinComparator comparator = new PinyinComparator();
        //Collections.sort(curAllTeamMemberInfos, comparator);
		try{		
			if (adapter == null){
	        	adapter = new SelectMemberToDeleteAdapter(curAllTeamMemberInfos,context);
	            selectMemberListView.setAdapter(adapter);
			Log.d("rs","initView set list adapter");
            }else{
                adapter.setData(curAllTeamMemberInfos);
                adapter.notifyDataSetChanged();
            }
      	} catch (Exception e) {
            e.printStackTrace();
        }


		
        imgClose = (ImageView) findViewById(R.id.imgClose);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        buttonDelete = (Button) findViewById(R.id.btn_ok);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter != null){
                    //applyMember(adapter.getSelect());
                    deleteTeamMember(adapter.getSelect());
                }
            }
        });

		Log.d("rs","initView end");
        //tvLoadingMember = (TextView)findViewById(R.id.loading_member);
    }


    /**
     * 删除群成员
     *
     * @param tm
     */
    public void deleteTeamMember(final TeamMemberInfo tm) {
        if (tm.getRole() == ProtoMessage.TeamRole.Owner_VALUE) {
        	ToastR.setToast(context, "不能删除群主");
			return;
        } else if (myType == ProtoMessage.TeamRole.Manager_VALUE &&
                        tm.getRole() == ProtoMessage.TeamRole.Manager_VALUE) {
            ToastR.setToast(context, "管理员不能删除管理员");
			return;
        }
						
        ProtoMessage.AcceptTeam.Builder builder = ProtoMessage.AcceptTeam.newBuilder();
        builder.setTeamID(teamID);
        builder.setPhoneNum(tm.getUserPhone());
        MyService.start(context, ProtoMessage.Cmd.cmdDeleteTeamMember.getNumber(), builder.build());
        IntentFilter filter = new IntentFilter();
        filter.addAction(DeleteTeamMemberProcesser.ACTION);
        new TimeoutBroadcast(context, filter, getBroadcastManager()).startReceiver(10, new ITimeoutBroadcast() {
            @Override
            public void onTimeout() {
                ToastR.setToast(context, "连接超时");
            }

            @Override
            public void onGot(Intent i) {
                if (i.getIntExtra("error_code", -1) ==
                        ProtoMessage.ErrorCode.OK.getNumber()) {

                    //refreshLocalData(tm);
                    ToastR.setToast(context, "删除成员成功");
					updateDeleteMember(teamID, tm.getUserPhone());
						
					Intent intent = new Intent();
		            //Bundle bundle = new Bundle();
		            //bundle.putParcelable("del_team_info", tm);
		            //bundle.putString("del_member_phone", tm.getUserPhone());
		            intent.putExtra("del_member_phone", tm.getUserPhone());
		            setResult(RESULT_OK, intent);
					finish();
                } else {
					ToastR.setToast(context, "删除成员失败");
                    fail(i.getIntExtra("error_code", -1));
                }
            }
        });
    }


    public void fail(int i) {
        new ResponseErrorProcesser(context, i);
    }

	public void updateDeleteMember(long tID, String userPhoneNum) {
			Log.d("rs", "updateDeleteMember:"+userPhoneNum);
	        try {
	            TeamMemberHelper teamMemberHelper = new TeamMemberHelper(context, tID + "TeamMember.dp", null);
	            SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
	            db.delete("LinkmanMember", "user_phone == ?", new String[]{userPhoneNum});
	            db.close();
	        } catch (Exception e) {
	            e.printStackTrace();
				Log.d("rs", "updateDeleteMember->exception:"+e.toString());
	        }
    	}
}
