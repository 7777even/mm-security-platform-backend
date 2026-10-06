package com.sinopec.mmsecurity.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sinopec.mmsecurity.config.AbacZoneMappingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 可配置的 location/area → 防区 映射解析器（ABAC 实时广播防区收紧的「规则注入点」）。
 *
 * <p><b>设计意图</b>：写方法返回的实体往往只携带 {@code location}（如装置区编码 / 区域名），
 * 并不直接持有防区名；而防区名须与 {@code sys_zone.zone_name} 对齐。location→防区 的语义
 * <b>由产品/运维在配置 {@code abac.zone-mapping.location-to-zones} 中提供</b>，本类不做任何硬编码映射，
 * 杜绝「AI 自造业务规则 / 权限模型」（AGENTS §11.3）。</p>
 *
 * <p><b>匹配模式</b>（{@code abac.zone-mapping.match-mode}，默认 {@code exact}，既有的精确匹配行为不变）：</p>
 * <ul>
 *   <li>{@code exact}：location 与配置键相等（忽略大小写/首尾空白）；</li>
 *   <li>{@code prefix}：location 以某配置键开头（最长键优先）；</li>
 *   <li>{@code contains}：location 包含某配置键（最长键优先）—— 适配自由文本 location（如
 *       {@code 炼油区-催化裂化装置西侧} 含 {@code 炼油区}）。</li>
 * </ul>
 *
 * <p><b>别名兜底</b>：{@code aliases} 把同义表述映射到某个 location-to-zones 键，prefix/contains 模式下
 * 同样按最长键优先尝试；目标须是已存在的键，否则该别名静默失效。</p>
 *
 * <p><b>fail-open 不变</b>：配置为空 / 某 location 未命中 / 模式非法 → 返回 {@code null}，
 * 等同于「该域尚未做防区映射」，广播维持 fail-open（推给全部已认证会话），与现有公开语义一致；
 * 产品填规则后无需改代码即自动收紧。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZoneMappingResolver {

    private final AbacZoneMappingProperties properties;

    private final Cache<String, Set<String>> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(512)
            .build();

    /**
     * 解析某 location 对应的防区集合。
     *
     * @return 非空防区集合；未命中/空白/配置为空 → {@code null}（fail-open）
     */
    public Set<String> resolveZonesByLocation(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }
        return cache.get(location.trim(), this::lookup);
    }

    private Set<String> lookup(String key) {
        Map<String, List<String>> mapping = properties.getLocationToZones();
        if (mapping == null || mapping.isEmpty()) {
            return null;
        }
        return switch (normalizeMode(properties.getMatchMode())) {
            case "prefix" -> matchBySubstring(key, mapping, true);
            case "contains" -> matchBySubstring(key, mapping, false);
            default -> exactMatch(key, mapping);
        };
    }

    private static String normalizeMode(String mode) {
        if (mode == null) {
            return "exact";
        }
        return switch (mode.trim().toLowerCase()) {
            case "exact", "prefix", "contains" -> mode.trim().toLowerCase();
            default -> "exact";
        };
    }

    private Set<String> exactMatch(String key, Map<String, List<String>> mapping) {
        List<String> exact = mapping.get(key);
        if (exact != null && !exact.isEmpty()) {
            return toSet(exact);
        }
        // 兜底：配置键大小写/空白不一致时再尝试 trim+忽略大小写匹配（不覆盖精确匹配）
        for (Map.Entry<String, List<String>> e : mapping.entrySet()) {
            String cfgKey = e.getKey();
            if (cfgKey != null && cfgKey.trim().equalsIgnoreCase(key)
                    && e.getValue() != null && !e.getValue().isEmpty()) {
                return toSet(e.getValue());
            }
        }
        return null;
    }

    private Set<String> matchBySubstring(String key, Map<String, List<String>> mapping, boolean prefix) {
        // 最长键优先：避免「罐区」抢在「罐区A」之前命中
        for (String k : sortedByLengthDesc(mapping.keySet())) {
            if (k == null || k.isBlank()) {
                continue;
            }
            if (substringHit(key, k.trim(), prefix)) {
                return toSet(mapping.get(k));
            }
        }
        // 别名兜底：命中别名 → 重定向到 canonical 键的防区集合
        Map<String, String> aliases = properties.getAliases();
        if (aliases != null && !aliases.isEmpty()) {
            for (String ak : sortedByLengthDesc(aliases.keySet())) {
                if (ak == null || ak.isBlank()) {
                    continue;
                }
                if (substringHit(key, ak.trim(), prefix)) {
                    String canonical = aliases.get(ak);
                    if (canonical != null && !canonical.isBlank()) {
                        List<String> zones = mapping.get(canonical.trim());
                        if (zones != null) {
                            return toSet(zones);
                        }
                    }
                }
            }
        }
        return null;
    }

    private static boolean substringHit(String key, String token, boolean prefix) {
        return prefix ? startsWithIgnoreCase(key, token) : containsIgnoreCase(key, token);
    }

    private static boolean startsWithIgnoreCase(String s, String prefix) {
        return s.length() >= prefix.length() && s.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    private static boolean containsIgnoreCase(String s, String token) {
        return s.toLowerCase().indexOf(token.toLowerCase()) >= 0;
    }

    private static List<String> sortedByLengthDesc(Set<String> keys) {
        List<String> list = new ArrayList<>(keys);
        list.sort((a, b) -> Integer.compare(b == null ? 0 : b.length(), a == null ? 0 : a.length()));
        return list;
    }

    private Set<String> toSet(List<String> zones) {
        Set<String> set = new LinkedHashSet<>();
        for (String z : zones) {
            if (z != null && !z.isBlank()) {
                set.add(z.trim());
            }
        }
        return set.isEmpty() ? null : set;
    }
}
