package com.sinopec.mmsecurity.security;

import com.sinopec.mmsecurity.config.AbacZoneMappingProperties;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * ZoneMappingResolver 测试（纯单测）：覆盖 fail-open 默认、配置命中、空白/大小写归一、未命中。
 * 验证「规则来自配置、代码不硬编码、缺省 fail-open」。
 */
class ZoneMappingResolverTest {

    private ZoneMappingResolver resolverWith(Map<String, List<String>> mapping) {
        AbacZoneMappingProperties props = new AbacZoneMappingProperties();
        props.setLocationToZones(mapping);
        return new ZoneMappingResolver(props);
    }

    @Test
    void emptyConfigFailsOpen() {
        ZoneMappingResolver r = resolverWith(Map.of());
        assertNull(r.resolveZonesByLocation("装置区A"));
        assertNull(r.resolveZonesByLocation(null));
        assertNull(r.resolveZonesByLocation("   "));
    }

    @Test
    void configuredHitReturnsZones() {
        ZoneMappingResolver r = resolverWith(Map.of("装置区A", List.of("z1", "z2")));
        assertEquals(Set.of("z1", "z2"), r.resolveZonesByLocation("装置区A"));
    }

    @Test
    void trimsAndSkipsBlank() {
        ZoneMappingResolver r = resolverWith(Map.of(" 区A ", List.of(" z1 ", "", "z2")));
        assertEquals(Set.of("z1", "z2"), r.resolveZonesByLocation("区A"));
    }

    @Test
    void caseInsensitiveFallback() {
        ZoneMappingResolver r = resolverWith(Map.of("ZoneA", List.of("z1")));
        assertEquals(Set.of("z1"), r.resolveZonesByLocation("zonea"));
    }

    @Test
    void unmappedLocationFailsOpen() {
        ZoneMappingResolver r = resolverWith(Map.of("ZoneA", List.of("z1")));
        assertNull(r.resolveZonesByLocation("Other"));
    }

    @Test
    void nullPropsFailsOpen() {
        ZoneMappingResolver r = resolverWith(null);
        assertNull(r.resolveZonesByLocation("ZoneA"));
    }
}
