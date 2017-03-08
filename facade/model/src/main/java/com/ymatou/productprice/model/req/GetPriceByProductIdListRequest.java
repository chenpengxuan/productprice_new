package com.ymatou.productprice.model.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 根据商品id列表获取价格信息
 * Created by chenpengxuan on 2017/3/8.
 */
public class GetPriceByProductIdListRequest {
    /**
     * 买手id
     *
     */
    @JsonProperty("BuyerId")
    @Min(value = 0,message = "买家id必须大于或者等于0,当等于0时表示当前买家尚未登录")
    private int buyerId;

    /**
     * 商品id
     */
    @JsonProperty("ProductIds")
    @NotNull(message = "商品id列表不能为空")
    private List<String> productIdList;

    @JsonIgnore
    public int getBuyerId() {
        return buyerId;
    }

    @JsonIgnore
    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }

    @JsonIgnore
    public List<String> getProductIdList() {
        return productIdList;
    }

    @JsonIgnore
    public void setProductIdList(List<String> productIdList) {
        this.productIdList = productIdList;
    }
}
