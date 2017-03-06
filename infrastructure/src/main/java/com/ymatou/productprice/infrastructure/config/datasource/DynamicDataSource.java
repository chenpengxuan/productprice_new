package com.ymatou.productprice.infrastructure.config.datasource;

/**
 * Created by chenpengxuan on 2016/9/1.
 */
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDataSourceContextHolder.getDataSourceType();
    }

}

