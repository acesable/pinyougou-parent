package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import pojoGroup.Cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanq on 2021/8/5.
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 将商品添加到购物车
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        // 1. 根据商品sku id查找商品sku信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("商品不存在!");
        }
        if(!"1".equals(item.getStatus())){
            throw new RuntimeException("商品状态不合法");
        }
        // 2. 根据商品id获取商家id
        String sellerId = item.getSellerId();

        // 3. 判断购物车列表中是否有该商家的购物车组
        Cart cart = getCartFromCartListBySellerId(cartList, sellerId);

        if (cart == null) {
            // 4. 购物车列表中没有该商家的购物车组
            // 4.1 创建商品订单明细，将它添加到购物车组
            TbOrderItem orderItem = newOrderItem(item, num);

            cart = new Cart();
            cart.setSellerId(item.getSellerId());
            cart.setSellerName(item.getSeller());
            List<TbOrderItem> orderItemList = new ArrayList<>();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            // 4.2 将购物车组添加到购物车
            cartList.add(cart);

        }else{
            // 5. 购物车列表中有该商家的购物车组, 判断购物车组中是否有该商品
            TbOrderItem orderItem = getOrderItemFromCartByItemId(cart, itemId);
            if (orderItem == null) {
                // 5.1 购物车组中没有该商品, 将商品添加到购物车组
                orderItem = newOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            }else{
                // 5.2 购物车组中有该商品, 添加该商品的数量, 并修改价格
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
                if(orderItem.getNum()<=0){
                    // 如果商品数量改到0, 移除此商品
                    cart.getOrderItemList().remove(orderItem);
                    if(cart.getOrderItemList().size()<=0){
                        //如果商品组的个数为0, 移除此商品组
                        cartList.remove(cart);
                    }
                }
            }
        }

        return cartList;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> getCartListFromRedis(String username) {
        System.out.println("从redis中获取购物车...");
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList == null) {
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public List<Cart> saveCartListToRedis(List<Cart> cartList, String username) {
        System.out.println("将购物车存入redis...");
        redisTemplate.boundHashOps("cartList").put(username, cartList);
        return cartList;
    }

    @Override
    public List<Cart> mergeCartLists(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList2) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList1;
    }

    // 根据商家id从购物车中查找购物车组
    public Cart getCartFromCartListBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if(sellerId.equals(cart.getSellerId())){
                return cart;
            }
        }
        return null;
    }

    public TbOrderItem getOrderItemFromCartByItemId(Cart cart, Long itemId) {
        for (TbOrderItem orderItem : cart.getOrderItemList()) {
            if (itemId.equals(orderItem.getItemId())) {
                return orderItem;
            }
        }
        return null;
    }

    public TbOrderItem newOrderItem(TbItem item, Integer num) {
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setItemId(item.getId());
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setNum(num);
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return orderItem;
    }
}
