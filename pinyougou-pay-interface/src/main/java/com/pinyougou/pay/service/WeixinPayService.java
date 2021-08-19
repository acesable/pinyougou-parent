package com.pinyougou.pay.service;

import java.util.Map;

public interface WeixinPayService {

    /**
     * 微信统一下单创建NATIVE链接URL
     * @param outTradeNo
     * @param totalFee
     * @return
     */
    Map createNative(String outTradeNo, String totalFee);


    /**
     * 查询付款状态
     *
     * @param outTradeNo
     * @return
     */
    Map queryPayStatus(String outTradeNo);

    /**
     * 关闭支付
     * @param outTradeNo
     */
    Map closePay(String outTradeNo);

}
