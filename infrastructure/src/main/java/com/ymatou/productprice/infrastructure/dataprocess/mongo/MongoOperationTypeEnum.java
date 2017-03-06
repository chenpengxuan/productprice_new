package com.ymatou.productprice.infrastructure.dataprocess.mongo;

/**
 * mongo 操作类型
 * Created by chenpengxuan on 2017/2/6.
 */
public enum  MongoOperationTypeEnum {
    //创建
    CREATE,
    //更新
    UPDATE,
    //更新不到时创建
    UPSERT,
    //删除
    DELETE,
    //查询单个
    SELECTSINGLE,
    //查询多个
    SELECTMANY
}
