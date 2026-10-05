# fire-situation Specification

## Purpose

大屏「消防态势」只读能力：装置区消防要素聚合、地图标记点与监管对象列表。数据真源归一为
`fac_fire_facility_monitor` 矩阵（设施类型 × 装置区），保证「装置区设备数」与「监测总数」自洽。

本 capability 的 Requirement 来自**已归档 Change 的 spec-delta 回填**（2026-10-06 治理批次），
来源：`openspec/archive/2026-09-23-fire-facility-zone-unification/`。

## Endpoints

| Method | Path | 权限 | 说明 |
| ------ | ---- | ---- | ---- |
| GET | `/api/v1/fire-situation/areas` | 登录即可 | 装置区列表（含按防区聚合的 `equipment` 消防设备数） |
| GET | `/api/v1/fire-situation/markers` | 登录即可 | 消防态势地图标记点 |
| GET | `/api/v1/fire-situation/monitored-objects` | 登录即可 | 消防监管对象列表 |

## Requirements

### Requirement: 装置区消防要素与监测真源归一

`fac_fire_facility_monitor` SHALL 新增 `zone_code` / `zone_name` 列，数据由 12 行（按类型）扩展为
168 行（14 区 × 12 类型）；`GET /api/v1/fire-situation/areas` 返回的各装置区 `equipment`（消防设备数）
SHALL 按 `zone_code` 聚合自 `fac_fire_facility_monitor`，与监测总数 983 自洽
（**原读 `fac_fire_monitor_area.equipment` 的 1399 口径作废**）。

`fac_fire_facility_param` SHALL 新增 `key_code` 列，参数关联由 `monitor_id` 改为 `key_code`（一对多行适配）。

#### Scenario: 装置区设备数与监测总数真源归一
- **GIVEN** 监测表按 (区, 类型) 矩阵存储，按类型求和 = 983、按区求和 = 983
- **WHEN** 前端请求 `GET /api/v1/fire-situation/areas`
- **THEN** 返回的 14 个装置区 `equipment` 之和 = 983，且其中炼油一部装置区 = 92

### Requirement: 消防态势只读端点

系统 SHALL 提供 `GET /api/v1/fire-situation/areas`、`GET /api/v1/fire-situation/markers`、
`GET /api/v1/fire-situation/monitored-objects` 三个只读端点（登录即可），
供大屏消防态势面板落图与列表渲染；响应统一 B3 包络（HTTP 200 + `code=0`），
只读端点不产生广播事件，未鉴权返回 401。
