# map-geojson Specification

## Purpose

地图一张图的报警/设备点位服务。由 Change `implement-remaining-contracts`（已归档）回填。

## Requirements

### Requirement: 报警点位 GeoJSON

系统须提供 `GET /api/v1/map/alarms`，返回报警点位的 GeoJSON FeatureCollection。

#### Scenario: 报警点位查询

- **WHEN** `GET /api/v1/map/alarms`
- **THEN** B3 包络返回 GeoJSON FeatureCollection，Feature 对应报警点位

### Requirement: 设备点位 GeoJSON

系统须提供 `GET /api/v1/map/devices`，返回设备点位的 GeoJSON FeatureCollection。

#### Scenario: 设备点位查询

- **WHEN** `GET /api/v1/map/devices`
- **THEN** B3 包络返回 GeoJSON FeatureCollection，Feature 对应设备点位

### Requirement: 点位标识使用业务 ID

地图点位 Feature 须使用对外业务标识（报警用 `alarmId`、设备用 20 位 MDM 编码），不暴露 Long 物理主键。

#### Scenario: 标识检查

- **WHEN** 检视 Feature properties
- **THEN** 不存在 Long 自增主键字段
