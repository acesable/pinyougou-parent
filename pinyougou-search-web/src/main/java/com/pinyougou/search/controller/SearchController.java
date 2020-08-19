package com.pinyougou.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.service.SearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/searchController")
public class SearchController {

    @Reference
    private SearchService searchService;

    @RequestMapping("/search")
    public Map search(@RequestBody Map searchMap) {
        return searchService.search(searchMap);
    }

}
