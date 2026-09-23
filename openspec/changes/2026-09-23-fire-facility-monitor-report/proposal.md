## Why

消防设施运行监测（fm-fire-facility「运行监控」页）展示的 12 张卡片与设备统计（总数/在线/离线/故障/完好率/在线率），目前仅来自 Flyway V20 种子，运行期无任何写入链路——`FireFacilityController` 对 `/monitors` 等全部为 GET，`fac_fire_facility_monitor` 与 `fac_fire_facility_param` 无写入口。这与已落地的应急指挥、消防报警「写回 + 实时」能力不一致，是「数据不落库」的设计缺口。

## What Changes

新增运行期上报写端点 `POST /api/v1/fire-facility/monitors/report`：设备/采集/模拟上报 → 按 `key_code` upsert `fac_fire_facility_monitor`（total/online/offline/fault/monitor_status/last_report_time）+ 整体替换 `fac_fire_facility_param`；返回刷新后的全量 `FireFacilityMonitorResult`。复用既有表列，**无 DDL、无新权限码、无新迁移 → 属 L3**（不触及 security/权限模型/DB 结构）。

## Impact

- 后端：新增 DTO（FireFacilityMonitorReportRequest / FireFacilityMonitorReportItem / FireFacilityMonitorReportParam）+ `FireFacilityService.reportMonitors` + 控制器端点（`perm=fire-facility:handle`，`@RealtimeSync(domain="fire-facility.monitor")`）。
- 前端：契约 `docs/api/fire-facility.openapi.json` 增 POST 路径与 3 个 schema；`services/fireFacility.ts` 增 `reportFireFacilityMonitors`；`npm run gen:api-types` 同步生成类型。
- 既有 GET 接口与表结构不变；校验失败走 B3 包络（`PARAM_INVALID=100`）。
