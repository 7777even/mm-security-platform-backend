# dashboard-analytics Specification

## Purpose

态势总览域的分析端点。由 Change `add-dashboard-alarm-trend`、`implement-remaining-contracts`（均已归档）回填。

## Requirements

### Requirement: 近 24 小时报警趋势

系统须提供 `GET /api/v1/dashboard/alarm-trend`，返回近 24 小时每小时报警计数序列。

#### Scenario: 趋势查询

- **WHEN** `GET /api/v1/dashboard/alarm-trend`
- **THEN** B3 包络返回 `Result<List<AlarmTrendPoint>>`，每个元素含 `hour`（`"08:00"` 形式的小时起点标签，24 桶之一）与 `count`（该小时 `fac_alarm`(deleted=0) 行数，无报警为 0）

### Requirement: 分区风险热力

系统须提供 `GET /api/v1/dashboard/risk-heatmap`，返回分区风险评分列表。

#### Scenario: 热力图查询

- **WHEN** `GET /api/v1/dashboard/risk-heatmap`
- **THEN** B3 包络返回 `RiskHeatItem[]` 分区风险评分

### Requirement: 总览端点不回填桩数据

总览域端点读取真实业务事实，禁止编造统计口径或回落模拟数据。

#### Scenario: 空数据返回

- **WHEN** 统计范围内无业务数据
- **THEN** 返回空集合或 0 值序列，不得返回虚构数据

### Requirement: 工作站防区过滤分页列表

系统须提供 `GET /api/v1/workstations`（登录可读，零下行控制），返回按 data_scope 行级 ABAC 过滤的工作站/工位分页列表；复用既有 `fac_workstation` 表与 `Workstation` DTO，与 `GET /api/v1/dashboard/workstations` 口径一致。

#### Scenario: 防区过滤分页查询

- **WHEN** `GET /api/v1/workstations?page=1&size=20&zone=&online=`
- **THEN** B3 包络返回 `Result<WorkstationPageResult>`（list=`Workstation[]`、`total`/`page`/`size`）；非 ALL 角色仅见其 `zone_codes` 内工作站（`resolveZones()` 空集合→`1=0` 零可见）
