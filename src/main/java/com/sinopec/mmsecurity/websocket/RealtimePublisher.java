package com.sinopec.mmsecurity.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

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
        publish(event.getDomain(), event.getAction(), event.getId(), event.getData());
    }

    public void publish(String domain, EntityChangedEvent.Action action, String id, Object data) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("domain", domain);
        payload.put("action", action.name().toLowerCase());
        payload.put("id", id);
        payload.put("data", data);
        broadcastService.broadcast(domain + ".changed", payload);
        log.debug("实时广播 {}：{}", domain + ".changed", action);
    }
}
