package com.ymatou.productprice.domain.service;

import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import com.ymatou.productprice.domain.model.ActivityProduct;
import com.ymatou.productprice.infrastructure.util.LogWrapper;
import com.ymatou.productprice.intergration.client.UserBehaviorAnalysisService;
import com.ymatou.productprice.model.Catalog;
import com.ymatou.productprice.model.ProductPrice;
import com.ymatou.productprice.model.ProductPriceForSearched;
import com.ymatou.useranalysis.facade.model.req.GetBuyerFirstOrderInfoReq;
import com.ymatou.useranalysis.facade.model.resp.GetBuyerFirstOrderInfoResp;
import com.ymatou.useranalysis.facade.model.resp.GetBuyerOrderStatisticsResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
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

    @Autowired
    private CalcPriceService calcPriceService;

    @Autowired
    private CalcPriceServiceForSearch calcPriceServiceForSearch;

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

            Optional<Catalog> catalogOptional = tempCatalogList
                    .stream()
                    .findAny();

            productPrice.setSellerId(catalogOptional
                    .isPresent() ?
                    catalogOptional
                            .get()
                            .getSellerId() : 0L);
        });

        //决定当前买家对不同的买手而言是新客还是老客
        GetBuyerOrderStatisticsResp resp = determineVipOrNewCustomer(buyerId, productPriceList);

        boolean isNewBuyer = checkIsNewBuyer(buyerId,activityProductList);

        //设置最终商品价格
        calcPriceService.decideProductRealPrice(buyerId,
                productPriceList,
                activityProductList,
                resp,
                isNewBuyer,
                isTradeIsolation);
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

        boolean isNewBuyer = checkIsNewBuyer(buyerId,activityProductList);

        //设置最终商品价格
        calcPriceServiceForSearch.decideProductRealPriceForSearch(buyerId,
                productPriceForSearchedList,
                activityProductList,
                resp,
                isNewBuyer,
                isTradeIsolation);
    }

    /**
     * 检查是否为码头新客
     *
     * @param buyerId
     * @param activityProductInfoList
     * @return
     */
    private boolean checkIsNewBuyer(long buyerId, List<ActivityProduct> activityProductInfoList) {
        List<ActivityProduct> newBuyerActivityProductList =
                activityProductInfoList != null
                        && !activityProductInfoList.isEmpty() ?
                        activityProductInfoList.stream()
                                .filter(ActivityProduct::getNewBuyer)
                                .collect(Collectors.toList()) : null;

        boolean isNewBuyerActivityProduct = buyerId > 0
                && activityProductInfoList != null
                && !activityProductInfoList.isEmpty()
                && newBuyerActivityProductList != null
                && !newBuyerActivityProductList.isEmpty();
        //如果前置条件都不符合，则没有必要调用用户行为服务
        if (!isNewBuyerActivityProduct) {
            return false;
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
}
