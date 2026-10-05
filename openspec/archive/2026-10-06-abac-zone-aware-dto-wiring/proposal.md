# Change: ABAC 实时广播防区收紧——写方法返回 DTO 接线 ZoneAware

## 为什么

P0 债务「ABAC zone 注入收紧」自 09-17 起一直 fail-open：链路代码虽已完整
（`ZoneAware` → `ZoneMappingResolver` → `RealtimeSyncAspect` → `RealtimeBroadcastService` 三态过滤），
但**几乎没有写方法的返回值实现 `ZoneAware`** → 事件 `zones` 恒为 null → 广播等同全量推送，
防区隔离形同虚设。

本 Change 定位到一个此前被忽略的关键点：**扩展点取的是「写方法的返回值」，不是实体**。

- 切面 `RealtimeSyncAspect` 用 `@AfterReturning(returning = "ret")` 解析防区；
- 但 46 个广播域的写方法绝大多数返回 **DTO / View**（`AlarmItem`、`GateControlItem`、
  `RescuePersonnelItem`、各类 `*View` 等），并非实体；
- 于是此前「给 30 个含 location 字段的**实体**实现 `ZoneAware`」这个思路**大部分是无效的**——
  实体实现了也进不了切面。真正生效的只有直接返回实体的 `FacDevice`。

## 变更内容

给 **18 个写方法返回 DTO** 接线 `ZoneAware`，覆盖 46 个广播域中的 18 个域：

- **走 `getLocation()`（经配置映射为防区）16 个**：AlarmItem、PatrolExecutionView、CommunicationDevice、
  EmergencyEventItem、EmergencyCaseItem、FireAlarmItem、FireFacilityLedgerItem、ProductionAlarmItem、
  FireBrigadeTeam、RescueVehicleItem（parkingLocation）、RescueEquipmentItem（storageLocation）、
  PersonSearchDetail（operationArea）、GateControlItem、PerimeterAlarmDetail、SpecialOperationItem、
  VideoCameraItem；
- **走 `getZoneName()`（直接持有防区名，跳过映射）2 个**：BollardItem（zone）、TvMonitorSummary（zoneName）。

新增 `ZoneAwareDtoWiringTest`（5 例）锁住「字段不漂移」与「未配置映射必须 fail-open」。
更新 `ZoneAware` 接口 Javadoc，写明「接线对象 = 写方法返回类型」与已接线清单。

## 范围与非目标

- **不改** `RealtimeBroadcastService` 的三态语义、**不改** `ZoneMappingResolver`、**不加**任何硬编码映射规则
  （location→防区 语义仍由产品在 `abac.zone-mapping.location-to-zones` 配置，代码不造业务规则）。
- **非目标**：
  - 不处理返回 `void` / `DeleteResult` 的删除类写方法——它们无法携带防区，天然 fail-open，属已知限制，已写入文档；
  - 不接线 `SystemUserItem`（`zoneCodes` 是多值逗号串，`ZoneAware` 只支持单防区名 / 单 location，
    强行取首个值会造成错误收紧）；
  - 不在本 Change 填生产映射规则（仍待产品定）。

## 兼容性

**行为不变的保证**：未配置映射或未命中 → `resolveZonesByLocation` 返回 null → 事件 `zones == null`
→ fail-open 推给全部已认证会话，与当前线上语义**完全一致**。
只有产品填入映射规则后，命中 location 的域才会按防区收紧（最小权限），且 ALL 用户
（`sessionZones == null`）始终不受影响。
