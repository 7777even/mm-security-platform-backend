package com.sinopec.mmsecurity.common;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

/**
 * 20 位 MDM 编码校验实现。
 * 规则：非空、长度严格 20、仅允许大写字母与数字（MDM 编码字符集）。
 */
public class DeviceCodeValidator implements ConstraintValidator<DeviceCode, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        return value.length() == 20 && value.matches("^[A-Z0-9]{20}$");
    }
}
