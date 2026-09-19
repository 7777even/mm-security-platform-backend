# Spec Delta: ABAC 实时广播防区映射接线

## ADDED Requirements

### Requirement: 可配置 location→防区 映射（产品驱动收紧）
系统 SHALL 提供配置驱动的 `location → 防区` 映射解析（`ZoneMappingResolver`，配置 `abac.zone-mapping.location-to-zones`），将写实体携带的 `location`/`area` 标识解析为与 `sys_zone.zone_name` 对齐的防区集合，作为实时广播防区过滤的输入。

#### Scenario: 配置为空 fail-open
- **WHEN** `abac.zone-mapping.location-to-zones` 为空（或某 location 未命中）
- **THEN** `resolveZonesByLocation` 返回 null，广播事件 `zones == null`，全部已认证会话 fail-open 接收（与现状一致）

#### Scenario: 配置命中收紧
- **WHEN** 某 location 在配置中存在映射
- **THEN** 返回对应的防区集合，广播时按三态过滤仅推送给防区交集命中的会话

#### Scenario: 规则来自配置不来自代码
- **WHEN** 检视 `ZoneMappingResolver` 实现
- **THEN** 不存在硬编码的 location→防区 映射规则；映射完全由 `abac.zone-mapping.location-to-zones` 配置提供（AI 不自建权限模型）

## MODIFIED Requirements

### Requirement: 防区注入唯一扩展点 ZoneAware
（原 requirement 保留，补充 getLocation 扩展点）

#### Scenario: 带防区名写方法（不变）
- **WHEN** 标注 `@RealtimeSync(domain)` 的写方法返回 `ZoneAware` 且其 `getZoneName()` 非空
- **THEN** 发布的 `EntityChangedEvent` 携带该防区，广播时按三态过滤

#### Scenario: 带 location 写方法经映射
- **WHEN** 写方法返回 `ZoneAware` 且其 `getLocation()` 非空，但 `getZoneName()` 为空
- **THEN** 系统经 `ZoneMappingResolver` 将 `getLocation()` 映射为防区集合注入事件 `zones`；映射未命中（null）则 fail-open

#### Scenario: 无防区写方法（预备基建，不变）
- **WHEN** 当前无任何写方法返回 `ZoneAware`（归属规则待产品定）
- **THEN** 广播事件 `zones == null`，全部已认证会话 fail-open 接收；`ZoneAware` + `ZoneMappingResolver` 作为扩展点就绪
