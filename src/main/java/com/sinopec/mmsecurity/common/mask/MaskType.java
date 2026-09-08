package com.sinopec.mmsecurity.common.mask;

/**
 * 脱敏类型白名单。仅登记的类型被掩，未知字段默认原样（避免误掩业务含义字段）。
 */
public enum MaskType {
    /** 姓名：张* */
    NAME,
    /** 手机号：138****8000 */
    PHONE,
    /** 身份证：110***********1234 */
    ID_CARD,
    /** 设备编码：MDM-****（功能键，仅日志侧脱敏，响应不掩） */
    DEVICE_CODE,
    /** 不脱敏，原样返回 */
    NONE
}
