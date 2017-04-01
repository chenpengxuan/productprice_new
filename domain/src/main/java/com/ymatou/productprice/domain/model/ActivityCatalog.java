package com.ymatou.productprice.domain.model;

/**
 * 活动商品规格
 * Created by chenpengxuan on 2017/3/28.
 */
public class ActivityCatalog {
    /**
     * 活动规格id
     */
    private String activityCatalogId;

    /**
     * 活动规格价格
     */
    private Double activityCatalogPrice;

    /**
     * 活动规格库存
     */
    private Integer activityStock;

    public String getActivityCatalogId() {
        return activityCatalogId;
    }

    public void setActivityCatalogId(String activityCatalogId) {
        this.activityCatalogId = activityCatalogId;
    }

    public Double getActivityCatalogPrice() {
        return activityCatalogPrice;
    }

    public void setActivityCatalogPrice(Double activityCatalogPrice) {
        this.activityCatalogPrice = activityCatalogPrice;
    }

    public Integer getActivityStock() {
        return activityStock;
    }

    public void setActivityStock(Integer activityStock) {
        this.activityStock = activityStock;
    }
}
