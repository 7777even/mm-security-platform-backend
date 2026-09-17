package com.sinopec.mmsecurity.websocket;

import com.sinopec.mmsecurity.security.JwtUtil;
import com.sinopec.mmsecurity.security.LoginUser;
import com.sinopec.mmsecurity.security.TokenVersionService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 实时 WebSocket 握手鉴权拦截器：把 WS 会话与登录身份绑定，关闭「匿名可收全部变更」的暴露面。
 *
 * <p><b>为何不靠 JwtFilter</b>：浏览器升级 WebSocket 时无法在握手请求上设置
 * {@code Authorization} 头，故 JwtFilter 的白名单仍保留 {@code /ws}（仅跳过其 Authorization
 * 头校验，并非「匿名公开」）；真正的鉴权改由本拦截器在握手阶段完成——从查询参数
 * {@code ?token=<accessToken>} 抽取令牌，复用 {@link JwtUtil} + {@link TokenVersionService}
 * 做与 JwtFilter 完全一致的校验（令牌类型须为 access、未失效），校验通过后将
 * {@link LoginUser} 写入会话属性，供 {@code AlarmWebSocketHandler} 绑定身份。</p>
 *
 * <p>校验失败（缺令牌 / 令牌无效 / 类型非法 / 已失效）返回 {@code false} 并置 401，握手被拒。
 * 受限用户（data_scope≠ALL）后续由 {@code RealtimeBroadcastService} 按 {@code zone_codes} 过滤推送。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeAuthHandshakeInterceptor implements HandshakeInterceptor {

    /** 会话属性键：存放握手阶段解析出的 {@link LoginUser}（供连接建立后绑定身份）。 */
    public static final String LOGIN_USER_KEY = "REALTIME_LOGIN_USER";

    private final JwtUtil jwtUtil;
    private final TokenVersionService tokenVersionService;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {

        String token = extractToken(request);
        if (token == null) {
            log.warn("[ws-auth] 握手缺少 token 查询参数，拒绝连接");
            reject(response);
            return false;
        }

        Claims claims = jwtUtil.parse(token);
        if (claims == null) {
            log.warn("[ws-auth] 握手令牌无效或已过期，拒绝连接");
            reject(response);
            return false;
        }

        String type = claims.get("type", String.class);
        if (!"access".equals(type)) {
            log.warn("[ws-auth] 握手令牌类型非法（须为 access），拒绝连接");
            reject(response);
            return false;
        }

        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            log.warn("[ws-auth] 握手令牌缺少 subject，拒绝连接");
            reject(response);
            return false;
        }

        if (tokenVersionService != null) {
            Object verObj = claims.get("ver");
            int ver = (verObj instanceof Number n) ? n.intValue() : 0;
            if (ver != tokenVersionService.current(subject)) {
                log.warn("[ws-auth] 握手令牌已失效（版本号不符），拒绝连接 subject={}", subject);
                reject(response);
                return false;
            }
        }

        String role = claims.get("role", String.class);
        attributes.put(LOGIN_USER_KEY, new LoginUser(null, subject, role));
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // 无需处理
    }

    private void reject(ServerHttpResponse response) {
        try {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.debug("[ws-auth] 设置 401 状态失败：{}", e.getMessage());
        }
    }

    /** 从握手请求的 {@code ?token=} 查询参数抽取令牌。 */
    private String extractToken(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletReq) {
            HttpServletRequest servletRequest = servletReq.getServletRequest();
            return servletRequest.getParameter("token");
        }
        // 非 Servlet 容器（如测试/内嵌）回退到 URI query 解析
        String query = request.getURI().getQuery();
        if (query == null) {
            return null;
        }
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0 && "token".equals(pair.substring(0, idx))) {
                return URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
