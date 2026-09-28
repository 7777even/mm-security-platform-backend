# Spec Delta: tv-collector-rbac

## Capability: tv

### MODIFIED

#### Requirement: 录像截图采集入库
`POST /api/v1/tv/snapshots` 的采集端点 SHALL 要求细粒度权限码 `video:snapshot:create`
（V80 已登记并授权 ADMIN 及岗位角色），取代此前「仅登录态 + ALLOWLIST 豁免」，以满足细粒度 RBAC。

##### Scenario: 无创建权限
- **WHEN** 已鉴权但不持有 `video:snapshot:create`
- **THEN** 返回 403

### ADDED

#### Requirement: 录像截图主动采集（TvCollector）
系统 SHALL 提供默认关闭的采集器 `TvCollector`（`tv.collector.enabled=false`），开启后经 `@Scheduled`
周期从 `TvSourceAdapter.fetchSnapshots()` 拉取真实工业电视设备截图帧，复用 `TvService.submitSnapshots`
批量落库并**单次**广播 `tv.snapshot.changed`。未接入真实源时 `NoOpTvSourceAdapter` 返回空列表，采集器
跳过上报，绝不向截图表写入假数据。

##### Scenario: 采集器未启用
- **WHEN** `tv.collector.enabled=false`（默认）
- **THEN** `TvCollector` 不注册为 Bean，不产生任何上报

##### Scenario: 采集器启用且适配器有数据
- **WHEN** `tv.collector.enabled=true` 且 `TvSourceAdapter` 返回有效截图帧
- **THEN** 系统批量落库并广播一次 `tv.snapshot.changed`

##### Scenario: 适配器无数据 / 异常
- **WHEN** 适配器返回空或抛异常
- **THEN** 采集器跳过本次上报并记录日志，不中断调度、不写假数据
