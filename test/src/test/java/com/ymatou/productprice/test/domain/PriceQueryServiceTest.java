package com.ymatou.productprice.test.domain;

import com.ymatou.productprice.domain.service.PriceQueryService;
import com.ymatou.productprice.model.ProductPrice;
import com.ymatou.productprice.web.ProductPriceApplication;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品价格查询服务测试
 * Created by chenpengxuan on 2017/3/9.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ProductPriceApplication.class)// 指定我们SpringBoot工程的Application启动类
public class PriceQueryServiceTest {
    @Autowired
    private PriceQueryService priceQueryService;

    /**
     * 测试根据商品id获取价格信息
     * 正常场景
     * 买手id存在 商品id存在 没有交易隔离
     */
    @Test
    public void testGetPriceInfoByProductId_Normal(){
        int buyerId = 20345997;
        String productId = "8ffec130-316b-48c2-97ec-70f0a54d7cb5";
        ProductPrice productPrice = priceQueryService.getPriceInfoByProductId(buyerId,productId,false);
        Assert.assertNotNull(productPrice);
        Assert.assertNotNull(productPrice.getCatalogs());
        Assert.assertFalse(productPrice.getCatalogs().stream().anyMatch(x -> x.getPrice() == 0));
    }

    /**
     * 测试根据商品id获取价格信息
     * 正常场景
     * 买手id存在 商品id存在 有交易隔离
     */
    @Test
    public void testGetPriceInfoByProductId_TradeIsolation(){
        int buyerId = 20345997;
        String productId = "8ffec130-316b-48c2-97ec-70f0a54d7cb5";
        ProductPrice productPrice = priceQueryService.getPriceInfoByProductId(buyerId,productId,true);
        Assert.assertNotNull(productPrice);
        Assert.assertNotNull(productPrice.getCatalogs());
        Assert.assertTrue(productPrice.getCatalogs().stream().anyMatch(x -> x.getPrice() > 0));
    }

    /**
     * 测试根据商品id获取价格信息
     * 正常场景
     * 买手id存在 商品id存在 没有交易隔离
     */
    @Test
    @Parameterized.Parameters
    public void testGetPriceInfoByProductIdList_Normal(){
        int buyerId = 20345997;
        List<String> productIdList = new ArrayList<>();
        productIdList.add("c1ba2ba5-ee5b-4139-8731-99127715ffb0");
        productIdList.add("ce4fed93-0e50-4595-a8c2-5adf9d99725e");
        productIdList.add("37bd5942-3ccf-4c24-ad2b-f026b18e6794");
        productIdList.add("8d74a622-fb36-456d-8927-5336b0226486");
        productIdList.add("88d079ac-45cf-430c-9f8d-0629bb8f17be");
        productIdList.add("8ffec130-316b-48c2-97ec-70f0a54d7cb5");
        List<ProductPrice> productPriceList = priceQueryService.getPriceInfoByProductIdList(buyerId,productIdList,false);
        Assert.assertNotNull(productPriceList);
        Assert.assertTrue(productPriceList.stream().allMatch(x -> x.getCatalogs().stream().allMatch(y -> y.getPrice() > 0 && y.getPriceType() >= 0)));
    }

    /**
     * 测试根据商品id获取价格信息
     * 正常场景
     * 买手id存在 商品id存在 有交易隔离
     */
    @Test
    public void testGetPriceInfoByProductIdList_TradeIsolation(){
        int buyerId = 20345997;
        List<String> productIdList = new ArrayList<>();
        productIdList.add("ae18bd41-612c-43c0-84ab-959eae8a8499");
        productIdList.add("ce4fed93-0e50-4595-a8c2-5adf9d99725e");
        productIdList.add("37bd5942-3ccf-4c24-ad2b-f026b18e6794");
        productIdList.add("8d74a622-fb36-456d-8927-5336b0226486");
        productIdList.add("88d079ac-45cf-430c-9f8d-0629bb8f17be");
        productIdList.add("8ffec130-316b-48c2-97ec-70f0a54d7cb5");
        List<ProductPrice> productPriceList = priceQueryService.getPriceInfoByProductIdList(buyerId,productIdList,true);
        Assert.assertNotNull(productPriceList);
        Assert.assertTrue(productPriceList.stream().allMatch(x -> x.getCatalogs().stream().allMatch(y -> y.getPrice() > 0 && y.getPriceType() >= 0)));
    }

    /**
     * 测试根据规格id列表获取价格信息
     * 正常场景
     * 买手id存在 商品id存在 没有交易隔离
     */
    public void testGetPriceInfoByCatalogIdList_Normal(){
        int buyerId = 20345997;
    }
}
