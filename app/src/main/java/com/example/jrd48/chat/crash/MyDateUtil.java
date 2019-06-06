package com.example.jrd48.chat.crash;

import android.annotation.SuppressLint;
import android.net.ParseException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MyDateUtil {
	/**
	 * 计算两个日期之间相差的天数
	 * 
	 * @param smdate
	 *            较小的时间
	 * @param bdate
	 *            较大的时间
	 * @return 相差天数
	 * @throws ParseException
	 * @throws java.text.ParseException
	 */
	@SuppressLint("SimpleDateFormat")
	public static int daysBetween(Date smdate, Date bdate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			smdate = sdf.parse(sdf.format(smdate));

			bdate = sdf.parse(sdf.format(bdate));
			Calendar cal = Calendar.getInstance();
			cal.setTime(smdate);
			long time1 = cal.getTimeInMillis();
			cal.setTime(bdate);
			long time2 = cal.getTimeInMillis();
			long between_days = (time2 - time1) / (1000 * 3600 * 24);

			return Integer.parseInt(String.valueOf(between_days));
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		return Integer.MIN_VALUE;
	}
	
	/**
	 * 计算两个时间之间的毫秒数差（date2 - date1）
	 * @param date1 小的时间
	 * @param date2 大的时间
	 * @return
	 */
	public static long milsecBetween(Date date1, Date date2)
	{
		 long a = date2.getTime();
		  long b = date1.getTime();
		  return a-b;
	}

	public static String formatDate(Date dt) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		return sdf.format(dt);
	}


}
