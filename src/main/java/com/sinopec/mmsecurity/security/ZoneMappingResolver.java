package com.sinopec.mmsecurity.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sinopec.mmsecurity.config.AbacZoneMappingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
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
 * <p><b>fail-open 不变</b>：配置为空或某 location 未命中 → 返回 {@code null}，
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
