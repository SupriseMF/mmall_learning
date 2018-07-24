package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * created by SupriseMF
 * date:2018-07-20
 */
public class PropertiesUtil {
    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    private static Properties props;

    static {
        String fileName = "mmall.properties";
        props = new Properties();
        //使用Reader方式传参数
        //1.new一个InputStreamReader
        //2.通过PropertiesUtil.class.getClassLoader()加载此Util
        //3.再通过getResourceAsStream从fileName读到配置信息
        //其中load方法实现中会抛出IOException，InputStreamReader中会捕获UnsupportedEncodingException，getResourceAsStream中会捕获IOException
        //由于使用InputStreamReader(InputStream in, String charsetName)其中第二个参数指定编码类型
        try {
            props.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName), "UTF-8"));
        } catch (IOException e) {
            logger.error("配置文件读取异常！", e);
        }
    }

    public static String getProperty(String key) {
        //为避免空格，将key进行trim()
        String value = props.getProperty(key.trim());
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    //重载方法，若value为空则传入defaultValue
    public static String getProperty(String key,String defaultValue) {
        //为避免空格，将key进行trim()
        String value = props.getProperty(key.trim());
        if (StringUtils.isBlank(value)) {
            value = defaultValue;
        }
        return value.trim();
    }





}
