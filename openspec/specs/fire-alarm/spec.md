# fire-alarm Specification

## Purpose

消防报警（区别于通用告警 `/api/v1/alarms` 与生产报警）的列表、新增、处置写回与删除能力。
列表供大屏消防钻取弹窗与管理端展示；处置写回用于确认 / 派单 / 闭环与误报标记。

> 口径提醒：消防报警 **不是** `/api/v1/alarms`（通用告警流）的同义词——后者与大屏报警面板 / 移动端告警
> 同源，其 `level` 为 GDS 阈值文本；本域 `status` 使用 `ACTIVE/ACKED/DISPATCHED/CLOSED` 枚举。
> 三端口径详见 `docs/architecture/alarm-three-surfaces.md`。

本 capability 的 Requirement 来自**已归档 Change 的 spec-delta 回填**（2026-10-06 治理批次），
来源：`openspec/archive/2026-09-22-add-fire-alarm-writeback/`、
`openspec/archive/2026-09-22-fire-alarm-disposal-persist/`、
`openspec/archive/2026-10-01-fire-alarm-crud/`。

## Endpoints

| Method | Path | 权限 | 说明 |
| ------ | ---- | ---- | ---- |
| GET | `/api/v1/fire-alarms` | 登录即可 | 消防报警列表 |
| POST | `/api/v1/fire-alarms` | `fire-alarm:create` | 报警新增 |
| PUT | `/api/v1/fire-alarms/{alarmId}` | `fire-alarm:ack` | 处置写回（全字段局部更新，19 字段） |
| DELETE | `/api/v1/fire-alarms/{alarmId}` | `fire-alarm:delete` | 报警删除（真删除） |

## Requirements

### Requirement: 消防报警新增

系统 SHALL 提供 `POST /api/v1/fire-alarms` 创建消防报警。

- 请求体 SHALL 含必填 `title`（报警名称）、`time`（报警时间，`yyyy-MM-dd HH:mm:ss`）；其余字段可选；
- `title` / `time` 为空 → B3 `code=100`（参数非法）；
- `status` 传入但非 `{ACTIVE,ACKED,DISPATCHED,CLOSED}` → B3 `code=100`；
- 成功 SHALL 落 `fac_fire_alarm`，返回创建后的 `FireAlarmItem`（B3 包络，`alarmId` 非空），
  并广播实时域 `fire-alarm.alarm`。

#### Scenario: 新增一条报警
- **GIVEN** 角色持 `fire-alarm:create` 权限
- **WHEN** 以 `{"title":"联动测试报警","time":"2026-10-01 21:00:00","typeLabel":"火灾报警","status":"ACTIVE"}` 请求 `POST /fire-alarms`
- **THEN** 响应 `code=0` 且 `data.alarmId` 非空（形如 `FA-...`），并广播 `fire-alarm.alarm.changed`

#### Scenario: 缺必填字段
- **WHEN** 以 `{"time":"2026-10-01 21:00:00"}`（无 title）请求 `POST /fire-alarms`
- **THEN** 返回 B3 `code=100`，不写库

### Requirement: 消防报警处置写回

系统 SHALL 提供 `PUT /api/v1/fire-alarms/{alarmId}`，对单条消防报警做处置状态流转或误报标记。

- 请求体 SHALL 为**局部更新**：仅非空字段被写入，未传字段保持不变；
- `status` ∈ {`ACTIVE`, `ACKED`, `DISPATCHED`, `CLOSED`}（对齐字典 `fire_alarm_status`）；
- `falseAlarm` ∈ {`是`, `否`, `未核实`}（对齐字典 `fire_alarm_false`）；
- 端点 SHALL 要求权限码 `fire-alarm:ack`；未授权返回 B3 `code=403`；
- `alarmId` 不存在 → B3 `code=404`（不抛 500）；枚举非法 → B3 `code=100`；
- 更新 SHALL 使用实体 `@Version` 乐观锁，避免并发覆盖；
- 成功 SHALL 返回更新后的 `FireAlarmItem`（B3 包络）并广播实时域 `fire-alarm.alarm`。

请求体 SHALL 支持 4 个处置字段（`FireAlarmUpdateRequest`，均可选、向后兼容旧客户端）：
`handleResult`（处置情况文本）、`handleTime`（处置时间 `yyyy-MM-dd HH:mm:ss`）、
`dispatchPersonnel`（派单人员，逗号分隔）、`notifyMethod`（通知方式，APP/SMS 逗号分隔）；
对应 `fac_fire_alarm` 的 4 列由迁移 V65（三方言）建立。

`PUT /api/v1/fire-alarms/{alarmId}` 的请求体 SHALL 扩为**全字段局部更新（19 字段，原 2 字段）**：
仅非空字段被写入，未传字段保持不变；权限码保持 `fire-alarm:ack` 不变。

#### Scenario: 确认报警
- **GIVEN** 一条 `status=ACTIVE` 的报警 `FA-20260907-001`
- **WHEN** 以 `{"status":"ACKED"}` 请求 `PUT /fire-alarms/FA-20260907-001`
- **THEN** 该行 `status` 变为 `ACKED`，响应 `code=0` 且 `data.status=ACKED`，并广播 `fire-alarm.alarm.changed`

#### Scenario: 标记误报
- **GIVEN** 一条报警记录
- **WHEN** 以 `{"falseAlarm":"是"}` 请求写回
- **THEN** 仅 `falseAlarm` 变为 `是`，`status` 保持不变

#### Scenario: 报警不存在
- **WHEN** 请求 `PUT /fire-alarms/UNKNOWN`
- **THEN** 返回 B3 `code=404`，不写库

#### Scenario: 非法枚举
- **WHEN** 请求 `PUT /fire-alarms/FA-...`，`status` 为非枚举值
- **THEN** 返回 B3 `code=100`，不写库

### Requirement: 消防报警删除

系统 SHALL 提供 `DELETE /api/v1/fire-alarms/{alarmId}` 真删除该条报警。

- 端点 SHALL 要求权限码 `fire-alarm:delete`；未授权返回 B3 `code=403`；
- `alarmId` 不存在 → B3 `code=404`，不写库；
- 成功 SHALL 广播实时域 `fire-alarm.alarm`。

#### Scenario: 删除不存在的报警
- **WHEN** 请求 `DELETE /fire-alarms/UNKNOWN`
- **THEN** 返回 B3 `code=404`，不写库
