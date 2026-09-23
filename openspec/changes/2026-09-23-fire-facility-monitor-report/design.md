# Design: fire-facility-monitor-report（后端）

## 数据流

```
设备/采集/模拟上报
  └─ POST /api/v1/fire-facility/monitors/report  (perm=fire-facility:handle)
       body: { items: [ { key, facilityType?, total?, online?, offline?, fault?, status?, lastReportTime?, params? } ] }
            │
            ├─ 校验：items 非空；每项 key 非空；status ∈ {正常,告警,离线,在线}；计数非负；新建项 facilityType 必填
            │         ── 任一不过 → BusinessException(PARAM_INVALID=100) → B3 包络
            │
            ├─ 按 key_code 查 fac_fire_facility_monitor
            │     ├─ 命中 → 局部更新计数/状态 + last_report_time=上报时间；updateById
            │     └─ 未命中 → insert（sort_no = max+1，计数缺省 0，status 缺省 正常）
            │
            ├─ 若传 params → DELETE fac_fire_facility_param WHERE monitor_id=?，再按顺序 insert（sort_no 1..n）
            │
            └─ 返回 monitors(null) 的 FireFacilityMonitorResult（typeOptions + 全部卡片）
```

## 关键决策

- **复用既有表、零 DDL（L3）**：`total_count/online_count/offline_count/fault_count/monitor_status/last_report_time` 与 `label/value_text/tone/sort_no` 列 V20 已具备，无需新迁移。
- **复用 `fire-facility:handle` 权限**：与 `PUT /faults/{faultId}` 同一权限码（V68 已授权 ADMIN 及岗位角色），避免新增权限迁移与 `sys_menu.perm_code` 种子；写端点显式带 `perm=`，不进 `check-endpoint-authz.mjs` 的 ALLOWLIST。
- **upsert 语义（read-modify-write）**：`key_code` 为逻辑唯一键（非 DB 唯一约束，避免动表）；命中即局部更新——不传不覆盖；`last_report_time` 每次上报刷新（体现「上报」动作）。
- **params 整体替换**：监控参数是卡片的一个整体快照，传参即整表重建，避免逐条 diff。
- **返回全量刷新结果**：上报可能同时改多张卡片，直接返回 `monitors(null)` 供前端即时回填，与 `updateFault` 返回单条的差异在于本域是多卡片聚合。
- **实时**：`@RealtimeSync(domain="fire-facility.monitor")` 复用既有切面，发布 `fire-facility.monitor.changed`，大屏订阅 `subscribeDomainChange` 去抖刷新。
