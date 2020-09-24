package com.drf.bi.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

/**
 * 子订单信息
 *
 * @author jian.zhang
 * @date 2019/6/17 15:28
 */
@Data
public class SubOrderInfo implements Serializable {
    //alternateNames: 反序列化时名称，name：序列化时名称

    /**
     * 子订单编号
     */
    @JSONField(alternateNames = "bizOrderId")
    private BigInteger orderItemId;

    /**
     * RT商品货号
     */
    @JSONField(alternateNames = "itemCode")
    private String rtItemNo;

    /**
     * 订单类型，COMMON：主商品，GIFT：赠品
     */
    @JSONField(alternateNames = "orderType")
    private String kind;

    /**
     * RT商品小类
     */
    @JSONField(alternateNames = "subGrpNo")
    private String rtCategoryId;

    /**
     * 标品数量
     */
    @JSONField
    private Integer quantity;

    /**
     * 称重品数量
     */
    @JSONField
    private Double nsQuantity;

    /**
     * 单价，单位为分
     */
    @JSONField
    private Integer price;

    /**
     * 总商品金额，单位分
     */
    @JSONField(alternateNames = "originalAmt")
    private Integer subTotalPrice;

    /**
     * 淘鲜达平台出资金额
     */
    @JSONField
    private Integer txdPmtAmt;

    /**
     * 税率编号
     */
    @JSONField
    private Integer vatNo;

    /**
     * 销货税率
     */
    @JSONField(alternateNames = "taxRate")
    private Double salesTaxRate;

    /**
     * 折扣总金额，单位为分
     */
    @JSONField(alternateNames = "promotionDiscountAmt")
    private Integer discountTotal;

    /**
     * 优惠信息
     */
    @JSONField
    private List<DiscountInfo> discountInfos;

    /**
     * 拣货数量
     */
    private Double pickAmountStock;

    /**
     * 商品类别：1：标品，2：称重品
     */
    private Integer itemType;

    private String ffCategoryId;

    private String onlineDivisionId;
    private String onlineDivisionName;
    private String onlineSectionId;
    private String onlineSectionName;
    private String onlineLineId;
    private String onlineLineName;

    private String offlineDivisionId;
    private String offlineDivisionName;
    private String offlineSectionId;
    private String offlineSectionName;
    private String offlineLineId;
    private String offlineLineName;

    private Integer payQty;
    private Double payWeightQty;
    private Integer deliverQty;
    private Double deliverWeightQty;
    private Double payAmt;
    private Double deliverAmt;


    /**
     * 对客售价
     */
    private Integer cusSalePrice;
    /**
     * 对客折扣
     */
    private Integer cusPayDiscount;
    /**
     * 对客支付金额
     */
    private Integer cusPayAmt;
    /**
     * 对客出货价
     */
    private Double cusDlvrAmt;

    /**
     * 是否是商店街平台标：0-否，1-是
     */
    @JSONField
    private Integer platform;

    public Integer getQuantity() {
        return quantity == null ? 0 : quantity;
    }

    public Double getNsQuantity() {
        return nsQuantity == null ? 0 : nsQuantity;
    }

    public Integer getItemType() {
        return this.getQuantity() <= 0 ? 2 : 1;
    }
    /**
     * 支付金额 = 订单支付总金额 - 折扣金额
     *
     * @return payAmt
     */
    public double getPayAmt() {
        return subTotalPrice - discountTotal;
    }


    @Override
    public String toString() {
        return "{" +
                "orderItemId=" + orderItemId +
                ", rtItemNo='" + rtItemNo + '\'' +
                ", platform=" + platform +
                '}';
    }
}
