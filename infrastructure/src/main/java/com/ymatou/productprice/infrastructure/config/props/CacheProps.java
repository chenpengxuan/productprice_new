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
}
