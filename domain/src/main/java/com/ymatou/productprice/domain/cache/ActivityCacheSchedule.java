package com.ymatou.productprice.domain.cache;

import com.ymatou.productprice.infrastructure.config.props.BizProps;
import com.ymatou.productprice.infrastructure.config.props.CacheProps;
import com.ymatou.productprice.infrastructure.util.LogWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import javax.annotation.PostConstruct;
import java.util.concurrent.ScheduledFuture;

/**
 * 活动商品缓存定时器
 * Created by chenpengxuan on 2017/4/1.
 */
@Configuration
public class ActivityCacheSchedule {
    @Autowired
    private CacheProps cacheProps;

    @Autowired
    private BizProps bizProps;

    @Autowired
    private Cache cache;

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Autowired
    private LogWrapper logWrapper;

    private ScheduledFuture<?> future;

    private ScheduledFuture<?> futureRefreshActivity;

    private static String cronSetting;

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @PostConstruct
    public void init() {
        if (bizProps.isUseCache() && cacheProps.isUseActivityCache()) {
            //初始化活动商品缓存
            int recordCount = cache.initActivityProductCache();
            logWrapper.recordInfoLog("初始化活动商品缓存已执行,新增{}条", recordCount);

            cronSetting = bizProps.isUseCache() && cacheProps.isUseActivityCache() ? "0/" +
                    cacheProps.getActivityProductFrequency() + " * * * * ?" : "";

            //定时添加活动商品增量
            scheduler();
        }
    }

    /**
     * 定时任务
     */
    public void scheduler() {
        try {
            future = threadPoolTaskScheduler.schedule(() ->
                            cache.addNewestActivityProductCache(),
                    new CronTrigger(cronSetting));


            futureRefreshActivity = threadPoolTaskScheduler.schedule(() ->
                            cache.refreshActivityProductCache(),
                    new CronTrigger(cronSetting));

        } catch (Exception ex) {
            logWrapper.recordErrorLog("活动商品缓存定时任务发生异常", ex);
        }
    }
}
