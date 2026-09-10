package com.sinopec.mmsecurity.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 控制器方法/类级鉴权注解。
 * RequireAuthInterceptor 检测到此注解时强制要求登录态。
 *
 * <p>三级门禁（按顺序判定，全部满足才放行）：</p>
 * <ol>
 *   <li>{@link #value()} = false → 完全跳过（等价无注解）；</li>
 *   <li>{@link #role()} 非空 → 要求当前角色名匹配（忽略大小写）；</li>
 *   <li>{@link #perm()} 非空 → 要求当前角色持有该权限码（经
 *       {@link RoleAuthorityService} 按 sys_role_menu 解析，写时失效、即时生效）。</li>
 * </ol>
 *
 * <p>role 与 perm 可同时标注（AND 关系）；系统管理域初期以 role="ADMIN" 粗粒度门禁，
 * 权限码同批登记后逐步切换为 perm（见 design ADR-5）。</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAuth {

    boolean value() default true;

    String role() default "";

    /**
     * 要求的权限码（如 {@code system:user:create}）。
     * 空串表示不校验权限码。
     */
    String perm() default "";
}
