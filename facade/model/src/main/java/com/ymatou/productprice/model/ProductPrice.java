package com.ymatou.productprice.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 商品价格信息
 * Created by chenpengxuan on 2017/3/1.
 */

public class ProductPrice {
    /**
     * 商品id
     */
    @JsonProperty("ProductId")
    private String productId;

    /**
     * 规格信息
     */
    @JsonProperty("Catalogs")
    private List<Catalog> catalogs;

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
    public List<Catalog> getCatalogs() {
        return catalogs;
    }

    @JsonIgnore
    public void setCatalogs(List<Catalog> catalogs) {
        this.catalogs = catalogs;
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
}
