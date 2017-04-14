package com.ymatou.productprice.domain.service;

import com.ymatou.productprice.domain.model.ActivityCatalog;
import com.ymatou.productprice.domain.model.ActivityProduct;
import com.ymatou.productprice.model.PriceEnum;
import com.ymatou.productprice.model.ProductPriceForSearched;
import com.ymatou.useranalysis.facade.model.resp.GetBuyerOrderStatisticsResp;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 计算价格的逻辑（用于新增接口->搜索商品列表）
 * Created by chenpengxuan on 2017/4/14.
 */
@Component
public class CalcPriceServiceForSearch extends CalcPriceService{
    /**
     * 决定最终价格
     *
     * @param buyerId
     * @param productPriceForSearchedList
     * @param activityProductInfoList
     * @param resp
     * @param isTradeIsolation
     */
    protected void decideProductRealPriceForSearch(long buyerId,
                                          List<ProductPriceForSearched> productPriceForSearchedList,
                                          List<ActivityProduct> activityProductInfoList,
                                          GetBuyerOrderStatisticsResp resp,
                                          boolean isNewBuyer,
                                          boolean isTradeIsolation) {

        productPriceForSearchedList.forEach(productPrice -> {

            ActivityProduct tempActivityProduct = activityProductInfoList != null
                    && !activityProductInfoList.isEmpty() ?
                    activityProductInfoList.stream()
                            .filter(x -> x.getProductId().equals(productPrice.getProductId()))
                            .findAny().orElse(null) : null;
                calcRealPrice(buyerId, productPrice.getSellerId(), resp, isNewBuyer, isTradeIsolation, productPrice, tempActivityProduct);
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
                                 ProductPriceForSearched productPriceForSearched,
                                 ActivityProduct activityProductInfo) {
        //活动商品价格逻辑优先级最高
        if (checkActivityPriceAsRealPrice(isNewBuyer, isTradeIsolation, activityProductInfo)) {
            setActivityPriceAsRealPrice(productPriceForSearched, activityProductInfo);
        }
        else if (checkVisitorPriceAsRealPrice(buyerId, resp)) {
            setVisitorPriceAsRealPriceLogicForSearch(productPriceForSearched);
        }
        else if (checkVipPriceAsRealPriceForSearch(sellerId, resp, productPriceForSearched)) {
            setVipPriceAsRealPriceForSearch(productPriceForSearched);
        }
        else if (checkNewCustomerPriceAsRealPrice(sellerId, resp, productPriceForSearched)) {
            setNewCustomerPriceAsRealPrice(productPriceForSearched);
        }
        else {
            setQuotePriceAsRealPriceLogic(productPriceForSearched);
        }
    }

    /**
     * 设置原价是否作为最终价格(用于新增接口->搜索商品列表)
     *
     * @param productPriceForSearched
     */
    private void setQuotePriceAsRealPriceLogic(ProductPriceForSearched productPriceForSearched) {
        productPriceForSearched.setPriceType(PriceEnum.QUOTEPRICE.getCode());
        productPriceForSearched.setMinPrice(productPriceForSearched.getMinOriginalPrice());
        productPriceForSearched.setMaxPrice(productPriceForSearched.getMaxOriginalPrice());
    }


    /**
     * 检查新客价格是否作为最终价格(用于新增接口->搜索商品列表)
     *
     * @param sellerId
     * @param resp
     * @param productPriceForSearched
     * @return
     */
    private boolean checkNewCustomerPriceAsRealPrice(Long sellerId, GetBuyerOrderStatisticsResp resp, ProductPriceForSearched productPriceForSearched) {
        //买家如果没有订单或订单全部取消
        if (resp != null
                && resp.getFromSeller().get(sellerId) != null
                && resp.getFromSeller().get(sellerId).isNoOrdersOrAllCancelled()
                && (productPriceForSearched.getMinNewpersonPrice() > 0 || productPriceForSearched.getMaxNewpersonPrice() > 0)
                ) {
            return true;
        }
        return false;
    }

    /**
     * 设置新客价格是否作为最终价格(用于新增接口->搜索商品列表)
     * @param productPriceForSearched
     * @return
     */
    private void setNewCustomerPriceAsRealPrice(ProductPriceForSearched productPriceForSearched) {
        productPriceForSearched.setMinPrice(productPriceForSearched.getMinNewpersonPrice() > 0 ?
                productPriceForSearched.getMinNewpersonPrice() : productPriceForSearched.getMinOriginalPrice());
        productPriceForSearched.setMaxPrice(productPriceForSearched.getMaxNewpersonPrice() > 0 ?
                productPriceForSearched.getMaxNewpersonPrice() : productPriceForSearched.getMaxOriginalPrice());
        productPriceForSearched.setPriceType(PriceEnum.VIPPRICE.getCode());
    }

    /**
     * 检查vip价格是否作为最终价格(用于新增接口->搜索商品列表)
     *
     * @param sellerId
     * @param resp
     * @param productPriceForSearched
     * @return
     */
    private boolean checkVipPriceAsRealPriceForSearch(Long sellerId,
                                                      GetBuyerOrderStatisticsResp resp,
                                                      ProductPriceForSearched productPriceForSearched) {
        //买家已经有确认的订单
        if (resp != null
                && resp.getFromSeller().get(sellerId) != null
                && resp.getFromSeller().get(sellerId).isHasConfirmedOrders()
                && (productPriceForSearched.getMinVipPrice() > 0 || productPriceForSearched.getMaxVipPrice() > 0)
                ) {
            return true;
        }
        return false;
    }

    /**
     * 设置vip价格作为最终价格(用于新增接口->搜索商品列表)
     * @param productPriceForSearched
     */
    private void setVipPriceAsRealPriceForSearch(ProductPriceForSearched productPriceForSearched) {
        productPriceForSearched.setMinPrice(productPriceForSearched.getMinVipPrice() > 0 ?
                productPriceForSearched.getMinVipPrice() : productPriceForSearched.getMinOriginalPrice());
        productPriceForSearched.setMaxPrice(productPriceForSearched.getMaxVipPrice() > 0 ?
                productPriceForSearched.getMaxVipPrice() : productPriceForSearched.getMaxOriginalPrice());
        productPriceForSearched.setPriceType(PriceEnum.VIPPRICE.getCode());
    }

    /**
     * 设置访客价格作为最终价格
     * @param productPriceForSearched
     */
    private void setVisitorPriceAsRealPriceLogicForSearch( ProductPriceForSearched productPriceForSearched) {
        Map<PriceEnum, Double> priceMap = new HashMap<>();
        priceMap.put(PriceEnum.NEWCUSTOMERPRICE, productPriceForSearched.getMinNewpersonPrice() > 0 ?
                productPriceForSearched.getMinNewpersonPrice() : productPriceForSearched.getMaxNewpersonPrice());
        priceMap.put(PriceEnum.QUOTEPRICE, productPriceForSearched.getMinOriginalPrice() > 0 ?
                productPriceForSearched.getMinOriginalPrice() : productPriceForSearched.getMaxOriginalPrice());
        priceMap.put(PriceEnum.VIPPRICE, productPriceForSearched.getMinVipPrice() > 0 ?
                productPriceForSearched.getMinVipPrice() : productPriceForSearched.getMaxVipPrice());

        Map.Entry<PriceEnum, Double> minEntry = priceMap.entrySet()
                .stream()
                .min((x, y) -> Double.compare(x.getValue().doubleValue(), y.getValue().doubleValue()))
                .get();

        Map.Entry<PriceEnum, Double> maxEntry = priceMap.entrySet()
                .stream()
                .max((x, y) -> Double.compare(x.getValue().doubleValue(), y.getValue().doubleValue()))
                .get();
        productPriceForSearched.setMinPrice(minEntry.getValue());
        productPriceForSearched.setMaxPrice(maxEntry.getValue());
        productPriceForSearched.setPriceType(minEntry.getKey().getCode());
    }

    /**
     * 检查活动价格逻辑(用于新增接口->搜索商品列表)
     *
     * @param isNewBuyer
     * @param isTradeIsolation
     * @param activityProductInfo
     * @return
     */
    private boolean checkActivityPriceAsRealPrice(boolean isNewBuyer,
                                                  boolean isTradeIsolation,
                                                  ActivityProduct activityProductInfo) {
        /**
         * 是否需要计算活动价格
         */
        boolean needsCalculateActivityProductPrice = activityProductInfo != null
                && (!activityProductInfo.getHasIsolation()
                || isTradeIsolation);
        List<ActivityCatalog> activityProductCatalogList = activityProductInfo != null ?
                activityProductInfo.getActivityCatalogList() : null;

        //过滤掉无效的商品规格列表
        List<ActivityCatalog> validActivityProductCatalogList = activityProductCatalogList != null ? activityProductCatalogList
                .stream()
                .filter(catalog ->
                        catalog.getActivityStock() > 0
                                && catalog.getActivityCatalogPrice() > 0).collect(Collectors.toList()) : null;

        if (needsCalculateActivityProductPrice
                && validActivityProductCatalogList != null
                && !validActivityProductCatalogList.isEmpty()
                ) {
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
     * 设置活动价作为最终价格
     *
     * @param productPriceForSearched
     * @param activityProductInfo
     * @return
     */
    private void setActivityPriceAsRealPrice(ProductPriceForSearched productPriceForSearched,
                                             ActivityProduct activityProductInfo) {

        List<ActivityCatalog> activityProductCatalogList = activityProductInfo != null ?
                activityProductInfo.getActivityCatalogList() : null;

        List<ActivityCatalog> validActivityProductCatalogList = activityProductCatalogList != null ? activityProductCatalogList
                .stream()
                .filter(catalog ->
                        catalog.getActivityStock() > 0
                                && catalog.getActivityCatalogPrice() > 0).collect(Collectors.toList()) : null;

        Optional<ActivityCatalog> maxValidActivityCatalogOptional = validActivityProductCatalogList
                .stream()
                .max((x, y) -> Double.compare(x.getActivityCatalogPrice(), y.getActivityCatalogPrice()));

        Optional<ActivityCatalog> minValidActivityCatalogOptional = validActivityProductCatalogList
                .stream()
                .min((x, y) -> Double.compare(x.getActivityCatalogPrice(), y.getActivityCatalogPrice()));

        //最高活动商品规格活动价
        double maxActivityPrice = maxValidActivityCatalogOptional.isPresent() ?
                maxValidActivityCatalogOptional
                        .get()
                        .getActivityCatalogPrice() : 0D;

        //最低活动商品规格活动价
        double minActivityPrice = minValidActivityCatalogOptional.isPresent() ?
                minValidActivityCatalogOptional
                        .get()
                        .getActivityCatalogPrice() : 0D;
        productPriceForSearched.setMaxPrice(maxActivityPrice);
        productPriceForSearched.setMinPrice(minActivityPrice);
        productPriceForSearched.setPriceType(PriceEnum.YMTACTIVITYPRICE.getCode());
    }

}
