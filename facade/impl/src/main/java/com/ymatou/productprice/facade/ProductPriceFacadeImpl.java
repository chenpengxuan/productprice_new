package com.ymatou.productprice.facade;

import com.alibaba.dubbo.config.annotation.Service;
import com.ymatou.productprice.domain.service.PriceQueryService;
import com.ymatou.productprice.model.CatalogPrice;
import com.ymatou.productprice.model.ProductPrice;
import com.ymatou.productprice.model.req.GetPriceByCatalogIdListRequest;
import com.ymatou.productprice.model.req.GetPriceByProdIdRequest;
import com.ymatou.productprice.model.req.GetPriceByProductIdListRequest;
import com.ymatou.productprice.model.resp.BaseResponseNetAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 价格服务
 * 同时支持http rpc
 * Created by chenpengxuan on 2017/3/1.
 */
@Service(protocol = {"rest", "dubbo"})
@Component
@Path("")
public class ProductPriceFacadeImpl implements ProductPriceFacade {

    @Autowired
    private PriceQueryService priceQueryService;

    /**
     * 根据商品id获取价格信息
     *
     * @param request
     * @return
     */
    @Override
    @GET
    @Path("/{api:(?i:api)}/{Price:(?i:Price)}/{GetPriceByProdId:(?i:GetPriceByProdId)}")
    @Produces({MediaType.APPLICATION_JSON})
    public BaseResponseNetAdapter getPriceByProdId(@BeanParam GetPriceByProdIdRequest request) {
        ProductPrice productPrice = priceQueryService.getPriceInfoByProductId(request.getBuyerId(),
                request.getProductId(),
                false);

        Map<String, Object> priceInfo = new HashMap<>();
        priceInfo.put("PriceInfo", productPrice);

        return BaseResponseNetAdapter.newSuccessInstance(priceInfo);
    }

    /**
     * 根据商品id获取交易隔离价格信息
     *
     * @param request
     * @return
     */
    @Override
    @GET
    @Path("/{api:(?i:api)}/{Price:(?i:Price)}/{GetPriceByProdIdTradeIsolation:(?i:GetPriceByProdIdTradeIsolation)}")
    @Produces({MediaType.APPLICATION_JSON})
    public BaseResponseNetAdapter getPriceByProdIdWithTradeIsolation(@BeanParam GetPriceByProdIdRequest request) {
        ProductPrice productPrice = priceQueryService.getPriceInfoByProductId(request.getBuyerId(),
                request.getProductId(),
                true);

        Map<String, Object> priceInfo = new HashMap<>();
        priceInfo.put("PriceInfo", productPrice);
        return BaseResponseNetAdapter.newSuccessInstance(priceInfo);
    }

    /**
     * 根据商品id列表获取价格信息
     *
     * @param request
     * @return
     */
    @Override
    @POST
    @Path("/{api:(?i:api)}/{Price:(?i:Price)}/{GetPriceByProdIds:(?i:GetPriceByProdIds)}")
    public BaseResponseNetAdapter getPriceByProductIdList(GetPriceByProductIdListRequest request) {
        List<ProductPrice> productPriceList = priceQueryService.getPriceInfoByProductIdList(request.getBuyerId(),
                request.getProductIdList()
                        .stream().distinct().collect(Collectors.toList()),
                false);

        Map<String, Object> priceInfoList = new HashMap<>();
        priceInfoList.put("ProductPriceList", productPriceList);
        return BaseResponseNetAdapter.newSuccessInstance(priceInfoList);
    }

    /**
     * 根据商品id列表获取交易隔离价格信息
     *
     * @param request
     * @return
     */
    @Override
    @POST
    @Path("/{api:(?i:api)}/{Price:(?i:Price)}/{GetPriceByProdIdsTradeIsolation:(?i:GetPriceByProdIdsTradeIsolation)}")
    public BaseResponseNetAdapter getPriceByProductIdListWithTradeIsolation(GetPriceByProductIdListRequest request) {
        List<ProductPrice> productPriceList = priceQueryService.getPriceInfoByProductIdList(request.getBuyerId(),
                request.getProductIdList()
                        .stream().distinct().collect(Collectors.toList()),
                true);

        Map<String, Object> priceInfoList = new HashMap<>();
        priceInfoList.put("ProductPriceList", productPriceList);

        return BaseResponseNetAdapter.newSuccessInstance(priceInfoList);
    }


    /**
     * 根据规格id列表获取价格信息
     *
     * @param request
     * @return
     */
    @Override
    @POST
    @Path("/{api:(?i:api)}/{Price:(?i:Price)}/{GetPriceByCatalogIds:(?i:GetPriceByCatalogIds)}")
    public BaseResponseNetAdapter getPriceByCatalogIdList(GetPriceByCatalogIdListRequest request) {
        List<CatalogPrice> catalogPriceList = priceQueryService.getPriceInfoByCatalogIdList(
                request.getBuyerId(),
                request.getCatalogIdList()
                        .stream().distinct().collect(Collectors.toList()),
                false);

        Map<String, Object> priceInfoList = new HashMap<>();
        priceInfoList.put("CatalogPriceList", catalogPriceList);

        return BaseResponseNetAdapter.newSuccessInstance(priceInfoList);
    }

    /**
     * 根据规格id列表获取交易隔离价格信息
     *
     * @param request
     * @return
     */
    @Override
    @POST
    @Path("/{api:(?i:api)}/{Price:(?i:Price)}/{GetPriceByCatalogIdsTradeIsolation:(?i:GetPriceByCatalogIdsTradeIsolation)}")
    public BaseResponseNetAdapter getPriceByCatalogIdListWithTradeIsolation(GetPriceByCatalogIdListRequest request) {
        List<CatalogPrice> catalogPriceList = priceQueryService.getPriceInfoByCatalogIdList(
                request.getBuyerId(),
                request.getCatalogIdList()
                        .stream().distinct().collect(Collectors.toList()),
                true);

        Map<String, Object> priceInfoList = new HashMap<>();
        priceInfoList.put("CatalogPriceList", catalogPriceList);

        return BaseResponseNetAdapter.newSuccessInstance(priceInfoList);
    }
}
