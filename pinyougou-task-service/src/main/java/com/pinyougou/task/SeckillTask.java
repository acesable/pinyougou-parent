package com.pinyougou.task;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zhanq on 2021/8/18.
 */
@Component
public class SeckillTask {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    /**
     * 更新秒杀商品进缓存
     * 考虑了缓存中已有的商品不更新
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void refreshSeckillGoods() {
        System.out.println("----更新秒杀商品缓存数据 任务开始："+new Date()+"----");

        ArrayList seckillGoodsIdList = new ArrayList<>(redisTemplate.boundHashOps("seckillGoods").keys());

        TbSeckillGoodsExample seckillGoodsExample = new TbSeckillGoodsExample();
        Criteria criteria = seckillGoodsExample.createCriteria();
        criteria.andStatusEqualTo("1");
        criteria.andStockCountGreaterThan(0);
        criteria.andStartTimeLessThan(new Date());
        criteria.andEndTimeGreaterThan(new Date());

        //如果缓存中存在数据，则缓存中的数据不用从数据库中更新
        if (seckillGoodsIdList.size() > 0) {
            criteria.andIdNotIn(seckillGoodsIdList);
        }

        List<TbSeckillGoods> seckillList = seckillGoodsMapper.selectByExample(seckillGoodsExample);

        for (TbSeckillGoods seckillGoods : seckillList) {
            redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
            System.out.println("新增秒杀商品ID："+seckillGoods.getId());
        }
        System.out.println("----任务结束----");
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void removeSeckillGoods() {
        /**
         * 1. 从缓存中获取秒杀商品数据
         * 2. 遍历商品并清除过期商品
         */
        System.out.println("----开始清除过期商品任务----");
        List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
        for (TbSeckillGoods seckillGoods : seckillGoodsList) {
            if (seckillGoods.getEndTime().getTime() < new Date().getTime()) {
                redisTemplate.boundHashOps("seckillGoods").delete(seckillGoods.getId());
                System.out.println("清除过期商品："+seckillGoods.getId());
            }
        }
        System.out.println("----任务结束----");

    }
}
