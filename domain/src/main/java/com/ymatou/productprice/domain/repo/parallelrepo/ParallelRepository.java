package com.ymatou.productprice.domain.repo.parallelrepo;

import com.ymatou.productprice.domain.model.ActivityProduct;
import com.ymatou.productprice.domain.model.Catalog;
import com.ymatou.productprice.domain.model.ProductPriceData;
import com.ymatou.productprice.domain.repo.Repository;
import com.ymatou.productprice.infrastructure.util.ParallelUtil.ParallelProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 并行仓储操作相关
 * Created by chenpengxuan on 2017/3/24.
 */
@Component("parallelRepository")
@DependsOn({"disconfMgrBean2"})
public class ParallelRepository implements Repository {
    @Autowired
    private ParallelProcessor parallelProcessor;

    @Resource(name="mongoRepository")
    private Repository mongoRepository;

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
    public ActivityProduct getActivityProduct(String productId) {
        return mongoRepository.getActivityProduct(productId);
    }

    /**
     * 获取活动商品信息列表
     * @param productIdList
     * @return
     */
    @Override
    public List<ActivityProduct> getActivityProductList(List<String> productIdList) {
        return parallelProcessor.doParallelProcess(productIdList, obj ->
                mongoRepository.getActivityProductList((List<String>) obj));
    }

    /**
     * 根据商品id列表获取价格边界信息（用于新增接口->搜索商品列表）
     * @param productIdList
     * @return
     */
    @Override
    public List<ProductPriceData> getPriceRangeListByProduct(List<String> productIdList) {
        return parallelProcessor.doParallelProcess(productIdList,obj ->
                mongoRepository.getPriceRangeListByProduct((List<String>) obj));
    }

    @Override
    public Map<String, Object> getTimeStampByProductId(String productId, List<String> stampKeyList) {
        return mongoRepository.getTimeStampByProductId(productId,stampKeyList);
    }

    @Override
    public List<Map<String, Object>> getTimeStampByProductIdList(List<String> productIdList, List<String> stampKeyList) {
        return mongoRepository.getTimeStampByProductIdList(productIdList,stampKeyList);
    }

    @Override
    public List<String> getNewestActivityProductIdList(Date newestActivityUpdateTime) {
        return mongoRepository.getNewestActivityProductIdList(newestActivityUpdateTime);
    }


    @Override
    public List<Map<String, Object>> getProductIdByCatalogIdList(List<String> catalogIdList) {
        return mongoRepository.getProductIdByCatalogIdList(catalogIdList);
    }

    @Override
    public List<ActivityProduct> getAllValidActivityProductList() {
        return mongoRepository.getAllValidActivityProductList();
    }
}
