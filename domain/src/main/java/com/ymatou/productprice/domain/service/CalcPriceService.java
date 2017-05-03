package com.ymatou.productprice.domain.service;

import com.ymatou.productprice.domain.model.ActivityCatalog;
import com.ymatou.productprice.domain.model.ActivityProduct;
import com.ymatou.productprice.infrastructure.util.LogWrapper;
import com.ymatou.productprice.model.Catalog;
import com.ymatou.productprice.model.PriceEnum;
import com.ymatou.productprice.model.ProductPrice;
import com.ymatou.useranalysis.facade.model.resp.GetBuyerOrderStatisticsResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 计算价格的逻辑
 * Created by chenpengxuan on 2017/4/14.
 */
@Component
public class CalcPriceService {
    @Autowired
    private LogWrapper logWrapper;

    /**
     * 决定最终价格
     *
     * @param buyerId
     * @param productPriceList
     * @param activityProductInfoList
     * @param resp
     * @param isTradeIsolation
     */
    public void decideProductRealPrice(long buyerId,
                                          List<ProductPrice> productPriceList,
                                          List<ActivityProduct> activityProductInfoList,
                                          GetBuyerOrderStatisticsResp resp,
                                          boolean isNewBuyer,
                                          boolean isTradeIsolation) {
        long now = new Date().getTime();
        productPriceList.stream().forEach(productPrice -> {

            ActivityProduct tempActivityProduct = activityProductInfoList != null
                    && !activityProductInfoList.isEmpty() ?
                    activityProductInfoList.stream()
                            .filter(x -> x.getProductId().equals(productPrice.getProductId())
                             && x.getStartTime().getTime() <= now && x.getEndTime().getTime() >= now)
                            .findAny().orElse(null) : null;

            productPrice.getCatalogs().forEach(catalog -> {
                calcRealPrice(buyerId,catalog.getSellerId(),resp,isNewBuyer,isTradeIsolation,catalog,tempActivityProduct);
            });
        });
    }

    /**
     * 计算最终价格
     */
    protected void calcRealPrice(long buyerId,
                                 long sellerId,
                                 GetBuyerOrderStatisticsResp resp,
                                 boolean isNewBuyer,
                                 boolean isTradeIsolation,
                                 Catalog catalog,
                                 ActivityProduct activityProductInfo) {
        //活动商品价格逻辑优先级最高
        if (checkActivityPriceAsRealPrice(isNewBuyer, isTradeIsolation, catalog, activityProductInfo)) {
            ActivityCatalog activityCatalog = activityProductInfo.getActivityCatalogList()
                    .stream().filter(x -> x.getActivityCatalogId().equals(catalog.getCatalogId()))
                    .findAny().orElse(null);
            setActivityPriceAsRealPrice(catalog, activityCatalog);
        }
        else if (checkVisitorPriceAsRealPrice(buyerId,resp)){
            setVisitorPriceAsRealPrice(catalog);
        }
        else if(checkVipPriceAsRealPrice(sellerId,resp,catalog)){
            setVipPriceAsRealPrice(catalog);
        }
        else if(checkNewCustomerPriceAsRealPrice(sellerId,resp,catalog)){
            setNewCustomerPriceAsRealPrice(catalog);
        }
        else{
            setQuotePriceAsRealPriceLogic(catalog);
        }
    }

    /**
     * 设置原价作为最终价格
     *
     * @param catalog
     */
    protected PriceEnum setQuotePriceAsRealPriceLogic(Catalog catalog) {
        catalog.setPriceType(PriceEnum.QUOTEPRICE.getCode());
        catalog.setPrice(catalog.getQuotePrice());
        return PriceEnum.QUOTEPRICE;
    }

    /**
     * 检查新客价格是否作为最终价格
     *
     * @param sellerId
     * @param resp
     * @param catalog
     */
    private boolean checkNewCustomerPriceAsRealPrice(Long sellerId, GetBuyerOrderStatisticsResp resp, Catalog catalog) {
        //买家如果没有订单或订单全部取消
        if (resp != null
                && resp.getFromSeller().get(sellerId) != null
                && resp.getFromSeller().get(sellerId).isNoOrdersOrAllCancelled()
                && catalog.getNewCustomerPrice() > 0
                && catalog.getNewCustomerPrice() < catalog.getQuotePrice()) {
            return true;
        }
        return false;
    }

    /**
     * 设置新客价格作为最终价格
     * @param catalog
     */
    private void setNewCustomerPriceAsRealPrice(Catalog catalog){
        catalog.setPrice(catalog.getNewCustomerPrice());
        catalog.setPriceType(PriceEnum.NEWCUSTOMERPRICE.getCode());
    }

    /**
     * 检查vip价格是否作为最终价格
     *
     * @param sellerId
     * @param resp
     * @param catalog
     * @return
     */
    private boolean checkVipPriceAsRealPrice(Long sellerId, GetBuyerOrderStatisticsResp resp, Catalog catalog) {
        //买家已经有确认的订单
        if (resp != null
                && resp.getFromSeller().get(sellerId) != null
                && resp.getFromSeller().get(sellerId).isHasConfirmedOrders()
                && catalog.getVipPrice() > 0
                && catalog.getVipPrice() < catalog.getQuotePrice()) {
            return true;
        }
        return false;
    }

    /**
     * 设置vip价格作为最终价格
     * @param catalog
     * @return
     */
    private void setVipPriceAsRealPrice(Catalog catalog) {
        catalog.setPrice(catalog.getVipPrice());
        catalog.setPriceType(PriceEnum.VIPPRICE.getCode());
    }

    /**
     * 检查访客价格是否作为最终价格
     *
     * @param buyerId
     * @param resp
     */
    protected boolean checkVisitorPriceAsRealPrice(Long buyerId, GetBuyerOrderStatisticsResp resp) {
        //买家未登录记录日志
        if (buyerId <= 0) {
            logWrapper.recordInfoLog("buyerId <=0");
        }
        if (buyerId <= 0
                && (resp == null || resp.getFromSeller() == null)) {
            return true;
        }
        return false;
    }

    /**
     * 设置访客价格作为最终价格
     * @param catalog
     * @return
     */
    private void setVisitorPriceAsRealPrice(Catalog catalog) {
        Map<PriceEnum, Double> priceMap = new HashMap<>();
        priceMap.put(PriceEnum.NEWCUSTOMERPRICE, catalog.getNewCustomerPrice());
        priceMap.put(PriceEnum.QUOTEPRICE, catalog.getQuotePrice());
        priceMap.put(PriceEnum.VIPPRICE, catalog.getVipPrice());
        Map.Entry<PriceEnum, Double> entry = priceMap.entrySet()
                .stream()
                .min((x, y) -> Double.compare(x.getValue().doubleValue(), y.getValue().doubleValue()))
                .get();
        catalog.setPrice(entry.getValue());
        catalog.setPriceType(entry.getKey().getCode());
    }


    /**
     * check活动价格是否作为最终价格
     * 活动商品价格逻辑优先级最高
     *
     * @param isNewBuyer
     * @param isTradeIsolation
     * @param catalog
     * @param activityProductInfo
     */
    private boolean checkActivityPriceAsRealPrice(boolean isNewBuyer,
                                                  boolean isTradeIsolation,
                                                  Catalog catalog,
                                                  ActivityProduct activityProductInfo) {
        /**
         * 是否需要计算活动价格
         */
        boolean needsCalculateActivityProductPrice = activityProductInfo != null
                && (!activityProductInfo.getHasIsolation()
                || isTradeIsolation);

        ActivityCatalog activityCatalog = needsCalculateActivityProductPrice ? activityProductInfo.getActivityCatalogList()
                .stream().filter(x -> x.getActivityCatalogId().equals(catalog.getCatalogId()))
                .findAny().orElse(null) : null;

        if (needsCalculateActivityProductPrice
                && activityCatalog != null
                && activityCatalog.getActivityCatalogPrice() > 0
                && activityCatalog.getActivityStock() > 0) {
            //新人活动
            if (activityProductInfo.getNewBuyer()
                    && isNewBuyer) {
                return true;
            } else if (activityProductInfo.getNewBuyer()
                    && !isNewBuyer) {
                return false;
            }

            return true;
        }
        return false;
    }

    /**
     * 设置活动价格作为最终价格
     *
     * @param catalog
     * @param activityCatalog
     */
    private void setActivityPriceAsRealPrice(Catalog catalog, ActivityCatalog activityCatalog) {
        catalog.setActivityPrice(activityCatalog.getActivityCatalogPrice());
        catalog.setPrice(catalog.getActivityPrice());
        catalog.setPriceType(PriceEnum.YMTACTIVITYPRICE.getCode());
    }
}
