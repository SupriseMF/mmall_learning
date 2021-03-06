package com.mmall.common;


import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by SupriseMF
 * Date: 2018-07-17
 */
public class Const {

    public static final String CURRENT_USER = "currentUser";

    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    //*****使用interface实现小分组,避免enum的繁重*****
    public interface ProductListOrderBy{
        //约定以下划线做一个分割，前边代表排序的字段，后边代表排序的规则
        //set集合查询的时间复杂度为O（1），list的为O（n）!!!!!
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc", "price_asc");

    }

    public interface Cart{
        int CHECKED = 1;//购物车被选中状态
        int UN_CHECKED = 0;//购物车未被选中状态

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";//超过限制数量
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";//未超过限制数量
    }

    //使用接口分组定义常量，而避免枚举的繁重
    public interface Role {
        int ROLE_CUSTOMER = 0;//0为普通用户
        int ROLE_ADMIN = 1;//1为管理员
    }

    public enum ProductStatusEnum {
        ON_SALE(1,"在线");
        private String value;
        private int code;

        ProductStatusEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }


    public enum OrderStatusEnum{
        //检查订单状态
        CANCELED(0,"已取消"),
        NO_PAY(10,"未支付"),
        PAID(20,"已付款"),
        SHIPPED(40,"已发货"),
        ORDER_SUCCESS(50,"订单完成"),
        ORDER_CLOSE(60,"订单关闭");


        OrderStatusEnum(int code,String value){
            this.code = code;
            this.value = value;
        }
        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        public static OrderStatusEnum codeOf(int code){
            for(OrderStatusEnum orderStatusEnum : values()){
                if(orderStatusEnum.getCode() == code){
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("未找到对应的枚举！");
        }
    }
    public interface  AlipayCallback{
        //回调的
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }



    public enum PayPlatformEnum{
        ALIPAY(1,"支付宝");

        PayPlatformEnum(int code,String value){
            this.code = code;
            this.value = value;
        }
        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }

    public enum PaymentTypeEnum{
        ONLINE_PAY(1,"在线支付");

        PaymentTypeEnum(int code,String value){
            this.code = code;
            this.value = value;
        }
        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }


        public static PaymentTypeEnum codeOf(int code){
            for(PaymentTypeEnum paymentTypeEnum : values()){
                if(paymentTypeEnum.getCode() == code){
                    return paymentTypeEnum;
                }
            }
            throw new RuntimeException("未找到对应的枚举！");
        }

    }
}
