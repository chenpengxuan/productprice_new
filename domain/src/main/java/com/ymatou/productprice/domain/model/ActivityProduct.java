package com.ymatou.productprice.domain.model;

import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

/**
 * 活动商品信息
 * Created by chenpengxuan on 2017/3/28.
 */
public class ActivityProduct {
    /**
     * 活动商品Mongo表主键
     */
    private ObjectId activityProductId;

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


    public ObjectId getActivityProductId() {
        return activityProductId;
    }

    public void setActivityProductId(ObjectId activityProductId) {
        this.activityProductId = activityProductId;
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
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
