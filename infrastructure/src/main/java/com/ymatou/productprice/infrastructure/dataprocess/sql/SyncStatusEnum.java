package com.ymatou.productprice.infrastructure.dataprocess.sql;

/**
 * 业务操作状态枚举
 * Created by chenpengxuan on 2017/2/13.
 */
public enum SyncStatusEnum {
    /**
     * 业务异常，需要重试
     */
    BizEXCEPTION(-3),

    /**
     * 参数异常无需重试
     */
    IllegalArgEXCEPTION(-2),

    /**
     * 系统异常，需要重试
     */
    FAILED(-1),

    /**
     * 初始
     */
    INIT(0),

    /**
     * 成功
     */
    SUCCESS(2),

    ;


    private int code;

    private SyncStatusEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
