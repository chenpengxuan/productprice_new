package com.ymatou.productprice.domain.mongorepo;

import com.ymatou.productprice.infrastructure.constants.Constants;
import com.ymatou.productprice.infrastructure.dataprocess.mongo.MongoOperationTypeEnum;
import com.ymatou.productprice.infrastructure.dataprocess.mongo.MongoProcessor;
import com.ymatou.productprice.infrastructure.dataprocess.mongo.MongoQueryData;
import com.ymatou.productprice.infrastructure.util.ParallelUtil.ParallelProcessor;
import com.ymatou.productprice.infrastructure.util.Utils;
import com.ymatou.productprice.model.Catalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * mongo 数据操作相关
 * Created by chenpengxuan on 2017/3/2.
 */
@Component
public class MongoRepository {
    @Autowired
    private MongoProcessor mongoProcessor;

    @Autowired
    private ParallelProcessor parallelProcessor;

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
        projectionMap.put("_id", false);
        queryData.setProjection(projectionMap);

        queryData.setTableName(Constants.CatalogDb);

        queryData.setOperationType(MongoOperationTypeEnum.SELECTMANY);
        return mongoProcessor
                .queryMongo(queryData)
                .stream().map(x -> convertMapToCatalog(x)).collect(Collectors.toList());
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
        projectionMap.put("_id", false);
        queryData.setProjection(projectionMap);

        queryData.setTableName(Constants.CatalogDb);

        queryData.setOperationType(MongoOperationTypeEnum.SELECTMANY);

        return mongoProcessor
                .queryMongo(queryData)
                .stream().map(x -> convertMapToCatalog(x)).collect(Collectors.toList());
    }

    /**
     * 获取规格信息列表
     *
     * @param productIdList
     * @return
     */
    public List<Catalog> getCatalogListByProductParallelWrapper(List<String> productIdList) {
        return parallelProcessor.doParallelProcess(productIdList, obj -> getCatalogListByProduct((List<String>) obj));
    }

    /**
     * 根据规格id获取规格信息
     *
     * @param catalogId
     * @return
     */
    public Catalog getCatalogByCatalogId(String catalogId) {
        MongoQueryData queryData = new MongoQueryData();

        Map<String, Object> matchConditionMap = new HashMap<>();
        matchConditionMap.put("cid", catalogId);
        queryData.setMatchCondition(matchConditionMap);

        queryData.setTableName(Constants.CatalogDb);

        queryData.setOperationType(MongoOperationTypeEnum.SELECTSINGLE);

        return mongoProcessor
                .queryMongo(queryData)
                .stream().map(x -> convertMapToCatalog(x)).findAny().orElse(new Catalog());
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
        projectionMap.put("_id", false);
        queryData.setProjection(projectionMap);

        queryData.setTableName(Constants.CatalogDb);

        queryData.setOperationType(MongoOperationTypeEnum.SELECTMANY);

        return mongoProcessor
                .queryMongo(queryData)
                .stream().map(x -> convertMapToCatalog(x)).collect(Collectors.toList());
    }

    /**
     * 根据规格id获取规格信息列表
     *
     * @param catalogIdList
     * @return
     */
    public List<Catalog> getCatalogByCatalogIdParallelWrapper(List<String> catalogIdList) {
        return parallelProcessor.doParallelProcess(catalogIdList, obj -> getCatalogByCatalogId((List<String>) obj));
    }

    /**
     * 获取活动商品信息
     *
     * @param productId
     * @return
     */
    public Map<String, Object> getActivityProduct(String productId) {
        MongoQueryData queryData = new MongoQueryData();
        Map<String, Boolean> projectionMap = new HashMap<>();
        projectionMap.put("spid", true);
        projectionMap.put("inaid", true);
        projectionMap.put("isolation", true);
        projectionMap.put("catalogs", true);
        projectionMap.put("nbuyer", true);
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

        return mongoProcessor.queryMongo(queryData).stream().findAny().orElse(Collections.emptyMap());
    }

    /**
     * 获取活动商品信息列表
     *
     * @param productIdList
     * @return
     */
    public List<Map<String, Object>> getActivityProductList(List<String> productIdList) {
        MongoQueryData queryData = new MongoQueryData();
        Map<String, Boolean> projectionMap = new HashMap<>();
        projectionMap.put("spid", true);
        projectionMap.put("inaid", true);
        projectionMap.put("isolation", true);
        projectionMap.put("catalogs", true);
        projectionMap.put("nbuyer", true);
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

        return mongoProcessor.queryMongo(queryData);
    }

    /**
     * 获取活动商品信息列表
     *
     * @param productIdList
     * @return
     */
    public List<Map<String, Object>> getActivityProductListParallelWrapper(List<String> productIdList) {
        return parallelProcessor.doParallelProcess(productIdList, obj -> getActivityProductList((List<String>) obj));
    }

    /**
     * 根据商品id查询买手id
     *
     * @param productId
     * @return
     */
    public Map<String, Object> getSellerIdByProductId(String productId) {
        MongoQueryData queryData = new MongoQueryData();
        Map<String, Boolean> projectionMap = new HashMap<>();
        projectionMap.put("sid", true);
        projectionMap.put("_id", false);
        queryData.setProjection(projectionMap);

        Map<String, Object> matchConditionMap = new HashMap<>();
        matchConditionMap.put("spid", productId);
        queryData.setMatchCondition(matchConditionMap);
        queryData.setTableName(Constants.ProductDb);
        queryData.setOperationType(MongoOperationTypeEnum.SELECTSINGLE);
        return mongoProcessor.queryMongo(queryData).stream().findAny().orElse(Collections.emptyMap());
    }

    /**
     * 根据商品id列表查询买手id
     *
     * @param productIdList
     * @return
     */
    public Map<String, Long> getSellerIdListByProductIdList(List<String> productIdList) {
        MongoQueryData queryData = new MongoQueryData();
        Map<String, Boolean> projectionMap = new HashMap<>();
        projectionMap.put("sid", true);
        projectionMap.put("spid", true);
        queryData.setProjection(projectionMap);

        Map<String, Object> matchConditionMap = new HashMap<>();
        Map<String, Object> tempMap = new HashMap<>();
        tempMap.put("$in", productIdList);
        matchConditionMap.put("spid", tempMap);
        queryData.setMatchCondition(matchConditionMap);
        queryData.setTableName(Constants.ProductDb);
        queryData.setOperationType(MongoOperationTypeEnum.SELECTMANY);

        Map<String, Long> resultMap = new HashMap<>();

        List<Map<String, Object>> mapList = mongoProcessor.queryMongo(queryData);

        mapList.forEach(x ->
                resultMap.put(Optional.ofNullable((String) x.get("spid")).orElse("")
                        , Long.valueOf(Optional.ofNullable((Integer) (x.get("sid"))).orElse(0))));
        return resultMap;
    }

    /**
     * 获取商品部分字段 用于性能测试
     *
     * @param productIdList
     * @return
     */
    public List<Map<String, Object>> getSellerOneFieldForTest(List<String> productIdList) {
        MongoQueryData queryData = new MongoQueryData();

        Map<String, Boolean> projectionMap = new HashMap<>();
        projectionMap.put("sid", true);
        projectionMap.put("start", true);
        projectionMap.put("_id", false);

        queryData.setProjection(projectionMap);

        Map<String, Object> matchConditionMap = new HashMap<>();
        Map<String, Object> tempMap = new HashMap<>();
        tempMap.put("$in", productIdList);
        matchConditionMap.put("spid", tempMap);
        queryData.setMatchCondition(matchConditionMap);
        queryData.setTableName(Constants.ProductDb);
        queryData.setOperationType(MongoOperationTypeEnum.SELECTMANY);

        Map<String, Long> resultMap = new HashMap<>();

        List<Map<String, Object>> mapList = mongoProcessor.queryMongo(queryData);

        return mapList;
    }

    /**
     * 获取商品部分字段 用于性能测试
     *
     * @param productIdList
     * @return
     */
    public List<Map<String, Object>> getSellerTwoFieldForTest(List<String> productIdList) {
        MongoQueryData queryData = new MongoQueryData();

        Map<String, Boolean> projectionMap = new HashMap<>();
        projectionMap.put("sid", true);
        projectionMap.put("start", true);
        projectionMap.put("end", true);
        projectionMap.put("_id", false);

        queryData.setProjection(projectionMap);

        Map<String, Object> matchConditionMap = new HashMap<>();
        Map<String, Object> tempMap = new HashMap<>();
        tempMap.put("$in", productIdList);
        matchConditionMap.put("spid", tempMap);
        queryData.setMatchCondition(matchConditionMap);
        queryData.setTableName(Constants.ProductDb);
        queryData.setOperationType(MongoOperationTypeEnum.SELECTMANY);

        Map<String, Long> resultMap = new HashMap<>();

        List<Map<String, Object>> mapList = mongoProcessor.queryMongo(queryData);

        return mapList;
    }

    /**
     * 获取商品部分字段 用于性能测试
     *
     * @param productIdList
     * @return
     */
    public List<Map<String, Object>> getSellerThreeFieldForTest(List<String> productIdList) {
        MongoQueryData queryData = new MongoQueryData();

        Map<String, Boolean> projectionMap = new HashMap<>();
        projectionMap.put("sid", true);
        projectionMap.put("start", true);
        projectionMap.put("end", true);
        projectionMap.put("addtime", true);
        projectionMap.put("_id", false);

        queryData.setProjection(projectionMap);

        Map<String, Object> matchConditionMap = new HashMap<>();
        Map<String, Object> tempMap = new HashMap<>();
        tempMap.put("$in", productIdList);
        matchConditionMap.put("spid", tempMap);
        queryData.setMatchCondition(matchConditionMap);
        queryData.setTableName(Constants.ProductDb);
        queryData.setOperationType(MongoOperationTypeEnum.SELECTMANY);

        Map<String, Long> resultMap = new HashMap<>();

        List<Map<String, Object>> mapList = mongoProcessor.queryMongo(queryData);

        return mapList;
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
        tempCatalog.setEarnestPrice(0.0d);//已经不存在定金价逻辑 这里只是做兼容处理
        tempCatalog.setQuotePrice(Utils.doubleFormat(Optional.ofNullable((Double) catalogMap.get("price")).orElse(0D), 2));
        tempCatalog.setNewCustomerPrice(Utils.doubleFormat(Optional.ofNullable(Double.valueOf(catalogMap.get("newp").toString())).orElse(0D), 2));
        tempCatalog.setVipPrice(Utils.doubleFormat(Optional.ofNullable(Double.valueOf(catalogMap.get("vip").toString())).orElse(0D), 2));
        tempCatalog.setSubsidyPrice(0.0d);//活动新人价已经不存在，这里做兼容操作
        return tempCatalog;
    }
}
