# Design: 三域写端点与实时广播

## 决策

### 1. 主键分配分三种，不可套用同一套写法
| 域 | 实体主键 | 分配方式 | 判重 |
| --- | --- | --- | --- |
| hazard | `Long`，`IdType.AUTO` | `LedgerIdSupport.nextId`（max(id)+1） | 服务端分配，无需判重 |
| special-operation | `Long`，`IdType.AUTO` | `LedgerIdSupport.nextId` + `nextSortNo` | 服务端分配，无需判重 |
| hazard.point | `String`，业务编码 | 请求体给定 | `selectById` 命中即抛 `ResultCode.CONFLICT`(409) |

`LedgerIdSupport` 不可用于 `hazard.point`：其签名要求 `Function<T, Long>` 取值器，而主键是 String。

### 2. 更新不覆盖主键（本批修复的缺陷）
`PUT /monitoring/points/{id}` 是子路径，主键由路径决定。`applyPointFields` **不写** `p.setId(in.getId())`——若以请求体 id 覆盖，`updateById` 会在 body 与 path 不一致时更新到另一行（命中则改错数据，未命中则静默 0 行却返回成功）。新建路径由 `createPoint` 单独 `setId`。
该项已固化在 `HazardServiceTest#updatePoint_bodyIdDoesNotOverridePathPrimaryKey`。

### 3. 写方法必须失效缓存
`HazardService` 的 `listMajorHazards` / `listMonitoringPoints` 走 Caffeine（TTL 60s）。读端点原本只有 GET，注释曾声明「只读无写入口，TTL 即一致窗口」——本批该前提不再成立。写方法统一 `invalidateAll()`：否则订阅端收到 `.changed` 后重拉仍命中旧缓存，实时刷新表现为「改了没反应」。`SpecialOperationService` 读端点无缓存（每次 `selectPage`），写后无需失效。

### 4. 三方言迁移只加列
V105 在 h2 / postgres / dameng 三份文件中仅 `ADD COLUMN version BIGINT DEFAULT 0`，不动种子数据与自增序列——避免触及既有序列滞后问题（id 分配已由代码侧的 `LedgerIdSupport` 兜住）。

### 5. 授权口径
沿用前两批：写端点 `@RequireAuth(role = "ADMIN")`。`check-endpoint-authz.mjs` 要求写端点具备 role/perm 约束（或进 ALLOWLIST 并写明理由），本批三者均带 role，故**无需**登记豁免。

## 风险与代价
- `LedgerIdSupport` 的 max+1 在并发插入下可能算出相同 id 而撞主键。台账属低频人工维护，失败返回明确 409 提示重试，优于「每次新增都必失败」的序列滞后现状。
- 前端三视图只订阅（`useDomainAutoRefresh`）不做写 UI，写能力暂由 ADMIN 直连端点验证。后续若开放管理端写，应同步补权限码、`sys_menu` 按钮种子与 `v-permission`。
