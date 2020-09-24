package com.drf.bi.config;

import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.feiniu.kafka.client.ProducerClient;
import com.feiniu.mq.client.common.KafkaConfig;
import com.feiniu.mq.client.common.RabbitMqConfig;
import com.feiniu.mq.client.provider.consumer.rabbitmq.RabbitmqConsumerClient;
import com.google.common.collect.Lists;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 配置信息类
 *
 * @author jian.zhang
 * @date 2019/6/17 11:31
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "bi")
public class AppConfig {

    private String version;

    private String currentTopic;

    private BusinessEnum business;

    private Map<String, String> kafka;

    @Deprecated
    private Map<String, String> topic;

    private Map<String, String> rabbitmq;

    private Map<String, Map<String, String>> api;

    private Map<String, List<Integer>> sourceType;

    private Map<String, Limit> limit;


    public String getQueue(String topic) {
        return rabbitmq.get("queue." + topic + ".queueName");
    }

    @Bean
    @ConditionalOnMissingBean
    public ProducerClient getKafkaProducerClient() {
        Properties properties = new Properties();
        properties.put(KafkaConfig.KAFKA_METADATA_BROKER_LIST, kafka.get(Constant.KAFKA_BROKER_LIST));
        properties.put(KafkaConfig.KAFKA_ZK_LIST, kafka.get(Constant.KAFKA_ZK_LIST));
        properties.put(KafkaConfig.KAFKA_SERIALIZER_CLASS, kafka.get(Constant.KAFKA_SERIALIZER));
        properties.put(KafkaConfig.KAFKA_PRODUCER_TYPE, kafka.get(Constant.KAFKA_PRODUCER_TYPE));
        properties.put(KafkaConfig.KAFKA_REQUEST_REQUIRED_ACKS, kafka.get(Constant.KAFKA_ACKS));
        //KafkaConfig kafkaConfig = new KafkaConfig();
        //kafkaConfig.setProps(properties);
        //KafkaProducerClient kafkaProducerClient = new KafkaProducerClient(kafkaConfig);
        //kafkaProducerClient.init();
        ProducerClient kafkaProducerClient = new ProducerClient(properties);
        return kafkaProducerClient;
    }

    @Bean
    @ConditionalOnMissingBean
    public RabbitMqConfig getRabbitMqConfig() {
        RabbitMqConfig rabbitMqConfig = new RabbitMqConfig();
        rabbitMqConfig.setVirtualHost(rabbitmq.get(Constant.RABBITMQ_COMMON_VIRTUAL_HOST));
        rabbitMqConfig.setHostList(rabbitmq.get(Constant.RABBITMQ_COMMON_HOSTS));
        rabbitMqConfig.setUsername(rabbitmq.get(Constant.RABBITMQ_COMMON_USERNAME));
        rabbitMqConfig.setPassword(rabbitmq.get(Constant.RABBITMQ_COMMON_PASSWORD));
        return rabbitMqConfig;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RabbitMqConfig.class)
    public RabbitmqConsumerClient getRabbitmqConsumerClient(RabbitMqConfig rabbitMqConfig) {
        RabbitmqConsumerClient rabbitmqConsumerClient = new RabbitmqConsumerClient();
        rabbitmqConsumerClient.setBaseConfig(rabbitMqConfig);
        return rabbitmqConsumerClient;
    }

    @Bean
    @ConditionalOnMissingBean
    public SerializeConfig getSerializeConfig() {
        SerializeConfig serializeConfig = new SerializeConfig();
        // 序列化时将将驼峰命名转为下划线命名
        serializeConfig.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        //serializeConfig.setPropertyNamingStrategy(PropertyNamingStrategy.SnakeCase);
        return serializeConfig;
    }

    @Bean
    @ConditionalOnMissingBean
    public SerializerFeature[] getSerializerFeature() {
        // 序列化时将值字段NULL值转为0，字符串字段NULL值转为空字符串
        List<SerializerFeature> featureList = Lists.newArrayList(SerializerFeature.WriteNullNumberAsZero,
                SerializerFeature.WriteNullStringAsEmpty);
        SerializerFeature[] features = new SerializerFeature[featureList.size()];
        return featureList.toArray(features);
    }

}
