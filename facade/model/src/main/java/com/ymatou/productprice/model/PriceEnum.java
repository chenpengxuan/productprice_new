package com.ymatou.productprice.model;

/**
 * 价格类型枚举
 * Created by chenpengxuan on 2017/3/1.
 */
public enum  PriceEnum {

    /**
     * 原价
     */
    QUOTEPRICE(0),

    /**
     * 直播新客价
     */
    NEWCUSTOMERPRICE(1),

    /**
     * 直播vip价
     */
    VIPPRICE(2),

    /**
     * 码头活动价
     */
    YMTACTIVITYPRICE(3)
    ;

    PriceEnum(int code) {
        this.code = code;
    }

    private int code;

    public int getCode() {
        return code;
    }
}
