package com.ymatou.productprice.domain;

import com.ymatou.productprice.domain.mongorepo.MongoRepository;
import com.ymatou.productprice.infrastructure.util.LogWrapper;
import com.ymatou.productprice.infrastructure.util.Utils;
import com.ymatou.productprice.model.Catalog;
import com.ymatou.productprice.model.PriceEnum;
import com.ymatou.productprice.model.ProductPrice;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Resource
    private BuyerOrderStatisticsFacade buyerOrderStatisticsFacade;

    @Resource
    private BuyerFirstOrderFacade buyerFirstOrderFacade;

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
        productPrice.ProductId = productId;

        //查询商品规格信息列表
        List<Catalog> catalogList = mongoRepository.getCatalogList(productId).stream().map(x -> {
            Catalog tempCatalog = new Catalog();
            tempCatalog.CatalogId = x.get("cid") != null ? x.get("cid").toString() : "";
            tempCatalog.EarnestPrice = x.get("earnest") != null ? Utils.doubleFormat((double) x.get("earnest"), 2) : 0.f;
            tempCatalog.QuotePrice = x.get("price") != null ? Utils.doubleFormat((double) x.get("price"), 2) : 0.f;
            tempCatalog.NewCustomerPrice = x.get("newp") != null ? Utils.doubleFormat((double) x.get("newp"), 2) : 0.f;
            tempCatalog.VipPrice = x.get("vip") != null ? Utils.doubleFormat((double) x.get("vip"), 2) : 0.f;
            tempCatalog.SubsidyPrice = 0.f;//活动新人价已经不存在，这里做兼容操作
            return tempCatalog;
        }).collect(Collectors.toList());
        if (catalogList == null || catalogList.isEmpty()) {
            logWrapper.recordErrorLog("根据商品id获取价格信息_getPriceInfoByProductId获取商品规格信息列表为空,buyerId为{},productId为{}", buyerId, productId);
            return null;
        }
        productPrice.Catalogs = catalogList;

        //查询sellerId
        Map<String, Object> tempSellerIdMap = mongoRepository.getSellerIdByProductId(productId);
        long sellerId = tempSellerIdMap.get("sid") != null ? (int)tempSellerIdMap.get("sid") : 0;
        logWrapper.recordDebugLog("根据商品id获取价格信息_getPriceInfoByProductId:sellerId{}", sellerId);

        //查询活动商品信息
        Map<String, Object> activityProductInfo = mongoRepository.getActivityProduct(productId);

        //查询用户特征信息
        GetBuyerOrderStatisticsResp resp = getBuyerBehavior(sellerId, buyerId);
        productPrice.HasConfirmedOrders = (resp != null
                && resp.getFromSeller() != null
                && resp.getFromSeller().get(sellerId) != null) && resp.getFromSeller().get(sellerId).isHasConfirmedOrders();
        productPrice.NoOrdersOrAllCancelled = (resp != null
                && resp.getFromSeller() != null
                && resp.getFromSeller().get(sellerId) != null) && resp.getFromSeller().get(sellerId).isNoOrdersOrAllCancelled();

        //设置商品价格
        decideProductRealPrice(buyerId,productPrice,activityProductInfo,resp,isTradeIsolation);

        return productPrice;
    }

    /**
     * 获取用户特征
     *
     * @param sellerId
     * @param buyerId
     * @return
     */
    private GetBuyerOrderStatisticsResp getBuyerBehavior(long sellerId, int buyerId) {
        if (buyerId <= 0) {
            return null;
        }

        GetBuyerOrderStatisticsReq req = new GetBuyerOrderStatisticsReq();
        req.setBuyerId(buyerId);
        req.setSellerIds(Arrays.asList(sellerId));

        try {
            GetBuyerOrderStatisticsResp resp = buyerOrderStatisticsFacade.getBuyerOrderStatistics(req);
            return resp;
        } catch (Exception ex) {
            logWrapper.recordErrorLog("获取用户特征_getBuyerOrderStatistics发生异常,sellerId:{},buyerId:{}", sellerId, buyerId, ex);
        }
        return null;
    }

    /**
     * 检查是否为码头新客
     *
     * @param buyerId
     * @param activityProductInfo
     * @return
     */
    private boolean checkIsNewBuyer(long buyerId, Map<String, Object> activityProductInfo) {

        boolean isNewBuyer = buyerId > 0
                && !activityProductInfo.isEmpty()
                && activityProductInfo.get("nbuyer") != null
                && (boolean) activityProductInfo.get("nbuyer");

        GetBuyerFirstOrderInfoReq req = new GetBuyerFirstOrderInfoReq();
        req.setBuyerIds(Arrays.asList(buyerId));

        try {
            GetBuyerFirstOrderInfoResp resp = buyerFirstOrderFacade.getBuyerFirstOrderInfo(req);
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
     *
     * @param buyerId
     * @param productPrice
     * @param activityProductInfo
     * @param resp
     * @param isTradeIsolation
     */
    private void decideProductRealPrice(long buyerId,
                                        ProductPrice productPrice,
                                        Map<String, Object> activityProductInfo,
                                        GetBuyerOrderStatisticsResp resp,
                                        boolean isTradeIsolation) {

        boolean isNewBuyer = checkIsNewBuyer(buyerId, activityProductInfo);
        boolean isActivityProduct = activityProductInfo != null && (!(activityProductInfo.get("isolation") != null ? (boolean) activityProductInfo.get("isolation") : false) || isTradeIsolation);

        productPrice.Catalogs.stream().forEach(catalog -> {
            List<Map<String, Object>> activityCatalogList = activityProductInfo != null
                    && activityProductInfo.get("catalogs") != null
                    ? (List<Map<String, Object>>) activityProductInfo.get("catalogs") : null;
            Map<String, Object> activityCatalog = activityCatalogList != null
                    ? activityCatalogList.stream().filter(x -> x.get("cid") != null
                    && x.get("cid").toString().equals(catalog.CatalogId)).findFirst().orElse(null) : null;
            catalog.ActivityPrice = 0;

            //活动商品价格逻辑优先级最高
            if (isActivityProduct
                    && activityProductInfo != null
                    && activityCatalog != null
                    && !activityCatalog.isEmpty()
                    && (activityCatalog.get("price") != null ? (double) activityCatalog.get("price") : 0) > 0
                    && (activityCatalog.get("stock") != null ? (int) activityCatalog.get("stock") : 0) > 0) {
                //新人活动
                if (activityCatalog.get("nbuyer") != null
                        && (boolean) activityCatalog.get("nbuyer")
                        && isNewBuyer) {
                    catalog.ActivityPrice = (double) activityCatalog.get("price");
                    catalog.Price = catalog.ActivityPrice;
                    catalog.PriceType = PriceEnum.YMTACTIVITYPRICE.getCode();
                    return;
                }
                catalog.ActivityPrice = (double) activityCatalog.get("price");
                catalog.Price = catalog.ActivityPrice;
                catalog.PriceType = PriceEnum.YMTACTIVITYPRICE.getCode();
                return;
            }

            //买家未登录或者用户行为为空
            if (buyerId <= 0
                    && (resp == null || resp.getFromSeller() == null)) {
                Map<PriceEnum, Double> priceMap = new HashMap<>();
                priceMap.put(PriceEnum.NEWCUSTOMERPRICE, catalog.NewCustomerPrice);
                priceMap.put(PriceEnum.QUOTEPRICE, catalog.QuotePrice);
                priceMap.put(PriceEnum.VIPPRICE, catalog.VipPrice);
                Map.Entry<PriceEnum, Double> entry = priceMap.entrySet().stream().min((x, y) -> Double.compare(x.getValue().doubleValue(), y.getValue().doubleValue())).get();
                catalog.Price = entry.getValue();
                catalog.PriceType = entry.getKey().getCode();
                return;
            }

            //买家已经有确认的订单
            if (resp != null
                    && resp.getFromSeller().get(buyerId) != null
                    && resp.getFromSeller().get(buyerId).isHasConfirmedOrders()
                    && catalog.VipPrice > 0
                    && catalog.VipPrice < catalog.QuotePrice) {
                catalog.Price = catalog.VipPrice;
                catalog.PriceType = PriceEnum.VIPPRICE.getCode();
                return;
            }

            //买家如果没有订单或订单全部取消
            if (resp != null
                    && resp.getFromSeller().get(buyerId) != null
                    && resp.getFromSeller().get(buyerId).isNoOrdersOrAllCancelled()
                    && catalog.NewCustomerPrice > 0
                    && catalog.NewCustomerPrice < catalog.QuotePrice) {
                catalog.Price = catalog.NewCustomerPrice;
                catalog.PriceType = PriceEnum.NEWCUSTOMERPRICE.getCode();
                return;
            }

            catalog.PriceType = PriceEnum.QUOTEPRICE.getCode();
            catalog.Price = catalog.QuotePrice;
        });
    }
}
