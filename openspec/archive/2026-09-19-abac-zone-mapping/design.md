# Design: ABAC 实时广播防区映射接线

## ADR-1：映射数据源选配置而非 `sys_zone_mapping` 表
产品规则（location→防区）本质是「可部署的配置」，而非高频事务数据。落地为 `@ConfigurationProperties`（`abac.zone-mapping.location-to-zones`）而非新增 DB 表：
- 避免三方言 Flyway 迁移负担（达梦/Oracle 兼容、PG 实跑）；
- 产品/运维改规则无需发版数据库迁移，改配置即生效；
- 缺省空 Map = fail-open，与现状零差异。
若后续规则规模/审计要求上升，可平滑迁移为 `sys_zone_mapping` 表 + Mapper，接口 `ZoneMappingResolver.resolveZonesByLocation` 不变（依赖倒置）。

## ADR-2：`ZoneAware` 双扩展点（getZoneName / getLocation）
- `getZoneName()`：实体已直接持有与 `sys_zone.zone_name` 对齐的防区名时直接用（既有语义）。
- `getLocation()`：实体仅持有 location/area 标识时，交由 `ZoneMappingResolver` 映射。
- 二者皆 `default` 返回 null，向后兼容；切面优先 `getZoneName()`、否则 `getLocation()`。

## ADR-3：fail-open 不变
`resolveZonesByLocation` 在未命中/空白/配置空时返回 `null`；切面将 null 注入事件 `zones`；`RealtimeBroadcastService` 已有「eventZones==null → 推全部已认证会话」三态语义。故配置为空时行为与今日完全一致，收紧是「加配置即生效」的纯增量。

## 风险
- R1 配置键大小写/空白不一致 → resolver 做 trim + 大小写兜底匹配（仅兜底，不覆盖精确匹配）。
- R2 缓存一致性：配置变更后最长 10min 经 TTL 生效；产品规则属低频变更，可接受。如需即时，后续加 `invalidateAll()` 管理端点（不在本次范围）。
- R3 误配导致过度收紧（某 location 映射到错误防区）→ 属产品配置责任；代码不校验防区名是否存在于 `sys_zone`（宽松，避免强耦合种子数据）。

## 依赖 / 数据影响
- 依赖：Caffeine（已引入，与 `DataScopeResolver` 同）。无新依赖。
- 数据：无。
