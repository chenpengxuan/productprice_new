/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.productprice.infrastructure.util;

import com.ymatou.productprice.infrastructure.constants.Constants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 线程池 增加跟踪日志requestId
 *
 * @author luoshiqian 2016/8/1 12:39
 */
public class ThreadPoolContextTaskExecutor extends ThreadPoolTaskExecutor {

    private static final long serialVersionUID = 8600084667597486819L;

    @Override
    public void execute(Runnable task) {
        String logPrefix = MDC.get(Constants.LOG_PREFIX);
        super.execute(() -> {
            if (StringUtils.isNotBlank(logPrefix)) {
                MDC.put(Constants.LOG_PREFIX, logPrefix);
            }
            try {
                task.run();
            } finally {
                MDC.clear();
            }
        });
    }
}
