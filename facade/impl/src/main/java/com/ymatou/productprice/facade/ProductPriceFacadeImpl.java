package com.ymatou.productprice.facade;

import com.alibaba.dubbo.config.annotation.Service;
import com.ymatou.productprice.domain.PricePriceQueryService;
import com.ymatou.productprice.model.ProductPrice;
import com.ymatou.productprice.model.req.GetPriceByProdIdRequest;
import com.ymatou.productprice.model.req.GetPriceByProductIdListRequest;
import com.ymatou.productprice.model.resp.BaseResponseNetAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

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
    private PricePriceQueryService pricePriceQueryService;

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
        if (request == null) {
            return BaseResponseNetAdapter.newBusinessFailureInstance("request不能为空");
        }
        try {
            ProductPrice productPrice = pricePriceQueryService.getPriceInfoByProductId(request.getBuyerId(), request.getProductId(), false);
            Map<String, Object> priceInfo = new HashMap<>();
            priceInfo.put("PriceInfo", productPrice);
            return BaseResponseNetAdapter.newSuccessInstance(priceInfo);
        } catch (Exception ex) {
            return BaseResponseNetAdapter.newSystemFailureInstance(ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * 根据商品id获取交易隔离价格信息
     * @param request
     * @return
     */
    @Override
    @GET
    @Path("/{api:(?i:api)}/{Price:(?i:Price)}/{GetPriceByProdIdTradeIsolation:(?i:GetPriceByProdIdTradeIsolation)}")
    @Produces({MediaType.APPLICATION_JSON})
    public BaseResponseNetAdapter getPriceByProdIdWithTradeIsolation(@BeanParam GetPriceByProdIdRequest request) {
        if (request == null) {
            return BaseResponseNetAdapter.newBusinessFailureInstance("request不能为空");
        }
        try {
            ProductPrice productPrice = pricePriceQueryService.getPriceInfoByProductId(request.getBuyerId(), request.getProductId(), true);
            Map<String, Object> priceInfo = new HashMap<>();
            priceInfo.put("PriceInfo", productPrice);
            return BaseResponseNetAdapter.newSuccessInstance(priceInfo);
        } catch (Exception ex) {
            return BaseResponseNetAdapter.newSystemFailureInstance(ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * 根据商品id列表获取价格信息
     * @param request
     * @return
     */
    @Override
    public BaseResponseNetAdapter getPriceByProductIdList(GetPriceByProductIdListRequest request) {
        if (request == null) {
            return BaseResponseNetAdapter.newBusinessFailureInstance("request不能为空");
        }
        try {
            return BaseResponseNetAdapter.newSuccessInstance(null);
        } catch (Exception ex) {
            return BaseResponseNetAdapter.newSystemFailureInstance(ex.getLocalizedMessage(), ex);
        }
    }


}
