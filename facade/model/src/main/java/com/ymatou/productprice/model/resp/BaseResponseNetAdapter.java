package com.ymatou.productprice.model.resp;

import java.util.HashMap;

/**
 * .net 返回风格匹配器
 * Created by chenpengxuan on 2017/3/6.
 */
public class BaseResponseNetAdapter {
    /**
     * 系统状态码 成功为200 失败为500
     */
    public int Code;

    /**
     * 业务状态码
      */
    public int BCode;

    /**
     * 返回消息
     */
    public String Msg;

    /**
     * 数据
     */
    public HashMap<String,Object> Data;

    public static BaseResponseNetAdapter newfailureInstance(){

    }
}
