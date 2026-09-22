# Design: add-fire-alarm-writeback

## 枚举映射决策

| 详情中文态（前端） | 后端 status | 字典 `fire_alarm_status` 标签 |
|---|---|---|
| 未确认 | `ACTIVE` | 待处理 |
| 已确认 | `ACKED` | 已确认 |
| 处理中 | `DISPATCHED` | 已派单 |
| 已处理 | `CLOSED` | 已闭环 |

误报：`是` / `否` / `未核实`（字典 `fire_alarm_false`）。

## 架构

- **新增 `dto/FireAlarmUpdateRequest`**：`status`（可空）+ `falseAlarm`（可空），局部更新语义。
- **`FireAlarmService.update(String alarmId, FireAlarmUpdateRequest req)`**：
  1. `fireAlarmMapper.selectById(alarmId)`；为 null 抛 `BusinessException(ResultCode.NOT_FOUND, ...)`。
  2. 非空字段先校验枚举（非法抛 `BusinessException(ResultCode.PARAM_INVALID, ...)`），再覆盖。
  3. `fireAlarmMapper.updateById(e)`——实体带 `@Version`，MyBatis-Plus 自动 `WHERE version=? AND version=version+1`；
     read-modify-write 保证并发下不丢更新，冲突由 MP 抛乐观锁异常（B3 兜底）。
  4. 返回 `toItem(e)` 供前端即时回填。
- **`FireAlarmController`** 新增 `@PutMapping("/fire-alarms/{alarmId}")`，
  `@RequireAuth(perm = "fire-alarm:ack")`（复用 V33 已种入的 `fire-alarm:ack` 权限码，不新增菜单项）。
- **`@RealtimeSync(domain = "fire-alarm.alarm")`**：写成功后切面广播 `fire-alarm.alarm.changed`，
  与既有 `fire-alarm.patrol` 同族命名。

## 为什么不加「处置情况文本/时间」列
`fac_fire_alarm` 现有列（V5）无 `handle_result` / `handle_time`，本次坚持「不改表结构」的最小面；
前端这两项保持会话态。若业务需要留痕，另立 Change 加列 + 迁移，避免本次写回把 DDL 与业务耦合。

## 契约同步
- 新增 path `PUT /api/v1/fire-alarms/{alarmId}` 与 schema `FireAlarmUpdateRequest`，写进
  `frontend-scaffold/docs/api/fire-alarm.openapi.json`（唯一真源）；同时修正 `FireAlarmItem.status`
  描述为 4 态（原仅写 ACTIVE/CLOSED，与字典不符）。
- 跑 `scripts/check-api-contract.mjs`：FireAlarmItem 18 字段、FireAlarmUpdateRequest 2 字段均「对齐」，
  路由差异 0。
