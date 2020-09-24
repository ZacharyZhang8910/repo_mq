package com.drf.bi.handler;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 订单消息处理类
 *
 * @Date 2020/3/23 下午10:34
 * @Created by jim
 */
@Slf4j
@Component
public class RabbitmqMessageHandler extends AbstractRabbitmqMessageHandler {

    @Override
    void process(JSONObject json) {
        String currentTopic = appConfig.getCurrentTopic();
        if (limitHandler.sourceLimitHandle(currentTopic, json)) {
            log.info("Process " + appConfig.getBusiness().name() + " mq msg to {}. Json:{}", currentTopic, json);
            List<Object> orderList = orderService.getOrderInfo(json);
            if (orderList != null && orderList.size() > 0) {
                producerClient.sendMessage(currentTopic, orderList);
            }
        }
    }

}