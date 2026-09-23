# Design: perimeter-alarm-writeback（后端）

## 数据流

```
确认 / 派单 / 处置 / 误报标记 / 派单人员 / 通知方式
  └─ PUT /api/v1/security/perimeter-alarms/{id}  (perm=security:perimeter-ack, @Version 乐观锁)
       body: PerimeterAlarmUpdateRequest { status?, falseAlarm?, handleResult?, handleTime?, dispatchPersonnel?, notifyApp?, notifySms? }
            │
            ├─ selectById(id) → 不存在 → BusinessException(NOT_FOUND) → B3 包络
            ├─ 字典校验：status ∈ {未确认,已确认,已派单,已处理}，falseAlarm ∈ {是,否,未核实}
            │         ── 非法 → BusinessException(PARAM_INVALID=100) → B3 包络
            ├─ read-modify-write：仅传非空字段覆盖到 FacPerimeterAlarm 实体
            ├─ updateById(e) → MyBatis-Plus @Version 自动比对 version 并自增
            │         ── 并发冲突（version 不匹配）→ OptimisticLockException → 由全局异常转 B3
            └─ 返回 toPerimeterAlarmDetail(e) 的 PerimeterAlarmDetail
       @RealtimeSync(domain="security.perimeter-alarm") → 发布 security.perimeter-alarm.changed
```

## 关键决策

- **与消防报警写回严格对等（L4）**：消防报警写回是已落地的范式参照（V67/V68 同款 @Version + 权限码种子 + DDL）。周界写回复用同一套机制，仅字段名/字典不同（`fac_perimeter_alarm` 用中文状态 `未确认/已确认/已派单/已处理`，消防用 `ACTIVE/ACKED/DISPATCHED/CLOSED`）。
- **V69 `@Version` 乐观锁**：与 `FacFireAlarm` 对齐，在 `updateById` 时自动比对 `version` 列，防护多端同时确认/处置导致的覆盖丢失；新增列 `NOT NULL DEFAULT 0`，存量行自动填 0。
- **V70 权限码种子**：`security:perimeter-ack` 在 `fm-security` 菜单下登记 BUTTON 级 `perm_code`（code=`fm-security-perimeter-ack`，sort_order=130），并授权六类角色（`sys_role_menu`），镜像 V68 消防报警的 `fire-alarm:ack` 落地方式。
- **局部更新（read-modify-write）**：请求体全字段可选，仅传非空才覆盖；不传字段保持原值，避免「全量覆盖」误伤其他字段（如仅标记误报不应清空处置结果）。
- **字典校验在前**：status / falseAlarm 先校验再写入，非法值直接 `PARAM_INVALID`，不触达 DB；其余自由文本字段（handleResult / handleTime / dispatchPersonnel / notifyApp / notifySms）不校验。
- **实时复用既有切面**：`@RealtimeSync` 由 AOP 拦截，发布 `<domain>.changed` 到统一 `/ws/alarm` 总线，前端 `subscribeDomainChange` 去抖刷新——无需新增 WS topic。
- **返回更新后详情**：写接口直接返回 `PerimeterAlarmDetail`，供调用方（AlarmDetailPanel）即时回填，与消防 `updateFireAlarm` 同源。

## 三方言迁移说明

- `version BIGINT NOT NULL DEFAULT 0`：H2 / PostgreSQL 支持 `NOT NULL DEFAULT`；达梦 DM8 `ALTER TABLE ... ADD version BIGINT DEFAULT 0`（DM 不支持列级 NOT NULL 与 DEFAULT 同写时语义差异，按 `flyway-multidialect-static-diff` 约定处理）。
- 权限种子 V70 三方言一致（`sys_menu` + `sys_role_menu` INSERT）。
