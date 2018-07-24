package com.mmall.test;

import org.junit.Test;

import java.math.BigDecimal;

/**
 * created by SupriseMF
 * date:2018-07-21
 */
public class BigDecimalTest {
    //使用Junit进行测试
    @Test
    public void test1() {
        System.out.println(0.05 + 0.01);//加
        System.out.println(1.0 - 0.42);//减
        System.out.println(4.015 * 100);//乘
        System.out.println(123.3 / 100);//除
    }

    @Test
    public void test2() {
        BigDecimal b1 = new BigDecimal(0.05);
        BigDecimal b2 = new BigDecimal(0.01);
        System.out.println(b1.add(b2));
    }

    @Test
    public void test3() {
        //使用String构造器传入参数
        BigDecimal b1 = new BigDecimal("0.05");
        BigDecimal b2 = new BigDecimal("0.01");
        System.out.println(b1.add(b2));
    }
}
