/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.productprice.model;


/**
 * 业务异常
 *
 * @author tuwenjie
 */
public class BizException extends RuntimeException {
    private static final long serialVersionUID = 1857440708804128584L;

    public BizException(int errorCode, String msg) {

        this(errorCode, msg, null);
    }


    public BizException(int errorCode) {
        this(errorCode, "业务异常", null);
    }


    public BizException(int errorCode, String msg, Throwable cause) {
        super(msg, cause);
    }

    public static void throwBizException(int errorCode, String message, Throwable cause) {
        throw new BizException(errorCode, message, cause);
    }

    public static void throwBizException(int errorCode, String message) {
        throwBizException(errorCode, message, null);
    }
}
