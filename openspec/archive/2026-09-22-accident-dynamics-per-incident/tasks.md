# Tasks: 事故救援「响应动态」按事件隔离

> 唯一任务真源；完成即勾选（`[x]`），全勾后同交付内归档。

- [x] 1. 三方言 `V63__accident_dynamics_per_incident.sql`：h2 加列 + 索引 + UPDATE 归默认事件 + 演练事件行（自增 id）+ 10 条演练动态（子查询 incident_id）；pg 用 `ADD COLUMN`；dm 逐条 INSERT 不支持多行 VALUES。
- [x] 2. `FacAccidentDynamic` 实体加 `incidentId` 字段（`@TableField("incident_id")`）。
- [x] 3. `AccidentRescueService`：`dynamics` 改按 `incident_id` 查 + 空兜底默认事件；调度/值班/辅助统计保持全局不变。
- [x] 4. 单测 `AccidentRescueServiceTest`：新增 `incident_dynamicsOwned_returnsWithoutFallback` 与 `incident_dynamicsEmpty_fallsBackToDefaultIncident`。
- [x] 5. 跑 `scripts/check-dialect-migration-consistency.py --dialects h2,dameng,postgresql` 三方言一致。
- [x] 6. 跑全量 `mvn test`：661 绿，0 failure/0 error。
- [x] 7. `scripts/check-api-contract.mjs --strict`：路由 0 差异 / schema 0 漂移（incident_id 不对外）。
- [x] 8. 通知前端同步契约 description（无 schema 变更）：前端 `validate-api-contracts.mjs` 通过、`gen:api-types` 仅 JSDoc 文字变更。
- [x] 9. 合并 spec-delta 到 `openspec/specs/emergency-event/spec.md`（新增动态隔离 Requirement + 修订约束），并 `git mv` 本 Change 到 `openspec/archive/2026-09-22-accident-dynamics-per-incident/`。
- [x] 10. 同步 `docs/system-facts.md`：记录 dynamics 按事件隔离（演练/真实各自独立，空兜底默认事件）。
