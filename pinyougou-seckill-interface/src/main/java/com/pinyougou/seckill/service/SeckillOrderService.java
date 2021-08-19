package com.pinyougou.seckill.service;
import java.util.List;
import com.pinyougou.pojo.TbSeckillOrder;

import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SeckillOrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbSeckillOrder seckillOrder);
	
	
	/**
	 * 修改
	 */
	public void update(TbSeckillOrder seckillOrder);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillOrder findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize);

	/**
	 * 提交秒杀订单
	 * @param seckillId
	 * @param userId
	 */
	void submitOrder(Long seckillId, String userId);

	/**
	 * 使用用户账号，从redis缓存中获取秒杀订单信息
	 * @param userId
	 * @return
	 */
	TbSeckillOrder getSeckillOrderFromRedis(String userId);

	/**
	 * 从缓存中查询订单，并存入数据库
	 *
	 * @param userId
	 * @param orderId
	 * @param transactionId
	 */
	void saveSeckillOrderFromRedisToDB(String userId, Long orderId, String transactionId);

	/**
	 * 删除用户的订单信息，并回退对应商品的库存
	 * @param userId
	 */
	void deleteSeckillOrder(String userId);
}
