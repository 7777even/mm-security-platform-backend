# tv Specification

## Purpose

工业电视大屏（fm-tv）的录像截图采集入库与设备采集能力。覆盖录像截图「设备/采集端自助上报」与「后端采集器主动拉取」两条入库链路 → 落库 `fac_tv_snapshot` → 经 `@RealtimeSync(domain="tv.snapshot")` 广播 `tv.snapshot.changed` 驱动前端 `TvSnapshotFeedPanel` 实时刷新。落库表 `fac_tv_snapshot`（V78 建表），按钮级权限码 `video:snapshot:ack`（V79 确认）/ `video:snapshot:create`（V80 采集）。

## Endpoints

| Method | Path | 权限 | 说明 |
| ------ | ---- | ---- | ---- |
| GET | `/api/v1/tv/snapshots` | 登录即可 | 分页列表（最新在前） |
| POST | `/api/v1/tv/snapshots` | `video:snapshot:create` | 录像截图采集入库 |
| GET | `/api/v1/tv/snapshots/{id}/snapshot` | 登录即可 | 截图 JPEG 字节（无则 404） |
| POST | `/api/v1/tv/snapshots/{id}/ack` | `video:snapshot:ack` | 确认截图（PENDING→ACKED） |
| GET | `/api/v1/tv/monitors` | 登录即可 | 监控设备摘要列表（含防区归属） |
| GET | `/api/v1/tv/monitors/{code}/snapshots` | 登录即可 | 单设备快照分页（支持 startTime/endTime 区间） |
| GET | `/api/v1/tv/snapshots` | 登录即可 | 列表扩展过滤：`alarmId`/`alarmType`/`monitorCode`/`zone`/`startTime`/`endTime` |

> **L4 库结构（V86）**：`fac_tv_monitor` / `fac_tv_snapshot` 新增 `zone_code` 列（关联 `sys_zone.zone_code`），设备与快照携带 `zoneCode` / `zoneName`，支持按防区筛选。详见 Change `2026-09-29-tv-zone-linkage`。

所有响应统一 B3 包络（HTTP 200 + `code=0` 为成功；参数/业务失败返回 HTTP 200 但 `code!=0`，**不返回 400**）。未鉴权返回 401，无权限返回 403。

## Requirements

### Requirement: 录像截图采集入库

系统 SHALL 提供 `POST /api/v1/tv/snapshots` 端点，允许持有 `video:snapshot:create` 权限的操作员或经授权的采集终端上报录像截图（base64 JPEG），解码后落库 `fac_tv_snapshot`（`review_status='PENDING'`、`source` 默认 `DEVICE`），并以 `@RealtimeSync(domain="tv.snapshot")` 广播 `tv.snapshot.changed`，使订阅面板自动刷新。

#### Scenario: 自助上报成功

- **WHEN** 已鉴权且持有 `video:snapshot:create` 的操作员提交 `monitorCode` + `imageBase64`
- **THEN** 系统落库一条 `PENDING` 截图，返回 `TvSnapshotIngestResult`（B3 包络 `code=0`）并广播 `tv.snapshot.changed`

#### Scenario: 无创建权限

- **WHEN** 已鉴权但不持有 `video:snapshot:create`
- **THEN** 返回 403

### Requirement: 录像截图主动采集（TvCollector）

系统 SHALL 提供默认关闭的采集器 `TvCollector`（`tv.collector.enabled=false`），开启后经 `@Scheduled` 周期从 `TvSourceAdapter.fetchSnapshots()` 拉取真实工业电视设备截图帧，复用 `TvService.submitSnapshots` 批量落库并**单次**广播 `tv.snapshot.changed`（避免 N 条上报触发 N 次刷新）。未接入真实源时 `NoOpTvSourceAdapter` 返回空列表，采集器跳过上报，绝不向截图表写入假数据。

#### Scenario: 采集器未启用

- **WHEN** `tv.collector.enabled=false`（默认）
- **THEN** `TvCollector` 不注册为 Bean，不产生任何上报

#### Scenario: 采集器启用且适配器有数据

- **WHEN** `tv.collector.enabled=true` 且 `TvSourceAdapter` 返回有效截图帧
- **THEN** 系统批量落库并广播一次 `tv.snapshot.changed`

#### Scenario: 适配器无数据 / 异常

- **WHEN** 适配器返回空或抛异常
- **THEN** 采集器跳过本次上报并记录日志，不中断调度、不写假数据

### Requirement: 录像截图确认

系统 SHALL 提供 `POST /api/v1/tv/snapshots/{id}/ack` 端点，允许持有 `video:snapshot:ack` 的操作员将截图的 `review_status` 由 `PENDING` 推进为 `ACKED`，并以 `@RealtimeSync(domain="tv.snapshot")` 广播刷新。

#### Scenario: 确认成功

- **WHEN** 已鉴权且持有 `video:snapshot:ack` 的操作员提交确认
- **THEN** 目标截图状态变为 `ACKED`，返回 `TvSnapshotAckResult`（B3 包络 `code=0`）并广播 `tv.snapshot.changed`

#### Scenario: 无确认权限

- **WHEN** 已鉴权但不持有 `video:snapshot:ack`
- **THEN** 返回 403

### Requirement: 跨域告警关联抓拍

系统 SHALL 在 `POST /api/v1/tv/snapshots` 入库请求上支持可选 `alarmId`（Long）与 `alarmType`（PRODUCTION / FIRE / PERIMETER），将抓拍绑定到具体告警，并在 `GET /api/v1/tv/snapshots` 提供按 `alarmId` / `alarmType` 过滤，使生产告警详情可精准内嵌关联抓拍。

#### Scenario: 带告警上下文上报

- **WHEN** 采集端/调用方在 `POST /tv/snapshots` 上送 `alarmId` + `alarmType`
- **THEN** 系统落库 `fac_tv_snapshot.alarm_id` / `alarm_type`（B3 包络 `code=0`），其余入库行为不变

#### Scenario: 按告警反查抓拍

- **WHEN** 调用方请求 `GET /tv/snapshots?alarmId=<id>`
- **THEN** 系统仅返回绑定该告警的快照分页（B3 包络 `code=0`）

### Requirement: 防区联动归属（L4 库结构）

系统 SHALL 在 `fac_tv_monitor` 与 `fac_tv_snapshot` 持有 `zone_code` 列（关联 `sys_zone.zone_code`，由 V86 迁移新增），设备与快照 DTO 携带 `zoneCode` / `zoneName`，并支持按防区筛选快照。

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
