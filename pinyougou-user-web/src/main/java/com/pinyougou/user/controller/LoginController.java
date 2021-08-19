package com.pinyougou.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhanq on 2021/8/5.
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    @RequestMapping("/name")
    public Map showName() {
        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
        Map info = new HashMap();
        info.put("loginName", loginName);
        return info;
    }

}
