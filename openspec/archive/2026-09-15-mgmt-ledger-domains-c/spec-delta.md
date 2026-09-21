# 规格变更说明（spec-delta）

本变更**不新增 / 不修改 / 不删除任何对外接口与 schema**，仅扩充既有 `mgmt-ledger` capability 的数据域清单与前端路由登记。

## 新增

- 无。（复用 `GET /api/v1/mgmt-ledger/{domain}` 与 `/meta`，契约 `frontend-scaffold/docs/api/mgmt-ledger.openapi.json` 未改。）

## 修改

对照 `openspec/specs/mgmt-ledger/spec.md`，归档时需回填以下内容：

### Requirement: 通用台账只读端点族（MODIFIED — 仅扩域清单）

**现状**：Purpose 列出的域为 18 个（报警配置、危化品库、演练管理、演练评估、应急储罐/罐区/装置/仓库/仓库分区、企业基本信息、消防救援预案、洪涝点、媒体消防参数、组织机构、生产应急、培训管理、水系、广播模板）。

**变更后**：域清单**增加 6 个**——`key-location`（重点部位）、`incident-archive`（事件档案）、`drill-script`（演练脚本）、`linkage-unit`（联动单位）、`emergency-pool`（应急资源池）、`ef-medium`（消防介质），由 **V52** 种子化 `mgmt_ledger_meta/row/cell` 提供。

接口契约、行为语义（列筛选 / 关键字 / 未知域 404）、零下行控制约束**均不变**。

#### Scenario: 新增 6 域可按域查询
- **WHEN** 已登录用户调用 `GET /api/v1/mgmt-ledger/key-location`
- **THEN** 返回 200 + `Result.ok`，`data.rows` 为该域 V52 种子定义的二维单元格数组，`data.columns` / `data.filters` 与前端 `mgmtMenus` 原型页约定一致

#### Scenario: 新增域元信息可用
- **WHEN** 调用 `GET /api/v1/mgmt-ledger/ef-medium/meta`
- **THEN** 返回 200 + `Result.ok`，`data.columns` 为列名数组，`data.filters` 为可筛选维度

## 移除

- 无。

## 四同步结论

- ① openspec：本 Change（`2026-09-15-mgmt-ledger-domains-c`）。
- ② 前端契约：**未改**（复用 `mgmt-ledger.openapi.json`，无 path/schema 变更）。
- ③ 后端实现：**未改**（复用既有 `MgmtLedgerController` / `MgmtLedgerService`），仅新增 V52 种子迁移。
- ④ 前端类型：**无需重生成**（`src/types/generated/mgmt-ledger.ts` 无变化）。
- 守门：`node scripts/check-api-contract.mjs --strict` 须保持 **0 漂移**。

> 归档时须将上文「修改」段合入 `openspec/specs/mgmt-ledger/spec.md`（扩 Purpose 域清单 + 追加 2 个 Scenario），再把本 Change 目录 `git mv` 到 `openspec/archive/`。
