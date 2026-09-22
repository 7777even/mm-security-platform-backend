# Proposal: fire-alarm-disposal-persist

> 本 Change 是 `2026-09-22-add-fire-alarm-writeback` 的延续：原提案把「处置情况文本 / 处置时间 / 派单人员 / 通知方式」
> 列为非目标（"若后续要持久化须另立 Change 加列"）。本 Change 即激活该项——用户于 09-22 明确要求"一并持久化"。

## 问题
`add-fire-alarm-writeback` 落地后，消防报警的 `status` / `falseAlarm` 已落 `fac_fire_alarm`，
但详情面板里受众编辑的 4 个处置字段仍**只有前端内存态**：

- 处置情况文本 `handleResult`
- 处置时间 `handleTime`
- 派单人员 `dispatchPersonnel`（数组）
- 通知方式 `notifyApp` / `notifySms`（两个布尔）

刷新页面或重新打开详情即从后端回读不到这些处置信息，与"状态已落库"体验不一致；
且 `fireListItemToDetail` 无法回填，列表→详情往返丢失派单/通知/处置文本。

## 目标
在 `fac_fire_alarm` 新增 4 列，并把既有写回端点 `PUT /api/v1/fire-alarms/{alarmId}` 扩展为可一并持久化这 4 字段：

- 新增迁移 `V65__fire_alarm_disposal_fields.sql`（h2 / postgresql / dameng 三方言各一份），`ALTER TABLE fac_fire_alarm ADD COLUMN`：
  - `handle_result VARCHAR(1024)` 处置情况文本
  - `handle_time VARCHAR(32)` 处置时间（格式 yyyy-MM-dd HH:mm:ss）
  - `dispatch_personnel VARCHAR(512)` 派单人员（逗号分隔）
  - `notify_method VARCHAR(32)` 通知方式（APP/SMS 逗号分隔）
- `FireAlarmUpdateRequest` 增加 4 个可选字段（自由文本，无枚举约束，仅非空时覆盖）。
- `FireAlarmItem` 回显 4 字段；`FireAlarmService.update` read-modify-write 时一并 set。
- 复用既有 `perm=fire-alarm:ack` 与 `@RealtimeSync(domain="fire-alarm.alarm")`，**不新增端点 / 不改权限模型**。

## 非目标
- 不新增写端点、不改权限码、不改乐观锁机制。
- 不对 `handle_result` 等做字典/枚举约束（保持自由文本，与详情面板既有交互一致）。
- 不改只读分页接口契约结构（仅 `FireAlarmItem` 增字段、向后兼容）。

## 影响面（L4 数据库结构变更）
- 触及 `fac_fire_alarm` 表结构（新增 4 列）——属 L4，**需人工确认**。
- 确认结论：用户于 2026-09-22 明确要求"要一并持久化"，逆转原 writeback 提案边界；本 Change 即据此落地。
- 数据影响：仅 `ALTER TABLE ADD COLUMN`（可空，无存量回填、无 DML）；已应用 V5/V9 实例不受影响，V65 幂等追加。
- 回退：DROP 该 4 列（或整库回滚至 V64）即可；前端恢复为内存态（不读这 4 字段）。

## Capabilities
- `fire-alarm`（消防报警）：扩展"报警处置写回"能力，覆盖处置情况/时间/派单/通知方式持久化。
