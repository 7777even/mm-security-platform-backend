# fire-monitoring Specification

## Purpose

防火巡查（巡查计划 / 巡查执行）与消防资源只读摘要能力：承接 mgmt 管理端「防火巡查」「巡查执行」模块的
全量 CRUD，以及大屏消防资源面板（消防设备、装备状态、救援力量、特殊作业摘要）的只读展示。

本 capability 的 Requirement 来自**已归档 Change 的 spec-delta 回填**（2026-10-06 治理批次），
来源：`openspec/archive/2026-10-03-mgmt-p2-fire-facility-patrol-crud/`（防火巡查部分）、
`openspec/archive/2026-10-01-mgmt-p0-batch-crud/`（巡更执行部分）。

## Endpoints

| Method | Path | 权限 | 说明 |
| ------ | ---- | ---- | ---- |
| GET | `/api/v1/fire/patrols` | 登录即可 | 防火巡查计划列表 |
| POST | `/api/v1/fire/patrols` | `fire:patrol-write` | 巡查记录新增 |
| PUT | `/api/v1/fire/patrols/{id}` | `fire:patrol-write` | 巡查记录编辑（覆盖更新，不含 `status`） |
| DELETE | `/api/v1/fire/patrols/{id}` | `fire:patrol-write` | 巡查记录删除（真删除） |
| GET | `/api/v1/fire/patrol-executions` | 登录即可 | 巡查执行记录列表 |
| POST | `/api/v1/fire/patrol-executions` | `fire-alarm:patrol:write` | 巡查执行上报（移动端签到/异常上报） |
| PUT | `/api/v1/fire/patrol-executions/{id}` | `fire-alarm:patrol:write` | 巡查执行编辑 |
| DELETE | `/api/v1/fire/patrol-executions/{id}` | `fire-alarm:patrol:write` | 巡查执行删除 |
| GET | `/api/v1/fire/equipment` | 登录即可 | 消防设备列表 |
| GET | `/api/v1/fire/equipment-status` | 登录即可 | 消防装备状态统计 |
| GET | `/api/v1/fire/rescue-forces` | 登录即可 | 消防救援力量摘要 |
| GET | `/api/v1/fire/special-operations` | 登录即可 | 特殊作业摘要（写能力见 `special-operation`） |

## Requirements

### Requirement: 防火巡查记录 CRUD

系统 SHALL 提供 `POST /api/v1/fire/patrols`（校验权限码 `fire:patrol-write`，`patrolDate` 缺失或为空时
返回 400 校验失败，成功返回落库后的 `FirePatrolRecord`）、`PUT /api/v1/fire/patrols/{id}`
（覆盖更新，不含 `status`，`@Version` 冲突返回 409）、`DELETE /api/v1/fire/patrols/{id}`
（真删除，不存在返回 404）。三个写操作成功后 SHALL 广播 `fire.patrol-record` 域变更事件。

### Requirement: 巡查执行记录 CRUD

系统 SHALL 提供 `POST / PUT / DELETE /api/v1/fire/patrol-executions[/ {id}]`（权限码
`fire-alarm:patrol:write`），供移动端一线人员上报巡查执行结果：

- 修改 SHALL 为**局部更新**：空值字段表示"不更新"，不得清空未传字段；
- `execResult` SHALL 强枚举校验 ∈ {`NORMAL`, `ABNORMAL`}，非法返回 B3 `PARAM_INVALID`；
- 删除为物理删除；记录不存在返回 B3 `NOT_FOUND`；
- 写操作 SHALL 发布 `fire.patrol` 域变更事件，并落审计（`SystemAuditHelper`）。

### Requirement: 乐观锁

`fac_fire_patrol` SHALL 带 `version` 列，更新走 `@Version` 乐观锁，并发冲突返回 409，
禁止静默后写覆盖。

#### Scenario: 并发编辑冲突
- **GIVEN** 两条 `PUT /fire/patrols/{id}` 携带同一 `version`
- **WHEN** 第二条提交
- **THEN** 返回 409，库内记录保持第一次提交的结果

#### Scenario: void 删除仍广播
- **WHEN** `deleteFirePatrol(id)` 执行成功
- **THEN** 尽管方法返回 void，仍 MUST 广播对应域变更事件（前端据此重拉）

### Requirement: 零下行控制

巡查写请求 DTO SHALL NOT 含 `status` 字段与检查项结果（`checkItems` 只读），
检查项结果 MUST NOT 经由写请求写回；所有写操作 MUST NOT 触发任何物理设备下行。

#### Scenario: 零下行控制
- **WHEN** 任何巡查写请求携带 `status` 字段
- **THEN** 该字段 MUST 被忽略（不在 DTO 内），设备实时状态不被覆盖

### Requirement: 消防资源只读摘要

系统 SHALL 提供消防设备 `/api/v1/fire/equipment`、装备状态 `/api/v1/fire/equipment-status`、
救援力量 `/api/v1/fire/rescue-forces`、特殊作业摘要 `/api/v1/fire/special-operations` 四个只读端点
（登录即可），供大屏消防面板渲染；响应统一 B3 包络，只读端点不产生广播事件。
