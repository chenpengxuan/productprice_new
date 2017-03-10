package com.ymatou.productprice.intergration.client;

import com.alibaba.fastjson.JSON;
import com.ymatou.productprice.infrastructure.util.LogWrapper;
import com.ymatou.useranalysis.facade.BuyerFirstOrderFacade;
import com.ymatou.useranalysis.facade.BuyerOrderStatisticsFacade;
import com.ymatou.useranalysis.facade.model.req.GetBuyerFirstOrderInfoReq;
import com.ymatou.useranalysis.facade.model.req.GetBuyerOrderStatisticsReq;
import com.ymatou.useranalysis.facade.model.resp.GetBuyerFirstOrderInfoResp;
import com.ymatou.useranalysis.facade.model.resp.GetBuyerOrderStatisticsResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 用户行为服务
 * Created by chenpengxuan on 2017/3/7.
 */
@Component
public class UserBehaviorAnalysisService {

    @Resource
    private BuyerOrderStatisticsFacade buyerOrderStatisticsFacade;

    @Resource
    private BuyerFirstOrderFacade buyerFirstOrderFacade;

    @Autowired
    private LogWrapper logWrapper;

    /**
     * 获取用户特征
     *
     * @param sellerIdList
     * @param buyerId
     * @return
     */
    public GetBuyerOrderStatisticsResp getBuyerBehavior(List<Long> sellerIdList, int buyerId) {
        if (buyerId <= 0) {
            return null;
        }

        GetBuyerOrderStatisticsReq req = new GetBuyerOrderStatisticsReq();
        req.setBuyerId(buyerId);
        req.setSellerIds(sellerIdList);

        try {
            GetBuyerOrderStatisticsResp resp = buyerOrderStatisticsFacade.getBuyerOrderStatistics(req);
            logWrapper.recordInfoLog("用户行为服务_getBuyerOrderStatistics,request为{},response为{}",req,resp);
            if (resp == null
                    || resp.getFromSeller() == null) {
                logWrapper.recordErrorLog("用户行为服务_getBuyerOrderStatistics接口返回值不正确,response：{},buyerId:{},sellerIdList{}",
                        resp,buyerId, JSON.toJSONString(Optional.ofNullable(sellerIdList).orElse(Arrays.asList())));
            }
            return resp;
        } catch (Exception ex) {
            logWrapper.recordErrorLog("获取用户特征_getBuyerOrderStatistics发生异常,sellerIdList:{},buyerId:{}", JSON.toJSONString(Optional.ofNullable(sellerIdList).orElse(Arrays.asList())) , buyerId, ex);
        }
        return null;
    }

    /**
     * 获取用户首单信息
     *
     * @param buyerId
     * @return
     */
    public GetBuyerFirstOrderInfoResp getBuyerFirstOrderInfo(long buyerId) {
        GetBuyerFirstOrderInfoReq req = new GetBuyerFirstOrderInfoReq();
        req.setBuyerIds(Arrays.asList(buyerId));

        try {
            GetBuyerFirstOrderInfoResp resp = buyerFirstOrderFacade.getBuyerFirstOrderInfo(req);
            logWrapper.recordInfoLog("用户行为服务_getBuyerFirstOrderInfo,request为{},response为{}",req,resp);
            if (resp == null
                    || resp.getFirstOrderInfos() == null
                    || resp.getFirstOrderInfos().get(buyerId) == null) {
                logWrapper.recordErrorLog("用户行为服务_getBuyerFirstOrderInfo接口返回值不正确,response：{},buyerId:{}", resp,buyerId);
            }
            return resp;
        } catch (Exception ex) {
            logWrapper.recordErrorLog("用户行为服务_getBuyerFirstOrderInfo发生异常,buyerId：{}", buyerId,ex);
            return null;
        }
    }
}
