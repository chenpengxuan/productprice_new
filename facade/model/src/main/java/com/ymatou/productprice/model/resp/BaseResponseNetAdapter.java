package com.ymatou.productprice.model.resp;

import java.util.HashMap;
import java.util.Map;

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
    public Map<String,Object> Data;

    /**
     * 创建系统异常resp
     * @return
     */
    public static BaseResponseNetAdapter newSystemFailureInstance(){
        BaseResponseNetAdapter baseResponseNetAdapter = new BaseResponseNetAdapter();
        baseResponseNetAdapter.BCode = 0;
        baseResponseNetAdapter.Code = 500;
        baseResponseNetAdapter.Msg = "系统出现异常";
        baseResponseNetAdapter.Data = new HashMap<>();
        return baseResponseNetAdapter;
    }

    /**
     * 创建业务异常resp
     * @return
     */
    public static BaseResponseNetAdapter newBusinessFailureInstance(String errorMsg){
        BaseResponseNetAdapter baseResponseNetAdapter = new BaseResponseNetAdapter();
        baseResponseNetAdapter.BCode = 500;
        baseResponseNetAdapter.Code = 500;
        baseResponseNetAdapter.Msg = "业务检查异常,异常原因为：" + errorMsg;
        baseResponseNetAdapter.Data = new HashMap<>();
        return baseResponseNetAdapter;
    }

    /**
     * 创建成功resp
     * @param data
     * @return
     */
    public static BaseResponseNetAdapter newSuccessInstance(Map<String,Object> data){
        BaseResponseNetAdapter baseResponseNetAdapter = new BaseResponseNetAdapter();
        baseResponseNetAdapter.BCode = 200;
        baseResponseNetAdapter.Code = 200;
        baseResponseNetAdapter.Msg = "";
        baseResponseNetAdapter.Data = data;
        return baseResponseNetAdapter;
    }
}
