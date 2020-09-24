package com.drf.bi.convert;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.drf.bi.config.AppConfig;
import com.drf.bi.config.Constant;
import com.drf.bi.util.DateUtils;
import com.drf.bi.util.SpringContextHolder;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.List;

/**
 * 根据不同的渠道赋值pushTime或payTime,并格式化为yyyy-MM-dd HH:mm:ss
 * @author: jian.zhang
 * @date: 2020/04/01
 */
public class PushTimeValueDeserializer implements ObjectDeserializer {

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        AppConfig appConfig = SpringContextHolder.getBean(AppConfig.class);
        List<Integer> pushTime1 = appConfig.getSourceType().get(Constant.SOURCES_PUSH_TIME1);
        List<Integer> pushTime2 = appConfig.getSourceType().get(Constant.SOURCES_PUSH_TIME2);
        JSONObject jsonObject = JSONObject.parseArray(parser.input.toString()).getJSONObject(0);
        int source = jsonObject.getInteger("source");
        if (pushTime1.contains(source)) {
            String pushTime = parser.getLexer().numberString();
            return StringUtils.isBlank(pushTime) || pushTime.equals("0") ? null : (T) DateUtils.stampToDate(pushTime);
        } else if (pushTime2.contains(source)) {
            String payTime = jsonObject.getString("payTime");
            return StringUtils.isBlank(payTime) || payTime.equals("0") ? null : (T) DateUtils.stampToDate(payTime);
        }
        return null;
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}
