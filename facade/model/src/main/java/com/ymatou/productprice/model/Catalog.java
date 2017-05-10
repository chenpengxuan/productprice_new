package com.ymatou.productprice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 商品规格
 * Created by chenpengxuan on 2017/3/1.
 */
public class Catalog {
    /**
     * 规格编号
     */
    @JsonProperty("CatalogId")
    private String catalogId;

    /**
     * 价格
     */
    @JsonProperty("Price")
    private double price;

    /**
     * 原价
     */
    @JsonProperty("QuotePrice")
    private double quotePrice;

    /**
     * 新客价
     */
    @JsonProperty("NewCustomerPrice")
    private double newCustomerPrice;

    /**
     * VIP价
     */
    @JsonProperty("VipPrice")
    private double vipPrice;

    /**
     * 活动价
     */
    @JsonProperty("ActivityPrice")
    private double activityPrice;

    /**
     * 活动新人价
     */
    @JsonProperty("SubsidyPrice")
    private double subsidyPrice;

    /**
     * 定金价
     */
    @JsonProperty("EarnestPrice")
    private double earnestPrice;

    /**
     * 价格类型
     */
    @JsonProperty("PriceType")
    private int priceType;

    /**
     * 商品id
     */
    @JsonIgnore
    private String productId;

    /**
     * 买手id
     */
    @JsonIgnore
    private Integer SellerId;

    /**
     * 该规格是否支持多物流
     */
    @JsonProperty("IsExtraDelivery")
    private boolean extraDelivery;

    /**
     * 多物流类型
     */
    @JsonIgnore
    private int multiLogistics;

    /**
     * 运费差价
     */
    @JsonIgnore
    private Double flightBalance;

    @JsonIgnore
    public String getCatalogId() {
        return catalogId;
    }

    @JsonIgnore
    public void setCatalogId(String catalogId) {
        this.catalogId = catalogId;
    }

    @JsonIgnore
    public double getPrice() {
        return price;
    }

    @JsonIgnore
    public void setPrice(double price) {
        this.price = price;
    }

    @JsonIgnore
    public double getQuotePrice() {
        return quotePrice;
    }

    @JsonIgnore
    public void setQuotePrice(double quotePrice) {
        this.quotePrice = quotePrice;
    }

    @JsonIgnore
    public double getNewCustomerPrice() {
        return newCustomerPrice;
    }

    @JsonIgnore
    public void setNewCustomerPrice(double newCustomerPrice) {
        this.newCustomerPrice = newCustomerPrice;
    }

    @JsonIgnore
    public double getVipPrice() {
        return vipPrice;
    }

    @JsonIgnore
    public void setVipPrice(double vipPrice) {
        this.vipPrice = vipPrice;
    }

    @JsonIgnore
    public double getActivityPrice() {
        return activityPrice;
    }

    @JsonIgnore
    public void setActivityPrice(double activityPrice) {
        this.activityPrice = activityPrice;
    }

    @JsonIgnore
    public double getSubsidyPrice() {
        return subsidyPrice;
    }

    @JsonIgnore
    public void setSubsidyPrice(double subsidyPrice) {
        this.subsidyPrice = subsidyPrice;
    }

    @JsonIgnore
    public double getEarnestPrice() {
        return earnestPrice;
    }

    @JsonIgnore
    public void setEarnestPrice(double earnestPrice) {
        this.earnestPrice = earnestPrice;
    }

    @JsonIgnore
    public int getPriceType() {
        return priceType;
    }

    @JsonIgnore
    public void setPriceType(int priceType) {
        this.priceType = priceType;
    }

    @JsonIgnore
    public String getProductId() {
        return productId;
    }

    @JsonIgnore
    public void setProductId(String productId) {
        this.productId = productId;
    }

    @JsonIgnore
    public Integer getSellerId() {
        return SellerId;
    }

    @JsonIgnore
    public void setSellerId(Integer sellerId) {
        SellerId = sellerId;
    }

    @JsonIgnore
    public boolean isExtraDelivery() {
        return extraDelivery;
    }

    @JsonIgnore
    public void setExtraDelivery(boolean extraDelivery) {
        this.extraDelivery = extraDelivery;
    }

    @JsonIgnore
    public Double getFlightBalance() {
        return flightBalance;
    }

    @JsonIgnore
    public void setFlightBalance(Double flightBalance) {
        this.flightBalance = flightBalance;
    }

    @JsonIgnore
    public int getMultiLogistics() {
        return multiLogistics;
    }

    @JsonIgnore
    public void setMultiLogistics(int multiLogistics) {
        this.multiLogistics = multiLogistics;
    }
}
