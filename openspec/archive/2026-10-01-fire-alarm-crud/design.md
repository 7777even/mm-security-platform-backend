# Design: fire-alarm-crud

## 权限码决策（V90 种子）
`fac_fire_alarm` 的写入口此前仅有 `fire-alarm:ack`（V33 种入）。本次新增两个按钮级权限码：
- `fire-alarm:create`：在 `fm-fire` 父菜单下登记按钮级菜单 `fm-fire-create`（排序 120）。
- `fire-alarm:delete`：登记 `fm-fire-delete`（排序 121）。
两码授权 ADMIN / COMMANDER / SCHEDULER / TEAM_LEADER / INNER_OPER / OUTER_OPER（对齐 V33/V68/V76 授权口径）。
> V33 固化「ADMIN 全量授权」，但按钮级菜单需显式补 `sys_role_menu`，否则即便 ADMIN 也不持码、

  `@RequireAuth(perm=...)` 会 403——故种子必须同时写 `sys_role_menu`。

## 全字段更新 read-modify-write
`FireAlarmService.update(String alarmId, FireAlarmUpdateRequest req)` 在原 6 字段基础上扩到 19 字段：
`typeLabel/typeTone/source/objectType/objectName/level/description/location/time/falseAlarm/status/
rescueEventId/monitorId/monitorLabel/onsiteMonitorId/onsiteMonitorLabel/title/handleResult/handleTime/
dispatchPersonnel/notifyMethod`。每个字段 `if (req.getXxx() != null) e.setXxx(req.getXxx());` 局部覆盖，
未传字段保持原值。`status`/`falseAlarm` 保留枚举校验（非法抛 `BusinessException(PARAM_INVALID)`）。
实体带 `@Version`，MyBatis-Plus 自动 `WHERE version=?` 乐观锁，避免并发覆盖。

## 创建（create）
- `alarmId = "FA-" + UUID.randomUUID().toString().replace("-","").substring(0,18).toUpperCase()`。
- `version` 初值 0L；`status` 不传默认 `ACTIVE`（仍须经 `VALID_STATUS` 校验）。
- `title`/`time` 用 `@NotBlank` 必填校验，缺失返回 B3 `code=100`（参数非法）。

## 删除（delete）
- `selectById(alarmId)` 为 null 抛 `BusinessException(NOT_FOUND)`（B3 `code=404`，不抛 500）。
- 否则 `deleteById` **真删除**（非逻辑删除，与 `fac_fire_alarm` 无 `deleted` 业务列的现状一致）。

## 实时广播
`create`/`delete`/`update` 三个写方法均标 `@RealtimeSync(domain = "fire-alarm.alarm")`，写成功切面广播
`fire-alarm.alarm.changed`，与既有 `fire-alarm.patrol` 同族命名；前端 mgmt 列表、`/fire` 大屏共用该域。

## 契约同步
前端 `docs/api/fire-alarm.openapi.json`（唯一真源）新增 `post`/`delete` 节点与 `FireAlarmCreateRequest`
schema、`put` 的 `FireAlarmUpdateRequest` 扩至 19 字段；`gen:api-types` 重产 `src/types/generated/fire-alarm.ts`；
后端 `scripts/check-api-contract.mjs --strict` 已 PASS（路由差异 0 / schema 漂移 0）。
