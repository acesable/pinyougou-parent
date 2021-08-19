package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;
import util.HttpClient;
import util.IdWorker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhanq on 2021/8/10.
 */
@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;

    @Override
    public Map createNative(String outTradeNo, String totalFee) {
        /**
         * 1. 参数封装
         * 2. 发送请求
         * 3. 获取结果
         */
        Map param = new HashMap();
        param.put("appid", appid);//公众账号ID
        param.put("mch_id", partner);//商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body", "品优购商品");//商品描述
        param.put("out_trade_no", outTradeNo);//商户订单号
        param.put("total_fee", totalFee); // 总价(分)
        param.put("spbill_create_ip", "127.0.0.1");//终端IP
        param.put("notify_url", "http://goods.pinyougou.com");//通知地址
        param.put("trade_type", "NATIVE");//交易类型

        try {
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("发起报文："+paramXml);

            // 商户的信息失效了，所以只能做挡板了
            /*HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();
            String resultXml = httpClient.getContent();
            System.out.println("返回报文："+resultXml);

            Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXml);

            Map returnMap = new HashMap();
            returnMap.put("code_url", resultMap.get("code_url"));*/
            Map returnMap = new HashMap();
            returnMap.put("code_url", "http://weixinzhifu.tradeNo="+outTradeNo);
            returnMap.put("out_trade_no", outTradeNo);
            returnMap.put("total_fee", totalFee);
            return returnMap;

        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }

    @Override
    public Map queryPayStatus(String outTradeNo) {
        /**
         * 1. 参数封装
         * 2. 发送请求
         * 3. 获取结果
         */
        Map param = new HashMap();
        param.put("appid", appid);//公众账号ID
        param.put("mch_id", partner);//商户号
        param.put("out_trade_no", outTradeNo);//商户订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串

        try {
            System.out.println(param.toString()+"|"+partnerkey);
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("查询付款状态发送报文："+paramXml);

            // 商户的信息失效了，所以只能做挡板了
            /*HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();
            String resultXml = httpClient.getContent();
            System.out.println("查询付款状态接受报文："+resultXml);

            Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXml);

            return resultMap;*/
            Map returnMap = new HashMap();
            returnMap.put("trade_state", "SUCCESS");
            returnMap.put("trade_state", "WAIT");
            returnMap.put("transaction_id", "微信返回的支付订单号");
            return returnMap;

        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }

    /**
     * 真实的关闭微信支付，需要间隔5分钟才能发起
     * @param outTradeNo
     * @return
     */
    @Override
    public Map closePay(String outTradeNo) {
        /**
         * 1. 参数封装
         * 2. 发送请求
         * 3. 获取结果
         */
        Map param = new HashMap();
        param.put("appid", appid);//公众账号ID
        param.put("mch_id", partner);//商户号
        param.put("out_trade_no", outTradeNo);//商户订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串

        try {
            System.out.println(param.toString()+"|"+partnerkey);
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("查询付款状态发送报文："+paramXml);

            // 商户的信息失效了，所以只能做挡板了
            /*HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();
            String resultXml = httpClient.getContent();
            System.out.println("查询付款状态接受报文："+resultXml);

            Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXml);

            return resultMap;*/
            Map returnMap = new HashMap();
            returnMap.put("return_code", "SUCCESS");
            //关闭成功
            returnMap.put("result_code", "SUCCESS");
            //关闭失败，已支付
            /*returnMap.put("result_code", "FAIL");
            returnMap.put("err_code", "ORDERPAID");*/
            return returnMap;

        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }

}
