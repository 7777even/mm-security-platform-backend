package com.sinopec.mmsecurity.annotation;

import com.sinopec.mmsecurity.websocket.EntityChangedEvent;
import com.sinopec.mmsecurity.websocket.ZoneAware;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 实时同步切面：拦截所有标注 {@link RealtimeSync} 的写方法，成功返回后发布
 * {@link EntityChangedEvent}。动作类型由方法名推断（create/save/add/insert→CREATED；
 * delete/remove/cancel→DELETED；其余→UPDATED）。
 *
 * <p>防区过滤扩展点：若写方法返回值实现 {@link ZoneAware}，切面自动把该实体所属防区注入事件
 * （{@code zones}），后端据此按 {@code zone_codes} 过滤推送目标；否则 {@code zones=null}
 * （该域未做防区映射，fail-open 推给全部已认证会话，待产品定 location→防区 规则）。</p>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RealtimeSyncAspect {

    private final ApplicationEventPublisher eventPublisher;

    @AfterReturning(pointcut = "@annotation(realtimeSync)", returning = "ret")
    public void afterWrite(JoinPoint joinPoint, RealtimeSync realtimeSync, Object ret) {
        EntityChangedEvent.Action action = inferAction(joinPoint.getSignature().getName());
        Set<String> zones = extractZones(ret);
        try {
            // 实时通知是「尽力而为」：任何异常都不得影响已提交的业务写操作。
            eventPublisher.publishEvent(
                    new EntityChangedEvent(joinPoint.getTarget(), realtimeSync.domain(), action, null, null, zones));
            log.debug("RealtimeSync 切面发布变更事件 domain={} action={} zones={}", realtimeSync.domain(), action, zones);
        } catch (Exception e) {
            log.error("RealtimeSync 切面发布变更事件失败（已忽略，不影响业务写）domain={} action={}",
                    realtimeSync.domain(), action, e);
        }
    }

    private Set<String> extractZones(Object ret) {
        if (ret instanceof ZoneAware z && z.getZoneName() != null && !z.getZoneName().isBlank()) {
            return Set.of(z.getZoneName().trim());
        }
        return null;
    }

    private EntityChangedEvent.Action inferAction(String methodName) {
        String m = methodName.toLowerCase();
        if (m.contains("create") || m.contains("save") || m.contains("add")
                || m.contains("insert") || m.contains("register") || m.contains("submit")) {
            return EntityChangedEvent.Action.CREATED;
        }
        if (m.contains("delete") || m.contains("remove") || m.contains("cancel")) {
            return EntityChangedEvent.Action.DELETED;
        }
        return EntityChangedEvent.Action.UPDATED;
    }
}
