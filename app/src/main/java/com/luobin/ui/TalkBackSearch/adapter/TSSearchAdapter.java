package com.luobin.ui.TalkBackSearch.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jrd48.chat.CircleImageView;
import com.example.jrd48.chat.GlobalImg;
import com.example.jrd48.chat.SQLite.TeamMemberHelper;
import com.example.jrd48.chat.search.SearchFriends;
import com.example.jrd48.chat.wiget.PuzzleView;
import com.example.jrd48.service.proto_gen.ProtoMessage;
import com.luobin.dvr.R;
import com.luobin.ui.TalkBackSearch.TalkbackSearchActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author wangjunjie
 */
public class TSSearchAdapter extends BaseAdapter {

    protected List<SearchFriends> list;
    protected String result; //需要匹配字符串
    protected Context context;
    protected LayoutInflater inflater = null;


    public TSSearchAdapter(Context context, List<SearchFriends> list) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    public void refresh(List<SearchFriends> list, String result) {
        this.list = list;
        this.result = result;
        notifyDataSetChanged();
    }


    @Override
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        SearchFriends af = list.get(i);
        ViewHolder viewHolder = null;
        ViewHolderNew viewHolderNew = null;
        ViewHolderNewTwo viewHolderNewTwo =null;
//        if (view == null) {
//            view = LayoutInflater.from(context).inflate(R.layout.adapter_search_item, null);
//            viewHolder = new ViewHolder(view);
//            view.setTag(viewHolder);
//        } else {
//            viewHolder = (ViewHolder) view.getTag();
//        }

        if (af.getSearchType().equals(TalkbackSearchActivity.linkSearch)) {
            view = inflater.inflate(R.layout.search_friend_and_team, null);
            viewHolderNewTwo =  new ViewHolderNewTwo(view);
            viewHolderNewTwo.searchMsg.setText(af.getUserName());
            if (af.getType() != null && af.getType().equals(TalkbackSearchActivity.linkSearch)) {
                viewHolderNewTwo.searchTemp.setText("在线搜索群组:");
            } else {
                viewHolderNewTwo.searchTemp.setText("在线搜索好友:");
            }
        }else if (af.getSearchType().equals(TalkbackSearchActivity.linkTitle)) {
            view = LayoutInflater.from(context).inflate(R.layout.index, null);
//            view.setBackgroundColor(context.getResources().getColor(R.color.search_bg));
            view.setFocusable(false);
            viewHolder =  new ViewHolder(view);
            view.setTag(viewHolder);
            viewHolder.indexTv.setText(af.getUserName());

        } else {
            view = inflater.inflate(R.layout.search_list_item, null);
            viewHolderNew =  new ViewHolderNew(view);

            view.setTag(viewHolder);
            if (af.getSearchType().equals(TalkbackSearchActivity.linkMan)) {
                //联系人
                viewHolderNew.TeamAvatar.setVisibility(View.GONE);
                viewHolderNew.searchMemberName.setVisibility(View.GONE);
                if (af.getPhoneNum() != null) {
                    Bitmap bitmap = GlobalImg.getImage(context, af.getPhoneNum());
                    if (bitmap == null) {
                        if (af.getUserSex() == ProtoMessage.Sex.female_VALUE) {
                            viewHolderNew.linkmanAvatar.setImageResource(R.drawable.woman);
                        } else {
                            viewHolderNew.linkmanAvatar.setImageResource(R.drawable.man);
                        }
                    } else {
                        viewHolderNew.linkmanAvatar.setImageBitmap(bitmap);
                    }
                }
                if (af.getType().equals(TalkbackSearchActivity.strName)) {
                    //名字搜索
                    String userName = af.getUserName();
                    int index = userName.indexOf(result);
                    SpannableString span = new SpannableString(userName);
                    if (index >= 0) {
                        span.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.textColor)), index, index + result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        span.setSpan(new StyleSpan(Typeface.BOLD), index, index + result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        viewHolderNew.userName.setText(span);
                    } else {
                        if (af.getKeyword() != null && af.getKeyword().length() > 0) {
                            index = userName.indexOf(af.getKeyword());
                            if (index >= 0) {
                                span.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.textColor)), index, index + af.getKeyword().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                span.setSpan(new StyleSpan(Typeface.BOLD), index, index + af.getKeyword().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                viewHolderNew.userName.setText(span);
                            } else {
                                viewHolderNew.userName.setText(userName);
                            }
                        } else {
                            viewHolderNew.userName.setText(userName);
                        }

                    }
                    viewHolderNew.mobile.setText(af.getPhoneNum());
                } else if (af.getType().equals(TalkbackSearchActivity.strPhone)) {
                    //电话搜索
                    String userPhone = af.getPhoneNum();
                    int index = userPhone.indexOf(result);
                    SpannableString span = new SpannableString(userPhone);
                    span.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.textColor)), index, index + result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    span.setSpan(new StyleSpan(Typeface.BOLD), index, index + result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    viewHolderNew.mobile.setText(span);
                    viewHolderNew.userName.setText(af.getUserName());
                }
            } else if (af.getSearchType().equals(TalkbackSearchActivity.linkTeam)) {
                //群聊
                viewHolderNew.linkmanAvatar.setVisibility(View.GONE);
//                viewHolder.linkmanAvatar.setImageResource(R.drawable.group);
                setTeamImageView(viewHolderNew.TeamAvatar, af);
                if (af.getUserName() == null || af.getUserName().length() <= 0) {
                    viewHolderNew.mobile.setVisibility(View.GONE);
                    viewHolderNew.searchMemberName.setVisibility(View.GONE);
                    String teamName = af.getTeamName();
                    int index = teamName.indexOf(result);
                    SpannableString span = new SpannableString(teamName);
                    if (index >= 0) {
                        span.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.white)), index, index + result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        span.setSpan(new StyleSpan(Typeface.BOLD), index, index + result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        viewHolderNew.userName.setText(span);
                    } else {
                        if (af.getKeyword() != null && af.getKeyword().length() > 0) {
                            index = teamName.indexOf(af.getKeyword());
                            if (index >= 0) {
                                span.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.white)), index, index + af.getKeyword().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                span.setSpan(new StyleSpan(Typeface.BOLD), index, index + af.getKeyword().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                viewHolderNew.userName.setText(span);
                            } else {
                                viewHolderNew.userName.setText(teamName);
                            }
                        } else {
                            viewHolderNew.userName.setText(teamName);
                        }
                    }
                } else if (af.getUserName().length() > 0) {
                    viewHolderNew.userName.setText(af.getTeamName());
                    viewHolderNew.tvPromt.setVisibility(View.VISIBLE);
                    viewHolderNew.searchLeftBrackets.setVisibility(View.VISIBLE);
                    viewHolderNew.searchRightBrackets.setVisibility(View.VISIBLE);
                    String name = af.getUserName();
                    int index = name.indexOf(result);
                    SpannableString span = new SpannableString(name);

                    if (af.getType().equals(TalkbackSearchActivity.strName)) {
                        viewHolderNew.mobile.setText(af.getPhoneNum());
                        if (index >= 0) {
                            span.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.white)), index, index + result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            span.setSpan(new StyleSpan(Typeface.BOLD), index, index + result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            viewHolderNew.searchMemberName.setText(span);
                        } else {
                            if (af.getKeyword() != null && af.getKeyword().length() > 0) {
                                index = name.indexOf(af.getKeyword());
                                if (index >= 0) {
                                    span.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.white)), index, index + af.getKeyword().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    span.setSpan(new StyleSpan(Typeface.BOLD), index, index + af.getKeyword().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    viewHolderNew.searchMemberName.setText(span);
                                } else {
                                    viewHolderNew.searchMemberName.setText(name);
                                }
                            } else {
                                viewHolderNew.searchMemberName.setText(name);
                            }
                        }
                    } else {
                        String phone = af.getPhoneNum();
                        SpannableString ss = new SpannableString(phone);
                        int t = phone.indexOf(result);
                        viewHolderNew.searchMemberName.setText(af.getUserName());
                        if (t >= 0) {
                            /*
                             SpannableStringBuilder style = new SpannableStringBuilder(friendsMsg.getUserName()+"邀请您加入" + friendsMsg.getTeamName() + "群组");
                        style.setSpan(new ForegroundColorSpan(Color.BLUE), 0, friendsMsg.getUserName().length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE); //设置指定位置文字的
                        style.setSpan(new StyleSpan(Typeface.BOLD), 0, friendsMsg.getUserName().length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE); //设置指定位置文字的背景颜色
                             */
                            ss.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.white)), t, t + result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            ss.setSpan(new StyleSpan(Typeface.BOLD), t, t + result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            viewHolderNew.mobile.setText(ss);
                        } else {
                            viewHolderNew.mobile.setText(phone);
                        }
                    }
                }
            }
        }


        return view;
    }

    @Override
    public boolean isEnabled(int position) {
        if (getItem(position).getSearchType().equals(TalkbackSearchActivity.linkTitle)) {
            return false;  // 表示该行不可以点击
        }
        return super.isEnabled(position);
    }

    static class ViewHolder {

        @BindView(R.id.indexTv)
        TextView indexTv;


        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    static class ViewHolderNewTwo {
        @BindView(R.id.tv_search_friend)
        TextView searchMsg;
        @BindView(R.id.tv_temp)
        TextView searchTemp;

        ViewHolderNewTwo(View view) {
            ButterKnife.bind(this, view);
        }
    }

    static class ViewHolderNew {
        @BindView(R.id.search_image)
        CircleImageView linkmanAvatar;
        @BindView(R.id.search_team_image)
        PuzzleView TeamAvatar;
        @BindView(R.id.search_name)
        TextView userName;
        @BindView(R.id.search_promt)
        TextView tvPromt;
        @BindView(R.id.search_member_name)
        TextView searchMemberName;
        @BindView(R.id.search_left_brackets)
        TextView searchLeftBrackets;
        @BindView(R.id.search_phone)
        TextView mobile;
        @BindView(R.id.search_right_brackets)
        TextView searchRightBrackets;


        ViewHolderNew(View view) {
            ButterKnife.bind(this, view);
        }
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
