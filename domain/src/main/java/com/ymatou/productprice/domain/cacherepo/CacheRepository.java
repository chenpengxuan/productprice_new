package com.ymatou.productprice.domain.cacherepo;

import com.ymatou.productprice.domain.mongorepo.MongoRepository;
import com.ymatou.productprice.domain.parallelrepo.ParallelRepository;
import com.ymatou.productprice.domain.repo.Repository;
import com.ymatou.productprice.infrastructure.config.props.BizProps;
import com.ymatou.productprice.infrastructure.util.CacheUtil.CacheManager;
import com.ymatou.productprice.model.Catalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 缓存 数据操作相关
 * Created by chenpengxuan on 2017/3/22.
 */
@Component
public class CacheRepository implements Repository{
    /**
     * 缓存工具类
     */
    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private BizProps bizProps;

    @Autowired
    private MongoRepository mongoRepository;

    @Autowired
    private ParallelRepository parallelRepository;

    private Repository realBusinessRepository;

    @PostConstruct
    public void init(){
        if(bizProps.isUseParallel()){
            realBusinessRepository = parallelRepository;
        }
        else{
            realBusinessRepository = mongoRepository;
        }
    }

    /**
     * 获取规格信息列表
     *
     * @param productId
     * @return
     */
    public List<Catalog> getCatalogListByProduct(String productId) {
        return (List<Catalog>) cacheManager.get(productId,
                obj -> buildSingleCacheKeyWithCatalogStamp((String) obj, "getCatalogListByProduct"),
                obj -> realBusinessRepository.getCatalogListByProduct(productId));
    }

    /**
     * 获取规格信息列表
     *
     * @param productIdList
     * @return
     */
    public List<Catalog> getCatalogListByProduct(List<String> productIdList) {
        return cacheManager.get(productIdList,
                obj -> buildMultiCacheKeyWithCatalogStamp((List<String>)obj,"getCatalogListByProduct"),
                obj -> realBusinessRepository.getCatalogListByProduct(productIdList),
                (pid,catalogList)
                        -> ((List<Catalog>)catalogList)
                        .stream()
                        .filter(x -> x.getProductId().equals(pid))
                        .collect(Collectors.toList()));
    }

    @Override
    public List<Catalog> getCatalogByCatalogId(List<String> catalogIdList) {
        return cacheManager.get(catalogIdList,
                obj -> buildMultiCacheKeyUseCatalogIdList((List<String>)obj,"getCatalogByCatalogId"),
                obj -> realBusinessRepository.getCatalogByCatalogId(catalogIdList),
                (pid,catalogList)
                        -> ((List<Catalog>)catalogList)
                        .stream()
                        .filter(x -> x.getCatalogId().equals(pid))
                        .collect(Collectors.toList()));
    }

    @Override
    public Map<String, Object> getActivityProduct(String productId) {
        return (Map<String, Object>) cacheManager.get(productId,
                obj -> buildSingleCacheKeyWithActivityStamp((String) obj, "getActivityProduct"),
                obj -> realBusinessRepository.getActivityProduct(productId));
    }

    @Override
    public List<Map<String, Object>> getActivityProductList(List<String> productIdList) {
        return cacheManager.get(productIdList,
                obj -> buildMultiCacheKeyWithActivityStamp((List<String>)obj,"getActivityProductList"),
                obj -> realBusinessRepository.getActivityProductList(productIdList),
                (pid,catalogList)
                        -> ((List<Map<String, Object>>)catalogList)
                        .stream()
                        .filter(x -> x.get("spid").equals(pid))
                        .collect(Collectors.toList()));
    }

    /**
     * 生成单个缓存key（用于规格商品）
     *
     * @param productId
     * @param methodName
     * @return
     */
    private String buildSingleCacheKeyWithCatalogStamp(String productId, String methodName) {
        return buildSingleCacheKeyUseProductId(productId,methodName,"cut");
    }

    /**
     * 生成单个缓存key(用于活动商品)
     *
     * @param productId
     * @param methodName
     * @return
     */
    private String buildSingleCacheKeyWithActivityStamp(String productId, String methodName) {
        return buildSingleCacheKeyUseProductId(productId,methodName,"aut");
    }

    /**
     * 生成多个缓存key（用于活动商品）
     * @param productIdList
     * @param methodName
     * @return
     */
    private Map<String, String> buildMultiCacheKeyWithActivityStamp(List<String> productIdList, String methodName) {
        return buildMultiCacheKeyUseProductIdList(productIdList,methodName,"aut");
    }

    /**
     * 生成多个缓存key（用于活动商品）
     * @param productIdList
     * @param methodName
     * @return
     */
    private Map<String, String> buildMultiCacheKeyWithCatalogStamp(List<String> productIdList, String methodName) {
        return buildMultiCacheKeyUseProductIdList(productIdList,methodName,"cut");
    }

    /**
     * 创建单个缓存key
     * @param productId
     * @param methodName
     * @param stampName
     * @return
     */
    private String buildSingleCacheKeyUseProductId(String productId, String methodName,String stampName) {
        Map<String, Object> cacheMap = mongoRepository.getTimeStampByProductId(productId, Arrays.asList(stampName));
        long cutStamp = cacheMap.get(stampName) != null ? ((Date) cacheMap.get(stampName)).getTime() : 0L;
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
    private Map<String, String> buildMultiCacheKeyUseProductIdList(List<String> productIdList, String methodName,String stampName) {
        List<Map<String, Object>> cacheMapList = mongoRepository.getTimeStampByProductIdList(productIdList, Arrays.asList(stampName));
        return cacheMapList.stream().collect(Collectors.toMap(cache -> {
            String tempProductId = Optional.ofNullable((String) cache.get("spid")).orElse("");
            return tempProductId;
        }, cache -> {
            long tempCutStamp = cache.get("cut") != null ? ((Date) cache.get(stampName)).getTime() : 0L;
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

    /**
     * 生成多个缓存key
     *
     * @param catalogIdList
     * @param methodName
     * @return
     */
    private Map<String, String> buildMultiCacheKeyUseCatalogIdList(List<String> catalogIdList, String methodName) {
        List<Map<String, Object>> catalogCacheMapList = mongoRepository.getTimeStampByCatalogIdList(catalogIdList, Arrays.asList("updatetime"));
        return catalogCacheMapList.stream().collect(Collectors.toMap(cache -> {
            String tempCatalogId = Optional.ofNullable((String) cache.get("cid")).orElse("");
            return tempCatalogId;
        }, cache -> {
            long tempCutStamp = cache.get("cut") != null ? ((Date) cache.get("updatetime")).getTime() : 0L;
            String tempCatalogId = Optional.ofNullable((String) cache.get("cid")).orElse("");
            StringBuffer sb = new StringBuffer();
            sb.append(methodName);
            sb.append("_");
            sb.append(tempCatalogId);
            sb.append("_");
            sb.append(String.valueOf(tempCutStamp));
            return sb.toString();
        }));
    }
}
