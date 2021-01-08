package com.pinyougou.page.service;

public interface ItemPageService {

    boolean getItemHtml(Long goodsId);

    //删除静态页
    boolean deleteItemHtml(Long[] goodsIds);
}
