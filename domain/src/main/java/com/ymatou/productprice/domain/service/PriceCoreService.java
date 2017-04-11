package com.ymatou.productprice.domain.service;

import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import com.ymatou.productprice.domain.model.ActivityCatalog;
import com.ymatou.productprice.domain.model.ActivityProduct;
import com.ymatou.productprice.infrastructure.util.LogWrapper;
import com.ymatou.productprice.intergration.client.UserBehaviorAnalysisService;
import com.ymatou.productprice.model.Catalog;
import com.ymatou.productprice.model.PriceEnum;
import com.ymatou.productprice.model.ProductPrice;
import com.ymatou.productprice.model.ProductPriceForSearched;
import com.ymatou.useranalysis.facade.model.req.GetBuyerFirstOrderInfoReq;
import com.ymatou.useranalysis.facade.model.resp.GetBuyerFirstOrderInfoResp;
import com.ymatou.useranalysis.facade.model.resp.GetBuyerOrderStatisticsResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 价格服务核心服务 重度内聚核心逻辑
 * Created by chenpengxuan on 2017/3/8.
 */
@Component
public class PriceCoreService {

    @Autowired
    private LogWrapper logWrapper;

    @Autowired
    private UserBehaviorAnalysisService userBehaviorAnalysisService;

    /**
     * 价格服务核心逻辑
     *
     * @param buyerId
     * @param catalogList
     * @param productPriceList
     * @param activityProductList
     * @param isTradeIsolation
     */
    public void calculateRealPriceCoreLogic(int buyerId,
                                            List<Catalog> catalogList,
                                            List<ProductPrice> productPriceList,
                                            List<ActivityProduct> activityProductList,
                                            boolean isTradeIsolation) {
        //填充catalogs
        productPriceList.forEach(productPrice -> {
            List<Catalog> tempCatalogList = catalogList
                    .stream().filter(x -> x.getProductId().equals(productPrice.getProductId()))
                    .collect(Collectors.toList());
            productPrice.setCatalogs(tempCatalogList);
            productPrice.setSellerId(new Long(!tempCatalogList
                    .stream()
                    .findAny()
                    .isPresent() ?
                     tempCatalogList
                    .stream()
                    .findAny()
                    .get()
                    .getSellerId():0L));
        });

        //决定当前买家对不同的买手而言是新客还是老客
        GetBuyerOrderStatisticsResp resp = determineVipOrNewCustomer(buyerId, productPriceList);

        //

        //设置最终商品价格
        decideProductRealPrice(buyerId, productPriceList, activityProductList, resp, isTradeIsolation);
    }

    /**
     * 价格服务核心逻辑（用于新增接口->搜索商品列表）
     *
     * @param buyerId
     * @param productPriceForSearchedList
     * @param activityProductList
     * @param isTradeIsolation
     */
    public void calculateRealPriceCoreLogic(int buyerId,
                                            List<ProductPriceForSearched> productPriceForSearchedList,
                                            List<ActivityProduct> activityProductList,
                                            boolean isTradeIsolation) {
        //决定当前买家对不同的买手而言是新客还是老客
        GetBuyerOrderStatisticsResp resp = determineVipOrNewCustomerForSearched(buyerId, productPriceForSearchedList);

        //设置最终商品价格
        decideProductRealPriceForSearched(buyerId, productPriceForSearchedList, activityProductList, resp, isTradeIsolation);
    }

    /**
     * vip价与新客价的前置检查
     *
     * @param catalogList
     * @return
     */
    private boolean preCheckVipAndNewCustomer(List<Catalog> catalogList) {
        return catalogList.stream().anyMatch(catalog ->
                catalog.getNewCustomerPrice() > 0 || catalog.getVipPrice() > 0
        );
    }

    /**
     * 决定当前买家对不同的买手而言是新客还是老客
     *
     * @param buyerId
     * @param productPriceList
     * @return
     */
    private GetBuyerOrderStatisticsResp determineVipOrNewCustomer(int buyerId, List<ProductPrice> productPriceList) {
        //过滤掉vip和新客价都为0的商品，如果全部都是0则不用查询sellerId也不用调用用户行为服务
        List<ProductPrice> needsCalculateVipAndNewCustomerPriceList = productPriceList.stream().filter(productPrice ->
                preCheckVipAndNewCustomer(productPrice.getCatalogs())
        ).collect(Collectors.toList());
        GetBuyerOrderStatisticsResp resp = null;
        if (!needsCalculateVipAndNewCustomerPriceList.isEmpty()) {

            resp = userBehaviorAnalysisService.getBuyerBehavior(needsCalculateVipAndNewCustomerPriceList
                    .stream()
                    .map(ProductPrice::getSellerId)
                    .collect(Collectors.toList()), buyerId);

            //填充sellerId及对应用户行为信息
            GetBuyerOrderStatisticsResp finalResp = resp;
            productPriceList
                    .forEach(x -> {
                x.setHasConfirmedOrders(
                        (Optional.of(finalResp != null
                                && finalResp.getFromSeller() != null
                                && finalResp.getFromSeller().get(x.getSellerId()) != null
                                && finalResp.getFromSeller().get(x.getSellerId()).isHasConfirmedOrders())
                                .orElse(false))
                );

                x.setNoOrdersOrAllCancelled(
                        (Optional.of(finalResp != null
                                && finalResp.getFromSeller() != null
                                && finalResp.getFromSeller().get(x.getSellerId()) != null
                                && finalResp.getFromSeller().get(x.getSellerId()).isNoOrdersOrAllCancelled())
                                .orElse(false))
                );
            });
        } else {
            productPriceList
                    .forEach(x -> {
                x.setNoOrdersOrAllCancelled(false);
                x.setHasConfirmedOrders(false);
            });
        }
        return resp;
    }

    /**
     * 决定当前买家对不同的买手而言是新客还是老客(用于新增接口->搜索商品列表)
     *
     * @param buyerId
     * @param productPriceForSearchedList
     * @return
     */
    private GetBuyerOrderStatisticsResp determineVipOrNewCustomerForSearched(int buyerId, List<ProductPriceForSearched> productPriceForSearchedList) {
        //过滤掉vip和新客价都为0的商品，如果全部都是0则不用查询sellerId也不用调用用户行为服务
        List<ProductPriceForSearched> needsCalculateVipAndNewCustomerPriceList = productPriceForSearchedList.stream().filter(productPrice ->
                productPrice.getMaxVipPrice() > 0 || productPrice.getMaxNewpersonPrice() > 0).collect(Collectors.toList());
        GetBuyerOrderStatisticsResp resp = null;
        if (needsCalculateVipAndNewCustomerPriceList != null
                && needsCalculateVipAndNewCustomerPriceList.size() > 0) {
            //查询ProductId --> SellerId map
            long[] sellerIdList = needsCalculateVipAndNewCustomerPriceList.stream().mapToLong(x -> x.getSellerId()).toArray();

            resp = userBehaviorAnalysisService.getBuyerBehavior(Longs.asList(sellerIdList), buyerId);

            //填充用户行为信息
            GetBuyerOrderStatisticsResp finalResp = resp;
            productPriceForSearchedList
                    .stream().forEach(x -> {
                x.setHasConfirmedOrders(
                        (Optional.of(finalResp != null
                                && finalResp.getFromSeller() != null
                                && finalResp.getFromSeller().get(x.getSellerId()) != null
                                && finalResp.getFromSeller().get(x.getSellerId()).isHasConfirmedOrders())
                                .orElse(false))
                );

                x.setNoOrdersOrAllCancelled(
                        (Optional.of(finalResp != null
                                && finalResp.getFromSeller() != null
                                && finalResp.getFromSeller().get(x.getSellerId()) != null
                                && finalResp.getFromSeller().get(x.getSellerId()).isNoOrdersOrAllCancelled())
                                .orElse(false))
                );
            });
        } else {
            productPriceForSearchedList
                    .stream().forEach(x -> {
                x.setNoOrdersOrAllCancelled(false);
                x.setHasConfirmedOrders(false);
            });
        }
        return resp;
    }

    /**
     * 检查是否为码头新客
     *
     * @param buyerId
     * @param activityProductInfoList
     * @return
     */
    private boolean checkIsNewBuyer(long buyerId, List<ActivityProduct> activityProductInfoList) {
        activityProductInfoList.removeAll(Collections.singleton(null));
        List<ActivityProduct> newBuyerActivityProductList = activityProductInfoList != null
                && !activityProductInfoList.isEmpty() ?
        activityProductInfoList.stream()
                .filter(ActivityProduct::getNewBuyer)
                .collect(Collectors.toList()) : null;

        boolean isNewBuyerActivityProduct = buyerId > 0
                && !activityProductInfoList.isEmpty()
                && newBuyerActivityProductList != null
                && !newBuyerActivityProductList.isEmpty();
        //如果前置条件都不符合，则没有必要调用用户行为服务
        if (!isNewBuyerActivityProduct) {
            return isNewBuyerActivityProduct;
        }
        GetBuyerFirstOrderInfoReq req = new GetBuyerFirstOrderInfoReq();
        req.setBuyerIds(Lists.newArrayList(buyerId));

        boolean isNewBuyer;
        try {
            GetBuyerFirstOrderInfoResp resp = userBehaviorAnalysisService.getBuyerFirstOrderInfo(buyerId);
            if (resp == null
                    || resp.getFirstOrderInfos() == null
                    || resp.getFirstOrderInfos().get(buyerId) == null) {
                logWrapper.recordErrorLog("用户行为服务_getBuyerFirstOrderInfo接口返回值不正确,response：{}", resp);
            }
            isNewBuyer =
                    resp != null
                            && resp.getFirstOrderInfos() != null
                            && resp.getFirstOrderInfos().get(buyerId) != null
                            && resp.getFirstOrderInfos().get(buyerId).isNewCustomer();

        } catch (Exception ex) {
            logWrapper.recordErrorLog("获取用户特征_发生异常,buyerId:{}", buyerId, ex);
            return false;
        }
        return isNewBuyer;
    }

    /**
     * 决定最终价格
     *
     * @param buyerId
     * @param productPriceList
     * @param activityProductInfoList
     * @param resp
     * @param isTradeIsolation
     */
    private void decideProductRealPrice(long buyerId,
                                        List<ProductPrice> productPriceList,
                                        List<ActivityProduct> activityProductInfoList,
                                        GetBuyerOrderStatisticsResp resp,
                                        boolean isTradeIsolation) {
        boolean isNewBuyer = checkIsNewBuyer(buyerId, activityProductInfoList);

        productPriceList.stream().forEach(productPrice -> {

            ActivityProduct tempActivityProduct = activityProductInfoList.stream()
                    .filter(x -> x.getProductId().equals(productPrice.getProductId()))
                    .findAny().orElse(null);

            productPrice.getCatalogs().forEach(catalog -> {
                PriceEnum tempPriceEnum;
                //活动商品价格逻辑优先级最高
                tempPriceEnum = decideActivityPriceAsRealPriceLogic(isNewBuyer,
                        isTradeIsolation,
                        catalog,
                        tempActivityProduct);
                if (tempPriceEnum != null) return;

                //访客价格逻辑
                tempPriceEnum = decideVistorPriceAsRealPriceLogic(buyerId, resp, catalog);
                if (tempPriceEnum != null) return;

                //vip价格逻辑
                tempPriceEnum = decideVipPriceAsRealPriceLogic(productPrice.getSellerId(), resp, catalog);
                if (tempPriceEnum != null) return;

                //直播新客价格逻辑
                tempPriceEnum = decideNewCustomerPriceAsRealPriceLogic(productPrice.getSellerId(), resp, catalog);
                if (tempPriceEnum != null) return;

                //原价价格逻辑
                tempPriceEnum = decideQuotePriceAsRealPriceLogic(catalog);
            });
        });
    }

    /**
     * 决定最终价格(用于新增接口->搜索商品列表)
     *
     * @param buyerId
     * @param productPriceForSearchedList
     * @param activityProductInfoList
     * @param resp
     * @param isTradeIsolation
     */
    private void decideProductRealPriceForSearched(long buyerId,
                                                   List<ProductPriceForSearched> productPriceForSearchedList,
                                                   List<ActivityProduct> activityProductInfoList,
                                                   GetBuyerOrderStatisticsResp resp,
                                                   boolean isTradeIsolation) {
        boolean isNewBuyer = checkIsNewBuyer(buyerId, activityProductInfoList);

        productPriceForSearchedList.stream().forEach(productPrice -> {
            ActivityProduct tempActivityProduct = activityProductInfoList.stream()
                    .filter(x -> x.getProductId().equals(productPrice.getProductId()))
                    .findAny().orElse(null);

            productPriceForSearchedList.stream().forEach(productPriceForSearched -> {
                PriceEnum priceEnum;

                //活动商品价格逻辑(优先级最高)
                priceEnum = decideActivityPriceAsRealPriceLogic(isNewBuyer,
                        isTradeIsolation,
                        productPriceForSearched,
                        tempActivityProduct);
                if (priceEnum != null) return;

                //访客价格逻辑
                priceEnum = decideVistorPriceAsRealPriceLogic(buyerId, resp, productPriceForSearched);
                if (priceEnum != null) return;

                //vip价格逻辑
                priceEnum = decideVipPriceAsRealPriceLogic(productPrice.getSellerId(), resp, productPriceForSearched);
                if (priceEnum != null) return;

                //直播新客价格逻辑
                priceEnum = decideNewCustomerPriceAsRealPriceLogic(productPrice.getSellerId(), resp, productPriceForSearched);
                if (priceEnum != null) return;

                //原价价格逻辑
                priceEnum = decideQuotePriceAsRealPriceLogic(productPriceForSearched);
            });
        });
    }

    /**
     * 决定活动价格是否作为最终价格
     * 活动商品价格逻辑优先级最高
     *
     * @param isNewBuyer
     * @param isTradeIsolation
     * @param catalog
     * @param activityProductInfo
     */
    private PriceEnum decideActivityPriceAsRealPriceLogic(boolean isNewBuyer,
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
                && activityCatalog.getActivityStock() > 0
                ) {
            //新人活动
            if (activityProductInfo.getNewBuyer()
                    && isNewBuyer) {
                catalog.setActivityPrice(activityCatalog.getActivityCatalogPrice());
                catalog.setPrice(catalog.getActivityPrice());
                catalog.setPriceType(PriceEnum.YMTACTIVITYPRICE.getCode());
                return PriceEnum.YMTACTIVITYPRICE;
            } else if (activityProductInfo.getNewBuyer()
                    && !isNewBuyer) {
                return null;
            }
            catalog.setActivityPrice(activityCatalog.getActivityCatalogPrice());
            catalog.setPrice(catalog.getActivityPrice());
            catalog.setPriceType(PriceEnum.YMTACTIVITYPRICE.getCode());
            return PriceEnum.YMTACTIVITYPRICE;
        }
        return null;
    }

    /**
     * 是否需要计算活动价格(用于新增接口->搜索商品列表)
     *
     * @param isNewBuyer
     * @param isTradeIsolation
     * @param productPriceForSearched
     * @param activityProductInfo
     * @return
     */
    private PriceEnum decideActivityPriceAsRealPriceLogic(boolean isNewBuyer,
                                                          boolean isTradeIsolation,
                                                          ProductPriceForSearched productPriceForSearched,
                                                          ActivityProduct activityProductInfo) {
        /**
         * 是否需要计算活动价格
         */
        boolean needsCalculateActivityProductPrice = activityProductInfo != null
                && (!activityProductInfo.getHasIsolation()
                || isTradeIsolation);
        List<ActivityCatalog> activityProductCatalogList = activityProductInfo.getActivityCatalogList();

        //过滤掉无效的商品规格列表
        List<ActivityCatalog> validActivityProductCatalogList = activityProductCatalogList
                .stream()
                .filter(catalog ->
                        catalog.getActivityStock() > 0
                                && catalog.getActivityCatalogPrice() > 0).collect(Collectors.toList());

        if (needsCalculateActivityProductPrice
                && validActivityProductCatalogList != null
                && !validActivityProductCatalogList.isEmpty()
                ) {

            //最高活动商品规格活动价
            double maxActivityPrice = validActivityProductCatalogList
                    .stream()
                    .max((x, y) -> Double.compare(x.getActivityCatalogPrice(), y.getActivityCatalogPrice()))
                    .get()
                    .getActivityCatalogPrice();

            //最低活动商品规格活动价
            double minActivityPrice = validActivityProductCatalogList
                    .stream()
                    .min((x, y) -> Double.compare(x.getActivityCatalogPrice(), y.getActivityCatalogPrice()))
                    .get()
                    .getActivityCatalogPrice();

            //新人活动
            if (activityProductInfo.getNewBuyer()
                    && isNewBuyer) {
                productPriceForSearched.setMaxPrice(maxActivityPrice);
                productPriceForSearched.setMinPrice(minActivityPrice);
                productPriceForSearched.setPriceType(PriceEnum.YMTACTIVITYPRICE.getCode());
                return PriceEnum.YMTACTIVITYPRICE;
            } else if (activityProductInfo.getNewBuyer()
                    && !isNewBuyer) {
                return null;
            }
            productPriceForSearched.setMaxPrice(maxActivityPrice);
            productPriceForSearched.setMinPrice(minActivityPrice);
            productPriceForSearched.setPriceType(PriceEnum.YMTACTIVITYPRICE.getCode());
            return PriceEnum.YMTACTIVITYPRICE;
        }
        return null;
    }

    /**
     * 决定访客价格是否作为最终价格
     *
     * @param buyerId
     * @param resp
     * @param catalog
     */
    private PriceEnum decideVistorPriceAsRealPriceLogic(Long buyerId, GetBuyerOrderStatisticsResp resp, Catalog catalog) {
        //买家未登录记录日志
        if (buyerId <= 0) {
            logWrapper.recordInfoLog("buyerId <=0");
        }
        if (buyerId <= 0
                && (resp == null || resp.getFromSeller() == null)
                && catalog != null) {
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
            return entry.getKey();
        }
        return null;
    }

    /**
     * 决定访客价格是否作为最终价格(用于新增接口->搜索商品列表)
     *
     * @param buyerId
     * @param resp
     * @param productPriceForSearched
     * @return
     */
    private PriceEnum decideVistorPriceAsRealPriceLogic(Long buyerId,
                                                        GetBuyerOrderStatisticsResp resp,
                                                        ProductPriceForSearched productPriceForSearched) {
        //买家未登录记录日志
        if (buyerId <= 0) {
            logWrapper.recordInfoLog("buyerId <=0");
        }
        if (buyerId <= 0
                && (resp == null || resp.getFromSeller() == null)) {
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
            return minEntry.getKey();
        }
        return null;
    }

    /**
     * 决定vip价格是否作为最终价格
     *
     * @param sellerId
     * @param resp
     * @param catalog
     * @return
     */
    private PriceEnum decideVipPriceAsRealPriceLogic(Long sellerId, GetBuyerOrderStatisticsResp resp, Catalog catalog) {
        //买家已经有确认的订单
        if (resp != null
                && resp.getFromSeller().get(sellerId) != null
                && resp.getFromSeller().get(sellerId).isHasConfirmedOrders()
                && catalog.getVipPrice() > 0
                && catalog.getVipPrice() < catalog.getQuotePrice()) {
            catalog.setPrice(catalog.getVipPrice());
            catalog.setPriceType(PriceEnum.VIPPRICE.getCode());
            return PriceEnum.VIPPRICE;
        }
        return null;
    }

    /**
     * 决定vip价格是否作为最终价格(用于新增接口->搜索商品列表)
     *
     * @param sellerId
     * @param resp
     * @param productPriceForSearched
     * @return
     */
    private PriceEnum decideVipPriceAsRealPriceLogic(Long sellerId, GetBuyerOrderStatisticsResp resp, ProductPriceForSearched productPriceForSearched) {
        //买家已经有确认的订单

        if (resp != null
                && resp.getFromSeller().get(sellerId) != null
                && resp.getFromSeller().get(sellerId).isHasConfirmedOrders()
                && (productPriceForSearched.getMinVipPrice() > 0 || productPriceForSearched.getMaxVipPrice() > 0)
                ) {
            productPriceForSearched.setMinPrice(productPriceForSearched.getMinVipPrice() > 0 ?
                    productPriceForSearched.getMinVipPrice() : productPriceForSearched.getMinOriginalPrice());
            productPriceForSearched.setMaxPrice(productPriceForSearched.getMaxVipPrice() > 0 ?
                    productPriceForSearched.getMaxVipPrice() : productPriceForSearched.getMaxOriginalPrice());
            productPriceForSearched.setPriceType(PriceEnum.VIPPRICE.getCode());
            return PriceEnum.VIPPRICE;
        }
        return null;
    }

    /**
     * 决定新客价格是否作为最终价格
     *
     * @param sellerId
     * @param resp
     * @param catalog
     */
    private PriceEnum decideNewCustomerPriceAsRealPriceLogic(Long sellerId, GetBuyerOrderStatisticsResp resp, Catalog catalog) {
        //买家如果没有订单或订单全部取消
        if (resp != null
                && resp.getFromSeller().get(sellerId) != null
                && resp.getFromSeller().get(sellerId).isNoOrdersOrAllCancelled()
                && catalog.getNewCustomerPrice() > 0
                && catalog.getNewCustomerPrice() < catalog.getQuotePrice()) {
            catalog.setPrice(catalog.getNewCustomerPrice());
            catalog.setPriceType(PriceEnum.NEWCUSTOMERPRICE.getCode());
            return PriceEnum.NEWCUSTOMERPRICE;
        }
        return null;
    }

    /**
     * 决定新客价格是否作为最终价格(用于新增接口->搜索商品列表)
     *
     * @param sellerId
     * @param resp
     * @param productPriceForSearched
     * @return
     */
    private PriceEnum decideNewCustomerPriceAsRealPriceLogic(Long sellerId, GetBuyerOrderStatisticsResp resp, ProductPriceForSearched productPriceForSearched) {
        //买家如果没有订单或订单全部取消
        if (resp != null
                && resp.getFromSeller().get(sellerId) != null
                && resp.getFromSeller().get(sellerId).isNoOrdersOrAllCancelled()
                && (productPriceForSearched.getMinNewpersonPrice() > 0 || productPriceForSearched.getMaxNewpersonPrice() > 0)
                ) {
            productPriceForSearched.setMinPrice(productPriceForSearched.getMinNewpersonPrice() > 0 ?
                    productPriceForSearched.getMinNewpersonPrice() : productPriceForSearched.getMinOriginalPrice());
            productPriceForSearched.setMaxPrice(productPriceForSearched.getMaxNewpersonPrice() > 0 ?
                    productPriceForSearched.getMaxNewpersonPrice() : productPriceForSearched.getMaxOriginalPrice());
            productPriceForSearched.setPriceType(PriceEnum.VIPPRICE.getCode());
            return PriceEnum.NEWCUSTOMERPRICE;
        }
        return null;
    }

    /**
     * 决定原价是否作为最终价格
     *
     * @param catalog
     */
    private PriceEnum decideQuotePriceAsRealPriceLogic(Catalog catalog) {
        catalog.setPriceType(PriceEnum.QUOTEPRICE.getCode());
        catalog.setPrice(catalog.getQuotePrice());
        return PriceEnum.QUOTEPRICE;
    }

    /**
     * 决定原价是否作为最终价格(用于新增接口->搜索商品列表)
     *
     * @param productPriceForSearched
     */
    private PriceEnum decideQuotePriceAsRealPriceLogic(ProductPriceForSearched productPriceForSearched) {
        productPriceForSearched.setPriceType(PriceEnum.QUOTEPRICE.getCode());
        productPriceForSearched.setMinPrice(productPriceForSearched.getMinOriginalPrice());
        productPriceForSearched.setMaxPrice(productPriceForSearched.getMaxOriginalPrice());
        return PriceEnum.QUOTEPRICE;
    }
}
