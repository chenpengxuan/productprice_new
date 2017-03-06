package com.ymatou.productprice.infrastructure.dataprocess.mongo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenpengxuan on 2017/2/7.
 * mongo参数创建
 */
public class MongoQueryBuilder {

    /**
     * 根据直播id查询
     *
     * @param liveId
     * @return
     */
    public static Map<String, Object> queryLiveId(long liveId) {
        Map<String, Object> matchConditionInfo = new HashMap();
        matchConditionInfo.put("lid", liveId);
        return matchConditionInfo;
    }

    /**
     * 根据商品id查询
     *
     * @param productId
     * @return
     */
    public static Map<String, Object> queryProductId(String productId) {
        Map<String, Object> matchConditionInfo = new HashMap();
        matchConditionInfo.put("spid", productId);
        return matchConditionInfo;
    }

    /**
     * 根据商品id与规格id查询
     * @param productId
     * @param catalogId
     * @return
     */
    public static Map<String, Object> queryProductIdAndCatalogId(String productId,String catalogId) {
        Map<String, Object> matchConditionInfo = new HashMap();
        matchConditionInfo.put("spid", productId);
        matchConditionInfo.put("cid",catalogId);
        return matchConditionInfo;
    }

    /**
     * 根据商品与直播id查询
     *
     * @param productId
     * @param liveId
     * @return
     */
    public static Map<String, Object> queryProductIdAndLiveId(String productId, long liveId) {
        Map<String, Object> matchConditionInfo = new HashMap();
        matchConditionInfo.put("spid", productId);
        matchConditionInfo.put("lid", liveId);
        return matchConditionInfo;
    }


    /**
     * 根据商品编号与活动编号查询
     *
     * @param productInactivityId
     * @return
     */
    public static Map<String, Object> queryProductIdAndActivityId(long productInactivityId) {
        Map<String, Object> matchConditionInfo = new HashMap();
        matchConditionInfo.put("inaid", productInactivityId);
        return matchConditionInfo;
    }
}
