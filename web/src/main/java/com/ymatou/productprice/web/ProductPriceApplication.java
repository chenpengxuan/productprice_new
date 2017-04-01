package com.ymatou.productprice.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

//// TODO: 2017/3/27 1.缓存不基于业务场景，去除缓存key中的methodname
//TODO 2.repository返回数据结构化
//TODO 3.活动商品全量缓存，不基于lru机制
//TODO 4.活动商品增量获取 5秒一次刷objectId的时间戳比较大小拿增量数据
//TODO 5.wiki上落地所有接口的查询数据的活动图逻辑
@Configuration
@EnableAutoConfiguration
		(exclude = {DataSourceAutoConfiguration.class
				,DataSourceTransactionManagerAutoConfiguration.class
				,JpaBaseConfiguration.class, HibernateJpaAutoConfiguration.class, PersistenceExceptionTranslationAutoConfiguration.class
				, MongoAutoConfiguration.class, MongoDataAutoConfiguration.class
		})
//@MapperScan(basePackages = {"com.ymatou.productprice.domain.sqlrepo"})
@ImportResource({"classpath:datasource-disconf.xml"})
@ComponentScan("com.ymatou")
public class ProductPriceApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(com.ymatou.productprice.web.ProductPriceApplication.class);
		app.run(args);
	}
}
