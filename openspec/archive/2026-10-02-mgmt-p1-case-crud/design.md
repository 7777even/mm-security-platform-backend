# Design: 事故案例库台账写端点

## 数据模型

新表 `fac_emergency_case`（id BIGINT 主键, title VARCHAR(200) NOT NULL, accident_type VARCHAR(100),
location VARCHAR(200), occurred_at TIMESTAMP, summary VARCHAR(2000), lessons VARCHAR(2000), create_time TIMESTAMP）。
三方言结构一致（BIGINT/VARCHAR/TIMESTAMP 通用）。

## 写路径

`EmergencyController` → `EmergencyService.caseList / createCase / updateCase / deleteCase` → `FacEmergencyCaseMapper`。
`createCase` 用 `LedgerIdSupport.nextId(mapper, FacEmergencyCase::getId, FacEmergencyCase::getId)` 分配 id。
`updateCase` / `deleteCase` 不存在抛 `BusinessException(NOT_FOUND)`。

## 实时

`@RealtimeSync(domain = "emergency.case")` 在 create/update/delete 后广播；前端 `useDomainAutoRefresh('emergency.case', load)` 订阅重拉。

## 权限

V96 在 `fm-emergency` 下登记按钮 `fm-emergency-case-write`（perm `emergency:case:write`），授权
ADMIN / COMMANDER / SCHEDULER；`@RequireAuth(perm = ...)` 兜底 403。
