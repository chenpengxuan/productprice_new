/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */
package com.ymatou.productprice.model.resp;


import com.ymatou.productprice.model.PrintFriendliness;

/**
 * 响应基类. <em>其所有子类必须有默认的构造函数</em>
 *
 * @author tuwenjie
 */
public class BaseResponse extends PrintFriendliness {

    private static final long serialVersionUID = -5719901720924490738L;

    private boolean isSuccess = true;

    private String errorMessage;

    private int errorCode;

    private String message;

    public BaseResponse(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public BaseResponse() {
    }

    public static BaseResponse newSuccessInstance() {
        BaseResponse result = new BaseResponse();
        result.setSuccess(true);
        result.setMessage("处理成功");
        return result;
    }

    public static BaseResponse newFailInstance(int errorCode,String message) {
        BaseResponse result = new BaseResponse();
        result.setSuccess(false);
        result.setErrorCode(errorCode);
        result.setErrorMessage(message);
        return result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
