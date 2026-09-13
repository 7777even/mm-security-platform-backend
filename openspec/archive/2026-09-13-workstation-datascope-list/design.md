# 设计：工作站域防区过滤列表端点

## ADR

- **ADR-1 复用既有 ABAC，不另建机制**：直接调 `DataScopeResolver.resolveZones()` + `DataScopeHelper.apply(qw, FacWorkstation::getZone, zones)`，与 `DeviceService.page()` / `RescueResourceService.brigades()` 同一约定（zone_codes 存中文 zone_name，直接 `qw.in(zone, zones)` 命中）。`resolveZones()` 三态：null=看全（ALL/匿名）、空集=1=0（最小权限）、非空=IN(zones)。
- **ADR-2 端点形态对齐设备域**：`GET /api/v1/workstations` 分页（page/size/zone/online），响应 `Result<WorkstationPageResult>`；list 元素复用既有 `Workstation` DTO（id/name/zone/online），与 `dashboard.openapi.json#/Workstation` 同名、无需新建 item schema。
- **ADR-3 契约 schema 名 == 后端 DTO 类名**：新增 `WorkstationPageResult` DTO 与契约 schema 同名，确保 `check-api-contract.mjs` 精确对拍（沿用 D3/D4 写侧已验证路径：同名才真校验，异名一律豁免）。
- **ADR-4 双端点口径一致**：`DashboardService.workstations()` 同步套防区过滤（注入 `DataScopeResolver`），避免"列表端点过滤、概览计数看全"的权限分裂；`overview().onlineWorkstation` 由此自动变为防区子集计数。
- **ADR-5 映射就近**：`FacWorkstation → Workstation` 映射逻辑从 `DashboardService.toWorkstation` 抽为 `WorkstationService` 内私有方法（或复用同一映射），避免两处字段不一致。

## 非目标（本期不做）

- 不为工作站建写端点（与视频墙 ADR 一致：只读资源）。
- 不改 `fac_workstation` 表结构、不接入真实 MDM 工位编码。
- 不处理 ② 无防区核心表（`fac_alarm`/`fac_video_camera`/`fac_major_hazard` 补列 + 回填）——那依赖产品定 location→防区 规则，另行立项。

## 风险

- 非 ALL 用户 zone_codes 为空 → `1=0` → 列表与概览计数均为 0（最小权限，符合 R1 设计；上线前由运维分配防区数据，非代码缺陷）。
- `DashboardService` 现有 10s TTL 缓存仍适用（workstations 只读，过滤后结果仍可缓存）；注入 resolver 后缓存键无需变（按登录用户维度由 resolver 自身 Caffeine 缓存隔离）。
- 前端 `dashboard.ts` 消费 `/dashboard/workstations` 行为变化（按防区过滤）属预期的数据权限收敛，无需前端改码；仅 `gen:api-types` 新增 `WorkstationPageResult` 类型。
