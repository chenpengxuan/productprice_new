package com.ymatou.productprice.test;

import com.google.common.collect.Lists;
import com.ymatou.productprice.domain.cache.Cache;
import com.ymatou.productprice.domain.model.Catalog;
import com.ymatou.productprice.domain.repo.Repository;
import com.ymatou.productprice.infrastructure.util.Tuple;
import com.ymatou.productprice.web.ProductPriceApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.*;

/**
 * mongo 数据仓储测试
 * Created by chenpengxuan on 2017/3/9.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ProductPriceApplication.class)// 指定我们SpringBoot工程的Application启动类
public class CacheRepositoryTest {
    @Autowired
    private Cache cacheRepository;

    @Resource(name = "mongoRepository")
    private Repository mongoRepository;

    /**
     * 测试根据商品id获取规格信息列表
     * 商品不存在的情况
     */
    @Test
    public void testGetCatalogListByProduct_ProductNotExist(){
        List<String> productIdList = new ArrayList<>();
        productIdList.add("c1ba2ba5-ee5b-4139-8731-99127715ffb1");
        productIdList.add("ce4fed93-0e50-4595-a8c2-5adf9d997252");
        productIdList.add("37bd5942-3ccf-4c24-ad2b-f026b18e6793");
        productIdList.add("8d74a622-fb36-456d-8927-5336b0226484");
        productIdList.add("88d079ac-45cf-430c-9f8d-0629bb8f17b5");
        productIdList.add("8ffec130-316b-48c2-97ec-70f0a54d7cb6");
        Tuple tempTuple = getCatalogAndActivityTimeStamp(productIdList);
        List<Catalog> cacheCatalogList = cacheRepository.getCatalogListByProduct(productIdList,(Map)tempTuple.first);
        Assert.assertTrue("规格列表必须为空",cacheCatalogList == null || cacheCatalogList.isEmpty());
    }

    /**
     * 测试根据商品id获取规格信息列表
     * 商品存在的情况
     */
    @Test
    public void testGetCatalogListByProductList_ProductListExist(){
        List<String> productIdList = new ArrayList<>();
        productIdList.add("c1ba2ba5-ee5b-4139-8731-99127715ffb0");
        productIdList.add("ce4fed93-0e50-4595-a8c2-5adf9d99725e");
        productIdList.add("37bd5942-3ccf-4c24-ad2b-f026b18e6794");
        productIdList.add("8d74a622-fb36-456d-8927-5336b0226486");
        productIdList.add("88d079ac-45cf-430c-9f8d-0629bb8f17be");
        productIdList.add("8ffec130-316b-48c2-97ec-70f0a54d7cb5");
        Tuple tempTuple = getCatalogAndActivityTimeStamp(productIdList);
        List<Catalog> cacheCatalogList = cacheRepository.getCatalogListByProduct(productIdList,(Map)tempTuple.first);
        Assert.assertNotNull("规格列表不能为空",cacheCatalogList);
    }


    private Tuple<Map,Map> getCatalogAndActivityTimeStamp(List<String> productIdList){
        //获取活动商品与规格的变更时间戳
        List<Map<String, Object>> updateStampMapList = mongoRepository
                .getTimeStampByProductIdList(productIdList, Lists.newArrayList("cut", "aut"));

        Map<String, Date> activityProductUpdateTimeMap = new HashMap<>();
        Map<String, Date> catalogUpdateTimeMap = new HashMap<>();

        //填充活动商品与规格时间戳
        updateStampMapList.forEach(x -> {
            activityProductUpdateTimeMap.put(x.get("spid").toString(),
                    Optional.ofNullable((Date) x.get("aut")).orElse(null));
            catalogUpdateTimeMap.put(x.get("spid").toString(),
                    Optional.ofNullable((Date) x.get("cut")).orElse(null));
        });
        return new Tuple<>(catalogUpdateTimeMap,activityProductUpdateTimeMap);
    }
}
