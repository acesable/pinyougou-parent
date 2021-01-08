package com.pinyougou.manager.controller;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;
import pojoGroup.Goods;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private Destination solrQueue; // 更新solr索引

	@Autowired
	private Destination solrQueueDel; // 删除solr索引

    @Autowired
    private Destination pageTopic; // 通知页面生成服务生成静态页

    @Autowired
    private Destination pageDeleteTopic;// 通知静态页服务删除详细页
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	

	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(final Long [] ids){
		try {
			goodsService.delete(ids);

			//从索引库中删除
//          itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
            jmsTemplate.send(solrQueueDel, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });

            //删除每个网页上的商品详细页
            jmsTemplate.send(pageDeleteTopic, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });

			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}

//	@Reference(timeout = 100000)
//	private SearchService searchService;

    /**
     * 更新审核状态
     */
    @RequestMapping("/updateAuditStatus")
    public Result updateAuditStatus(Long[] ids, String auditStatus) {
        try {
            goodsService.updateAuditStatus(ids, auditStatus);
            if ("1".equals(auditStatus)) {//审核通过
                //等到需要导入的sku列表
                List<TbItem> itemListByGoodsIdListAndStatus = goodsService.findItemListByGoodsIdListAndStatus(ids, auditStatus);
//              searchService.importList(itemListByGoodsIdListAndStatus);
                final String itemListStr = JSON.toJSONString(itemListByGoodsIdListAndStatus);
                // 导入solr索引
                jmsTemplate.send(solrQueue, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createTextMessage(itemListStr);
                    }
                });

                // 商品生成详细页
                for(final Long goodsId:ids){
//                    itemPageService.getItemHtml(goodsId);
                    jmsTemplate.send(pageTopic, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(goodsId+"");
                        }
                    });
                }
            }
            return new Result(true, "审核成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "审核失败");
        }
    }

//    @Reference(timeout = 400000)
//    private ItemPageService itemPageService;

//    @RequestMapping("/getItemHtml")
//    public void getItemHtml(Long goodsId) {
//        itemPageService.getItemHtml(goodsId);
//    }





}
