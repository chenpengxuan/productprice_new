package com.ymatou.productprice.domain.parallelrepo;

import com.ymatou.productprice.domain.cacherepo.CacheRepository;
import com.ymatou.productprice.domain.mongorepo.MongoRepository;
import com.ymatou.productprice.domain.repo.Repository;
import com.ymatou.productprice.infrastructure.config.props.BizProps;
import com.ymatou.productprice.infrastructure.util.ParallelUtil.ParallelProcessor;
import com.ymatou.productprice.model.Catalog;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * 并行仓储操作相关
 * Created by chenpengxuan on 2017/3/24.
 */
public class ParallelRepository implements Repository{
    @Autowired
    private ParallelProcessor parallelProcessor;

    @Autowired
    private BizProps bizProps;

    @Autowired
    private MongoRepository mongoRepository;

    @Autowired
    private CacheRepository cacheRepository;

    private Repository realBusinessRepository;

    @PostConstruct
    public void init(){
        if(bizProps.isUseCache()){
            realBusinessRepository = cacheRepository;
        }
        else{
            realBusinessRepository = mongoRepository;
        }
    }

    @Override
    public List<Catalog> getCatalogListByProduct(String productId) {
        return realBusinessRepository.getCatalogListByProduct(productId);
    }

    @Override
    public List<Catalog> getCatalogListByProduct(List<String> productIdList) {
        return parallelProcessor.doParallelProcess(productIdList,obj ->
                realBusinessRepository.getCatalogListByProduct((List<String>)obj));
    }

    @Override
    public List<Catalog> getCatalogByCatalogId(List<String> catalogIdList) {
        return parallelProcessor.doParallelProcess(catalogIdList,obj ->
                realBusinessRepository.getCatalogByCatalogId((List<String>)obj));
    }

    @Override
    public Map<String, Object> getActivityProduct(String productId) {
        return realBusinessRepository.getActivityProduct(productId);
    }

    @Override
    public List<Map<String, Object>> getActivityProductList(List<String> productIdList) {
        return parallelProcessor.doParallelProcess(productIdList,obj ->
                realBusinessRepository.getActivityProductList((List<String>)obj));
    }
}
