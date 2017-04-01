package com.ymatou.productprice.infrastructure.config.props;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import org.springframework.stereotype.Component;

/**
 * 缓存相关配置
 * Created by chenpengxuan on 2017/3/21.
 */
@Component
@DisconfFile(fileName = "cache.properties")
public class CacheProps {
    /**
     * 缓存类型
     */
    private String cacheType;

    /**
     * 缓存条目数
     */
    private long cacheSize;

    /**
     * 缓存过期时间
     */
    private int expireTime;

    /**
     * 写缓存的线程数
     */
    private int writeConcurrencyNum;

    /**
     * 活动商品缓存条目数
     */
    private int activityProductCacheSize;

    /**
     * 是否使用活动缓存
     */
    private boolean useActivityCache;

    /**
     * #活动商品获取增量时间频次(单位：秒)
     */
    private int activityProductFrequency;

    @DisconfFileItem(name = "cacheType")
    public String getCacheType() {
        return cacheType;
    }

    public void setCacheType(String cacheType) {
        this.cacheType = cacheType;
    }

    @DisconfFileItem(name = "cacheSize")
    public long getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(long cacheSize) {
        this.cacheSize = cacheSize;
    }

    @DisconfFileItem(name = "expireTime")
    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }

    @DisconfFileItem(name = "writeConcurrencyNum")
    public int getWriteConcurrencyNum() {
        return writeConcurrencyNum;
    }

    public void setWriteConcurrencyNum(int writeConcurrencyNum) {
        this.writeConcurrencyNum = writeConcurrencyNum;
    }

    @DisconfFileItem(name = "activityProductCacheSize")
    public int getActivityProductCacheSize() {
        return activityProductCacheSize;
    }

    public void setActivityProductCacheSize(int activityProductCacheSize) {
        this.activityProductCacheSize = activityProductCacheSize;
    }

    @DisconfFileItem(name = "useActivityCache")
    public boolean isUseActivityCache() {
        return useActivityCache;
    }

    public void setUseActivityCache(boolean useActivityCache) {
        this.useActivityCache = useActivityCache;
    }

    @DisconfFileItem(name = "activityProductFrequency")
    public int getActivityProductFrequency() {
        return activityProductFrequency;
    }

    public void setActivityProductFrequency(int activityProductFrequency) {
        this.activityProductFrequency = activityProductFrequency;
    }
}
