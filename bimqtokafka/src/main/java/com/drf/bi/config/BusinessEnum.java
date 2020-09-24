package com.drf.bi.config;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author jian.zhang
 * <p>
 * DateTime 2019/7/3 9:57
 */
public enum BusinessEnum {
    NONE("none"),
    PAY("pay"),
    DLVR("dlvr"),
    PUSHTIME("pushtime");

    String name;

    BusinessEnum(String name) {
        this.name = name;
    }

    public static BusinessEnum getBusinessEnum(String business) {
        return Arrays.stream(values()).filter(k -> k.name.equals(business)).findFirst().orElse(NONE);
    }

    public static String getSupportTypeName() {
        return Arrays.stream(values()).filter(k -> k != NONE).map(Objects::toString).collect(Collectors.joining("|"));
    }
}
