package com.ymatou.productprice.model.req;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.QueryParam;

/**
 * 根据商品id获取价格信息
 * Created by chenpengxuan on 2017/3/1.
 */
public class GetPriceByProdIdRequest extends BaseRequest {
    /**
     * 买手id
     *
     */
    @QueryParam("BuyerId")
    @Min(value = 0,message = "买家id必须大于或者等于0,当等于0时表示当前买家尚未登录")
    private int buyerId;

    /**
     * 商品id
     */
    @QueryParam("ProductId")
    @NotNull(message = "商品id不能为空")
    private String productId;

    public int getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}
