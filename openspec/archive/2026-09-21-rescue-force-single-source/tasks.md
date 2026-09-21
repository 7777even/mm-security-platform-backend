# 任务：救援力量唯一真源

## DB / 迁移（V62 三方言）

- [x] `fac_rescue_personnel` ADD COLUMN person_group / phone / duty_status（h2 · dameng · postgresql）
- [x] `fac_rescue_equipment` ADD COLUMN category / unit（三方）
- [x] 回填人员分组 / 联系方式 / 在岗状态（按 id 与 role 推导）
- [x] 回填装备类别 / 单位（按装备名 CASE 归类）
- [x] `sys_emergency_strength` 的 `装备车辆` → `救援装备`（含 count）
- [x] DROP `fac_brigade_equipment` / `fac_brigade_person` / `fac_brigade_vehicle`

## 后端

- [x] `FacRescuePersonnel` / `FacRescueEquipment` 实体补新字段
- [x] `RescueResourceService`：删 3 个队伍子表 mapper；`toBrigadeTeams` 改按中队从扁平表归组；`totalSets`/`totalCount` 改实时计数；删 375 常量
- [x] `FireMonitoringService.rescueForces()`：队伍=中队表、人员/装备/车辆=扁平台账
- [x] `EmergencyService`：`装备车辆`→`救援装备`（接装备台账）；`应急物资` 回落统计口径
- [x] 删除 `FacBrigade{Vehicle,Person,Equipment}` 实体 + mapper（6 文件）

## 契约 / 前端

- [x] `emergency.openapi.json`：枚举 装备车辆→救援装备 + items/description
- [x] `fire-monitoring.openapi.json`：消防救援力量示例值更新
- [x] 前端 `services/emergency.ts` 类型 + DEV_FIXTURE；`SectorEmergencyCommand.vue` 映射键；面板注释
- [x] `npm run gen:api-types` 重生成 `types/generated/emergency.ts`

## 测试 / 门禁

- [x] `EmergencyServiceTest`（装备车辆→救援装备、应急物资统计口径）· 22/22
- [x] `RescueResourceServiceTest`（队伍详情改扁平表归组、totalSets/Count 实时）· 10/10
- [x] `RescueResourceAbacTest` 与 `FireMonitoringServiceTest` 同步 mock
- [x] `check-dialect-migration-consistency.py --dialects h2,dameng,postgresql`
- [x] `check-api-contract.mjs --strict`
- [x] 全量 `mvn test`

## 收尾

- [x] 更新 `docs/system-facts.md`（救援力量唯一真源口径）
- [x] 归档本 Change 到 `openspec/archive/`
