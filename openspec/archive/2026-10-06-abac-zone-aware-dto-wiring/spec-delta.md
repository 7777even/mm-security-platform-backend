# Spec Delta: realtime-broadcast（ABAC 防区收紧——写方法返回 DTO 接线 ZoneAware）

## ADDED — 防区扩展点的接线对象

- 后端 SHALL 让 `@RealtimeSync` 写方法的**返回值类型**实现 `ZoneAware`，使 `RealtimeSyncAspect`
  能解析出 `EntityChangedEvent.zones`；**对实体接线而返回值是 DTO 的，不产生任何收紧效果**。
- 已接线 18 个写方法返回 DTO：走 `getLocation()` 映射的 16 个（AlarmItem / PatrolExecutionView /
  CommunicationDevice / EmergencyEventItem / EmergencyCaseItem / FireAlarmItem / FireFacilityLedgerItem /
  ProductionAlarmItem / FireBrigadeTeam / RescueVehicleItem / RescueEquipmentItem / PersonSearchDetail /
  GateControlItem / PerimeterAlarmDetail / SpecialOperationItem / VideoCameraItem），
  走 `getZoneName()` 直达的 2 个（BollardItem / TvMonitorSummary）。

## ADDED — 未映射必须 fail-open（不变式）

- `ZoneMappingResolver` SHALL 在配置为空或 location 未命中时返回 `null`，
  使事件 `zones == null` → `RealtimeBroadcastService` 按「未映射」态推给全部已认证会话。
- 系统 MUST NOT 在缺少产品映射规则时凭 location 字面量臆造防区
  （AGENTS §11.3：禁止 AI 自造业务规则 / 权限模型）。
- ALL 用户（会话 `zones == null`）SHALL 始终收到全部广播，不受收紧影响。

## 已知限制（不视为违规）

- 返回 `void` / `DeleteResult` / `long` / `int` / `List<*>` 的写方法（含全部删除操作）无法携带防区，
  `zones` 恒为 null → fail-open。
- `SystemUserItem` 的 `zoneCodes` 为多值逗号串，`ZoneAware` 仅支持单防区名 / 单 location，
  故 `system.user` 域不接线，避免错误收紧账号管理广播。

## 不变

- `RealtimeBroadcastService` 三态过滤语义、`ZoneMappingResolver` 解析逻辑、`@RealtimeSync` 切面行为均不改；
- 不加任何硬编码 location→防区 规则；产品填配置即生效，无需改代码。
