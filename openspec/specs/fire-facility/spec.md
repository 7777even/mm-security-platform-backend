# fire-facility Specification

## Purpose

消防设施（台账 / 故障 / 报警 / 运行监测 / 工单）的读写能力：承接 mgmt 管理端「消防设施」模块全量 CRUD
与大屏消防设施面板的只读展示，写操作经 `@RealtimeSync` 广播驱动三端实时刷新。

本 capability 的 Requirement 全部来自**已归档 Change 的 spec-delta 回填**（2026-10-06 治理批次），
来源：`openspec/archive/2026-10-01-fire-facility-fault-crud/`、`2026-10-03-mgmt-p2-fire-facility-patrol-crud/`、
`2026-09-23-fire-facility-monitor-report/`、`2026-10-04-fire-facility-alarm-handle/`。

## Endpoints

| Method | Path | 权限 | 说明 |
| ------ | ---- | ---- | ---- |
| GET | `/api/v1/fire-facility/ledger` | `fire-facility:handle` | 设施台账列表 |
| POST | `/api/v1/fire-facility/ledger` | `fire-facility:ledger:write` | 台账新增 |
| PUT | `/api/v1/fire-facility/ledger/{id}` | `fire-facility:ledger:write` | 台账编辑（覆盖更新，不含 `status`） |
| DELETE | `/api/v1/fire-facility/ledger/{id}` | `fire-facility:ledger:write` | 台账删除（级联清理维保记录后真删除） |
| POST | `/api/v1/fire-facility/ledger/{ledgerId}/maintenance` | `fire-facility:ledger:write` | 维保记录新增 |
| DELETE | `/api/v1/fire-facility/maintenance/{recordId}` | `fire-facility:ledger:write` | 维保记录删除 |
| GET | `/api/v1/fire-facility/faults` | `fire-facility:handle` | 故障列表 |
| POST | `/api/v1/fire-facility/faults` | `fire-facility:fault-create` | 故障台账录入 |
| PUT | `/api/v1/fire-facility/faults/{faultId}` | `fire-facility:handle` | 故障状态流转 + 基础字段局部更新 |
| DELETE | `/api/v1/fire-facility/faults/{faultId}` | `fire-facility:fault-delete` | 故障删除（级联清理时间线后真删除） |
| GET | `/api/v1/fire-facility/alarms` | `fire-facility:handle` | 报警列表（故障派生命名视图，`id` 形如 `AL-<故障号>`） |
| PUT | `/api/v1/fire-facility/alarms/{alarmId}` | `fire-facility:handle` | 按报警 id 处置（反查底层故障后复用故障处置语义） |
| GET | `/api/v1/fire-facility/monitors` | `fire-facility:handle` | 运行监测 12 类卡片 |
| POST | `/api/v1/fire-facility/monitors/report` | `fire-facility:handle` | 监测运行数据上报（按 `key_code` upsert） |
| GET | `/api/v1/fire-facility/work-orders` | `fire-facility:handle` | 工单列表 |

## Requirements

### Requirement: 消防设施台账 CRUD

系统 SHALL 提供 `POST /api/v1/fire-facility/ledger`（校验 `fire-facility:ledger:write`，设施编码重复返回 B3 `code=409`，
成功返回落库后的 `FireFacilityLedgerItem`）、`PUT /api/v1/fire-facility/ledger/{id}`（覆盖更新，**不含 `status`**，
不存在返回 404）、`DELETE /api/v1/fire-facility/ledger/{id}`（级联清理维保记录后真删除，不存在返回 404）。
三个写操作成功后 SHALL 广播 `fire-facility.ledger` 域变更事件。

#### Scenario: 台账新增广播
- **GIVEN** 操作员具备 `fire-facility:ledger:write`
- **WHEN** 提交 `POST /fire-facility/ledger`（`facilityCode="F001"`、`facilityName="消防水泵"`、`facilityType="消防水泵"`）
- **THEN** 落库返回带主键的 `FireFacilityLedgerItem`，并广播 `fire-facility.ledger.changed`

#### Scenario: 必填校验拦截
- **WHEN** 提交 `POST /fire-facility/ledger` 且 `facilityName` 为空串
- **THEN** 返回 B3 `code=400`，不写库、不广播

#### Scenario: void 删除仍广播
- **WHEN** `deleteLedger(id)` 执行成功
- **THEN** 尽管方法返回 void，仍 MUST 广播对应域变更事件（前端据此重拉）

### Requirement: 消防设施故障全量 CRUD

系统 SHALL 提供故障台账录入 `POST /api/v1/fire-facility/faults`（权限码 `fire-facility:fault-create`，
落库 `fac_fire_facility_fault` 并返回含空时间线的新建条目）与真删除
`DELETE /api/v1/fire-facility/faults/{faultId}`（权限码 `fire-facility:fault-delete`，
级联清理 `fac_fire_facility_fault_timeline` 后物理删除）。
`PUT /api/v1/fire-facility/faults/{faultId}` SHALL 在既有状态流转 + 派单/维修/验收字段之外，
额外支持 facilityName / facilityType / faultType / faultLevel / discoverTime / discoverMethod /
phenomenon / cause 的局部更新。三个写方法均 SHALL 标注 `@RealtimeSync(domain = "fire-facility.fault")`。

- 故障级别枚举固定为 紧急 / 重要 / 一般；
- 故障状态枚举固定为 待确认 / 已确认 / 已派单 / 维修中 / 待验收 / 已闭环；
- 所有失败路径走 B3 包络：参数非法 100、不存在 404、编号冲突 409。

### Requirement: 按报警 id 处置消防设施报警

后端 SHALL 提供 `PUT /api/v1/fire-facility/alarms/{alarmId}`，`alarmId` 为报警列表返回的
`AL-<故障号数字部分>` 字符串；以 `REPLACE(REPLACE(fault_code,'FLT-',''),'-','') = <数字串>` 反查底层故障
（数字串仅保留数字字符；空数字串 → `PARAM_INVALID`，未命中 → `NOT_FOUND`），
复用故障处置语义（状态流转受既有故障状态枚举约束、派单/维修/验收字段**局部更新**、可选追加故障时间线），
要求权限码 `fire-facility:handle`，成功后返回更新后的 `FireFacilityFaultItem` 并触发 `fire-facility.fault` 实时广播。

> 实现约束（内部，非对外契约）：故障写回核心逻辑 SHALL 收敛为 `doUpdateFault`，由 `updateFault` 与
> `updateAlarm` 共用，保证两入口行为一致且各自触发一次实时广播（Spring 代理型切面不拦截自调用，
> 故不得在同类内互调标注方法）。

### Requirement: 消防设施运行监测上报

系统 SHALL 提供 `POST /api/v1/fire-facility/monitors/report`，接收设备/采集/模拟上报，
按 `key_code` upsert `fac_fire_facility_monitor`（total/online/offline/fault/monitor_status/last_report_time）
并整体替换 `fac_fire_facility_param`，返回刷新后全量 `FireFacilityMonitorResult`。

- 请求体 SHALL 含非空 `items`；每项 `key` 非空、`status` ∈ {`正常`,`告警`,`离线`,`在线`}、计数非负；
  新建项 `facilityType` 必填；任一不过 → B3 `code=100`；
- 端点 SHALL 要求权限码 `fire-facility:handle`，`@RealtimeSync(domain="fire-facility.monitor")` 广播
  `fire-facility.monitor.changed`，复用统一 `/ws/alarm` 总线；
- upsert SHALL 命中即局部更新、未命中即 insert（`sort_no=max+1`，计数缺省 0，status 缺省 正常），
  `last_report_time` 每次上报刷新；
- 传 `params` SHALL 先 `DELETE` 旧参数再按顺序 `INSERT`，实现整体替换快照。

#### Scenario: 上报已有监测点
- **GIVEN** `key_code=MON-001` 已存在监测点（online=10 / total=12）
- **WHEN** 以 `{"items":[{"key":"MON-001","online":11,"total":12}]}` 请求上报
- **THEN** 该行 online 更新为 11，响应 `code=0` 且全部卡片刷新

#### Scenario: 上报新监测点
- **WHEN** 以含新建项（带 `facilityType`）的请求上报
- **THEN** 插入新行（`sort_no=max+1`，status 缺省 正常），返回刷新后全量

#### Scenario: 参数非法
- **WHEN** 请求 items 中 `status` 为非枚举值或计数为负
- **THEN** 返回 B3 `code=100`，不写库

### Requirement: 零下行控制

消防设施台账 / 故障 / 监测的写请求 DTO SHALL NOT 含 `status` 字段；`status` 为设备实时状态，仅读不写，
系统 MUST NOT 接受对 `status` 的写回，亦 MUST NOT 因写操作触发任何物理设备下行。

#### Scenario: 零下行控制
- **WHEN** 任何台账/故障写请求携带 `status` 字段
- **THEN** 该字段 MUST 被忽略（不在 DTO 内），设备实时状态不被覆盖
