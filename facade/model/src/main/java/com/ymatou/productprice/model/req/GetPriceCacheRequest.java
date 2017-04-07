package com.ymatou.productprice.model.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by chenpengxuan on 2017/4/6.
 */
public class GetPriceCacheRequest extends BaseRequest{
    /**
     * 商品id
     */
    @JsonProperty("ProductIdList")
    @NotNull(message = "商品id列表不能为空")
    private List<String> productIdList;

    @JsonIgnore
    public List<String> getProductIdList() {
        return productIdList;
    }

    @JsonIgnore
    public void setProductIdList(List<String> productIdList) {
        this.productIdList = productIdList;
    }
}
