package com.sinopec.mmsecurity.common;

/**
 * 业务错误码枚举。按域分段，便于前端做差异化提示。
 */
public final class ResultCode {

    private ResultCode() {}

    /** 通用错误 1xx */
    public static final int PARAM_INVALID = 100;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;

    /** 鉴权域 2xx */
    public static final int TOKEN_EXPIRED = 201;
    public static final int TOKEN_INVALID = 202;
    public static final int SIGNATURE_INVALID = 203;
    public static final int SIGNATURE_EXPIRED = 204;

    /** 设备域 3xx */
    public static final int DEVICE_CODE_INVALID = 301;
    public static final int DEVICE_NOT_FOUND = 302;

    /** 硬控域 5xx（下行控制被拒绝） */
    public static final int HARD_CONTROL_BLOCKED = 503;
}
