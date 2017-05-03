package com.ymatou.productprice.infrastructure.util.CacheUtil;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ymatou.productprice.infrastructure.config.props.CacheProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 缓存管理类
 * Created by chenpengxuan on 2017/3/21.
 */
@Component
@DependsOn({"disconfMgrBean2"})
public class CacheManager {

    @Autowired
    private CacheProps cacheProps;

    private Cache cacheContainer;

    private ConcurrentMap activityProductCacheContainer;

    private static final int CACHE_SIZE_UNIT = 10000;

    /**
     * 预期活动商品缓存数量
     */
    private volatile int expectActivityCacheSize;

    @PostConstruct
    public void init() {
        switch (CacheTypeEnum.valueOf(cacheProps.getCacheType().toUpperCase())) {
            case GUAVACACHE:
                cacheContainer = CacheBuilder.newBuilder()
                        .maximumSize(cacheProps.getCacheSize() * CACHE_SIZE_UNIT)
                        .expireAfterAccess(cacheProps.getExpireTime(), TimeUnit.HOURS)
                        .concurrencyLevel(cacheProps.getWriteConcurrencyNum())
                        .recordStats()
                        .build();
                activityProductCacheContainer = new ConcurrentHashMap(cacheProps.getActivityProductCacheSize());
                break;
            case EHCACHE:
                break;
            default:
                break;
        }
    }

    /**
     * 获取缓存统计信息
     *
     * @return
     */
    public CacheStats getCacheStats() {
        return cacheContainer.stats();
    }

    /**
     * 获取单个key的缓存
     * 增加缓存有效性检查机制
     *
     * @param queryParam          查询参数
     * @param generateKeyFunc     生成缓存key的func
     * @param repositoryFunc      查mongo获取相关数据的func
     * @param checkCacheValidFunc 检查缓存是否有效的func
     * @param <K>                 参数与缓存key的数据类型
     * @param <V>                 返回数据的类型
     * @return
     */
    public <K, V, Z> V get(K queryParam,
                           Function<K, K> generateKeyFunc,
                           Function<K, Z> repositoryFunc,
                           BiFunction<K, V, Boolean> checkCacheValidFunc,
                           BiFunction<V, Z, V> updateDataFunc) {

        K cacheKey = generateKeyFunc.apply(queryParam);

        V cacheResult = (V) cacheContainer.getIfPresent(cacheKey);

        if (cacheResult == null) {
            Z tempData = repositoryFunc.apply(queryParam);
            //将缓存数据结构中部分无效的数据结构更新掉
            cacheResult = updateDataFunc.apply(cacheResult, tempData);
        } else {
            //如果缓存检查无效则重新从repository里面取一把数据
            if (!checkCacheValidFunc.apply(queryParam, cacheResult)) {
                Z tempData = repositoryFunc.apply(queryParam);
                cacheResult = updateDataFunc.apply(cacheResult, tempData);
            }
        }
        if (cacheResult != null) {
            cacheContainer.put(cacheKey, cacheResult);
        }
        return cacheResult;
    }

    /**
     * 获取单个key的缓存
     *
     * @param cacheKey
     * @param <K>
     * @param <V>
     */
    public <K, V> V get(K cacheKey) {
        return
                Optional.ofNullable((V) cacheContainer.getIfPresent(cacheKey)).orElse(null);
    }

    /**
     * 获取活动商品
     *
     * @param cacheKey
     * @param <K>
     * @param <V>
     * @return
     */
    public <K, V> V getActivityProduct(K cacheKey) {
        return
                Optional.ofNullable((V) activityProductCacheContainer.get(cacheKey)).orElse(null);
    }

    /**
     * 获取活动商品
     *
     * @param <K>
     * @param <V>
     * @param cacheKeyList
     * @return
     */
    public <K, V> List<V> getActivityProduct(List<K> cacheKeyList) {
        return cacheKeyList
                .stream()
                .map(x -> (V) getActivityProduct(x))
                .collect(Collectors.toList());
    }

    /**
     * 添加活动商品缓存
     *
     * @param cacheKey
     * @param cacheVal
     * @param <K>
     * @param <V>
     */
    public <K, V> void putActivityProduct(K cacheKey, V cacheVal) {
        synchronized (this) {
            expectActivityCacheSize = activityProductCacheContainer.size() + 1;
        }
        if (expectActivityCacheSize <= cacheProps.getActivityProductCacheSize()) {
            activityProductCacheContainer.putIfAbsent(cacheKey, cacheVal);
        }
    }

    /**
     * 添加活动商品缓存
     *
     * @param cacheList
     * @param <K>
     * @param <V>
     */
    public <K, V> void putActivityProduct(Map<K, V> cacheList) {
        synchronized (this) {
            expectActivityCacheSize = activityProductCacheContainer.size() + cacheList.size();
        }
        if (expectActivityCacheSize <= cacheProps.getActivityProductCacheSize()) {
            activityProductCacheContainer.putAll(cacheList);
        }
    }

    /**
     * 删除无效商品缓存数据
     *
     * @param cacheKeyList
     * @param <K>
     */
    public <K> void deleteActivityProduct(List<K> cacheKeyList) {
        cacheKeyList.forEach(x -> activityProductCacheContainer.remove(x));
    }

    /**
     * 删除无效商品缓存数据
     *
     * @param cacheKey
     * @param <K>
     */
    public <K> void deleteActivityProduct(K cacheKey) {
        if(activityProductCacheContainer.keySet().contains(cacheKey)){
            activityProductCacheContainer.remove(cacheKey);
        }
    }

    /**
     * 获取活动商品容器
     *
     * @param <K>
     * @param <V>
     * @return
     */
    public <K, V> ConcurrentMap<K, V> getActivityProductCacheContainer() {
        return activityProductCacheContainer;
    }

    /**
     * 获取多个key的缓存
     *
     * @param cacheKeyList
     * @param <K>
     * @param <V>
     * @return
     */
    public <K, V> Map<K, V> get(List<K> cacheKeyList) {
        return (Map<K, V>) cacheContainer.getAllPresent(cacheKeyList);
    }

    /**
     * 添加单个缓存信息
     *
     * @param cacheKey
     * @param cacheVal
     * @param <K>
     * @param <V>
     */
    public <K, V> void put(K cacheKey, V cacheVal) {
        cacheContainer.put(cacheKey, cacheVal);
    }

    /**
     * 添加多个缓存信息
     *
     * @param cacheMap
     * @param <K>
     * @param <V>
     */
    public <K, V> void put(Map<K, V> cacheMap) {
        cacheContainer.putAll(cacheMap);
    }

    /**
     * 获取多个key的缓存
     * 大道至简
     * 高度抽象
     * 但是会造成代码阅读性较低
     * 废弃不用
     *
     * @param queryParamList      queryParamList  查询数据的参数集合
     * @param generateKeyFunc     generateKeyFunc 创建缓存key的方法 返回map key为queryParam value为cacheKey
     * @param repositoryFunc      缓存没有命中获取数据的方法
     * @param mapperFunc          每个queryParam与数据的映射关系
     * @param checkCacheValidFunc 缓存数据检查是否有效
     * @param <K>                 查询参数类型
     * @param <CK>                缓存key参数类型
     * @param <V>                 缓存值类型
     * @param <Z>                 查询接口返回类型
     * @return
     */
    @Deprecated
    public <K, CK, V, Z> List<V> get(List<K> queryParamList,
                                     Function<List<K>, Map<List<K>, CK>> generateKeyFunc,
                                     Function<List<K>, Z> repositoryFunc,
                                     BiFunction<List<K>, List<V>, List<V>> mapperFunc,
                                     BiFunction<K, List<V>, Boolean> checkCacheValidFunc,
                                     BiFunction<List<V>, Z, List<V>> updateDataFunc) {
        Map<List<K>, CK> cacheKeyMap = generateKeyFunc.apply(queryParamList);
        List<V> cacheResultList = new ArrayList<>();

        Map<K, List<V>> cacheResultMap = cacheContainer.getAllPresent(cacheKeyMap.values());
        cacheResultList.addAll(Lists.newArrayList((V[]) cacheResultMap.values().toArray()));

        Set<K> cachedKeyList = cacheResultMap.keySet();
        //过滤参数列表将没有命中和业务缓存数据失效的数据一起重新查询数据
        Map<List<K>, CK> needReloadMap = cacheResultMap.isEmpty() ?
                cacheKeyMap :
                Maps.filterEntries(
                        cacheKeyMap, entry ->
                                //没有命中的数据
                                (!cachedKeyList.contains(entry.getValue()))
                                        //业务缓存失效
                                        && entry.getKey()
                                        .stream()
                                        .allMatch(
                                                entryKey ->
                                                        checkCacheValidFunc
                                                                .apply(entryKey, cacheResultMap.get(entry.getValue()))
                                        )
                );

        List<V> invalidCacheDataList = new ArrayList<>();
        cacheResultMap.forEach((x, y) -> {
            if (!checkCacheValidFunc.apply(x, y)) {
                invalidCacheDataList.addAll((List) y);
            }
        });
        cacheResultList.addAll(reloadDataToCache(needReloadMap,
                invalidCacheDataList,
                repositoryFunc,
                mapperFunc,
                updateDataFunc));
        return cacheResultList;
    }

    /**
     * 重新获取数据库数据并进行缓存
     *
     * @param queryParamWithCacheKeyMap 查询参与缓存key的map
     * @param repositoryFunc            缓存没有命中获取数据的方法
     * @param mapperFunc                每个queryParam与数据的映射关系
     * @param <K>
     * @param <V>
     * @return
     */
    private <K, CK, V, Z> List<V> reloadDataToCache(Map<List<K>, CK> queryParamWithCacheKeyMap,
                                                    List<V> partialInvalidCacheDataList,
                                                    Function<List<K>, Z> repositoryFunc,
                                                    BiFunction<List<K>, List<V>, List<V>> mapperFunc,
                                                    BiFunction<List<V>, Z, List<V>> updateDataFunc) {
        List<V> cacheResultList = new ArrayList<>();

        if (!queryParamWithCacheKeyMap.isEmpty()) {
            List<K> paramList = Lists.newArrayList();
            queryParamWithCacheKeyMap.keySet().forEach(x -> paramList.addAll(x));
            Z repositoryResultList = repositoryFunc
                    .apply(paramList);
            List<V> result = updateDataFunc.apply(partialInvalidCacheDataList, repositoryResultList);
            cacheResultList.addAll(result);

            queryParamWithCacheKeyMap.entrySet().forEach(reload -> {

                List<V> mapResult = mapperFunc.apply(reload.getKey(), result);
                if (mapResult != null && !mapResult.isEmpty()) {
                    cacheContainer.put(reload.getValue(), mapResult);
                }
            });
        }
        return cacheResultList;
    }
}
