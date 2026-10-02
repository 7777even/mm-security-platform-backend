# Design: 应急预案目录台账写端点

## 数据模型

复用既有 `fac_emergency_plan_catalog`（V39 落地）：id BIGINT 主键, plan_code VARCHAR, label VARCHAR,
plan_name VARCHAR, can_switch INTEGER, is_current INTEGER, sort_no INTEGER。无新表。

## 写路径

`EmergencyController` → `EmergencyPlanService.planCatalogRows / createPlanCatalogRow / updatePlanCatalogRow / deletePlanCatalogRow`
→ `FacEmergencyPlanCatalogMapper`。`createPlanCatalogRow` 用 `LedgerIdSupport.nextId` 分配 id。

## 实时

`@RealtimeSync(domain = "emergency.plan-catalog")` 在 create/update/delete 后广播；前端 `useDomainAutoRefresh` 订阅重拉。

## 权限

V97 在 `fm-emergency` 下登记按钮 `fm-emergency-plan-catalog-write`（perm `emergency:plan-catalog:write`），授权
ADMIN / COMMANDER / SCHEDULER；`@RequireAuth(perm = ...)` 兜底 403。
