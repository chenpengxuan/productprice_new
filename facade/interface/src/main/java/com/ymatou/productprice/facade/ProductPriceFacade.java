package com.ymatou.productprice.facade;

import com.ymatou.productprice.model.req.*;
import com.ymatou.productprice.model.resp.BaseResponseNetAdapter;

/**
 * 商品价格服务
 * Created by chenpengxuan on 2017/3/1.
 */
public interface ProductPriceFacade {
    /**
     * 根据商品id获取价格信息
     * @param request
     * @return
     */
    BaseResponseNetAdapter getPriceByProdId(GetPriceByProdIdRequest request);

    /**
     * 根据商品id获取交易隔离价格信息
     * @param request
     * @return
     */
    BaseResponseNetAdapter getPriceByProdIdWithTradeIsolation(GetPriceByProdIdRequest request);

    /**
     * 根据商品id列表获取价格信息（用于直播中商品列表）
     * @param request
     * @return
     */
    BaseResponseNetAdapter getPriceByProductIdList(GetPriceByProductIdListRequest request);

    /**
     * 根据商品id列表获取交易隔离价格信息（用于直播中商品列表）
     * @param request
     * @return
     */
    BaseResponseNetAdapter getPriceByProductIdListWithTradeIsolation(GetPriceByProductIdListRequest request);

    /**
     * 新增接口
     * 根据商品id列表获取价格信息（用于搜索后的商品列表）
     * @param request
     * @return
     */
    BaseResponseNetAdapter getPriceByProductIdListForSearchedList(GetPriceByProductIdListRequest request);

    /**
     * 新增接口
     * 根据商品id列表获取交易隔离价格信息（用于搜索后的商品列表）
     * @param request
     * @return
     */
    BaseResponseNetAdapter getPriceByProductIdListWithTradeIsolationForSearchedList(GetPriceByProductIdListRequest request);

    /**
     * 根据规格id列表获取价格信息
     * @param request
     * @return
     */
    BaseResponseNetAdapter getPriceByCatalogIdList(GetPriceByCatalogIdListRequest request);

    /**
     * 根据规格id列表获取交易隔离价格信息
     * @param request
     * @return
     */
    BaseResponseNetAdapter getPriceByCatalogIdListWithTradeIsolation(GetPriceByCatalogIdListRequest request);

    /**
     * 根据商品id获取缓存信息
     * @param request
     * @return
     */
    BaseResponseNetAdapter getCacheInfoByProductId(GetPriceCacheRequest request);

    /**
     * 获取缓存统计信息
     * @return
     */
    BaseResponseNetAdapter getCacheInfoStatsInfo();

    /**
     * 根据规格id列表获取交易隔离价格信息(多物流)
     * @param request
     * @return
     */
    BaseResponseNetAdapter getPriceByCatalogIdListByDeliveryExtraWithTradeIsolation(GetCatalogPriceListByDeliveryExtraRequest request);

    /**
     * 根据规格id列表获取价格信息(多物流)
     * @param request
     * @return
     */
    BaseResponseNetAdapter getPriceByCatalogIdListByDeliveryExtra(GetCatalogPriceListByDeliveryExtraRequest request);
}
