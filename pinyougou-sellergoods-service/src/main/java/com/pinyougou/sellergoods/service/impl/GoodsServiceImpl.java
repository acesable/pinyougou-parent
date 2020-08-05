package com.pinyougou.sellergoods.service.impl;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.support.json.JSONParser;
import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsExample;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import pojoGroup.Goods;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
    private TbGoodsDescMapper goodsDescMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
    private TbItemMapper tbItemMapper;

	@Autowired
    private TbItemCatMapper tbItemCatMapper;

	@Autowired
    private TbBrandMapper tbBrandMapper;

	@Autowired
    private TbSellerMapper tbSellerMapper;

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
        goods.getGoods().setAuditStatus("0");// 状态未审核
		goodsMapper.insert(goods.getGoods());
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        goodsDescMapper.insert(goods.getGoodsDesc());
        setItemValue(goods);
	}

    private void setItemValue(Goods goods) {
        TbGoods tbGoods = goods.getGoods();
        //如果是更新的话,先删除改goods的所有item记录,重新存入
        if (goods.getGoods().getId() != null) {
            TbItemExample tbItemExample = new TbItemExample();
            TbItemExample.Criteria criteria = tbItemExample.createCriteria();
            criteria.andGoodsIdEqualTo(goods.getGoods().getId());
            tbItemMapper.deleteByExample(tbItemExample);
        }
        if ("1".equals(tbGoods.getIsEnableSpec())) {
            for (TbItem item : goods.getItemList()) {
                String title = tbGoods.getGoodsName();
                Map<String,Object> map = JSON.parseObject(item.getSpec());
                for (String key : map.keySet()) {
                    title += " "+map.get(key);
                }
                item.setTitle(title);
                setDefaultItemValue(item, goods);
                tbItemMapper.insert(item);
            }
        }else{
            TbItem item = new TbItem();
            item.setTitle(tbGoods.getGoodsName());
            item.setPrice(tbGoods.getPrice());
            item.setNum(999999);
            item.setStatus("0");
            item.setIsDefault("1");

            setDefaultItemValue(item, goods);
            tbItemMapper.insert(item);
        }
    }

    private void setDefaultItemValue(TbItem item, Goods goods) {
	    TbGoods tbGoods = goods.getGoods();
        item.setSellPoint(tbGoods.getCaption());//卖点
        List<Map> maps = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        if (maps.size() > 0) {
            item.setImage((String)maps.get(0).get("url"));
        }
        item.setCategoryid(tbGoods.getCategory3Id());//三级分类ID
        item.setCreateTime(new Date());
        item.setUpdateTime(new Date());
        item.setGoodsId(tbGoods.getId());
        item.setSellerId(tbGoods.getSellerId());
        String category = tbItemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id()).getName();
        item.setCategory(category);
        item.setBrand(tbBrandMapper.selectByPrimaryKey(tbGoods.getBrandId()).getName());
        item.setSeller(tbSellerMapper.selectByPrimaryKey(tbGoods.getSellerId()).getNickName());
    }

	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		goodsMapper.updateByPrimaryKey(goods.getGoods());
        goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
        setItemValue(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
	    Goods goods = new Goods();
	    goods.setGoods(goodsMapper.selectByPrimaryKey(id));
        goods.setGoodsDesc(goodsDescMapper.selectByPrimaryKey(id));
        TbItemExample tbItemExample = new TbItemExample();
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<TbItem> tbItems = tbItemMapper.selectByExample(tbItemExample);
        goods.setItemList(tbItems);
        return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			goodsMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(goods!=null){			
		    if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				// criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
                criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
