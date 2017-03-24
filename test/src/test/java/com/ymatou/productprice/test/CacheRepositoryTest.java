package com.ymatou.productprice.test;

import com.ymatou.productprice.domain.cacherepo.CacheRepository;
import com.ymatou.productprice.model.Catalog;
import com.ymatou.productprice.web.ProductPriceApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * mongo 数据仓储测试
 * Created by chenpengxuan on 2017/3/9.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ProductPriceApplication.class)// 指定我们SpringBoot工程的Application启动类
public class CacheRepositoryTest {
    @Autowired
    private CacheRepository cacheRepository;

    /**
     * 测试根据商品id获取规格信息列表
     * 商品存在的情况
     */
    @Test
    public void testGetCatalogListByProduct_ProductExist(){
        String productId = "beaa252d-b880-430c-a639-28a1b361c69a";
        List<Catalog> catalogList = cacheRepository.getCatalogListByProduct(productId);
        Assert.assertNotNull("规格列表不能为空",catalogList);
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
        List<Catalog> tempList = cacheRepository.getCatalogListByProduct(productIdList);
        Assert.assertNotNull("规格列表不能为空",tempList);
    }

    /**
     * 测试根据商品id获取规格信息列表
     * 商品存在的情况
     */
    @Test
    public void testGetCatalogListByProductList_PureCache(){
        List<String> productIdList = new ArrayList<>();
        productIdList.add("c1ba2ba5-ee5b-4139-8731-99127715ffb0");
        productIdList.add("ce4fed93-0e50-4595-a8c2-5adf9d99725e");
        productIdList.add("37bd5942-3ccf-4c24-ad2b-f026b18e6794");
        productIdList.add("8d74a622-fb36-456d-8927-5336b0226486");
        productIdList.add("88d079ac-45cf-430c-9f8d-0629bb8f17be");
        productIdList.add("8ffec130-316b-48c2-97ec-70f0a54d7cb5");
        List<Catalog> cacheCatalogList = cacheRepository.getCatalogListByProduct(productIdList);
        Assert.assertNotNull("规格列表不能为空",cacheCatalogList);
    }
}
