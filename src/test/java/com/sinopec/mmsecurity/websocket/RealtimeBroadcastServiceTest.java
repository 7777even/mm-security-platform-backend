package com.sinopec.mmsecurity.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinopec.mmsecurity.security.DataScopeResolver;
import com.sinopec.mmsecurity.security.LoginUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RealtimeBroadcastService 防区过滤逻辑测试（纯 Mockito）：覆盖三态语义——ALL 用户、
 * 事件未映射（null）、受限用户防区交集；以及关闭会话跳过。
 */
class RealtimeBroadcastServiceTest {

    private DataScopeResolver dataScopeResolver;
    private RealtimeBroadcastService service;

    @BeforeEach
    void setUp() {
        dataScopeResolver = mock(DataScopeResolver.class);
        service = new RealtimeBroadcastService(new ObjectMapper(), dataScopeResolver);
    }

    private WebSocketSession sessionWithZones(LoginUser user, Set<String> zones, boolean open) {
        when(dataScopeResolver.resolveZonesFor(user)).thenReturn(zones);
        WebSocketSession s = mock(WebSocketSession.class);
        when(s.isOpen()).thenReturn(open);
        service.addSession(s, user);
        return s;
    }

    @Test
    @DisplayName("ALL 用户（zones=null）收到任意防区的变更")
    void allUserReceivesAnyZone() throws IOException {
        LoginUser all = new LoginUser(null, "admin", "ADMIN");
        WebSocketSession s = sessionWithZones(all, null, true);

        service.broadcast("x.changed", "payload", Set.of("炼油区"));

        verify(s).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("受限用户仅收到防区交集命中的变更")
    void scopedUserReceivesOnlyIntersectingZone() throws IOException {
        LoginUser lianYou = new LoginUser(null, "u1", "SCHEDULER");
        LoginUser guanYou = new LoginUser(null, "u2", "SCHEDULER");
        WebSocketSession sLian = sessionWithZones(lianYou, Set.of("炼油区"), true);
        WebSocketSession sGuan = sessionWithZones(guanYou, Set.of("罐区"), true);

        // 事件属于 炼油区
        service.broadcast("x.changed", "payload", Set.of("炼油区"));

        verify(sLian).sendMessage(any(TextMessage.class));
        verify(sGuan, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("事件未做防区映射（zones=null）推给全部已认证会话（fail-open）")
    void unmappedEventBroadcastsToAllAuthenticated() throws IOException {
        LoginUser all = new LoginUser(null, "admin", "ADMIN");
        LoginUser scoped = new LoginUser(null, "u1", "SCHEDULER");
        WebSocketSession sAll = sessionWithZones(all, null, true);
        WebSocketSession sScoped = sessionWithZones(scoped, Set.of("罐区"), true);

        service.broadcast("x.changed", "payload", null);

        verify(sAll).sendMessage(any(TextMessage.class));
        verify(sScoped).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("关闭的会话被跳过（不发送）")
    void closedSessionSkipped() throws IOException {
        LoginUser all = new LoginUser(null, "admin", "ADMIN");
        WebSocketSession s = sessionWithZones(all, null, false);

        service.broadcast("x.changed", "payload", null);

        verify(s, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("无防区交集时受限用户不收到（最小权限）")
    void noIntersectionNoSend() throws IOException {
        LoginUser scoped = new LoginUser(null, "u1", "SCHEDULER");
        WebSocketSession s = sessionWithZones(scoped, Set.of("仓储区"), true);

        service.broadcast("x.changed", "payload", Set.of("炼油区", "罐区"));

        verify(s, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("无参 broadcast 等同未映射（fail-open）")
    void noArgBroadcastFailsOpen() throws IOException {
        LoginUser scoped = new LoginUser(null, "u1", "SCHEDULER");
        WebSocketSession s = sessionWithZones(scoped, Set.of("罐区"), true);

        service.broadcast("alarm.push", "payload");

        verify(s).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("sessionCount 反映已登记会话数")
    void sessionCountReflectsSessions() {
        LoginUser all = new LoginUser(null, "admin", "ADMIN");
        sessionWithZones(all, null, true);
        assertEquals(1, service.sessionCount());
    }
}
