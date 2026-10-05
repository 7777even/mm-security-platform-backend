# device Specification

## Purpose

设备台账能力：以 `fac_device` 承接设备台账的列表、详情与全量 CRUD，供 mgmt 管理端「设备管理」页、
大屏地图设备落图与移动端消费。物理主键为 **20 位 MDM 编码 `device_code`（不自增）**。

本 capability 的 Requirement 来自**已归档 Change 的 spec-delta 回填**（2026-10-06 第二批），
来源：`openspec/archive/2026-10-03-mgmt-device-comm-record-crud-realtime/`。

## Endpoints

| Method | Path | 权限 | 说明 |
| ------ | ---- | ---- | ---- |
| GET | `/api/v1/devices` | 登录即可 | 设备分页列表（含防区过滤） |
| POST | `/api/v1/devices` | `device:write` | 设备新增 |
| GET | `/api/v1/devices/{code}` | 登录即可 | 设备详情（20 位 MDM 编码解析） |
| PUT | `/api/v1/devices/{code}` | `device:write` | 设备更新 |
| DELETE | `/api/v1/devices/{code}` | `device:write` | 设备删除（**软删除**） |

## Requirements

### Requirement: 设备台账 CRUD 与广播

系统 SHALL 提供 `POST /api/v1/devices`、`PUT /api/v1/devices/{code}`（权限码 `device:write`）；
`DeviceService` 的 createDevice / updateDevice / deleteDevice 在事务提交成功后 SHALL 广播 `device.changed`。

- 写请求体 `DeviceWriteRequest` 含 7 字段：deviceCode / deviceName / deviceType / zone / status / lat / lon；
- `DELETE /api/v1/devices/{code}` 为**软删除**（设备台账留痕，不做物理删除）；
- 读端点 `/devices` GET 与 `/devices/{code}` GET 响应 schema 不变（写端点不变更既有读契约）。

### Requirement: 20 位 MDM 编码为主键

设备物理主键 SHALL 为 20 位 MDM `device_code`，`@TableId(type = IdType.INPUT)` 由客户端给定，
**不依赖数据库自增**（自增序列滞后会撞主键 409）；`GET /devices/{code}` 按该编码解析详情。

### Requirement: ABAC 防区过滤（已接线）

`FacDevice` SHALL 实现 `ZoneAware` 并暴露 `getZoneName()`（字段 `zone`，须与 `sys_zone.zone_name` 对齐），
使设备域写广播按防区过滤；为空时该事件 `zones == null` → fail-open（推给全部已认证会话）。

### Requirement: 列表防区过滤与失败路径

`GET /api/v1/devices` SHALL 复用 data_scope 行级 ABAC（`DataScopeHelper.apply(qw, 'zone', zones)`），
非 ALL 角色仅见其 `zone_codes` 内设备。失败路径走 B3 包络（HTTP 200 + `code != 0`）：
参数非法 100、不存在 404、编码冲突 409；未鉴权 401、无权限 403。
