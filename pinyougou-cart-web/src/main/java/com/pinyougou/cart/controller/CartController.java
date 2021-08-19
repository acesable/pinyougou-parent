package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pojoGroup.Cart;
import util.CookieUtil;

import javax.rmi.CORBA.Util;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by zhanq on 2021/8/6.
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Reference(timeout = 6000)
    private CartService cartService;

    @RequestMapping("/getCartList")
    public List<Cart> getCartList() {
        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(loginName);

        // 从cookie中获取出本地购物车 -- 合并用
        String cartListStr = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListStr == null || "".equals(cartListStr)) {
            cartListStr = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListStr, Cart.class);

        if ("anonymousUser".equals(loginName)) {
            System.out.println("从cookie中获取购物车");
            // 用户未登录
            return cartList_cookie;
        } else {
            System.out.println("从redis中获取购物车");
            // 用户已登录
            List<Cart> cartList_redis = cartService.getCartListFromRedis(loginName);
            if(cartList_cookie.size()>0){
                System.out.println("合并购物车...");
                //cookie中有本地购物车, 合并购物车
                List<Cart> cartList = cartService.mergeCartLists(cartList_cookie, cartList_redis);
                cartService.saveCartListToRedis(cartList, loginName);
                util.CookieUtil.deleteCookie(request, response, "cartList");
                return cartList;
            }

            return cartList_redis;
        }
    }

    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:9105", allowCredentials = "true")
    public Result addGoodsToCartList(Long itemId, Integer num) {

        //允许此域进行跨域请求此方法
//        response.addHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        //如果本方法涉及对cookie的操作，需要添加以下代码，表明允许操作cookie
//        response.addHeader("Access-Control-Allow-Credentials", "true");

        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(loginName);

        try {
            String cartListStr = "";
            // 1. 查找购物车
            List<Cart> cartList = getCartList();
            // 2. 将商品存入购物车
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);


            // 更新购物车到cookie或redis中
            if ("anonymousUser".equals(loginName)) {
                System.out.println("更新购物车到cookie中");
                // 用户未登录 将购物车更新到cookie中
                cartListStr = JSON.toJSONString(cartList);
                CookieUtil.setCookie(request, response, "cartList",
                        cartListStr, 3600 * 24, "UTF-8");
            } else {
                System.out.println("更新购物车到redis中");
                // 用户已登录 将购物车更新到redis中
                cartService.saveCartListToRedis(cartList, loginName);
            }

            return new Result(true, "添加商品成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加商品失败");
        }


    }

}
