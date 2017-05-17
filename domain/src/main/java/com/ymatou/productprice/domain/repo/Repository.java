package com.ymatou.productprice.domain.repo;

import com.ymatou.productprice.domain.model.ActivityProduct;
import com.ymatou.productprice.domain.model.Catalog;
import com.ymatou.productprice.domain.model.ProductPriceData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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
     * 获取活动商品信息列表
     * @param productInActivityIdList
     * @return
     */
    List<ActivityProduct> getActivityProductListByInActivityIdList(List<Integer> productInActivityIdList);

    /**
     * 获取活动商品表中所有有效的活动商品id
     * @return
     */
    List<Integer> getValidProductInActivityIdList();

    /**
     * 根据商品id列表获取价格边界信息（用于新增接口->搜索商品列表）
     * @param productIdList
     * @return
     */
    List<ProductPriceData> getPriceRangeListByProduct(List<String> productIdList);

    /**
     * 根据商品id与时间戳列名获取对应时间戳
     * 用于缓存功能
     *
     * @param productId
     * @param stampKeyList
     * @return
     */
    Map<String, Object> getTimeStampByProductId(String productId, List<String> stampKeyList);

    /**
     * 根据商品id列表与时间戳列名获取对应时间戳
     *
     * @param productIdList
     * @param stampKeyList
     * @return
     */
    List<Map<String, Object>> getTimeStampByProductIdList(List<String> productIdList, List<String> stampKeyList);

    /**
     * 获取新增活动商品信息列表
     *
     * @param newestProductInActivityId 最新活动商品关联id
     * @return
     */
    List<ActivityProduct> getNewestActivityProductIdList(Integer newestProductInActivityId);

    /**
     * 根据规格id列表获取商品id规格id映射关系
     * @param catalogIdList
     * @return
     */
    List<Map<String, Object>> getProductIdByCatalogIdList(List<String> catalogIdList);

    /**
     * 根据商品id列表获取商品id规格id映射关系
     * @param catalogIdList
     * @return
     */
    List<Map<String, Object>> getCatalogIdByProductIdList(List<String> catalogIdList);

    /**
     * 获取全部有效活动商品列表
     * @return
     */
    List<ActivityProduct> getAllValidActivityProductList();

    /**
     * 获取商品多物流信息
     * @param productIdList
     * @return
     */
    List<Map<String, Object>> getMultiLogisticsByProductIdList(List<String> productIdList);
}
