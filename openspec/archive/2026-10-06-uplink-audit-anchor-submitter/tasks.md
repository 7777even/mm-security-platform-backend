# Tasks

- [x] 新建三方言迁移 `V108__audit_log_actor.sql`（h2/postgresql/dameng）：`ALTER TABLE fac_audit_log ADD actor VARCHAR(64)`
- [x] `FacAuditLog.java` 增 `actor` 字段（`@TableField("actor")`）
- [x] `AuditLogItem.java` 增 `actor`；`UplinkService.reportAudit` 由 `UserContext.username()` 写入、`toAuditItem` 映射
- [x] 契约 `uplink.openapi.json`：`AuditLogItem` 增 `actor` 属性 + 示例（外科式插入）
- [x] `scripts/check-endpoint-authz.mjs` L48–51 `⚠️待安全确认` 标记更新为已处置说明
- [x] 后端单测 `UplinkServiceTest`/`UplinkControllerTest` 断言 actor 被置为当前登录用户且非空
- [x] 前端 `npm run gen:api-types` 重生 `AuditLogItem` 类型；`AuditView.vue` 增"操作人"列
- [x] 跑后端门禁：`check-api-contract --strict`（路由0/漂移0）、`check-endpoint-authz`（148/148）、`mvn test`
- [x] 跑前端门禁：`validate-api-contracts`、`npm run type-check`
- [x] 修订 `docs/architecture/audit-log.md §3` 过时约束；渗透清单 §6 回填结论 + 回链本 Change
- [x] 合入 `openspec/specs/uplink-audit/spec.md` 提交人锚定需求，归档本 Change
