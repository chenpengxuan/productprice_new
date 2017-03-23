package com.ymatou.productprice.infrastructure.util.CacheUtil;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.ymatou.productprice.infrastructure.config.props.CacheProps;
import com.ymatou.productprice.infrastructure.util.LogWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 缓存管理类
 * Created by chenpengxuan on 2017/3/21.
 */
@Component
public class CacheManager<K, V> {

    @Autowired
    private CacheProps cacheProps;

    @Autowired
    private LogWrapper logWrapper;

    private Cache<K, V> singleResultCacheFactory;

    private Cache<K, List<V>> listResultCacheFactory;

    @PostConstruct
    public void init() {
        switch (CacheTypeEnum.valueOf(cacheProps.getCacheType().toUpperCase())) {
            case GUAVACACHE:
                singleResultCacheFactory = CacheBuilder.newBuilder()
                        .maximumSize(cacheProps.getCacheSize())
                        .expireAfterAccess(cacheProps.getExpireTime(), TimeUnit.HOURS)
                        .concurrencyLevel(cacheProps.getWriteConcurrencyNum())
                        .recordStats()
                        .build();
                listResultCacheFactory = CacheBuilder.newBuilder()
                        .maximumSize(cacheProps.getCacheSize())
                        .expireAfterAccess(cacheProps.getExpireTime(), TimeUnit.HOURS)
                        .concurrencyLevel(cacheProps.getWriteConcurrencyNum())
                        .recordStats()
                        .build();
                break;
            case EHCACHE:
                break;
            default:
                break;
        }
    }

    /**
     * 获取单个key的缓存
     *
     * @param queryParam
     * @param generateKeyFunc
     * @param repositoryFunc
     * @return
     */
    public V getWithSingleKey(K queryParam, Function<K, K> generateKeyFunc, Function<K, V> repositoryFunc) {
        K cacheKey = generateKeyFunc.apply(queryParam);
        V cacheResult = null;
        try {
            cacheResult = singleResultCacheFactory.get(cacheKey, null);
        } catch (ExecutionException e) {
            logWrapper.recordErrorLog("获取缓存发生异常_getWithSingleKey{}", e);
        }
        if (cacheResult == null) {
            cacheResult = repositoryFunc.apply(queryParam);
            singleResultCacheFactory.put(cacheKey, cacheResult);
        }
        return cacheResult;
    }

    /**
     * 获取多个key的缓存
     *
     * @param queryParamList  查询数据的参数集合
     * @param generateKeyFunc 创建缓存key的方法 返回map key为queryparam value为cachekey
     * @param repositoryFunc  缓存没有命中获取数据的方法
     * @param mapperFunc
     * @return
     */
    public List<V> getWithMultipleKey(List<K> queryParamList,
                                      Function<List<K>, Map<K, K>> generateKeyFunc,
                                      Function<List<K>, List<V>> repositoryFunc,
                                      BiFunction<K, List<V>, List<V>> mapperFunc) {
        Map<K, K> cacheKeyMap = generateKeyFunc.apply(queryParamList);
        List<V> cacheResultList = new ArrayList<>();

        Map<K,List<V>> cacheResultMap = listResultCacheFactory.getAllPresent(cacheKeyMap.values());
        cacheResultList.addAll(Arrays.asList((V[]) cacheResultMap.values().toArray()));

        Set<K> cachedKeyList = cacheResultMap.keySet();
        Map<K, K> needReloadMap = cacheResultMap.isEmpty() ? cacheKeyMap : Maps.filterEntries(cacheKeyMap, kkEntry -> !cachedKeyList.contains(kkEntry.getValue()));

        if(!needReloadMap.isEmpty()) {
            List<V> repositoryResultList = repositoryFunc.apply(Arrays.asList((K[]) needReloadMap.keySet().toArray()));
            cacheResultList.addAll(repositoryResultList);

            needReloadMap.entrySet().stream().forEach(reload -> {

                List<V> mapResult = mapperFunc.apply(reload.getKey(), repositoryResultList);

                listResultCacheFactory.put(reload.getValue(), mapResult);
            });
        }
        return cacheResultList;
    }
}
