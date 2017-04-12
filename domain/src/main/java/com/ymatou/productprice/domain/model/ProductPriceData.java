package com.ymatou.productprice.domain.model;

import java.util.Date;
import java.util.List;

/**
 * 商品价格信息
 * Created by chenpengxuan on 2017/3/28.
 */
public class ProductPriceData {

    /**
     * 商品id
     */
    private String productId;

    /**
     * 价格最大区间
     */
    private String priceMaxRange;

    /**
     * 价格最小区间
     */
    private String priceMinRange;

    /**
     * 买手id
     */
    private Integer sellerId;

    /**
     * 未下过单或者所有订单都已取消。
     * 用作是否享受新客价的条件之一
     */
    private Boolean noOrdersOrAllCancelled;

    /**
     * 是否有确认收货的订单.
     * 用作是否享受VIP价的条件之一
     */
    private Boolean hasConfirmedOrders;

    /**
     * 商品规格价格信息列表
     */
    private List<Catalog> catalogList;

    /**
     * 更新时间
     */
    private Date updateTime;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getPriceMaxRange() {
        return priceMaxRange;
    }

    public void setPriceMaxRange(String priceMaxRange) {
        this.priceMaxRange = priceMaxRange;
    }

    public String getPriceMinRange() {
        return priceMinRange;
    }

    public void setPriceMinRange(String priceMinRange) {
        this.priceMinRange = priceMinRange;
    }

    public Integer getSellerId() {
        return sellerId;
    }

    public void setSellerId(Integer sellerId) {
        this.sellerId = sellerId;
    }

    public Boolean getNoOrdersOrAllCancelled() {
        return noOrdersOrAllCancelled;
    }

    public void setNoOrdersOrAllCancelled(Boolean noOrdersOrAllCancelled) {
        this.noOrdersOrAllCancelled = noOrdersOrAllCancelled;
    }

    public Boolean getHasConfirmedOrders() {
        return hasConfirmedOrders;
    }

    public void setHasConfirmedOrders(Boolean hasConfirmedOrders) {
        this.hasConfirmedOrders = hasConfirmedOrders;
    }

    public List<Catalog> getCatalogList() {
        return catalogList;
    }

    public void setCatalogList(List<Catalog> catalogList) {
        this.catalogList = catalogList;
    }

    public Date getUpdateTime() {
        return updateTime == null ? null : (Date)updateTime.clone();
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime == null ? null : (Date)updateTime.clone();
    }
}
