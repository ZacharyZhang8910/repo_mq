package com.drf.bi.convert;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.drf.bi.App;
import com.drf.bi.config.AppConfig;
import com.drf.bi.config.Constant;
import com.drf.bi.util.DateUtils;
import com.drf.bi.util.SpringContextHolder;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.List;

/**
 * 打包时间时间戳反序列化转换类，将时间戳转换为yyyy-MM-dd HH:mm:ss格式
 * @author: ziyi.zhang
 * @date: 07/2019
 */
public class PackageTimeValueDeserializer implements ObjectDeserializer {

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        AppConfig appConfig = SpringContextHolder.getBean(AppConfig.class);
        List<Integer> packageTime1 = appConfig.getSourceType().get(Constant.SOURCES_PACKAGE_TIME1);
        List<Integer> packageTime2 = appConfig.getSourceType().get(Constant.SOURCES_PACKAGE_TIME2);
        JSONObject jsonObject = JSONObject.parseArray(parser.input.toString()).getJSONObject(0);
        int source = jsonObject.getInteger("source");
        if (packageTime1.contains(source)) {
            // 取打包时间
            String packageTime = parser.getLexer().numberString();
            return StringUtils.isBlank(packageTime) || packageTime.equals("0") ? null : (T) DateUtils.stampToDate(packageTime);
        } else if (packageTime2.contains(source)) {
            // 取支付时间
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
