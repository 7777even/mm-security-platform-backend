package com.sinopec.mmsecurity.common.mask;

import java.util.Map;

/**
 * 集中式脱敏策略。所有出口（响应序列化 / 日志 MDC）统一调用本类，禁止业务代码各自写正则。
 *
 * <p>策略为白名单：仅登记的类型被掩，未知类型默认原样返回，避免误掩业务含义字段。</p>
 *
 * <p>核心原则（见 {@code docs/architecture/data-masking.md}）：脱敏只在「出口」做，
 * 数据库存储字段不脱敏（否则无法精确匹配 / 检索）。即「存真、出掩」。</p>
 */
public final class MaskUtil {

    private MaskUtil() {}

    /** 日志 MDC 敏感键 -> 脱敏类型。业务日志写入 MDC 前按此表选择策略。 */
    private static final Map<String, MaskType> LOG_SENSITIVE_KEYS = Map.of(
            "username", MaskType.NAME,
            "realName", MaskType.NAME,
            "name", MaskType.NAME,
            "phone", MaskType.PHONE,
            "mobile", MaskType.PHONE,
            "idCard", MaskType.ID_CARD,
            "deviceCode", MaskType.DEVICE_CODE
    );

    /**
     * 按脱敏类型掩码。
     *
     * @param type  脱敏类型（null / NONE 原样返回）
     * @param value 原始值（null / 空串原样返回）
     */
    public static String mask(MaskType type, String value) {
        if (type == null || type == MaskType.NONE) {
            return value;
        }
        if (value == null || value.isEmpty()) {
            return value;
        }
        return switch (type) {
            case NAME -> maskName(value);
            case PHONE -> maskPhone(value);
            case ID_CARD -> maskIdCard(value);
            case DEVICE_CODE -> maskDeviceCode(value);
            default -> value;
        };
    }

    /** 姓名：保留首字 + 单个 {@code *}（张三 -> 张*；欧阳修 -> 欧*）。 */
    public static String maskName(String name) {
        if (name == null || name.length() <= 1) {
            return name;
        }
        return name.charAt(0) + "*";
    }

    /** 手机号：保留前 3 后 4，中间 ****（13812345678 -> 138****5678）。 */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /** 身份证：保留前 3 后 4，中间 *（110101199001011234 -> 110***********1234，18 位中段 11 个 *）。 */
    public static String maskIdCard(String id) {
        if (id == null || id.length() < 8) {
            return id;
        }
        int head = 3;
        int tail = 4;
        if (id.length() <= head + tail) {
            return id;
        }
        return id.substring(0, head) + "*".repeat(id.length() - head - tail) + id.substring(id.length() - tail);
    }

    /** 设备编码：保留前缀（如 MDM-），其余以 4 个 {@code *} 替换（MDM-2024010100001234 -> MDM-****）。 */
    public static String maskDeviceCode(String code) {
        if (code == null || code.isEmpty()) {
            return code;
        }
        int idx = code.indexOf('-');
        String prefix = (idx > 0) ? code.substring(0, idx + 1) : "";
        return prefix + "****";
    }

    /**
     * 日志场景脱敏：按 MDC key 选择脱敏类型；未知 key 原样返回。
     * 用法：{@code log.info("user={}", MaskUtil.maskForLog("username", user.getUsername()));}
     */
    public static String maskForLog(String key, String value) {
        if (key == null || value == null) {
            return value;
        }
        MaskType t = LOG_SENSITIVE_KEYS.get(key);
        return t == null ? value : mask(t, value);
    }
}
