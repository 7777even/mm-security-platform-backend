# Spec Delta: tv-zone-linkage（工业电视防区联动与设备/历史回放）

> 本变更新建/扩展了 `tv` capability：在既有「录像截图采集入库 / 主动采集 / 确认」之上，新增「跨域告警关联抓拍」「防区联动归属（L4）」「设备与历史回放」能力。

## ADDED Requirements

### Requirement: 跨域告警关联抓拍
系统 SHALL 在 `POST /api/v1/tv/snapshots` 入库请求上支持可选 `alarmId`（Long）与 `alarmType`（PRODUCTION / FIRE / PERIMETER），将抓拍绑定到具体告警，并在 `GET /api/v1/tv/snapshots` 提供按 `alarmId` / `alarmType` 过滤，使生产告警详情可精准内嵌关联抓拍。

#### Scenario: 带告警上下文上报
- **WHEN** 采集端/调用方在 `POST /tv/snapshots` 上送 `alarmId` + `alarmType`
- **THEN** 系统落库 `fac_tv_snapshot.alarm_id` / `alarm_type`（B3 包络 `code=0`），其余入库行为不变

#### Scenario: 按告警反查抓拍
- **WHEN** 调用方请求 `GET /tv/snapshots?alarmId=<id>`
- **THEN** 系统仅返回绑定该告警的快照分页（B3 包络 `code=0`）

### Requirement: 防区联动归属（L4 库结构）
系统 SHALL 在 `fac_tv_monitor` 与 `fac_tv_snapshot` 持有 `zone_code` 列（关联 `sys_zone.zone_code`），设备与快照 DTO 携带 `zoneCode` / `zoneName`，并支持按防区筛选快照。

#### Scenario: 设备带防区归属
- **WHEN** 调用方请求 `GET /tv/monitors`
- **THEN** 返回设备摘要含 `zoneCode` / `zoneName`（按 `monitor_code` 关联 `sys_zone` 取得）

#### Scenario: 按防区筛选快照
- **WHEN** 调用方请求 `GET /tv/snapshots?zone=YIXI`
- **THEN** 系统仅返回 `zone_code='YIXI'` 的快照分页（B3 包络 `code=0`）

### Requirement: 设备与历史回放端点
系统 SHALL 提供 `GET /api/v1/tv/monitors`（设备摘要列表）与 `GET /api/v1/tv/monitors/{code}/snapshots`（单设备快照分页，支持 startTime/endTime 区间），支撑前端「设备/防区筛选 + 历史回放」二级页；并扩展 `GET /tv/snapshots` 支持 `monitorCode` / `zone` / `startTime` / `endTime` 过滤。

#### Scenario: 设备列表
- **WHEN** 已鉴权用户请求 `GET /tv/monitors`
- **THEN** 返回全部监控设备摘要（含在线状态、部门、防区）

#### Scenario: 单设备历史快照
- **WHEN** 已鉴权用户请求 `GET /tv/monitors/{code}/snapshots?startTime=&endTime=`
- **THEN** 返回该设备在时间区间内的快照分页（B3 包络 `code=0`）

#### Scenario: 扩展列表过滤
- **WHEN** 已鉴权用户请求 `GET /tv/snapshots?monitorCode=&zone=&startTime=&endTime=`
- **THEN** 系统在原有 alarmId/alarmType 过滤基础上叠加设备/防区/时间区间过滤

## 关联 Spec
- 目标 spec 文件：`openspec/specs/tv/spec.md`（本变更 AMEND 该 capability，新增上述三条 Requirement）。
