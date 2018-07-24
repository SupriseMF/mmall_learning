package com.mmall.common;
/**
 * Created by SupriseMF
 * Date: 2018-07-17
 */

public enum ResponseCode {

    SUCCESS(0,"SUCCESS"),
    ERROR(1,"ERROR"),
    NEED_LOGIN(10,"NEED_LOGIN"),
    ILLEAGLE_ARGUMENT(2,"ILLEGAL_ARGUMENT");

    //code
    private final int code;
    //description描述
    private final String desc;

    ResponseCode(int code,String desc){
        this.code = code;
        this.desc = desc;
    }
    //开放出内属性
    public int getCode(){
        return code;
    }
    public String getDesc(){
        return desc;
    }

}
