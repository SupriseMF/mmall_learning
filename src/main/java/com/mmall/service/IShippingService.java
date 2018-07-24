package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;

/**
 * created by SupriseMF
 * date:2018-07-23
 */

public interface IShippingService {

    ServerResponse add(Integer userId, Shipping shipping);

    ServerResponse<String> del(Integer userId,Integer shippingId);

    ServerResponse update(Integer userId, Shipping shipping);

    ServerResponse<Shipping> select(Integer userId, Integer shippingId);

    ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize);

}