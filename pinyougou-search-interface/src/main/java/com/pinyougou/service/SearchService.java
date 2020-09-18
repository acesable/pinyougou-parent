package com.pinyougou.service;

import java.util.List;
import java.util.Map;

public interface SearchService {

    Map search(Map searchMap);

    /**
     * 导入列表
     * @param list
     */
    void importList(List list);

}
