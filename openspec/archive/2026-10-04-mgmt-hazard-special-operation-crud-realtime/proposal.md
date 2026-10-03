# Proposal: 重大危险源 / 监测点位 / 特殊作业票 CRUD 及实时广播

## 背景
监测 / 通信 / 生产域共有 7 个只读视图无写能力、因而也无实时广播源。第 1 批（video.camera / communication.device）与第 2 批（device / communication.record）已完成；本批收口剩余 3 个只读域：`hazard`（重大危险源，`HazardMgmtView`）、`hazard.point`（监测点位，`MonitorPointView`）、`special-operation`（特殊作业票，`SpecialOpsView`）。

三者的后端 scope 此前未被提交门禁收录，故本批先扩 scope 枚举（AGENTS.md §6.5 与 `scripts/commit-msg-lint.sh` 的 `ALLOWED_SCOPES` 必须同步——后者硬编码、不读 AGENTS.md）。

## 目标
- `hazard`：`/api/v1/hazards` 增加 POST，`/api/v1/hazards/{id}` 增加 PUT / DELETE，广播 `hazard.changed`。
- `hazard.point`：`/api/v1/monitoring/points` 增加 POST，`/api/v1/monitoring/points/{id}` 增加 PUT / DELETE，广播 `hazard.point.changed`。
- `special-operation`：`/api/v1/special-operations` 增加 POST，`/api/v1/special-operations/{id}` 增加 PUT / DELETE，广播 `special-operation.changed`。
- 三表（`fac_major_hazard` / `fac_monitoring_point` / `fac_special_operation_ticket`）由 V105 三方言迁移补 `version` 乐观锁列，实体补 `@Version`。
- 契约四同步：`hazard.openapi.json` / `special-operation.openapi.json` 补写端点与写请求 schema。
- 长期危险来源与监测点位的写方法必须失效对应 Caffeine 缓存，否则订阅端重拉取到过期列表。
- 三域新增/更新/删除行为纳入单测（含缓存失效与字符串主键冲突路径）。

## 非目标
- 不新增权限码与 `sys_menu` 按钮种子：本批前端仍只订阅刷新、无写 UI，写端点沿用 `@RequireAuth(role = "ADMIN")`（已通过 `check-endpoint-authz` 守门）。
- 不改动既有读端点响应 schema。
- 不开嵌套明细写：重大危险源的 7 类 JSON 明细列（联系人 / 档案 / 监测点 / 视频 / 化学品 / 疏散路线 / 应急操作）仍由 seeder 维护；特殊作业票的现场视频 / 气体检测点 / 作业人员子表本批不开放写，只写主票表。
