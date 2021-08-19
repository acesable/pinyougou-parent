package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.order.service.OrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import pojoGroup.Cart;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private TbOrderItemMapper orderItemMapper;

	@Autowired
	private TbPayLogMapper payLogMapper;

	/**
	 * 增加订单
	 */
	@Override
	public void add(TbOrder order) {
		/**
		 * 1. 根据用户id查询购物车
		 * 2. 遍历购物车 获得订单信息并存入数据库
		 * 新增2.1 存支付日志
		 * 新增2.2 redis存下支付日志信息，给pay服务生成付钱二维码时获取支付编号和支付金额使用
		 * 3. 清空redis
		 */
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());

		//订单ID列表
		List<String> orderIdList = new ArrayList<>();
		//支付总金额
		BigDecimal totalFee = new BigDecimal(0);

		for (Cart cart : cartList) {
			TbOrder tbOrder = new TbOrder();
			tbOrder.setOrderId(idWorker.nextId());
			tbOrder.setPaymentType(order.getPaymentType());
			tbOrder.setStatus("1");
			tbOrder.setCreateTime(new Date());
			tbOrder.setUpdateTime(new Date());
			tbOrder.setUserId(order.getUserId());
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());
			tbOrder.setReceiverMobile(order.getReceiverMobile());
			tbOrder.setReceiver(order.getReceiver());
			tbOrder.setSourceType(order.getSourceType());
			tbOrder.setSellerId(cart.getSellerId());

			BigDecimal totalMoney = new BigDecimal(0);
			for (TbOrderItem orderItem : cart.getOrderItemList()) {
				orderItem.setId(idWorker.nextId());
				orderItem.setOrderId(tbOrder.getOrderId());
				orderItemMapper.insert(orderItem);
				totalMoney = totalMoney.add(orderItem.getTotalFee());
			}
			tbOrder.setPayment(totalMoney); // 合计金额
			orderMapper.insert(tbOrder);

			orderIdList.add(tbOrder.getOrderId()+"");
			totalFee = totalFee.add(totalMoney);
		}

		//微信支付时存支付日志（系统目前支持了微信支付）
		if("1".equals(order.getPaymentType())){
			TbPayLog payLog = new TbPayLog();
			payLog.setOutTradeNo(idWorker.nextId() + ""); // 支付编号
			payLog.setCreateTime(new Date()); // 创建时间
			payLog.setOrderList(
					orderIdList.toString()
							.replace("[","")
							.replace("]",""));// 订单列表
			payLog.setPayType("1");//微信支付
			payLog.setTradeState("0");//未支付
			payLog.setUserId(order.getUserId());// 用户ID
			payLog.setTotalFee(totalFee.multiply(new BigDecimal(100)).longValue()); // 支付金额(分)
			payLogMapper.insert(payLog);
			// redis存下支付日志信息，给pay服务生成付钱二维码时获取支付编号和支付金额使用
			redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);
		}

		redisTemplate.boundHashOps("cartList").delete(order.getUserId());

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public TbPayLog searchPayLogFromRedis(String userid) {
		return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userid);
	}

	@Override
	public void updateOrderStatus(String outTradeNo, String transactionId) {
		/**
		 * 1. 更新数据库支付日志表状态
		 * 2. 更新数据库订单表订单状态
		 * 3. 清空redis缓存中的客户支付日志
		 */
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(outTradeNo);
		payLog.setPayTime(new Date());
		payLog.setTransactionId(transactionId);
		payLog.setTradeState("1");//已支付
		payLogMapper.updateByPrimaryKey(payLog);

		String[] orderIds = payLog.getOrderList().split(",");
		for (String orderId : orderIds) {
			TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
			order.setStatus("2");
			order.setPaymentTime(new Date());
			orderMapper.updateByPrimaryKey(order);
		}

		redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
	}

}
