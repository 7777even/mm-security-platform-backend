# Tasks

- [x] V66 三方言迁移：`content` → `detail_json`（h2/dm8 LONGVARCHAR、pg TEXT），种子改 JSON
- [x] `FacFormRecord` 实体 + `FormRecord*` DTO：`content` → `detailJson`
- [x] `FormRecordService`：`detailJson` 非空校验 + `formType` 枚举校验
- [x] `FormRecordController`：`POST` 去 ADMIN、`PUT` 保留 ADMIN
- [x] 契约同步（前端 `docs/api/form-records.openapi.json` 已改）
- [x] 门禁：`check-api-contract --strict` 0 差异、`mvn test`

## 验收标准

- [x] `tasks.md` 全部勾选。
- [x] `check-api-contract --strict` 无 schema/路由漂移。
- [x] `mvn test` 全绿。
