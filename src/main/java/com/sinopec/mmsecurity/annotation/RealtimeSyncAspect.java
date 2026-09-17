package com.sinopec.mmsecurity.annotation;

import com.sinopec.mmsecurity.websocket.EntityChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 实时同步切面：拦截所有标注 {@link RealtimeSync} 的写方法，成功返回后发布
 * {@link EntityChangedEvent}。动作类型由方法名推断（create/save/add/insert→CREATED；
 * delete/remove/cancel→DELETED；其余→UPDATED）。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RealtimeSyncAspect {

    private final ApplicationEventPublisher eventPublisher;

    @AfterReturning("@annotation(realtimeSync)")
    public void afterWrite(JoinPoint joinPoint, RealtimeSync realtimeSync) {
        EntityChangedEvent.Action action = inferAction(joinPoint.getSignature().getName());
        try {
            // 实时通知是「尽力而为」：任何异常都不得影响已提交的业务写操作。
            eventPublisher.publishEvent(
                    new EntityChangedEvent(joinPoint.getTarget(), realtimeSync.domain(), action, null, null));
            log.debug("RealtimeSync 切面发布变更事件 domain={} action={}", realtimeSync.domain(), action);
        } catch (Exception e) {
            log.error("RealtimeSync 切面发布变更事件失败（已忽略，不影响业务写）domain={} action={}",
                    realtimeSync.domain(), action, e);
        }
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
