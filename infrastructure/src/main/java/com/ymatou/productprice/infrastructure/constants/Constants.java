package com.ymatou.productprice.infrastructure.constants;


import com.ymatou.productprice.infrastructure.config.TomcatConfig;

/**
 * Created by zhangyifan on 2016/12/9.
 */
public class Constants {
    public static final String APP_ID = "productprice.iapi.ymatou.com";
    public static final String LOG_PREFIX = "logPrefix";
    public static TomcatConfig TOMCAT_CONFIG;
    public static final String SNAPSHOP_MQ_ID = "product";
    public static final String SNAPSHOP_MQ_CODE = "snapshotmq_from_apollo";

    /**
     * Mongo商品库名
     */
    public static final String ProductDb = "Products";

    /**
     * 规格库名
     */
    public static final String CatalogDb = "Catalogs";

    /**
     * 活动商品库名
     */
    public static final String ActivityProductDb = "ActivityProducts";

    /**
     * 活动商品库名
     */
    public static final String ProductDescriptionDb = "ProductDescriptions";

    /**
     * Mongo直播库名
     */
    public static final String LiveDb = "Lives";

    /**
     * 直播商品库名
     */
    public static final String LiveProudctDb = "LiveProducts";
}
