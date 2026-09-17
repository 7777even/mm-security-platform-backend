package com.sinopec.mmsecurity.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sinopec.mmsecurity.dto.AlarmItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 通用广播器包络契约测试（纯 Mockito，不起 Spring 上下文）。
 * 守护点：后端下发包络必须与前端 ws.ts 解析器一致（含 topic 字段）。
 * 一旦失配，表现是「WS 连得上但数据永不刷新」，无任何报错——最难排查的一类缺陷，故用测试锁死。
 */
class RealtimeBroadcastServiceTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private RealtimeBroadcastService service;
    private WebSocketSession session;

    @BeforeEach
    void setUp() {
        service = new RealtimeBroadcastService(mapper);
        session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        when(session.getId()).thenReturn("test-session");
        service.addSession(session);
    }

    @Test
    @DisplayName("alarm.push 包络必须对齐契约 {topic:'alarm.push', payload:AlarmItem}")
    void alarmEnvelopeMatchesContract() throws Exception {
        AlarmItem item = new AlarmItem();
        item.setAlarmId("AE-2026-011");
        item.setLevel(1);
        item.setType("FIRE");
        item.setStatus("ACTIVE");
        item.setDeviceCode("DT-B-3008");
        item.setTs(LocalDateTime.of(2026, 9, 4, 8, 5, 0));

        service.broadcast("alarm.push", item);

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(captor.capture());

        var node = mapper.readTree(captor.getValue().getPayload());
        assertEquals("alarm.push", node.get("topic").asText(),
                "topic 必须为 alarm.push，否则前端 ws.ts 因缺 topic 字段静默丢弃该消息");
        assertTrue(node.has("payload"), "告警实体必须挂在 payload 下");

        var p = node.get("payload");
        assertEquals("AE-2026-011", p.get("alarmId").asText());
        assertEquals(1, p.get("level").asInt(), "level 必须是数字，前端 isAlarmItem 强校验 typeof level === 'number'");
        assertEquals("DT-B-3008", p.get("deviceCode").asText());
        assertTrue(p.get("ts").asText().startsWith("2026-09-04T08:05"),
                "ts 应序列化为 ISO-8601 字符串（依赖 JavaTimeModule，缺失会导致广播抛异常）");
    }

    @Test
    @DisplayName("<domain>.changed 包络 {topic:'device.changed', payload:{domain,action,id,data}}")
    void dataChangedEnvelope() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("domain", "device");
        payload.put("action", "updated");
        payload.put("id", null);
        payload.put("data", null);

        service.broadcast("device.changed", payload);

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(captor.capture());

        var node = mapper.readTree(captor.getValue().getPayload());
        assertEquals("device.changed", node.get("topic").asText());
        var p = node.get("payload");
        assertEquals("device", p.get("domain").asText());
        assertEquals("updated", p.get("action").asText());
    }
}
