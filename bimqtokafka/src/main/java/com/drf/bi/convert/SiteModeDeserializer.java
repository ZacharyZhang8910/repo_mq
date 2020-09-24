package com.drf.bi.convert;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

import java.lang.reflect.Type;

public class SiteModeDeserializer implements ObjectDeserializer {

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        JSONObject jsonObject = JSONObject.parseObject(parser.getLexer().stringVal());
        return (T) (jsonObject !=null ? jsonObject.getInteger("siteMode") : null);
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}
