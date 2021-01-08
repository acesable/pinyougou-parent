package com.pinyougou.page.service.impl;

import com.alibaba.fastjson.JSON;
import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;

@Component
public class PageDeleteListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            System.out.println("收到消息: "+objectMessage.getObject());
            Long[] goodsIds = (Long[]) objectMessage.getObject();
            System.out.println(goodsIds.toString());
            boolean flag = itemPageService.deleteItemHtml(goodsIds);
            System.out.println("删除静态页: "+goodsIds.toString()+"结果:"+flag);
        } catch (JMSException e) {
            e.printStackTrace();
            System.out.println("123");
        }
    }
}
