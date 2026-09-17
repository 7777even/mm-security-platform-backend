package com.sinopec.mmsecurity.websocket;

import org.springframework.context.ApplicationEvent;

import java.util.Set;

/**
 * 数据变更事件：任一写操作成功后发布，由 {@link RealtimePublisher} 经 WebSocket 广播
 * {@code <domain>.changed} 通知三端刷新。id / data 当前留空，客户端收到后按域重新拉取权威数据。
 *
 * <p>{@code zones} 为该次变更所属防区集合（用于按 {@code zone_codes} 过滤推送目标）；
 * 为 {@code null} 表示此域尚未做防区映射（待产品定 location→防区 规则），广播给全部已认证会话。</p>
 */
public class EntityChangedEvent extends ApplicationEvent {

    private final String domain;
    private final Action action;
    private final String id;
    private final Object data;
    private final Set<String> zones;

    public EntityChangedEvent(Object source, String domain, Action action, String id, Object data) {
        this(source, domain, action, id, data, null);
    }

    public EntityChangedEvent(Object source, String domain, Action action, String id, Object data, Set<String> zones) {
        super(source);
        this.domain = domain;
        this.action = action;
        this.id = id;
        this.data = data;
        this.zones = zones;
    }

    public String getDomain() {
        return domain;
    }

    public Action getAction() {
        return action;
    }

    public String getId() {
        return id;
    }

    public Object getData() {
        return data;
    }

    public Set<String> getZones() {
        return zones;
    }

    public enum Action {
        CREATED,
        UPDATED,
        DELETED
    }
}
