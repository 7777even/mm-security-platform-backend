package com.sinopec.mmsecurity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记写方法需要广播数据变更事件。切面 {@link RealtimeSyncAspect} 在方法成功返回后发布
 * {@link com.sinopec.mmsecurity.websocket.EntityChangedEvent}，由 RealtimePublisher 经
 * WebSocket 广播 {@code <domain>.changed}，触发三端对应域刷新。
 *
 * 覆盖保证：每个对外写方法均须标注本注解（CI / 评审以「写方法是否带注解」为审计点），
 * 新增域按约定标注即自动接入实时广播。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RealtimeSync {
    /** 业务域标识，如 device / workstation / role / ledger / emergency.command 等。 */
    String domain();
}
