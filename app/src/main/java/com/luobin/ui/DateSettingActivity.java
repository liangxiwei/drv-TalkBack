package com.luobin.ui;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;

import com.luobin.dvr.R;

import java.util.Calendar;

public class DateSettingActivity  extends BaseDialogActivity implements View.OnClickListener,
        DatePicker.OnDateChangedListener, RadioGroup.OnCheckedChangeListener {

    public static long MIN_DATE = 1194220800000L;

    RadioGroup mSettingMode;
    RadioButton mBtnAudo, mBtnManual;
    Button mBtnOk, mBtnCancel;
    DatePicker mDatePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_settings);
        mDatePicker = (DatePicker) findViewById(R.id.date_picker);
        mSettingMode = (RadioGroup) findViewById(R.id.setting_mode);
        mBtnAudo = (RadioButton) findViewById(R.id.btn_auto);
        mBtnManual = (RadioButton) findViewById(R.id.btn_manual);
        mBtnOk = findViewById(R.id.btn_confirm);
        mBtnCancel = findViewById(R.id.btn_cancel);

        boolean autoTime = (Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME, 0) > 0);
        if (autoTime) {
            mBtnAudo.setChecked(true);
        } else {
            mBtnManual.setChecked(true);
        }
        mSettingMode.setOnCheckedChangeListener(this);

        mDatePicker.setEnabled(!autoTime);
        mDatePicker.setOnDateChangedListener(this);

        mBtnOk.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btn_cancel:
                finish();
                break;
            case R.id.btn_confirm:
                boolean autoTime = (mSettingMode.getCheckedRadioButtonId() == R.id.btn_auto);
                Settings.Global.putInt(getContentResolver(), Settings.Global.AUTO_TIME, autoTime ? 1 : 0);
                if (!autoTime) {
                    setDate(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
                }
                sendBroadcast(new Intent("android.luobin.ui.TIME_RESET"));
                finish();
                break;
        }
    }

    void setDate(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        long when = Math.max(c.getTimeInMillis(), MIN_DATE);
        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
    }

    public void onCheckedChanged(RadioGroup view, int id) {
        int checkedId = mSettingMode.getCheckedRadioButtonId();
        switch(checkedId) {
            case R.id.btn_auto:
                mDatePicker.setEnabled(false);
                break;
            case R.id.btn_manual:
                mDatePicker.setEnabled(true);
                break;
        }
    }
}