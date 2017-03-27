package com.ymatou.productprice.domain.repo.parallelrepo;

import com.ymatou.productprice.domain.repo.mongorepo.MongoRepository;
import com.ymatou.productprice.domain.repo.Repository;
import com.ymatou.productprice.infrastructure.util.ParallelUtil.ParallelProcessor;
import com.ymatou.productprice.model.Catalog;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * 并行仓储操作相关
 * Created by chenpengxuan on 2017/3/24.
 */
public class ParallelRepository implements Repository {
    @Autowired
    private ParallelProcessor parallelProcessor;

    @Autowired
    private MongoRepository mongoRepository;

    /**
     * 获取规格信息列表
     * @param productId
     * @return
     */
    @Override
    public List<Catalog> getCatalogListByProduct(String productId) {
        return mongoRepository.getCatalogListByProduct(productId);
    }

    /**
     * 获取规格信息列表
     * @param productIdList
     * @return
     */
    @Override
    public List<Catalog> getCatalogListByProduct(List<String> productIdList) {
        return parallelProcessor.doParallelProcess(productIdList, obj ->
                mongoRepository.getCatalogListByProduct((List<String>) obj));
    }

    /**
     *根据规格id获取规格信息列表
     * @param catalogIdList
     * @return
     */
    @Override
    public List<Catalog> getCatalogByCatalogId(List<String> catalogIdList) {
        return parallelProcessor.doParallelProcess(catalogIdList, obj ->
                mongoRepository.getCatalogByCatalogId((List<String>) obj));
    }

    /**
     * 获取活动商品信息
     * @param productId
     * @return
     */
    @Override
    public Map<String, Object> getActivityProduct(String productId) {
        return mongoRepository.getActivityProduct(productId);
    }

    /**
     * 获取活动商品信息列表
     * @param productIdList
     * @return
     */
    @Override
    public List<Map<String, Object>> getActivityProductList(List<String> productIdList) {
        return parallelProcessor.doParallelProcess(productIdList, obj ->
                mongoRepository.getActivityProductList((List<String>) obj));
    }

    /**
     * 根据商品id列表获取价格边界信息（用于新增接口->搜索商品列表）
     * @param productIdList
     * @return
     */
    @Override
    public List<Map<String, Object>> getPriceRangeListByProduct(List<String> productIdList) {
        return parallelProcessor.doParallelProcess(productIdList,obj ->
                mongoRepository.getPriceRangeListByProduct((List<String>) obj));
    }
}
