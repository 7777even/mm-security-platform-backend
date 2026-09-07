package com.sinopec.mmsecurity.security;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 鉴权拦截器：检测 @RequireAuth。
 * 类级注解作为默认值，方法级注解覆盖类级。
 */
@Component
public class RequireAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        RequireAuth methodAnn = hm.getMethodAnnotation(RequireAuth.class);
        RequireAuth classAnn = hm.getBeanType().getAnnotation(RequireAuth.class);
        RequireAuth ann = methodAnn != null ? methodAnn : classAnn;
        if (ann == null || !ann.value()) {
            return true;
        }

        LoginUser user = UserContext.get();
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录或令牌过期");
        }
        if (!ann.role().isEmpty() && !ann.role().equalsIgnoreCase(user.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "权限不足，需要角色：" + ann.role());
        }
        return true;
    }
}
