# uplink-audit Specification

## Purpose

上行回传域：审计埋点落库与现场采集回传。由 Change `implement-remaining-contracts`（已归档）回填。

## Requirements

### Requirement: 审计日志批量落库

系统须提供 `POST /api/v1/audit/log`，将前端路由/操作审计埋点批量写入 `fac_audit_log`。

#### Scenario: 批量上报

- **WHEN** `POST /api/v1/audit/log` 携带审计条目集合
- **THEN** 条目落库并以 B3 包络返回

### Requirement: 现场采集回传受理

系统须提供 `POST /api/v1/field-reports`，受理防爆手机现场采集回传。

#### Scenario: 回传受理

- **WHEN** `POST /api/v1/field-reports` 携带回传内容
- **THEN** 返回 HTTP 204 No Content（契约明示 bypass B3 包络），数据落库 `fac_field_report`
- **AND** 服务端覆盖 reporter 字段为当前认证用户，不信任客户端自报身份
