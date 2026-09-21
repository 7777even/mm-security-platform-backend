# Tasks

- [x] 契约真源扩展 `docs/api/communication.openapi.json`（`GET /communication/records` + `CommunicationRecord` / `CommunicationRecordList`）
- [x] V57 三方言迁移（h2 / dameng / postgresql）新增 `fac_comm_record` + 15 条种子
- [x] 后端 `FacCommRecord` 实体 / `FacCommRecordMapper` / `CommRecordService` / `CommRecordController`
- [x] `dto.CommunicationRecord` / `dto.CommunicationRecordList`
- [x] `CommRecordControllerTest`（standalone MockMvc + 纯 Mockito）
- [x] `check-api-contract.mjs --strict` 通过（路由 0 差异 / schema 0 漂移）
- [x] H2 实跑 E2E（`GET /api/v1/communication/records` 全量 + `?type=` 过滤）
- [x] 回写 `docs/system-facts.md`
- [x] 达梦 / PostgreSQL 方言 V57 实跑校验（PG V1–V57 `flyway migrate` 通过；DM8 disql 全量重跑 0 错误，`fac_comm_record` 15 行种子落库，2026-09-17）
