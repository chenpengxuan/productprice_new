package com.ymatou.productprice.domain.model;

import java.util.Date;

/**
 * 规格价格信息
 * Created by chenpengxuan on 2017/3/28.
 */
public class Catalog {
    /**
     * 规格id
     */
    private String catalogId;

    /**
     * 原价
     */
    private Double quotePrice;

    /**
     * 新客价
     */
    private Double newCustomerPrice;

    /**
     * vip价
     */
    private Double vipPrice;

    /**
     * 买手id
     */
    private Integer sellerId;

    /**
     * 商品id
     */
    private String productId;

    /**
     * 更新时间
     */
    private Date updateTime;

    public String getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(String catalogId) {
        this.catalogId = catalogId;
    }

    public Double getQuotePrice() {
        return quotePrice;
    }

    public void setQuotePrice(Double quotePrice) {
        this.quotePrice = quotePrice;
    }

    public Double getNewCustomerPrice() {
        return newCustomerPrice;
    }

    public void setNewCustomerPrice(Double newCustomerPrice) {
        this.newCustomerPrice = newCustomerPrice;
    }

    public Double getVipPrice() {
        return vipPrice;
    }

    public void setVipPrice(Double vipPrice) {
        this.vipPrice = vipPrice;
    }

    public Integer getSellerId() {
        return sellerId;
    }

    public void setSellerId(Integer sellerId) {
        this.sellerId = sellerId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
