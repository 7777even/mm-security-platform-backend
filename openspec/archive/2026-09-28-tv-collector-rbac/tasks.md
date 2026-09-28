# Tasks: tv-collector-rbac（后端）

- [x] `TvCollector` + `TvSourceAdapter` + `NoOpTvSourceAdapter` + `TvCollectorProperties`（默认关，复用入库/广播链路）
- [x] `TvService` 新增 `submitSnapshots(List)` 批量入库（单次广播）+ 抽出 `ingestOne`
- [x] V80 迁移（h2/pg/dm）登记 `video:snapshot:create` 权限码并授权 6 角色
- [x] `TvController.submitSnapshot` 收紧为 `@RequireAuth(perm="video:snapshot:create")`，清理 ALLOWLIST 豁免
- [x] dev：`TvSnapshotRenderer` + `TvSnapshotSeeder` 生成样例截图
- [x] 单测：`TvServiceTest.submitSnapshots` / `TvCollectorTest`（空数据跳过、有效数据上报、无效项过滤）
- [x] 门禁：`check-endpoint-authz` / `check-api-contract --strict` 零漂移
- [x] 双仓（后端 + 前端）按 scope 拆分提交并推送，openspec 归档
