package com.sinopec.mmsecurity.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinopec.mmsecurity.dto.AlarmItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 告警实时推送 WebSocket 端点（/ws/alarm）。
 *
 * 前端脚手架的 realtime/Cesium 告警流对接目标：
 *   - 管理端订阅：接收告警等级、设备编码、推送时间
 *   - 服务端：由 {@link AlarmSimulator} 基于 fac_alarm 增量轮询，推送真实告警（AlarmItem）
 *
 * 会话管理采用 CopyOnWriteArraySet，支持多端并发订阅。
 */
@Slf4j
@Component
public class AlarmWebSocketHandler extends TextWebSocketHandler {

    private static final Set<WebSocketSession> SESSIONS = new CopyOnWriteArraySet<>();
    private final ObjectMapper om = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        SESSIONS.add(session);
        log.info("WS 告警会话建立：{} (当前 {} 个)", session.getId(), SESSIONS.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        SESSIONS.remove(session);
        log.info("WS 告警会话关闭：{} (剩余 {} 个)", session.getId(), SESSIONS.size());
    }

    /** 广播告警消息（AlarmItem）到所有已连接会话，包络为 {type:"alarm", data: item} */
    public void broadcastAlarm(AlarmItem alarmItem) {
        try {
            String json = om.writeValueAsString(java.util.Map.of("type", "alarm", "data", alarmItem));
            TextMessage msg = new TextMessage(json);
            for (WebSocketSession s : SESSIONS) {
                if (s.isOpen()) {
                    try {
                        s.sendMessage(msg);
                    } catch (IOException e) {
                        log.warn("WS 发送失败：{}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("告警广播失败", e);
        }
    }

    public int sessionCount() {
        return SESSIONS.size();
    }
}
