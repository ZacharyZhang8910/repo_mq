package com.drf.bi.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.drf.bi.config.Constant;
import lombok.Data;

import java.io.Serializable;

/**
 * 优惠信息
 *
 * @author jian.zhang
 * @date 2019/6/17 15:37
 */
@Data
public class DiscountInfo implements Serializable {

    /**
     * 折扣组别，1：优惠券，2：营销活动
     */
    private Integer discountGroup;

    /**
     * 折扣类型
     */
    @JSONField(alternateNames = "activeType")
    private String discountType;

    /**
     * 活动编号
     */
    @JSONField
    private String activeId;

    /**
     * 活动名称
     */
    @JSONField
    private String activeName;

    /**
     * 折扣金额 单位为分
     */
    @JSONField(alternateNames = "discountFee")
    private Integer discount;

    /**
     * 优惠券编号
     */
    @JSONField
    private String voucherId;

    /**
     * 通过discountType判断赋值
     * shopbonus itemcoupon -- 商品券
     * 1:商品券 2:营销活动
     */
    public Integer getDiscountGroup() {
        return Constant.DISCOUNT_TYPE.contains(discountType == null ? null : discountType.trim()) ? 1 : 2;
    }
}
