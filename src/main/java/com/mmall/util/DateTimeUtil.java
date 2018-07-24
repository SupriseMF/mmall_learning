package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * created by SupriseMF
 * date:2018-07-20
 */
public class DateTimeUtil {

    public static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";
    //使用joda-time开源包实现工具类
    //需实现两个方法：1.string--->>Date;2:Date--->>string
    //方法1：string--->>Date
    public static Date strToDate(String dateTimeStr, String formatStr) {
        //formatStr为转换的字符串格式
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(formatStr);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }

    //方法2：Date--->>string
    //StringUtils.EMPTY与""空字符串的区别
    public static String dateToStr(Date date, String formatStr) {
        if (date == null) {
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        //dateTime.toString()中指定参数为转换格式
        return dateTime.toString(formatStr);
    }

    public static Date strToDate(String dateTimeStr) {
        //formatStr为转换的字符串格式
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(STANDARD_FORMAT);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }

    //方法2：Date--->>string
    //StringUtils.EMPTY与""空字符串的区别
    public static String dateToStr(Date date ) {
        if (date == null) {
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        //dateTime.toString()中指定参数为转换格式
        return dateTime.toString(STANDARD_FORMAT);
    }
}
