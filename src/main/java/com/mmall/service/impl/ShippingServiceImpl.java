package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * created by SupriseMF
 * date:2018-07-23
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {


    @Autowired
    private ShippingMapper shippingMapper;

    //添加收货地址
    //通过springMVC的对象绑定传入Shipping对象，省去复杂繁冗的参数方式
    public ServerResponse add(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        int rowCount = shippingMapper.insert(shipping);
        //为了在insert()生效之后立刻拿到Mybatis自动生成的id，
        //需要增加两个配置：1：在写SQL语句时insert语句添加userGeneratedKeys="true"；2：keyProperty="id"
        if(rowCount > 0){
            //因为需要在新增之后给出前端的提示信息，需将新增的id返回，使用Map承载该数据
            Map result = Maps.newHashMap();
            result.put("shippingId",shipping.getId());
            return ServerResponse.createBySuccess("新建地址成功",result);
        }
        return ServerResponse.createByErrorMessage("新建地址失败");
    }

    public ServerResponse<String> del(Integer userId,Integer shippingId){
        //由于存在的**横向越权**，在删除中不可以使用deleteByPrimaryKey(),-->由于shippingId未与用户关联，导致可能会通过随意指定的shippingId删除，因此应通过传入的userId进行删除
        int resultCount = shippingMapper.deleteByShippingIdUserId(userId,shippingId);
        if(resultCount > 0){
            return ServerResponse.createBySuccess("删除地址成功");
        }
        return ServerResponse.createByErrorMessage("删除地址失败");
    }


    public ServerResponse update(Integer userId, Shipping shipping){
        //不需要将id返回至前端
        //从登陆用户中获取并指定shipping的userId
        shipping.setUserId(userId);
        //shippingMapper.updateByShipping()方法借用Mybatis生成的updateByPrimaryKey()方法，
        // 但是不能更新当前的userId，通过在where条件中指定该shippingId的userId即可
        int rowCount = shippingMapper.updateByShipping(shipping);
        if(rowCount > 0){
            return ServerResponse.createBySuccess("更新地址成功");
        }
        return ServerResponse.createByErrorMessage("更新地址失败");
    }

    public ServerResponse<Shipping> select(Integer userId, Integer shippingId){
        //查询并返回一个Shipping对象
        Shipping shipping = shippingMapper.selectByShippingIdUserId(userId,shippingId);
        if(shipping == null){
            return ServerResponse.createByErrorMessage("无法查询到该地址");
        }
        return ServerResponse.createBySuccess("更新地址成功",shipping);
    }


    public ServerResponse<PageInfo> list(Integer userId,int pageNum,int pageSize){
        //需要分页，即返回pagehelper的pageInfo类型
        PageHelper.startPage(pageNum,pageSize);
        //返回所有的地址信息
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        //通过list构造pageInfo
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }







}
