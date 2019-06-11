package com.luobin.ui.settingitem.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.luobin.dvr.R;
import com.luobin.ui.InterestBean;
import com.luobin.ui.SelectInterestAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SetDrawVideoAdapter extends
        RecyclerView.Adapter<SetDrawVideoAdapter.ViewHolder> {
    private Context context = null;

    ArrayList<String> list = new ArrayList<>();

    public SetDrawVideoAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        this.list = list;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //转换一个ViewHolder对象，决定了item的样式，参数1.上下文 2.XML布局资源 3.null
        View itemView = View.inflate(context, R.layout.adapter_set_draw_item, null);
        //创建一个ViewHodler对象
        ViewHolder viewHolder = new ViewHolder(itemView);
        //把ViewHolder传出去
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if (position == 1 || position == 3) {
            holder.imgSmallLeft.setVisibility(View.GONE);
            holder.imgSmallRight.setVisibility(View.VISIBLE);
        } else {
            holder.imgSmallLeft.setVisibility(View.VISIBLE);
            holder.imgSmallRight.setVisibility(View.GONE);
        }

        final String s = list.get(position);


        holder.rlitem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    //注意这里使用getTag方法获取数据

                    Log.i("aihao", "shujju>");
                    mOnItemClickListener.onItemClick(list);
                }
            }
        });

    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return null != list ? list.size() : 0;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imgBig)
        ImageView imgBig;

        @BindView(R.id.imgShow)
        ImageView imgShow;

        @BindView(R.id.imgSmallLeft)
        ImageView imgSmallLeft;

        @BindView(R.id.imgSmallRight)
        ImageView imgSmallRight;
        @BindView(R.id.rlitem)
        RelativeLayout rlitem;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }


    public interface OnRecyclerViewItemClickListener {
        /**
         * 列表点击
         */
        void onItemClick(ArrayList<String> interestBeans);
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

}
