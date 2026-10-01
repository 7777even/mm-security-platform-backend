# Proposal: fire-alarm-crud

## 问题
消防报警（`/fire` 大屏、管理端消防报警列表）此前只有**只读** `GET /api/v1/fire-alarms`
与**处置写回** `PUT /api/v1/fire-alarms/{alarmId}`（权限码 `fire-alarm:ack`，仅 2 字段
`status`/`falseAlarm`）。存在两处能力缺口：

1. **无新增 / 删除入口**：管理后台无法手工增录报警、也无法销条（如测试数据、误录）。
2. **PUT 字段过窄**：业务字段（`location`/`objectType`/`objectName`/`description`/`handleResult`/
   `handleTime`/`dispatchPersonnel`/`notifyMethod` 等）无法经后端编辑落库，仅能在前端会话态保留，
   刷新即丢，且与真实记录脱节。

## 目标
扩展既有 `FireAlarmController`/`FireAlarmService`，把消防报警做成**全套 CRUD**：

- 新增 `POST /api/v1/fire-alarms`（`fire-alarm:create`）：创建消防报警，`title`/`time` 必填，
  `status` 不传默认 `ACTIVE`，返回创建后的 `FireAlarmItem`。
- 新增 `DELETE /api/v1/fire-alarms/{alarmId}`（`fire-alarm:delete`）：**真删除**，不存在返回 B3 `code=404`。
- 把现有 `PUT` 扩为**全字段局部更新**（19 字段，逐字段非空覆盖），权限码保持 `fire-alarm:ack` 不变。
- 三方法均 `@RealtimeSync(domain = "fire-alarm.alarm")`，写成功广播 `fire-alarm.alarm.changed`，
  前端 mgmt 列表与大屏 `/fire` 经 WS 在 400ms 去抖后自动重拉。
- V90 三方言权限种子：在 `fm-fire` 父菜单下登记按钮级菜单 `fire-alarm:create`/`fire-alarm:delete`，
  授权 ADMIN 及五类岗位角色，使 `@RequireAuth(perm=...)` 不 403。

## 非目标
- 不新增 / 修改 `fac_fire_alarm` 表结构（现有列已覆盖本次全字段，`alarm_id` 字符串主键 + `@Version`
  乐观锁已落地，V5/V9 既有）。
- 不动 `PUT` 的权限码（保持 `fire-alarm:ack`，与大屏既有调用语义一致）。
- 不改只读分页接口与 `FireAlarmItem` 字段集（`alarmId` 等基础字段维持）。

## 影响面
- 新增 `dto/FireAlarmCreateRequest.java`；`FireAlarmUpdateRequest.java` 扩为 19 字段（全可选，局部更新）。
- `FireAlarmService` 新增 `create(req)` / `delete(alarmId)`，并把 `update` 的 read-modify-write 块从
  6 字段扩到 19 字段（status/falseAlarm 保留枚举校验）。
- `FireAlarmController` 新增 `@PostMapping("/fire-alarms")` + `@DeleteMapping("/fire-alarms/{alarmId}")`。
- 三方言迁移 `V90__fire_alarm_write_perm.sql`（h2/postgresql/dameng 同名同号）。
- 契约四同步：前端 `docs/api/fire-alarm.openapi.json` 新增 `post`/`delete` 与 `FireAlarmCreateRequest`、
  `put` 的 `FireAlarmUpdateRequest` 扩全字段，并由 `gen:api-types` 重产 TS 类型（已在 Task 6 完成）。
- 数据影响：仅 `fac_fire_alarm` 的增 / 改 / 删，无 DDL、无存量迁移。
- 回退：移除 POST/DELETE 端点与 V90 种子、回退 `FireAlarmUpdateRequest` 至 2 字段即可；前端恢复为只读 + 内存态。

## Capabilities
- `fire-alarm`（消防报警）：新增「报警新增 / 删除 / 全字段编辑」能力，并复用既有「处置写回」实时广播域。
