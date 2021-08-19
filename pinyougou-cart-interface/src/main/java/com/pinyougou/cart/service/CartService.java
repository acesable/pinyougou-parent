package com.pinyougou.cart.service;

import pojoGroup.Cart;

import java.util.List;

public interface CartService {
    /**
     * 将商品添加到购物车
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);

    /**
     * 从redis中获取购物车
     * @param username
     * @return
     */
    List<Cart> getCartListFromRedis(String username);

    /**
     * 将购物车保存到reids
     * @param cartList
     * @param username
     * @return
     */
    List<Cart> saveCartListToRedis(List<Cart> cartList, String username);


    /**
     * 将两个购物车合并
     * @param cartList1
     * @param cartList2
     * @return
     */
    List<Cart> mergeCartLists(List<Cart> cartList1, List<Cart> cartList2);

}
