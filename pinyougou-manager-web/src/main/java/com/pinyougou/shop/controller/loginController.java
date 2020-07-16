package com.pinyougou.shop.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class loginController {

    @RequestMapping("/name")
    public Map getLoginName() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map login = new HashMap();
        login.put("loginName", name);
        return login;
    }

}
