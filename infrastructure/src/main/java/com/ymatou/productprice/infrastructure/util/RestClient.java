package com.ymatou.productprice.infrastructure.util;

import com.ymatou.productprice.infrastructure.config.props.BizProps;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * restful 客户端调用工具类
 * Created by chenpengxuan on 2017/3/3.
 */
@Component
public class RestClient {
    @Autowired
    private BizProps bizProps;

    @Autowired
    private LogWrapper logWrapper;

    private static OkHttpClient client;

    private static OkHttpClient.Builder builder;

    private static ConnectionPool pool;
    /**
     * 构造函数
     */
    public RestClient() {
        pool = new ConnectionPool(bizProps.getRestconnectionpoolsize(),bizProps.getRestconnectionaliveduration(),TimeUnit.MINUTES);
        builder = new OkHttpClient.Builder();
        builder
                .connectTimeout(bizProps.getRestconnectiontimeout(), TimeUnit.MILLISECONDS)
                .connectionPool(pool)
                .readTimeout(bizProps.getRestreadtimeout(),TimeUnit.MILLISECONDS)
                .writeTimeout(bizProps.getRestwritetimeout(),TimeUnit.MILLISECONDS);
        client = builder.build();
    }

    /**
     *get 方法
     * @param apiUrl
     */
    public void get(String apiUrl){
        Request request = new Request.Builder()
                .url(apiUrl)
                .build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            System.out.println(response.body().string());
        } catch (IOException e) {
            logWrapper.recordErrorLog("RestClient get 方法发生异常，apiUrl为{}",apiUrl);
        }
    }
}
