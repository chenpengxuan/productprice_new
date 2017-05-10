package com.ymatou.productprice.model.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ymatou.productprice.model.CatalogDeliveryInfo;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by chenpengxuan on 2017/5/8.
 */
public class GetCatalogPriceListByDeliveryExtraRequest extends BaseRequest{
    /**
     * 买手id
     *
     */
    @JsonProperty("BuyerId")
    @Min(value = 0,message = "买家id必须大于或者等于0,当等于0时表示当前买家尚未登录")
    private int buyerId;

    @JsonProperty("CatalogList")
    @NotNull(message = "规格id列表不能为空")
    private List<CatalogDeliveryInfo> catalogDeliveryInfoList;

    @JsonIgnore
    public int getBuyerId() {
        return buyerId;
    }

    @JsonIgnore
    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }

    @JsonIgnore
    public List<CatalogDeliveryInfo> getCatalogDeliveryInfoList() {
        return catalogDeliveryInfoList;
    }

    @JsonIgnore
    public void setCatalogDeliveryInfoList(List<CatalogDeliveryInfo> catalogDeliveryInfoList) {
        this.catalogDeliveryInfoList = catalogDeliveryInfoList;
    }
}
