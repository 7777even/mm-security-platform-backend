package com.sinopec.mmsecurity.annotation;

import com.sinopec.mmsecurity.config.AbacZoneMappingProperties;
import com.sinopec.mmsecurity.security.ZoneMappingResolver;
import com.sinopec.mmsecurity.websocket.EntityChangedEvent;
import com.sinopec.mmsecurity.websocket.ZoneAware;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RealtimeSyncAspect 防区解析接线测试（纯 Mockito）：验证 extractZones 优先 getZoneName、
 * 否则 getLocation→resolver；未命中→null（fail-open）；并验证 afterWrite 发布事件的 zones 正确注入。
 */
class RealtimeSyncAspectTest {

    record ZoneAwareStub(String zoneName, String location) implements ZoneAware {
        @Override
        public String getZoneName() {
            return zoneName;
        }

        @Override
        public String getLocation() {
            return location;
        }
    }

    private RealtimeSyncAspect aspectWith(Map<String, List<String>> mapping) {
        AbacZoneMappingProperties props = new AbacZoneMappingProperties();
        props.setLocationToZones(mapping);
        return new RealtimeSyncAspect(mock(org.springframework.context.ApplicationEventPublisher.class), new ZoneMappingResolver(props));
    }

    @Test
    void nonZoneAwareReturnsNull() {
        RealtimeSyncAspect aspect = aspectWith(Map.of());
        assertNull(aspect.extractZones(new Object()));
    }

    @Test
    void locationUnmappedFailsOpen() {
        RealtimeSyncAspect aspect = aspectWith(Map.of());
        assertNull(aspect.extractZones(new ZoneAwareStub(null, "装置区A")));
    }

    @Test
    void locationMappedReturnsZones() {
        RealtimeSyncAspect aspect = aspectWith(Map.of("装置区A", List.of("z1")));
        assertEquals(Set.of("z1"), aspect.extractZones(new ZoneAwareStub(null, "装置区A")));
    }

    @Test
    void zoneNameTakesPrecedenceOverLocation() {
        RealtimeSyncAspect aspect = aspectWith(Map.of("装置区A", List.of("z1")));
        assertEquals(Set.of("zX"), aspect.extractZones(new ZoneAwareStub("zX", "装置区A")));
    }

    @Test
    void afterWritePublishesEventWithResolvedZones() {
        var publisher = mock(org.springframework.context.ApplicationEventPublisher.class);
        AbacZoneMappingProperties props = new AbacZoneMappingProperties();
        props.setLocationToZones(Map.of("装置区A", List.of("z1")));
        RealtimeSyncAspect aspect = new RealtimeSyncAspect(publisher, new ZoneMappingResolver(props));

        RealtimeSync ann = mock(RealtimeSync.class);
        when(ann.domain()).thenReturn("alarm");
        JoinPoint jp = mock(JoinPoint.class);
        Signature sig = mock(Signature.class);
        when(sig.getName()).thenReturn("updateAlarm");
        when(jp.getSignature()).thenReturn(sig);
        // org.springframework.context.ApplicationEvent 要求 source 非 null（EventObject 构造器校验）；
        // 生产环境 joinPoint.getTarget() 恒为被通知的 bean 实例，此处 mock 须显式返回非 null。
        when(jp.getTarget()).thenReturn(new Object());

        aspect.afterWrite(jp, ann, new ZoneAwareStub(null, "装置区A"));

        ArgumentCaptor<EntityChangedEvent> cap = ArgumentCaptor.forClass(EntityChangedEvent.class);
        verify(publisher).publishEvent(cap.capture());
        assertEquals(Set.of("z1"), cap.getValue().getZones());
        assertEquals("alarm", cap.getValue().getDomain());
    }
}
