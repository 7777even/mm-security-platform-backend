# 通用管理台账只读能力（接入 18 个后台静态域）

## Why
后台管理端 `apps/mgmt` 仍有 18 个功能页由 `src/data/mgmtMenus.ts` 硬编码渲染静态列表，未接真实后端，违背「全功能真后端化清零」主线。这些域（报警配置、危化品库、演练管理、演练评估、应急储罐/罐区/装置/仓库/仓库分区、企业基本信息、消防救援预案、洪涝点、媒体消防参数、组织机构、生产应急、培训管理、水系、广播模板）多为配置/主数据类，且为只读展示。

## What Changes
- 新增通用管理台账能力：`mgmt_ledger_meta / mgmt_ledger_row / mgmt_ledger_cell` 三表（Flyway V51，h2/postgresql/dameng 三方言语义一致）。
- 新增只读端点族 `GET /api/v1/mgmt-ledger/{domain}` 与 `GET /api/v1/mgmt-ledger/{domain}/meta`，返回列定义、筛选维度、二维单元格（含 ok/warn/bad 语义着色标记）。
- 18 个域的 columns / filters / rows / cells 以种子数据固化进 DB，前端按 `:domain` 通用渲染。

## Capabilities
- `mgmt-ledger`（新建 capability spec）

## Impact
- 仅 GET，零下行控制，符合 B3 红线（无写接口、无 HardControl 风险）。
- 不改变既有任何对外接口；新增端点独立于 system/security 等既有域。
- 前端契约真源：`frontend-scaffold/docs/api/mgmt-ledger.openapi.json`（本次由前端库提交，本库不复制第二份主契约）。
