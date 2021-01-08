package com.pinyougou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

@Component
public class ItemSearchListener implements MessageListener {

    @Autowired
    private SearchService searchService;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            System.out.println("监听到消息: "+textMessage.getText());
            List<TbItem> tbItemList = JSON.parseArray(textMessage.getText(), TbItem.class);
            searchService.importList(tbItemList);
            System.out.println("将item数据导入到solr");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
