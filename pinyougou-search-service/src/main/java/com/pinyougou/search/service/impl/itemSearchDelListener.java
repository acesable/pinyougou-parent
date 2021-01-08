package com.pinyougou.search.service.impl;

import com.pinyougou.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Arrays;

@Component
public class itemSearchDelListener implements MessageListener {

    @Autowired
    private SearchService searchService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] ids = (Long[]) objectMessage.getObject();
            System.out.println("监听到消息: "+ids);
            searchService.deleteByGoodsIds(Arrays.asList(ids));
            System.out.println("将solr中的item数据删除");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}

