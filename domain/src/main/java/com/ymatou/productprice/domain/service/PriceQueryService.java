package com.ymatou.productprice.domain.service;

import com.ymatou.productprice.domain.mongorepo.MongoRepository;
import com.ymatou.productprice.infrastructure.util.LogWrapper;
import com.ymatou.productprice.model.Catalog;
import com.ymatou.productprice.model.CatalogPrice;
import com.ymatou.productprice.model.ProductPrice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品价格服务相关
 * Created by chenpengxuan on 2017/3/2.
 */
@Component
public class PriceQueryService {

    @Autowired
    private MongoRepository mongoRepository;

    @Autowired
    private LogWrapper logWrapper;

    @Autowired
    private PriceCoreService priceCoreService;

    /**
     * 根据商品id获取价格信息
     *
     * @param buyerId
     * @param productId
     * @param isTradeIsolation
     * @return
     */
    public ProductPrice getPriceInfoByProductId(int buyerId, String productId, boolean isTradeIsolation) {
        //组装商品价格信息
        ProductPrice productPrice = new ProductPrice();
        productPrice.setProductId(productId);

        //查询商品规格信息列表
        List<Catalog> catalogList = mongoRepository.getCatalogListByProduct(productId);

        //查询sellerId
        Map<String, Object> tempSellerIdMap = mongoRepository.getSellerIdByProductId(productId);
        long sellerId = tempSellerIdMap.get("sid") != null ? (int) tempSellerIdMap.get("sid") : 0;
        logWrapper.recordDebugLog("根据商品id获取价格信息_getPriceInfoByProductId:sellerId{}", sellerId);
        productPrice.setSellerId(sellerId);

        //查询活动商品信息
        Map<String, Object> activityProductInfo = mongoRepository.getActivityProduct(productId);

        //价格核心逻辑
        priceCoreService.calculateRealPriceCoreLogic(buyerId, catalogList, Arrays.asList(productPrice), Arrays.asList(activityProductInfo), isTradeIsolation);

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
        //组装商品价格信息列表
        List<ProductPrice> productPriceList = productIdList.stream().map(x -> {
            ProductPrice tempProductPrice = new ProductPrice();
            tempProductPrice.setProductId(x);
            return tempProductPrice;
        }).collect(Collectors.toList());

        //查询所有商品的规格信息
        List<Catalog> catalogList = mongoRepository.getCatalogListByProduct(productIdList.stream().distinct().collect(Collectors.toList()));

        //查询活动商品列表
        List<Map<String, Object>> activityProductList = mongoRepository.getActivityProductList(productIdList);

        //价格核心逻辑
        priceCoreService.calculateRealPriceCoreLogic(buyerId, catalogList, productPriceList, activityProductList, isTradeIsolation);
        return productPriceList;
    }

    /**
     * 根据规格id列表获取价格信息
     *
     * @param buyerId
     * @param catalogIdList
     * @param isTradeIsolation
     * @return
     */
    public List<CatalogPrice> getPriceInfoByCatalogIdList(int buyerId, List<String> catalogIdList, boolean isTradeIsolation) {
        //过滤重复catalogId
        catalogIdList = catalogIdList.stream().distinct().collect(Collectors.toList());

        //获取规格信息
        List<Catalog> catalogList = mongoRepository.getCatalogByCatalogId(catalogIdList);

        //组装商品id列表
        List<String> productIdList = catalogList.stream().map(catalog -> catalog.getProductId()).collect(Collectors.toList());

        //组装商品价格信息列表
        List<ProductPrice> productPriceList = productIdList.stream().map(x -> {
            ProductPrice tempProductPrice = new ProductPrice();
            tempProductPrice.setProductId(x);
            return tempProductPrice;
        }).collect(Collectors.toList());

        //查询活动商品列表
        List<Map<String, Object>> activityProductList = mongoRepository.getActivityProductList(productIdList);

        //价格核心逻辑
        priceCoreService.calculateRealPriceCoreLogic(buyerId, catalogList, productPriceList, activityProductList, isTradeIsolation);

        //组装规格价格信息列表
        List<CatalogPrice> catalogPriceList = new ArrayList<>();
        productPriceList.stream().forEach(productPrice -> {
            List<CatalogPrice> tempCatalogPriceList = productPrice.getCatalogs().stream().map(catalog -> {

                CatalogPrice catalogPrice = new CatalogPrice();
                catalogPrice.setProductId(productPrice.getProductId());
                catalogPrice.setCatalogInfo(catalog);
                catalogPrice.setHasConfirmedOrders(productPrice.getHasConfirmedOrders());
                catalogPrice.setNoOrdersOrAllCancelled(productPrice.getNoOrdersOrAllCancelled());
                return catalogPrice;
            }).collect(Collectors.toList());

            catalogPriceList.addAll(tempCatalogPriceList);
        });
        return catalogPriceList;
    }
}