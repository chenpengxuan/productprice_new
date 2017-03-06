/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */
package com.ymatou.productprice.model.req;


import com.ymatou.productprice.model.PrintFriendliness;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;


/**
 * 
 * @author tuwenjie 2016年5月7日 下午3:55:15
 *
 */
public abstract class BaseRequest extends PrintFriendliness {


    private static Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * 客户端应用ID
     */
    private String appId;

    /**
     * 请求ID
     */
    private String requestId;

    public String getAppId() {
        return appId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void validate() {
        StringBuilder errorMsgs = new StringBuilder();
        Set<ConstraintViolation<BaseRequest>> violations = VALIDATOR.validate(this);
        if (violations != null && violations.size() > 0) {
            for (ConstraintViolation<BaseRequest> violation : violations) {
                errorMsgs.append(violation.getPropertyPath()).append(":").append(violation.getMessage()).append("|");
            }
            throw new IllegalArgumentException(errorMsgs.substring(0, errorMsgs.length() - 1));
        }
    }
    
    
    /**
     * 一般请求，requestId不强制必填
     * @return
     */
    public boolean requireRequestId() {
        return false;
    }

    /**
     * 老的.net系统的请求都沒有appId
     * @return
     */
    public boolean requireAppId() {
        return false;
    }
}
