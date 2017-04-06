package com.ymatou.productprice.model.req;

import javax.validation.constraints.NotNull;
import javax.ws.rs.QueryParam;
import java.util.List;

/**
 * Created by chenpengxuan on 2017/4/6.
 */
public class GetPriceCacheRequest extends BaseRequest{
    /**
     * 商品id
     */
    @QueryParam("productIdList")
    @NotNull(message = "商品id列表不能为空")
    private List<String> productIdList;

    public List<String> getProductIdList() {
        return productIdList;
    }

    public void setProductIdList(List<String> productIdList) {
        this.productIdList = productIdList;
    }
}
