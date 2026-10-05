# security Specification

## Purpose

安全防恐能力：道闸 / 防恐柱设备台账、人员与车辆识别检索记录、周界入侵告警、安防事件、巡检摄像头、
人员轨迹，以及安防黑名单（车辆 / 人员）。承接 mgmt 管理端「安防」模块全量 CRUD 与大屏安防面板的只读消费。

> 周界告警的独立能力规约见 `perimeter-alarm`；本 capability 覆盖 `/api/v1/security/*` 全部端点
> （含 `/security/perimeter-alarms/*` 与 `/security/blacklist/*`，二者在前端契约中分属
> `security` / `security-blacklist` 两个域文件，能力上同属安全防恐）。

本 capability 的 Requirement 来自**已归档 Change 的 spec-delta 回填**（2026-10-06 治理批次），
来源：`openspec/archive/2026-10-03-mgmt-p2-gate-bollard-crud/`、
`openspec/archive/2026-10-03-mgmt-p2-security-crud/`、`2026-10-03-mgmt-p2-perimeter-maintenance-closure/`。

## Endpoints

| Method | Path | 权限 | 说明 |
| ------ | ---- | ---- | ---- |
| GET | `/api/v1/security/gate-controls` | 登录即可 | 道闸台账列表 |
| POST | `/api/v1/security/gate-controls` | `security:gate-write` | 道闸新增 |
| PUT | `/api/v1/security/gate-controls/{id}` | `security:gate-write` | 道闸编辑（覆盖更新，不含 `status`） |
| DELETE | `/api/v1/security/gate-controls/{id}` | `security:gate-write` | 道闸删除（真删除） |
| GET | `/api/v1/security/bollards` | 登录即可 | 防恐柱台账列表 |
| POST | `/api/v1/security/bollards` | `security:bollard-write` | 防恐柱新增 |
| PUT | `/api/v1/security/bollards/{id}` | `security:bollard-write` | 防恐柱编辑（覆盖更新，不含 `status`） |
| DELETE | `/api/v1/security/bollards/{id}` | `security:bollard-write` | 防恐柱删除（真删除） |
| GET | `/api/v1/security/search/person` | 登录即可 | 人员识别检索列表 |
| POST | `/api/v1/security/search/person` | `security:person-write` | 人员检索记录新增 |
| GET | `/api/v1/security/search/person/{id}` | 登录即可 | 人员检索详情 |
| PUT | `/api/v1/security/search/person/{id}` | `security:person-write` | 人员检索记录编辑（全字段覆盖） |
| DELETE | `/api/v1/security/search/person/{id}` | `security:person-write` | 人员检索记录删除（真删除） |
| GET | `/api/v1/security/search/vehicle` | 登录即可 | 车辆识别检索列表 |
| POST | `/api/v1/security/search/vehicle` | `security:vehicle-write` | 车辆检索记录新增 |
| GET | `/api/v1/security/search/vehicle/{id}` | 登录即可 | 车辆检索详情 |
| PUT | `/api/v1/security/search/vehicle/{id}` | `security:vehicle-write` | 车辆检索记录编辑（`confidence` 允许为空） |
| DELETE | `/api/v1/security/search/vehicle/{id}` | `security:vehicle-write` | 车辆检索记录删除（真删除） |
| GET | `/api/v1/security/perimeter-alarms` | 登录即可 | 周界入侵告警列表 |
| GET | `/api/v1/security/perimeter-alarms/latest` | 登录即可 | 最新周界告警（大屏周界面板） |
| GET | `/api/v1/security/perimeter-alarms/{id}` | 登录即可 | 周界告警详情 |
| GET | `/api/v1/security/perimeter-alarms/{id}/snapshot` | 登录即可 | 周界告警抓拍图（无抓拍返回 404 属设计） |
| POST | `/api/v1/security/perimeter-alarms` | `security:perimeter-create` | 周界告警新增 |
| PUT | `/api/v1/security/perimeter-alarms/{id}` | `security:perimeter-ack` | 周界告警确认 / 处置写回 |
| DELETE | `/api/v1/security/perimeter-alarms/{id}` | `security:perimeter-delete` | 周界告警删除（真删除） |
| GET | `/api/v1/security/events` | 登录即可 | 安防事件列表 |
| GET | `/api/v1/security/patrol-cameras` | 登录即可 | 巡检摄像头（含防区，供巡更联动复用） |
| GET | `/api/v1/security/track/summary` | 登录即可 | 人员轨迹汇总 |
| GET | `/api/v1/security/track/timeline` | 登录即可 | 人员轨迹时间线 |
| GET | `/api/v1/security/blacklist` | `role:ADMIN` | 安防黑名单（车辆 + 人员） |
| DELETE | `/api/v1/security/blacklist/vehicles/{id}` | `role:ADMIN` | 移除黑名单车辆 |
| DELETE | `/api/v1/security/blacklist/persons/{id}` | `role:ADMIN` | 移除黑名单人员 |

## Requirements

### Requirement: 道闸与防恐柱台账 CRUD

系统 SHALL 提供道闸 `POST / PUT / DELETE /api/v1/security/gate-controls[/ {id}]`
（权限码 `security:gate-write`，`name` 缺失或为空返回 400 校验失败，PUT 覆盖更新**不含 `status`**，
`@Version` 冲突返回 409，DELETE 真删除且不存在返回 404）与防恐柱
`POST / PUT / DELETE /api/v1/security/bollards[/ {id}]`（权限码 `security:bollard-write`，约束相同）。
六个写操作成功后 SHALL 分别广播 `security.gate-control` / `security.bollard` 域变更事件。

> 主键约束：`FacGateControl` / `FacBollard` 为 `@TableId(ASSIGN_ID)`，写端点不得依赖数据库自增，
> 否则主键为空会返回 409。

#### Scenario: 道闸新增广播
- **GIVEN** 操作员具备 `security:gate-write`
- **WHEN** 提交 `POST /security/gate-controls`（`name="1#门-道闸1"`）
- **THEN** 落库返回带主键的 `GateControlItem`，并广播 `security.gate-control.changed`

#### Scenario: 必填校验拦截
- **WHEN** 提交 `POST /security/bollards` 且 `name` 为空串
- **THEN** 返回 B3 `code=400`，不写库、不广播

### Requirement: 人员与车辆识别检索记录 CRUD

系统 SHALL 提供人员检索 `POST / PUT / DELETE /api/v1/security/search/person[/ {id}]`
（权限码 `security:person-write`，`name` 缺失或为空返回 400，PUT 全字段覆盖）与车辆检索
`POST / PUT / DELETE /api/v1/security/search/vehicle[/ {id}]`（权限码 `security:vehicle-write`，
`plate` 缺失或为空返回 400，`confidence` 允许为空）。写操作成功后 SHALL 广播
`security.person-search` / `security.vehicle-search` 域变更事件。

#### Scenario: 人员登记新增广播
- **GIVEN** 操作员具备 `security:person-write`
- **WHEN** 提交 `POST /security/search/person`（`name="张三"`）
- **THEN** 落库返回带主键的 `PersonSearchDetail`，并广播 `security.person-search.changed`

### Requirement: 周界入侵告警写端点

系统 SHALL 提供周界告警新增 `POST /api/v1/security/perimeter-alarms`
（`security:perimeter-create`）、处置写回 `PUT /api/v1/security/perimeter-alarms/{id}`
（`security:perimeter-ack`）、真删除 `DELETE /api/v1/security/perimeter-alarms/{id}`
（`security:perimeter-delete`，不存在返回 404）；成功后 SHALL 广播 `security.perimeter-alarm` 域变更事件。

周界告警抓拍 `GET /api/v1/security/perimeter-alarms/{id}/snapshot` **无抓拍返回 404 属设计**（非空态兜底）。

### Requirement: 安防黑名单移除

系统 SHALL 提供 `DELETE /api/v1/security/blacklist/vehicles/{id}` 与
`DELETE /api/v1/security/blacklist/persons/{id}`（`role:ADMIN`）用于移除黑名单车辆 / 人员；
前端无 `VITE_API_BASE` 时保持本地移除，失败不得假成功。

### Requirement: 乐观锁

`fac_gate_control` / `fac_bollard` / `fac_person_search` / `fac_vehicle_search` SHALL 带 `version` 列，
更新走 `@Version` 乐观锁，并发冲突返回 409，禁止静默后写覆盖。

#### Scenario: 并发编辑冲突
- **GIVEN** 两条 `PUT /security/search/person/{id}` 携带同一 `version`
- **WHEN** 第二条提交
- **THEN** 返回 409，库内记录保持第一次提交的结果

#### Scenario: void 删除仍广播
- **WHEN** `deleteGate(id)` / `deleteBollard(id)` / `deletePerson(id)` / `deleteVehicle(id)` /
  `deletePerimeterAlarm(id)` 执行成功
- **THEN** 尽管方法返回 void，仍 MUST 广播对应域变更事件（前端据此重拉）

### Requirement: 零下行控制

道闸 / 防恐柱写请求 DTO SHALL NOT 含 `status` 字段；`status` 为设备实时状态，仅读不写，
系统 MUST NOT 接受对 `status` 的写回，亦 MUST NOT 因写操作触发任何物理设备下行。

#### Scenario: 零下行控制
- **WHEN** 任何道闸 / 防恐柱写请求携带 `status` 字段
- **THEN** 该字段 MUST 被忽略（不在 DTO 内），设备实时状态不被覆盖

### Requirement: 安防只读消费端点

系统 SHALL 提供安防事件 `/api/v1/security/events`、巡检摄像头 `/api/v1/security/patrol-cameras`、
人员轨迹 `/api/v1/security/track/summary` 与 `/api/v1/security/track/timeline`、黑名单
`/api/v1/security/blacklist` 等只读端点；响应统一 B3 包络，只读端点不产生广播事件。
