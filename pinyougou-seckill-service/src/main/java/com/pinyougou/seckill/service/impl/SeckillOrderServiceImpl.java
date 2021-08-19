package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private IdWorker idWorker;

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	@Override
	public void submitOrder(Long seckillId, String userId) {
		/**
		 * 1. 从redis缓存中获取秒杀商品信息
		 * 1.1 如果秒杀商品不存在 或 秒杀商品库存为0则提示对应错误
		 * 2. 更新库存数量
		 * 3. 如果库存数量<=0，更新数据库中秒杀商品信息，清空redis中该秒杀商品
		 * 4. 产生订单，不存数据库，在订单成功付款后才存入订单表 见saveSeckillOrderFromRedisToDB方法
		 * 5. 经秒杀订单存入redis缓存，供支付订单使用
		 */

		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);

		if(seckillGoods==null){
			throw new RuntimeException("商品不存在");
		}
		if (seckillGoods.getStockCount() == 0) {
			throw new RuntimeException("商品已被抢完");
		}
		seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
		redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);

		if(seckillGoods.getStockCount()<=0){
			seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
			redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
		}

		TbSeckillOrder seckillOrder = new TbSeckillOrder();

		seckillOrder.setId(idWorker.nextId());
		seckillOrder.setSeckillId(seckillId);
		seckillOrder.setMoney(seckillGoods.getCostPrice());
		seckillOrder.setUserId(userId);
		seckillOrder.setSellerId(seckillGoods.getSellerId());
		seckillOrder.setCreateTime(new Date());
		seckillOrder.setStatus("0");

		redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);
	}

	@Override
	public TbSeckillOrder getSeckillOrderFromRedis(String userId) {
		return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
	}

	@Override
	public void saveSeckillOrderFromRedisToDB(String userId, Long orderId, String transactionId) {
		/**
		 * 1. 从redis中查询订单
		 * 2. 更新订单的属性
		 * 3. 存入数据库
		 * 4. 清除redis中的缓存
		 */
		TbSeckillOrder seckillOrder = getSeckillOrderFromRedis(userId);

		if(seckillOrder==null){
			throw new RuntimeException("用户订单信息不存在");
		}
		if (seckillOrder.getId().longValue() != orderId.longValue()) {
			throw new RuntimeException("用户订单与提交订单号不一致");
		}

		seckillOrder.setPayTime(new Date());
		seckillOrder.setStatus("1");
		seckillOrder.setTransactionId(transactionId);

		seckillOrderMapper.insert(seckillOrder);

		redisTemplate.boundHashOps("seckillOrder").delete(userId);
	}

	@Override
	public void deleteSeckillOrder(String userId) {
		/**
		 * 1. 从redis中获取用户的订单信息
		 * 2. 回退订单中的商品的库存信息
		 * 3. 删除redis中的用户订单信息
		 */
		TbSeckillOrder seckillOrder = getSeckillOrderFromRedis(userId);

		if (seckillOrder == null) {
			return;
		}
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
		if (seckillGoods == null) {
			seckillGoods.setStockCount(1);
			//todo 查询数据库填充seckillGooods的其他属性
		}else{
			seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
		}
		redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);

		redisTemplate.boundHashOps("seckillOrder").delete(userId);
	}

}
