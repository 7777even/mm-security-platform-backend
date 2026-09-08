# Spec Delta: implement-remaining-contracts

## ADDED（新增后端实现，契约已在前端库存在）

- `POST /api/v1/alarms` → 创建应急事件（EmergencyEventPayload → AlarmItem），B3 包络
- `PUT /api/v1/alarms/{alarmId}` → 更新应急事件（不存在返回 data=null），B3 包络
- `DELETE /api/v1/alarms/{alarmId}` → 逻辑删除（data={ok:boolean}），B3 包络
- `GET /api/v1/dashboard/risk-heatmap` → 分区风险评分 RiskHeatItem[]，B3 包络
- `GET /api/v1/emergency/strength` → EmergencyStrength，B3 包络
- `GET /api/v1/emergency/closed-cases` → ClosedCaseList（fac_alarm CLOSED 聚合），B3 包络
- `GET /api/v1/emergency/duty` → DutyRoster，B3 包络
- `GET /api/v1/emergency/phones` → EmergencyPhoneBook，B3 包络
- `GET /api/v1/emergency/knowledge` → KnowledgeList，B3 包络
- `GET /api/v1/map/alarms` → GeoJSON FeatureCollection（报警点位），B3 包络
- `GET /api/v1/map/devices` → GeoJSON FeatureCollection（设备点位），B3 包络
- `POST /api/v1/audit/log` → 审计批量落库（fac_audit_log），B3 包络
- `POST /api/v1/field-reports` → 现场采集回传受理，204 No Content（非 B3，契约明示 bypass）

## MODIFIED

- `AlarmService`：新增 create/update/delete/nextAlarmId（扩写能力，不改动既有 page）
- `DashboardService`：新增 riskHeatmap（扩写能力，不改动既有的 overview/trend24h/workstations）

## REMOVED

（无；纯增量补齐契约债）
