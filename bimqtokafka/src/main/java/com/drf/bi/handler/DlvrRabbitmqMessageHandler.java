package com.drf.bi.handler;

import com.alibaba.fastjson.JSONObject;
import com.drf.bi.config.Constant;
import com.drf.bi.util.SpringContextHolder;
import com.feiniu.mq.client.provider.producer.kafka.KafkaProducerClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 出货订单消息处理类
 *
 * @author jian.zhang
 * @date 2019/6/17 14:41
 */
@Deprecated
@Slf4j
@Lazy
@Component
public class DlvrRabbitmqMessageHandler extends AbstractRabbitmqMessageHandler {

    @Override
    void process(JSONObject json) throws Exception {
        log.info("Process dlvr mq msg.{}", json);
        List<Object> dlvrOrderList = orderService.getOrderInfo(json);
        KafkaProducerClient kafkaProducerClient = SpringContextHolder.getBean(KafkaProducerClient.class);
        kafkaProducerClient.sendMessage(appConfig.getTopic().get(Constant.DLVR_TOPIC), dlvrOrderList);
    }

}
