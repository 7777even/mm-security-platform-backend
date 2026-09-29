# Design: 工业电视监控点台账 CRUD + 防区归属编辑（tv-monitor-crud）

## 目标与约束
- 目标：管理后台「设备管理」域新增监控点（摄像头台账）写能力——新增/编辑/删除 + 防区归属 `zone_code` 编辑；并经 `@RealtimeSync(domain="tv.monitor")` 广播。
- 硬约束：零下行控制红线（台账维护非设备控制）；权限码解耦（create/update/delete 三独立码）；复用 `fac_tv_monitor`（不改表结构、不引入新实体）。

## 架构与方案

### 1. 权限码与种子（V87 三方言迁移）
- 新增 `tv:monitor:create/update/delete`；授权 `ADMIN` + `COMMANDER/SCHEDULER/TEAM_LEADER/INNER_OPER/OUTER_OPER`（照抄 V83 `tv:snapshot:ack` 范式）。
- V87 三方言（h2/postgresql/dameng）`INSERT` 权限 + 角色权限关联。

### 2. DTO 与更新语义
- `TvMonitorUpsertRequest`：`monitorCode` 必填，其余（`online/integrity/monitorType/department/zoneCode/location/height/angle`）可选。
- `updateMonitor` 仅覆盖非空字段（read-modify-write）；`online` 为 `Boolean` 包装——传 `false` 置离线、`null` 不更新，避免误清零。

### 3. 端点（TvController，均 @RequireAuth + perm）
- `POST /api/v1/tv/monitors`（`tv:monitor:create`）
- `PUT /api/v1/tv/monitors/{code}`（`tv:monitor:update`）
- `DELETE /api/v1/tv/monitors/{code}`（`tv:monitor:delete`）
- 重复 `monitorCode` → B3 包络 `code!=0`（非 400）。

### 4. 实时广播
- 三端点均 `@RealtimeSync(domain="tv.monitor")`，写后广播 `tv.monitor.changed`，大屏地图撒点 + 管理页列表自动刷新。

## 决策记录（ADR）
- ADR-1 三权限码解耦：增/改/删权限分离，满足最小授权。
- ADR-2 update 跳过空字段：监控点编辑常只改防区，避免把未传字段清零。

## 风险与缓解
| 风险 | 缓解 |
| --- | --- |
| 三方言权限种子语法差异 | V87 按方言编写；h2 实跑；PG/DM 静态对拍 |
| 契约守门误报差异 | `check-api-contract.mjs --strict` 归零；前端 `tv.openapi.json` 四铁律 PASS |
| 端到端冒烟未跑 | 本期补隔离实例 curl（见遗留收口） |

## 依赖
- 上游：`FacTvMonitor` / `TvService` / `TvController`；V87 迁移。
- 下游：前端 `TvMonitorMgmtView.vue` + `tv.openapi.json` 契约（前端同名 Change）。
