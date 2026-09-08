package com.sinopec.mmsecurity.common.mask;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记字段在响应序列化阶段需要脱敏。须与 {@link com.fasterxml.jackson.databind.annotation.JsonSerialize}
 * 搭配使用，指定 {@code using = MaskingSerializer.class}。
 *
 * <pre>{@code
 * @JsonSerialize(using = MaskingSerializer.class)
 * @Masked(MaskType.PHONE)
 * private String phone;
 * }</pre>
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Masked {
    MaskType value() default MaskType.NONE;
}
