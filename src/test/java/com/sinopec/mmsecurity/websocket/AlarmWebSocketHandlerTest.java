package com.sinopec.mmsecurity.websocket;

import com.sinopec.mmsecurity.dto.AlarmItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AlarmWebSocketHandler 委托测试（纯 Mockito）：验证会话管理与广播委托给 RealtimeBroadcastService；
 * 包络契约由 RealtimeBroadcastServiceTest 守护。
 */
class AlarmWebSocketHandlerTest {

    private RealtimeBroadcastService broadcastService;
    private AlarmWebSocketHandler handler;
    private WebSocketSession session;

    @BeforeEach
    void setUp() {
        broadcastService = mock(RealtimeBroadcastService.class);
        handler = new AlarmWebSocketHandler(broadcastService);
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
    @DisplayName("连接建立/关闭委托给 RealtimeBroadcastService 管理会话")
    void connectionDelegatesToBroadcastService() {
        verify(broadcastService).addSession(session);
        handler.afterConnectionClosed(session, CloseStatus.NORMAL);
        verify(broadcastService).removeSession(session);
    }

    @Test
    @DisplayName("broadcastAlarm 委托广播 alarm.push")
    void broadcastAlarmDelegates() {
        AlarmItem item = new AlarmItem();
        item.setAlarmId("AE-2026-011");
        handler.broadcastAlarm(item);
        verify(broadcastService).broadcast(AlarmWebSocketHandler.TOPIC_ALARM_PUSH, item);
    }

    @Test
    @DisplayName("客户端心跳 {type:'ping'} 不得抛异常（契约 client→server ping）")
    void handlePing_tolerated() {
        assertDoesNotThrow(() ->
                handler.handleTextMessage(session, new TextMessage("{\"type\":\"ping\",\"ts\":1717488000000}")));
    }
}
