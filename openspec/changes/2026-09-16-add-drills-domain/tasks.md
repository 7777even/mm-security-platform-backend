# Tasks

- [x] 前端契约 `docs/api/drills.openapi.json`（`GET /drills`、`GET /drills/{id}` + 4 schema）
- [x] Flyway V55 三方言建表 `fac_drill` + `fac_drill_task` + 种子
- [x] 后端 `DrillController` / `DrillService` / 双 Mapper / 双实体 / 4 DTO
- [x] `DrillControllerTest`（standalone MockMvc + Mockito）
- [x] `node scripts/check-api-contract.mjs --strict` 0 差异（DrillItem/DrillDetail 10、DrillTaskItem 2、DrillList 2 字段对齐）
- [x] 前端 `npm run gen:api-types` 生成 `src/types/generated/drills.ts`
- [x] 移动端 `drills.vue` / `drill-detail.vue` 接 `@/services/drill`
- [x] `docs/frozen-prototype.md` 解除 `/drills`、`/drills/:id` 冻结
- [ ] 达梦 / PostgreSQL 方言 V55 实跑校验（待 DM8/PG 实例 `flyway migrate`）
