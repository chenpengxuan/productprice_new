package com.ymatou.productprice.domain.service;

import com.ymatou.productprice.domain.mongorepo.MongoRepository;
import com.ymatou.productprice.infrastructure.util.LogWrapper;
import com.ymatou.productprice.infrastructure.util.MapUtil;
import com.ymatou.productprice.intergration.client.UserBehaviorAnalysisService;
import com.ymatou.productprice.model.Catalog;
import com.ymatou.productprice.model.PriceEnum;
import com.ymatou.productprice.model.ProductPrice;
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
    private MongoRepository mongoRepository;

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
                                            List<Map<String, Object>> activityProductList,
                                            boolean isTradeIsolation) {
        //填充catalogs
        productPriceList.stream().forEach(productPrice -> productPrice.setCatalogs(catalogList
                .stream().filter(x -> x.getProductId().equals(productPrice.getProductId()))
                .collect(Collectors.toList())));

        //决定当前买家对不同的买手而言是新客还是老客
        GetBuyerOrderStatisticsResp resp = determineVipOrNewCustomer(buyerId, productPriceList);

        //设置最终商品价格
        decideProductRealPrice(buyerId, productPriceList, activityProductList, resp, isTradeIsolation);
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

            //查询ProductId --> SellerId map
            Map<String, Long> tempMap = mongoRepository
                    .getSellerIdListByProductIdList(needsCalculateVipAndNewCustomerPriceList
                            .stream().map(x -> x.getProductId())
                            .collect(Collectors.toList()));

            List<Long> sellerIdList = tempMap.values().stream().collect(Collectors.toList());
            resp = userBehaviorAnalysisService.getBuyerBehavior(sellerIdList, buyerId);

            //填充sellerId及对应用户行为信息
            GetBuyerOrderStatisticsResp finalResp = resp;
            productPriceList
                    .stream().forEach(x -> {

                Long tempSellerId = Optional.ofNullable(tempMap.get(x.getProductId())).orElse(Long.valueOf("0"));
                x.setSellerId(tempSellerId);

                x.setHasConfirmedOrders(
                        (finalResp != null
                                && finalResp.getFromSeller() != null
                                && finalResp.getFromSeller().get(tempSellerId) != null)
                                && finalResp.getFromSeller().get(tempSellerId).isHasConfirmedOrders());

                x.setNoOrdersOrAllCancelled(
                        (finalResp != null
                                && finalResp.getFromSeller() != null
                                && finalResp.getFromSeller().get(tempSellerId) != null)
                                && finalResp.getFromSeller().get(tempSellerId).isNoOrdersOrAllCancelled());
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
    private boolean checkIsNewBuyer(long buyerId, List<Map<String, Object>> activityProductInfoList) {
        List<Map<String, Object>> newBuyerActivityProductList = activityProductInfoList.stream()
                .filter(x -> Optional.ofNullable((Boolean) x.get("nbuyer")).orElse(false))
                .collect(Collectors.toList());
        boolean isNewBuyerActivityProduct = buyerId > 0
                && !activityProductInfoList.isEmpty()
                && newBuyerActivityProductList != null
                && !newBuyerActivityProductList.isEmpty();
        //如果前置条件都不符合，则没有必要调用用户行为服务
        if (!isNewBuyerActivityProduct) {
            return isNewBuyerActivityProduct;
        }
        GetBuyerFirstOrderInfoReq req = new GetBuyerFirstOrderInfoReq();
        req.setBuyerIds(Arrays.asList(buyerId));

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
                                        List<Map<String, Object>> activityProductInfoList,
                                        GetBuyerOrderStatisticsResp resp,
                                        boolean isTradeIsolation) {
        boolean isNewBuyer = checkIsNewBuyer(buyerId, activityProductInfoList);

        productPriceList.stream().forEach(productPrice -> {
            Map<String, Object> tempActivityProduct = activityProductInfoList.stream()
                    .filter(x -> Optional.ofNullable(x.get("spid")).orElse("").equals(productPrice.getProductId()))
                    .findFirst().orElse(Collections.emptyMap());

            productPrice.getCatalogs().stream().forEach(catalog -> {
                PriceEnum tempPriceEnum;
                Map<PriceEnum, Double> priceMap = new HashMap<>();
                priceMap.put(PriceEnum.NEWCUSTOMERPRICE, catalog.getNewCustomerPrice());
                priceMap.put(PriceEnum.QUOTEPRICE, catalog.getQuotePrice());
                priceMap.put(PriceEnum.VIPPRICE, catalog.getVipPrice());

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
                                                          Map<String, Object> activityProductInfo) {
        /**
         * 是否需要计算活动价格
         */
        boolean needsCalculateActivityProductPrice = activityProductInfo != null
                && (!Optional.ofNullable((Boolean) activityProductInfo.get("isolation")).orElse(false)
                || isTradeIsolation);
        Map<String, Object> activityCatalog = ((List<Map<String, Object>>) MapUtil.getMapKeyValueWithDefault(activityProductInfo, "catalogs", new ArrayList<Map<String, Object>>()))
                .stream().filter(x -> Optional.ofNullable(x.get("cid")).orElse("").equals(catalog.getCatalogId()))
                .findFirst().orElse(Collections.emptyMap());

        if (needsCalculateActivityProductPrice
                && activityCatalog != null
                && !activityCatalog.isEmpty()
                && Optional.ofNullable((Double) activityCatalog.get("price")).orElse(0D) > 0
                && Optional.ofNullable((Integer) activityCatalog.get("stock")).orElse(0) > 0
                ) {
            //新人活动
            if (Optional.ofNullable((Boolean) activityProductInfo.get("nbuyer")).orElse(false)
                    && isNewBuyer) {
                catalog.setActivityPrice((double) activityCatalog.get("price"));
                catalog.setPrice(catalog.getActivityPrice());
                catalog.setPriceType(PriceEnum.YMTACTIVITYPRICE.getCode());
                return PriceEnum.YMTACTIVITYPRICE;
            }
            else if(Optional.ofNullable((Boolean) activityProductInfo.get("nbuyer")).orElse(false)
                    && !isNewBuyer){
                return null;
            }
            catalog.setActivityPrice((double) activityCatalog.get("price"));
            catalog.setPrice(catalog.getActivityPrice());
            catalog.setPriceType(PriceEnum.YMTACTIVITYPRICE.getCode());
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
                && (resp == null || resp.getFromSeller() == null)) {
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
     * 决定原价是否作为最终价格
     *
     * @param catalog
     */
    private PriceEnum decideQuotePriceAsRealPriceLogic(Catalog catalog) {
        catalog.setPriceType(PriceEnum.QUOTEPRICE.getCode());
        catalog.setPrice(catalog.getQuotePrice());
        return PriceEnum.QUOTEPRICE;
    }
}
