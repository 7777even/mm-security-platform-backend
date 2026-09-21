# Tasks

- [x] 前端契约 `docs/api/msds.openapi.json`（`GET /msds`、`GET /msds/{cas}` + `MsdsItem`/`MsdsDetail`/`MsdsList`）
- [x] Flyway V56 三方言建表 `fac_msds` + 3 条种子
- [x] 后端 `MsdsController` / `MsdsService` / `FacMsdsMapper` / `FacMsds` / 3 DTO
- [x] `MsdsControllerTest`（standalone MockMvc + Mockito）
- [x] `node scripts/check-api-contract.mjs --strict` 0 差异（MsdsDetail 11、MsdsItem 4、MsdsList 2 字段对齐）
- [x] 前端 `npm run gen:api-types` 生成 `src/types/generated/msds.ts`
- [x] 移动端 `msds.vue` / `msds-detail.vue` 接 `@/services/msds`
- [x] `docs/frozen-prototype.md` 解除 `/msds`、`/msds/:cas` 冻结
- [x] 达梦 / PostgreSQL 方言 V56 实跑校验（PG V1–V57 `flyway migrate` 通过；DM8 disql 全量重跑 0 错误，2026-09-17）
