# Spec Delta: ABAC 防区映射配置注入与零下行闭环验证

> 本 Change 不新增/修改 capability Requirement（复用 2026-09-19 `abac-zone-mapping` 已批准 Requirement），仅补充配置注入与闭环验证证据。

## ADDED Evidence
### Scenario: dev 映射注入后零下行闭环生效
- **WHEN** `abac.zone-mapping.location-to-zones` 注入与 V34 防区对齐的 dev 示例映射
- **THEN** 受限会话（防区={炼油区}）仅接收防区交集命中的广播；未映射事件（zones=null）fail-open 全推；无交集被排除（由 `RealtimeBroadcastServiceTest` 4 例证明）
