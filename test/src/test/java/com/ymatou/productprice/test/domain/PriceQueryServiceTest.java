package com.ymatou.productprice.test.domain;

import com.ymatou.productprice.domain.service.PriceQueryService;
import com.ymatou.productprice.model.CatalogPrice;
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
    public void testGetPriceInfoByProductId_Normal() {
        int buyerId = 20345997;
        String productId = "8ffec130-316b-48c2-97ec-70f0a54d7cb5";
        ProductPrice productPrice = priceQueryService.getPriceInfoByProductId(buyerId, productId, false);
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
    public void testGetPriceInfoByProductId_TradeIsolation() {
        int buyerId = 20345997;
        String productId = "8ffec130-316b-48c2-97ec-70f0a54d7cb5";
        ProductPrice productPrice = priceQueryService.getPriceInfoByProductId(buyerId, productId, true);
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
    public void testGetPriceInfoByProductIdList_Normal() {
        int buyerId = 20345997;
        List<String> productIdList = new ArrayList<>();
        productIdList.add("c1ba2ba5-ee5b-4139-8731-99127715ffb0");
        productIdList.add("ce4fed93-0e50-4595-a8c2-5adf9d99725e");
        productIdList.add("37bd5942-3ccf-4c24-ad2b-f026b18e6794");
        productIdList.add("8d74a622-fb36-456d-8927-5336b0226486");
        productIdList.add("88d079ac-45cf-430c-9f8d-0629bb8f17be");
        productIdList.add("8ffec130-316b-48c2-97ec-70f0a54d7cb5");
        List<ProductPrice> productPriceList = priceQueryService.getPriceInfoByProductIdList(buyerId, productIdList, false);
        Assert.assertNotNull(productPriceList);
        Assert.assertTrue(productPriceList.stream().allMatch(x -> x.getCatalogs().stream().allMatch(y -> y.getPrice() > 0 && y.getPriceType() >= 0)));
    }

    /**
     * 测试根据商品id获取价格信息
     * 正常场景
     * 买手id存在 商品id存在 有交易隔离
     */
    @Test
    public void testGetPriceInfoByProductIdList_TradeIsolation() {
        int buyerId = 20345997;
        List<String> productIdList = new ArrayList<>();
        productIdList.add("ae18bd41-612c-43c0-84ab-959eae8a8499");
        productIdList.add("ce4fed93-0e50-4595-a8c2-5adf9d99725e");
        productIdList.add("37bd5942-3ccf-4c24-ad2b-f026b18e6794");
        productIdList.add("8d74a622-fb36-456d-8927-5336b0226486");
        productIdList.add("88d079ac-45cf-430c-9f8d-0629bb8f17be");
        productIdList.add("8ffec130-316b-48c2-97ec-70f0a54d7cb5");
        List<ProductPrice> productPriceList = priceQueryService.getPriceInfoByProductIdList(buyerId, productIdList, true);
        Assert.assertNotNull(productPriceList);
        Assert.assertTrue(productPriceList.stream().allMatch(x -> x.getCatalogs().stream().allMatch(y -> y.getPrice() > 0 && y.getPriceType() >= 0)));
        Assert.assertEquals(productIdList.size(),productPriceList.size());
    }

    /**
     * 测试根据规格id列表获取价格信息
     * 正常场景
     * 买手id存在 商品id存在 没有交易隔离
     * 较多catalogId 顺便简单看下性能
     */
    @Test
    public void testGetPriceInfoByCatalogIdList_Normal() {
        int buyerId = 20345997;
        List<String> catalogIdList = new ArrayList<>();
        catalogIdList.add("80025bc8-5353-4560-bc54-4eff1f4fc686");
        catalogIdList.add("e5f78f4a-4f45-4bf1-8662-65eb8f8ccbd9");
        catalogIdList.add("58aa4923-c05f-45f4-b9ee-dfb409eb709c");
        catalogIdList.add("673db9d9-1e5e-447e-b7df-935bf7d4c47a");
        catalogIdList.add("a29922ce-6640-4a02-b8dd-3f9cb7536fd3");
        catalogIdList.add("a5c33961-b8a3-429b-8fa8-d021688450c6");
        catalogIdList.add("60684f17-d1b4-4a5b-9fdf-8c7ea6f35701");
        catalogIdList.add("60c52ae1-b0ad-4ae7-9d7f-8962d6f8e5dd");
        catalogIdList.add("8e6ba154-c105-42cd-919a-1f893735908f");
        catalogIdList.add("d08bce43-67cb-4eb2-8fd2-30d172812e9b");
        catalogIdList.add("0face3af-8640-43be-b9fc-535c5caf36dd");
        catalogIdList.add("b76fb649-a188-428e-93ec-9c05aa745255");
        catalogIdList.add("63220317-e373-4425-9534-f35b706133c1");
        catalogIdList.add("7b70de18-9121-43c0-9c39-39e5e6d38a5f");
        catalogIdList.add("9daf719d-1f85-4035-84aa-2dd224603d8c");
        catalogIdList.add("ab09a23c-ca0a-4a22-be7d-35562a10a69f");
        catalogIdList.add("98938613-600f-46fa-9833-c35c01a94d9e");
        catalogIdList.add("1fcd9047-bbbf-41c6-92b3-d9e1633aa638");
        catalogIdList.add("9cba5d17-7d21-4ed3-ae7e-f3ebe2c82761");
        catalogIdList.add("53eed5c9-d194-4bec-ad27-f70ab8472125");
        catalogIdList.add("d7c592bc-0637-4b66-92dc-4a24a61fc343");
        catalogIdList.add("d1185d0c-9a8b-459c-8ed6-7afe1dfda86c");
        catalogIdList.add("a9f3cb08-755b-4a35-b644-7a3ab6d79252");
        catalogIdList.add("c71195a4-37ec-44cf-9867-2509b635eb3f");
        catalogIdList.add("d1040c42-bc1e-42f0-bf2c-4ff95ae5b559");
        catalogIdList.add("e6af82ce-38e3-4781-a0c5-584d841a21d3");
        catalogIdList.add("6574f391-7b9f-433f-ba77-7825ef41ae9b");
        catalogIdList.add("ac71b44d-e789-4f47-9ce1-dff3cfefb677");
        catalogIdList.add("5931ad61-60c7-4443-bcf1-6547180aa588");
        catalogIdList.add("a7c72c99-1c55-46b3-b4aa-718c5a75cd27");
        catalogIdList.add("a2c47c64-edc1-4b83-add5-26248b67be40");
        catalogIdList.add("4a442512-5eb0-4e6c-a5e6-63146aaa1026");
        catalogIdList.add("0424ab9a-0e21-42ff-a6ce-9bb7a835a8d2");
        catalogIdList.add("e7558c99-b84b-4773-9aee-4934a6e2b6ac");
        catalogIdList.add("472d367f-043c-4bca-b315-a9cc15f97327");
        catalogIdList.add("1956b4a0-25bd-4bdf-b5c9-d396ccd92a13");
        catalogIdList.add("5d823b4a-8a80-47d6-8bf0-d2a138e1a5bc");
        catalogIdList.add("81784d7e-9f49-4eee-bee1-161dc3923523");
        catalogIdList.add("b709a701-43ce-4d55-b024-55fba617cf94");
        catalogIdList.add("dcd50519-cfdb-4f28-97e3-22d0b4a684ba");
        catalogIdList.add("ad0e85fe-8d67-4864-8cf4-47d7ca9bcdde");
        catalogIdList.add("207a8726-1f85-4352-8a74-4fb263501fab");
        catalogIdList.add("fd9d53ac-e29c-476f-a595-30fcddaf84ef");
        catalogIdList.add("182175b5-e5c0-4b74-a221-e9d1af60406c");
        catalogIdList.add("f85aaabd-9a08-488d-9e5b-56c1095c8a44");
        catalogIdList.add("06d944ed-92bb-4fd4-83ba-2a3694e97c2e");
        catalogIdList.add("d34a1aca-6f9f-4e1b-be7d-5eebd2796df5");
        catalogIdList.add("0451d6e1-b25b-4580-8d3e-da69cd42b8a1");
        catalogIdList.add("71094b50-0232-4bac-9531-0ab03b9bf314");
        catalogIdList.add("47ed52f2-fc80-44ed-8f9c-e98b07ac1f8c");
        catalogIdList.add("5b68cad8-4235-4901-8ddf-8f2209b0b05d");
        catalogIdList.add("9e0d36a5-a05d-44fe-9205-965a3f1608c1");
        catalogIdList.add("037d9cc0-405d-4f06-8660-b19227d6c86a");
        catalogIdList.add("0866f1c1-632f-4b44-927f-bf7ef05f0255");
        catalogIdList.add("4eaf5063-f106-4f7d-abc1-584921842301");
        catalogIdList.add("31b81f98-ce4d-49a1-887a-401de8a34968");
        catalogIdList.add("382cbb46-f7a6-41cc-9ed5-b96786a25ccf");
        catalogIdList.add("85f06233-9e91-4c68-891a-a4b2089f902a");
        catalogIdList.add("c279d456-8596-4010-b481-b0d45f9db291");
        catalogIdList.add("12a62e0e-2cb7-4a3d-99ea-f1042cbad158");
        catalogIdList.add("67b0acfe-4b13-4d4f-8ed1-2ae661137756");
        catalogIdList.add("f7daa1e0-abea-404e-8dd2-dab74a7becbe");
        catalogIdList.add("55acd299-64e9-43c8-91b0-98a06c192638");
        catalogIdList.add("778db5c3-624f-429e-999b-b9cda98dc433");
        catalogIdList.add("87518a62-5650-436e-b8ce-1be8bd59f060");
        catalogIdList.add("c5500ec2-193d-49a3-a186-0787c2f5a0a3");
        catalogIdList.add("51504a78-0f1e-4529-bcf5-9fe4460a7c88");
        catalogIdList.add("a7865c11-773b-404a-aadb-0648cf5f477e");
        catalogIdList.add("c435ce29-1c0f-497a-8133-0bd28e26874f");
        catalogIdList.add("fa6ef900-6af1-4c7f-b935-9e68438bfb52");
        catalogIdList.add("22e4e535-ce5e-42c3-85e0-8ede1bcdee4c");
        catalogIdList.add("2fa4220e-57bd-4083-a2ee-81ccea633730");
        catalogIdList.add("7b6ad969-9245-4928-a935-ab6ae4b0acf1");
        catalogIdList.add("948bc149-e5a3-45be-bfa8-5871bd27e0af");
        catalogIdList.add("37cdd567-5b39-459b-9639-7d7f1fbf1ff0");
        catalogIdList.add("1ebd9666-da16-4969-840a-4acfdc788c4a");
        catalogIdList.add("a3cd92f2-4025-4214-8aa6-1f6134de722c");
        catalogIdList.add("19e4209a-89a1-4f2e-bad3-119766726519");
        catalogIdList.add("5836835d-3102-47bf-a6e7-0d8aa4e836f2");
        catalogIdList.add("c3d65af1-a1bd-42ab-a9e9-d05796a11697");
        catalogIdList.add("97df8551-4084-4271-b914-d1cd216f8bba");
        catalogIdList.add("c7a8fb42-c7aa-4321-9ca2-1e3b03742024");
        catalogIdList.add("b3b48848-0d8e-4a70-8327-233a511d7c0f");
        catalogIdList.add("cf876037-c414-42d1-9a81-787c57f5d682");
        catalogIdList.add("ac1b3797-8d98-4e45-9589-a37a2863c82d");
        catalogIdList.add("97741f1b-f944-44fc-90fc-67f6db6a4190");
        catalogIdList.add("d45b8cfe-f416-484c-be89-e72a5316fba4");
        catalogIdList.add("d39ee472-edff-4954-a2b8-ba4d9dfbabb6");
        catalogIdList.add("236dd5c2-5bfd-474e-a6d2-64713592872b");
        catalogIdList.add("51d06ab7-da38-4904-b7e5-22a1fb8f8e8b");
        catalogIdList.add("72e1757a-96ea-4f87-b8db-f392a24a5edf");
        catalogIdList.add("b767e5c0-acda-4cc6-b951-51f6156bcc8a");
        catalogIdList.add("7aa0d0a0-5741-4429-a08f-3c20cd8be37a");
        catalogIdList.add("9abc8f1b-04c5-49ba-9810-d7ea835cdaa7");
        catalogIdList.add("7f0bb4b1-967b-40ad-8a7b-f9e8ee979706");
        catalogIdList.add("1f61f70a-152e-43f3-a12d-cfc2cbcfda20");
        catalogIdList.add("0d3787ae-603e-4fec-be16-60498f10778e");
        catalogIdList.add("e2751f16-9c1e-41b1-ba60-0d60411c600b");
        catalogIdList.add("b1efcce3-858b-4af1-9f13-938df2f8906e");
        catalogIdList.add("8d27de30-8b6a-4239-837e-561bcbbe91b5");
        catalogIdList.add("8603e074-35d4-45a0-9468-62316a8034a6");
        catalogIdList.add("26834519-e3f9-4f0a-8af9-157ec8ea8afe");
        catalogIdList.add("de87a1d6-3c5b-4658-be5a-adc6656a1cf0");
        catalogIdList.add("218f3347-f2e0-4112-88b1-781d368384fd");
        catalogIdList.add("d9380f71-78e2-4059-8f31-4dc5a33968b5");
        catalogIdList.add("ecf060f6-264d-4ae3-81b5-04bccb53a55c");
        catalogIdList.add("6f1b7516-b813-42af-86ee-677c33576f15");
        catalogIdList.add("9921c06f-aea8-4e44-b76d-5fe721f3b8c9");
        catalogIdList.add("8aa4b1fe-54c5-4508-b22b-7066d2f7c54e");
        catalogIdList.add("29004a5c-ea8a-4887-b746-ba712456c416");
        catalogIdList.add("5caf65fc-5508-459b-a59b-e0a0795e196f");
        catalogIdList.add("60b58242-e2bf-44ec-838a-2830451671b8");
        catalogIdList.add("7ae3bb97-bf3b-4249-9ef5-f111c5d3dc33");
        catalogIdList.add("85390fa5-aefb-4305-bc28-1093f3d5f588");
        catalogIdList.add("8693cc2a-2903-4f5c-b04f-85b4467ee01c");
        catalogIdList.add("c992ccc5-042b-40b8-b264-ac71d65ceece");
        catalogIdList.add("5bbfbb4d-8eca-4a48-b530-2f9448356807");
        catalogIdList.add("c842ce7a-8c6d-42fc-8f3e-69f969ceb465");
        catalogIdList.add("bdbd0cc8-0db0-4da5-bea9-1d52a3150898");
        catalogIdList.add("d604b163-5840-4b73-bc9e-8e24555a4529");
        catalogIdList.add("63ff5a7b-7283-4815-8291-f81e0dcc66ce");
        catalogIdList.add("4faeced5-83bb-4c09-b786-09ac83a0d423");
        catalogIdList.add("9c585b0d-473a-4857-9d48-3ba58ace116f");
        catalogIdList.add("ccd81d3f-884b-4a26-b471-204189ddba53");
        catalogIdList.add("f9cd4e76-e353-4117-a150-849adf6b0b3d");
        catalogIdList.add("7b75a4a0-4846-4f0d-9147-2befcd2db163");
        catalogIdList.add("cf3de53b-c325-43c2-b625-d5272cbe9a21");
        catalogIdList.add("1d86fe67-7f7c-4824-90c1-2fe118a19f0a");
        catalogIdList.add("452b29c3-accc-4f3f-859d-b5c483d0c7af");
        catalogIdList.add("9af584a0-31bc-4a84-9fcf-ff78aacb66b3");
        catalogIdList.add("7ae0305b-a941-43cc-9754-3d9157499953");
        catalogIdList.add("be7707f9-4b57-4539-b9b1-512e6e8682ee");
        catalogIdList.add("498c0e10-7dd1-4d51-a2da-b0948851ef6f");
        catalogIdList.add("7d4a0984-194c-4d0d-a3a8-5b1b4ebe407b");
        catalogIdList.add("64921c69-5f80-4f1e-b203-0ea3dd330154");
        catalogIdList.add("71cd68f2-aece-467f-a13d-a00374319711");
        catalogIdList.add("a7165569-8225-46c7-9db9-865ec7c5e172");
        catalogIdList.add("b2deef57-c5f7-4ca1-b9fc-c9ec43d04c41");
        catalogIdList.add("bef3eef1-6033-4f1e-a53c-850dcbfab7fc");
        catalogIdList.add("8110a262-003c-4268-8575-8dc8bc9fe52f");
        catalogIdList.add("92610e00-33e6-4f99-b819-b80f1b09d04f");
        catalogIdList.add("8907dae2-70e7-4db4-b685-b9aec1fdd231");
        catalogIdList.add("9687981e-ba6f-4512-aefc-919b145ce0de");
        catalogIdList.add("99cb0a64-cad9-4eab-9c15-35454b5ec007");
        catalogIdList.add("6501514f-7fc8-4fac-b7fc-e47c3d1bd358");
        catalogIdList.add("741a0542-ed4c-4f34-8e04-d26b8e5a5650");
        catalogIdList.add("9b07f4aa-91de-47d6-acca-e35f2c5ccffb");
        catalogIdList.add("c2e833df-ce7f-4ce6-8236-20141d60035c");
        catalogIdList.add("f10e92d4-d20c-4d8b-9c32-1d79a9bc86f8");
        catalogIdList.add("a1ee3be0-265f-4ea7-a992-aab64256b47d");
        catalogIdList.add("6655742d-8b32-4b87-9086-9498793f00e9");
        catalogIdList.add("b246c6a7-905f-4cbc-b52b-f51892d3ec55");
        catalogIdList.add("cda95e2b-f586-483c-8da9-416863ae8181");
        catalogIdList.add("6a68d251-660e-4c91-b1df-7f447d3a82bc");
        catalogIdList.add("77cca0ca-1271-4129-8e30-391ab132615b");
        catalogIdList.add("27e4e829-41df-4bac-8666-6cf8a164bbf6");
        catalogIdList.add("fbd41c83-ce06-481f-bf10-3344414967a8");
        catalogIdList.add("4f63b780-122a-4aaf-9095-d569718fd56c");
        catalogIdList.add("89c62e41-9eb8-4f10-a1bf-3835f01495b1");
        catalogIdList.add("91e25241-ed78-4e71-b34a-c11aa838b83c");
        catalogIdList.add("c6f759ed-1523-4e30-bd4e-21b6cecbace7");
        catalogIdList.add("e01d8338-6007-44ef-b82b-e85901aaf63f");
        catalogIdList.add("628f4f6f-482e-47db-89ee-c73b96a8624c");
        catalogIdList.add("2d463297-15af-4d6e-8c55-a6656b760f55");
        catalogIdList.add("89a592b1-86e6-4d7b-88bc-2e6f9503c4a5");
        catalogIdList.add("a73a7664-8e1d-4cdc-9205-189ebecc5439");
        catalogIdList.add("ab031523-1fc3-41ba-8ee7-945f676214b0");
        catalogIdList.add("d17f4e97-e8b1-41cd-857c-9c479db6d61d");
        catalogIdList.add("77efa0a5-7995-45b3-b135-b83a31803d50");
        catalogIdList.add("2f316381-b0d9-4c46-bc07-ef7c690f2e65");
        catalogIdList.add("41590023-ebfd-4c44-a065-82674427007a");
        catalogIdList.add("a2abc5de-9cc6-4ad6-80f2-a84d19776458");
        catalogIdList.add("b7aabc46-e43a-45fd-9fc8-108a7b80a50e");
        catalogIdList.add("d9331b28-ca58-4768-8dfa-74c7ef45f9b3");
        catalogIdList.add("fd7aad70-3ad1-4a97-989f-4a0f3426db87");
        catalogIdList.add("c254ef6b-cf86-4853-be3c-c3b08aa98f88");
        catalogIdList.add("25d44f74-0f57-4051-9d90-936c789f5e21");
        catalogIdList.add("bc3f2379-c4fe-478c-9083-9c34294dfab3");
        catalogIdList.add("d92faeb0-c03e-4005-8b72-5639c1124746");
        catalogIdList.add("b98e0998-d298-46d6-836a-b7cf5cf0526c");
        catalogIdList.add("0fc0efaa-e373-4326-9968-2a675bcf3d71");
        catalogIdList.add("6dbe1a69-4641-41ae-b2ed-607646907384");
        catalogIdList.add("bf069ffb-cb28-4f58-b677-a94c97dc2982");
        catalogIdList.add("562675f5-0562-4717-a70a-1b71c8858a9c");
        catalogIdList.add("9c768b97-6d56-4161-8621-81cb978bd553");
        catalogIdList.add("17986adf-9540-4e90-b9f0-e1a08714c5c0");
        catalogIdList.add("25d833e5-db01-43ff-ad38-bf7e35e71e57");
        catalogIdList.add("7d7e5cd4-81db-476d-ac64-a935311e1a5d");
        catalogIdList.add("8013ea97-e1dd-43d3-9d47-d9fa888084d7");
        catalogIdList.add("f60cbc47-048d-42e1-9ca9-db6b36187807");
        catalogIdList.add("9da190b0-994d-451b-b2b2-fa0cc913beae");
        catalogIdList.add("33fe3aaf-cbc4-48f0-a22d-b5aa157ab808");
        catalogIdList.add("6374a2a2-954c-4221-84e1-4f9d061f5acd");
        catalogIdList.add("0a14183c-66f4-43d7-b530-50d9f1180e64");
        catalogIdList.add("1fbbc8d3-5745-4bba-8006-27469ab8c607");
        catalogIdList.add("2cdf642e-dbcf-4f3a-8ce3-c46138ac107d");
        catalogIdList.add("9223c9ad-b02f-4e82-988b-3483f5e71fc0");
        catalogIdList.add("5d663219-6bd1-452a-9cb8-61e30bb4196e");
        catalogIdList.add("8997ea1f-876f-455b-aa0b-29a7a9d57991");
        catalogIdList.add("20486a5f-5fc7-454f-b166-7c72bd9cf72f");
        catalogIdList.add("dbcfd81a-c8b5-4d33-b4fa-9d170044a9b1");
        catalogIdList.add("fe2bc6dd-346c-4a71-ad52-c479153f7d90");
        catalogIdList.add("46bbc891-b5d5-4f1a-aaf8-9552f3f56b46");
        catalogIdList.add("e1a1c21b-6522-44a9-a547-fe8e5233b1ae");
        catalogIdList.add("2e0afc0c-2a87-4ebe-9900-959e49d24fd2");
        catalogIdList.add("d8f819a5-adb9-4f04-b615-7306b8914c96");
        catalogIdList.add("5c1fefde-4ee8-4087-8238-1b6f622a1ad8");
        catalogIdList.add("395b0a6c-5e09-457f-b9b8-6cf4b270d873");
        catalogIdList.add("7ba9d4a7-e8a5-489a-a78e-4beb65bc7aa7");
        catalogIdList.add("3b993730-e416-4153-b747-1ef5c09d0175");
        catalogIdList.add("03b74e93-ec11-4fad-ae6b-68b6dd474313");
        catalogIdList.add("9b2d255f-118f-4a97-8d1b-26b02ac88951");
        catalogIdList.add("3f9fe4ef-5674-4367-ba63-d4ba90b4c68e");
        catalogIdList.add("85707adb-7318-4bf6-9f75-e3e3d5724e72");
        catalogIdList.add("e4835e8b-02a5-4aa5-be60-590d513d5042");
        catalogIdList.add("f2ee0bbf-f456-4c14-b0c1-9eb1e4803048");
        catalogIdList.add("9e0d3173-31d1-4885-9d79-f55f07ad2183");
        catalogIdList.add("04f32456-e62a-4a52-840d-3669206e5e2f");
        catalogIdList.add("cc22fd74-621c-42b8-9bf1-3cb6c47d7696");
        catalogIdList.add("1957b169-7fe0-4d9d-bb3d-fe9afa87b226");
        catalogIdList.add("1cc55fd4-7041-4aae-957b-fc215b1d7865");
        catalogIdList.add("4902060e-fa72-40fe-a86d-01920f4ce7bd");
        catalogIdList.add("c1681864-f7a2-4473-a01e-5e05e9f9de38");
        catalogIdList.add("9d29733b-0bd8-4cdf-a639-6c375dc7b75c");
        catalogIdList.add("b0b7f0de-ee1b-486a-8a21-767bc808bf53");
        catalogIdList.add("c28bfc86-18cf-4a91-b5c8-06d7d4afb013");
        catalogIdList.add("a6e1e145-17e1-4c5d-adad-747c7f0f3a1f");
        catalogIdList.add("2c9e472d-b9c5-4b1c-a7b5-33826ec1a251");
        catalogIdList.add("823b503c-e30d-4807-aeea-c043d03565e1");
        catalogIdList.add("b7f7a856-849d-48b2-874f-ba281b68dac1");
        catalogIdList.add("f3d25341-dbe2-4ef2-84fc-9252a40e8789");
        catalogIdList.add("2fd524cc-0ddd-4af1-b921-f7397800b74b");
        catalogIdList.add("12b62bf5-b70c-4f62-856b-972ea873c9cb");
        catalogIdList.add("3cdb3b54-b5ee-47e6-9c51-f74354262e43");
        catalogIdList.add("8c46230b-ad50-49ad-90f9-c2a48f2a3e88");
        catalogIdList.add("efdf15ef-8f72-4e2d-94dc-429826390752");
        catalogIdList.add("99dd9813-8013-416d-9ff8-ae5c07ce0ef1");
        catalogIdList.add("16b48a26-4eef-46fe-9e75-fd83559b307d");
        catalogIdList.add("418c96ef-0953-4f4b-af71-2d67dc580f5d");
        catalogIdList.add("61abcfab-346f-499e-8b3e-e1110a74e8e7");
        catalogIdList.add("d99d3d70-3b38-4a9a-b630-0c3de83cd922");
        catalogIdList.add("c941b7b3-11fa-4330-870a-d45f89c6e3ca");
        catalogIdList.add("ec3d3caf-4921-4985-b3fc-f507f3e624ad");
        catalogIdList.add("5f1831cc-4687-4968-b5cd-a9c904b4b90e");
        catalogIdList.add("95b42b89-af2a-46d4-b2bc-26c606778a3a");
        catalogIdList.add("aa7b1d16-fd34-4a9f-b80c-6e0c05b6d096");
        catalogIdList.add("091ddef7-daaf-49bd-a0f4-4da06d838ae0");
        catalogIdList.add("487485ae-d71a-4b45-b84e-3ebf7dccc8ec");
        catalogIdList.add("2f3930ae-3478-462d-ad35-36f4dd1c881c");
        catalogIdList.add("35bd0842-1a88-46f3-9ce0-c97c2d759d61");
        catalogIdList.add("9262faf1-10ec-4770-9eb3-d48dff0163f4");
        catalogIdList.add("0879b449-9a66-40eb-9be4-398263d2ac62");
        catalogIdList.add("09a88187-532a-44d9-8a83-c3f9057d0216");
        catalogIdList.add("293bcbce-729a-4233-b944-9596c9dd8824");
        catalogIdList.add("49f067d5-d1f7-49b2-ab6a-d6ea285df834");
        catalogIdList.add("f8234b20-5669-4c92-b2db-b5594d8711da");
        catalogIdList.add("dd0fc20d-8f1a-45f2-8bf8-eebfcde84ed2");
        catalogIdList.add("30552acd-49f9-4707-8949-1949177fde8d");
        catalogIdList.add("468ce8ed-3c82-4d10-bb9c-e273dd792e1f");
        catalogIdList.add("332ed06a-cb0e-4d16-a26f-e638167e599e");
        catalogIdList.add("1c149b97-ef1d-4802-abdf-126d10500fcb");
        catalogIdList.add("011966dd-157e-449e-affd-a4efdb361bc9");
        catalogIdList.add("867fb062-2eec-483e-9b28-3449c16f5205");
        catalogIdList.add("a1531ab3-7be9-4183-97e2-998e07d93e43");
        catalogIdList.add("f6f6de3d-5060-4892-8947-edeac12fcad3");
        catalogIdList.add("0d74d8ea-37da-4b70-abe0-d434dc715841");
        catalogIdList.add("a0ae7602-e64d-47ec-b7c4-c49ccee46e98");
        catalogIdList.add("7f55821d-aa56-482a-b8dd-f7f8c8a3e8cc");
        catalogIdList.add("0acbec3f-ca1d-4c38-80be-567585d9e34c");
        catalogIdList.add("c89d9b49-93c7-4f5b-b17c-7dfa5d432deb");
        catalogIdList.add("48cd458c-e1ce-4724-b90f-61def8d223f0");
        catalogIdList.add("679f56d1-c540-460a-afb2-cff168c4b119");
        catalogIdList.add("497bfc7b-4ad0-40e8-ab04-17e62c136285");
        catalogIdList.add("fa365589-7087-4369-81cb-98a5870bda77");
        catalogIdList.add("bb325c3e-3fc3-4141-9224-43dff68b662c");
        catalogIdList.add("b3255883-6110-441c-9062-59304dad1e0c");
        catalogIdList.add("e857eae5-3ed3-4ee2-92c0-b9a839c1651c");
        catalogIdList.add("af154b7a-0d2d-432d-a512-d909fe975f5a");
        catalogIdList.add("611147c6-fa72-4bd4-b6a5-b38180773a54");
        catalogIdList.add("38e99bef-34f2-4904-8042-77fa7e4ed870");
        catalogIdList.add("47254854-8de4-4ef3-9df0-4324f05c3f7e");
        catalogIdList.add("90f3ba7f-06c3-406c-8ab8-c618c3c4ce4a");
        catalogIdList.add("a64dd364-4b4b-4892-828c-c6e434e0c73b");
        catalogIdList.add("5c26f2ff-6490-46a9-b0c9-408ca5b91eeb");
        catalogIdList.add("214a5aef-bfae-4579-bc41-c94c8a32ef1c");
        catalogIdList.add("6ee6d7e3-2e2d-4341-91be-855b3c89eaac");
        catalogIdList.add("2befe353-cb76-46bc-9738-7f960bb72dd0");
        catalogIdList.add("cb3c3cd9-1f74-4577-8039-a8e922168424");
        catalogIdList.add("a65fad55-efe5-4ccf-9c9e-ae967619e1c4");
        catalogIdList.add("a3388055-5868-4865-a59e-6cc096e6c14f");
        catalogIdList.add("ccb435ed-5201-4134-846f-ee36f1017778");
        catalogIdList.add("b21c773e-0536-4213-9ddb-ae6025bfbc1f");
        catalogIdList.add("1fc9bc8e-d350-4c6a-9c7e-392e65a6813a");
        catalogIdList.add("348e1bb6-fb49-4fd8-b082-05f7962f1d7c");
        catalogIdList.add("dfc65999-0b4b-43a7-87f2-af09ed0e78bf");
        catalogIdList.add("e9b1f89a-0c97-4db2-be39-ab6ee812e2f5");
        catalogIdList.add("53fcc58e-8173-4607-a1ac-757fe5849600");
        catalogIdList.add("8d46ca15-2aca-44fe-92d6-523a6b5b12b5");
        catalogIdList.add("88f95596-60f2-47f5-8463-bd0f0b5418e5");
        catalogIdList.add("2bc01e58-45e2-445c-b6e8-3ace0c462e30");
        catalogIdList.add("9446b7a8-c605-4a81-86b1-d905b8482fdf");
        catalogIdList.add("c3fb0d3d-73b2-4001-9412-4b1e43f39845");
        catalogIdList.add("f853e01b-48b0-4d3e-bd9a-1406e514e6e8");
        catalogIdList.add("777315bd-4b13-4b6b-9630-34ac0f38f734");
        catalogIdList.add("fa1c4fad-d3c6-440a-8cc5-afbe89d29158");
        catalogIdList.add("597507ff-9156-4c4d-ab5a-778b30eabc98");
        catalogIdList.add("c4d505f4-2e78-4a61-b553-87d93f797cfd");
        catalogIdList.add("4d258d55-f0fc-4f81-84ea-12ee3c19fcb8");
        catalogIdList.add("8d6e0c9d-3537-4e6f-a61f-5f807a64df51");
        catalogIdList.add("83cf53ba-0fe5-4d9c-9374-abec1b15cddc");
        catalogIdList.add("564c3da2-3853-46a3-a7a6-1794bde7c26d");
        catalogIdList.add("8ae1a4c0-a17c-4440-9c85-88bf814beee2");
        catalogIdList.add("986f93d1-7230-4087-b3a1-2af33ceed74b");
        catalogIdList.add("d6ac8df2-24f1-409b-9898-9e0652749848");
        catalogIdList.add("e2a92d3d-7015-4431-bd1d-05deff353aee");
        catalogIdList.add("f131804a-1467-461a-81b8-c16c8c79c33d");
        catalogIdList.add("1b5ad760-b6c4-4a97-9024-6891725d6951");
        catalogIdList.add("5abbac91-2cba-4588-b78b-4490020ab539");
        catalogIdList.add("d60e70c9-2ec6-4280-b8ee-bc8cfb0f45f5");
        catalogIdList.add("e17b3c12-c146-4715-be38-2451cac7afb8");
        catalogIdList.add("01fd2dd5-51d5-421c-b5c8-3c49606ca707");
        catalogIdList.add("1e4e761f-800f-414e-b94d-c97f954a05b5");
        catalogIdList.add("c09dca43-5dae-4104-928f-78eb1c91e8f8");
        catalogIdList.add("c9fd2e07-1844-4abd-ac89-ed21666f0ece");
        catalogIdList.add("388f4197-58f6-4622-87d2-8cf8c756b110");
        catalogIdList.add("4ba926fd-d347-42d9-a1c9-f3b96953e9b6");
        catalogIdList.add("c66c292e-ddd0-45a7-ba9f-36850e3646c3");
        catalogIdList.add("ece4e77d-4487-494f-b292-128390c1b9c2");
        catalogIdList.add("22f297fb-48aa-4132-816d-10d20139cdad");
        catalogIdList.add("49645cbc-5f94-4d3a-9871-a5a448f083d1");
        catalogIdList.add("78aacffb-ff69-49af-a8c4-91ce66be72e8");
        catalogIdList.add("d532341b-e0ca-4cff-a214-6d8e83ee1e4e");
        catalogIdList.add("878d6920-dc41-4f47-9ee4-9a419cd599b3");
        catalogIdList.add("c4a566b8-5f77-4e84-b3ca-1acc6f7145d4");
        catalogIdList.add("b35d3e0b-70e2-4a30-ae55-bea70bbfa8c8");
        catalogIdList.add("89a7aede-0212-463a-a25d-6b0e2405ec1b");
        catalogIdList.add("f712fe7d-b5e7-4b01-8b6d-9ebaddadcb55");
        catalogIdList.add("9908955f-9630-4b9f-b237-b74f21b554f7");
        catalogIdList.add("d3d9b18a-ca12-4777-80d9-dd17a1bb6249");
        catalogIdList.add("06e45e1e-59f4-4b00-9ebd-7ee5e924b704");
        catalogIdList.add("bc65b233-c194-4e6c-82b5-034e362d078a");
        catalogIdList.add("9b61345d-b898-4b66-a005-bb735040a51d");
        catalogIdList.add("70eb6bdd-1509-4136-ad41-fed9c7196a15");
        catalogIdList.add("2dc30ced-a820-4d08-ac93-7fa2977458a6");
        catalogIdList.add("c3050a69-d43e-4b66-8ec3-65535d17402e");
        catalogIdList.add("27a0ede6-8542-4d49-8f78-e97674fa30c8");
        catalogIdList.add("e88c253e-4a3b-4e2f-bdf4-625bd69dcb5f");
        catalogIdList.add("c5feb564-d8d8-4c72-9f45-43b7c61dbd52");
        catalogIdList.add("35a58672-08b1-468c-b74e-f3d24cb827d0");
        catalogIdList.add("63888f91-0035-4450-b47f-c47a0a0dbccf");
        catalogIdList.add("a9cb69d3-2201-4ffe-8401-b3b4dc59d7b9");
        catalogIdList.add("cac7ff17-a45c-40ec-8170-f5be640e2d42");
        catalogIdList.add("adb6afb1-fd03-411b-be4c-d74234c14861");
        catalogIdList.add("66c0d10d-4376-4a74-b482-200b91b6dcb1");
        catalogIdList.add("98d06501-ed94-4e29-a7b1-1a6189b89233");
        catalogIdList.add("b7424956-d12c-4724-a234-9a59cf15384f");
        catalogIdList.add("219eb9a1-605e-422a-906b-af2c47838ff8");
        catalogIdList.add("4c79b087-6e8d-46b2-ad2e-289efb88cc6a");
        catalogIdList.add("917fdc6c-447b-40ff-8e8d-e2075048882f");
        catalogIdList.add("d1ac7898-68b8-484c-8487-0e2744d32b21");
        catalogIdList.add("3a649bf9-13ff-455c-847f-388f2c93c871");
        catalogIdList.add("21866d5f-068d-43f4-accc-f8aeadeb1dd1");
        catalogIdList.add("82ac9f0c-d8a5-4e8a-97de-79d25c4c09ce");
        catalogIdList.add("3a0de758-2c35-42b0-b709-021a088daf28");
        catalogIdList.add("5225e0d7-848f-480f-98d7-b372f1116021");
        catalogIdList.add("6b86677a-7253-41e2-8fde-1be4f697a5f9");
        catalogIdList.add("b3cd5388-91dd-4e60-bd70-af17cb274734");
        catalogIdList.add("289364f8-2243-4e14-ba23-ca827c815c8c");
        catalogIdList.add("4791060d-693b-475c-9336-4431799ebeac");
        catalogIdList.add("822a35e7-44ec-4b0b-a61b-4808977b15f9");
        catalogIdList.add("8499ad1e-b2ee-484c-806e-ecb7b38f19d9");
        catalogIdList.add("6d9fbc07-7eee-489e-8ab0-bde845a268da");
        List<CatalogPrice> catalogPriceList = priceQueryService.getPriceInfoByCatalogIdList(buyerId,catalogIdList,false);
        Assert.assertNotNull(catalogPriceList);
        Assert.assertEquals(catalogIdList.size(),catalogPriceList.size());
        Assert.assertTrue(catalogPriceList.stream().allMatch(x -> x.getCatalogInfo().getPrice() > 0));
    }
}
