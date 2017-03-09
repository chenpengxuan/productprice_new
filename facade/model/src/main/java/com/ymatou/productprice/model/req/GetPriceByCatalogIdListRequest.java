package com.ymatou.productprice.model.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 根据规格id列表获取价格信息
 * Created by chenpengxuan on 2017/3/9.
 */
public class GetPriceByCatalogIdListRequest extends BaseRequest{
    /**
     * 买手id
     *
     */
    @JsonProperty("BuyerId")
    @Min(value = 0,message = "买家id必须大于或者等于0,当等于0时表示当前买家尚未登录")
    private int buyerId;

    @JsonProperty("BuyerId")
    @NotNull(message = "规格id列表不能为空")
    private List<String> catalogIdList;

    @JsonIgnore
    public int getBuyerId() {
        return buyerId;
    }

    @JsonIgnore
    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }

    @JsonIgnore
    public List<String> getCatalogIdList() {
        return catalogIdList;
    }

    @JsonIgnore
    public void setCatalogIdList(List<String> catalogIdList) {
        this.catalogIdList = catalogIdList;
    }
}
