package com.sinopec.mmsecurity.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 实时发布器：监听 {@link EntityChangedEvent}，将其转成 {@code <domain>.changed} 广播。
 * 写入口经 {@code @RealtimeSync} 切面发布事件后，由本类统一转发到 WebSocket。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimePublisher {

    private final RealtimeBroadcastService broadcastService;

    @EventListener
    public void onEntityChanged(EntityChangedEvent event) {
        publish(event.getDomain(), event.getAction(), event.getId(), event.getData(), event.getZones());
    }

    /** 兼容旧调用：不携带防区信息（推给全部已认证会话）。 */
    public void publish(String domain, EntityChangedEvent.Action action, String id, Object data) {
        publish(domain, action, id, data, null);
    }

    /**
     * 发布 {@code <domain>.changed}，并按 {@code zones} 过滤推送目标。
     *
     * @param zones 该变更所属防区集合；为 {@code null} 表示未做防区映射（fail-open 推全部已认证会话）
     */
    public void publish(String domain, EntityChangedEvent.Action action, String id, Object data, Set<String> zones) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("domain", domain);
        payload.put("action", action.name().toLowerCase());
        payload.put("id", id);
        payload.put("data", data);
        broadcastService.broadcast(domain + ".changed", payload, zones);
        log.debug("实时广播 {}：{} (zones={})", domain + ".changed", action, zones);
    }
}
