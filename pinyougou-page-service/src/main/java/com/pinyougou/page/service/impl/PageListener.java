package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Component
public class PageListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            boolean itemHtml = itemPageService.getItemHtml(Long.parseLong(textMessage.getText()));
            System.out.println("静态网页生成结果: "+textMessage.getText() +" | " + itemHtml);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
