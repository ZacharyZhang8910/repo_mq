package com.drf.bi.convert;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.google.common.collect.Lists;

import java.lang.reflect.Type;
import java.math.BigDecimal;

/**
 * 计算销售范围（0：原区，1：扩区）
 */
public class SaleScopeDeserializer implements ObjectDeserializer {

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        BigDecimal distance = parser.getLexer().decimalValue();
        JSONObject jsonObject = JSONObject.parseArray(parser.input.toString()).getJSONObject(0);
        int source = jsonObject.getInteger("source");
        String saleScope = "0";
        if (Lists.newArrayList(2, 10, 11).contains(source)) {
            // 淘系根据距离计算
            saleScope = distance.doubleValue() > 5000 ? "1" : "0";
        } else if (source == 50) {
            // 优鲜直接获取
            saleScope = distance.intValue() + "";
        }
        return (T) saleScope;
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}
