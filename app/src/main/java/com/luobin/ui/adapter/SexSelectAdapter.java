package com.luobin.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.luobin.dvr.R;
import com.luobin.widget.SexSelectDialog;

import java.util.List;

public class SexSelectAdapter extends BaseAdapter {

    List<SexSelectDialog.SexSelectBean> list;
    Context context;

    public SexSelectAdapter(List<SexSelectDialog.SexSelectBean> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder;
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.item_sex_select,null);
            viewHolder = new ViewHolder();

            viewHolder.name = (TextView) view.findViewById(R.id.name);
            viewHolder.radioButton = (RadioButton) view.findViewById(R.id.radiobutton);
            view.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) view.getTag();
        }

        SexSelectDialog.SexSelectBean sexSelectBean = list.get(position);

        viewHolder.name.setText(sexSelectBean.getName());
        viewHolder.radioButton.setSelected(sexSelectBean.isSelect());

        return view;
    }


    class ViewHolder{
        TextView name;
        RadioButton radioButton;
    }

}
