package com.drf.bi.exception;

/**
 * 黑名单异常
 *
 * @Date 2020/3/3 下午1:54
 * @Created by jim
 */
public class LimitException extends RuntimeException {

    public LimitException(String message) {
        super(message);
    }

}