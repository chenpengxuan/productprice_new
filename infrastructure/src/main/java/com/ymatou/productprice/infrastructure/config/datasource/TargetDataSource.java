package com.ymatou.productprice.infrastructure.config.datasource;

import java.lang.annotation.*;

/**
 * 自定义注解用于指定数据源
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface TargetDataSource {
    String value();
}