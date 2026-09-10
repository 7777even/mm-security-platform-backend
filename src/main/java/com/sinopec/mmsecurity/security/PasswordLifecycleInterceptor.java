package com.sinopec.mmsecurity.security;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.common.cache.PasswordStateCache;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * 口令生命周期拦截器（强制首登改密的服务端兜底）。
 *
 * <p>背景：默认管理员账号与「管理员重置密码」都会置 {@code must_change_pwd=1}。
 * 前端会引导跳改密页，但**不能只靠前端**——否则用户直接用令牌调业务写接口即可绕过。
 * 本拦截器对**变更类请求**（非 GET/HEAD/OPTIONS）做兜底拒绝，形成「前端拦截 + 后端拒绝」双保险。</p>
 *
 * <p>豁免（否则会把改密路径本身也堵死）：</p>
 * <ul>
 *   <li>{@code /api/v1/auth/**} 全部（login/refresh/logout/me/password/profile）；</li>
 *   <li>{@code /api/v1/uplink/audit}（审计旁路上报，安全无害且登录后即触发）。</li>
 * </ul>
 *
 * <p>未登录请求不在此处理（由 {@link RequireAuthInterceptor} / {@code JwtFilter} 负责），
 * 本拦截器只在「已登录且须改密」时拒绝。</p>
 */
@Component
@RequiredArgsConstructor
public class PasswordLifecycleInterceptor implements HandlerInterceptor {

    /** 变更类请求才检查（读接口放行，避免只读页面在改密前完全不可用） */
    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private static final String AUTH_PREFIX = "/api/v1/auth/";
    private static final String AUDIT_UPLINK = "/api/v1/uplink/audit";

    private final PasswordStateCache passwordStateCache;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        if (method == null || !MUTATING_METHODS.contains(method.toUpperCase())) {
            return true;
        }
        String uri = request.getRequestURI();
        if (uri == null || uri.startsWith(AUTH_PREFIX) || uri.equals(AUDIT_UPLINK)) {
            return true;
        }
        LoginUser user = UserContext.get();
        if (user == null) {
            return true;
        }
        if (passwordStateCache.mustChange(user.getUsername())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "请先修改初始密码后再进行该操作");
        }
        return true;
    }
}
