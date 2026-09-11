package com.sinopec.mmsecurity.security;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.common.TraceContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 鉴权过滤器：解析 Authorization: Bearer <token>，解出登录态写入 UserContext。
 * 鉴权失败【直接写出 HTTP 401 + B3 包络】，不再抛异常冒泡给 Tomcat（否则变成 500 且无 CORS 头）。
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
    private final ObjectMapper objectMapper;
    /** 令牌失效版本号服务；为 null 时跳过版本校验（仅测试构造走这条，生产由 SecurityBeans 注入）。 */
    private final TokenVersionService tokenVersionService;

    public JwtFilter(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this(jwtUtil, objectMapper, null);
    }

    public JwtFilter(JwtUtil jwtUtil, ObjectMapper objectMapper, TokenVersionService tokenVersionService) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.tokenVersionService = tokenVersionService;
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

    /**
     * 免鉴权路径白名单：仅登录/刷新/登出三类端点（拿令牌/换发令牌/清 Cookie 必须免鉴权）。
     * 注意：/auth/me 与 /auth/menus 已【移出】白名单，必须携带有效 access 令牌（Bearer）才能访问——
     * 二者依赖 UserContext 中的当前登录态（me 取真实身份、menus 按角色做 RBAC），故不能免鉴权。
     */
    private static final String[] WHITELIST = {
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
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
                reject(ResultCode.TOKEN_INVALID, "缺少 Authorization 头", response);
                return;
            }

            String token = header.substring(TOKEN_PREFIX.length());
            Claims claims = jwtUtil.parse(token);
            if (claims == null) {
                reject(ResultCode.TOKEN_INVALID, "令牌无效或已过期", response);
                return;
            }

            String type = claims.get("type", String.class);
            if (!"access".equals(type)) {
                reject(ResultCode.TOKEN_INVALID, "令牌类型非法", response);
                return;
            }

            // 令牌失效版本号（V45）：登出 / 改密 / 强制下线后版本号递增，
            // 此前签发的令牌因版本落后在此被拒，而不是继续用到自然过期。
            // tokenVersionService 为 null（测试构造）时跳过，保持原有行为。
            if (tokenVersionService != null) {
                Object verObj = claims.get("ver");
                int ver = (verObj instanceof Number n) ? n.intValue() : 0;
                if (ver != tokenVersionService.current(claims.getSubject())) {
                    reject(ResultCode.TOKEN_INVALID, "令牌已失效，请重新登录", response);
                    return;
                }
            }

            String role = claims.get("role", String.class);
            UserContext.set(new LoginUser(null, claims.getSubject(), role));
            chain.doFilter(request, response);
        } finally {
            UserContext.clear();
            TraceContext.clear();
        }
    }

    /**
     * 鉴权失败：直接写出 HTTP 401 + B3 包络（UTF-8 JSON），不抛异常。
     * 这样响应（已被 CorsFilter 加上 CORS 头）对浏览器可读，前端 http.ts 能按 code 处理。
     */
    private void reject(int code, String msg, HttpServletResponse response) throws IOException {
        HttpStatus status = (code == ResultCode.UNAUTHORIZED) ? HttpStatus.UNAUTHORIZED
                : (code == ResultCode.FORBIDDEN) ? HttpStatus.FORBIDDEN
                : HttpStatus.UNAUTHORIZED;
        log.warn("[{}] JWT 鉴权失败 code={} msg={}", TraceContext.get(), code, msg);
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(code, msg)));
        response.getWriter().flush();
    }

    private boolean isWhite(String uri) {
        for (String p : WHITELIST) {
            if (uri.startsWith(p)) return true;
        }
        return false;
    }
}
