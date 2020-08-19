package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 10000)
public class SearchServiceImpl implements SearchService {

    @Autowired
    private SolrTemplate solrTemplate;
    
    @Override
    public Map search(Map searchMap) {
        Map resultMap = new HashMap();
        resultMap.putAll(searchItemList(searchMap));
        resultMap.put("categoryList", searchCategory(searchMap));
        return resultMap;
    }

    /**
     * 商品列表
     */
    public Map searchItemList(Map searchMap) {
        Map resultMap = new HashMap();
        /*Query query = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        ScoredPage<TbItem> tbItems = solrTemplate.queryForPage(query, TbItem.class);
        System.out.println("~~~~~~~666~~~~~~~~~~");
        resultMap.put("rows", tbItems.getContent());*/

        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));

        HighlightQuery highlightQuery = new SimpleHighlightQuery(criteria);
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        highlightOptions.setSimplePrefix("<em style='color:red'>").setSimplePostfix("</em>");
        highlightQuery.setHighlightOptions(highlightOptions);

        HighlightPage<TbItem> tbItems = solrTemplate.queryForHighlightPage(highlightQuery, TbItem.class);
        // 高亮内容的入口集合(多条记录)
        List<HighlightEntry<TbItem>> highlighted = tbItems.getHighlighted();

        for (HighlightEntry<TbItem> highlightEntry : highlighted) {
            /*//高亮域个数(设置了多少个高亮field)
            List<HighlightEntry.Highlight> highlights = highlightEntry.getHighlights();
            for (HighlightEntry.Highlight highlight : highlights) {
                //每个高亮域可能存储多值(复值域)
                System.out.println(highlight.getSnipplets());
            }*/
            //因为只有一个
            highlightEntry.getEntity().setTitle(highlightEntry.getHighlights().get(0).getSnipplets().get(0));
        }

        resultMap.put("rows", tbItems.getContent());
        return resultMap;
    }

    public List searchCategory(Map searchMap) {
        List<String> categoryList = new ArrayList<String>();
        Query query = new SimpleQuery("*:*");
        //根据关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);

        // 获取分组页
        GroupPage<TbItem> tbItems = solrTemplate.queryForGroupPage(query, TbItem.class);
        //获取某个分组结果对象
        GroupResult<TbItem> groupResult = tbItems.getGroupResult("item_category");
        //获取某个分组入口
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //获取该分组信息集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        //获取该分组中每条记录的信息
        for (GroupEntry entry : content) {
            categoryList.add(entry.getGroupValue());
        }
        return categoryList;
    }
}
