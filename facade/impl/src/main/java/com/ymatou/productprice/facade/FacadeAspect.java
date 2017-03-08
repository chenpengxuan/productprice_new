package com.ymatou.productprice.facade;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.ymatou.productprice.infrastructure.constants.Constants;
import com.ymatou.productprice.infrastructure.util.LogWrapper;
import com.ymatou.productprice.infrastructure.util.Utils;
import com.ymatou.productprice.model.BizException;
import com.ymatou.productprice.model.req.BaseRequest;
import com.ymatou.productprice.model.resp.BaseResponseNetAdapter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Facade实现方法的AOP.
 * 实现与业务无关的通用操作。
 * 1，日志
 * 2，异常处理等
 *
 * Created by chenpengxuan on 2017/3/7.
 */
@Aspect
@Component
public class FacadeAspect {
    @Autowired
    private LogWrapper logWrapper;

    @Pointcut("execution(* com.ymatou.productprice.facade.*Facade.*(*)) && args(req)")
    public void executeFacade(BaseRequest req) {
    }

    @Around("executeFacade(req)")
    public Object aroundFacadeExecution(ProceedingJoinPoint joinPoint, BaseRequest req)
            throws InstantiationException, IllegalAccessException {

        if (req == null) {
            logWrapper.recordErrorLog("{} Recv: null", joinPoint.getSignature());
            return BaseResponseNetAdapter.newBusinessFailureInstance("request is null");
        }

        if (req.requireRequestId() && StringUtils.isEmpty(req.getRequestId())) {
            return BaseResponseNetAdapter.newBusinessFailureInstance("requestId not provided");
        }

        if (req.requireAppId() && StringUtils.isEmpty(req.getAppId())) {
            return BaseResponseNetAdapter.newBusinessFailureInstance("appId not provided");
        }

        long startTime = System.currentTimeMillis();

        if (StringUtils.isEmpty(req.getRequestId())) {
            req.setRequestId(Utils.uuid());
        }

        // log日志配有"logPrefix"占位符
        MDC.put(Constants.LOG_PREFIX, getRequestFlag(req));

        logWrapper.recordInfoLog("RequestInfo:" + req);

        Object resp = null;

        try {

            req.validate();

            resp = joinPoint.proceed(new Object[]{req});

        } catch (IllegalArgumentException e) {
            resp = BaseResponseNetAdapter.newBusinessFailureInstance(e.getLocalizedMessage());
            logWrapper.recordErrorLog("Invalid request: {}", req, e);
        } catch (BizException e) {
            //前端可能将错误msg直接抛给用户
            resp = BaseResponseNetAdapter.newBusinessFailureInstance(e.getLocalizedMessage());
            logWrapper.recordErrorLog("Failed to execute request: {}, Error:{}", req.getRequestId(),
                   e.getMessage(), e);
        } catch (Throwable e) {
            //前端可能将错误msg直接抛给用户
            resp = BaseResponseNetAdapter.newSystemFailureInstance(e.getLocalizedMessage(),e);
            logWrapper.recordErrorLog("Unknown error in executing request:{}", req, e);
        } finally {
            long consumedTime = System.currentTimeMillis() - startTime;

            if ( consumedTime >= 300L) {
                logWrapper.recordErrorLog("Slow query({}ms). Req:{}", consumedTime, req);
            }
            MDC.clear();
        }

        return resp;
    }

    private String getRequestFlag(BaseRequest req) {
        return req.getClass().getSimpleName() + "|" + req.getRequestId();
    }

}
