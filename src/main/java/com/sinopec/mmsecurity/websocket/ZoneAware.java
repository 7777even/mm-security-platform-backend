package com.sinopec.mmsecurity.websocket;

/**
 * 防区归属标记接口：标注本接口的业务实体，实时广播管线据其返回防区以做 {@code zone_codes} 过滤。
 *
 * <p>实时广播防区过滤的<b>唯一扩展点</b>：当某写方法（标注 {@code @RealtimeSync}）的返回值实现本接口时，
 * {@code RealtimeSyncAspect} 会据 {@link #getZoneName()} 或 {@link #getLocation()} 解析出该记录的防区，
 * 注入 {@link EntityChangedEvent#getZones()}，后端据此按 {@code zone_codes} 过滤推送目标
 * （仅推给防区交集命中的已认证会话）。</p>
 *
 * <p><b>两个扩展方法（皆默认返回 null，向后兼容）</b>：</p>
 * <ul>
 *   <li>{@link #getZoneName()}：实体已直接持有与 {@code sys_zone.zone_name} 对齐的防区名时直接用；</li>
 *   <li>{@link #getLocation()}：实体仅持有 location/area 标识时，交由 {@code ZoneMappingResolver}
 *       经配置 {@code abac.zone-mapping.location-to-zones} 映射为防区集合（规则由产品提供，代码不硬编码）。</li>
 * </ul>
 *
 * <p><b>产品依赖</b>：监测告警/设备等多数域的 location→防区 映射待产品定（填配置即生效，无需改代码）。
 * 当前无写方法返回 {@code ZoneAware} 时，事件 {@code zones == null} → 全推（fail-open）。</p>
 */
public interface ZoneAware {

    /** 实体所属防区名（须与 {@code sys_zone.zone_name} 一致）。非空时直接作为防区，跳过映射。 */
    default String getZoneName() {
        return null;
    }

    /** 实体的位置/区域标识（如 area_code / 装置区编码 / 区域名）。非空时经 {@code ZoneMappingResolver} 映射为防区集合；映射未命中→fail-open。 */
    default String getLocation() {
        return null;
    }
}
