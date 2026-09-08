package com.sinopec.mmsecurity.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sinopec.mmsecurity.dto.AlarmItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * WS 告警推送包络契约测试（纯 Mockito，不起 Spring 上下文）。
 *
 * 守护点：后端下发包络必须与前端 ws.ts 解析器一致（含 topic 字段）。
 * 一旦失配，表现是「WS 连得上但告警永不刷新」，无任何报错——最难排查的一类缺陷，故用测试锁死。
 */
class AlarmWebSocketHandlerTest {

    /** 模拟 Spring 容器的 ObjectMapper（注册 JavaTimeModule + ISO-8601 输出） */
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private AlarmWebSocketHandler handler;
    private WebSocketSession session;

    @BeforeEach
    void setUp() {
        handler = new AlarmWebSocketHandler(mapper);
        session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        when(session.getId()).thenReturn("test-session");
        handler.afterConnectionEstablished(session);
    }

    @AfterEach
    void tearDown() {
        handler.afterConnectionClosed(session, CloseStatus.NORMAL);
    }

    @Test
    @DisplayName("广播包络必须对齐契约 {topic:'alarm.push', payload:AlarmItem}")
    void broadcast_envelopeMatchesContract() throws Exception {
        AlarmItem item = new AlarmItem();
        item.setAlarmId("AE-2026-011");
        item.setLevel(1);
        item.setType("FIRE");
        item.setStatus("ACTIVE");
        item.setDeviceCode("DT-B-3008");
        item.setTs(LocalDateTime.of(2026, 9, 4, 8, 5, 0));

        handler.broadcastAlarm(item);

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(captor.capture());

        JsonNode node = mapper.readTree(captor.getValue().getPayload());
        assertEquals("alarm.push", node.get("topic").asText(),
                "topic 必须为 alarm.push，否则前端 ws.ts 因缺 topic 字段静默丢弃该消息");
        assertTrue(node.has("payload"), "告警实体必须挂在 payload 下");

        JsonNode p = node.get("payload");
        assertEquals("AE-2026-011", p.get("alarmId").asText());
        assertEquals(1, p.get("level").asInt(), "level 必须是数字，前端 isAlarmItem 强校验 typeof level === 'number'");
        assertEquals("DT-B-3008", p.get("deviceCode").asText());
        assertTrue(p.get("ts").asText().startsWith("2026-09-04T08:05"),
                "ts 应序列化为 ISO-8601 字符串（依赖 JavaTimeModule，缺失会导致广播抛异常）");
    }

    @Test
    @DisplayName("客户端心跳 {type:'ping'} 不得抛异常（契约 client→server ping）")
    void handlePing_tolerated() {
        assertDoesNotThrow(() ->
                handler.handleTextMessage(session, new TextMessage("{\"type\":\"ping\",\"ts\":1717488000000}")));
    }
}
