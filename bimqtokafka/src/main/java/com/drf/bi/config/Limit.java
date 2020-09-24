package com.drf.bi.config;

import lombok.Data;

import java.util.List;

/**
 * 过滤参数对象
 *
 * @Date 2020/3/23 下午2:47
 * @Created by jim
 */
@Data
public class Limit {

    private List<String> topic;

    private List<String> value;

    private boolean enabled;

}