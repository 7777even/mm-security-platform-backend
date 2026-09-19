package com.sinopec.mmsecurity.annotation;

import com.sinopec.mmsecurity.security.ZoneMappingResolver;
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
 * <p>防区过滤扩展点：若写方法返回值实现 {@link ZoneAware}，切面据其 {@code getZoneName()}（直接防区名）
 * 或 {@code getLocation()}（经 {@link ZoneMappingResolver} 映射）解析出防区集合注入事件（{@code zones}），
 * 后端据此按 {@code zone_codes} 过滤推送目标；映射未命中（null）则 {@code zones=null}
 * （该域未做防区映射，fail-open 推给全部已认证会话，待产品定 location→防区 规则）。</p>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RealtimeSyncAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final ZoneMappingResolver zoneMappingResolver;

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

    /**
     * 从写方法返回值解析防区集合：优先 {@code getZoneName()}（直接防区名），否则 {@code getLocation()}
     * 经 {@link ZoneMappingResolver} 映射；映射未命中/空白返回 null（fail-open，与现状一致）。
     * 包可见以便单测直接校验解析逻辑。
     */
    Set<String> extractZones(Object ret) {
        if (!(ret instanceof ZoneAware z)) {
            return null;
        }
        String zoneName = z.getZoneName();
        if (zoneName != null && !zoneName.isBlank()) {
            return Set.of(zoneName.trim());
        }
        String location = z.getLocation();
        if (location != null && !location.isBlank()) {
            return zoneMappingResolver.resolveZonesByLocation(location.trim());
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
