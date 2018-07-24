package com.mmall.common;

import ch.qos.logback.classic.Logger;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * created by SupriseMF
 * date:2018-07-17
 */
public class TokenCache {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TokenCache.class);

    public static final String TOKEN_PREFIX = "token_";

    //Guava中放本地缓存
    //调用链模式,1000为缓存的初始化容量,当超过设置的最大size，Guava使用LRU(最少使用)算法移除缓存项,设置访问后过期即有效期，2个参数，第二参数为时间单位
    private static LoadingCache<String, String> localCache = CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).expireAfterAccess(12,TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                //此build（CacheLoader<>为一抽象类），本次使用匿名实现
                //为默认的数据加载实现，当get取值，key未命中，则调用此方法加载
                @Override
                public String load(String s) throws Exception {
                    //为避免判断key.equals()时key为null,将此处return的null转换为字符串的“null”
                    return "null";
                }
            });

    public static void setKey(String key, String value) {
        localCache.put(key, value);
    }

    public static String getKey(String key) {
        String value = null;
        try {
            value = localCache.get(key);
            if ("null".equals(value)) {
                //对应上面注释，此时返回上面应返回的null
                return null;
            }
            return value;
        } catch (Exception e) {
            logger.error("LocalCache捕获到异常！",e);
        }
        return null;
    }
}
