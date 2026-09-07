package com.sinopec.mmsecurity.common;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 20 位中石化 MDM 设备编码校验注解。
 * 物理主键固定 20 位，禁止自创物理主键。
 */
@Documented
@Constraint(validatedBy = DeviceCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface DeviceCode {

    String message() default "设备编码必须为 20 位中石化 MDM 编码";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
