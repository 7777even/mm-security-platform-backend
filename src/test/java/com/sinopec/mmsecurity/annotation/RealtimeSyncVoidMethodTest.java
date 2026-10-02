package com.sinopec.mmsecurity.annotation;

import com.sinopec.mmsecurity.config.AbacZoneMappingProperties;
import com.sinopec.mmsecurity.security.ZoneMappingResolver;
import com.sinopec.mmsecurity.websocket.EntityChangedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @RealtimeSync 对 void 返回写方法是否照常广播的回归（RealtimeSyncAspect 使用
 * {@code @AfterReturning(returning="ret")}，历史上 AspectJ 对 void 方法的绑定是易踩差异点）。
 *
 * 消防报警删除 FireAlarmService#delete 返回 void：若切面不匹配 void 方法，管理后台删除后
 * 不会广播 fire-alarm.alarm.changed，大屏「消防报警」列表不会同步移除——本用例锁定该行为。
 *
 * 说明：ApplicationEventPublisher 是 Spring 内建可解析依赖，切面会被注入容器自身（而非外部 mock），
 * 因此这里用 ApplicationListener 从真实容器捕获事件，避免误判。
 */
class RealtimeSyncVoidMethodTest {

    @Configuration
    // 被增强类无接口，必须启用 CGLIB 类代理，否则不会被代理、切面看似「未生效」。
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class Config {
        @Bean
        ZoneMappingResolver resolver() {
            return new ZoneMappingResolver(new AbacZoneMappingProperties());
        }

        @Bean
        RealtimeSyncAspect aspect(org.springframework.context.ApplicationEventPublisher publisher,
                ZoneMappingResolver resolver) {
            return new RealtimeSyncAspect(publisher, resolver);
        }

        @Bean
        SampleWriteService sampleWriteService() {
            return new SampleWriteService();
        }

        @Bean
        EventRecorder eventRecorder() {
            return new EventRecorder();
        }
    }

    static class EventRecorder implements ApplicationListener<EntityChangedEvent> {
        final List<EntityChangedEvent> events = new ArrayList<>();

        @Override
        public void onApplicationEvent(EntityChangedEvent event) {
            events.add(event);
        }
    }

    /** 写服务样本：一非 void（对照）+ 一 void（Delete 形态），两者均标 @RealtimeSync。 */
    static class SampleWriteService {
        @RealtimeSync(domain = "fire-alarm.alarm")
        public String update() {
            return "ok";
        }

        @RealtimeSync(domain = "fire-alarm.alarm")
        public void delete() {
            // no-op
        }
    }

    @Test
    void nonVoidMethodBroadcasts_asControl() {
        try (AnnotationConfigApplicationContext ctx =
                new AnnotationConfigApplicationContext(Config.class)) {
            EventRecorder rec = ctx.getBean(EventRecorder.class);
            ctx.getBean(SampleWriteService.class).update();

            assertEquals(1, rec.events.size());
            assertEquals("fire-alarm.alarm", rec.events.get(0).getDomain());
        }
    }

    @Test
    void voidMethodStillBroadcastsDomainChanged() {
        try (AnnotationConfigApplicationContext ctx =
                new AnnotationConfigApplicationContext(Config.class)) {
            EventRecorder rec = ctx.getBean(EventRecorder.class);
            ctx.getBean(SampleWriteService.class).delete();

            assertEquals(1, rec.events.size());
            assertEquals("fire-alarm.alarm", rec.events.get(0).getDomain());
        }
    }
}
