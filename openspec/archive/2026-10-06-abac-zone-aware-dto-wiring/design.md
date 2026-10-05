# Design: 写方法返回 DTO 接线 ZoneAware

## 1. 扩展点到底读什么（先读代码再动手）

`RealtimeSyncAspect`：

```java
@AfterReturning(pointcut = "@annotation(realtimeSync)", returning = "ret")
public void afterWrite(JoinPoint jp, RealtimeSync realtimeSync, Object ret) {
    Set<String> zones = extractZones(ret);   // ← 取的是「返回值」
}

Set<String> extractZones(Object ret) {
    if (!(ret instanceof ZoneAware z)) return null;
    if (z.getZoneName() != null && !blank) return Set.of(z.getZoneName().trim());
    if (z.getLocation() != null && !blank) return zoneMappingResolver.resolveZonesByLocation(...);
    return null;
}
```

结论：**实体实现 `ZoneAware` 只有在该实体被写方法直接返回时才有效**。
扫 `@RealtimeSync` 全部写方法的返回类型（46 个域）后确认：除 `device` 域返回 `FacDevice` 外，
其余全部返回 DTO / View / `DeleteResult` / `void`。

## 2. 三态过滤语义（决定风险边界）

`RealtimeBroadcastService.broadcast`：

| 会话 zones | 事件 zones | 结果 |
| ---------- | ---------- | ---- |
| null（ALL 用户） | 任意 | 推送 |
| 非 null | null（未映射） | 推送（fail-open） |
| 非 null | 非 null | 仅交集命中才推送 |

`ZoneMappingResolver`：配置为空 / location 未命中 → 返回 **null**。

**因此本次接线的行为风险为零**：只要产品没填映射，`zones` 恒为 null，广播范围与现状完全相同；
填了之后才按防区收紧（这正是目标行为），且 ADMIN 等 ALL 用户始终不受影响。

## 3. 接线清单（18 个 DTO）

| DTO | 广播域 | 暴露方式 | 字段 |
| --- | ------ | -------- | ---- |
| AlarmItem | `alarm` | getLocation | location |
| PatrolExecutionView | `fire.patrol` | getLocation | location |
| CommunicationDevice | `communication.device` | getLocation | location |
| EmergencyEventItem | `emergency.event` | getLocation | location |
| EmergencyCaseItem | `emergency.case` | getLocation | location |
| FireAlarmItem | `fire-alarm.alarm` | getLocation | location |
| FireFacilityLedgerItem | `fire-facility.ledger` | getLocation | location |
| ProductionAlarmItem | `production.alarm` | getLocation | location |
| FireBrigadeTeam | `rescue.brigade` | getLocation | location |
| RescueVehicleItem | `rescue.vehicle` | getLocation | parkingLocation |
| RescueEquipmentItem | `rescue.equipment` | getLocation | storageLocation |
| PersonSearchDetail | `security.person-search` | getLocation | operationArea |
| GateControlItem | `security.gate-control` | getLocation | location |
| PerimeterAlarmDetail | `security.perimeter-alarm` | getLocation | location |
| SpecialOperationItem | `special-operation` | getLocation | location |
| VideoCameraItem | `video.camera` | getLocation | location |
| BollardItem | `security.bollard` | getZoneName | zone |
| TvMonitorSummary | `tv.monitor` | getZoneName | zoneName |

**刻意不接线的**：

- `SystemUserItem`（`system.user`）：`zoneCodes` 是多值逗号串，`ZoneAware` 只支持单防区名 / 单 location，
  取首个值会错误收紧账号管理域广播；
- 返回 `void` / `DeleteResult` / `long` / `int` / `List<*>` 的写方法（含全部删除操作）：无位置信息可携带，
  天然 fail-open，已在 `ZoneAware` Javadoc 中记为已知限制。

## 4. 测试设计

`ZoneAwareDtoWiringTest`（5 例）：

1. 18 个 DTO 全部 `instanceof ZoneAware`；
2. 暴露字段与预期一致（防后续重构把 `location` 改名导致静默失效）；
3. **未配置映射时 `resolveZonesByLocation` 必须返回 null**（锁死 fail-open，防止「有 location 就硬造防区」）；
4. 配置映射后命中 → 解析出防区集合（证明「填配置即生效、无需改代码」）；
5. 空白 / null location → null。

## 5. 剩余待办（不在本 Change）

- **产品侧**：给出 `location → 防区` 语义规则，填入 `abac.zone-mapping.location-to-zones`
  （dev 现有 6 条示例：厂区南门 / 厂区西门 / 码头区 / 乙烯装置区 / 芳烃罐区 / 特勤保障区）；
- 删除类写方法（返回 void）的防区归属：需另设计「删除前查一次实体防区」的机制，当前 fail-open。
