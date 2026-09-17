package com.sinopec.mmsecurity.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinopec.mmsecurity.security.DataScopeResolver;
import com.sinopec.mmsecurity.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用实时广播器：持有 WebSocket 会话集合，向已连接会话下发 {topic, payload} 包络。
 * 复用既有 {@code /ws/alarm} 端点（由 AlarmWebSocketHandler 委托），alarm.push 与
 * {@code <domain>.changed} 同连接承载。
 *
 * <p><b>身份与防区</b>：每个会话在建立时绑定 {@link LoginUser}，并解析其可见防区集合
 * （{@link DataScopeResolver#resolveZonesFor}）。{@code zones == null} 表示该用户
 * data_scope=ALL（看全部）。</p>
 *
 * <p><b>防区过滤（三态，与 data_scope ABAC 一致）</b>：</p>
 * <ul>
 *   <li>会话 zones == null（ALL 用户）→ 推送给该会话；</li>
 *   <li>事件 zones == null（该域尚未做防区映射，待产品定）→ 推送给全部已认证会话（fail-open 保持既有公开语义）；</li>
 *   <li>二者皆非 null → 仅当事件防区与用户防区有交集才推送（最小权限，避免非 ALL 用户看到越权数据）。</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeBroadcastService {

    private final ObjectMapper om;
    private final DataScopeResolver dataScopeResolver;

    /** 会话 → 会话元数据（绑定的身份 + 解析后的防区集合）。 */
    private final Map<WebSocketSession, RealtimeSessionMeta> sessions = new ConcurrentHashMap<>();

    /**
     * 绑定会话身份并解析其可见防区（连接建立时调用）。
     *
     * @param user 握手阶段解析出的登录用户（必非 null；为空表示鉴权缺口，调用方应已拒绝）
     */
    public void addSession(WebSocketSession session, LoginUser user) {
        Set<String> zones = dataScopeResolver.resolveZonesFor(user);
        sessions.put(session, new RealtimeSessionMeta(user, zones));
    }

    public void removeSession(WebSocketSession session) {
        sessions.remove(session);
    }

    public int sessionCount() {
        return sessions.size();
    }

    /**
     * 向所有已连接且已认证的会话广播 {topic, payload}（payload 可为任意可序列化对象）。
     * 未携带防区信息（兼容旧调用 / alarm.push 增量流）→ 推给全部已认证会话（fail-open）。
     */
    public void broadcast(String topic, Object payload) {
        broadcast(topic, payload, null);
    }

    /**
     * 按防区过滤向已认证会话广播。见类文档三态语义。
     */
    public void broadcast(String topic, Object payload, Set<String> eventZones) {
        try {
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("topic", topic);
            envelope.put("payload", payload);
            String json = om.writeValueAsString(envelope);
            TextMessage msg = new TextMessage(json);

            for (Map.Entry<WebSocketSession, RealtimeSessionMeta> entry : sessions.entrySet()) {
                WebSocketSession s = entry.getKey();
                if (s == null || !s.isOpen()) {
                    continue;
                }
                RealtimeSessionMeta meta = entry.getValue();
                Set<String> sessionZones = meta.zones();
                // ALL 用户（null）或事件域未映射（null）→ 推送给该会话
                if (sessionZones == null || eventZones == null) {
                    send(s, msg);
                    continue;
                }
                // 受限用户：仅当事件防区与用户防区有交集才推送（最小权限）
                if (intersects(eventZones, sessionZones)) {
                    send(s, msg);
                }
            }
        } catch (Exception e) {
            log.error("实时广播失败 topic={}", topic, e);
        }
    }

    private boolean intersects(Set<String> a, Set<String> b) {
        // 小集合遍历，避免无谓开销
        Set<String> smaller = a.size() <= b.size() ? a : b;
        Set<String> larger = smaller == a ? b : a;
        for (String z : smaller) {
            if (larger.contains(z)) {
                return true;
            }
        }
        return false;
    }

    private void send(WebSocketSession s, TextMessage msg) {
        try {
            s.sendMessage(msg);
        } catch (IOException e) {
            log.warn("WS 发送失败：{}", e.getMessage());
        }
    }

    /** 会话元数据：绑定的登录用户 + 解析后的可见防区（null = data_scope=ALL，看全部）。 */
    public record RealtimeSessionMeta(LoginUser user, Set<String> zones) {
    }
}
