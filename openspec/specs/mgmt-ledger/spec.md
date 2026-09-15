# mgmt-ledger Specification

## Purpose
通用管理台账能力：以 `mgmt_ledger_meta/row/cell` 三表承接后台管理端 18 个只读配置/主数据域（报警配置、危化品库、演练管理、演练评估、应急储罐/罐区/装置/仓库/仓库分区、企业基本信息、消防救援预案、洪涝点、媒体消防参数、组织机构、生产应急、培训管理、水系、广播模板），提供按 `:domain` 通用渲染的只读接口族。设计依据见 `openspec/archive/2026-09-15-mgmt-ledger-generic/design.md`。

## Requirements

### Requirement: 通用台账只读端点族
系统须提供 `GET /api/v1/mgmt-ledger/{domain}` 与 `GET /api/v1/mgmt-ledger/{domain}/meta` 两个只读端点，返回列定义、筛选维度与二维单元格（含 ok/warn/bad 着色语义）。

#### Scenario: 查询域台账
- **WHEN** 已登录用户调用 `GET /api/v1/mgmt-ledger/alarm-config`
- **THEN** 返回 200 + `Result.ok`，`data.rows` 为二维单元格数组，`data.columns` 与 `data.filters` 对齐该域种子定义。

#### Scenario: 查询域元信息
- **WHEN** 已登录用户调用 `GET /api/v1/mgmt-ledger/alarm-config/meta`
- **THEN** 返回 200 + `Result.ok`，`data.columns` 为列名数组，`data.filters` 为可筛选维度（column/options）。

#### Scenario: 列筛选
- **WHEN** 调用 `GET /api/v1/mgmt-ledger/alarm-config?f_级别=一级`
- **THEN** 仅返回该列等于「一级」的行。

#### Scenario: 关键字
- **WHEN** 调用 `GET /api/v1/mgmt-ledger/alarm-config?keyword=原油`
- **THEN** 仅返回任意单元格含「原油」的行。

#### Scenario: 未知域
- **WHEN** 调用 `GET /api/v1/mgmt-ledger/does-not-exist`
- **THEN** 返回 404，message 含「台账不存在」，不抛 500。

### Requirement: 零下行控制
通用台账端点仅提供只读 GET，不提供任何写接口，符合 B3 红线。
