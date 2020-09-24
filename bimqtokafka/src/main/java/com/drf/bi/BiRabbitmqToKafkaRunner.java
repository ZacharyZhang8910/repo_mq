package com.drf.bi;

import com.drf.bi.config.AppConfig;
import com.drf.bi.config.BusinessEnum;
import com.drf.bi.config.Constant;
import com.drf.bi.config.Limit;
import com.drf.bi.handler.RabbitmqMessageHandler;
import com.drf.bi.util.SpringContextHolder;
import com.feiniu.mq.client.provider.consumer.handler.IMessageHandler;
import com.feiniu.mq.client.provider.consumer.rabbitmq.RabbitmqConsumerClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jian.zhang
 * <p>
 * DateTime 2019/6/17 11:26
 */
@Slf4j
@Component
public class BiRabbitmqToKafkaRunner implements ApplicationRunner {

    private final AppConfig appConfig;

    @Autowired
    public BiRabbitmqToKafkaRunner(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) {
        String business = applicationArguments.getSourceArgs()[0];
        String topic = applicationArguments.getSourceArgs()[1];
        BusinessEnum businessEnum = BusinessEnum.getBusinessEnum(business);
        appConfig.setBusiness(businessEnum);
        appConfig.setCurrentTopic(topic);
        log.info("Access data source is:{}, business is:{}, version: {}", getConsumeSource(topic), business, appConfig.getVersion());

        RabbitmqConsumerClient rabbitmqConsumerClient = SpringContextHolder.getBean(RabbitmqConsumerClient.class);
        Map<String, IMessageHandler> consumerConfig = new HashMap<>();
        // 根据Topic配置对应消费的队列及消息处理类
        consumerConfig.put(appConfig.getQueue(topic), SpringContextHolder.getBean(RabbitmqMessageHandler.class));
        rabbitmqConsumerClient.setConsumerMonitorMap(consumerConfig);
        rabbitmqConsumerClient.init();
    }

    /**
     * 获取当前topic消费的渠道
     *
     * @param topic topic
     * @return 返回消费的渠道
     */
    private String getConsumeSource(String topic) {
        Limit limit = appConfig.getLimit().get("sourceId");
        if (limit.getTopic().contains(topic)) {
            return StringUtils.join(limit.getValue(), ",");
        } else {
            return "***ALL***";
        }
    }
}
