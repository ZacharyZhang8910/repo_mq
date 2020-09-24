package com.drf.bi.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.drf.bi.config.AppConfig;
import com.drf.bi.config.BusinessEnum;
import com.drf.bi.config.Constant;
import com.drf.bi.limit.LimitHandler;
import com.drf.bi.model.DiscountInfo;
import com.drf.bi.model.MainOrderInfo;
import com.drf.bi.model.SubOrderInfo;
import com.drf.bi.util.HttpUtil;
import com.drf.bi.util.JsonUtil;
import com.drf.bi.util.ListConvertAdapter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author ziyi.zhang
 * @date 06/2019
 */
@Slf4j
@Component
public class OrderService {

    private final AppConfig appConfig;
    private final LimitHandler limitHandler;
    private final SerializeConfig serializeConfig;
    private final SerializerFeature[] features;
    private final Cache cache;

    @Autowired
    OrderService(AppConfig appConfig,
                 LimitHandler limitHandler,
                 SerializeConfig serializeConfig,
                 SerializerFeature[] features,
                 Cache cache) {
        this.appConfig = appConfig;
        this.limitHandler = limitHandler;
        this.serializeConfig = serializeConfig;
        this.features = features;
        this.cache = cache;
        log.info("OrderService init.");
    }

    /**
     * 获取订单完整信息
     *
     * @param json Rabbitmq订单信息
     * @return 返回子订单JSON集合
     */
    public List<Object> getOrderInfo(JSONObject json) {

        // 1.获取主订单信息
        List<MainOrderInfo> mainOrderInfos = getOrderInfo(json, MainOrderInfo.class);
        MainOrderInfo mainOrderInfo = mainOrderInfos == null ? null : mainOrderInfos.get(0);
        if (mainOrderInfo == null) {
            throw new RuntimeException("没有找到[" + json.getString("bizOrderId") + "]对应的主订单信息");
        }

        // 1.1 鲜月票订单过滤
        limitHandler.xypLimitHandle(appConfig.getCurrentTopic(), mainOrderInfo);

        // 2.获取子订单信息
        List<SubOrderInfo> subOrderInfos = getOrderInfo(json, SubOrderInfo.class);
        if (subOrderInfos == null || subOrderInfos.isEmpty()) {
            throw new RuntimeException("没有找到[" + mainOrderInfo.getOrderMainId() + "]对应的子订单信息");
        }

        // 2.1 商店街订单过滤
        limitHandler.streetLimitHandle(appConfig.getCurrentTopic(), subOrderInfos);

        if (!subOrderInfos.isEmpty()) {
            // 3.将子单按100个分组，防止调用接口参数数量超过限制
            List<List<SubOrderInfo>> splitList = Lists.partition(subOrderInfos, 100);

            // 4.补全信息
            fillStoreInfo(mainOrderInfo);
            fillMcCusOrderInfo(json, subOrderInfos);
            fillOnlineCategory(mainOrderInfo, splitList);
            fillOfflineCategoryAndTaxRate(mainOrderInfo, splitList);
            fillCkx(splitList);
            fillDiscountInfo(json, splitList);
            computeField(mainOrderInfo.getSourceId(), subOrderInfos);

            // 5.合并对象
            List<Object> orderInfoList = Lists.newArrayList();
            for (SubOrderInfo subOrderInfo : subOrderInfos) {
                JSONObject jsonObject = new JSONObject();

                jsonObject.putAll(JSONObject.parseObject(JSONObject.toJSONString(mainOrderInfo, serializeConfig, features)));
                jsonObject.putAll(JSONObject.parseObject(JSONObject.toJSONString(subOrderInfo, serializeConfig, features)));
                String resultJson = JSON.toJSONString(jsonObject, SerializerFeature.WriteMapNullValue);
                if (log.isDebugEnabled()) {
                    log.debug(resultJson);
                }
                orderInfoList.add(resultJson);
            }
            return orderInfoList;
        }
        return null;
    }

    /**
     * 查询订单信息
     *
     * @param json  订单消息
     * @param clazz 需要返回的具体类类型
     * @param <T>   泛型类型
     * @return 返回订单对象集合
     */
    private <T> List<T> getOrderInfo(JSONObject json, Class<T> clazz) {
        String orderParam = String.format("storeId=%s&bizOrderId=[%s]", JsonUtil.getValue(json, "storeId", ""),
                JsonUtil.getValue(json, "bizOrderId", ""));

        JSONArray orderArray = new JSONArray();
        if (clazz == MainOrderInfo.class) {
            orderArray = HttpUtil.getJsonArray(appConfig.getApi().get(Constant.API_MAIN_ORDER_PREFIX), orderParam);
        } else if (clazz == SubOrderInfo.class) {
            orderArray = HttpUtil.getJsonArray(appConfig.getApi().get(Constant.API_SUB_ORDER_PREFIX), orderParam);
        }
        return orderArray.isEmpty() ? null : JSONArray.parseArray(orderArray.toJSONString(), clazz);
    }

    /**
     * 获取猫超的对客支付金额等指标
     *
     * @param json          订单消息
     * @param subOrderInfos 子订单对象集合
     */
    private void fillMcCusOrderInfo(JSONObject json, List<SubOrderInfo> subOrderInfos) {
        int sourceId = json.getInteger("source");
        // 获取[渠道编号： 12-猫1， 13-猫半]对客金额
        if (appConfig.getSourceType().get(Constant.SOURCES_CUS_AMT).contains(sourceId)) {
            String orderParam = String.format("storeId=%s&bizOrderId=[%s]", JsonUtil.getValue(json, "storeId", ""),
                    JsonUtil.getValue(json, "bizOrderId", ""));
            JSONArray orderArray = HttpUtil.getJsonArray(appConfig.getApi().get(Constant.API_MC_SUB_ORDER_PREFIX), orderParam);
            if (orderArray == null || orderArray.isEmpty()) {
                throw new RuntimeException(String.format("没有查询到%s订单的对客信息, json: %s", sourceId == 12 ? "猫1" : "猫半", json));
            } else if (orderArray.size() != subOrderInfos.size()) {
                throw new RuntimeException(String.format("获取到的%s子订单数与原子订单数不一致。猫超接口返回订单数： %d, 原订单数： %d。JSON: %s",
                        sourceId == 12 ? "猫1" : "猫半", orderArray.size(), subOrderInfos.size(), json));
            } else {
                for (SubOrderInfo subOrderInfo : subOrderInfos) {
                    for (Object obj : orderArray) {
                        JSONObject jsonObject = JSONObject.parseObject(obj.toString());
                        if (subOrderInfo.getOrderItemId().equals(jsonObject.getBigInteger("bizOrderId"))) {
                            subOrderInfo.setCusSalePrice(JsonUtil.getValue(jsonObject, "originalAmt", 0));
                            subOrderInfo.setCusPayDiscount(JsonUtil.getValue(jsonObject, "discountAmt", 0));
                            //subOrderInfo.setCusPayAmt(JsonUtil.getValue(jsonObject, "rtShareAmt", 0));
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 通过RT货号，获取线上商品小类
     *
     * @param mainOrderInfo 主订单信息
     * @param subOrderInfos 子订对象集合
     */
    private void fillOnlineCategory(MainOrderInfo mainOrderInfo, List<List<SubOrderInfo>> subOrderInfos) {
        // 判断是否是线上渠道
        if (appConfig.getSourceType().get(Constant.SOURCES_ONLINE).contains(mainOrderInfo.getSourceId())) {
            for (List<SubOrderInfo> orderInfos : subOrderInfos) {
                // 未在缓存中找到的RT货号
                Set<String> items = Sets.newHashSet();
                // 先从缓存中查找
                for (SubOrderInfo orderInfo : orderInfos) {
                    String res = cache.get(orderInfo.getRtItemNo() + "_onlineCategoryId", String.class);
                    if (StringUtils.isNotBlank(res)) {
                        orderInfo.setFfCategoryId(res);
                    } else {
                        items.add(orderInfo.getRtItemNo());
                    }
                }

                // 处理未在缓存中找到的
                if (CollectionUtils.isNotEmpty(items)) {
                    String itemNos = JSONObject.toJSONString(items.toArray());
                    String rtItemNoParams = String.format("data={\"serialNos\":%s,\"merchantType\":1}", itemNos);
                    JSONArray rtGoodsInfo = HttpUtil.getJsonArray(appConfig.getApi().get(Constant.API_GOODS_PREFIX), rtItemNoParams);
                    for (SubOrderInfo subOrderInfo : orderInfos) {
                        for (Object item : rtGoodsInfo) {
                            JSONObject object = (JSONObject) item;
                            String itemNo = JsonUtil.getValue(object, "serialNo", "");
                            String onlineCategoryId = JsonUtil.getValue(object, "cpSeq", "");
                            if (itemNo.equals(subOrderInfo.getRtItemNo())) {
                                // 加入缓存中
                                cache.put(itemNo + "_onlineCategoryId", onlineCategoryId);
                                subOrderInfo.setFfCategoryId(onlineCategoryId);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 通过RT货号，获取线下商品小类和商品税率
     *
     * @param mainOrderInfo 主订单对象
     * @param subOrderInfos 子订单集合
     */
    private void fillOfflineCategoryAndTaxRate(MainOrderInfo mainOrderInfo, List<List<SubOrderInfo>> subOrderInfos) {
        for (List<SubOrderInfo> orderInfos : subOrderInfos) {
            // 未在缓存中找到的RT货号
            Set<String> items = Sets.newHashSet();
            // 先从缓存中查找
            for (SubOrderInfo orderInfo : orderInfos) {
                String offlineCategoryId = cache.get(orderInfo.getRtItemNo() + "_offlineCategoryId", String.class);
                Double taxRate = cache.get(orderInfo.getRtItemNo() + "_taxRate", Double.class);
                if (StringUtils.isNotBlank(offlineCategoryId) && taxRate != null) {
                    orderInfo.setRtCategoryId(offlineCategoryId);
                    orderInfo.setSalesTaxRate(taxRate);
                } else {
                    items.add(orderInfo.getRtItemNo());
                }
            }

            if (CollectionUtils.isNotEmpty(items)) {
                String itemNos = JSONObject.toJSONString(items.toArray());
                String storeId = mainOrderInfo.getStoreId().substring(3);
                String rtItemNoParams = String.format("data={\"skuCodeList\":%s,\"storeId\":%s}", itemNos, storeId);
                JSONArray rtCategoryInfo = HttpUtil.getJsonArray(appConfig.getApi().get(Constant.API_OFFLINE_CATEGORY_PREFIX), rtItemNoParams);
                for (SubOrderInfo subOrderInfo : orderInfos) {
                    for (Object item : rtCategoryInfo) {
                        JSONObject object = (JSONObject) item;
                        String itemNo = JsonUtil.getValue(object, "skuCode", "");
                        String offlineCategoryId = JsonUtil.getValue(object, "subGrpNo", "");
                        double taxRate = JsonUtil.getValue(object, "taxRateCode", 0.0D);
                        if (itemNo.equals(subOrderInfo.getRtItemNo())) {
                            cache.put(itemNo + "_offlineCategoryId", offlineCategoryId);
                            cache.put(itemNo + "_taxRate", taxRate);
                            subOrderInfo.setRtCategoryId(offlineCategoryId);
                            subOrderInfo.setSalesTaxRate(taxRate);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取出课线
     *
     * @param subOrderInfos 子单对象集合
     */
    private void fillCkx(List<List<SubOrderInfo>> subOrderInfos) {
        for (List<SubOrderInfo> orderInfos : subOrderInfos) {
            Set<String> ffCategoryIdList = Sets.newHashSet();
            Set<String> rtCategoryIdList = Sets.newHashSet();

            for (SubOrderInfo orderInfo : orderInfos) {
                // 线上出课线
                String ffCategoryId = orderInfo.getFfCategoryId();
                JSONObject onlineChx = cache.get(ffCategoryId+ "_onlineChx", JSONObject.class);
                if (onlineChx == null || onlineChx.isEmpty()) {
                    if (StringUtils.isNotBlank(ffCategoryId)) ffCategoryIdList.add(ffCategoryId);
                } else {
                    orderInfo.setOnlineDivisionId(JsonUtil.getValue(onlineChx, "DEPTCODE1", ""));
                    orderInfo.setOnlineDivisionName(JsonUtil.getValue(onlineChx, "DEPTNAME1", ""));
                    orderInfo.setOnlineSectionId(JsonUtil.getValue(onlineChx, "DEPTCODE2", ""));
                    orderInfo.setOnlineSectionName(JsonUtil.getValue(onlineChx, "DEPTNAME2", ""));
                    orderInfo.setOnlineLineId(JsonUtil.getValue(onlineChx, "LINE_SEQ", ""));
                    orderInfo.setOnlineLineName(JsonUtil.getValue(onlineChx, "LINE_NAME", ""));
                }

                // 线下出课线
                String  rtCategoryId = orderInfo.getRtCategoryId();
                JSONObject offlineChx = cache.get(rtCategoryId + "_offlineChx", JSONObject.class);
                if (offlineChx == null || offlineChx.isEmpty()) {
                    if (StringUtils.isNotBlank(rtCategoryId)) rtCategoryIdList.add(rtCategoryId);
                } else {
                    orderInfo.setOfflineDivisionId(JsonUtil.getValue(offlineChx, "dept_code1", ""));
                    orderInfo.setOfflineDivisionName(JsonUtil.getValue(offlineChx, "dept_name1", ""));
                    orderInfo.setOfflineSectionId(JsonUtil.getValue(offlineChx, "dept_code2", ""));
                    orderInfo.setOfflineSectionName(JsonUtil.getValue(offlineChx, "dept_name2", ""));
                    orderInfo.setOfflineLineId(JsonUtil.getValue(offlineChx, "line_seq", ""));
                    orderInfo.setOfflineLineName(JsonUtil.getValue(offlineChx, "line_name", ""));
                }
            }

            // 线上出课线
            if (!ffCategoryIdList.isEmpty()) {
                String ffCategoryIds = JSONObject.toJSONString(ffCategoryIdList.toArray());
                String onlineChxParams = String.format("data={\"CP_SEQS\":%s}", ffCategoryIds);
                JSONArray onlineChxArray = HttpUtil.getJsonArray(appConfig.getApi().get(Constant.API_ONLINE_CHX_PREFIX), onlineChxParams);
                // 必须先遍历子订单集合，因为相同的小分类，API只出返回一条。如果先遍历JSON集合，那么会有子订单遍历不到。
                for (SubOrderInfo subOrderInfo : orderInfos) {
                    for (Object o : onlineChxArray) {
                        JSONObject item = (JSONObject) o;
                        String categoryId = JsonUtil.getValue(item, "CP_SEQ", "");
                        cache.put(categoryId + "_onlineChx", item);

                        if (categoryId.equals(subOrderInfo.getFfCategoryId())) {
                            subOrderInfo.setOnlineDivisionId(JsonUtil.getValue(item, "DEPTCODE1", ""));
                            subOrderInfo.setOnlineDivisionName(JsonUtil.getValue(item, "DEPTNAME1", ""));
                            subOrderInfo.setOnlineSectionId(JsonUtil.getValue(item, "DEPTCODE2", ""));
                            subOrderInfo.setOnlineSectionName(JsonUtil.getValue(item, "DEPTNAME2", ""));
                            subOrderInfo.setOnlineLineId(JsonUtil.getValue(item, "LINE_SEQ", ""));
                            subOrderInfo.setOnlineLineName(JsonUtil.getValue(item, "LINE_NAME", ""));
                            break;
                        }
                    }
                }
            }

            // 线下出课线
            if (!rtCategoryIdList.isEmpty()) {
                String rtCategoryIds = JSONObject.toJSONString(rtCategoryIdList.toArray());
                String offlineChxParams = String.format("data={\"cs_codes\":%s,\"dept_code\":\"RT0\"}", rtCategoryIds);
                JSONArray offlineChxArray = HttpUtil.getJsonArray(appConfig.getApi().get(Constant.API_OFFLINE_CHX_PREFIX), offlineChxParams);
                // 必须先遍历子订单集合，因为相同的小分类，API只出返回一条。如果先遍历JSON集合，那么会有子订单遍历不到。
                for (SubOrderInfo subOrderInfo : orderInfos) {
                    for (Object o : offlineChxArray) {
                        JSONObject item = (JSONObject) o;
                        String categoryId = JsonUtil.getValue(item, "cs_code", "");
                        cache.put(categoryId + "_offlineChx", item);

                        if (categoryId.equals(subOrderInfo.getRtCategoryId())) {
                            subOrderInfo.setOfflineDivisionId(JsonUtil.getValue(item, "dept_code1", ""));
                            subOrderInfo.setOfflineDivisionName(JsonUtil.getValue(item, "dept_name1", ""));
                            subOrderInfo.setOfflineSectionId(JsonUtil.getValue(item, "dept_code2", ""));
                            subOrderInfo.setOfflineSectionName(JsonUtil.getValue(item, "dept_name2", ""));
                            subOrderInfo.setOfflineLineId(JsonUtil.getValue(item, "line_seq", ""));
                            subOrderInfo.setOfflineLineName(JsonUtil.getValue(item, "line_name", ""));
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取优惠信息
     *
     * @param json          订单消息
     * @param subOrderInfos 子订单对象集合
     */
    private void fillDiscountInfo(JSONObject json, List<List<SubOrderInfo>> subOrderInfos) {
        for (List<SubOrderInfo> orderInfos : subOrderInfos) {
            // 子单号数组，eg: ["123","213",...]
            String itemIds = JSONObject.toJSONString(new ListConvertAdapter<String, SubOrderInfo>(orderInfos,
                    ListConvertAdapter.CommonPropertyAware.ORDER_ITEM_ID).getElementsArray());

            String discountParam = String.format("storeId=%s&bizOrderId=%s", JsonUtil.getValue(json, "storeId", ""), itemIds);
            JSONArray discountArray = HttpUtil.getJsonArray(appConfig.getApi().get(Constant.API_PROMOTION_PREFIX), discountParam);

            // 子单与优惠信息是1对多，所以必须是先遍历子单
            for (SubOrderInfo subOrderInfo : orderInfos) {
                List<DiscountInfo> discountInfos = new ArrayList<>();
                for (Object o : discountArray) {
                    JSONObject item = (JSONObject) o;
                    long itemId = JsonUtil.getValue(item, "bizOrderId", 0L);
                    if (subOrderInfo.getOrderItemId() != null && itemId == subOrderInfo.getOrderItemId().longValue()) {
                        discountInfos.add(JSONObject.parseObject(item.toJSONString(), DiscountInfo.class));
                    }
                }
                subOrderInfo.setDiscountInfos(discountInfos);
            }
        }
    }

    /**
     * 根据门店ID获取门店信息
     *
     * @param mainOrderInfo 主订单信息
     */
    private void fillStoreInfo(MainOrderInfo mainOrderInfo) {
        String storeId = mainOrderInfo.getStoreId().substring(3);
        JSONObject jsonObject = cache.get(storeId, JSONObject.class);
        if (jsonObject == null || jsonObject.isEmpty()) {
            String storeIdParam = String.format("data={\"storeIds\":[\"%s\"],\"name\":\"\"}", storeId);
            JSONArray storeInfo = HttpUtil.getJsonArray(appConfig.getApi().get(Constant.API_STORE_PREFIX), storeIdParam, storeId);
            if (!storeInfo.isEmpty()) {
                jsonObject = storeInfo.getJSONObject(0);
                cache.put(storeId, jsonObject);
            }
        }
        mainOrderInfo.setStoreName(JsonUtil.getValue(jsonObject, "storeName", ""));
        mainOrderInfo.setSubRegionId(Constant.REGION_PREFIX + JsonUtil.getValue(jsonObject, "subId", ""));
        mainOrderInfo.setSubRegionName(JsonUtil.getValue(jsonObject, "subArea", ""));
        mainOrderInfo.setRegionId(Constant.REGION_PREFIX + JsonUtil.getValue(jsonObject, "pgSeq", ""));
        mainOrderInfo.setRegionName(JsonUtil.getValue(jsonObject, "pgName", ""));
    }

    /**
     * 计算指标
     *
     * @param sourceId      渠道编号
     * @param subOrderInfos 子订单集合
     */
    private void computeField(int sourceId, List<SubOrderInfo> subOrderInfos) {
        fillPayQtyAndPayWeightQty(sourceId, subOrderInfos);
        if (BusinessEnum.DLVR.equals(appConfig.getBusiness())) {
            fillDlvrQtyAndDlvrWeightQtyAndDlvrAmt(sourceId, subOrderInfos);
        }
    }

    /**
     * 根据各渠道计算支付商品件数和支付商品重量
     *
     * @param sourceId      渠道编号
     * @param subOrderInfos 子订单集合
     */
    private void fillPayQtyAndPayWeightQty(int sourceId, List<SubOrderInfo> subOrderInfos) {
        for (SubOrderInfo subOrderInfo : subOrderInfos) {
            int quantity = subOrderInfo.getQuantity() == null ? 0 : subOrderInfo.getQuantity();
            double nsQuantity = subOrderInfo.getNsQuantity() == null ? 0.0 : subOrderInfo.getNsQuantity();
            Integer cusSalePrice = subOrderInfo.getCusSalePrice() == null ? 0 : subOrderInfo.getCusSalePrice();
            Integer cusPayDiscount = subOrderInfo.getCusPayDiscount() == null ? 0 : subOrderInfo.getCusPayDiscount();

            int payQty = 0;
            double payWeightQty = 0.0;
            //payQty
            if (Constant.KIND_GIFT.equals(subOrderInfo.getKind())) {
                payQty = quantity >= 1 ? quantity : (int) nsQuantity;
            } else {
                if (appConfig.getSourceType().get(Constant.SOURCES_PAY_QTY1).contains(sourceId)) {
                    payQty = Math.max(quantity, 1);
                } else if (appConfig.getSourceType().get(Constant.SOURCES_PAY_QTY2).contains(sourceId)) {
                    payQty = quantity;
                }
            }
            // payWeightQty
            if (appConfig.getSourceType().get(Constant.SOURCES_PAY_QTY1).contains(sourceId)) {
                payWeightQty = quantity >= 1 ? quantity : nsQuantity;
            } else if (appConfig.getSourceType().get(Constant.SOURCES_PAY_QTY2).contains(sourceId)) {
                payWeightQty = quantity;
            }
            subOrderInfo.setPayQty(payQty);
            subOrderInfo.setPayWeightQty(payWeightQty);
            subOrderInfo.setCusPayAmt(cusSalePrice - cusPayDiscount);
        }
    }

    /**
     * 根据各渠道计算出货商品件数、出货商品重量和出货金额
     *
     * @param sourceId      渠道编号
     * @param subOrderInfos 子订单集合
     */
    private void fillDlvrQtyAndDlvrWeightQtyAndDlvrAmt(int sourceId, List<SubOrderInfo> subOrderInfos) {
        for (SubOrderInfo subOrderInfo : subOrderInfos) {
            int quantity = subOrderInfo.getQuantity() == null ? 0 : subOrderInfo.getQuantity();
            Double nsQuantity = subOrderInfo.getNsQuantity() == null ? 0.0 : subOrderInfo.getNsQuantity();
            Double pickAmountStock = subOrderInfo.getPickAmountStock() == null ? 0.0 : subOrderInfo.getPickAmountStock();
            Integer subTotalPrice = subOrderInfo.getSubTotalPrice() == null ? 0 : subOrderInfo.getSubTotalPrice();
            Integer discountTotal = subOrderInfo.getDiscountTotal() == null ? 0 : subOrderInfo.getDiscountTotal();
            Integer cusSalePrice = subOrderInfo.getCusSalePrice() == null ? 0 : subOrderInfo.getCusSalePrice();
            Integer cusPayDiscount = subOrderInfo.getCusPayDiscount() == null ? 0 : subOrderInfo.getCusPayDiscount();

            int deliverQty = 0;
            double deliverWeightQty = 0.0;
            double deliverAmt = 0.0;
            double cusDlvrAmt = 0;
            //deliverQty
            if (Constant.KIND_GIFT.equals(subOrderInfo.getKind())) {
                deliverQty = quantity >= 1 ? quantity : nsQuantity.intValue();
            } else {
                if (appConfig.getSourceType().get(Constant.SOURCES_DLVR_QTY1).contains(sourceId)) {
                    deliverQty = Math.max(quantity, 1);
                } else if (appConfig.getSourceType().get(Constant.SOURCES_DLVR_QTY2).contains(sourceId)) {
                    deliverQty = quantity >= 1 ? pickAmountStock.intValue() : 1;
                } else if (appConfig.getSourceType().get(Constant.SOURCES_DLVR_QTY3).contains(sourceId)) {
                    deliverQty = quantity;
                }
            }
            //deliverWeightQty
            if (appConfig.getSourceType().get(Constant.SOURCES_DLVR_WEIGHT1).contains(sourceId)) {
                deliverWeightQty = quantity >= 1 ? quantity : nsQuantity;
            } else if (appConfig.getSourceType().get(Constant.SOURCES_DLVR_WEIGHT2).contains(sourceId)) {
                deliverWeightQty = pickAmountStock;
            } else if (appConfig.getSourceType().get(Constant.SOURCES_DLVR_WEIGHT3).contains(sourceId)) {
                deliverWeightQty = quantity;
            }
            // delverAmt
            if (appConfig.getSourceType().get(Constant.SOURCES_DLVR_AMT1).contains(sourceId)) {
                deliverAmt = (subTotalPrice - discountTotal);
            } else if (appConfig.getSourceType().get(Constant.SOURCES_DLVR_AMT2).contains(sourceId)) {
                if (nsQuantity > 0) {
                    deliverAmt = pickAmountStock / nsQuantity > 1 ?
                            (subTotalPrice - discountTotal) :
                            (pickAmountStock / nsQuantity) * (subTotalPrice - discountTotal);

                    cusDlvrAmt = pickAmountStock / nsQuantity > 1 ?
                            (cusSalePrice - cusPayDiscount) :
                            (pickAmountStock / nsQuantity) * (cusSalePrice - cusPayDiscount);
                } else {
                    if (quantity != 0) {
                        deliverAmt = (pickAmountStock / quantity) * (subTotalPrice - discountTotal);
                        cusDlvrAmt = (pickAmountStock / quantity) * (cusSalePrice - cusPayDiscount);
                    }
                }
            } else if (appConfig.getSourceType().get(Constant.SOURCES_DLVR_AMT3).contains(sourceId)) {
                deliverAmt = subTotalPrice - discountTotal;
            }

            subOrderInfo.setDeliverQty(deliverQty);
            subOrderInfo.setDeliverWeightQty(deliverWeightQty);
            subOrderInfo.setDeliverAmt(deliverAmt);
            subOrderInfo.setCusDlvrAmt(cusDlvrAmt);
        }
    }
}
