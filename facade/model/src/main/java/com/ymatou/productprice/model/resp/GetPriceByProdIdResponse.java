package com.ymatou.productprice.model.resp;

import com.ymatou.productprice.model.ProductPrice;

/**
 * 根据商品id获取价格信息
 * Created by chenpengxuan on 2017/3/2.
 */
public class GetPriceByProdIdResponse extends BaseResponse{
    /**
     * 价格信息
     */
    public ProductPrice PriceInfo;
}
