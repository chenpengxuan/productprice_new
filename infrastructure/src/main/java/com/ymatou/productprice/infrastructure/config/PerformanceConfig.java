/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.productprice.infrastructure.config;

import com.ymatou.performancemonitorclient.PerformanceMonitorAdvice;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 性能监控配置
 *
 * @author luoshiqian
 */
@Aspect
@Configuration
public class PerformanceConfig {

    @Value("${performance.server.url}")
    private String performanceServerUrl;

    @Bean(name = "performanceMonitorAdvice")
    public PerformanceMonitorAdvice performanceMonitorAdvice() {
        PerformanceMonitorAdvice performanceMonitorAdvice = new PerformanceMonitorAdvice();
        performanceMonitorAdvice.setAppId("productprice.iapi.ymatou.com");
        performanceMonitorAdvice.setServerUrl(String.format("http://%s/api/perfmon", performanceServerUrl));
        return performanceMonitorAdvice;
    }

    @Bean(name = "performancePointcut")
    public AspectJExpressionPointcut aspectJExpressionPointcut() {
        AspectJExpressionPointcut aspectJExpressionPointcut = new AspectJExpressionPointcut();

        aspectJExpressionPointcut.setExpression(
                "execution(* com.ymatou.productprice.facade.*Facade.*(..))"
                        + "|| execution(* com.ymatou.productprice.domain.mongorepo.MongoRepository.excuteMongo(..))"
                        + "|| execution(* com.ymatou.productprice.domain.service.PriceQueryService.*(..))"
                        + "|| execution(* com.ymatou.productprice.facade.ProductPriceFacade.*(..))"
        );

        return aspectJExpressionPointcut;
    }


    /**
     * 对应xml
     * <aop:config>
     * <aop:advisor advice-ref="performanceMonitorAdvice"
     * pointcut-ref="performancePointcut" />
     * </aop:config>
     *
     * @return
     */
    @Bean
    public Advisor performanceMonitorAdvisor() {
        return new DefaultPointcutAdvisor(aspectJExpressionPointcut(), performanceMonitorAdvice());
    }

}
