# Spec Delta: tv-monitor-crud

## Capability: tv-monitor

> 工业电视监控点（fac_tv_monitor）的台账读写能力。既有 capability 仅覆盖只读（GET 列表/详情）；本变更补齐「新增/编辑/删除 + 防区归属编辑」写侧。

### ADDED

#### Requirement: 维护监控点台账
系统 SHALL 提供 `POST /api/v1/tv/monitors`、`PUT /api/v1/tv/monitors/{code}`、`DELETE /api/v1/tv/monitors/{code}` 三个端点，允许持有对应 `tv:monitor:create/update/delete` 权限的操作员维护监控点台账（含防区归属 `zone_code` 编辑），并经 `@RealtimeSync(domain="tv.monitor")` 广播，使大屏地图撒点与管理页列表自动刷新。

##### Scenario: 操作员新增监控点（成功）
- **WHEN** 已鉴权且持有 `tv:monitor:create` 的操作员提交 `monitorCode` 等字段
- **THEN** 系统落库 `fac_tv_monitor`，返回 `TvMonitorSummary`（HTTP 200，B3 包络 `code=0`）
- **AND** 广播 `tv.monitor.changed`，前端管理页与大屏重拉监控点列表

##### Scenario: 重复编码被拒（B3 包络）
- **WHEN** 提交请求 `monitorCode` 已存在
- **THEN** 返回 HTTP 200 且 B3 包络 `code!=0`（非 400），提示编码已存在

##### Scenario: 编辑仅覆盖非空字段
- **WHEN** 持有 `tv:monitor:update` 的操作员提交部分字段（含 `online=false`）
- **THEN** 系统仅更新非空字段，传 `false` 即将该点置离线，返回更新后 `TvMonitorSummary`

##### Scenario: 删除监控点（成功）
- **WHEN** 持有 `tv:monitor:delete` 的操作员提交存在的 `code`
- **THEN** 系统物理删除该记录，返回 HTTP 200，并广播 `tv.monitor.changed`

##### Scenario: 未鉴权 / 无权限
- **WHEN** 请求未携带 token 或不持有对应权限码
- **THEN** 返回 401 / 403
