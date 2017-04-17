package com.ymatou.productprice.domain.repo.mongorepo;

import com.google.common.collect.Lists;
import com.ymatou.productprice.domain.model.ActivityCatalog;
import com.ymatou.productprice.domain.model.ActivityProduct;
import com.ymatou.productprice.domain.model.Catalog;
import com.ymatou.productprice.domain.model.ProductPriceData;
import com.ymatou.productprice.domain.repo.Repository;
import com.ymatou.productprice.infrastructure.constants.Constants;
import com.ymatou.productprice.infrastructure.dataprocess.mongo.MongoOperationTypeEnum;
import com.ymatou.productprice.infrastructure.dataprocess.mongo.MongoProcessor;
import com.ymatou.productprice.infrastructure.dataprocess.mongo.MongoQueryData;
import com.ymatou.productprice.infrastructure.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * mongo 数据操作相关
 * Created by chenpengxuan on 2017/3/2.
 */
@Service("mongoRepository")
public class MongoRepository implements Repository {
    @Autowired
    private MongoProcessor mongoProcessor;

    /**
     * 根据商品id与时间戳列名获取对应时间戳
     * 用于缓存功能
     *
     * @param productId
     * @param stampKeyList
     * @return
     */
    public Map<String, Object> getTimeStampByProductId(String productId, List<String> stampKeyList) {
        MongoQueryData queryData = new MongoQueryData();

        Map<String, Object> matchConditionMap = new HashMap<>();
        matchConditionMap.put("spid", productId);
        queryData.setMatchCondition(matchConditionMap);

        Map<String, Boolean> projectionMap = new HashMap<>();
        stampKeyList.forEach(key -> projectionMap.put(key, true));
        projectionMap.put("_id", false);
        queryData.setProjection(projectionMap);

        queryData.setTableName(Constants.ProductTimeStampDb);

        queryData.setOperationType(MongoOperationTypeEnum.SELECTSINGLE);
        return mongoProcessor
                .queryMongo(queryData)
                .stream()
                .findAny().orElse(Collections.emptyMap());
    }

    /**
     * 根据商品id列表与时间戳列名获取对应时间戳
     *
     * @param productIdList
     * @param stampKeyList
     * @return
     */
    public List<Map<String, Object>> getTimeStampByProductIdList(List<String> productIdList, List<String> stampKeyList) {
        MongoQueryData queryData = new MongoQueryData();

        Map<String, Object> matchConditionMap = new HashMap<>();
        Map<String, Object> tempMap = new HashMap<>();
        tempMap.put("$in", productIdList);
        matchConditionMap.put("spid", tempMap);
        queryData.setMatchCondition(matchConditionMap);

        Map<String, Boolean> projectionMap = new HashMap<>();
        stampKeyList.forEach(key -> projectionMap.put(key, true));
        projectionMap.put("spid", true);
        projectionMap.put("_id", false);
        queryData.setProjection(projectionMap);

        queryData.setTableName(Constants.ProductTimeStampDb);

        queryData.setOperationType(MongoOperationTypeEnum.SELECTMANY);
        return mongoProcessor
                .queryMongo(queryData)
                .stream()
                .collect(Collectors.toList());
    }

    /**
     * 根据规格id列表获取商品id规格id映射关系
     * @param catalogIdList
     * @return
     */
    public List<Map<String, Object>> getProductIdByCatalogIdList(List<String> catalogIdList) {
        MongoQueryData queryData = new MongoQueryData();

        Map<String, Object> matchConditionMap = new HashMap<>();
        Map<String, Object> tempMap = new HashMap<>();
        tempMap.put("$in", catalogIdList);
        matchConditionMap.put("cid", tempMap);
        queryData.setMatchCondition(matchConditionMap);

        Map<String, Boolean> projectionMap = new HashMap<>();
        projectionMap.put("spid", true);
        projectionMap.put("cid", true);
        projectionMap.put("_id", false);
        queryData.setProjection(projectionMap);

        queryData.setTableName(Constants.CatalogDb);

        queryData.setOperationType(MongoOperationTypeEnum.SELECTMANY);
        return mongoProcessor
                .queryMongo(queryData)
                .stream()
                .collect(Collectors.toList());
    }

    /**
     * 根据商品id列表获取商品id规格id映射关系
     * @param catalogIdList
     * @return
     */
    @Override
    public List<Map<String, Object>> getCatalogIdByProductIdList(List<String> catalogIdList){
        MongoQueryData queryData = new MongoQueryData();

        Map<String, Object> matchConditionMap = new HashMap<>();
        Map<String, Object> tempMap = new HashMap<>();
        tempMap.put("$in", catalogIdList);
        matchConditionMap.put("spid", tempMap);
        queryData.setMatchCondition(matchConditionMap);

        Map<String, Boolean> projectionMap = new HashMap<>();
        projectionMap.put("cid", true);
        projectionMap.put("_id", false);
        queryData.setProjection(projectionMap);

        queryData.setTableName(Constants.CatalogDb);

        queryData.setOperationType(MongoOperationTypeEnum.SELECTMANY);
        return mongoProcessor
                .queryMongo(queryData)
                .stream()
                .collect(Collectors.toList());
    }

    /**
     * 根据商品id列表获取价格边界信息（用于新增接口->搜索商品列表）
     *
     * @param productIdList
     * @return
     */
    public List<ProductPriceData> getPriceRangeListByProduct(List<String> productIdList) {
        MongoQueryData queryData = new MongoQueryData();

        Map<String, Object> matchConditionMap = new HashMap<>();
        Map<String, Object> tempMap = new HashMap<>();
        tempMap.put("$in", productIdList);
        matchConditionMap.put("spid", tempMap);
        queryData.setMatchCondition(matchConditionMap);

        Map<String, Boolean> projectionMap = new HashMap<>();
        projectionMap.put("maxp", true);
        projectionMap.put("minp", true);
        projectionMap.put("sid", true);
        projectionMap.put("spid", true);
        projectionMap.put("_id", false);
        queryData.setProjection(projectionMap);

        queryData.setTableName(Constants.ProductDb);

        queryData.setOperationType(MongoOperationTypeEnum.SELECTMANY);
        List<ProductPriceData> productPriceDataList = mongoProcessor
                .queryMongo(queryData)
                .stream()
                .map(this::convertMapToProductPriceData)
                .collect(Collectors.toList());

        setProductStamp(productPriceDataList);

        return productPriceDataList;
    }

    private void setProductStamp(List<ProductPriceData> productPriceDataList){
        if(productPriceDataList != null && !productPriceDataList.isEmpty()){
            List<String> tempProductIdList = productPriceDataList.stream().map(ProductPriceData::getProductId)
                    .collect(Collectors.toList());

            List<Map<String,Object>> tempStampList = getTimeStampByProductIdList(tempProductIdList,Arrays.asList("sut"));

            productPriceDataList.forEach(x -> {
                Map<String,Object> tempStampMap = tempStampList.stream().filter(z ->
                        Optional.ofNullable(z.get("spid")).orElse("").equals(x.getProductId()))
                        .findAny().orElse(null);

                x.setUpdateTime(tempStampMap != null ? Optional.ofNullable((Date)tempStampMap.get("sut")).orElse(null):null);
            });
        }
    }

    /**
     * 获取规格信息列表
     *
     * @param productId
     * @return
     */
    public List<Catalog> getCatalogListByProduct(String productId) {
        MongoQueryData queryData = new MongoQueryData();

        Map<String, Object> matchConditionMap = new HashMap<>();
        matchConditionMap.put("spid", productId);
        queryData.setMatchCondition(matchConditionMap);

        Map<String, Boolean> projectionMap = new HashMap<>();
        projectionMap.put("cid", true);
        projectionMap.put("newp", true);
        projectionMap.put("price", true);
        projectionMap.put("vip", true);
        projectionMap.put("spid", true);
        projectionMap.put("sid", true);
        projectionMap.put("_id", false);
        queryData.setProjection(projectionMap);

        queryData.setTableName(Constants.CatalogDb);

        queryData.setOperationType(MongoOperationTypeEnum.SELECTMANY);
        List<Catalog> tempCatalogList = mongoProcessor
                .queryMongo(queryData)
                .stream().map(this::convertMapToCatalog).collect(Collectors.toList());

        setCatalogStamp(tempCatalogList);

        return tempCatalogList;
    }

    /**
     * 获取规格信息列表
     *
     * @param productIdList
     * @return
     */
    public List<Catalog> getCatalogListByProduct(List<String> productIdList) {
        MongoQueryData queryData = new MongoQueryData();

        Map<String, Object> matchConditionMap = new HashMap<>();
        Map<String, Object> tempMap = new HashMap<>();
        tempMap.put("$in", productIdList);
        matchConditionMap.put("spid", tempMap);
        queryData.setMatchCondition(matchConditionMap);

        Map<String, Boolean> projectionMap = new HashMap<>();
        projectionMap.put("cid", true);
        projectionMap.put("newp", true);
        projectionMap.put("price", true);
        projectionMap.put("vip", true);
        projectionMap.put("spid", true);
        projectionMap.put("sid", true);
        projectionMap.put("_id", false);
        queryData.setProjection(projectionMap);

        queryData.setTableName(Constants.CatalogDb);

        queryData.setOperationType(MongoOperationTypeEnum.SELECTMANY);

        List<Catalog> tempCatalogList = mongoProcessor
                .queryMongo(queryData)
                .stream().map(this::convertMapToCatalog).collect(Collectors.toList());

        setCatalogStamp(tempCatalogList);

        return tempCatalogList;
    }

    /**
     * 根据规格id获取规格信息列表
     *
     * @param catalogIdList
     * @return
     */
    public List<Catalog> getCatalogByCatalogId(List<String> catalogIdList) {
        MongoQueryData queryData = new MongoQueryData();
        Map<String, Object> matchConditionMap = new HashMap<>();
        Map<Object, Object> tempMap = new HashMap<>();
        tempMap.put("$in", catalogIdList);
        matchConditionMap.put("cid", tempMap);
        queryData.setMatchCondition(matchConditionMap);

        Map<String, Boolean> projectionMap = new HashMap<>();
        projectionMap.put("cid", true);
        projectionMap.put("newp", true);
        projectionMap.put("price", true);
        projectionMap.put("vip", true);
        projectionMap.put("spid", true);
        projectionMap.put("sid", true);
        projectionMap.put("_id", false);
        queryData.setProjection(projectionMap);

        queryData.setTableName(Constants.CatalogDb);

        queryData.setOperationType(MongoOperationTypeEnum.SELECTMANY);

        List<Catalog> tempCatalogList = mongoProcessor
                .queryMongo(queryData)
                .stream().map(this::convertMapToCatalog).collect(Collectors.toList());

        setCatalogStamp(tempCatalogList);

        return tempCatalogList;
    }

    /**
     * 组装规格更新时间
     * @param tempCatalogList
     */
    private void setCatalogStamp(List<Catalog> tempCatalogList){
        if(tempCatalogList != null && !tempCatalogList.isEmpty()){
            List<String> tempProductIdList = tempCatalogList.stream().map(Catalog::getProductId)
                    .collect(Collectors.toList());

            List<Map<String,Object>> tempStampList = getTimeStampByProductIdList(tempProductIdList,Arrays.asList("cut"));

            tempCatalogList.forEach(x -> {
                Map<String,Object> tempStampMap = tempStampList.stream().filter(z ->
                        Optional.ofNullable(z.get("spid")).orElse("").equals(x.getProductId()))
                        .findAny().orElse(null);

                x.setUpdateTime(tempStampMap != null ? Optional.ofNullable((Date)tempStampMap.get("cut")).orElse(null):null);
            });
        }
    }

    /**
     * 获取活动商品信息
     *
     * @param productId
     * @return
     */
    public ActivityProduct getActivityProduct(String productId) {
        MongoQueryData queryData = new MongoQueryData();
        Map<String, Boolean> projectionMap = new HashMap<>();
        projectionMap.put("spid", true);
        projectionMap.put("inaid", true);
        projectionMap.put("isolation", true);
        projectionMap.put("catalogs", true);
        projectionMap.put("nbuyer", true);
        projectionMap.put("start", true);
        projectionMap.put("end", true);
        projectionMap.put("_id", false);
        queryData.setProjection(projectionMap);

        Map<String, Object> matchConditionMap = new HashMap<>();
        matchConditionMap.put("spid", productId);
        Map<String, Object> tempGteMap = new HashMap<>();
        tempGteMap.put("$gte", new Date());
        Map<String, Object> tempLteMap = new HashMap<>();
        tempLteMap.put("$lte", new Date());
        matchConditionMap.put("start", tempLteMap);
        matchConditionMap.put("end", tempGteMap);
        queryData.setMatchCondition(matchConditionMap);

        //正常业务逻辑 一个时间段只能有一个活动 但是有异常情况 变成一个时间段有多个活动 以下代码为兜底逻辑
        Map<String, Boolean> sort = new HashMap<>();
        sort.put("inaid", false);
        queryData.setSort(sort);

        queryData.setTableName(Constants.ActivityProductDb);

        queryData.setOperationType(MongoOperationTypeEnum.SELECTSINGLE);

        ActivityProduct activityProduct = mongoProcessor.queryMongo(queryData)
                .stream()
                .map(this::convertMapToActivityProduct)
                .findAny()
                .orElse(null);

        if(activityProduct != null){
            activityProduct.setUpdateTime(getUpdateTimeByProductId(activityProduct.getProductId(), "aut"));
        }

        return activityProduct;
    }

    /**
     * 获取活动商品信息列表
     *
     * @param productIdList
     * @return
     */
    public List<ActivityProduct> getActivityProductList(List<String> productIdList) {
        MongoQueryData queryData = new MongoQueryData();
        Map<String, Boolean> projectionMap = new HashMap<>();
        projectionMap.put("spid", true);
        projectionMap.put("start", true);
        projectionMap.put("end", true);
        projectionMap.put("inaid", true);
        projectionMap.put("isolation", true);
        projectionMap.put("catalogs", true);
        projectionMap.put("nbuyer", true);
        projectionMap.put("start", true);
        projectionMap.put("end", true);
        projectionMap.put("_id", false);
        queryData.setProjection(projectionMap);

        Map<String, Object> matchConditionMap = new HashMap<>();
        Map<String, Object> tempProductIdMap = new HashMap<>();
        tempProductIdMap.put("$in", productIdList);
        matchConditionMap.put("spid", tempProductIdMap);
        Map<String, Object> tempGteMap = new HashMap<>();
        tempGteMap.put("$gte", new Date());
        Map<String, Object> tempLteMap = new HashMap<>();
        tempLteMap.put("$lte", new Date());
        matchConditionMap.put("start", tempLteMap);
        matchConditionMap.put("end", tempGteMap);
        queryData.setMatchCondition(matchConditionMap);


        queryData.setTableName(Constants.ActivityProductDb);

        queryData.setOperationType(MongoOperationTypeEnum.SELECTMANY);

        List<ActivityProduct> activityProductList = mongoProcessor.queryMongo(queryData)
                .stream()
                .map(this::convertMapToActivityProduct)
                .collect(Collectors.toList());

       setActivityProductStamp(activityProductList);

        return activityProductList;
    }

    /**
     * 获取新增活动商品信息列表
     *
     * @param newestProductInActivityId 最新活动商品关联id
     * @return
     */
    public List<ActivityProduct> getNewestActivityProductIdList(Integer newestProductInActivityId) {
        MongoQueryData queryData = new MongoQueryData();
        Map<String, Boolean> projectionMap = new HashMap<>();
        projectionMap.put("spid", true);
        projectionMap.put("start", true);
        projectionMap.put("end", true);
        projectionMap.put("inaid", true);
        projectionMap.put("isolation", true);
        projectionMap.put("catalogs", true);
        projectionMap.put("nbuyer", true);
        projectionMap.put("start", true);
        projectionMap.put("end", true);
        projectionMap.put("_id", false);
        queryData.setProjection(projectionMap);

        Map<String, Object> matchConditionMap = new HashMap<>();
        Map<String, Object> tempGtMap = new HashMap<>();
        tempGtMap.put("$gt", newestProductInActivityId);
        matchConditionMap.put("inaid", tempGtMap);
        queryData.setMatchCondition(matchConditionMap);

        queryData.setTableName(Constants.ActivityProductDb);

        queryData.setOperationType(MongoOperationTypeEnum.SELECTMANY);

        List<ActivityProduct> activityProductList = mongoProcessor.queryMongo(queryData)
                .stream()
                .map(this::convertMapToActivityProduct)
                .collect(Collectors.toList());

        setActivityProductStamp(activityProductList);

        return activityProductList;
    }

    /**
     * 组装活动商品列表更新时间
     * @param activityProductList
     */
    private void setActivityProductStamp(List<ActivityProduct> activityProductList){
        if(activityProductList != null && !activityProductList.isEmpty()) {
            activityProductList.removeAll(Collections.singleton(null));

            List<String> activityProductIdList = activityProductList.stream().map(ActivityProduct::getProductId)
                    .collect(Collectors.toList());

            List<Map<String,Object>> tempStampList = getTimeStampByProductIdList(activityProductIdList,Arrays.asList("aut"));

            activityProductList.forEach(x -> {
                Map<String,Object> tempStampMap = tempStampList.stream().filter(z ->
                        Optional.ofNullable(z.get("spid")).orElse("").equals(x.getProductId()))
                        .findAny().orElse(null);

                x.setUpdateTime(tempStampMap != null ? Optional.ofNullable((Date)tempStampMap.get("aut")).orElse(null):null);
            });
        }
    }


    /**
     * 获取全部有效活动商品列表
     * @return
     */
    public List<ActivityProduct> getAllValidActivityProductList() {
        MongoQueryData queryData = new MongoQueryData();
        Map<String, Boolean> projectionMap = new HashMap<>();
        projectionMap.put("spid", true);
        projectionMap.put("start", true);
        projectionMap.put("end", true);
        projectionMap.put("inaid", true);
        projectionMap.put("isolation", true);
        projectionMap.put("catalogs", true);
        projectionMap.put("nbuyer", true);
        projectionMap.put("start", true);
        projectionMap.put("end", true);
        projectionMap.put("_id", false);
        queryData.setProjection(projectionMap);

        Map<String, Object> matchConditionMap = new HashMap<>();
        Map<String, Object> tempGteMap = new HashMap<>();
        tempGteMap.put("$gte", new Date());
        Map<String, Object> tempLteMap = new HashMap<>();
        tempLteMap.put("$lte", new Date());
        matchConditionMap.put("start", tempLteMap);
        matchConditionMap.put("end", tempGteMap);
        queryData.setMatchCondition(matchConditionMap);


        queryData.setTableName(Constants.ActivityProductDb);

        queryData.setOperationType(MongoOperationTypeEnum.SELECTMANY);

        List<ActivityProduct> activityProductList = mongoProcessor.queryMongo(queryData)
                .stream()
                .map(x -> convertMapToActivityProduct(x))
                .collect(Collectors.toList());

       setActivityProductStamp(activityProductList);

        return activityProductList;
    }

    /**
     * 根据商品id获取对应变更表的updateTime
     *
     * @param productId
     * @param tableUpdateColName
     * @return
     */
    public Date getUpdateTimeByProductId(String productId, String tableUpdateColName) {
        Map<String, Object> tempChangeMap = getTimeStampByProductId(productId, Lists.newArrayList(tableUpdateColName));
        return Optional.ofNullable((Date) tempChangeMap.get(tableUpdateColName)).orElse(null);
    }

    /**
     * catalog转换器
     *
     * @param catalogMap
     * @return
     */
    private Catalog convertMapToCatalog(Map<String, Object> catalogMap) {
        Catalog tempCatalog = new Catalog();
        tempCatalog.setProductId(Optional.ofNullable((String) catalogMap.get("spid")).orElse(""));
        tempCatalog.setCatalogId(Optional.ofNullable((String) catalogMap.get("cid")).orElse(""));
        tempCatalog.setSellerId(Optional.ofNullable((Integer) catalogMap.get("sid")).orElse(0));
        tempCatalog.setQuotePrice(
                Utils.doubleFormat(Optional.ofNullable((Double) catalogMap.get("price")).orElse(0D), 2)
        );
        tempCatalog.setNewCustomerPrice(
                Utils.doubleFormat(
                        Optional.ofNullable(Double.valueOf(catalogMap.get("newp").toString())).orElse(0D), 2)
        );
        tempCatalog.setVipPrice(
                Utils.doubleFormat(Optional.ofNullable(Double.valueOf(catalogMap.get("vip").toString())).orElse(0D), 2)
        );

        return tempCatalog;
    }

    /**
     * product转换器
     *
     * @param productMap
     * @return
     */
    private ProductPriceData convertMapToProductPriceData(Map<String, Object> productMap) {
        ProductPriceData tempProductPriceData = new ProductPriceData();
        tempProductPriceData.setProductId(Optional.ofNullable((String) productMap.get("spid")).orElse(""));
        tempProductPriceData.setPriceMaxRange(Optional.ofNullable((String) productMap.get("maxp")).orElse(""));
        tempProductPriceData.setPriceMinRange(Optional.ofNullable((String) productMap.get("minp")).orElse(""));
        tempProductPriceData.setSellerId(Optional.ofNullable((Integer) productMap.get("sid")).orElse(0));

        return tempProductPriceData;
    }

    /**
     * activityProduct转换器
     *
     * @param activityProductMap
     * @return
     */
    private ActivityProduct convertMapToActivityProduct(Map<String, Object> activityProductMap) {
        ActivityProduct tempActivityProduct = new ActivityProduct();

            tempActivityProduct.setProductInActivityId(
                    Optional.ofNullable((Integer) activityProductMap.get("inaid")).orElse(0)
            );

            tempActivityProduct.setProductId(Optional.ofNullable((String) activityProductMap.get("spid")).orElse(""));
            List<Map<String, Object>> tempCatalogs = Optional
                    .ofNullable((List<Map<String, Object>>) activityProductMap.get("catalogs"))
                    .orElse(Lists.newArrayList());

            tempActivityProduct.setActivityCatalogList(tempCatalogs.stream().map(x -> {
                ActivityCatalog tempCatalog = new ActivityCatalog();
                tempCatalog.setActivityCatalogId(Optional.ofNullable((String) x.get("cid")).orElse(""));
                tempCatalog.setActivityCatalogPrice(Optional.ofNullable((Double) x.get("price")).orElse(0D));
                tempCatalog.setActivityStock(Optional.ofNullable((Integer) x.get("stock")).orElse(0));
                return tempCatalog;
            }).collect(Collectors.toList()));

            tempActivityProduct.setHasIsolation(
                    Optional.ofNullable((Boolean) activityProductMap.get("isolation")).orElse(false)
            );

            tempActivityProduct.setNewBuyer(Optional.ofNullable((Boolean) activityProductMap.get("nbuyer"))
                    .orElse(false));

            tempActivityProduct.setStartTime(Optional.ofNullable((Date) activityProductMap.get("start")).orElse(null));

            tempActivityProduct.setEndTime(Optional.ofNullable((Date) activityProductMap.get("end")).orElse(null));

        return tempActivityProduct;
    }
}
