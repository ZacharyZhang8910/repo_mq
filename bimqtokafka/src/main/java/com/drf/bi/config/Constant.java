package com.drf.bi.config;

import java.util.Arrays;
import java.util.List;

/**
 * 常量类
 *
 * @author jian.zhang
 * <p>
 * DateTime: 2019-06-29 22:15:21
 */
public interface Constant {
    String KAFKA_BROKER_LIST = "brokerList";
    String KAFKA_ZK_LIST = "zkList";
    String KAFKA_SERIALIZER = "serializer";
    String KAFKA_PRODUCER_TYPE = "producerType";
    String KAFKA_ACKS = "acks";
    String PAY_TOPIC = "pay";
    String DLVR_TOPIC = "dlvr";
    String RABBITMQ_COMMON_USERNAME = "common.username";
    String RABBITMQ_COMMON_PASSWORD = "common.password";
    String RABBITMQ_COMMON_HOSTS = "common.hosts";
    String RABBITMQ_COMMON_VIRTUAL_HOST = "common.virtualHost";
//    String RABBITMQ_QUEUE_PAY_EXCHANGE_NAME = "queue.pay.exchangeName";
//    String RABBITMQ_QUEUE_PAY_QUEUE_NAME = "queue.pay.queueName";
//    String RABBITMQ_QUEUE_PAY_ROUTING_KEY = "queue.pay.routingKey";
//    String RABBITMQ_QUEUE_DLVR_EXCHANGE_NAME = "queue.dlvr.exchangeName";
//    String RABBITMQ_QUEUE_DLVR_QUEUE_NAME = "queue.dlvr.queueName";
//    String RABBITMQ_QUEUE_DLVR_ROUTING_KEY = "queue.dlvr.routingKey";
    String API_MAIN_ORDER_PREFIX = "mainOrder";
    String API_SUB_ORDER_PREFIX = "subOrder";
    String API_MC_SUB_ORDER_PREFIX = "mcSubOrder";
    String API_PROMOTION_PREFIX = "promotion";
    String API_GOODS_PREFIX = "goods";
    String API_STORE_PREFIX = "store";
    String API_OFFLINE_CATEGORY_PREFIX = "offlineCate";
    String API_ONLINE_CHX_PREFIX = "onlineChx";
    String API_OFFLINE_CHX_PREFIX = "offlineChx";
    String API_URL = "url";
    String API_STATUS_FIELD = "statusField";
    String API_STATUS_CODE = "statusCode";
    String API_DATA_FIELD = "dataField";
    String API_DATA_TYPE = "dataType";

    String KIND_GIFT = "GIFT";
    List<String> DISCOUNT_TYPE = Arrays.asList("shopbonus", "itemcoupon");
    String REGION_PREFIX = "CPG";

    String SOURCES_PAY_QTY1 = "payQty1";
    String SOURCES_PAY_QTY2 = "payQty2";
    String SOURCES_DLVR_QTY1 = "dlvrQty1";
    String SOURCES_DLVR_QTY2 = "dlvrQty2";
    String SOURCES_DLVR_QTY3 = "dlvrQty3";
    String SOURCES_DLVR_WEIGHT1 = "dlvrWeight1";
    String SOURCES_DLVR_WEIGHT2 = "dlvrWeight2";
    String SOURCES_DLVR_WEIGHT3 = "dlvrWeight3";
    String SOURCES_DLVR_AMT1 = "dlvrAmt1";
    String SOURCES_DLVR_AMT2 = "dlvrAmt2";
    String SOURCES_DLVR_AMT3 = "dlvrAmt3";
    String SOURCES_PACKAGE_TIME1 = "packageTime1";
    String SOURCES_PACKAGE_TIME2 = "packageTime2";
    String SOURCES_PUSH_TIME1 = "pushTime1";
    String SOURCES_PUSH_TIME2 = "pushTime2";
    String SOURCES_SALE_SCOPE = "saleScope";
    String SOURCES_CUS_AMT = "cusAmt";
    String SOURCES_ONLINE = "online";

    String ONLINE_ENV = "online";
    String BETA_ENV = "beta";
//    List<Integer> SOURCE_IDS_PAY = Arrays.asList(1, 2, 3, 10, 11, 12, 13, 20, 21, 22, 50);
//    List<Integer> SOURCE_IDS_EMPTY = Arrays.asList(30, 40);
//
//    List<Integer> SOURCE_IDS_DELIVER_QTY1 = Arrays.asList(1, 3, 20, 21, 22, 30, 40);
//    List<Integer> SOURCE_IDS_DELIVER_QTY2 = Arrays.asList(2, 10, 11, 12, 13, 50);
//
//    List<Integer> SOURCE_IDS_DELIVER_WEIGHT1 = Arrays.asList(1, 3, 20, 21, 22);
//    List<Integer> SOURCE_IDS_DELIVER_WEIGHT2 = Arrays.asList(2, 10, 11, 12, 13, 50);
//    List<Integer> SOURCE_IDS_DELIVER_WEIGHT3 = Arrays.asList(30, 40);
//
//    List<Integer> SOURCE_IDS_DELIVER_AMT1 = Arrays.asList(1, 20);
//    List<Integer> SOURCE_IDS_DELIVER_AMT2 = Arrays.asList(2,  10, 11, 12, 13, 50);
//    List<Integer> SOURCE_IDS_DELIVER_AMT3 = Arrays.asList(3, 21, 22, 30, 40);
//
//    List<Integer> SOURCE_IDS_PACKAGE_TIME1 = Arrays.asList(2, 10, 11, 12, 13, 50, 52);
//    List<Integer> SOURCE_IDS_PACKAGE_TIME2 = Arrays.asList(1, 3, 20, 21, 22, 30, 40);
//
//    List<Integer> SOURCE_SALE_SCOPE = Arrays.asList(2, 10, 11, 12, 13);
}
