package com.sinopec.mmsecurity.websocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinopec.mmsecurity.security.DataScopeResolver;
import com.sinopec.mmsecurity.security.LoginUser;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;

/**
 * 零下行权限控制闭环验证：实时广播按「会话防区 × 事件防区」三态过滤。
 *
 * <p>三态语义（与 {@link RealtimeBroadcastService} 类文档一致）：</p>
 * <ul>
 *   <li>会话 zones == null（data_scope=ALL）→ 推送给该会话；</li>
 *   <li>事件 zones == null（域未做防区映射，fail-open）→ 推送给全部已认证会话；</li>
 *   <li>二者皆非 null → 仅交集非空才推送（最小权限，越权数据不下行）。</li>
 * </ul>
 *
 * <p>本测试直接驱动 {@link RealtimeBroadcastService#broadcast(String, Object, Set)}，
 * 用 mock {@link DataScopeResolver} 为各会话绑定可见防区，断言越权事件不下行。</p>
 */
class RealtimeBroadcastServiceTest {

    private final ObjectMapper om = new ObjectMapper();
    private final DataScopeResolver dataScopeResolver = mock(DataScopeResolver.class);
    private final RealtimeBroadcastService service = new RealtimeBroadcastService(om, dataScopeResolver);

    /** 为某登录用户注册一个已认证会话，并将其可见防区绑定到给定集合（null = ALL）。 */
    private WebSocketSession sessionFor(LoginUser user, Set<String> zones) {
        when(dataScopeResolver.resolveZonesFor(user)).thenReturn(zones);
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        service.addSession(session, user);
        return session;
    }

    @Test
    void restrictedUserOnlyReceivesIntersectingZone() throws Exception {
        LoginUser allUser = mock(LoginUser.class);
        LoginUser refineryUser = mock(LoginUser.class);
        LoginUser tankUser = mock(LoginUser.class);

        WebSocketSession allSession = sessionFor(allUser, null); // ALL：看全部
        WebSocketSession refinerySession = sessionFor(refineryUser, Set.of("炼油区"));
        WebSocketSession tankSession = sessionFor(tankUser, Set.of("罐区"));

        // 事件防区 = {炼油区}：炼油区用户收到、ALL 用户收到、罐区用户（无交集）不收
        service.broadcast("domain.changed", "payload", Set.of("炼油区"));

        verify(refinerySession).sendMessage(any(TextMessage.class));
        verify(allSession).sendMessage(any(TextMessage.class));
        verify(tankSession, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    void unmappedEventZoneFailsOpenToAllAuthenticated() throws Exception {
        LoginUser refineryUser = mock(LoginUser.class);
        WebSocketSession refinerySession = sessionFor(refineryUser, Set.of("炼油区"));

        // 事件域未做防区映射（null）→ fail-open：推给全部已认证会话（含受限用户）
        service.broadcast("domain.changed", "payload", null);

        verify(refinerySession).sendMessage(any(TextMessage.class));
    }

    @Test
    void noIntersectionExcludesRestrictedUser() throws Exception {
        LoginUser refineryUser = mock(LoginUser.class);
        WebSocketSession refinerySession = sessionFor(refineryUser, Set.of("炼油区"));

        // 事件防区 = {码头区} 与 用户防区 {炼油区} 无交集 → 不下行
        service.broadcast("domain.changed", "payload", Set.of("码头区"));

        verify(refinerySession, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    void sessionCountReflectsRegisteredSessions() {
        LoginUser user = mock(LoginUser.class);
        sessionFor(user, Set.of("炼油区"));
        assertEquals(1, service.sessionCount());
    }
}
