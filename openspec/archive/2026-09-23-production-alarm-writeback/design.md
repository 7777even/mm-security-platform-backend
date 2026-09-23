# Design: production-alarm-writeback（后端）

## 数据流

```
确认 / 处置中 / 已处置 / 误报标记 / 派单人员 / 通知方式 / 处置情况
  └─ PUT /api/v1/production/alarms/{id}  (perm=production:ack, @Version 乐观锁)
       body: ProductionAlarmUpdateRequest { status?, falseAlarm?, handleResult?, handleTime?, dispatchPersonnel?, notifyMethod? }
            │
            ├─ selectById(id) → 不存在 → BusinessException(NOT_FOUND=404) → B3 包络
            ├─ 字典校验：status ∈ {未处置, 已确认, 处置中, 已处置}，falseAlarm ∈ {是, 否, 未核实}
            │         ── 非法 → BusinessException(PARAM_INVALID=100) → B3 包络
            ├─ read-modify-write：仅传非空字段覆盖到 FacProductionAlarm 实体
            ├─ updateById(e) → MyBatis-Plus @Version 自动比对 version 并自增
            │         ── 并发冲突（version 不匹配）→ OptimisticLockException → 由全局异常转 B3
            └─ 返回 toAlarm(e) 的 ProductionAlarmItem（含 5 个回填字段）
       @RealtimeSync(domain="production.alarm") → 发布 production.alarm.changed
```

## 关键决策

- **与消防 / 周界写回严格对等（L4）**：复用已落地的 `@Version` + 权限码种子 + DDL 三件套机制，仅字段名 / 字典不同。生产报警后端字段全中文，无英文枚举（status 直接存 `未处置/已确认/处置中/已处置`）。
- **V73 处置列 + `@Version` 乐观锁**：`version BIGINT NOT NULL DEFAULT 0`，在 `updateById` 时自动比对防护并发覆盖；其余 5 列均为可空自由文本 / 中文枚举，存量行自动填 NULL / 0。
- **V74 权限码种子**：`production:ack` 在 `fm-production` 菜单下登记 BUTTON 级 `perm_code`（code=`fm-production-alarm-ack`，sort_order=130），并授权六类角色（`sys_role_menu`），镜像 V68 消防 `fire-alarm:ack`、V70 周界 `security:perimeter-ack` 落地方式。注意 V33「ADMIN 全授权」为固定逻辑，新增按钮级 perm 必须显式回填 ADMIN + 5 角色，否则 `@RequireAuth(perm=...)` 对 ADMIN 也 403。
- **局部更新（read-modify-write）**：请求体全字段可选，仅传非空才覆盖；不传字段保持原值，避免「全量覆盖」误伤其他字段（如仅标记误报不应清空处置结果）。
- **字典校验在前**：status / falseAlarm 先校验再写入，非法值直接 `PARAM_INVALID`，不触达 DB；其余自由文本字段（handleResult / handleTime / dispatchPersonnel / notifyMethod）不校验。
- **新增「已确认」态**：原生产 KPI 仅 `未处置/处置中/已处置` 三态；详情面板统一态含「已确认」（对应消防 ACKED、周界已确认），故后端字典扩展为四态。详情态映射：`未确认→未处置`、`已确认→已确认`、`处理中→处置中`、`已处理→已处置`；反向回填同理。
- **KPI 口径联动**：「未处置告警」统计现聚合 `未处置 + 已确认`（已确认 = 已确认但未开始处置，仍属待办口径），与详情态映射一致，避免「已确认」告警从待办统计中消失。
- **实时复用既有切面**：`@RealtimeSync` 由 AOP 拦截，发布 `<domain>.changed` 到统一 `/ws/alarm` 总线，前端 `subscribeDomainChange` 去抖刷新——无需新增 WS topic。

## 三方言迁移说明

- `version BIGINT NOT NULL DEFAULT 0`：H2 / PostgreSQL 支持 `NOT NULL DEFAULT`；达梦 DM8 按 `flyway-multidialect-static-diff` 约定（`ALTER TABLE ... ADD version BIGINT DEFAULT 0`）。
- 5 个处置列均为可空（无 `NOT NULL`），三方言语法一致。
- 权限种子 V74 三方言一致（`sys_menu` + `sys_role_menu` INSERT）。
