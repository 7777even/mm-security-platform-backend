# emergency-plan Specification

## Purpose

应急预案能力：主记录台账（`fac_emergency_plan`）、目录扁平台账（`fac_emergency_plan_catalog`）、
预案矩阵与行动卡、预案调用记录。承接 mgmt 管理端「应急预案」模块全量 CRUD 与大屏应急指挥页的只读消费。

本 capability 的 Requirement 来自**已归档 Change 的 spec-delta 回填**（2026-10-06 治理批次），
来源：`openspec/archive/2026-10-02-mgmt-p1-plan-crud/`、
`openspec/archive/2026-10-02-mgmt-p1-plan-catalog-crud/`。

## Endpoints

| Method | Path | 权限 | 说明 |
| ------ | ---- | ---- | ---- |
| GET | `/api/v1/emergency-plans` | `role:ADMIN` | 主记录列表 |
| POST | `/api/v1/emergency-plans` | `emergency:plan:write` | 主记录新增 |
| PUT | `/api/v1/emergency-plans/{id}` | `emergency:plan:write` | 主记录局部更新 |
| DELETE | `/api/v1/emergency-plans/{id}` | `emergency:plan:write` | 主记录物理删除 |
| GET | `/api/v1/emergency-plans/catalog` | `role:ADMIN` | 预案目录（树/分组） |
| GET | `/api/v1/emergency-plans/catalog-detail` | `role:ADMIN` | 预案目录详情 |
| GET | `/api/v1/emergency-plans/catalog-items` | `role:ADMIN` | 目录扁平台账列表 |
| POST | `/api/v1/emergency-plans/catalog-items` | `emergency:plan-catalog:write` | 目录台账新增 |
| PUT | `/api/v1/emergency-plans/catalog-items/{id}` | `emergency:plan-catalog:write` | 目录台账局部更新 |
| DELETE | `/api/v1/emergency-plans/catalog-items/{id}` | `emergency:plan-catalog:write` | 目录台账物理删除 |
| GET | `/api/v1/emergency-plans/matrix` | `role:ADMIN` | 预案矩阵（行动卡） |
| GET | `/api/v1/emergency-plans/options` | `role:ADMIN` | 预案下拉选项 |
| POST | `/api/v1/emergency-plans/{id}/invoke` | `role:ADMIN` | 预案调用留痕 |
| POST | `/api/v1/emergency-plans/{planId}/action-cards` | `role:ADMIN` | 行动卡新增 |
| PUT | `/api/v1/emergency-plans/{planId}/action-cards/{cardId}` | `role:ADMIN` | 行动卡编辑 / 状态推进 |
| DELETE | `/api/v1/emergency-plans/{planId}/action-cards/{cardId}` | `role:ADMIN` | 行动卡删除 |

## Requirements

### Requirement: 应急预案主记录写端点

系统应为 `fac_emergency_plan` 主记录提供 `POST /api/v1/emergency-plans`、
`PUT` 与 `DELETE /api/v1/emergency-plans/{id}`。

- **权限**：需 `emergency:plan:write`（V98 登记，授权 ADMIN / COMMANDER / SCHEDULER）；
- **新增必填**：`planName`；缺失或空白返回 B3 `PARAM_INVALID`（文案点名 `planName`）；
- **编辑语义**：局部更新，请求体中为 `null` 的字段表示不修改；
- **删除**：物理删除；不存在（含重复删除）返回 B3 `NOT_FOUND`；
- **主键**：新增行 id 取当前 `max(id) + 1`；
- **广播**：成功写入后发布 `emergency.plan` 域变更。

#### Scenario: 局部更新不覆盖未传字段
- **WHEN** `PUT /api/v1/emergency-plans/1` 请求体为 `{"facility":"乙烯罐区"}`
- **THEN** 该行 `facility` 变「乙烯罐区」，`planName`/`domain` 保持原值

#### Scenario: 新增必填缺失
- **WHEN** `POST /api/v1/emergency-plans` 未传 `planName`
- **THEN** 返回 B3 `PARAM_INVALID`，不执行任何插入

#### Scenario: 重复删除
- **WHEN** 对同一已删除 id 再次发起 DELETE
- **THEN** 返回 B3 `NOT_FOUND`（不静默成功）

### Requirement: 应急预案目录台账写端点

系统应为 `fac_emergency_plan_catalog` 扁平台账提供 `POST /api/v1/emergency-plans/catalog-items`、
`PUT` 与 `DELETE /api/v1/emergency-plans/catalog-items/{id}`。

- **权限**：需 `emergency:plan-catalog:write`（V97 登记，授权 ADMIN / COMMANDER / SCHEDULER）；
- **新增必填**：`label`；缺失或空白返回 B3 `PARAM_INVALID`（文案点名 `label`）；
- **编辑语义**：局部更新，请求体中为 `null` 的字段表示不修改；
- **删除**：物理删除；不存在（含重复删除）返回 B3 `NOT_FOUND`；
- **主键**：新增行 id 取当前 `max(id) + 1`（V39 种子显式插 id，分配器规避序列滞后）；
- **广播**：成功写入后发布 `emergency.plan-catalog` 域变更。

#### Scenario: 局部更新不覆盖未传字段
- **WHEN** `PUT /api/v1/emergency-plans/catalog-items/1` 请求体为 `{"planName":"新预案"}`
- **THEN** 该行 `planName` 变「新预案」，`label`/`canSwitch` 保持原值

#### Scenario: 新增必填缺失
- **WHEN** `POST /api/v1/emergency-plans/catalog-items` 未传 `label`
- **THEN** 返回 B3 `PARAM_INVALID`，不执行任何插入

#### Scenario: 重复删除
- **WHEN** 对同一个已删除 id 再次发起 DELETE
- **THEN** 返回 B3 `NOT_FOUND`（不静默成功）

### Requirement: 预案矩阵行动卡与调用留痕

系统应提供预案矩阵只读 `GET /api/v1/emergency-plans/matrix` 与行动卡写端点
`POST / PUT / DELETE /api/v1/emergency-plans/{planId}/action-cards[/ {cardId}]`（`role:ADMIN`），
以及预案调用留痕 `POST /api/v1/emergency-plans/{id}/invoke`（`role:ADMIN`）。

> 业务域列名为 `domain_code`（`domain` 是达梦 DM8 硬保留字，实体以 `@TableField("domain_code")` 映射），
> 详见根 `AGENTS.md §5`。

行动卡写端点在无后端（`VITE_API_BASE` 未配置）时前端保持本地演示改动，不得假成功；
后端写成功须触发对应域实时刷新，使大屏预案矩阵同步。

### Requirement: 预案只读消费端点

系统应提供 `GET /api/v1/emergency-plans`、`/catalog`、`/catalog-detail`、`/catalog-items`、`/options`
（`role:ADMIN`）供管理端预案模块与大屏预案目录渲染；响应统一 B3 包络，只读端点不产生广播事件。
