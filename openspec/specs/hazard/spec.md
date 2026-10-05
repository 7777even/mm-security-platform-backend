# hazard Specification

## Purpose

重大危险源与监测点位能力：`fac_major_hazard`（重大危险源台账 + 详情）、`fac_monitoring_point`
（监测点位）、监测报警，以及装置区设施详情。承接 mgmt 管理端「重大危险源」「监测点位」模块全量 CRUD
与大屏地图落图（`MajorHazardMapOverlay`）的只读消费。

本 capability 的 Requirement 来自**已归档 Change 的 spec-delta 回填**（2026-10-06 治理批次），
来源：`openspec/archive/2026-10-04-mgmt-hazard-special-operation-crud-realtime/`（hazard / hazard.point 部分）。

## Endpoints

| Method | Path | 权限 | 说明 |
| ------ | ---- | ---- | ---- |
| GET | `/api/v1/hazards` | 登录即可 | 重大危险源列表 |
| POST | `/api/v1/hazards` | `hazard:write` | 重大危险源新增 |
| GET | `/api/v1/hazards/{id}` | 登录即可 | 重大危险源详情 |
| PUT | `/api/v1/hazards/{id}` | `hazard:write` | 重大危险源更新（乐观锁） |
| DELETE | `/api/v1/hazards/{id}` | `hazard:write` | 重大危险源删除 |
| GET | `/api/v1/monitoring/points` | 登录即可 | 监测点位列表 |
| POST | `/api/v1/monitoring/points` | `hazard:point-write` | 监测点位新增（主键重复 → 409） |
| PUT | `/api/v1/monitoring/points/{id}` | `hazard:point-write` | 监测点位更新（乐观锁） |
| DELETE | `/api/v1/monitoring/points/{id}` | `hazard:point-write` | 监测点位删除 |
| GET | `/api/v1/monitoring/alarms` | 登录即可 | 监测报警列表 |
| GET | `/api/v1/facilities/detail` | 登录即可 | 装置区设施详情 |

## Requirements

### Requirement: 重大危险源 CRUD 与广播

系统 SHALL 提供 `POST /api/v1/hazards`、`PUT /api/v1/hazards/{id}`、`DELETE /api/v1/hazards/{id}`
（权限码 `hazard:write`）；`HazardService` 的 createHazard / updateHazard / deleteHazard 在事务提交
成功后 SHALL 广播 `hazard.changed`。

- 写请求体 `MajorHazardWriteRequest` 含 10 字段：name / level / rValue / monitorCount / videoCount /
  enterprise / category / code / longitude / latitude；
- 删除不存在 → B3 `NOT_FOUND`；成功写操作均须广播，使订阅面板自动重拉。

### Requirement: 监测点位 CRUD 与广播

系统 SHALL 提供 `POST /api/v1/monitoring/points`（**遇主键重复返回 409**）、
`PUT /api/v1/monitoring/points/{id}`、`DELETE /api/v1/monitoring/points/{id}`
（权限码 `hazard:point-write`）；`HazardService` 的 createPoint / updatePoint / deletePoint
在事务提交成功后 SHALL 广播 `hazard.point.changed`。

- 写请求体 `MonitoringPointWriteRequest` 含 8 字段：id / name / category / status / lastTime / org /
  longitude / latitude；
- **`applyPointFields` SHALL NOT 以请求体 id 覆盖实体主键**；PUT 子路径的主键由路径变量决定。

### Requirement: 乐观锁

`fac_major_hazard` / `fac_monitoring_point` SHALL 带 `version BIGINT DEFAULT 0` 列（迁移 V105，三方言一致），
实体相应补 `@Version`；并发冲突返回 409，禁止静默后写覆盖。

### Requirement: 只读消费端点不变

读端点响应 schema 不变：`MajorHazardItem` / `MajorHazardDetail` / `MonitoringPoint` /
`MonitoringAlarm` / `FacilityDetailInfo`。
**重大危险源的 7 类 JSON 明细列仍不经写端点编辑**（只编辑主记录字段）。

### Requirement: 失败路径 B3 包络

所有失败路径走 B3 包络（HTTP 200 + `code != 0`）：参数非法 100、不存在 404、主键冲突 409；
未鉴权 401、无权限 403。写操作 MUST NOT 触发任何物理设备下行。
