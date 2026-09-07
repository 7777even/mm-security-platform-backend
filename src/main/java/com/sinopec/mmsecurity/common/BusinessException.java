package com.sinopec.mmsecurity.common;

import lombok.Getter;

/**
 * 业务异常。code 非 0，可自定义错误码，消息透传前端。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this(400, message);
    }
}
