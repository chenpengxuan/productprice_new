package com.ymatou.productprice.domain;

import com.ymatou.productprice.domain.mongorepo.MongoRepository;
import com.ymatou.productprice.infrastructure.util.LogWrapper;
import com.ymatou.productprice.infrastructure.util.MapUtil;
import com.ymatou.productprice.infrastructure.util.Utils;
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
 * 商品价格服务相关
 * Created by chenpengxuan on 2017/3/2.
 */
@Component
public class PricePriceQueryService {

    @Autowired
    private MongoRepository mongoRepository;

    @Autowired
    private LogWrapper logWrapper;

    @Autowired
    private UserBehaviorAnalysisService userBehaviorAnalysisService;

    /**
     * 根据商品id获取价格信息
     *
     * @param buyerId
     * @param productId
     * @param isTradeIsolation
     * @return
     */
    public ProductPrice getPriceInfoByProductId(int buyerId, String productId, boolean isTradeIsolation) {

        ProductPrice productPrice = new ProductPrice();
        productPrice.setProductId(productId);

        //查询商品规格信息列表
        List<Catalog> catalogList = mongoRepository.getCatalogList(productId);

        if (catalogList == null || catalogList.isEmpty()) {
            logWrapper.recordErrorLog("根据商品id获取价格信息_getPriceInfoByProductId获取商品规格信息列表为空,buyerId为{},productId为{}", buyerId, productId);
            return null;
        }
        productPrice.setCatalogs(catalogList);

        //查询sellerId
        Map<String, Object> tempSellerIdMap = mongoRepository.getSellerIdByProductId(productId);
        long sellerId = tempSellerIdMap.get("sid") != null ? (int) tempSellerIdMap.get("sid") : 0;
        logWrapper.recordDebugLog("根据商品id获取价格信息_getPriceInfoByProductId:sellerId{}", sellerId);

        //查询活动商品信息
        Map<String, Object> activityProductInfo = mongoRepository.getActivityProduct(productId);

        //查询用户特征信息
        //vip 和 新客价 如果为0 则不用调用用户行为服务
        GetBuyerOrderStatisticsResp resp = null;
        if (!catalogList.stream()
                .filter(catalog -> catalog.getVipPrice() > 0 && catalog.getNewCustomerPrice() > 0)
                .collect(Collectors.toList()).isEmpty()) {
            resp = userBehaviorAnalysisService.getBuyerBehavior(Arrays.asList(sellerId), buyerId);
            productPrice.setHasConfirmedOrders(
                    (resp != null
                            && resp.getFromSeller() != null
                            && resp.getFromSeller().get(sellerId) != null)
                            && resp.getFromSeller().get(sellerId).isHasConfirmedOrders());
            productPrice.setNoOrdersOrAllCancelled(
                    (resp != null
                            && resp.getFromSeller() != null
                            && resp.getFromSeller().get(sellerId) != null)
                            && resp.getFromSeller().get(sellerId).isNoOrdersOrAllCancelled());
        }

        //设置商品价格
        decideProductRealPrice(buyerId,sellerId, Arrays.asList(productPrice), Arrays.asList(activityProductInfo), resp, isTradeIsolation);

        return productPrice;
    }

    /**
     * 根据商品id获取价格信息
     *
     * @param buyerId
     * @param productIdList
     * @param isTradeIsolation
     * @return
     */
    public List<ProductPrice> getPriceInfoByProductIdList(int buyerId, List<String> productIdList, boolean isTradeIsolation) {
        List<ProductPrice> productPriceList = productIdList.stream().map(x -> {
            ProductPrice tempProductPrice = new ProductPrice();
            tempProductPrice.setProductId(x);
            return tempProductPrice;
        }).collect(Collectors.toList());
        List<Catalog> catalogList = mongoRepository.getCatalogList(productIdList);
        if (catalogList == null || catalogList.isEmpty()) {
            logWrapper.recordErrorLog("根据商品id列表获取价格信息_getPriceInfoByProductIdList获取商品规格信息列表为空,buyerId为{},productIdList为{}", buyerId, productIdList);
            return null;
        }
        productPriceList.stream().forEach(productPrice -> productPrice.setCatalogs(catalogList
                .stream().filter(x -> x.getProductId().equals(productPrice.getProductId()))
                .collect(Collectors.toList())));
        //过滤掉vip和新客价都为0的商品，如果全部都是0则不用查询sellerId也不用调用用户行为服务
        List<ProductPrice> needsCalculateVipAndNewCustomerPriceList = productPriceList.stream().filter(productPrice ->
            !productPrice.getCatalogs().stream().filter(catalog ->
                catalog.getNewCustomerPrice() > 0 && catalog.getVipPrice() > 0
            ).collect(Collectors.toList()).isEmpty()
        ).collect(Collectors.toList());
        if(!needsCalculateVipAndNewCustomerPriceList.isEmpty()){
            //查询SellerIdList
            List<Long> sellerIdList = mongoRepository.getSellerIdListByProductIdList(needsCalculateVipAndNewCustomerPriceList
                    .stream().map(x -> x.getProductId())
                    .collect(Collectors.toList()));
            GetBuyerOrderStatisticsResp resp = userBehaviorAnalysisService.getBuyerBehavior(sellerIdList,buyerId);

        }
        return productPriceList;
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
        boolean isNewBuyer = buyerId > 0
                && !activityProductInfoList.isEmpty()
                && newBuyerActivityProductList != null
                && !newBuyerActivityProductList.isEmpty();
        //如果前置条件都不符合，则没有必要调用用户行为服务
        if (!isNewBuyer) {
            return isNewBuyer;
        }
        GetBuyerFirstOrderInfoReq req = new GetBuyerFirstOrderInfoReq();
        req.setBuyerIds(Arrays.asList(buyerId));

        try {
            GetBuyerFirstOrderInfoResp resp = userBehaviorAnalysisService.getBuyerFirstOrderInfo(buyerId);
            if (resp == null
                    || resp.getFirstOrderInfos() == null
                    || resp.getFirstOrderInfos().get(buyerId) == null) {
                logWrapper.recordErrorLog("用户行为服务_getBuyerFirstOrderInfo接口返回值不正确,response：{}", resp);
            }
            isNewBuyer = isNewBuyer
                    && resp != null
                    && resp.getFirstOrderInfos() != null
                    && resp.getFirstOrderInfos().get(buyerId) != null
                    && resp.getFirstOrderInfos().get(buyerId).isNewCustomer();
        } catch (Exception ex) {
            isNewBuyer = false;
            logWrapper.recordErrorLog("获取用户特征_发生异常,buyerId:{}", buyerId, ex);
        }
        return isNewBuyer;
    }


    /**
     * 决定最终价格
     * @param buyerId
     * @param sellerId
     * @param productPriceList
     * @param activityProductInfoList
     * @param resp
     * @param isTradeIsolation
     */
    private void decideProductRealPrice(long buyerId,
                                        long sellerId,
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
                tempPriceEnum = decideVipPriceAsRealPriceLogic(sellerId, resp, catalog);
                if (tempPriceEnum != null) return;
                //直播新客价格逻辑
                tempPriceEnum = decideNewCustomerPriceAsRealPriceLogic(sellerId, resp, catalog);
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
    private PriceEnum decideActivityPriceAsRealPriceLogic(boolean isNewBuyer, boolean isTradeIsolation, Catalog catalog, Map<String, Object> activityProductInfo) {
        /**
         * 是否需要计算活动价格
         */
        boolean needsCalculateActivityProductPrice = activityProductInfo != null
                && (!(Optional.ofNullable((Boolean) activityProductInfo.get("isolation")).orElse(false))
                || isTradeIsolation);
        Map<String, Object> activityCatalog = ((List<Map<String, Object>>) MapUtil.getMapKeyValueWithDefault(activityProductInfo, "catalogs", new ArrayList<Map<String, Object>>()))
                .stream().filter(x -> Utils.makeNullDefaultValue((String) x.get("cid"), "").equals(catalog.getCatalogId()))
                .findFirst().orElse(Collections.emptyMap());

        if (needsCalculateActivityProductPrice
                && activityCatalog != null
                && !activityCatalog.isEmpty()
                && (activityCatalog.get("price") != null ? (double) activityCatalog.get("price") : 0) > 0
                && (activityCatalog.get("stock") != null ? (int) activityCatalog.get("stock") : 0) > 0) {
            //新人活动
            if (Utils.makeNullDefaultValue((boolean) activityProductInfo.get("nbuyer"), false)
                    && isNewBuyer) {
                catalog.setActivityPrice((double) activityCatalog.get("price"));
                catalog.setPrice(catalog.getActivityPrice());
                catalog.setPriceType(PriceEnum.YMTACTIVITYPRICE.getCode());
                return PriceEnum.YMTACTIVITYPRICE;
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
