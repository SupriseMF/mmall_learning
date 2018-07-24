package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * created by SupriseMF
 * date:2018-07-21
 */
public class CartVo {
    List<CartProductVo> cartProductVoList;
    private BigDecimal cartTotalPrice;
    private Boolean allChecked;//是否全部勾选

    private String imageHost;

    public List<CartProductVo> getCartProductVoList() {
        return cartProductVoList;
    }

    public void setCartProductVoList(List<CartProductVo> cartProductVoList) {
        this.cartProductVoList = cartProductVoList;
    }

    public BigDecimal getCartTotalPrice() {
        return cartTotalPrice;
    }

    public void setCartTotalPrice(BigDecimal cartTotalPrice) {
        this.cartTotalPrice = cartTotalPrice;
    }

    public Boolean getAllChecked() {
        return allChecked;
    }

    public void setAllChecked(Boolean allChecked) {
        this.allChecked = allChecked;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }
}
