package com.luobin.tool;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatUtil {

    public static final String formatType = "yyyy-MM-dd";
    public static final String formatType1 = "yyyy-MM-dd hh:mm:ss";

    public DateFormatUtil() {

    }

    /**
     * 把Date转换成long类型
     *
     * @param date 需要转换的date数据
     * @return
     */
    public static long dateToLong(Date date) {

        return date.getTime();
    }

    /**
     * 将long类型时间转换为Date类型时间
     *
     * @param time 需要转换的时间
     * @return 需要的Date类型时间
     */
    public static Date longToDate(long time) {

        Date date = new Date(time);

        return date;
    }

    /**
     * 将String类型数据转为Date数据
     *
     * @param time       需要转换的时间
     * @param formatType 转换的String格式
     * @return Date类型时间
     * @throws ParseException
     */
    public static Date stringToDate(String time, String formatType) throws ParseException {
        Date date = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat(formatType);
//        Date date = format.parse(time);
            date = new Date(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 将Date类型数据转换为formatType格式的字符串数据
     *
     * @param date       需要转换的Date类型时间
     * @param formatType 转换的格式
     * @return 需要得到的String类型时间
     */
    public static String dateToString(Date date, String formatType) {

        SimpleDateFormat format = new SimpleDateFormat(formatType);

        String time = format.format(date);

        return time;
    }

    /**
     * 将long类型数据转换为formatType格式的字符串数据
     *
     * @param time       需要转换的long类型时间
     * @param formatType 转换的格式
     * @return 需要得到的String类型时间
     */
    public static String longToString(long time, String formatType) {

        Date date = new Date(time);

        return dateToString(date, formatType);
    }

    /**
     * 将String类型数据转换为long类型时间
     *
     * @param time       需要转换的String类型时间
     * @param formatType 转换的格式
     * @return 需要得到的long类型时间
     * @throws ParseException
     */
    public static long stringToLong(String time, String formatType) throws ParseException {

        Date date = stringToDate(time, formatType);

        return date.getTime();
    }

}  