package com.ymatou.productprice.model;

/**
 * 商品规格
 * Created by chenpengxuan on 2017/3/1.
 */
public class Catalog {
    /**
     * 规格编号
     */
    public String CatalogId;

    /**
     * 价格
     */
    public double Price;

    /**
     * 原价
     */
    public double QuotePrice;

    /**
     * 新客价
     */
    public double NewCustomerPrice;

    /**
     * VIP价
     */
    public double VipPrice;

    /**
     * 活动价
     */
    public double ActivityPrice;

    /**
     * 活动新人价
     */
    public double SubsidyPrice;

    /**
     * 定金价
     */
    public double EarnestPrice;

    /**
     * 价格类型
     */
    public PriceEnum PriceType;
}
