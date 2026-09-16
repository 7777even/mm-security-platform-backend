# Tasks

- [x] 前端契约 `docs/api/tasks.openapi.json`（`GET /tasks`、`GET /tasks/{id}` + `TaskItem`/`TaskList`）
- [x] Flyway V54 三方言建表 `fac_dispatch_task` + 4 条种子
- [x] 后端 `TaskController` / `TaskService` / `FacDispatchTaskMapper` / `FacDispatchTask` / `dto.TaskItem`、`TaskList`
- [x] `TaskControllerTest`（standalone MockMvc + Mockito）
- [x] `node scripts/check-api-contract.mjs --strict` 0 差异（TaskItem 9 字段 / TaskList 2 字段对齐）
- [x] 前端 `npm run gen:api-types` 生成 `src/types/generated/tasks.ts`
- [x] 移动端 `tasks.vue` / `task-detail.vue` / `path-nav.vue` 接 `@/services/task`
- [x] `docs/frozen-prototype.md` 解除 `/tasks`、`/tasks/:id`、`/path` 冻结
- [ ] 达梦 / PostgreSQL 方言 V54 实跑校验（待 DM8/PG 实例 `flyway migrate`）
