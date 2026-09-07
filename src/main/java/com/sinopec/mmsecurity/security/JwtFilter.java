package com.sinopec.mmsecurity.security;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.common.TraceContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 鉴权过滤器：解析 Authorization: Bearer <token>，解出登录态写入 UserContext。
 * 鉴权失败抛 BusinessException(code=401)，由 GlobalExceptionHandler 收敛为 B3 包络。
 *
 * 令牌内存态：服务端不持久存 token（无状态），前端脚手架要求前端走 HttpOnly Cookie / 内存，
 * 本过滤器只校验签名有效性 + 过期，不在服务端落成 localStorage 明文。
 *
 * 注册：本类不再使用 @Component 自动注册（顺序不可控），改由
 * {@code SecurityBeans#jwtFilterRegistration} 通过 FilterRegistrationBean 显式装配，
 * 顺序固定为 HIGHEST_PRECEDENCE + 10（在 HmacFilter 之后、拦截器链之前）。
 */
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 浏览器预检(OPTIONS)不带 Authorization 头。若在此阶段就做鉴权，会被 401 拦截，
     * 而 CORS 响应头由 DispatcherServlet 内的 CORS 拦截器添加——预检在到达它之前就被拒，
     * 浏览器会报「No 'Access-Control-Allow-Origin' header」。故 OPTIONS 必须直接放行，
     * 让其直达 CORS 拦截器完成预检握手。
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    /** 免鉴权路径白名单 */
    private static final String[] WHITELIST = {
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/health",
            "/actuator",
            "/h2-console",
            "/ws",
            "/error"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        TraceContext.init();
        String uri = request.getRequestURI();

        try {
            if (isWhite(uri)) {
                chain.doFilter(request, response);
                return;
            }

            String header = request.getHeader(AUTH_HEADER);
            if (header == null || !header.startsWith(TOKEN_PREFIX)) {
                throw new BusinessException(ResultCode.TOKEN_INVALID, "缺少 Authorization 头");
            }

            String token = header.substring(TOKEN_PREFIX.length());
            Claims claims = jwtUtil.parse(token);
            if (claims == null) {
                throw new BusinessException(ResultCode.TOKEN_INVALID, "令牌无效或已过期");
            }

            String type = claims.get("type", String.class);
            if (!"access".equals(type)) {
                throw new BusinessException(ResultCode.TOKEN_INVALID, "令牌类型非法");
            }

            String role = claims.get("role", String.class);
            UserContext.set(new LoginUser(null, claims.getSubject(), role));
            chain.doFilter(request, response);
        } finally {
            UserContext.clear();
            TraceContext.clear();
        }
    }

    private boolean isWhite(String uri) {
        for (String p : WHITELIST) {
            if (uri.startsWith(p)) return true;
        }
        return false;
    }
}
