package com.fireflyest.netcontrol.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * @author Fireflyest
 */
public class CalendarUtil {

    private final static String sdf = "MM.dd HH:mm:ss";

    private CalendarUtil(){

    }

    /**
     * 获取当前月份
     * @return int
     */
    public static int getMonth(){
        return Calendar.getInstance(Locale.CHINA).get(Calendar.MONTH)+1;
    }

    /**
     * 获取当天是当月第几天
     * @return int
     */
    public static int getDay(){
        return Calendar.getInstance(Locale.CHINA).get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取当前小时
     * @return int
     */
    public static int getHour(){
        return Calendar.getInstance(Locale.CHINA).get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取当前分钟
     * @return int
     */
    public static int getMinute(){
        return Calendar.getInstance(Locale.CHINA).get(Calendar.MINUTE);
    }

    /**
     * 获取当前秒钟
     * @return int
     */
    public static int getSecond(){
        return Calendar.getInstance(Locale.CHINA).get(Calendar.SECOND);
    }

    /**
     * 获取星期几
     * @return int
     */
    public static int getDayOfWeek(){
        return Calendar.getInstance(Locale.CHINA).get(Calendar.DAY_OF_WEEK)-1;
    }

    /**
     * 获取当前日期字符串
     * @return String
     */
    public static String convertTime(long date){
        return new SimpleDateFormat(sdf, Locale.CHINA).format(date);
    }

    public static String convertHour(long date){
        return String.valueOf(date/1000/60/60);
    }

    public static String convertDay(long date){
        return String.valueOf(date/1000/60/60/24);
    }

    public static String getTimeNow(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(sdf, Locale.CHINA);
        return simpleDateFormat.format(getDate());
    }


    /**
     * 获取当前时间数据
     * @return long
     */
    public static long getDate(){
        return Calendar.getInstance(Locale.CHINA).getTime().getTime();
    }

    /**
     * 获取本月最大天数
     * @return 最大天
     */
    public static int getMaxDay(){
        return new GregorianCalendar().getActualMaximum(Calendar.DATE);
    }

    /**
     * 获取本月第一天在星期几
     * @return 第一天在星期几
     */
    public static int getFirstDay(){
        Calendar first = new GregorianCalendar();
        first.set(Calendar.DAY_OF_MONTH, 1);
        return first.get(Calendar.DAY_OF_WEEK);
    }

}
