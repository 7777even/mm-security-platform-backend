package com.sinopec.mmsecurity.websocket;

/**
 * 防区归属标记接口：标注本接口的业务实体，其 {@code getZoneName()} 返回该记录所属防区
 * （取值须与 {@code sys_zone.zone_name} 对齐，亦须与 {@code sys_user.zone_codes} 的防区命名一致）。
 *
 * <p>实时广播防区过滤的<b>唯一扩展点</b>：当某写方法（标注 {@code @RealtimeSync}）的返回值实现本接口时，
 * {@code RealtimeSyncAspect} 会自动把该记录的防区注入 {@link EntityChangedEvent#getZones()}，
 * 后端据此按 {@code zone_codes} 过滤推送目标（仅推给防区交集命中的已认证会话）。</p>
 *
 * <p><b>产品依赖</b>：当前仅「救援队伍」等少数域的 area 词表已对齐 {@code sys_zone}
 * （见 data_scope ABAC 设计 §3），其余域（监测告警/设备等）的 location→防区 映射待产品定。
 * 待产品对齐某域 area 词表后，只需让该域写方法返回的实体实现本接口，防区过滤即自动生效，
 * 无需改动广播管线本身。</p>
 */
public interface ZoneAware {

    /** 实体所属防区名（须与 {@code sys_zone.zone_name} 一致）。 */
    String getZoneName();
}
