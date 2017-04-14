package com.ymatou.productprice.domain.model;

import java.util.Date;
import java.util.List;

/**
 * 活动商品信息
 * Created by chenpengxuan on 2017/3/28.
 */
public class ActivityProduct {
    /**
     * 活动商品id
     */
    private Integer productInActivityId;

    /**
     * 商品id
     */
    private String productId;

    /**
     * 是否交易隔离的活动
     */
    private Boolean hasIsolation;

    /**
     * 是否为新人活动
     */
    private Boolean newBuyer;

    /**
     * 活动商品规格信息列表
     */
    private List<ActivityCatalog> activityCatalogList;

    /**
     * 活动开始时间
     */
    private Date startTime;

    /**
     * 活动结束时间
     */
    private Date endTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    public Integer getProductInActivityId() {
        return productInActivityId;
    }

    public void setProductInActivityId(Integer productInActivityId) {
        this.productInActivityId = productInActivityId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Boolean getHasIsolation() {
        return hasIsolation;
    }

    public void setHasIsolation(Boolean hasIsolation) {
        this.hasIsolation = hasIsolation;
    }

    public Boolean getNewBuyer() {
        return newBuyer;
    }

    public void setNewBuyer(Boolean newBuyer) {
        this.newBuyer = newBuyer;
    }

    public List<ActivityCatalog> getActivityCatalogList() {
        return activityCatalogList;
    }

    public void setActivityCatalogList(List<ActivityCatalog> activityCatalogList) {
        this.activityCatalogList = activityCatalogList;
    }

    public Date getStartTime() {
        return startTime == null ? null : (Date)startTime.clone();
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime == null ? null :(Date) startTime.clone();
    }

    public Date getEndTime() {
        return endTime == null ? null :(Date)endTime.clone();
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime == null ? null : (Date)endTime.clone();
    }

    public Date getUpdateTime() {
        return updateTime == null ? null : (Date)updateTime.clone();
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime == null ? null : (Date)updateTime.clone();
    }
}
