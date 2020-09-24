package com.drf.bi.limit;

import com.alibaba.fastjson.JSONObject;
import com.drf.bi.config.AppConfig;
import com.drf.bi.config.Limit;
import com.drf.bi.exception.LimitException;
import com.drf.bi.model.MainOrderInfo;
import com.drf.bi.model.SubOrderInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

/**
 * 过滤处理类
 *
 * @Date 2020/3/23 下午9:49
 * @Created by jim
 */
@Slf4j
@Component
@Lazy
public class LimitHandler {

    private final AppConfig appConfig;

    public LimitHandler(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    /**
     * 渠道过滤
     *
     * @param topic topic
     * @param json  消息对象
     * @return true通过，false排除
     */
    public boolean sourceLimitHandle(String topic, JSONObject json) {
        Limit limit = appConfig.getLimit().get("sourceId");
        if (limit != null && limit.isEnabled() && limit.getTopic().contains(topic)) {
            String val = json.getString("source");
            return limit.getValue().contains(val);
        }
        return true;
    }

    /**
     * 鲜月票过滤
     *
     * @param topic         topic
     * @param mainOrderInfo 主订单对象
     */
    public void xypLimitHandle(String topic, MainOrderInfo mainOrderInfo) {
        Limit limit = appConfig.getLimit().get("xyp");
        if (limit != null && limit.isEnabled() && limit.getTopic().contains(topic)) {
            JSONObject extendJson = JSONObject.parseObject(mainOrderInfo.getExtendedJson());
            if (extendJson != null && !extendJson.isEmpty()) {
                if (extendJson.containsKey("gBizExt") &&
                        extendJson.getString("gBizExt").equals(limit.getValue().get(0))) {
                    throw new LimitException("排除鲜月票订单：" + mainOrderInfo);
                }
            }
        }
    }

    /**
     * 商店街过滤
     *
     * @param topic         topic
     * @param subOrderInfos 子订单对象列表
     */
    public void streetLimitHandle(String topic, List<SubOrderInfo> subOrderInfos) {
        Limit limit = appConfig.getLimit().get("street");
        if (limit != null && limit.isEnabled() && limit.getTopic().contains(topic)) {
            Iterator<SubOrderInfo> iterator = subOrderInfos.iterator();
            while (iterator.hasNext()) {
                SubOrderInfo subOrderInfo = iterator.next();
                String platform = subOrderInfo.getPlatform().toString();
                if (platform.equals(limit.getValue().get(0))) {
                    iterator.remove();
                    log.warn("排除商店街子订单：" + subOrderInfo);
                }
            }
        }
    }

    /**
     * for test
     *
     * @param topic         topic
     * @param subOrderInfos 子订单对象列表
     */
    public void testMessage(String topic, List<SubOrderInfo> subOrderInfos) {
        Limit limit = appConfig.getLimit().get("street");
        if (limit != null && limit.equals("1") && limit.getTopic().contains(topic)) {
            for (SubOrderInfo subOrderInfo : subOrderInfos) {
                System.out.println(subOrderInfo.getCusPayAmt());
            }
        }
    }
}