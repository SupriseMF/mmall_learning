package com.mmall.util;

import java.math.BigDecimal;

/**
 * created by SupriseMF
 * date:2018-07-21
 */
public class BigDecimalUtil {
    //不许在外部构造-->私有构造器
    private BigDecimalUtil() {

    }

    public static BigDecimal add(double v1, double v2) {
        //调用Double.toString(double)方法
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2);
    }

    public static BigDecimal sub(double v1, double v2) {
        //调用Double.toString(double)方法
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2);
    }

    public static BigDecimal mul(double v1, double v2) {
        //调用Double.toString(double)方法
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2);
    }

    public static BigDecimal div(double v1, double v2) {
        //调用Double.toString(double)方法
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        //考虑到除不尽的情况：并保留小数2位，进行四舍五入ROUND_HALF_UP。
        return b1.divide(b2,2,BigDecimal.ROUND_HALF_UP);
    }

}
