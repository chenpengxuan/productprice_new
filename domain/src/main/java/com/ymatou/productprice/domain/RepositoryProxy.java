package com.ymatou.productprice.domain;

import com.ymatou.productprice.domain.cacherepo.CacheRepository;
import com.ymatou.productprice.domain.mongorepo.MongoRepository;
import com.ymatou.productprice.domain.parallelrepo.ParallelRepository;
import com.ymatou.productprice.domain.repo.Repository;
import com.ymatou.productprice.infrastructure.config.props.BizProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 仓储代理
 * Created by chenpengxuan on 2017/3/24.
 */
@Component
public class RepositoryProxy {
    @Autowired
    private BizProps bizProps;

    @Autowired
    private CacheRepository cacheRepository;

    @Autowired
    private ParallelRepository parallelRepository;

    @Autowired
    private MongoRepository mongoRepository;

    private Repository realRepository;

    /**
     * 获取实际仓储处理对象
     * @return
     */
    public Repository getRepository(){
        if(bizProps.isUseCache()){
            realRepository = cacheRepository;
        }
       else if(bizProps.isUseParallel()){
            realRepository = parallelRepository;
        }
        else{
            realRepository = mongoRepository;
        }
        return realRepository;
    }
}
