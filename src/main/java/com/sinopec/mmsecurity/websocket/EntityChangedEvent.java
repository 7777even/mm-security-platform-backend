package com.sinopec.mmsecurity.websocket;

import org.springframework.context.ApplicationEvent;

/**
 * 数据变更事件：任一写操作成功后发布，由 {@link RealtimePublisher} 经 WebSocket 广播
 * {@code <domain>.changed} 通知三端刷新。id / data 当前留空，客户端收到后按域重新拉取权威数据。
 */
public class EntityChangedEvent extends ApplicationEvent {

    private final String domain;
    private final Action action;
    private final String id;
    private final Object data;

    public EntityChangedEvent(Object source, String domain, Action action, String id, Object data) {
        super(source);
        this.domain = domain;
        this.action = action;
        this.id = id;
        this.data = data;
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

    public enum Action {
        CREATED,
        UPDATED,
        DELETED
    }
}
