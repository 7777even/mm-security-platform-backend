package com.sinopec.mmsecurity.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 控制器方法/类级鉴权注解。
 * RequireAuthInterceptor 检测到此注解时强制要求登录态。
 * 也可配 role = "ADMIN" 强制要求管理员身份。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAuth {

    boolean value() default true;

    String role() default "";
}
