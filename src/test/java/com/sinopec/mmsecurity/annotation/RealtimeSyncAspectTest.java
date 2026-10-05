package com.sinopec.mmsecurity.annotation;

import com.sinopec.mmsecurity.config.AbacZoneMappingProperties;
import com.sinopec.mmsecurity.entity.FacDevice;
import com.sinopec.mmsecurity.security.ZoneMappingResolver;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

/**
 * RealtimeSyncAspect 防区解析单测：验证「写方法返回值实现 ZoneAware → 事件带 zones」这条新路径。
 * 三态广播过滤（越权会话不收）已由 {@code RealtimeBroadcastServiceTest} 覆盖，本测试只验证解析本身。
 */
class RealtimeSyncAspectTest {

    // extractZones 为包可见方法，同包可直接调用
    private final RealtimeSyncAspect aspect = new RealtimeSyncAspect(
            mock(ApplicationEventPublisher.class),
            new ZoneMappingResolver(new AbacZoneMappingProperties()));

    @Test
    void facDevice_zoneName_injectedAsEventZone() {
        FacDevice d = new FacDevice();
        d.setZone("炼油区");
        assertEquals(Set.of("炼油区"), aspect.extractZones(d));
    }

    @Test
    void nullZone_failsOpen() {
        FacDevice d = new FacDevice();
        d.setZone(null);
        assertNull(aspect.extractZones(d));
    }

    @Test
    void blankZone_failsOpen() {
        FacDevice d = new FacDevice();
        d.setZone("   ");
        assertNull(aspect.extractZones(d));
    }

    @Test
    void nonZoneAware_returnsNull() {
        assertNull(aspect.extractZones("just a string"));
    }
}
