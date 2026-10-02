# Design: 应急预案主记录台账写端点

## 数据模型

复用既有 `fac_emergency_plan`（V18 落地）：id BIGINT 主键, tab_key, plan_name, accident_type, facility,
domain, nuclear BOOLEAN, is_active BOOLEAN, invoke_count INTEGER, last_invoked_at TIMESTAMP, sort_no INTEGER。无新表。

## 写路径

`EmergencyController` → `EmergencyPlanService.planMetaList / createPlan / updatePlan / deletePlan` → `FacEmergencyPlanMapper`。
`createPlan` 用 `LedgerIdSupport.nextId` 分配 id；新行 `invokeCount` 置 0（调用次数由 `invokePlan` 维护，不在此处清零）。

## 实时

`@RealtimeSync(domain = "emergency.plan")` 在 create/update/delete 后广播；前端 `useDomainAutoRefresh` 订阅重拉。

## 权限

V98 在 `fm-emergency` 下登记按钮 `fm-emergency-plan-write`（perm `emergency:plan:write`），授权
ADMIN / COMMANDER / SCHEDULER；`@RequireAuth(perm = ...)` 兜底 403。
