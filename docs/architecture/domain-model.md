# 领域模型（Domain Model）

> 摘自 `src/main/java/com/sinopec/mmsecurity/entity/` 与 mapper。表名以 `fac_`（业务事实表）/ `sys_`（系统表）前缀区分。改动实体或新增表后须同步本文与 Flyway 增量。

## 1. 聚合划分

```
Auth / RBAC
  ├─ SysUser        (id, username, real_name, role[ADMIN|USER], password, ...)
  └─ SysMenu        (id, menu_key[=fm-*], name, path, sort, allowed_roles["ADMIN,USER"])

Alarm（报警处置）
  ├─ FacAlarm            (alarm_id, device_code, level[1-4], type, status[ACTIVE/ACKED/DISPATCHED/CLOSED],
  │                        occurred_at, location, category, warned, plan_id, deleted)
  └─ FacFireAlarm       (消防报警独立表)

Uplink / Audit（审计与防爆手机）
  ├─ FacFieldReport   (id[客户端UUID], kind, title, note, device_code, media_json,
  │                     status, reporter[服务端覆盖], attempts, last_error, synced_at, created_at)
  └─ FacAuditLog      (路由/操作审计埋点)

Emergency Reference（应急参考表，由硬编码迁 DB）
  ├─ SysEmergencyStrength  (应急力量)
  ├─ SysEmergencyPhone     (通讯录)
  ├─ SysKnowledgeItem      (知识库)
  └─ SysDutyMember         (值班表)

Map / GIS（一张图）
  ├─ FacDevice          (device_code[20位MDM], ...)
  ├─ FacMajorHazard     (重大危险源)
  ├─ FacFacilityDetail  (装置/厂区详情)
  ├─ FacMonitoringPoint (监测点位)
  ├─ FacMonitoringAlarm (监测报警)
  ├─ FacPatrolCamera    (巡检摄像头)
  ├─ FacGateControl     (闸口)
  ├─ FacBollard         (防撞柱)
  ├─ FacSecurityEvent   (安防事件)
  ├─ FacVehicleSearch / FacPersonSearch (检索结果快照)
  └─ FacWorkstation     (工作站)
```

## 2. 关键关系与不变量

- **报警状态机（ACK_FLOW）**：`ACTIVE → ACKED → DISPATCHED → CLOSED`，`ack()` 仅前进一格，禁止跳步 / 回退。闭环写 `CLOSED` 即终态。
- **菜单 RBAC**：`SysMenu.allowed_roles` 逗号分隔，含当前 `UserContext.role()` 才在 `/auth/menus` 返回；当前仅 5 个顶部 `fm-*` 菜单（其余 7 个由前端二级路由承载，不在此表）。
- **现场回传 Reporter 覆盖**：`FacFieldReport.reporter` 由 `UplinkService` 以 `UserContext.username()` **服务端覆盖**，客户端传入值不可信（防身份冒用 / 水平越权）；`assertSelfOrAdmin(reporter)` 守门。
- **逻辑删除**：全表统一 `deleted`（`0/1`），查询链路自动过滤；禁止物理删除业务行。
- **20 位 MDM 编码**：`FacDevice.deviceCode` 等中石化物理主键，固定 20 位，由 `/system/device-code` 解析，禁止自创。

## 3. 与契约的映射

实体出参一律经 DTO 转换（禁止直接序列化 Entity）。具名 DTO 与契约 schema 字段级对齐，由 `scripts/check-api-contract.mjs --strict` 守门（路由 0 漂移 / 24+ DTO 0 漂移）。
