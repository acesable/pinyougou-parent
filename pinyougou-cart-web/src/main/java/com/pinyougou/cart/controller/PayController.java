package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.IdWorker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhanq on 2021/8/11.
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference(timeout = 5000)
    private WeixinPayService weixinPayService;

    @Reference
    private OrderService orderService;

    @RequestMapping("/createNative")
    public Map createNative(String outTradeNo, String totalFee){

        // 测试
        /*IdWorker idWorker = new IdWorker(0,0);
        return weixinPayService.createNative(idWorker.nextId() + "", "1");*/
        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog payLog = orderService.searchPayLogFromRedis(loginName);
        if(payLog!=null){
            return weixinPayService.createNative(payLog.getOutTradeNo() + "", payLog.getTotalFee()+"");
        }else{
            return new HashMap();
        }
    }

    /**
     * 查询订单状态，如果支付成功，更新订单表状态
     * @param outTradeNo
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String outTradeNo){
        Result result = null;

        int queryTimes = 0;
        while (true) {

            Map resultMap = weixinPayService.queryPayStatus(outTradeNo);
            if (resultMap == null) {
                result = new Result(false, "支付失败！");
                break;
            }
            if("SUCCESS".equals(resultMap.get("trade_state"))){
                result = new Result(true, "支付成功！");
                // 更细订单状态
                orderService.updateOrderStatus(outTradeNo, (String) resultMap.get("transaction_id"));
                break;
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            queryTimes++;
            if (queryTimes > 10) {
                return new Result(false, "二维码超时");
            }
        }

        return result;
    }

}
