package com.ymatou.productprice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by chenpengxuan on 2017/5/8.
 */
public class CatalogDeliveryInfo {
    /**
     * 规格id
     */
    @JsonProperty("CatalogId")
    private String catalogId;

    /**
     * 多物流类型
     */
    @JsonProperty("DeliveryType")
    private Integer deliveryType;

    @JsonIgnore
    public String getCatalogId() {
        return catalogId;
    }

    @JsonIgnore
    public void setCatalogId(String catalogId) {
        this.catalogId = catalogId;
    }

    @JsonIgnore
    public Integer getDeliveryType() {
        return deliveryType;
    }

    @JsonIgnore
    public void setDeliveryType(Integer deliveryType) {
        this.deliveryType = deliveryType;
    }
}
