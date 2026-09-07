# Proposal: implement-remaining-contracts

## Why

安全管控指挥系统后端脚手架的「契约技术债」经 `#17` 消化 `GET /api/v1/dashboard/alarm-trend` 后，仍剩 **13 项** 前端契约有 / 后端无（实现有/契约无 = 0）：

- alarm：`POST /alarms`、`PUT /alarms/{alarmId}`、`DELETE /alarms/{alarmId}`
- dashboard：`GET /api/v1/dashboard/risk-heatmap`
- emergency：`GET /emergency/{strength,closed-cases,duty,phones,knowledge}`（5）
- map：`GET /map/{alarms,devices}`（2）
- uplink：`POST /audit/log`、`POST /field-reports`（2）

契约真源已在前端库 `frontend-scaffold/docs/api/*.openapi.json`（API Contract First），后端仅为实现方，无需改写契约语义。用户已于 2026-09-07 经 AskUserQuestion 确认：**本地 dev 继续用 H2 内存库、代码 DB 无关**，本轮即按既有契约补齐这 13 项能力。

## What

按既有契约逐域补齐 Controller/Service/DTO（emergency/map/uplink 为新建控制器），并补零依赖单测。无下行控制写能力（仍守住 §3.1 零硬控红线）；uplink 属「只监不控」上行域。

### Capabilities

- alarm 应急事件 CRUD（已有 GET，补齐写链路）
- dashboard 风险热力图（分区真实风险评分）
- emergency 5 类应急资源只读聚合 / 参考配置
- map 报警 / 设备 GeoJSON 点位（WGS84）
- uplink 审计埋点 + 防爆手机现场采集回传（204）

## Impact

- 新增控制器：EmergencyController、MapController、UplinkController；扩展 AlarmController、DashboardController
- 新增 DTO 约 18 个；新增实体/表 `fac_audit_log`（uplink 审计落库）
- 数据库：仅 dev=`H2` 快照 `schema.sql` 新增 `fac_audit_log`；生产达梦迁移为延迟项（P3，Flyway 未引入）
- 错误码：复用既有 `DEVICE_CODE_INVALID(301)` / `PARAM_INVALID(100)`，无新增段
- 不改动 B3 包络、/api/v1 前缀、鉴权模型、硬控名单（不触 L4 门禁）

## 人工确认关卡

- [x] 本地 dev=H2、代码 DB 无关（用户 2026-09-07 已确认）
- [x] 13 项均为实现既有契约、不改契约语义（无 L4 语义变更）
- [x] uplink/field-reports 返回 204 且 bypass B3 包络（严格遵循前端契约描述，非自创）
