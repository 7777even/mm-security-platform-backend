# Proposal: 新增化学品 MSDS 域（msds，移动端化学品知识）

## Why
`apps/mobile` 的 `/msds`、`/msds/:cas` 仍走 `data/mock.ts` 的 `msds` 静态数据（④-A 冻结清单），
后端仅有 `hazard`（重大危险源）嵌套，**无独立 MSDS 端点**。按跨库四同步新增只读域 `msds`，解除冻结。

## What Changes
- Flyway **V56**（h2 / dameng / postgresql 三方言）新增只读表 `fac_msds` + 3 条种子。
- 后端新增 `MsdsController`（`GET /api/v1/msds`、`GET /api/v1/msds/{cas}`）/ `MsdsService` /
  `FacMsdsMapper` / `FacMsds` 实体 / `dto.MsdsItem`、`MsdsDetail`、`MsdsList`。
- 前端契约新增 `docs/api/msds.openapi.json`（唯一真源）。
- 移动端 `msds.vue` / `msds-detail.vue` 改接 `@/services/msds`。

## Capabilities
- 移动端「化学品知识检索 / MSDS 详情」由后端 `fac_msds` 驱动（纯只读）。
- 详情按 **CAS 号**查询（与移动端 `/msds/:cas` 路由一致），非自增 id。

## Impact
- 仅新增表 + 端点，无存量变更。回退：删 V56 迁移 + 移除 Controller/Service/契约。
- 达梦 / PostgreSQL 方言沿现状「静态同步维护、未实跑」，待 DM8/PG 实例 `flyway migrate` 校验。
