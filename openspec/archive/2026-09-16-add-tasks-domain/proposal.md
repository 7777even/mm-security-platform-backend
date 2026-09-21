# Proposal: 新增处置任务域（tasks，移动端任务中心）

## Why
`apps/mobile` 的 `/tasks`、`/tasks/:id`、`/path` 仍走 `data/mock.ts` 的 `tasks` 静态数据
（属 ④-A 冻结清单 `docs/frozen-prototype.md`），后端无对应 REST 端点。按跨库四同步新增
只读域 `tasks`，解除冻结——作为「缺端点移动端域先打通一个再批量复制」的模板域。

## What Changes
- Flyway **V54**（h2 / dameng / postgresql 三方言）新增只读表 `fac_dispatch_task` + 4 条种子。
- 后端新增 `TaskController`（`GET /api/v1/tasks`、`GET /api/v1/tasks/{id}`）/ `TaskService` /
  `FacDispatchTaskMapper` / `FacDispatchTask` 实体 / `dto.TaskItem`、`dto.TaskList`。
- 前端契约新增 `docs/api/tasks.openapi.json`（唯一真源）。
- 移动端 `tasks.vue` / `task-detail.vue` / `path-nav.vue` 改接 `@/services/task`。

## Capabilities
- 移动端任务中心「列表 / 详情 / 路径规划」由后端 `fac_dispatch_task` 表驱动（纯只读）。
- **语义边界**：仅任务台账查询，不含任何物理下发动作；下行红线 `HardControlPaths` 不变。

## Impact
- 仅新增表 + 端点，无存量数据/接口变更。回退：删除 V54 迁移 + 移除 Controller/Service/契约。
- 达梦 / PostgreSQL 方言沿现状「静态同步维护、未实跑」状态，待 DM8/PG 实例 `flyway migrate` 校验。
- 列名用 `task_level`（而非 `level`）：规避 `LEVEL` 在达梦/Oracle 为保留字的方言冲突
  （类比既有 H2 保留字坑 `value`/`command`/`type`）。
