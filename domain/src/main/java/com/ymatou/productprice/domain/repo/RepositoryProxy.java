package com.ymatou.productprice.domain.repo;

import com.ymatou.productprice.infrastructure.config.props.BizProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 仓储代理
 * Created by chenpengxuan on 2017/3/24.
 */
@Component
@DependsOn({"disconfMgrBean2"})
public class RepositoryProxy {
    @Autowired
    private BizProps bizProps;

    @Resource(name="parallelRepository")
    private Repository parallelRepository;

    @Resource(name="mongoRepository")
    private Repository mongoRepository;

    private Repository realRepository;

    /**
     * 获取实际仓储处理对象
     * @return
     */
    public Repository getRepository(){
        if(bizProps.isUseParallel()){
            realRepository = parallelRepository;
        }
        else{
            realRepository = mongoRepository;
        }
        return realRepository;
    }
}
