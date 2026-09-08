package com.sinopec.mmsecurity.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinopec.mmsecurity.dto.AlarmItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
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
@RequiredArgsConstructor
public class AlarmWebSocketHandler extends TextWebSocketHandler {

    private static final Set<WebSocketSession> SESSIONS = new CopyOnWriteArraySet<>();

    /**
     * 注入 Spring 管理的 ObjectMapper（已注册 JavaTimeModule 且关闭时间戳输出）：
     * AlarmItem.ts 为 LocalDateTime，自建 ObjectMapper 缺 JavaTimeModule 会抛
     * InvalidDefinitionException，导致告警广播静默失败（仅留一条 error 日志）。
     */
    private final ObjectMapper om;

    /**
     * 契约 topic：frontend-scaffold/docs/api/realtime.openapi.json 约定服务端下发
     * {topic:"alarm.push", payload: AlarmItem}；前端 ws.ts 仅解析含 topic 字段的消息。
     */
    public static final String TOPIC_ALARM_PUSH = "alarm.push";

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

    /**
     * 客户端上行消息：契约约定客户端每 15s 发送心跳 {type:'ping', ts}（realtime.openapi.json）。
     * 心跳不承载业务，服务端显式吞掉（不回包、不报错）；其余上行消息一律忽略，避免连接被误判异常。
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
     * {topic:"alarm.push", payload: AlarmItem}（原 {type,data} 会被前端 ws.ts 静默丢弃）。
     */
    public void broadcastAlarm(AlarmItem alarmItem) {
        try {
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("topic", TOPIC_ALARM_PUSH);
            envelope.put("payload", alarmItem);
            String json = om.writeValueAsString(envelope);
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
