package com.drf.bi.model;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.drf.bi.config.AppConfig;
import com.drf.bi.config.Constant;
import com.drf.bi.convert.*;
import com.drf.bi.util.SpringContextHolder;
import lombok.Data;

import java.math.BigInteger;
import java.util.List;

/**
 * 主订单信息
 *
 * @author jian.zhang
 * @date 2019/6/17 20:06
 */
@Data
public class MainOrderInfo {

    /**
     * 主订单号
     */
    @JSONField(alternateNames = "bizOrderId")
    private BigInteger orderMainId;

    /**
     * 支付时间
     */
    @JSONField(alternateNames = "payTime", deserializeUsing = TimestampValueDeserializer.class)
    private String orderPayTime;

    /**
     * 打包时间(出货时间)
     */
    @JSONField(alternateNames = "packageTime", deserializeUsing = PackageTimeValueDeserializer.class)
    private String orderPackageTime;

    /**
     * 入账时间
     */
    @JSONField(alternateNames = "pushTime", deserializeUsing = PushTimeValueDeserializer.class)
    private String orderPushTime;

    /**
     * 转单时间
     */
    @JSONField(alternateNames = "transferTime", deserializeUsing = TimestampValueDeserializer.class)
    private String orderTransferTime;

    /**
     * 门店编号
     */
    @JSONField(serializeUsing = StoreValueSerializer.class, deserializeUsing = StoreValueDeserializer.class)
    private String storeId;

    /**
     * 会员唯一编号
     */
    @JSONField
    private String openUid;

    /**
     * 扩展字段
     */
    @JSONField
    private String extendedJson;

    /**
     * 平台类型:1-飞牛网，5-ASUS，6-ASUS(手机)，7-代出货，8-京东，9-IOS，10-安卓，11-触屏，12-万点通，
     * 13-RT-IOS，14-RT－安卓，15-苏州中移动，16-团购手工单，17-电子屏，18-商赢，19-常州邮政，20-微分销，
     * 21-实惠，22-GRS，23-拍拍货，24-补发货，25-微信小程序，26-安卓飞牛APP，27-IOS飞牛APP
     */
    //@JSONField(alternateNames = "extendedJson", deserializeUsing = SiteModeDeserializer.class)
    private Integer siteMode;

    public Integer getSiteMode() {
        JSONObject jsonObject = JSONObject.parseObject(this.extendedJson);
        return jsonObject !=null ? jsonObject.getInteger("siteMode") :  null;
    }

    /**
     * 用户到配送站距离，单位米(优鲜使用0：原区，1：扩区)
     */
    @JSONField
    private Double deliveryDistance;


    /**
     * 指定开始送达时间
     */
    @JSONField(alternateNames = "expectArriveTimeBegin", deserializeUsing = TimestampValueDeserializer.class)
    private String requireDeliverTime;

    /**
     * 指定结束送达时间
     */
    @JSONField(alternateNames = "expectArriveTimeEnd", deserializeUsing = TimestampValueDeserializer.class)
    private String requireDeliverTimeEnd;

    /**
     * 渠道编号
     */
    @JSONField(alternateNames = "source")
    private Integer sourceId;

    /**
     * 订单创建时间
     */
    @JSONField(alternateNames = "gmtCreate", deserializeUsing = TimestampValueDeserializer.class)
    private String orderCreateTime;

    /**
     * 销售范围 （０:默认三公里 １:三至五公里）
     */
    private Integer saleScope;

    /**
     * 销售范围 （０:默认三公里 １:三至五公里）
     *
     * @return
     */
    public Integer getSaleScope() {
        int scope = 0;
        AppConfig appConfig = SpringContextHolder.getBean(AppConfig.class);
        List<Integer> saleScopeSources = appConfig.getSourceType().get(Constant.SOURCES_SALE_SCOPE);
        if (saleScopeSources.contains(sourceId)) {
            if (deliveryDistance >= 1) {
                scope = 1;
            }
        }else {
            if (deliveryDistance >= 3001) {
                scope = 1;
            }
        }
        return scope;
    }

    /**
     * 门店名称
     */
    private String storeName;
    /**
     * 小区编号
     */
    private String subRegionId;
    /**
     * 小区名称
     */
    private String subRegionName;
    /**
     * 大区编号
     */
    private String regionId;
    /**
     * 大区名称
     */
    private String regionName;

    /**
     * 机台号
     */
    @JSONField
    private Integer posNo;

    /**
     * 主单类型：1-正常，2-预售，3-外卖，4-堂食，5-外带
     */
    @JSONField
    private Integer OrderType;

    @Override
    public String toString() {
        return "{" +
                "orderMainId=" + orderMainId +
                ", storeId='" + storeId + '\'' +
                ", sourceId=" + sourceId +
                '}';
    }
}
