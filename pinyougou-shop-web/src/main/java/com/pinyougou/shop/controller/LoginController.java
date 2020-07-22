package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Reference
    private SellerService sellerService;

    @RequestMapping("/userInfo")
    public Map userInfo() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(userId);
        TbSeller seller = sellerService.findOne(userId);
        Map userInfo = new HashMap();
        userInfo.put("loginName", seller.getNickName());
        userInfo.put("loginId", userId);
        return userInfo;
    }

}
