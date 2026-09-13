# 任务：工作站域防区过滤列表端点

## 后端

- [ ] T1 `WorkstationService.page(page,size,zone,online)`：LambdaQueryWrapper（deleted=0 / zone like / online eq / orderByDesc updatedAt）+ `DataScopeHelper.apply(qw, FacWorkstation::getZone, dataScopeResolver.resolveZones())`；返回 `Page<FacWorkstation>`，映射为 `Workstation`。
- [ ] T2 DTO `WorkstationPageResult`（`list:List<Workstation>` / `total` / `page` / `size`）。
- [ ] T3 `WorkstationController` `GET /api/v1/workstations`（`@RequireAuth`，参数 page/size/zone/online）→ `Result<WorkstationPageResult>`。
- [ ] T4 `DashboardService` 注入 `DataScopeResolver`，`workstations()` 套防区过滤（复用同映射）。
- [ ] T5 `WorkstationServiceTest` 单测：zone 过滤生效 / online 过滤 / 空 zone→1=0 / ALL→看全 / 分页 total 正确。
- [ ] T6 TDD 先红：`WorkstationServiceTest` 在 T1 实现前先写失败用例。
- [ ] T7 `mvn -s ci-settings.xml test` 全绿（591+ 基线）+ jacoco 门禁达标。

## 契约

- [ ] T8 `frontend-scaffold/docs/api/dashboard.openapi.json` 新增 `GET /api/v1/workstations`（summary/description/中文参数/example + `WorkstationPageResult` schema，list 元素 `$ref Workstation`）；`/dashboard/workstations` 描述补"已套防区过滤（data_scope 行级 ABAC）"注记。
- [ ] T9 `node scripts/check-api-contract.mjs --strict` 路由 0 差异 / schema 0 漂移。
- [ ] T10 `node scripts/validate-api-contracts.mjs` 全域通过。

## 前端

- [ ] T11 `npm run gen:api-types` 重新生成 `WorkstationPageResult` 类型并提交 `src/types/generated/`。
- [ ] T12 前端三绿保持：`vue-tsc --noEmit` 0 错 / `gate:screen` PASS / `vitest run` 全绿。

## 归档

- [ ] T13 spec 回填 `openspec/specs/dashboard-analytics/spec.md`（新增"工作站列表防区过滤"需求 + 现有 getWorkstations 补防区注记）；`git mv` 归档至 `openspec/archive/2026-09-13-workstation-datascope-list`；`node scripts/check-openspec-hygiene.mjs` 通过。
- [ ] T14 双仓提交按 scope 拆分（契约先行 → 后端实现 → 测试），先推前端契约再推后端实现；`origin` 跟踪 `0 0`。
