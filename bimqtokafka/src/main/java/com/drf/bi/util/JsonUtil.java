package com.drf.bi.util;

import com.alibaba.fastjson.JSONObject;

/**
 * Json工具类
 *
 * @author jian.zhang
 * @date 2019/5/21 10:10
 */
public class JsonUtil {

    public static <T> T getValue(JSONObject jsonObject, String key, T defaultValue) {
        if (jsonObject == null) return defaultValue;

        String type = defaultValue.getClass().getSimpleName();
        T value;
        switch (type) {
            case "String":
                value = (T) jsonObject.getString(key);
                break;
            case "Double":
                value = (T) jsonObject.getDouble(key);
                break;
            case "Integer":
                value = (T) jsonObject.getInteger(key);
                break;
            default:
                value = (T) jsonObject.get(key);
                break;
        }
        return value == null ? defaultValue : value;
    }

}
