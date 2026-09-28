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
