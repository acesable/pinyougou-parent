package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 品牌接口
 */
public interface BrandService {
    /**
     * 查询所有品牌
     *
     * @return
     */
    List<TbBrand> findAll();

    /**
     * 查询当前页
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageResult findPage(int pageNum, int pageSize);

    /**
     * 新增品牌
     *
     * @param tbBrand
     */
    void add(TbBrand tbBrand);

    /**
     * 根据ID查询品牌信息
     *
     * @param id
     * @return
     */
    TbBrand findOne(Long id);

    /**
     * 跟据ID更新品牌信息
     *
     * @param tbBrand
     */
    void update(TbBrand tbBrand);

    /**
     * 删除n个品牌信息
     *
     * @param ids
     */
    void delete(Long[] ids);

    /**
     * 条件查询
     *
     * @param tbBrand
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageResult search(TbBrand tbBrand, int pageNum, int pageSize);

    /**
     * 品牌查询
     */
    List<Map> selectOptionList();
}
