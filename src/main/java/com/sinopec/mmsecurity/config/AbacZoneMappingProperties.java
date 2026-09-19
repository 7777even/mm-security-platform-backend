package com.sinopec.mmsecurity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ABAC 实时广播防区映射的配置载体（location/area 标识 → 防区名集合）。
 *
 * <p><b>规则来源</b>：`abac.zone-mapping.location-to-zones` 由产品/运维在配置中提供，
 * 代码<b>不硬编码任何映射</b>（AGENTS §11.3 禁止 AI 自建权限模型）。缺省为空 Map，
 * 等价于「尚未定义任何域的 location→防区 映射」，广播维持 fail-open。</p>
 *
 * <p>防区名须与 {@code sys_zone.zone_name} 对齐，由填配置方保证一致性。</p>
 */
@Component
@ConfigurationProperties(prefix = "abac.zone-mapping")
public class AbacZoneMappingProperties {

    /** location/area 标识 → 防区名列表（须与 sys_zone.zone_name 对齐）。默认空 = fail-open。 */
    private Map<String, List<String>> locationToZones = new LinkedHashMap<>();

    public Map<String, List<String>> getLocationToZones() {
        return locationToZones;
    }

    public void setLocationToZones(Map<String, List<String>> locationToZones) {
        this.locationToZones = locationToZones == null ? new LinkedHashMap<>() : locationToZones;
    }
}
