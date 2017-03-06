package com.ymatou.productprice.infrastructure.config.datasource;

import com.ymatou.productprice.infrastructure.util.LogWrapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Created by chenpengxuan on 2016/9/1.
 */
@Aspect
@Order(-1) // 保证该AOP在@Transactional之前执行
@Component
public class DynamicDataSourceAspect {

    private final Logger logger = LoggerFactory.getLogger(DynamicDataSourceAspect.class);

    @Autowired
    private LogWrapper logWrapper;

    @Pointcut("execution(* com.ymatou.productprice.domain.sqlrepo.*Query.*(..))")
    public void executeRepository() {
    }


    @Before("executeRepository()")
    public void before(JoinPoint point) throws Exception{
        Object target = point.getTarget();
        String method = point.getSignature().getName();

        Class<?>[] classz = target.getClass().getInterfaces();

        Class<?>[] parameterTypes = ((MethodSignature) point.getSignature()).getMethod().getParameterTypes();
        try {
            Method m = classz[0].getMethod(method, parameterTypes);
            if (m != null && m.isAnnotationPresent(TargetDataSource.class)) {
                TargetDataSource data = m.getAnnotation(TargetDataSource.class);
                DynamicDataSourceContextHolder.setDataSourceType(data.value());
                logger.debug("DataSource：" + data.value());
            }else {
                throw new Exception("缺少数据库注解");
            }
        } catch (Exception e) {
            logWrapper.recordErrorLog(e.getMessage(),e);
        }
    }

    @After("executeRepository()")
    public void restoreDataSource(JoinPoint point) {
        DynamicDataSourceContextHolder.clearDataSourceType();
    }
}