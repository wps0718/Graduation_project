package com.qingyuan.secondhand.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String msg;

    public BusinessException(String msg) {
        super(msg);
        this.msg = msg;
    }
}
