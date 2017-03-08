package com.ymatou.productprice.facade;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * 业务无关接口 例如：点火
 * Created by chenpengxuan on 2017/3/7.
 */
@Service(protocol = {"rest", "dubbo"})
@Component
@Path("")
public class SystemFacadeImpl implements SystemFacade{
    /**
     * 点火
     * @return
     */
    @GET
    @Path("/{warmup:(?i:warmup)}")
    @Produces({MediaType.TEXT_PLAIN})
    public String warmUp() {
        return "ok";
    }
}
