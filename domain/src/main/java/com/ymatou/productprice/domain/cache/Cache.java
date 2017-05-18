package com.ymatou.productprice.domain.cache;

import com.google.common.cache.CacheStats;
import com.google.common.collect.Maps;
import com.google.common.primitives.Longs;
import com.ymatou.productprice.domain.model.ActivityProduct;
import com.ymatou.productprice.domain.model.Catalog;
import com.ymatou.productprice.domain.model.ProductPriceData;
import com.ymatou.productprice.domain.repo.Repository;
import com.ymatou.productprice.infrastructure.config.props.BizProps;
import com.ymatou.productprice.infrastructure.config.props.CacheProps;
import com.ymatou.productprice.infrastructure.util.CacheUtil.CacheManager;
import com.ymatou.productprice.infrastructure.util.LogWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * 缓存 数据操作相关
 * Created by chenpengxuan on 2017/3/22.
 */
@Component
public class Cache {
    /**
     * 缓存工具类
     */
    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private BizProps bizProps;

    @Autowired
    private CacheProps cacheProps;

    @Resource(name = "mongoRepository")
    private Repository mongoRepository;

    @Resource(name = "parallelRepository")
    private Repository parallelRepository;

    @Autowired
    private LogWrapper logWrapper;

    private Repository realBusinessRepository;

    @PostConstruct
    public void init() {
        if (bizProps.isUseParallel()) {
            realBusinessRepository = parallelRepository;
        } else {
            realBusinessRepository = mongoRepository;
        }
    }

    /**
     * 获取缓存统计信息
     *
     * @return
     */
    public CacheStats getCacheStats() {
        return cacheManager.getCacheStats();
    }

    /**
     * 获取商品缓存信息
     *
     * @param productIdList
     * @return
     */
    public Map<String, ProductPriceData> getProductCacheList(List<String> productIdList) {
        return cacheManager.get(productIdList);
    }

    /**
     * 获取规格信息列表
     *
     * @param productId
     * @return
     */
    public List<Catalog> getCatalogListByProduct(String productId, Date catalogUpdateTime) throws IllegalArgumentException {
        ProductPriceData tempData = cacheManager.get(
                productId,

                obj -> obj,

                obj -> realBusinessRepository.getCatalogListByProduct(productId),

                (pid, productPriceData) -> {
                    Long catalogUpdateStamp = catalogUpdateTime != null ? catalogUpdateTime.getTime() : 0L;
                    return productPriceData != null
                            && productPriceData.getCatalogList() != null
                            && !productPriceData.getCatalogList().isEmpty()
                            && productPriceData.getCatalogList()
                            .stream()
                            .allMatch(x ->
                                    Longs.compare(x.getUpdateTime() != null
                                            ? x.getUpdateTime().getTime() : 0L, catalogUpdateStamp) == 0);
                },

                ((productPriceData, catalogList) -> {
                    if (productPriceData == null) {
                        if (catalogList != null && !catalogList.isEmpty()) {
                            productPriceData = new ProductPriceData();
                            Optional<Catalog> catalogOptional = catalogList.stream().findAny();
                            productPriceData.setSellerId(catalogOptional.isPresent() ?
                                    catalogOptional.get().getSellerId() : 0);
                            productPriceData.setCatalogList(catalogList);
                        } else {
                            throw new IllegalArgumentException("catalog不能为空,ProductId为:" + productId);
                        }
                    } else {
                        productPriceData.setCatalogList(catalogList);
                    }
                    return productPriceData;
                })
        );
        return tempData != null ? tempData.getCatalogList() : null;
    }

    /**
     * 创建新缓存数据
     *
     * @param catalogList
     */
    private void createNewCacheData(List<Catalog> catalogList) {
        //将从数据库取到的最新数据刷新缓存结构
        Map<String, List<Catalog>> cacheGroup = catalogList
                .stream()
                .collect(Collectors.groupingBy(Catalog::getProductId));
        Map<String, ProductPriceData> cacheMap = Maps.transformValues(cacheGroup, v -> {
            ProductPriceData tempData = new ProductPriceData();
            Catalog tempCatalog = v != null && !v.isEmpty() ? v.stream().findAny().get() : null;
            tempData.setProductId(tempCatalog != null ? tempCatalog.getProductId() : String.valueOf(""));
            tempData.setCatalogList(v);
            return tempData;
        });

        //批量刷缓存
        cacheManager.put(cacheMap);
    }

    /**
     * 过滤有效商品缓存数据
     *
     * @param productPriceDataList
     * @param catalogUpdateTimeMap
     */
    private List<ProductPriceData> filterValidCacheData(List<ProductPriceData> productPriceDataList,
                                                        Map<String, Date> catalogUpdateTimeMap) {
        //过滤有效的业务数据
        return productPriceDataList
                .stream()
                .filter(p -> {
                    //缓存中规格更新时间戳
                    //默认值设置为-1为了排除规格时间戳为空 mongo中的时间戳也为空 返回相等的情况
                    //由于时间戳by商品维度 当商品中任意规格发生变化 规格时间戳都会发生变化 只取一个规格进行比较即可
                    Optional<Catalog> catalogOptional = p.getCatalogList() != null ?
                            p.getCatalogList().stream().findAny() : null;

                    Catalog tempCatalog = catalogOptional != null && catalogOptional.isPresent()
                            ? catalogOptional.get() : null;

                    Long cacheCatalogUpdateStamp = tempCatalog != null && tempCatalog.getUpdateTime() != null
                            ? tempCatalog.getUpdateTime().getTime() : -1L;

                    //时间戳表中规格更新时间戳
                    Long catalogUpdateStamp = catalogUpdateTimeMap != null
                            && catalogUpdateTimeMap.get(p.getProductId()) != null
                            ? catalogUpdateTimeMap.get(p.getProductId()).getTime() : 0L;

                    //时间戳比较，不相等则表示发生变更，业务数据需要重新到数据库拉取
                    return Long.compare(cacheCatalogUpdateStamp, catalogUpdateStamp) == 0;
                })
                .collect(Collectors.toList());
    }

    /**
     * 更新缓存数据
     *
     * @param reloadCatalogGroup
     */
    private void updateCacheData(List<ProductPriceData> cacheProductList,
                                 Map<String, List<Catalog>> reloadCatalogGroup) {
        //创建批量刷缓存的结构
        Map<String, ProductPriceData> batchRefreshCacheMap = new HashMap<>();

        reloadCatalogGroup.entrySet()
                .forEach(x -> {
                    ProductPriceData tempData = cacheProductList != null
                            ? cacheProductList
                            .stream()
                            .filter(xx -> Optional.ofNullable(xx.getProductId()).orElse("").equals(x.getKey()))
                            .findAny()
                            .orElse(null) : null;

                    //针对没有命中的数据，构建缓存结构
                    if (tempData == null) {
                        tempData = new ProductPriceData();
                        tempData.setProductId(x.getKey());
                    }
                    //不管业务数据过期还是缓存没有命中，都需要重新设置数据
                    tempData.setCatalogList(x.getValue());

                    //填充批量缓存的数据
                    batchRefreshCacheMap.put(x.getKey(), tempData);
                });

        //根据批量缓存结构进行批量缓存
        cacheManager.put(batchRefreshCacheMap);
    }

    /**
     * 处理并组装商品缓存数据
     *
     * @param productIdList
     * @param cacheProductList
     * @param catalogUpdateTimeMap
     * @return
     */
    private List<Catalog> processProductPriceDataCacheList(List<String> productIdList,
                                                           List<ProductPriceData> cacheProductList,
                                                           Map<String, Date> catalogUpdateTimeMap) {
        List<Catalog> result;

        //缓存全部没有命中的情况
        if (cacheProductList == null || cacheProductList.isEmpty()) {
            //从数据库中获取数据
            List<Catalog> catalogList = realBusinessRepository.getCatalogListByProduct(productIdList);

            result = catalogList;

            //设置缓存
            createNewCacheData(catalogList);

            return result;
        } else {

            //过滤有效业务缓存数据
            List<ProductPriceData> validProductList = filterValidCacheData(cacheProductList, catalogUpdateTimeMap);

            //组装有效缓存规格数据
            List<Catalog> validCatalogList = new ArrayList<>();
            validProductList.forEach(x -> validCatalogList.addAll(x.getCatalogList()));

            //获取有效缓存ProductId
            List<String> validProductIdList = validProductList
                    .stream()
                    .map(ProductPriceData::getProductId)
                    .distinct()
                    .collect(Collectors.toList());

            //组装有效数据
            result = new ArrayList<>();
            result.addAll(validCatalogList);

            //传入的productId与有效数据对应的productId列表的差集就是需要重新拉取的数据
            List<String> needReloadProductIdList = new ArrayList<>();

            needReloadProductIdList.addAll(productIdList);
            needReloadProductIdList.removeAll(validProductIdList);

            if (!needReloadProductIdList.isEmpty()) {
                //需要重新刷缓存的数据
                List<Catalog> reloadCatalogList = realBusinessRepository.getCatalogListByProduct(needReloadProductIdList);

                if (reloadCatalogList != null && !reloadCatalogList.isEmpty()) {
                    reloadCatalogList.removeAll(Collections.singleton(null));
                    Map<String, List<Catalog>> reloadCatalogGroup = reloadCatalogList
                            .stream()
                            .collect(Collectors.groupingBy(Catalog::getProductId));

                    //更新缓存
                    updateCacheData(cacheProductList, reloadCatalogGroup);

                    //添加重刷的有效数据
                    result.addAll(reloadCatalogList);
                }
            }

            return result;
        }
    }

    /**
     * 获取规格信息列表
     *
     * @param productIdList
     * @return
     */
    public List<Catalog> getCatalogListByProduct(List<String> productIdList,
                                                 Map<String, Date> catalogUpdateTimeMap) {
        //过滤重复商品id
        productIdList = productIdList
                .stream()
                .distinct()
                .collect(Collectors.toList());

        //根据商品id列表获取缓存信息
        List<ProductPriceData> cacheProductList = cacheManager.get(productIdList).values()
                .stream()
                .map(x -> (ProductPriceData) x)
                .collect(Collectors.toList());

        return processProductPriceDataCacheList(productIdList, cacheProductList, catalogUpdateTimeMap);
    }

    /**
     * 根据规格id获取规格信息列表
     *
     * @param mapList
     * @return
     */
    public List<Catalog> getCatalogByCatalogId(List<Map<String, Object>> mapList,
                                               Map<String, Date> catalogUpdateTimeMap) {
        //组装商品id列表
        List<String> productIdList = mapList
                .stream()
                .map(x -> x.get("spid").toString())
                .distinct()
                .collect(Collectors.toList());

        //根据商品id列表获取缓存信息
        List<ProductPriceData> cacheProductList = cacheManager.get(productIdList).values()
                .stream()
                .map(x -> (ProductPriceData) x)
                .collect(Collectors.toList());

        //过滤有效业务缓存数据
        List<ProductPriceData> validProductList = filterValidCacheData(cacheProductList, catalogUpdateTimeMap);

        //组装全部catalogId
        List<String> catalogIdList = mapList
                .stream()
                .map(x -> x.get("cid").toString())
                .distinct()
                .collect(Collectors.toList());

        //组装有效的catalogId与catalog
        List<String> validCatalogIdList = new ArrayList<>();
        List<Catalog> validCatalogList = new ArrayList<>();
        validProductList.forEach(p -> p.getCatalogList().forEach(c -> {
            if (catalogIdList.contains(c.getCatalogId())) {
                validCatalogIdList.add(c.getCatalogId());
                validCatalogList.add(c);
            }
        }));

        //需要重新取mongo取的catalogIdList
        List<String> needReloadCatalogIdList = new ArrayList<>();
        needReloadCatalogIdList.addAll(catalogIdList);
        needReloadCatalogIdList.removeAll(validCatalogIdList);

        List<Catalog> resultList = new ArrayList<>();

        //如果有未命中或者无效的规格数据,则重新取mongo中取
        if (!needReloadCatalogIdList.isEmpty()) {
            resultList.addAll(realBusinessRepository.getCatalogByCatalogId(needReloadCatalogIdList));
        }

        resultList.addAll(validCatalogList);

        return resultList;
    }

    /**
     * 初始化活动商品缓存
     */
    public int initActivityProductCache() {
        List<ActivityProduct> activityProductList = realBusinessRepository.getAllValidActivityProductList();
        if (activityProductList != null && !activityProductList.isEmpty()) {

            Map activityProductMap = activityProductList.stream().collect(Collectors.groupingBy(ActivityProduct::getProductId));

            cacheManager.putActivityProduct(activityProductMap);
        }
        return activityProductList.size();
    }

    /**
     * 添加活动商品增量信息
     */
    public void addNewestActivityProductCache() {
        ConcurrentMap activityProductCache = cacheManager.getActivityProductCacheContainer();

        List<Integer> cacheInActivityIdList = new ArrayList<>();

        activityProductCache.values().forEach(z ->
                ((List<ActivityProduct>) z).forEach(x -> cacheInActivityIdList.add(x.getProductInActivityId())));

        List<Integer> validInActivityIdList = realBusinessRepository.getValidProductInActivityIdList();

        List<Integer> needReloadInActivityIdList = new ArrayList<>();
        needReloadInActivityIdList.addAll(validInActivityIdList);
        needReloadInActivityIdList.removeAll(cacheInActivityIdList);

        //获取新增的mongo活动商品信息
        List<ActivityProduct> newestActivityProductList = realBusinessRepository
                .getActivityProductListByInActivityIdList(needReloadInActivityIdList);

        if (newestActivityProductList != null && !newestActivityProductList.isEmpty()) {
            List<String> productIdList = newestActivityProductList.stream().map(ActivityProduct::getProductId).collect(Collectors.toList());

            List<ActivityProduct> cacheActivityProductList = new ArrayList<>();
            productIdList.forEach(x -> {
                if (activityProductCache.get(x) != null) {
                    cacheActivityProductList.addAll((List) activityProductCache.get(x));
                }
            });

            cacheActivityProductList.addAll(newestActivityProductList);

            Map tempMap = cacheActivityProductList.stream().collect(Collectors.groupingBy(ActivityProduct::getProductId));

            //批量添加至缓存
            cacheManager.putActivityProduct(tempMap);

            logWrapper.recordInfoLog("增量添加活动商品缓存已执行,新增{}条", newestActivityProductList.size());
        }
    }

    /**
     * 获取活动商品信息
     *
     * @param productId
     * @param activityProductUpdateTime
     * @return
     */
    public ActivityProduct getActivityProduct(String productId, Date activityProductUpdateTime) {
        List<ActivityProduct> cacheActivityList;
        ActivityProduct result;
        String cacheKey = productId;
        //先从缓存中取
        cacheActivityList = cacheManager.getActivityProduct(cacheKey);

        //如果缓存中没有命中，则认为此商品不是活动商品
        if (cacheActivityList != null && !cacheActivityList.isEmpty()) {
            Map tempUpdateTimeMap = new HashMap();
            tempUpdateTimeMap.put(productId,activityProductUpdateTime);
            List<ActivityProduct> tempResultList = processCacheActivityProduct(cacheActivityList, tempUpdateTimeMap);
            return tempResultList.stream().filter(x -> {
                Long startTime = x.getStartTime().getTime();
                Long endTime = x.getEndTime().getTime();
                Long now = new Date().getTime();
                return now >= startTime && now <= endTime;
            }).findAny().orElse(null);
        } else {
            if (cacheManager.getActivityProductCacheContainer().size() < cacheProps.getActivityProductCacheSize()) {
                return null;
            } else {
                result = realBusinessRepository.getActivityProduct(productId);
                logWrapper.recordErrorLog("活动商品缓存size需要扩容，超出容量的活动商品已改为从mongo查询，不影响正常业务");
            }
        }
        return result;
    }

    /**
     * 缓存活动商品数据处理逻辑
     *
     * @param activityProductList
     * @return
     */
    private List<ActivityProduct> processCacheActivityProduct(List<ActivityProduct> activityProductList, Map<String,Date> activityProductUpdateTimeMap) {
        if(activityProductList != null && !activityProductList.isEmpty()){
           List<ActivityProduct> validProductList = activityProductList.stream().filter(ap -> checkValidActivityProduct(ap,activityProductUpdateTimeMap.get(ap.getProductId())))
                   .collect(Collectors.toList());

            List<String> activityProductIdList = activityProductList.stream().map(ActivityProduct::getProductId).collect(Collectors.toList());
            List<String> validProductIdList = validProductList.stream().map(ActivityProduct::getProductId).collect(Collectors.toList());

            List<String> needReloadActivityProductIdList = new ArrayList<>();
            needReloadActivityProductIdList.addAll(activityProductIdList);
            needReloadActivityProductIdList.removeAll(validProductIdList);

            List<ActivityProduct> cacheActivityProductList = new ArrayList<>();
            cacheActivityProductList.addAll(validProductList);

            if(!needReloadActivityProductIdList.isEmpty()){
                List<ActivityProduct> reloadActivityProductList = realBusinessRepository.getActivityProductList(needReloadActivityProductIdList);
                List<String> reloadActivityProductIdList = reloadActivityProductList.stream().map(ActivityProduct::getProductId).collect(Collectors.toList());

                List<List<ActivityProduct>> tempCacheActivityProductList = cacheManager.getActivityProduct(reloadActivityProductIdList);


                if(tempCacheActivityProductList != null && !tempCacheActivityProductList.isEmpty()){
                    tempCacheActivityProductList.forEach(x -> cacheActivityProductList.addAll(x));
                }
                cacheActivityProductList.addAll(reloadActivityProductList);

                Map tempCacheMap = cacheActivityProductList.stream().collect(Collectors.groupingBy(ActivityProduct::getProductId));

                cacheManager.putActivityProduct(tempCacheMap);
            }
            return cacheActivityProductList;
         }
        return null;
    }

    /**
     * 活动商品有效性检查
     * @param activityProduct
     * @param activityProductUpdateTime
     * @return
     */
    private boolean checkValidActivityProduct(ActivityProduct activityProduct, Date activityProductUpdateTime){
        Long startTime = activityProduct.getStartTime().getTime();
        Long endTime = activityProduct.getEndTime().getTime();
        Long now = new Date().getTime();
        Long updateStamp = activityProductUpdateTime != null ? activityProductUpdateTime.getTime() : 0L;
        Long activityProductStamp = activityProduct.getUpdateTime() != null
                ? activityProduct.getUpdateTime().getTime() : -1L;
        //当活动商品发生变更时，有可能从mongo中根据限定条件取出来是空，所以先把productId取出来
        String activityProductId = activityProduct.getProductId();
        if (Long.compare(activityProductStamp, updateStamp) != 0) {
           return false;
        }

        //过期的活动商品
        if (now > endTime) {
            cacheManager.deleteActivityProduct(activityProductId);
            return false;
        }
        //活动商品数据发生变化，取数据重新刷缓存
        else if (now < startTime) {
            return false;
        }
        return true;
    }

    /**
     * 获取活动商品信息列表
     *
     * @param productIdList
     * @return
     */
    public List<ActivityProduct> getActivityProductList(List<String> productIdList,
                                                        Map<String, Date> activityProductStampMap) {
        productIdList = productIdList
                .stream()
                .distinct()
                .collect(Collectors.toList());

        //从缓存中获取数据
        List<List<ActivityProduct>> cacheList = cacheManager.getActivityProduct(productIdList);
        if (cacheList != null && !cacheList.isEmpty()) {
            //针对Lists.newArrayList创建的列表 排除空元素
            cacheList.removeAll(Collections.singleton(null));
        }

        List<ActivityProduct> cacheActivityList = new ArrayList<>();
        List<ActivityProduct> finalCacheActivityList = cacheActivityList;
        cacheList.forEach(x -> finalCacheActivityList.addAll(x));

        //如果缓存为空 则认为都不是活动商品
        if (finalCacheActivityList == null || finalCacheActivityList.isEmpty()) {
            //如果缓存为空，但是缓存容器没有满的情况下则认为不是活动商品
            if (cacheManager.getActivityProductCacheContainer().size() < cacheProps.getActivityProductCacheSize()) {
                return null;
            } else {
                cacheActivityList = realBusinessRepository.getActivityProductList(productIdList);
                logWrapper.recordErrorLog("活动商品缓存size需要扩容，超出容量的活动商品已改为从mongo查询，不影响正常业务");
                return cacheActivityList;
            }
        } else {
            return processCacheActivityProduct(finalCacheActivityList,activityProductStampMap);
        }
    }

    /**
     * 根据商品id列表获取价格边界信息（用于新增接口->搜索商品列表）
     *
     * @param productIdList
     * @return
     */
    public List<ProductPriceData> getPriceRangeListByProduct(List<String> productIdList,
                                                             Map<String, Date> productUpdateStampMap) {
        List<ProductPriceData> result;

        //从缓存中获取数据
        List<ProductPriceData> cacheProductList = cacheManager.get(productIdList).values()
                .stream()
                .map(x -> (ProductPriceData) x)
                .collect(Collectors.toList());

        if (cacheProductList != null && !cacheProductList.isEmpty()) {
            //针对Lists.newArrayList创建的列表 排除空元素
            cacheProductList.removeAll(Collections.singleton(null));
        }

        //缓存完全不命中
        if (cacheProductList == null || cacheProductList.isEmpty()) {
            result = realBusinessRepository.getPriceRangeListByProduct(productIdList);

            cacheManager.put(result
                    .stream()
                    .collect(Collectors.toMap(ProductPriceData::getProductId, y -> y, (key1, key2) -> key2))
            );
        } else {
            //获取有效的商品缓存数据
            List<ProductPriceData> validProductPriceDataList = cacheProductList
                    .stream()
                    .filter(x -> {
                        Long cacheProductUpdateStamp = x.getUpdateTime() != null ? x.getUpdateTime().getTime() : -1L;
                        Long productUpdateStamp = productUpdateStampMap.get(x.getProductId()) != null ?
                                productUpdateStampMap.get(x.getProductId()).getTime() : 0;
                        return Long.compare(cacheProductUpdateStamp, productUpdateStamp) == 0;
                    })
                    .collect(Collectors.toList());

            List<String> validProductIdList = validProductPriceDataList
                    .stream()
                    .map(ProductPriceData::getProductId)
                    .collect(Collectors.toList());


            //过滤出业务数据过期的商品缓存列表
            List<ProductPriceData> invalidProductPriceDataList = new ArrayList<>();
            invalidProductPriceDataList.addAll(cacheProductList);
            invalidProductPriceDataList.removeAll(validProductPriceDataList);
            invalidProductPriceDataList.removeAll(Collections.singleton(null));

            //组装需要重新取数据库获取的数据
            List<String> needReloadProductIdList = new ArrayList<>();
            needReloadProductIdList.addAll(productIdList);
            needReloadProductIdList.removeAll(validProductIdList);

            List<ProductPriceData> reloadProductList = new ArrayList<>();

            if (needReloadProductIdList != null && !needReloadProductIdList.isEmpty()) {
                reloadProductList = realBusinessRepository
                        .getPriceRangeListByProduct(needReloadProductIdList);
            }

            if (reloadProductList != null && !reloadProductList.isEmpty()) {
                //去除空数据
                reloadProductList.removeAll(Collections.singleton(null));
                //组装需要刷缓存的数据
                reloadProductList.forEach(x -> {
                    //针对缓存结构中 商品数据过期 但是商品中规格数据可能有效的情况，保留其规格缓存数据
                    ProductPriceData invalidProductCacheData = invalidProductPriceDataList
                            .stream()
                            .filter(z -> Optional.ofNullable(z.getProductId()).orElse("").equals(x.getProductId()))
                            .findAny()
                            .orElse(null);

                    if (invalidProductCacheData != null) {
                        x.setCatalogList(invalidProductCacheData.getCatalogList());
                    }
                });

                //批量刷缓存
                cacheManager.put(reloadProductList
                        .stream()
                        .collect(Collectors.toMap(ProductPriceData::getProductId, y -> y, (key1, key2) -> key2)));
            }

            //合并有效数据并返回
            result = new ArrayList<>();
            result.addAll(validProductPriceDataList);
            result.addAll(reloadProductList);
        }
        return result;
    }
}
