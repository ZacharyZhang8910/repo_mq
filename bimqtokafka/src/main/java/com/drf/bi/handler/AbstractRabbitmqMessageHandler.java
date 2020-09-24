package com.drf.bi.handler;

import com.alibaba.fastjson.JSONObject;
import com.drf.bi.config.AppConfig;
import com.drf.bi.config.Constant;
import com.drf.bi.exception.LimitException;
import com.drf.bi.limit.LimitHandler;
import com.drf.bi.service.OrderService;
import com.drf.bi.util.SpringContextHolder;
import com.feiniu.kafka.client.ProducerClient;
import com.feiniu.mq.client.common.message.EventMessage;
import com.feiniu.mq.client.common.message.EventMessageProperties;
import com.feiniu.mq.client.provider.consumer.handler.MessageAdapterHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * Rabbitmq消息处理抽象类
 *
 * @author jian.zhang
 * @date 2019/6/17 14:38
 */
@Slf4j
public abstract class AbstractRabbitmqMessageHandler extends MessageAdapterHandler {

    @Autowired
    public AppConfig appConfig;

    @Autowired
    public LimitHandler limitHandler;

    @Autowired
    public OrderService orderService;

    @Autowired
    public ProducerClient producerClient;

    AbstractRabbitmqMessageHandler() {
        log.info("Call AbstractRabbitmqMessageHandler Constructor.");
        this.ackConfirmMode = MessageAdapterHandler.ACK_CONFIRM_MANUAL;
    }

    /**
     * 消息处理
     *
     * @param eventMessage Rabbitmq消息
     * @return 消息处理状态
     */
    @Override
    public boolean handleMessage(EventMessage eventMessage) {
        boolean result = SpringContextHolder.getEnv().equals(Constant.ONLINE_ENV) ?
                MessageAdapterHandler.MESSAGE_SUCCESS : MessageAdapterHandler.MESSAGE_FAIL;
        try {
            if (log.isDebugEnabled()) {
                EventMessageProperties prop = (EventMessageProperties) eventMessage.getMessageProperties();
                log.debug("messageId:{}", prop.getMessageId());
            }
            JSONObject json = new JSONObject();
            String msgBody = new String(eventMessage.getBody());
            switch (appConfig.getBusiness()) {
                case PUSHTIME:
                    json = JSONObject.parseObject(msgBody);
                    json.put("storeId", json.getString("store_id"));
                    json.put("bizOrderId", json.getLongValue("main_bizorder_id"));
                    json.put("tbBizOrderId", json.getLongValue("main_tb_bizorder_id"));
                    json.put("pushTime", json.getLongValue("push_time"));
                    json.put("source", json.getIntValue("source"));
                    break;
                case PAY:
                    json = JSONObject.parseObject(msgBody);
                    break;
                case DLVR:
                    String[] a = msgBody.split("\\|");
                    json.put("storeId", a[0]);
                    json.put("bizOrderId", a[1]);
                    json.put("tbBizOrderId", a[2]);
                    json.put("packageTime", a[3]);
                    json.put("source", a[4]);
                    break;
                default:
                    break;
            }
            if (json == null || json.isEmpty()) {
                log.warn("Mq json body is empty.");
                return result;
            }
            process(json);
        } catch (LimitException ex) {
            log.warn(ex.getMessage());
        } catch (Exception e) {
            result = MessageAdapterHandler.MESSAGE_FAIL;
            log.error("Message handling error", e);
        }
        return result;
    }

    /**
     * 业务处理
     *
     * @param json Rabbitmq消息
     * @throws IOException 异常
     */
    abstract void process(JSONObject json) throws Exception;

}
