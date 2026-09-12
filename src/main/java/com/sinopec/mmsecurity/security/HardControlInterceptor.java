package com.sinopec.mmsecurity.security;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 硬控路径兜底拦截器（零下行控制红线）。
 *
 * 前端脚手架约定：前端只监不控，services 不得定义硬控写接口，
 * 任何出站请求命中硬控路径由 front-end guardHardControl 拦截。
 * 本拦截器为服务端兜底：凡是命中硬控路径前缀的 PUT/POST/DELETE 请求，
 * 一律拒绝（返回 code=503 HARD_CONTROL_BLOCKED），避免误接下行控制。
 *
 * 硬控路径清单集中维护，新增下行能力时只允许在名单外的路径，
 * 且必须经评审确认不属于下行控制。
 */
@Component
public class HardControlInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        if (!"POST".equalsIgnoreCase(method) && !"PUT".equalsIgnoreCase(method) && !"DELETE".equalsIgnoreCase(method)) {
            return true;
        }
        String uri = request.getRequestURI();
        // 单一真源：硬控清单集中在 HardControlPaths，前端经 /system/hard-control-paths 取同一份
        if (HardControlPaths.matches(uri)) {
            throw new BusinessException(ResultCode.HARD_CONTROL_BLOCKED,
                    "硬控路径 [" + uri + "] 已被服务端兜底拒绝：前端只监不控，禁止下行控制指令");
        }
        return true;
    }
}
