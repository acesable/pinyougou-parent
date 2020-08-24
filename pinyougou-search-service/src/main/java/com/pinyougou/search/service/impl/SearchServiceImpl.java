package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Filter;

@Service(timeout = 10000)
public class SearchServiceImpl implements SearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;
    
    @Override
    public Map search(Map searchMap) {
        Map resultMap = new HashMap();
        resultMap.putAll(searchItemList(searchMap));
        List categoryList=searchCategory(searchMap);
        resultMap.put("categoryList", categoryList);
        if (categoryList.size() > 0) {
            Long categoryId = (Long) redisTemplate.boundHashOps("itemCats").get(categoryList.get(0));
            resultMap.put("brands", selectBrands(categoryId));
            resultMap.put("specItems", selectSpecItems(categoryId));
        }
        return resultMap;
    }

    /**
     * 商品列表
     */
    public Map searchItemList(Map searchMap) {
        // 1 查询高亮
        Map resultMap = new HashMap();
        /*Query query = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        ScoredPage<TbItem> tbItems = solrTemplate.queryForPage(query, TbItem.class);
        System.out.println("~~~~~~~666~~~~~~~~~~");
        resultMap.put("rows", tbItems.getContent());*/

        HighlightQuery highlightQuery = new SimpleHighlightQuery();

        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        highlightOptions.setSimplePrefix("<em style='color:red'>").setSimplePostfix("</em>");
        highlightQuery.setHighlightOptions(highlightOptions);

        String keywords = (String) searchMap.get("keywords");
        Criteria criteria = new Criteria("item_keywords").is(keywords.replace(" ",""));
        highlightQuery.addCriteria(criteria);


        //2 筛选条件
        //2.1 商品分类
        if(!"".equals(searchMap.get("category"))){
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            highlightQuery.addFilterQuery(filterQuery);
        }
        //2.2 品牌
        if(!"".equals(searchMap.get("brand"))){
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            highlightQuery.addFilterQuery(filterQuery);
        }
        //2.3 规格
        if (searchMap.get("spec") != null) {
            Map<String,String> specMap = (Map<String, String>) searchMap.get("spec");
                for (String key : specMap.keySet()) {
                    Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
                    FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                    highlightQuery.addFilterQuery(filterQuery);
                }
        }

        //2.4 价格区间
        if(!"".equals(searchMap.get("price"))){
            String[] price = ((String)searchMap.get("price")).split("-");
            if(!"0".equals(price[0])){
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                highlightQuery.addFilterQuery(filterQuery);
            }
            if(!"*".equals(price[1])){
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                highlightQuery.addFilterQuery(filterQuery);
            }
        }

        //2.5 分页
        Integer pageNum = searchMap.get("pageNum")==null?1:(Integer)searchMap.get("pageNum");
        Integer pageSize = searchMap.get("pageSize")==null?20:(Integer)searchMap.get("pageSize");
        highlightQuery.setOffset((pageNum-1)*pageSize);
        highlightQuery.setRows(pageSize);

        //2.6 排序
        String sort = (String) searchMap.get("sort");
        String sortField = (String) searchMap.get("sortField");



        // 3 获取结果
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
            //增加了判断是因为如果高亮区域没有搜索的关键字, 高亮的搜索结果就是空数组 highlightEntry.getHighlights().size()=0
            if(highlightEntry.getHighlights().size()>0 && highlightEntry.getHighlights().get(0).getSnipplets().size()>0){
                //因为只有一个
                highlightEntry.getEntity().setTitle(highlightEntry.getHighlights().get(0).getSnipplets().get(0));
            }
        }

        resultMap.put("rows", tbItems.getContent());
        resultMap.put("totalPageNum", tbItems.getTotalPages());
        resultMap.put("totalNum", tbItems.getTotalElements());
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

    public List selectBrands(Long id) {
        return (List) redisTemplate.boundHashOps("brands").get(id);
    }

    public List selectSpecItems(Long id) {
        return (List) redisTemplate.boundHashOps("specItems").get(id);
    }


}
