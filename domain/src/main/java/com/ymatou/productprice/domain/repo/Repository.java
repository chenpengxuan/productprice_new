package com.ymatou.productprice.domain.repo;

import com.ymatou.productprice.model.Catalog;

import java.util.List;
import java.util.Map;

/**
 * 仓储接口
 * Created by chenpengxuan on 2017/3/24.
 */
public interface Repository {

    /**
     * 获取规格信息列表
     *
     * @param productId
     * @return
     */
    List<Catalog> getCatalogListByProduct(String productId);

    /**
     * 获取规格信息列表
     *
     * @param productIdList
     * @return
     */
    List<Catalog> getCatalogListByProduct(List<String> productIdList);

    /**
     * 根据规格id获取规格信息列表
     *
     * @param catalogIdList
     * @return
     */
    List<Catalog> getCatalogByCatalogId(List<String> catalogIdList);

    /**
     * 获取活动商品信息
     *
     * @param productId
     * @return
     */
    Map<String, Object> getActivityProduct(String productId);

    /**
     * 获取活动商品信息列表
     *
     * @param productIdList
     * @return
     */
    List<Map<String, Object>> getActivityProductList(List<String> productIdList);
}
