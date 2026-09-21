# rescue-resource Specification

## Purpose

救援资源域（装备 / 人员 / 车辆 / 消防队伍）的唯一真源口径与端点契约。由 Change
`rescue-force-single-source`（V62）建立，取代「扁平台账 + 队伍体系子表」两套并行表。

## Requirements

### Requirement: 救援力量唯一真源

救援力量域（装备 / 人员 / 车辆）SHALL 以扁平资源台账 `fac_rescue_equipment` / `fac_rescue_personnel` /
`fac_rescue_vehicle` 为唯一真源；`fac_brigade_team` 仅作为「中队主表」保留；队伍体系子表
`fac_brigade_equipment` / `fac_brigade_person` / `fac_brigade_vehicle` SHALL 退役。

#### Scenario: 大屏与接口同源
- **WHEN** 消防大屏读取 `GET /fire-monitoring/rescue-forces`
- **THEN** 「救援人员 / 救援装备 / 救援车辆」分别为 `fac_rescue_personnel` / `fac_rescue_equipment` /
  `fac_rescue_vehicle` 的实时计数，与 `GET /rescue-resources/{personnel,equipment,vehicles}` 结果条数一致
- **AND** 「消防队伍」为 `fac_brigade_team` 的实时计数

#### Scenario: 队伍详情由扁平表归组
- **WHEN** 调用 `GET /rescue-resources/brigades[/{id}]`
- **THEN** 每个队伍的 `vehicles` / `personnel` / `equipment` 由扁平表按 `squadron == team_name` 归组得到
- **AND** 人员含 `group` / `phone` / `dutyStatus`，装备含 `category` / `unit`（由 V62 补充列提供）

#### Scenario: 业务总量取真实条数
- **WHEN** 调用 `GET /rescue-resources/equipment` 或 `/rescue-resources/personnel`
- **THEN** `totalSets` / `totalCount` 为对应台账的实时条数，SHALL NOT 使用任何硬编码常量（原 `375` 已移除）

#### Scenario: 应急力量「救援装备」与「应急物资」分离
- **WHEN** 调用 `GET /emergency/strength`
- **THEN** `kind == 救援装备` 的计数与明细取自 `fac_rescue_equipment`
- **AND** `kind == 应急物资` 为统计口径：计数沿用 `sys_emergency_strength` 人工维护值，`items` 为 null
