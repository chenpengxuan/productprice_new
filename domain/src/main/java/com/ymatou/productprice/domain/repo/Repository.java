package com.ymatou.productprice.domain.repo;

import com.ymatou.productprice.domain.model.ActivityProduct;
import com.ymatou.productprice.domain.model.Catalog;
import com.ymatou.productprice.domain.model.ProductPriceData;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 仓储接口
 * Created by chenpengxuan on 2017/3/24.
 */
@Component
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
    ActivityProduct getActivityProduct(String productId);

    /**
     * 获取活动商品信息列表
     *
     * @param productIdList
     * @return
     */
    List<ActivityProduct> getActivityProductList(List<String> productIdList);

    /**
     * 根据商品id列表获取价格边界信息（用于新增接口->搜索商品列表）
     * @param productIdList
     * @return
     */
    List<ProductPriceData> getPriceRangeListByProduct(List<String> productIdList);
}
