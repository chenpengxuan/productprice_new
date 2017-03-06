/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.productprice.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * dubbox config
 */
@Configuration
@ImportResource("classpath:spring/dubbo-provider.xml")
public class DubboConfig {
}
