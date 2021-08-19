package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private SeckillOrderService seckillOrderService;

    @RequestMapping("/createNative")
    public Map createNative(String outTradeNo, String totalFee){

        // 测试
        /*IdWorker idWorker = new IdWorker(0,0);
        return weixinPayService.createNative(idWorker.nextId() + "", "1");*/
        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
        TbSeckillOrder seckillOrder = seckillOrderService.getSeckillOrderFromRedis(loginName);
        if(seckillOrder!=null){
            return weixinPayService.createNative(seckillOrder.getId() + "",
                    seckillOrder.getMoney().setScale(2, RoundingMode.DOWN).multiply(new BigDecimal(100)).longValue()+"");
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
        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();

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
                seckillOrderService.saveSeckillOrderFromRedisToDB(loginName, Long.parseLong(outTradeNo), (String) resultMap.get("transaction_id"));
                break;
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            queryTimes++;
            if (queryTimes > 100) {
                result = new Result(false, "二维码超时");
                // 关闭支付
                Map closeResultMap = weixinPayService.closePay(outTradeNo);
                // 关闭支付检查
                if(closeResultMap==null || "FAIL".equals(closeResultMap.get("result_code"))){
                    if("ORDERPAID".equals(closeResultMap.get("err_code"))){
                        // 关闭支付时，返回已支付，则当成功处理
                        result = new Result(true, "支付成功！");
                        // 更细订单状态
                        seckillOrderService.saveSeckillOrderFromRedisToDB(loginName, Long.parseLong(outTradeNo), (String) resultMap.get("transaction_id"));
                    }
                }
                // 支付失败
                if(result.isSuccess()==false){
                    seckillOrderService.deleteSeckillOrder(loginName);
                }
            }
        }

        return result;
    }

}
