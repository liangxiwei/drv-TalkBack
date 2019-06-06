package com.example.jrd48.chat.search;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jrd48.chat.BaseActivity;
import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.MainActivity;
import com.luobin.dvr.R;
import com.example.jrd48.chat.SQLite.TeamMemberHelper;
import com.example.jrd48.chat.friend.AppliedFriends;
import com.example.jrd48.chat.wiget.PuzzleView;
import com.example.jrd48.service.proto_gen.ProtoMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/2/7.
 */

public class SearchListItemAdapter extends BaseAdapter {
    protected List<SearchFriends> list;
    protected String result; //需要匹配字符串
    protected Context context;
    protected LayoutInflater inflater = null;

    public SearchListItemAdapter(Context context, List<SearchFriends> list) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    public void refresh(List<SearchFriends> list, String result) {
        this.list = list;
        this.result = result;
        notifyDataSetChanged();
    }

    public int getCount() {
        return list.size();
    }

    @Override
    public SearchFriends getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean isEnabled(int position) {
        if(getItem(position).getSearchType().equals(SearchActivity.linkTitle)){
            return false ;  // 表示该行不可以点击
        }
        return super.isEnabled(position);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        SearchFriends af = list.get(i);
        viewHolder = new ViewHolder();
        if (af.getSearchType().equals(SearchActivity.linkSearch)) {
            view = inflater.inflate(R.layout.search_friend_and_team, null);
            viewHolder.searchMsg = (TextView) view.findViewById(R.id.tv_search_friend);
            viewHolder.searchMsg.setText(af.getUserName());
            viewHolder.searchTemp = (TextView) view.findViewById(R.id.tv_temp);
            if (af.getType() != null && af.getType().equals(SearchActivity.linkSearch)) {
                viewHolder.searchTemp.setText("在线搜索群组:");
            } else {
                viewHolder.searchTemp.setText("在线搜索好友:");
            }
        } else if (af.getSearchType().equals(SearchActivity.linkTitle)) {
            view = LayoutInflater.from(context).inflate(R.layout.index, null);
//            view.setBackgroundColor(context.getResources().getColor(R.color.search_bg));
            view.setFocusable(false);
            viewHolder.indexTv = (TextView) view.findViewById(R.id.indexTv);
            viewHolder.indexTv.setText(af.getUserName());
        } else {
            view = inflater.inflate(R.layout.search_list_item, null);
            viewHolder.TeamAvatar = (PuzzleView) view.findViewById(R.id.search_team_image);
            viewHolder.linkmanAvatar = (ImageView) view.findViewById(R.id.search_image);
            viewHolder.userName = (TextView) view.findViewById(R.id.search_name);
            viewHolder.mobile = (TextView) view.findViewById(R.id.search_phone);
            viewHolder.tvPromt = (TextView) view.findViewById(R.id.search_promt);
            viewHolder.searchLeftBrackets = (TextView) view.findViewById(R.id.search_left_brackets);
            viewHolder.searchRightBrackets = (TextView) view.findViewById(R.id.search_right_brackets);
            viewHolder.searchMemberName = (TextView) view.findViewById(R.id.search_member_name);

            if (af.getSearchType().equals(SearchActivity.linkMan)) {
                //联系人
                viewHolder.TeamAvatar.setVisibility(View.GONE);
                viewHolder.searchMemberName.setVisibility(View.GONE);
                if (af.getPhoneNum() != null) {
                    Bitmap bitmap = GlobalImg.getImage(context, af.getPhoneNum());
                    if (bitmap == null) {
                        if (af.getUserSex() == ProtoMessage.Sex.female_VALUE) {
                            viewHolder.linkmanAvatar.setImageResource(R.drawable.woman);
                        } else {
                            viewHolder.linkmanAvatar.setImageResource(R.drawable.man);
                        }
                    } else {
                        viewHolder.linkmanAvatar.setImageBitmap(bitmap);
                    }
                }
                if (af.getType().equals(SearchActivity.strName)) {
                    //名字搜索
                    String userName = af.getUserName();
                    int index = userName.indexOf(result);
                    SpannableString span = new SpannableString(userName);
                    if (index >= 0) {
                        span.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.textColor)), index, index + result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        span.setSpan(new StyleSpan(Typeface.BOLD), index, index + result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        viewHolder.userName.setText(span);
                    } else {
                        if (af.getKeyword() != null && af.getKeyword().length() > 0) {
                            index = userName.indexOf(af.getKeyword());
                            if (index >= 0) {
                                span.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.textColor)), index, index + af.getKeyword().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                span.setSpan(new StyleSpan(Typeface.BOLD), index, index + af.getKeyword().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                viewHolder.userName.setText(span);
                            } else {
                                viewHolder.userName.setText(userName);
                            }
                        } else {
                            viewHolder.userName.setText(userName);
                        }

                    }
                    viewHolder.mobile.setText(af.getPhoneNum());
                } else if (af.getType().equals(SearchActivity.strPhone)) {
                    //电话搜索
                    String userPhone = af.getPhoneNum();
                    int index = userPhone.indexOf(result);
                    SpannableString span = new SpannableString(userPhone);
                    span.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.textColor)), index, index + result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    span.setSpan(new StyleSpan(Typeface.BOLD), index, index + result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    viewHolder.mobile.setText(span);
                    viewHolder.userName.setText(af.getUserName());
                }
            } else if (af.getSearchType().equals(SearchActivity.linkTeam)) {
                //群聊
                viewHolder.linkmanAvatar.setVisibility(View.GONE);
//                viewHolder.linkmanAvatar.setImageResource(R.drawable.group);
                setTeamImageView(viewHolder.TeamAvatar, af);
                if (af.getUserName() == null || af.getUserName().length() <= 0) {
                    viewHolder.mobile.setVisibility(View.GONE);
                    viewHolder.searchMemberName.setVisibility(View.GONE);
                    String teamName = af.getTeamName();
                    int index = teamName.indexOf(result);
                    SpannableString span = new SpannableString(teamName);
                    if (index >= 0) {
                        span.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.textColor)), index, index + result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        span.setSpan(new StyleSpan(Typeface.BOLD), index, index + result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        viewHolder.userName.setText(span);
                    } else {
                        if (af.getKeyword() != null && af.getKeyword().length() > 0) {
                            index = teamName.indexOf(af.getKeyword());
                            if (index >= 0) {
                                span.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.textColor)), index, index + af.getKeyword().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                span.setSpan(new StyleSpan(Typeface.BOLD), index, index + af.getKeyword().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                viewHolder.userName.setText(span);
                            } else {
                                viewHolder.userName.setText(teamName);
                            }
                        } else {
                            viewHolder.userName.setText(teamName);
                        }
                    }
                } else if (af.getUserName().length() > 0) {
                    viewHolder.userName.setText(af.getTeamName());
                    viewHolder.tvPromt.setVisibility(View.VISIBLE);
                    viewHolder.searchLeftBrackets.setVisibility(View.VISIBLE);
                    viewHolder.searchRightBrackets.setVisibility(View.VISIBLE);
                    String name = af.getUserName();
                    int index = name.indexOf(result);
                    SpannableString span = new SpannableString(name);

                    if (af.getType().equals(SearchActivity.strName)) {
                        viewHolder.mobile.setText(af.getPhoneNum());
                        if (index >= 0) {
                            span.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.textColor)), index, index + result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            span.setSpan(new StyleSpan(Typeface.BOLD), index, index + result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            viewHolder.searchMemberName.setText(span);
                        } else {
                            if (af.getKeyword() != null && af.getKeyword().length() > 0) {
                                index = name.indexOf(af.getKeyword());
                                if (index >= 0) {
                                    span.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.textColor)), index, index + af.getKeyword().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    span.setSpan(new StyleSpan(Typeface.BOLD), index, index + af.getKeyword().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    viewHolder.searchMemberName.setText(span);
                                } else {
                                    viewHolder.searchMemberName.setText(name);
                                }
                            } else {
                                viewHolder.searchMemberName.setText(name);
                            }
                        }
                    } else {
                        String phone = af.getPhoneNum();
                        SpannableString ss = new SpannableString(phone);
                        int t = phone.indexOf(result);
                        viewHolder.searchMemberName.setText(af.getUserName());
                        if (t >= 0) {
                            /*
                             SpannableStringBuilder style = new SpannableStringBuilder(friendsMsg.getUserName()+"邀请您加入" + friendsMsg.getTeamName() + "群组");
                        style.setSpan(new ForegroundColorSpan(Color.BLUE), 0, friendsMsg.getUserName().length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE); //设置指定位置文字的
                        style.setSpan(new StyleSpan(Typeface.BOLD), 0, friendsMsg.getUserName().length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE); //设置指定位置文字的背景颜色
                             */
                            ss.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.textColor)), t, t + result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            ss.setSpan(new StyleSpan(Typeface.BOLD), t, t + result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            viewHolder.mobile.setText(ss);
                        } else {
                            viewHolder.mobile.setText(phone);
                        }
                    }
                }
            }
        }
        return view;
    }

    class ViewHolder {
        public TextView indexTv;
        public ImageView linkmanAvatar;
        public PuzzleView TeamAvatar;
        public TextView userName;
        public TextView mobile;
        public TextView tvPromt;
        public TextView searchLeftBrackets;
        public TextView searchRightBrackets;
        public TextView searchMemberName;
        public TextView searchMsg;
        public TextView searchTemp;
//        public RelativeLayout userView;
    }

    public void setTeamImageView(PuzzleView TeamAvatar, SearchFriends af) {
        TeamMemberHelper teamMemberHelper = new TeamMemberHelper(context, af.getTeamID() + "TeamMember.dp", null);
        SQLiteDatabase db = teamMemberHelper.getWritableDatabase();
        Cursor cursor = db.query("LinkmanMember", null, null, null, null, null, null);
        ArrayList<Bitmap> mBmps = new ArrayList<Bitmap>();
        int i = 0;
        if (cursor.moveToLast()) {
            String phone;
            Bitmap head;
            do {
                if (i >= 9) {
                    break;
                }
                phone = cursor.getString(cursor.getColumnIndex("user_phone"));
                head = GlobalImg.getImage(context, phone);
                if (head != null) {
                    mBmps.add(head);
                } else {
                    mBmps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.man));
                }
                ++i;
            } while (cursor.moveToPrevious());
            try {
                TeamAvatar.setBackgroundResource(R.color.teamHeadBackColor);
                TeamAvatar.setImageBitmaps(mBmps);
            } catch (Exception e) {
                e.printStackTrace();
                TeamAvatar.setBackgroundResource(R.drawable.group);
            }
        } else {
            mBmps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.nocolor));
            TeamAvatar.setImageBitmaps(mBmps);
            TeamAvatar.setBackgroundResource(R.drawable.group);
        }
        cursor.close();
        db.close();
    }

}
