package com.drf.bi.convert;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 门店号字段序列化特殊处理
 * online环境门店号是：DRF1001这种格式，需要截取为1001
 *
 * @author jian.zhang
 * @date 2019/5/22 15:09
 */
public class StoreValueSerializer implements ObjectSerializer {

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        serializer.out.write(object.toString().substring(3));
    }

}
