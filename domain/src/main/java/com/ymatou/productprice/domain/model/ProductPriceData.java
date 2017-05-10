package com.ymatou.productprice.domain.model;

import java.util.Date;
import java.util.List;

/**
 * 商品价格信息
 * Created by chenpengxuan on 2017/3/28.
 */
public class ProductPriceData {

    /**
     * 商品id
     */
    private String productId;

    /**
     * 价格最大区间
     */
    private String priceMaxRange;

    /**
     * 价格最小区间
     */
    private String priceMinRange;

    /**
     * 买手id
     */
    private Integer sellerId;

    /**
     * 未下过单或者所有订单都已取消。
     * 用作是否享受新客价的条件之一
     */
    private Boolean noOrdersOrAllCancelled;

    /**
     * 是否有确认收货的订单.
     * 用作是否享受VIP价的条件之一
     */
    private Boolean hasConfirmedOrders;

    /**
     * 商品规格价格信息列表
     */
    private List<Catalog> catalogList;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 商品上默认的物流方式
     */
    private int deliveryMethod;

    /**
     * 多物流方式
     * Unknown =0，               // 未知
     * China = 1,                       // 国内快递.由国内第三方物流公司配送
     * US = 2,                            //第三方直邮，直邮.由非官方认证的第三方国际物流公司进行承运，从海外空运并清关后完成配送
     * SailProtect = 3,               //贝海直邮.由洋码头官方物流贝海国际速递承运，从海外空运并清关后完成配送
     * Bonded = 4,                    // 第三方保税.由非官方认证的第三方国际物流公司进行承运，从国内保税仓发货并清关后完成配送
     * XloboBonded = 5,         //贝海保税.由洋码头官方物流贝海国际速递进行承运，从国内保税仓发货并清关后完成配送
     * OverSeaProxy = 6,          //认证直邮，认证直邮.由洋码头官方认证的第三方国际物流公司进行承运，从海外空运并清关后完成配送
     * OverSeaPinYou = 7,        //拼邮，拼邮. 卖家集货后由非官方认证的第三方国际物流公司承运，头程从海外空运并清关后，再在国内分包由国内快递完成二程配送
     */
    private int extraDeliveryType;

    /**
     * 运费差价
     */
    private double extraDeliveryFee;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getPriceMaxRange() {
        return priceMaxRange;
    }

    public void setPriceMaxRange(String priceMaxRange) {
        this.priceMaxRange = priceMaxRange;
    }

    public String getPriceMinRange() {
        return priceMinRange;
    }

    public void setPriceMinRange(String priceMinRange) {
        this.priceMinRange = priceMinRange;
    }

    public Integer getSellerId() {
        return sellerId;
    }

    public void setSellerId(Integer sellerId) {
        this.sellerId = sellerId;
    }

    public Boolean getNoOrdersOrAllCancelled() {
        return noOrdersOrAllCancelled;
    }

    public void setNoOrdersOrAllCancelled(Boolean noOrdersOrAllCancelled) {
        this.noOrdersOrAllCancelled = noOrdersOrAllCancelled;
    }

    public Boolean getHasConfirmedOrders() {
        return hasConfirmedOrders;
    }

    public void setHasConfirmedOrders(Boolean hasConfirmedOrders) {
        this.hasConfirmedOrders = hasConfirmedOrders;
    }

    public List<Catalog> getCatalogList() {
        return catalogList;
    }

    public void setCatalogList(List<Catalog> catalogList) {
        this.catalogList = catalogList;
    }

    public Date getUpdateTime() {
        return updateTime == null ? null : (Date)updateTime.clone();
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime == null ? null : (Date)updateTime.clone();
    }

    public int getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(int deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public int getExtraDeliveryType() {
        return extraDeliveryType;
    }

    public void setExtraDeliveryType(int extraDeliveryType) {
        this.extraDeliveryType = extraDeliveryType;
    }

    public double getExtraDeliveryFee() {
        return extraDeliveryFee;
    }

    public void setExtraDeliveryFee(double extraDeliveryFee) {
        this.extraDeliveryFee = extraDeliveryFee;
    }
}
