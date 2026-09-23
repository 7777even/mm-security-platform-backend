# Spec Delta: fire-facility-monitor-report

## Capability: fire-facility-monitor（消防设施运行监测）

### ADDED — 消防设施监测运行数据上报写回

- 系统 SHALL 提供 `POST /api/v1/fire-facility/monitors/report`，接收设备/采集/模拟上报，按 `key_code` upsert `fac_fire_facility_monitor`（total/online/offline/fault/monitor_status/last_report_time）并整体替换 `fac_fire_facility_param`，返回刷新后全量 `FireFacilityMonitorResult`。属 L3：复用既有表，无 DDL、无新权限码、无新迁移。
- 请求体 SHALL 含非空 `items`；每项 `key` 非空、`status` ∈ {`正常`,`告警`,`离线`,`在线`}、计数非负；新建项 `facilityType` 必填，任一不过 → B3 `code=100`。
- 端点 SHALL 要求权限码 `fire-facility:handle`（复用 V68 已授权角色，与 `PUT /faults/{faultId}` 同码），`@RealtimeSync(domain="fire-facility.monitor")` 广播 `fire-facility.monitor.changed`，复用统一 `/ws/alarm` 总线。
- upsert SHALL 命中即局部更新、未命中即 insert（sort_no=max+1，计数缺省 0，status 缺省 正常）；`last_report_time` 每次上报刷新。
- 传 `params` SHALL 先 `DELETE` 旧参数再按顺序 `INSERT`，实现整体替换快照。

#### Scenario: 上报已有监测点

- **GIVEN** `key_code=MON-001` 已存在监测点（online=10 / total=12）
- **WHEN** 以 `{"items":[{"key":"MON-001","online":11,"total":12}]}` 请求上报
- **THEN** 该行 online 更新为 11，响应 `code=0` 且 `data.typeOptions` 与全部卡片刷新

#### Scenario: 上报新监测点

- **WHEN** 以含新建项（带 `facilityType`）的请求上报
- **THEN** 插入新行（sort_no=max+1，status 缺省 正常），返回刷新后全量

#### Scenario: 参数非法

- **WHEN** 请求 items 中 `status` 为非枚举值或计数为负
- **THEN** 返回 B3 `code=100`，不写库
