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
     * 针对单次查询数量过大的情况 做出查询数量限制
     */
    public static final Integer FORK_COUNT_LIMIT = 30;

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
     * 商品变更边界时间戳库名
     */
    public static final String ProductTimeStampDb = "ProductTimeStamp";

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
