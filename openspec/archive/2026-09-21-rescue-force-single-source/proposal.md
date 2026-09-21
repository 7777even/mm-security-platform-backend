# 救援力量唯一真源（扁平资源台账为准 · 退役队伍子表）

## Why

「应急救援资源」域存在两套并行表，说的是**同一批 8 个中队**（乙烯/炼油/罐区/仓储/码头/芳烃/特勤一/特勤二），却各登记了一份装备/人员/车辆：

- 扁平资源台账：`fac_rescue_equipment`(35) / `fac_rescue_personnel`(52) / `fac_rescue_vehicle`(12)
- 队伍体系子表：`fac_brigade_equipment`(71) / `fac_brigade_person`(110) / `fac_brigade_vehicle`(39)

**根因**：V19 把前端 4 个互不相干的 mock（`rescueEquipmentMock` / `rescuePersonnelMock` / `rescueVehicleMock` / `fireBrigadeMock`）各自独立落库，未先合并「中队」主数据。

**后果**：消防大屏「消防救援力量」与 `GET /rescue-resources/*`、应急面板「应急救援力量」数字长期不一致（装备 71 vs 35、人员 110 vs 52、车辆 39 vs 12）；「救援装备」一名对应两张表；且队伍详情/大屏浮层曾各写死一个 mock 常量（`EQUIPMENT_TOTAL_SETS=375` / `PERSONNEL_TOTAL_COUNT=375`）。

## What

统一为**扁平资源台账唯一真源**：

1. 装备 / 人员 / 车辆一律取 `fac_rescue_{equipment,personnel,vehicle}`；`fac_brigade_team` 仅保留为「中队主表」。
2. 退役 `fac_brigade_{equipment,person,vehicle}`（V62 三方言 DROP）。
3. 给扁平表补「队伍详情」分组所需字段：`fac_rescue_personnel` +`person_group`/`phone`/`duty_status`；`fac_rescue_equipment` +`category`/`unit`（原仅存在于队伍子表）。
4. `/rescue-resources/brigades` 队伍详情的车辆/人员/装备改由扁平表**按中队名归组**；消防大屏 `rescueForces()` 改取扁平表计数。
5. 应急面板「装备车辆」更名「**救援装备**」并接 `fac_rescue_equipment`；「应急物资」脱离装备台账、回落人工统计口径（V8 的 3510，点击提示统计口径）。

## Capabilities

- `rescue-resource`（新增 capability：救援资源域唯一真源口径）

## Impact

- **DB（L4）**：V62 三方言 —— `ADD COLUMN`×5 / `UPDATE` 回填 / `DROP TABLE`×3 / `sys_emergency_strength` kind 改名。
- **契约**：`docs/api/emergency.openapi.json`（枚举 装备车辆→救援装备 + items description）；`docs/api/fire-monitoring.openapi.json`（示例值）。
- **后端**：`RescueResourceService`、`FireMonitoringService`、`EmergencyService`；删除 3 个已退役实体 + 3 个 mapper。
- **前端**：`services/emergency.ts`（类型/fixture）、`SectorEmergencyCommand.vue`（映射键）、`EmergencyRescuePanel.vue`（注释）、`types/generated/emergency.ts`（重生成）。
- **数字变化（预期）**：大屏 救援人员 110→52 / 救援装备 71→35 / 救援车辆 39→12；队伍详情每队明细随之变少。

## 人工确认关卡

| 关卡 | 状态 |
| ---- | ---- |
| 库表结构变更（ADD COLUMN / DROP TABLE / UPDATE） | ✅ 已确认（用户选「全量执行」） |
| 契约语义变更（枚举改名 + 数据源切换） | ✅ 已确认 |
| 三方言迁移 | ✅ 已确认 |
| 数字下滑（大屏/队伍详情）影响 | ✅ 已确认 |
