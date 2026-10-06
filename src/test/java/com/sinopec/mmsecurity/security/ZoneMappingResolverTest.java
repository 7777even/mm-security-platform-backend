package com.sinopec.mmsecurity.security;

import com.sinopec.mmsecurity.config.AbacZoneMappingProperties;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link ZoneMappingResolver} 三种匹配模式 + 别名兜底 的单测。
 *
 * <p>锁两件事：① exact/prefix/contains 各自命中与不命中的边界；② 别名在 contains 模式下
 * 重定向到 canonical 防区；③ 任何未命中/空白/非法模式一律 fail-open（返回 null）。</p>
 */
class ZoneMappingResolverTest {

    private ZoneMappingResolver resolverWith(String mode, Map<String, List<String>> mapping) {
        AbacZoneMappingProperties p = new AbacZoneMappingProperties();
        p.setMatchMode(mode);
        p.setLocationToZones(mapping);
        return new ZoneMappingResolver(p);
    }

    @Test
    void exactModeMatchesOnlyEquality() {
        ZoneMappingResolver r = resolverWith("exact", Map.of("厂区南门", List.of("炼油区", "罐区")));
        assertEquals(Set.of("炼油区", "罐区"), r.resolveZonesByLocation("厂区南门"));
        // 空白/大小写不一致由兜底命中
        assertEquals(Set.of("炼油区", "罐区"), r.resolveZonesByLocation(" 厂区南门 "));
        // 非相等（子串/前缀）不命中 → fail-open
        assertNull(r.resolveZonesByLocation("厂区南门-西侧"));
        assertNull(r.resolveZonesByLocation("西侧厂区南门"));
    }

    @Test
    void prefixModeHitsPrefixOnly() {
        ZoneMappingResolver r = resolverWith("prefix", Map.of("炼油区", List.of("炼油区")));
        assertEquals(Set.of("炼油区"), r.resolveZonesByLocation("炼油区-催化裂化装置西侧"));
        assertNull(r.resolveZonesByLocation("西侧炼油区")); // 非前缀
    }

    @Test
    void containsModeHitsSubstringLongestFirst() {
        ZoneMappingResolver r = resolverWith("contains", Map.of(
                "罐区", List.of("罐区"),
                "罐区A", List.of("罐区A")));
        assertEquals(Set.of("罐区A"), r.resolveZonesByLocation("罐区A泵房")); // 最长键优先
        assertEquals(Set.of("罐区"), r.resolveZonesByLocation("罐区泵房"));     // 不含 "罐区A"
    }

    @Test
    void containsModeWithFreeTextLocation() {
        ZoneMappingResolver r = resolverWith("contains", Map.of("炼油区", List.of("炼油区")));
        assertEquals(Set.of("炼油区"), r.resolveZonesByLocation("炼油区-催化裂化装置西侧"));
    }

    @Test
    void aliasFallbackRedirectsToCanonicalZone() {
        AbacZoneMappingProperties p = new AbacZoneMappingProperties();
        p.setMatchMode("contains");
        p.setLocationToZones(Map.of("炼油区", List.of("炼油区")));
        p.setAliases(Map.of("催化裂化", "炼油区"));
        ZoneMappingResolver r = new ZoneMappingResolver(p);
        // 同义表述不含防区名，但命中别名 → 重定向到 canonical 防区
        assertEquals(Set.of("炼油区"), r.resolveZonesByLocation("催化裂化装置西侧"));
    }

    @Test
    void unknownModeFallsBackToExact() {
        ZoneMappingResolver r = resolverWith("fuzzy", Map.of("厂区南门", List.of("炼油区")));
        assertNull(r.resolveZonesByLocation("厂区南门-西侧"));
        assertEquals(Set.of("炼油区"), r.resolveZonesByLocation("厂区南门")); // 精确仍命中
    }

    @Test
    void blankOrNullLocationFailsOpen() {
        ZoneMappingResolver r = resolverWith("contains", Map.of("炼油区", List.of("炼油区")));
        assertNull(r.resolveZonesByLocation(null));
        assertNull(r.resolveZonesByLocation("  "));
    }

    @Test
    void emptyMappingFailsOpenRegardlessOfMode() {
        ZoneMappingResolver r = resolverWith("contains", Map.of());
        assertNull(r.resolveZonesByLocation("炼油区-催化裂化装置西侧"));
    }
}
