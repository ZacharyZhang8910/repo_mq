package com.drf.bi.convert;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.drf.bi.util.DateUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;

/**
 * 反序列化时时间戳字段处理类
 * 将时间戳转为yyyy-MM-dd HH:mm:ss格式字符串
 *
 * @author jian.zhang
 * @date 2019/5/22 15:25
 */
public class TimestampValueDeserializer implements ObjectDeserializer {
    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        String timestamp = parser.getLexer().numberString();
        return StringUtils.isNotBlank(timestamp) && !timestamp.equals("0") ? (T) DateUtils.stampToDate(timestamp) : null;
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}
