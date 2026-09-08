# alarm-domain Specification

## Purpose

报警/应急事件域：对外契约字段与事件 CRUD。由 Change `align-alarm-contract-fields`、`implement-remaining-contracts`（均已归档）回填。

## Requirements

### Requirement: 报警列表对外字段与契约一致

`GET /api/v1/alarms` 响应 `data.list[]` 元素须与前端 `alarm.openapi.json` 的 `AlarmItem` 完全一致，禁止直接序列化 `FacAlarm` 实体。

#### Scenario: 对外字段结构

- **WHEN** 调用 `GET /api/v1/alarms`
- **THEN** 列表元素包含 `alarmId`（业务 ID，替代 Long 主键）、`status`（string 枚举 ACTIVE/ACKED/DISPATCHED/CLOSED）、`ts`、`description`、`location`、`category`、`warned`、`planId`
- **AND** 不对外暴露 `id`(Long)、`content`、`occurred_at`、`created_at`、`deleted`

### Requirement: 报警事件增删改

系统须提供报警事件的创建、更新与逻辑删除能力，响应统一 B3 包络。

#### Scenario: 创建应急事件

- **WHEN** `POST /api/v1/alarms` 携带 EmergencyEventPayload
- **THEN** 创建报警记录并以 B3 包络返回 AlarmItem

#### Scenario: 更新应急事件

- **WHEN** `PUT /api/v1/alarms/{alarmId}` 携带更新内容
- **THEN** 更新对应记录并以 B3 包络返回；目标不存在时 `data=null`

#### Scenario: 逻辑删除应急事件

- **WHEN** `DELETE /api/v1/alarms/{alarmId}`
- **THEN** 逻辑删除对应记录，B3 包络返回 `data={ok:boolean}`

### Requirement: 报警状态机

报警 `status` 由 int 存储映射为 string 枚举 ACTIVE / ACKED / DISPATCHED / CLOSED，沿确认流（ACK_FLOW）逐格推进。

#### Scenario: 状态映射

- **WHEN** 读取或写入报警状态
- **THEN** 对外仅暴露四个枚举值，不泄漏 int 存储值
