package com.ymatou.productprice.infrastructure.util;

/**
 * 二元组
 * Created by chenpengxuan on 2017/4/6.
 */
public class Tuple<A,B> {
    public final A first;
    public final B second;

    public Tuple(A a, B b) {
        this.first = a;
        this.second = b;
    }
}
