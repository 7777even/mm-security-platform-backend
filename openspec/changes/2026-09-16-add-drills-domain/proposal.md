# Proposal: 新增应急演练域（drills，移动端演练信息）

## Why
`apps/mobile` 的 `/drills`、`/drills/:id` 仍走 `data/mock.ts` 的 `drills` 静态数据（④-A 冻结清单），
后端无对应 REST 端点。按跨库四同步新增只读域 `drills`，解除冻结（复制 `tasks` 域模板）。

## What Changes
- Flyway **V55**（h2 / dameng / postgresql 三方言）新增只读表 `fac_drill` + `fac_drill_task` + 种子。
- 后端新增 `DrillController`（`GET /api/v1/drills`、`GET /api/v1/drills/{id}`）/ `DrillService` /
  `FacDrillMapper`、`FacDrillTaskMapper` / `FacDrill`、`FacDrillTask` 实体 /
  `dto.DrillItem`、`DrillDetail`、`DrillTaskItem`、`DrillList`。
- 前端契约新增 `docs/api/drills.openapi.json`（唯一真源）。
- 移动端 `drills.vue` / `drill-detail.vue` 改接 `@/services/drill`。

## Capabilities
- 移动端「演练信息列表 / 演练详情（含任务子项）」由后端 `fac_drill`(`_task`) 驱动（纯只读）。
- **语义边界**：仅演练台账查询，不含演练执行下发动作。

## Impact
- 仅新增表 + 端点，无存量变更。回退：删 V55 迁移 + 移除 Controller/Service/契约。
- 达梦 / PostgreSQL 方言沿现状「静态同步维护、未实跑」，待 DM8/PG 实例 `flyway migrate` 校验。
