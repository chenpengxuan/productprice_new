package com.ymatou.productprice.domain.mongorepo;

import com.ymatou.productprice.infrastructure.constants.Constants;
import com.ymatou.productprice.infrastructure.dataprocess.mongo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

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
     * @param productId
     * @return
     */
    public List<Map<String,Object>> getCatalogList(String productId){
        return mongoProcessor.queryMongo(MongoDataBuilder.queryCatalogList(MongoQueryBuilder.queryProductId(productId)));
    }

    /**
     * 获取活动商品信息
     * @param productId
     * @return
     */
    public Map<String,Object> getActivityProduct(String productId) {
        MongoQueryData queryData = new MongoQueryData();
        Map<String,Boolean> projectionMap = new HashMap<>();
        projectionMap.put("spid",true);
        projectionMap.put("inaid",true);
        projectionMap.put("isolation",true);
        projectionMap.put("catalogs",true);
        projectionMap.put("nbuyer",true);
        queryData.setProjection(projectionMap);
        Map<String,Object> matchConditionMap = new HashMap<>();
        matchConditionMap.put("spid",productId);
        Map<String, Object> tempGteMap = new HashMap<>();
        tempGteMap.put("$gte", new Date());
        Map<String, Object> tempLteMap = new HashMap<>();
        tempGteMap.put("$lte", new Date());
        matchConditionMap.put("start",tempLteMap);
        matchConditionMap.put("end",tempGteMap);
        Map<String,Boolean> sort = new HashMap<>();
        sort.put("inaid",false);
        queryData.setSort(sort);
        queryData.setMatchCondition(matchConditionMap);
        queryData.setTableName(Constants.ActivityProductDb);
        queryData.setOperationType(MongoOperationTypeEnum.SELECTSINGLE);
        return mongoProcessor.queryMongo(queryData).stream().findFirst().orElse(Collections.emptyMap());
    }

    /**
     * 根据
     * @param productId
     * @return
     */
    public Map<String,Object> getSellerIdByProductId(String productId){
        MongoQueryData queryData = new MongoQueryData();
        Map<String,Boolean> projectionMap = new HashMap<>();
        projectionMap.put("sid",true);
        queryData.setProjection(projectionMap);
        Map<String,Object> matchConditionMap = new HashMap<>();
        matchConditionMap.put("spid",productId);
        queryData.setMatchCondition(matchConditionMap);
        queryData.setTableName(Constants.ProductDb);
        queryData.setOperationType(MongoOperationTypeEnum.SELECTSINGLE);
        return mongoProcessor.queryMongo(queryData).stream().findFirst().orElse(Collections.emptyMap());
    }
}
