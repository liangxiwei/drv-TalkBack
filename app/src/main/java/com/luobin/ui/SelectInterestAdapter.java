package com.luobin.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;


import com.luobin.dvr.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangjunjie
 */
public class SelectInterestAdapter extends
        RecyclerView.Adapter<SelectInterestAdapter.ViewHolder> {

    private Context context = null;
    private List<InterestBean> dataList = new ArrayList<>();


    public SelectInterestAdapter(Context context, List<InterestBean> dataList) {
        this.context = context;
        this.dataList = dataList;


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //转换一个ViewHolder对象，决定了item的样式，参数1.上下文 2.XML布局资源 3.null
        View itemView = View.inflate(context, R.layout.item_select_interset, null);
        //创建一个ViewHodler对象
        ViewHolder viewHolder = new ViewHolder(itemView);
        //把ViewHolder传出去
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        final InterestBean interestBean = dataList.get(position);

        holder.ckItem.setText(interestBean.getName());
        //将数据保存在itemView的Tag中，以便点击时进行获取
        holder.ckItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mOnItemClickListener != null) {
                    //注意这里使用getTag方法获取数据

                    dataList.get(position).setChecked(isChecked);
                    Log.i("aihao","shujju>");
                    mOnItemClickListener.onItemClick(dataList);
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return null != dataList ? dataList.size() : 0;

    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox ckItem;

        public ViewHolder(View itemView) {
            super(itemView);
            ckItem = (CheckBox) itemView.findViewById(R.id.cbItem);
        }


    }

    public interface OnRecyclerViewItemClickListener {
        /**
         * 列表点击
         *
         */
        void onItemClick(List<InterestBean> interestBeans);
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }


}
