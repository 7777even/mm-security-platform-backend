# 提案：工作站域防区过滤列表端点（数据权限 A1-①）

> **状态：`proposed`** —— 分级 **L3**（新增只读列表端点 + 复用既有表 + 套既有 data_scope ABAC；不改库结构 / 权限模型 / 错误码分段）。
> 触发：用户于 2026-09-13 确认"按现有 DeviceService.page() 同模式补齐工作站域防区过滤"（选项 ①）。

## Why

工作站域当前**无独立列表查询**，仅在 `DashboardService.workstations()` 聚合里出现，且**未套防区过滤**；非 ALL 角色用户（zone_codes 非空/空）能看到全部工作站，与已落地的设备域（`DeviceService.page()`）、救援队伍域（`RescueResourceService.brigades()`）行级 ABAC 口径不一致，存在数据权限越权（R1 同类风险）。V49 已补工作站域 4 个防区词（全厂范围/化工区/储运区/公用工程区），只差列表端点落地 + 套过滤。

## What Changes

- 新增 `WorkstationController`（`/api/v1/workstations`）+ `WorkstationService.page(page,size,zone,online)`：分页返回工作站列表，复用 `DataScopeResolver.resolveZones()` + `DataScopeHelper.apply(qw, FacWorkstation::getZone, zones)` 套防区过滤，**镜像 `DeviceService.page()`**。
- 新增 DTO `WorkstationPageResult`（`list:List<Workstation>` / `total` / `page` / `size`），list 元素复用既有 `Workstation` DTO（与 `dashboard.openapi.json#/Workstation` 同名）。
- `DashboardService` 注入 `DataScopeResolver`，`workstations()` 查询套防区过滤，使现有 `GET /dashboard/workstations` 与 overview `onlineWorkstation` 计数与新建列表端点口径一致（消除非 ALL 用户越权可见）。

## Capabilities

### Added Capabilities

- `dashboard-analytics`：新增"工作站列表（防区过滤）"需求，提供 `GET /api/v1/workstations` 分页列表端点。

### Modified Capabilities

- `dashboard-analytics`：现有 `getWorkstations`（`/dashboard/workstations`）现声明为防区过滤（数据权限），与列表端点口径统一。

## Impact

- 受影响：`controller/WorkstationController`(新)、`service/WorkstationService`(新)、`service/DashboardService`(改)、`dto/WorkstationPageResult`(新)；`fac_workstation`(只读)。
- 契约同步：`frontend-scaffold/docs/api/dashboard.openapi.json` 新增 `GET /api/v1/workstations` + schema `WorkstationPageResult`；`/dashboard/workstations` 描述补"已套防区过滤"注记。
- 数据影响：不涉及（复用 `fac_workstation`，无迁移 / 无字段变更；回退 = 删端点）。
- 不触碰：设备域 / 救援队伍域既有过滤、硬控名单、B3 包络、鉴权白名单、权限码、`fac_workstation` 表结构。
- 安全语义：未改鉴权 / 权限模型 / 签名 / 硬控；仅复用既有 data_scope ABAC（与设备域同约定：zone_codes 存中文 zone_name，`qw.in(zone, zones)` 命中）。
- 回归面：`DashboardServiceTest`（workstations 现返回过滤后子集）、现有 dashboard 冒烟；前端 `dashboard.ts` 消费 `/dashboard/workstations` 行为变为按登录用户防区过滤（只读展示无需改）。

## 人工确认关卡（L3 须过）

- [ ] 范围确认：仅新增只读列表端点 + 复用既有 ABAC，无需求扩散、无自造平行任务。
- [ ] 契约合规（AGENTS.md §3）：零下行控制 / B3 包络 / 无新错误码 / 复用 `@RequireAuth`（登录可读，同 devices/dashboard 口径）。
- [ ] 跨库四同步：前端 `dashboard.openapi.json` 同步范围与 `gen:api-types` 负责人已明确（同一次交付）。
- [ ] 数据影响：不涉及改表（回退 = 删端点），已确认。
- [ ] 高风险项：未触及 L4 清单（权限模型 / 库结构 / 安全过滤器链均未改），确认按 L3 实施即可。
- [ ] 设计抉择：现有 `DashboardService.workstations()` 一并套防区过滤（保证概览计数与列表口径一致）——若仅要新建端点、保留概览看全，请在此否决。
