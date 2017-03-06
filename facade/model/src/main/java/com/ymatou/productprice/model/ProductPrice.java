package com.ymatou.productprice.model;

import java.util.List;

/**
 * 商品价格信息
 * Created by chenpengxuan on 2017/3/1.
 */
public class ProductPrice {
    /**
     * 商品id
     */
    public String ProductId;

    /**
     * 规格信息
     */
    public List<Catalog> Catalogs;

    /**
     * 未下过单或者所有订单都已取消。用作是否享受新客价的条件之一
     */
    public Boolean NoOrdersOrAllCancelled;

    /**
     * 是否有确认收货的订单，用作是否享受VIP价的条件
     */
    public Boolean HasConfirmedOrders;
}
