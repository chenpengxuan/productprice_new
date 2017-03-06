package com.ymatou.productprice.facade;

import com.alibaba.dubbo.config.annotation.Service;
import com.ymatou.productprice.domain.PricePriceQueryService;
import com.ymatou.productprice.infrastructure.dataprocess.sql.SyncStatusEnum;
import com.ymatou.productprice.model.req.GetPriceByProdIdRequest;
import com.ymatou.productprice.model.resp.BaseResponse;
import com.ymatou.productprice.model.resp.GetPriceByProdIdResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * 价格服务
 * 同时支持http rpc
 * Created by chenpengxuan on 2017/3/1.
 */
@Service(protocol = {"rest", "dubbo"})
@Component
@Path("")
public class ProductPriceFacadeImpl implements ProductPriceFacade{

    @Autowired
    private PricePriceQueryService pricePriceQueryService;
    /**
     * 点火
     * @return
     */
    @Override
    @GET
    @Path("/{warmup:(?i:warmup)}")
    @Produces({MediaType.TEXT_PLAIN})
    public String warmUp() {
        return "ok";
    }

    /**
     * 根据商品id获取价格信息
     * @param request
     * @return
     */
    @Override
    @GET
    @Path("/{api:(?i:api)}/{Price:(?i:Price)}/{GetPriceByProdId:(?i:GetPriceByProdId)}")
    @Produces({MediaType.APPLICATION_JSON})
    public BaseResponse getPriceByProdId(@BeanParam GetPriceByProdIdRequest request) {
        if(request == null){
            return BaseResponse.newFailInstance(SyncStatusEnum.IllegalArgEXCEPTION.getCode(),"request不能为空");
        }
        GetPriceByProdIdResponse response = new GetPriceByProdIdResponse();
        try{
            response.PriceInfo = pricePriceQueryService.getPriceInfoByProductId(request.getBuyerId(),request.getProductId(),false);
            response.setMessage("操作成功");
            response.setSuccess(true);
            return response;
        }catch (Exception ex){
            return BaseResponse.newFailInstance(SyncStatusEnum.FAILED.getCode(),ex.getMessage());
        }
    }


}
