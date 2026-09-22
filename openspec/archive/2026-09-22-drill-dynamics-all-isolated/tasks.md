# Tasks: 演练事件 12–16 响应动态各自独立展示

- [x] V64 三方言迁移：演练事件 12–16 各建 `fac_accident_incident` 行 + 各 7 条演练专属动态
- [x] 方言一致性校验通过（h2/dameng/postgresql 三方言对齐）
- [x] 后端契约守门 `check-api-contract --strict` 0/0（无 schema 变更）
- [x] `DbLayerIntegrationIT` 在 H2 Flyway 真实应用 V64 通过（4/4）
- [x] `AccidentRescueServiceTest` 事件隔离逻辑 6/6 通过
- [x] 修订 `openspec/specs/emergency-event/spec.md` 隔离 Requirement 覆盖 11–16
- [x] 两端 `docs/system-facts.md` 同步 2026-09-22 条目
- [x] 按 scope 拆分提交（feat(db) 迁移 + docs(openspec) + docs(docs) 事实基线）并双仓推送
