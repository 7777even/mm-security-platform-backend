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
 *
 * <p><b>匹配模式 {@code match-mode}</b>（2026-10-06 增）：业务库里的 location 多为自由文本
 * （如 {@code 炼油区-催化裂化装置西侧}、{@code 水东港区码头平台}），逐条精确枚举既不现实也易腐。
 * 故在既有「精确匹配」之上增加两种可配置模式，<b>默认 {@code exact} 保持既有行为不变</b>，
 * 由产品/运维按需开启：</p>
 * <ul>
 *   <li>{@code exact}：仅 location 与配置键相等（忽略大小写/首尾空白）时命中；</li>
 *   <li>{@code prefix}：location 以某配置键开头时命中（<b>最长键优先</b>）；</li>
 *   <li>{@code contains}：location 包含某配置键时命中（<b>最长键优先</b>）。</li>
 * </ul>
 * <p>三种模式未命中一律返回 null → 该域 fail-open，语义不变。</p>
 *
 * <p><b>别名兜底 {@code aliases}</b>：业务库 location 可能用与防区名不同的同义表述（如 {@code 催化裂化装置西侧}
 * 不含防区名 {@code 炼油区}）。{@code aliases} 把这类别名映射到某个 location-to-zones 的键，在 prefix/contains
 * 模式下同样按「最长键优先」尝试；映射目标须是 {@code location-to-zones} 中已存在的键，否则该别名静默失效（fail-open）。</p>
 */
@Component
@ConfigurationProperties(prefix = "abac.zone-mapping")
public class AbacZoneMappingProperties {

    /** 匹配模式：exact（默认，既有行为）/ prefix / contains。非法值按 exact 处理。 */
    private String matchMode = "exact";

    /** location/area 标识 → 防区名列表（须与 sys_zone.zone_name 对齐）。默认空 = fail-open。 */
    private Map<String, List<String>> locationToZones = new LinkedHashMap<>();

    /** 别名 → location-to-zones 的键（prefix/contains 模式下的同义表述兜底）。默认空 = 无别名。 */
    private Map<String, String> aliases = new LinkedHashMap<>();

    public String getMatchMode() {
        return matchMode;
    }

    public void setMatchMode(String matchMode) {
        this.matchMode = (matchMode == null || matchMode.isBlank()) ? "exact" : matchMode.trim().toLowerCase();
    }

    public Map<String, List<String>> getLocationToZones() {
        return locationToZones;
    }

    public void setLocationToZones(Map<String, List<String>> locationToZones) {
        this.locationToZones = locationToZones == null ? new LinkedHashMap<>() : locationToZones;
    }

    public Map<String, String> getAliases() {
        return aliases;
    }

    public void setAliases(Map<String, String> aliases) {
        this.aliases = aliases == null ? new LinkedHashMap<>() : aliases;
    }
}
