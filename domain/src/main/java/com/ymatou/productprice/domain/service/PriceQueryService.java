package com.ymatou.productprice.domain.service;

import com.google.common.primitives.Doubles;
import com.ymatou.productprice.domain.cache.Cache;
import com.ymatou.productprice.domain.model.*;
import com.ymatou.productprice.domain.repo.Repository;
import com.ymatou.productprice.domain.repo.RepositoryProxy;
import com.ymatou.productprice.domain.repo.mongorepo.MongoRepository;
import com.ymatou.productprice.infrastructure.config.props.BizProps;
import com.ymatou.productprice.infrastructure.config.props.CacheProps;
import com.ymatou.productprice.model.*;
import com.ymatou.productprice.model.Catalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品价格服务相关
 * Created by chenpengxuan on 2017/3/2.
 */
@Component
public class PriceQueryService {
    @Autowired
    RepositoryProxy repositoryProxy;

    @Autowired
    private PriceCoreService priceCoreService;

    @Autowired
    private CacheProps cacheProps;

    @Autowired
    private BizProps bizProps;

    @Autowired
    private Cache cache;

    private Repository repository;

    @Autowired
    private MongoRepository mongoRepository;

    @PostConstruct
    public void init() {
        repository = repositoryProxy.getRepository();
    }

    /**
     * 根据商品id获取价格信息
     *
     * @param buyerId
     * @param productId
     * @param isTradeIsolation
     * @return
     */
    public ProductPrice getPriceInfoByProductId(int buyerId,
                                                String productId,
                                                boolean isTradeIsolation) throws BizException {
        //组装商品价格信息
        ProductPrice productPrice = new ProductPrice();
        productPrice.setProductId(productId);

        //获取活动商品与规格的变更时间戳
        Map<String, Object> updateStampMap = mongoRepository
                .getTimeStampByProductId(productId, Arrays.asList("cut", "aut"));

        //如果时间戳为空，则取当前时间
        Date catalogUpdateTime = Optional.ofNullable((Date) updateStampMap.get("cut")).orElse(new Date());

        //查询商品规格信息列表
        List<com.ymatou.productprice.domain.model.Catalog> catalogList;
        if (bizProps.isUseCache()) {
            catalogList = cache.getCatalogListByProduct(productId, catalogUpdateTime);
        } else {
            catalogList = repository.getCatalogListByProduct(productId);
        }

        if (catalogList == null || catalogList.isEmpty()) {
            BizException.throwBizException("商品信息不存在");
        }

        //如果时间戳为空，则取当前时间
        Date activityProductUpdateTime = Optional.ofNullable((Date) updateStampMap.get("aut")).orElse(new Date());

        //查询活动商品信息
        ActivityProduct activityProductInfo;
        if (bizProps.isUseCache() && cacheProps.isUseActivityCache()) {
            activityProductInfo = cache.getActivityProduct(productId, activityProductUpdateTime);
        } else {
            activityProductInfo = repository.getActivityProduct(productId);
        }
        List<Catalog> outputCatalogList = convertCatalogForOutput(catalogList);
        //价格核心逻辑
        priceCoreService.calculateRealPriceCoreLogic(buyerId,
                outputCatalogList,
                Arrays.asList(productPrice),
                Arrays.asList(activityProductInfo),
                isTradeIsolation);

        return productPrice;
    }

    /**
     * 规格结构转换器（用于输出）
     * @param inputCatalogList
     * @return
     */
    private List<Catalog> convertCatalogForOutput(List<com.ymatou.productprice.domain.model.Catalog> inputCatalogList){
        return inputCatalogList
                .stream()
                .map(x -> {
                    Catalog tempOutputCatalog = new Catalog();
                    tempOutputCatalog.setProductId(x.getProductId());
                    tempOutputCatalog.setSellerId(x.getSellerId());
                    tempOutputCatalog.setCatalogId(x.getCatalogId());
                    tempOutputCatalog.setNewCustomerPrice(x.getNewCustomerPrice());
                    tempOutputCatalog.setVipPrice(x.getVipPrice());
                    tempOutputCatalog.setQuotePrice(x.getQuotePrice());
                    return tempOutputCatalog;
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据商品id获取价格信息
     *
     * @param buyerId
     * @param productIdList
     * @param isTradeIsolation
     * @return
     */
    public List<ProductPrice> getPriceInfoByProductIdList(int buyerId,
                                                          List<String> productIdList,
                                                          boolean isTradeIsolation) throws BizException {
        //组装商品价格信息列表
        List<ProductPrice> productPriceList = productIdList.stream().map(x -> {
            ProductPrice tempProductPrice = new ProductPrice();
            tempProductPrice.setProductId(x);
            return tempProductPrice;
        }).collect(Collectors.toList());

        //获取活动商品与规格的变更时间戳
        List<Map<String, Object>> updateStampMapList = mongoRepository
                .getTimeStampByProductIdList(productIdList, Arrays.asList("cut", "aut"));

        Map<String,Date> activityProductUpdateTimeMap = new HashMap<>();
        Map<String,Date> catalogUpdateTimeMap = new HashMap<>();

        //填充活动商品与规格时间戳
        updateStampMapList.forEach(x -> {
            activityProductUpdateTimeMap.put(x.get("spid").toString(),
                Optional.ofNullable((Date)x.get("aut")).orElse(new Date()));
            catalogUpdateTimeMap.put(x.get("spid").toString(),
                    Optional.ofNullable((Date)x.get("cut")).orElse(new Date()));
        });


        //查询所有商品的规格信息
        List<com.ymatou.productprice.domain.model.Catalog> catalogList;
        if (bizProps.isUseCache()) {
            catalogList = cache.getCatalogListByProduct(productIdList, catalogUpdateTimeMap);
        } else {
            catalogList = repository.getCatalogListByProduct(productIdList);
        }

        if (catalogList == null || catalogList.isEmpty()) {
            BizException.throwBizException("商品信息不存在");
        }

        //查询活动商品列表
        List<ActivityProduct> activityProductList;
        if (bizProps.isUseCache() && cacheProps.isUseActivityCache()) {
            activityProductList = cache.getActivityProductList(productIdList, activityProductUpdateTimeMap);
        } else {
            activityProductList = repository.getActivityProductList(productIdList);
        }
        List<Catalog> outputCatalogList = convertCatalogForOutput(catalogList);

        //价格核心逻辑
        priceCoreService.calculateRealPriceCoreLogic(buyerId,
                outputCatalogList,
                productPriceList,
                activityProductList,
                isTradeIsolation);
        return productPriceList;
    }

    /**
     * 根据商品id获取价格信息（用于新增接口->搜索商品列表）
     *
     * @param buyerId
     * @param productIdList
     * @param isTradeIsolation
     * @return
     */
    public List<ProductPriceForSearched> getPriceInfoByProductIdListForSearched(int buyerId,
                                                                                List<String> productIdList,
                                                                                boolean isTradeIsolation) throws BizException {
        //组装商品价格信息列表
        List<ProductPriceForSearched> productPriceList = productIdList.stream().distinct().map(x -> {
            ProductPriceForSearched tempProductPrice = new ProductPriceForSearched();
            tempProductPrice.setProductId(x);
            return tempProductPrice;
        }).collect(Collectors.toList());

        //查询所有商品的价格区间信息并进行组装
        List<ProductPriceData> productList = repository.getPriceRangeListByProduct
                (productIdList.stream().distinct().collect(Collectors.toList()));

        if (productList == null || productList.isEmpty()) {
            BizException.throwBizException("商品信息不存在");
        }

        productList.stream().forEach(x -> {
            ProductPriceForSearched tempPrice = productPriceList
                    .stream()
                    .filter(p -> p.getProductId().equals(x.getProductId()))
                    .findAny()
                    .orElse(null);

            String[] maxPriceList = x.getPriceMaxRange().split(",");
            String[] minPriceList = x.getPriceMinRange().split(",");

            //设置原价区间
            tempPrice.setMinOriginalPrice(Doubles.tryParse(minPriceList[0]));
            tempPrice.setMaxOriginalPrice(Doubles.tryParse(maxPriceList[0]));
            //设置新客价区间
            tempPrice.setMinNewpersonPrice(Doubles.tryParse(minPriceList[1]));
            tempPrice.setMaxNewpersonPrice(Doubles.tryParse(maxPriceList[1]));
            //设置vip价区间
            tempPrice.setMinVipPrice(Doubles.tryParse(minPriceList[2]));
            tempPrice.setMaxVipPrice(Doubles.tryParse(maxPriceList[2]));
            //设置sellerId
            tempPrice.setSellerId(new Long(x.getSellerId()));
        });

        //查询活动商品列表
        List<ActivityProduct> activityProductList = repository.getActivityProductList(productIdList);

        //价格核心逻辑
        priceCoreService.calculateRealPriceCoreLogic(buyerId, productPriceList, activityProductList, isTradeIsolation);
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
    public List<CatalogPrice> getPriceInfoByCatalogIdList(int buyerId, List<String> catalogIdList, boolean isTradeIsolation) throws BizException {
        //过滤重复catalogId
        catalogIdList = catalogIdList.stream().distinct().collect(Collectors.toList());

        //获取规格信息
        List<com.ymatou.productprice.domain.model.Catalog> catalogList = repository.getCatalogByCatalogId(catalogIdList);
        if (catalogList == null || catalogList.isEmpty()) {
            BizException.throwBizException("商品信息不存在");
        }
        //组装商品id列表
        List<String> productIdList = catalogList.stream().map(catalog -> catalog.getProductId()).distinct().collect(Collectors.toList());

        //组装商品价格信息列表
        List<ProductPrice> productPriceList = productIdList.stream().map(x -> {
            ProductPrice tempProductPrice = new ProductPrice();
            tempProductPrice.setProductId(x);
            return tempProductPrice;
        }).collect(Collectors.toList());

        //查询活动商品列表
        List<ActivityProduct> activityProductList = repository.getActivityProductList(productIdList);

        List<Catalog> outputCatalogList = convertCatalogForOutput(catalogList);

        //价格核心逻辑
        priceCoreService.calculateRealPriceCoreLogic(buyerId, outputCatalogList, productPriceList, activityProductList, isTradeIsolation);

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
