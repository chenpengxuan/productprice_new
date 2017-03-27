package com.ymatou.productprice.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 商品价格信息(用于搜索后的商品列表)
 * Created by chenpengxuan on 2017/3/1.
 */

public class ProductPriceForSearched {
    /**
     * 商品id
     */
    @JsonProperty("ProductId")
    private String productId;

    /**
     * 最高最终价格
     */
    @JsonProperty("MaxPrice")
    private double maxPrice;

    /**
     * 最低最终价格
     */
    @JsonProperty("MinPrice")
    private double minPrice;

    /**
     * 最低原价
     */
    @JsonProperty("MinOriPrice")
    private double minOriginalPrice;

    /**
     * 最高原价
     */
    @JsonProperty("MaxOriPrice")
    private double maxOriginalPrice;

    /**
     * 最低新客价
     */
    @JsonProperty("MinNewpPrice")
    private double minNewpersonPrice;

    /**
     * 最高新客价
     */
    @JsonProperty("MaxNewpPrice")
    private double maxNewpersonPrice;

    /**
     * 最低vip价格
     */
    @JsonProperty("MinVipPrice")
    private double minVipPrice;

    /**
     * 最高vip价格
     */
    @JsonProperty("MaxVipPrice")
    private double maxVipPrice;

    /**
     * 价格类型
     */
    @JsonProperty("PriceType")
    private int priceType;

    /**
     * 未下过单或者所有订单都已取消。用作是否享受新客价的条件之一
     */
    @JsonProperty("NoOrdersOrAllCancelled")
    private Boolean noOrdersOrAllCancelled;

    /**
     * 是否有确认收货的订单，用作是否享受VIP价的条件
     */
    @JsonProperty("HasConfirmedOrders")
    private Boolean hasConfirmedOrders;

    /**
     * 买手id
     */
    @JsonIgnore
    private Long sellerId;

    @JsonIgnore
    public String getProductId() {
        return productId;
    }

    @JsonIgnore
    public void setProductId(String productId) {
        this.productId = productId;
    }

    @JsonIgnore
    public Boolean getNoOrdersOrAllCancelled() {
        return noOrdersOrAllCancelled;
    }

    @JsonIgnore
    public void setNoOrdersOrAllCancelled(Boolean noOrdersOrAllCancelled) {
        this.noOrdersOrAllCancelled = noOrdersOrAllCancelled;
    }

    @JsonIgnore
    public Boolean getHasConfirmedOrders() {
        return hasConfirmedOrders;
    }

    @JsonIgnore
    public void setHasConfirmedOrders(Boolean hasConfirmedOrders) {
        this.hasConfirmedOrders = hasConfirmedOrders;
    }

    @JsonIgnore
    public Long getSellerId() {
        return sellerId;
    }

    @JsonIgnore
    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(double minPrice) {
        this.minPrice = minPrice;
    }

    public int getPriceType() {
        return priceType;
    }

    public void setPriceType(int priceType) {
        this.priceType = priceType;
    }

    public double getMinOriginalPrice() {
        return minOriginalPrice;
    }

    public void setMinOriginalPrice(double minOriginalPrice) {
        this.minOriginalPrice = minOriginalPrice;
    }

    public double getMaxOriginalPrice() {
        return maxOriginalPrice;
    }

    public void setMaxOriginalPrice(double maxOriginalPrice) {
        this.maxOriginalPrice = maxOriginalPrice;
    }

    public double getMinNewpersonPrice() {
        return minNewpersonPrice;
    }

    public void setMinNewpersonPrice(double minNewpersonPrice) {
        this.minNewpersonPrice = minNewpersonPrice;
    }

    public double getMaxNewpersonPrice() {
        return maxNewpersonPrice;
    }

    public void setMaxNewpersonPrice(double maxNewpersonPrice) {
        this.maxNewpersonPrice = maxNewpersonPrice;
    }

    public double getMinVipPrice() {
        return minVipPrice;
    }

    public void setMinVipPrice(double minVipPrice) {
        this.minVipPrice = minVipPrice;
    }

    public double getMaxVipPrice() {
        return maxVipPrice;
    }

    public void setMaxVipPrice(double maxVipPrice) {
        this.maxVipPrice = maxVipPrice;
    }
}
