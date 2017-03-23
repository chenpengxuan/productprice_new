package com.ymatou.productprice.domain.cacherepo;

import com.ymatou.productprice.domain.mongorepo.MongoRepository;
import com.ymatou.productprice.infrastructure.util.CacheUtil.CacheManager;
import com.ymatou.productprice.model.Catalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 缓存 数据操作相关
 * Created by chenpengxuan on 2017/3/22.
 */
@Component
public class CacheRepository {
    /**
     * 缓存工具类
     */
    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private MongoRepository mongoRepository;

    /**
     * 获取规格信息列表
     *
     * @param productId
     * @return
     */
    public List<Catalog> getCatalogListByProduct(String productId) {
        return (List<Catalog>) cacheManager.getWithSingleKey(productId,
                obj -> buildSingleCacheKeyWithCatalogStamp((String) obj, "getCatalogListByProduct"),
                obj -> mongoRepository.getCatalogListByProduct(productId));
    }

    /**
     * 获取规格信息列表
     *
     * @param productIdList
     * @return
     */
    public List<Catalog> getCatalogListByProduct(List<String> productIdList) {
        return cacheManager.getWithMultipleKey(productIdList,
                obj -> buildMultiCacheKeyWithCatalogStamp((List<String>)obj,"getCatalogListByProduct"),
                obj -> mongoRepository.getCatalogListByProduct(productIdList),
                (pid,catalogList)
                        -> ((List<Catalog>)catalogList)
                        .stream()
                        .filter(x -> x.getProductId().equals(pid))
                        .collect(Collectors.toList()));
    }

    /**
     * 生成单个缓存key
     *
     * @param productId
     * @param methodName
     * @return
     */
    private String buildSingleCacheKeyWithCatalogStamp(String productId, String methodName) {
        Map<String, Object> cacheMap = mongoRepository.getTimeStampByProductId(productId, Arrays.asList("cut"));
        long cutStamp = cacheMap.get("cut") != null ? ((Date) cacheMap.get("cut")).getTime() : 0L;
        StringBuffer sb = new StringBuffer();
        sb.append(methodName);
        sb.append("_");
        sb.append(productId);
        sb.append("_");
        sb.append(String.valueOf(cutStamp));
        return sb.toString();
    }

    /**
     * 生成多个缓存key
     *
     * @param productIdList
     * @param methodName
     * @return
     */
    private Map<String, String> buildMultiCacheKeyWithCatalogStamp(List<String> productIdList, String methodName) {
        List<Map<String, Object>> cacheMapList = mongoRepository.getTimeStampByProductIdList(productIdList, Arrays.asList("cut"));
        return cacheMapList.stream().collect(Collectors.toMap(cache -> {
            String tempProductId = Optional.ofNullable((String) cache.get("spid")).orElse("");
            return tempProductId;
        }, cache -> {
            long tempCutStamp = cache.get("cut") != null ? ((Date) cache.get("cut")).getTime() : 0L;
            String tempProductId = Optional.ofNullable((String) cache.get("spid")).orElse("");
            StringBuffer sb = new StringBuffer();
            sb.append(methodName);
            sb.append("_");
            sb.append(tempProductId);
            sb.append("_");
            sb.append(String.valueOf(tempCutStamp));
            return sb.toString();
        }));
    }
}
