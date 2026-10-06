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

    // —— 以下用例对齐 dev application-dev.yml 的推荐默认映射，锁定「真实 location 自由文本 → 防区」实际解析 ——

    /** 子区域关键词 → 父防区（炼油区系列，基于 H2 dev 取样）。 */
    @Test
    void freeTextRefiningSubAreasResolveToLianyou() {
        ZoneMappingResolver r = resolverWith("contains", Map.of(
                "炼油区", List.of("炼油区"),
                "催化裂化", List.of("炼油区"),
                "常减压", List.of("炼油区"),
                "1#催化", List.of("炼油区"),
                "1#联合装置", List.of("炼油区"),
                "炼油三部", List.of("炼油区")));
        assertEquals(Set.of("炼油区"), r.resolveZonesByLocation("催化裂化装置区"));
        assertEquals(Set.of("炼油区"), r.resolveZonesByLocation("常减压装置"));
        assertEquals(Set.of("炼油区"), r.resolveZonesByLocation("炼油一部 1#催化装置"));
        assertEquals(Set.of("炼油区"), r.resolveZonesByLocation("炼油一部 1#联合装置"));
        assertEquals(Set.of("炼油区"), r.resolveZonesByLocation("炼油三部泵房"));
    }

    /** 显式防区前缀优先于更长的子区域关键词（炼油区+催化裂化 同现 → 保炼油区）。 */
    @Test
    void explicitZonePrefixWinsOverLongerSubAreaKeyword() {
        ZoneMappingResolver r = resolverWith("contains", Map.of(
                "炼油区", List.of("炼油区"),
                "催化裂化", List.of("炼油区")));
        assertEquals(Set.of("炼油区"), r.resolveZonesByLocation("炼油区-催化裂化装置"));
        assertEquals(Set.of("炼油区"), r.resolveZonesByLocation("炼油区-催化裂化装置西侧"));
    }

    /** 储罐区A/B 精确落到 罐区A/B，而非泛化 罐区（精确子防区优先，依赖 LinkedHashMap 保序，同 dev yml）。 */
    @Test
    void storageTankSubZoneResolvesPrecisely() {
        // 必须用 LinkedHashMap 显式保序（Map.of 不保证顺序，等长键平局会不确定）：
        // 罐区A/B 须排在 储罐区 之前，才能对「储罐区A-2」优先命中 罐区A。
        Map<String, List<String>> mapping = new java.util.LinkedHashMap<>();
        mapping.put("罐区", List.of("罐区"));
        mapping.put("罐区A", List.of("罐区A"));
        mapping.put("罐区B", List.of("罐区B"));
        mapping.put("储罐区", List.of("罐区"));
        ZoneMappingResolver r = resolverWith("contains", mapping);
        assertEquals(Set.of("罐区A"), r.resolveZonesByLocation("储罐区A-2"));
        assertEquals(Set.of("罐区B"), r.resolveZonesByLocation("储罐区B-3"));
        assertEquals(Set.of("罐区"), r.resolveZonesByLocation("储运部罐区"));
    }

    /** 特勤保障类（中控/消防/指挥）兜底到 特勤保障区（基于 COMM/FIRE_FAC 取样）。 */
    @Test
    void supportAndEmergencyKeywordsResolveToTeqin() {
        ZoneMappingResolver r = resolverWith("contains", Map.of(
                "特勤保障区", List.of("特勤保障区"),
                "中央控制室", List.of("特勤保障区"),
                "中控室", List.of("特勤保障区"),
                "消防队", List.of("特勤保障区"),
                "消防泵房", List.of("特勤保障区"),
                "指挥", List.of("特勤保障区")));
        assertEquals(Set.of("特勤保障区"), r.resolveZonesByLocation("中央控制室"));
        assertEquals(Set.of("特勤保障区"), r.resolveZonesByLocation("中控室操作台"));
        assertEquals(Set.of("特勤保障区"), r.resolveZonesByLocation("消防队值班室"));
        assertEquals(Set.of("特勤保障区"), r.resolveZonesByLocation("消防泵房北侧"));
        assertEquals(Set.of("特勤保障区"), r.resolveZonesByLocation("指挥中心"));
    }

    /** 厂区/周界/门/全厂区 等泛化表述 → 全厂范围（周界/门按全厂 fail-open 收紧）。 */
    @Test
    void plantWideKeywordsResolveToAllPlant() {
        ZoneMappingResolver r = resolverWith("contains", Map.of(
                "全厂范围", List.of("全厂范围"),
                "周界", List.of("全厂范围"),
                "厂区", List.of("全厂范围"),
                "全厂区", List.of("全厂范围"),
                "门", List.of("全厂范围")));
        assertEquals(Set.of("全厂范围"), r.resolveZonesByLocation("周界东段"));
        assertEquals(Set.of("全厂范围"), r.resolveZonesByLocation("厂区南门西侧"));
        assertEquals(Set.of("全厂范围"), r.resolveZonesByLocation("全厂区"));
        assertEquals(Set.of("全厂范围"), r.resolveZonesByLocation("1#门"));
    }

    /** 码头/水东港(别名) → 码头区（基于 EVT 港区码头取样）。 */
    @Test
    void dockKeywordsResolveToMatou() {
        AbacZoneMappingProperties p = new AbacZoneMappingProperties();
        p.setMatchMode("contains");
        p.setLocationToZones(Map.of("码头区", List.of("码头区"), "码头", List.of("码头区")));
        p.setAliases(Map.of("水东港", "码头区", "水东港区", "码头区"));
        ZoneMappingResolver r = new ZoneMappingResolver(p);
        assertEquals(Set.of("码头区"), r.resolveZonesByLocation("水东港区码头"));
        assertEquals(Set.of("码头区"), r.resolveZonesByLocation("港区液体化工码头2号泊位"));
        assertEquals(Set.of("码头区"), r.resolveZonesByLocation("港区码头输油管廊北段"));
    }

    /** 不可解析的传感器 ID / 纯装置区编码 仍 fail-open（不误命中任何防区）。 */
    @Test
    void unresolvableFreeTextStillFailsOpen() {
        ZoneMappingResolver r = resolverWith("contains", Map.of(
                "炼油区", List.of("炼油区"), "罐区", List.of("罐区"), "厂区", List.of("全厂范围")));
        assertNull(r.resolveZonesByLocation("SMOKE-MUUWJMC9")); // 报警传感器 ID
        assertNull(r.resolveZonesByLocation("A装置区东侧"));     // 装置区编码，无对应防区
        assertNull(r.resolveZonesByLocation("原料泵房-2#"));     // 泵房无泛化映射
    }
}
