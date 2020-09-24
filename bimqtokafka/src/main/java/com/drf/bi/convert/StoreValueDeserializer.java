package com.drf.bi.convert;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.drf.bi.util.SpringContextHolder;

import java.lang.reflect.Type;

public class StoreValueDeserializer implements ObjectDeserializer {
    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        String value = parser.getLexer().stringVal();
        return (T) ("beta".equals(SpringContextHolder.getEnv()) ? "DRF" + value.substring(6) : value);
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}
