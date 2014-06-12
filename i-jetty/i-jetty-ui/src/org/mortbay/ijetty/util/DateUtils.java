package org.mortbay.ijetty.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.ParseException;
import android.util.Log;

public class DateUtils
{
    private static SimpleDateFormat sf = null;
    /*获取系统时间 格式为："yyyy/MM/dd "*/
    public static String getCurrentDate() {
        Date d = new Date();
         sf = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
        return sf.format(d);
    }
                                      
    /*时间戳转换成字符窜*/
    public static String getDateToString(long time) {
        //Log.v("getDateToString", Long.toString(time));
        //Date d = new Date(time);
        sf = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
        String dateTime = sf.format(new Date(time*1000L)); 
        return dateTime;
    }
                                      
    /*将字符串转为时间戳*/
    public static long getStringToDate(String time) {
        sf = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
        Date date = new Date();
        try
        {
            date = sf.parse(time);
        }
        catch (java.text.ParseException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return date.getTime();
    }
}
