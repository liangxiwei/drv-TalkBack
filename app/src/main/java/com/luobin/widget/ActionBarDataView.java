package com.luobin.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.luobin.dvr.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ActionBarDataView extends FrameLayout {
    private final String TAG = "ActionBarDataView";
    private TextView mTime1,mTime2,mTime3,mTime4,mTime5;
    private TextView mDate,mWeek;
    private Context mContext;

    int second;

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            init();
            handler.sendMessageDelayed(handler.obtainMessage(), 60 * 1000);
        }
    };

    public ActionBarDataView(Context context) {
        super(context);
        init(context);
    }

    public ActionBarDataView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ActionBarDataView(Context context,AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public ActionBarDataView( Context context,  AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context){
        mContext = context;
        View view = LayoutInflater.from(getContext()).inflate(
                R.layout.actionbar_data, this);

        mTime1 = (TextView) view.findViewById(R.id.time1);
        mTime2 = (TextView) view.findViewById(R.id.time2);
        mTime3 = (TextView) view.findViewById(R.id.time3);
        mTime4 = (TextView) view.findViewById(R.id.time4);
        mTime5 = (TextView) view.findViewById(R.id.time5);
        //Typeface typeFace = Typeface.createFromAsset(context.getAssets(), "fonts/Expansiva.ttf");
        //mTime.setTypeface(typeFace);
        //mTime.getPaint().setFakeBoldText(true);//equals to android:textStyle="bold" in xml
        //tv_week = (TextView) view.findViewById(R.id.tv_week);
        mDate = (TextView) view.findViewById(R.id.date);
        mWeek = (TextView) view.findViewById(R.id.week);
        init();
        final Calendar calendar = Calendar.getInstance();
        second = calendar.get(Calendar.SECOND);
        handler.sendMessageDelayed(handler.obtainMessage(),
                (60 - second) * 1000);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_DATE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
    }

    public void init() {
        SimpleDateFormat timeFormat;
        timeFormat = new SimpleDateFormat("HH:mm");
        String time = timeFormat.format(new Date()).toString();
        if (time.length() == 5){
            mTime1.setText(time.charAt(0)+"");
            mTime2.setText(time.charAt(1)+"");
            mTime3.setText(time.charAt(2)+"");
            mTime4.setText(time.charAt(3)+"");
            mTime5.setText(time.charAt(4)+"");
        }else{
            Log.d(TAG,"time size = " + time.length());
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        SimpleDateFormat weekFormat = new SimpleDateFormat("EEEE");
        mDate.setText(dateFormat.format(new Date(System.currentTimeMillis())));
        mWeek.setText(weekFormat.format(new Date(System.currentTimeMillis())));
    }

}
