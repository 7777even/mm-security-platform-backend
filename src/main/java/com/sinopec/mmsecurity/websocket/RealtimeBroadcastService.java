package com.sinopec.mmsecurity.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 通用实时广播器：持有 WebSocket 会话集合，向所有已连接会话下发 {topic, payload} 包络。
 * 复用既有 {@code /ws/alarm} 端点（由 AlarmWebSocketHandler 委托），alarm.push 与
 * {@code <domain>.changed} 同连接承载。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeBroadcastService {

    private final ObjectMapper om;
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    public void addSession(WebSocketSession session) {
        sessions.add(session);
    }

    public void removeSession(WebSocketSession session) {
        sessions.remove(session);
    }

    public int sessionCount() {
        return sessions.size();
    }

    /**
     * 向所有已连接会话广播 {topic, payload}（payload 可为任意可序列化对象，如 AlarmItem 或 Map）。
     */
    public void broadcast(String topic, Object payload) {
        try {
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("topic", topic);
            envelope.put("payload", payload);
            String json = om.writeValueAsString(envelope);
            TextMessage msg = new TextMessage(json);
            for (WebSocketSession s : sessions) {
                if (s.isOpen()) {
                    try {
                        s.sendMessage(msg);
                    } catch (IOException e) {
                        log.warn("WS 发送失败：{}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("实时广播失败 topic={}", topic, e);
        }
    }
}
