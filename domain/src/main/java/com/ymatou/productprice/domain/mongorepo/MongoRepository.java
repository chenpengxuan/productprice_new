package com.ymatou.productprice.domain.mongorepo;

import com.ymatou.productprice.infrastructure.constants.Constants;
import com.ymatou.productprice.infrastructure.dataprocess.mongo.*;
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

    /**
     * 获取规格信息列表
     *
     * @param productId
     * @return
     */
    public List<Catalog> getCatalogList(String productId) {
        return mongoProcessor
                .queryMongo(MongoDataBuilder.queryCatalogList(MongoQueryBuilder.queryProductId(productId)))
                .stream().map(x -> convertMapToCatalog(x)).collect(Collectors.toList());
    }

    /**
     * 获取规格信息列表
     *
     * @param productIdList
     * @return
     */
    public List<Catalog> getCatalogList(List<String> productIdList) {
        MongoQueryData queryData = new MongoQueryData();
        Map<String, Object> matchConditionMap = new HashMap<>();
        Map<String, Object> tempMap = new HashMap<>();
        tempMap.put("$in", productIdList);
        matchConditionMap.put("spid", tempMap);
        queryData.setMatchCondition(matchConditionMap);
        queryData.setTableName(Constants.CatalogDb);

        queryData.setOperationType(MongoOperationTypeEnum.SELECTMANY);

        return mongoProcessor
                .queryMongo(queryData)
                .stream().map(x -> convertMapToCatalog(x)).collect(Collectors.toList());
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
        queryData.setProjection(projectionMap);

        Map<String, Object> matchConditionMap = new HashMap<>();
        matchConditionMap.put("spid", productId);
        Map<String, Object> tempGteMap = new HashMap<>();
        tempGteMap.put("$gte", new Date());
        Map<String, Object> tempLteMap = new HashMap<>();
        tempGteMap.put("$lte", new Date());
        matchConditionMap.put("start", tempLteMap);
        matchConditionMap.put("end", tempGteMap);
        queryData.setMatchCondition(matchConditionMap);

        //正常业务逻辑 一个时间段只能有一个活动 但是有异常情况 变成一个时间段有多个活动 以下代码为兜底逻辑
        Map<String, Boolean> sort = new HashMap<>();
        sort.put("inaid", false);
        queryData.setSort(sort);

        queryData.setTableName(Constants.ActivityProductDb);

        queryData.setOperationType(MongoOperationTypeEnum.SELECTSINGLE);

        return mongoProcessor.queryMongo(queryData).stream().findFirst().orElse(Collections.emptyMap());
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
        queryData.setProjection(projectionMap);

        Map<String, Object> matchConditionMap = new HashMap<>();
        Map<String, Object> tempProductIdMap = new HashMap<>();
        tempProductIdMap.put("$in",productIdList);
        matchConditionMap.put("spid", tempProductIdMap);
        Map<String, Object> tempGteMap = new HashMap<>();
        tempGteMap.put("$gte", new Date());
        Map<String, Object> tempLteMap = new HashMap<>();
        tempGteMap.put("$lte", new Date());
        matchConditionMap.put("start", tempLteMap);
        matchConditionMap.put("end", tempGteMap);
        queryData.setMatchCondition(matchConditionMap);


        queryData.setTableName(Constants.ActivityProductDb);

        queryData.setOperationType(MongoOperationTypeEnum.SELECTMANY);

        return mongoProcessor.queryMongo(queryData);
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
        queryData.setProjection(projectionMap);

        Map<String, Object> matchConditionMap = new HashMap<>();
        matchConditionMap.put("spid", productId);
        queryData.setMatchCondition(matchConditionMap);
        queryData.setTableName(Constants.ProductDb);
        queryData.setOperationType(MongoOperationTypeEnum.SELECTSINGLE);
        return mongoProcessor.queryMongo(queryData).stream().findFirst().orElse(Collections.emptyMap());
    }

    /**
     * 根据商品id列表查询买手id
     *
     * @param productIdList
     * @return
     */
    public List<Map<String,Object>> getSellerIdListByProductIdList(List<String> productIdList) {
        MongoQueryData queryData = new MongoQueryData();
        Map<String, Boolean> projectionMap = new HashMap<>();
        projectionMap.put("sid", true);
        projectionMap.put("spid",true);
        queryData.setProjection(projectionMap);

        Map<String, Object> matchConditionMap = new HashMap<>();
        Map<String, Object> tempMap = new HashMap<>();
        tempMap.put("$in", productIdList);
        matchConditionMap.put("spid", tempMap);
        queryData.setMatchCondition(matchConditionMap);
        queryData.setTableName(Constants.ProductDb);
        queryData.setOperationType(MongoOperationTypeEnum.SELECTMANY);
        return mongoProcessor.queryMongo(queryData);
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
        tempCatalog.setCatalogId(Utils.makeNullDefaultValue((String) catalogMap.get("cid"), ""));
        tempCatalog.setEarnestPrice(0.0d);//已经不存在定金价逻辑 这里只是做兼容处理
        tempCatalog.setQuotePrice(Utils.doubleFormat(Utils.makeNullDefaultValue((double) catalogMap.get("price"), 0.0d), 2));
        tempCatalog.setNewCustomerPrice(Utils.doubleFormat(Utils.makeNullDefaultValue((double) catalogMap.get("newp"), 0.0d), 2));
        tempCatalog.setVipPrice(Utils.doubleFormat(Utils.makeNullDefaultValue((double) catalogMap.get("vip"), 0.0d), 2));
        tempCatalog.setSubsidyPrice(0.0d);//活动新人价已经不存在，这里做兼容操作
        return tempCatalog;
    }
}
