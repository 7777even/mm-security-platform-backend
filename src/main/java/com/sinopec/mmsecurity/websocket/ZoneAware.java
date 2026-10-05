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
 * <p><b>接线对象 = 写方法的返回值类型</b>（不是实体）：切面取的是 {@code @RealtimeSync} 写方法的
 * {@code returning} 值，而多数写方法返回的是 DTO / View（如 {@code AlarmItem}、{@code GateControlItem}），
 * 只有 {@code FacDevice} 等少数直接返回实体。故收紧时应对着「写方法返回类型」接线，对实体接线大多无效。</p>
 *
 * <p><b>已接线清单（2026-10-06）</b>：{@code FacDevice}（getZoneName）+ 18 个写方法返回 DTO
 * ——AlarmItem / PatrolExecutionView / CommunicationDevice / EmergencyEventItem / EmergencyCaseItem /
 * FireAlarmItem / FireFacilityLedgerItem / ProductionAlarmItem / FireBrigadeTeam / RescueVehicleItem /
 * RescueEquipmentItem / PersonSearchDetail / GateControlItem / PerimeterAlarmDetail /
 * SpecialOperationItem / VideoCameraItem（以上走 getLocation 映射）与 BollardItem / TvMonitorSummary
 * （直接持有防区名，走 getZoneName）。断言见 {@code ZoneAwareDtoWiringTest}。</p>
 *
 * <p><b>产品依赖</b>：location→防区 的语义规则由产品在配置
 * {@code abac.zone-mapping.location-to-zones} 中提供（填了即生效，无需改代码）；
 * 未配置或未命中 → {@code zones == null} → 全推（fail-open，维持既有公开语义）。
 * 另：写方法返回 {@code void} 或 {@code DeleteResult}（删除类）无法携带防区，天然 fail-open，属已知限制。</p>
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
