# Proposal: add-fire-alarm-writeback

## 问题
消防报警（`/fire` 大屏、管理端消防报警列表）此前只有**只读**接口 `GET /api/v1/fire-alarms`，
后端**没有任何写回端点**。前端「告警详情」面板的状态机（确认 → 开始处置 → 提交处置 / 标记误报）
只调用本地 `patchAlarmDetail` 改内存态，刷新即丢——用户实测「确认了一条报警，状态没有落库」。

同时前端 `fireListItemToDetail` 将后端 4 态（`ACTIVE/ACKED/DISPATCHED/CLOSED`）压缩为
「非 CLOSED 一律未确认」，详情页与真实记录**状态脱节**（数据没联通）。

## 目标
新增 `PUT /api/v1/fire-alarms/{alarmId}`，把处置状态与误报标记**落 fac_fire_alarm 真实表**：
- 请求体 `FireAlarmUpdateRequest`（可选字段局部更新）：`status`（ACTIVE/ACKED/DISPATCHED/CLOSED）、
  `falseAlarm`（是/否/未核实），枚举对齐字典 `fire_alarm_status` / `fire_alarm_false`。
- 需权限码 `fire-alarm:ack`；成功后广播实时域 `fire-alarm.alarm`（写方法统一带 `@RealtimeSync`）。
- read-modify-write + 实体既有 `@Version` 乐观锁；记录不存在返回 B3 `code=404`，枚举非法返回 `code=100`。
- 成功返回更新后的 `FireAlarmItem`（B3 包络）。

## 非目标
- 不新增/修改 `fac_fire_alarm` 表结构（`status`/`falseAlarm`/`version` 列已存在，V5+V9 落地）。
- 不落「处置情况文本 / 处置时间 / 派单人员 / 通知方式」——现有表无对应列，本次保持前端会话态；
  若后续要持久化须另立 Change 加列。
- 不动 fire-alarm 只读分页接口与 `FireAlarmItem` 字段集。

## 影响面
- 新增 `dto/FireAlarmUpdateRequest.java`；`FireAlarmService` 新增 `update(alarmId, req)`；
  `FireAlarmController` 新增 `@PutMapping("/fire-alarms/{alarmId}")` + `@RequireAuth(perm="fire-alarm:ack")`。
- 契约同步：`frontend-scaffold/docs/api/fire-alarm.openapi.json` 新增该 path 与 `FireAlarmUpdateRequest`
  schema，并修正 `FireAlarmItem.status` 描述为 4 态——**同一次交付内完成**（四同步 step 2）。
- 数据影响：仅更新既有行的 `status`/`falseAlarm`/`version`，无 DDL、无存量迁移。
- 回退：删除该 path 与 DTO/service/controller 方法即可，前端恢复为内存态（不落库）。

## Capabilities
- `fire-alarm`（消防报警）：新增「报警处置写回」能力。
