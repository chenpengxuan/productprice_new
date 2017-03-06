package com.ymatou.productprice.facade;

import com.ymatou.productprice.model.req.GetPriceByProdIdRequest;
import com.ymatou.productprice.model.resp.BaseResponse;

/**
 * 商品价格服务
 * Created by chenpengxuan on 2017/3/1.
 */
public interface ProductPriceFacade {
    /**
     * 点火
     * @return
     */
    String warmUp();

    /**
     * 根据商品id获取价格信息
     * @param request
     * @return
     */
    BaseResponse getPriceByProdId(GetPriceByProdIdRequest request);
}
