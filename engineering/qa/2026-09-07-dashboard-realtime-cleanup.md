# QA · Dashboard / 实时推送 去 Random 清理（2026-09-07）

## 范围
清理 `DashboardService` / `DashboardController` 的硬编码 `Map` 汇总与 `AlarmSimulator` 的 `Random` 造数演示桩，
改为基于真实表聚合（`fac_device` / `fac_alarm` / `fac_workstation`）的逻辑。属 `harden-backend-baseline`
之后第三轮技术债清理，承接 #11（告警 mock 清理）、#14（401/403 真实状态码）、#15（告警字段对齐）。

## 改动
- 新增 `fac_workstation` 主数据表（`schema.sql`）+ 3 条种子（`data.sql`），替代原 `workstations()` 的硬编码 `Map` 列表
- 新增 `FacWorkstation` 实体 + `FacWorkstationMapper`
- `DashboardService.overview()`：
  - `deviceTotal` = `fac_device`(deleted=0) 计数
  - `deviceOnline` = `fac_device`(deleted=0, status=1) 计数（契约 0=离线 1=在线 2=告警）
  - `activeAlarm` = `fac_alarm`(deleted=0, status=0) 计数
  - `onlineWorkstation` = 工位列表 `online=true` 计数（与 `workstations()` 同源，保持一致）
  - `riskIndex` = 四舍五入 2 位：`activeAlarm*0.7 + offlineDevice*0.3`
  - `ts` = 计算时刻 `LocalDateTime.now()`
  - 返回强类型 `DashboardOverview`（与前端 `DashboardOverview` 字节级对齐）
- `DashboardService.workstations()`：返回 `List<Workstation>`（由 `FacWorkstation` 映射，id←workstation_id）
- `DashboardController`：`/overview` → `Result<DashboardOverview>`，`/workstations` → `Result<List<Workstation>>`
- `AlarmSimulator`：去除 `Random`；每 12s 轮询 `fac_alarm` 最新一条（occurred_at desc, id desc），
  经 `AlarmAssembler` 转 `AlarmItem` 广播，用 `lastPushedId` 去重，无新告警不推送
- `AlarmWebSocketHandler.broadcastAlarm` 入参由 `Object` 收为 `AlarmItem`（类型安全）

## 验证
- `mvn test`：**35 case 全绿**（新增 `DashboardServiceTest` 3 + `DashboardControllerTest` 2 + `AlarmSimulatorTest` 4；基线 26 → 35）
- `scripts/check-api-contract.mjs`：实现有/契约无 = 0，无新增漂移；`/dashboard/overview`、`/dashboard/workstations`、`/ws/alarm` 仍在对齐清单

## 残余债（已识别，待定范围）
1. **生产库在线迁移**：`fac_workstation` 等表目前仅 dev 用 H2 `schema.sql` 全量重建（不写 ALTER）；
   生产 MySQL 在线迁移需 Flyway/Liquibase（当前未引入，P3 债）。
2. **14 项"契约有/实现无"前瞻桩**：alarm CRUD（POST/PUT/DELETE）、`alarm-trend`、`risk-heatmap`、
   emergency/map/uplink 系列、`gis` 外部网关。后端待实现，需各自独立 openspec Change 消化。
3. **`riskIndex` 公式口径**：当前为 `activeAlarm*0.7 + offlineDevice*0.3` 的透明加权，
   非风控模型产出；若前端期望 0–10 归一化或接入真实风险评估模型，需后续 Change 调整。

## 决策点（本轮已定）
- 工位数据源：新增 `fac_workstation` 真实主数据表（而非返回空列表），使态势页有真实数据可用。
- 设备在线口径：`status==1` 视为在线，与前端契约一致；`status==2`（告警）不计入 `deviceOnline`。
- 实时推送：采用"轮询最新告警 + 去重"而非 DB CDC；dev 环境足够，生产可升级为 binlog/触发器事件。
