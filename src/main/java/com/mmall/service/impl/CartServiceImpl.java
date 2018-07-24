package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * created by SupriseMF
 * date: 2018-07-21
 */

@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    //复用list()方法
    public ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count) {
        //校验
        if (productId==null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGLE_ARGUMENT.getCode(), ResponseCode.ILLEAGLE_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
        if (cart == null) {
            //若为空，购物车则不存在该产品记录，需新增
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);
            //需要配置setChecked()中参数为常量
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);

            cartMapper.insert(cartItem);
        } else {
            //产品已存在购物车，只需增加数量
            count = cart.getQuantity() + count;
            //更新count
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.list(userId);
    }

    public ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count) {
        //校验
        if (productId==null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGLE_ARGUMENT.getCode(), ResponseCode.ILLEAGLE_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
        if (cart != null) {
            cart.setQuantity(count);
        }
        //更新count
        return this.list(userId);
}

    public ServerResponse<CartVo> deleteProduct(Integer userId, String productIds) {
        //Guava中splitter工具，分割并转为list
        List<String> productList = Splitter.on(",").splitToList(productIds);
        if (CollectionUtils.isEmpty(productList)) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGLE_ARGUMENT.getCode(), ResponseCode.ILLEAGLE_ARGUMENT.getDesc());
        }
        //若存在则删除
        cartMapper.deleteByUserIdProductIds(userId, productList);
        return this.list(userId);
    }

    public ServerResponse<CartVo> list(Integer userId) {
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<CartVo> selectOrUnSelect(Integer userId,Integer productId,Integer checked) {
        cartMapper.checkedOrUncheckedProduct(userId,productId,checked);
        return this.list(userId);
    }

    public ServerResponse<Integer> getCartProuctCount(Integer userId) {
        if (userId == null) {
            return ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }


    /**
     * 关键方法！！！包括全选、反选都会调用此私有方法
     * @param userId
     * @return
     */
    private CartVo getCartVoLimit(Integer userId){
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        //将CartProductVo放入CartVo，调用Guava创建
        List<CartProductVo> cartProductVoList = Lists.newArrayList();

        //初始化购物车总价：使用String构造器初始化
        BigDecimal cartTotalPrice = new BigDecimal("0");

        //cartList非空则遍历并创建CartProductVo
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(cartList)) {
            for (Cart cartItem : cartList) {
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cartItem.getProductId());
                //查询购物车中产品
                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if (product != null) {
                    //product存在则组装cartProductVo
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    //判断库存,先初始化
                    int buyLimitCount = 0;
                    if (product.getStock() >= cartItem.getQuantity()) {
                        //库存足够
                        buyLimitCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    } else {
                        //库存不够，只能赋库存值
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        //购物车中更新有效库存，通过Id查找并只更新Quantity字段
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);

                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    //使用BigDecimal计算该产品总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartProductVo.getQuantity().doubleValue()));
                    //调用cartItem.getChecked()状态进行勾选
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }
                if (cartItem.getChecked() == Const.Cart.CHECKED) {
                    //若已勾选，购物车总价cartTotalPrice增加cartProductVoget.ProductTotalPrice()（其中值都是doubleValue()，再转换为String）并更新
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        //计算完成，开始组建并返回cartVo
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return cartVo;
    }

    private boolean getAllCheckedStatus(Integer userId) {
        if (userId == null) {
            return false;
        }
        //若SQL查询出对应userId下product的checked=0（未勾选）状态数量为0，则为已全选
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;
    }





}
