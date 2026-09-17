package com.sinopec.mmsecurity.websocket;

import com.sinopec.mmsecurity.dto.AlarmItem;
import com.sinopec.mmsecurity.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

/**
 * 实时推送 WebSocket 端点（/ws/alarm）。
 *
 * 复用既有端点承载多 topic：既广播 alarm.push（告警增量，由 AlarmSimulator 驱动），
 * 也承载 {@code <domain>.changed}（数据变更刷新通知，由 RealtimeBroadcastService 统一下发）。
 * 会话管理与通用广播委托 RealtimeBroadcastService，保持单一 WS 连接。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmWebSocketHandler extends TextWebSocketHandler {

    private final RealtimeBroadcastService broadcastService;

    /**
     * 契约 topic：frontend-scaffold/docs/api/realtime.openapi.json 约定服务端下发
     * {topic:"alarm.push", payload: AlarmItem}。
     */
    public static final String TOPIC_ALARM_PUSH = "alarm.push";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 身份须在握手阶段由 RealtimeAuthHandshakeInterceptor 注入；缺失表示鉴权缺口，强制关闭。
        Object attr = session.getAttributes().get(RealtimeAuthHandshakeInterceptor.LOGIN_USER_KEY);
        if (!(attr instanceof LoginUser loginUser)) {
            log.warn("WS 会话缺少身份（握手拦截器未注入），强制关闭 session={}", session.getId());
            try {
                session.close(CloseStatus.NOT_ACCEPTABLE);
            } catch (IOException ignored) {
                // 关闭失败也无妨，会话本就被拒绝
            }
            return;
        }
        broadcastService.addSession(session, loginUser);
        log.info("WS 会话建立：{} user={} (当前 {} 个)", session.getId(), loginUser.getUsername(), broadcastService.sessionCount());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        broadcastService.removeSession(session);
        log.info("WS 会话关闭：{} (剩余 {} 个)", session.getId(), broadcastService.sessionCount());
    }

    /**
     * 客户端上行消息：契约约定客户端每 15s 发送心跳 {type:'ping', ts}（realtime.openapi.json）。
     * 心跳不承载业务，服务端显式吞掉（不回包、不报错）；其余上行消息一律忽略。
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String body = message.getPayload();
        if (body != null && body.contains("\"ping\"")) {
            log.debug("WS 心跳保活：session={}", session.getId());
            return;
        }
        log.debug("WS 收到非业务上行消息，已忽略：session={}", session.getId());
    }

    /**
     * 广播告警到所有已连接会话，包络严格对齐前端契约：
     * {topic:"alarm.push", payload: AlarmItem}（委托 RealtimeBroadcastService 统一下发）。
     */
    public void broadcastAlarm(AlarmItem alarmItem) {
        broadcastService.broadcast(TOPIC_ALARM_PUSH, alarmItem);
    }

    public int sessionCount() {
        return broadcastService.sessionCount();
    }
}
